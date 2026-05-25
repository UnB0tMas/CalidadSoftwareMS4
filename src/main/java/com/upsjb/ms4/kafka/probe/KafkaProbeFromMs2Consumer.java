package com.upsjb.ms4.kafka.probe;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class KafkaProbeFromMs2Consumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaProbeFromMs2Consumer.class);

    private final ObjectMapper objectMapper;
    private final KafkaProbeProperties properties;
    private final KafkaProbePublisher publisher;
    private final KafkaProbeRegistry registry;

    public KafkaProbeFromMs2Consumer(
            ObjectMapper objectMapper,
            KafkaProbeProperties properties,
            KafkaProbePublisher publisher,
            KafkaProbeRegistry registry
    ) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.publisher = publisher;
        this.registry = registry;
    }

    @KafkaListener(
            topics = "${app.kafka.probe.topics.ms2-to-ms4:dev.ms2.ms4.probe.v1}",
            groupId = "${app.kafka.probe.consumer-group:ms4-probe-consumer}"
    )
    public void consumeProbeFromMs2(
            ConsumerRecord<String, String> record,
            Acknowledgment acknowledgment
    ) {
        try {
            if (record == null || record.value() == null || record.value().isBlank()) {
                log.warn("[KAFKA-PROBE][MS4] Probe vacío recibido desde MS2.");
                acknowledgment.acknowledge();
                return;
            }

            KafkaProbePayload probe = objectMapper.readValue(
                    record.value(),
                    KafkaProbePayload.class
            );

            if (!probe.valid()) {
                log.warn(
                        "[KAFKA-PROBE][MS4] Probe inválido recibido desde MS2. topic={}, partition={}, offset={}, key={}, payload={}",
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        record.key(),
                        record.value()
                );

                acknowledgment.acknowledge();
                return;
            }

            registry.markReceived(
                    probe.probeId(),
                    "MS2_TO_MS4",
                    record.topic(),
                    record.key(),
                    "MS4 received probe from MS2."
            );

            log.info(
                    "[KAFKA-PROBE][MS4] Probe recibido desde MS2. probeId={}, topic={}, partition={}, offset={}, key={}",
                    probe.probeId(),
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key()
            );

            KafkaProbeAckPayload ack = KafkaProbeAckPayload.ackToMs2(
                    probe,
                    properties.getServiceName(),
                    record.topic(),
                    record.key()
            );

            String ackTopic = properties.ms4ToMs2AckTopic();
            String ackKey = "probe-ack:" + probe.probeId();

            RecordMetadata metadata = publisher.publishAck(ack, ackTopic, ackKey);

            log.info(
                    "[KAFKA-PROBE][MS4] ACK enviado hacia MS2. probeId={}, topic={}, key={}, partition={}, offset={}",
                    probe.probeId(),
                    ackTopic,
                    ackKey,
                    metadata == null ? null : metadata.partition(),
                    metadata == null ? null : metadata.offset()
            );

            acknowledgment.acknowledge();
        } catch (Exception ex) {
            log.error(
                    "[KAFKA-PROBE][MS4] Error consumiendo probe desde MS2. topic={}, partition={}, offset={}, key={}",
                    record == null ? null : record.topic(),
                    record == null ? null : record.partition(),
                    record == null ? null : record.offset(),
                    record == null ? null : record.key(),
                    ex
            );

            acknowledgment.acknowledge();
        }
    }
}