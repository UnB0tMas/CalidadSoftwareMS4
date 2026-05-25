package com.upsjb.ms4.kafka.probe;

import java.time.Instant;
import java.util.Map;

public record KafkaProbePayload(
        String probeId,
        String sourceService,
        String targetService,
        String eventType,
        String direction,
        Instant sentAt,
        String message,
        Map<String, String> metadata
) {

    public static KafkaProbePayload ms4ToMs3(
            String probeId,
            String sourceService,
            String targetService
    ) {
        return new KafkaProbePayload(
                probeId,
                sourceService,
                targetService,
                "KAFKA_PROBE",
                "MS4_TO_MS3",
                Instant.now(),
                "probe-ms4-to-ms3",
                Map.of(
                        "persistence", "DISABLED",
                        "outbox", "DISABLED",
                        "businessValidations", "DISABLED",
                        "sourceRole", "PRODUCER_AND_CONSUMER",
                        "targetRole", "CONSUMER",
                        "purpose", "KAFKA_CONNECTIVITY_TEST"
                )
        );
    }

    public boolean valid() {
        return probeId != null
                && !probeId.isBlank()
                && "KAFKA_PROBE".equalsIgnoreCase(eventType)
                && direction != null
                && !direction.isBlank();
    }
}