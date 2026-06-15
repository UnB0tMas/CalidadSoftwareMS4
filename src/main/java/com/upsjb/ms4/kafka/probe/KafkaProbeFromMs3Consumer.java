package com.upsjb.ms4.kafka.probe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaProbeFromMs3Consumer {

    private static final Logger log =
            LoggerFactory.getLogger(
                    KafkaProbeFromMs3Consumer.class
            );

    private final ObjectMapper objectMapper;
    private final KafkaProbeProperties properties;
    private final KafkaProbePublisher publisher;
    private final KafkaProbeRegistry registry;

    public KafkaProbeFromMs3Consumer(
            ObjectMapper objectMapper,
            KafkaProbeProperties properties,
            KafkaProbePublisher publisher,
            KafkaProbeRegistry registry
    ) {
        this.objectMapper =
                objectMapper;

        this.properties =
                properties;

        this.publisher =
                publisher;

        this.registry =
                registry;
    }

    @KafkaListener(
            topics = "${app.kafka.probe.topics.ms3-to-ms4:dev.ms3.ms4.probe.v1}",
            groupId = "${app.kafka.probe.consumer-group:ms4-probe-consumer}-from-ms3",
            clientIdPrefix = "ms4-probe-from-ms3",
            containerFactory = "kafkaProbeListenerContainerFactory"
    )
    public void consumeProbeFromMs3(
            ConsumerRecord<String, String> record
    ) {
        if (!hasPayload(record)) {
            log.warn(
                    "[KAFKA-PROBE][MS4] Probe vacío recibido desde MS3. topic={}, partition={}, offset={}, key={}",
                    record == null
                            ? null
                            : record.topic(),
                    record == null
                            ? null
                            : record.partition(),
                    record == null
                            ? null
                            : record.offset(),
                    record == null
                            ? null
                            : record.key()
            );

            return;
        }

        KafkaProbePayload probe =
                deserialize(record);

        if (probe == null) {
            return;
        }

        if (!probe.valid()) {
            log.warn(
                    "[KAFKA-PROBE][MS4] Probe inválido recibido desde MS3. topic={}, partition={}, offset={}, key={}, payload={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key(),
                    record.value()
            );

            return;
        }

        registry.markReceived(
                probe.probeId(),
                "MS3_TO_MS4",
                record.topic(),
                record.key(),
                "MS4 received probe from MS3."
        );

        log.info(
                "[KAFKA-PROBE][MS4] Probe recibido desde MS3. probeId={}, topic={}, partition={}, offset={}, key={}",
                probe.probeId(),
                record.topic(),
                record.partition(),
                record.offset(),
                record.key()
        );

        KafkaProbeAckPayload ack =
                KafkaProbeAckPayload.ackToMs3(
                        probe,
                        properties.getServiceName(),
                        record.topic(),
                        record.key()
                );

        String ackTopic =
                properties.ms4ToMs3AckTopic();

        String ackKey =
                "probe-ack:"
                        + probe.probeId();

        try {
            RecordMetadata metadata =
                    publisher.publishAck(
                            ack,
                            ackTopic,
                            ackKey
                    );

            log.info(
                    "[KAFKA-PROBE][MS4] ACK enviado hacia MS3. probeId={}, topic={}, key={}, partition={}, offset={}",
                    probe.probeId(),
                    ackTopic,
                    ackKey,
                    metadata == null
                            ? null
                            : metadata.partition(),
                    metadata == null
                            ? null
                            : metadata.offset()
            );
        } catch (RuntimeException ex) {
            log.error(
                    "[KAFKA-PROBE][MS4] No se pudo enviar el ACK hacia MS3. probeId={}, topic={}, key={}",
                    probe.probeId(),
                    ackTopic,
                    ackKey,
                    ex
            );

            throw ex;
        }
    }

    private KafkaProbePayload deserialize(
            ConsumerRecord<String, String> record
    ) {
        try {
            return objectMapper.readValue(
                    record.value(),
                    KafkaProbePayload.class
            );
        } catch (JsonProcessingException ex) {
            log.error(
                    "[KAFKA-PROBE][MS4] Probe con JSON inválido recibido desde MS3. topic={}, partition={}, offset={}, key={}, payload={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key(),
                    record.value(),
                    ex
            );

            return null;
        }
    }

    private boolean hasPayload(
            ConsumerRecord<String, String> record
    ) {
        return record != null
                && record.value() != null
                && !record.value().isBlank();
    }
}