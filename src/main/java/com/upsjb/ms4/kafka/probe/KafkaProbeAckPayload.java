package com.upsjb.ms4.kafka.probe;

import java.time.Instant;

public record KafkaProbeAckPayload(
        String probeId,
        String sourceService,
        String targetService,
        String eventType,
        String direction,
        String receivedTopic,
        String receivedKey,
        Instant ackAt,
        String status,
        String message
) {

    public static KafkaProbeAckPayload ackToMs2(
            KafkaProbePayload received,
            String sourceService,
            String receivedTopic,
            String receivedKey
    ) {
        return new KafkaProbeAckPayload(
                received.probeId(),
                sourceService,
                received.sourceService(),
                "KAFKA_PROBE_ACK",
                "MS4_TO_MS2_ACK",
                receivedTopic,
                receivedKey,
                Instant.now(),
                "OK",
                "MS4 received probe from MS2 without persistence."
        );
    }

    public static KafkaProbeAckPayload ackToMs3(
            KafkaProbePayload received,
            String sourceService,
            String receivedTopic,
            String receivedKey
    ) {
        return new KafkaProbeAckPayload(
                received.probeId(),
                sourceService,
                received.sourceService(),
                "KAFKA_PROBE_ACK",
                "MS4_TO_MS3_ACK",
                receivedTopic,
                receivedKey,
                Instant.now(),
                "OK",
                "MS4 received probe from MS3 without persistence."
        );
    }

    public boolean isOk() {
        return "KAFKA_PROBE_ACK".equalsIgnoreCase(eventType)
                && "OK".equalsIgnoreCase(status)
                && probeId != null
                && !probeId.isBlank();
    }
}