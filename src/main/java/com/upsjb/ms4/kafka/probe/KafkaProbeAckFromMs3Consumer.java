package com.upsjb.ms4.kafka.probe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaProbeAckFromMs3Consumer {

    private static final Logger log =
            LoggerFactory.getLogger(
                    KafkaProbeAckFromMs3Consumer.class
            );

    private final ObjectMapper objectMapper;
    private final KafkaProbeRegistry registry;

    public KafkaProbeAckFromMs3Consumer(
            ObjectMapper objectMapper,
            KafkaProbeRegistry registry
    ) {
        this.objectMapper =
                objectMapper;

        this.registry =
                registry;
    }

    @KafkaListener(
            topics = "${app.kafka.probe.topics.ms3-to-ms4-ack:dev.ms3.ms4.probe-ack.v1}",
            groupId = "${app.kafka.probe.consumer-group:ms4-probe-consumer}-ack-from-ms3",
            clientIdPrefix = "ms4-probe-ack-from-ms3",
            containerFactory = "kafkaProbeListenerContainerFactory"
    )
    public void consumeAckFromMs3(
            ConsumerRecord<String, String> record
    ) {
        if (!hasPayload(record)) {
            log.warn(
                    "[KAFKA-PROBE][MS4] ACK vacío recibido desde MS3. topic={}, partition={}, offset={}, key={}",
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

        KafkaProbeAckPayload ack =
                deserialize(record);

        if (ack == null) {
            return;
        }

        if (!ack.isOk()) {
            log.warn(
                    "[KAFKA-PROBE][MS4] ACK inválido recibido desde MS3. topic={}, partition={}, offset={}, key={}, payload={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key(),
                    record.value()
            );

            return;
        }

        registry.markAcked(
                ack.probeId(),
                "MS4_TO_MS3",
                record.topic(),
                record.key(),
                ack.message()
        );

        log.info(
                "[KAFKA-PROBE][MS4] ACK recibido desde MS3. probeId={}, topic={}, partition={}, offset={}, key={}, status={}",
                ack.probeId(),
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                ack.status()
        );
    }

    private KafkaProbeAckPayload deserialize(
            ConsumerRecord<String, String> record
    ) {
        try {
            return objectMapper.readValue(
                    record.value(),
                    KafkaProbeAckPayload.class
            );
        } catch (JsonProcessingException ex) {
            log.error(
                    "[KAFKA-PROBE][MS4] ACK con JSON inválido recibido desde MS3. topic={}, partition={}, offset={}, key={}, payload={}",
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