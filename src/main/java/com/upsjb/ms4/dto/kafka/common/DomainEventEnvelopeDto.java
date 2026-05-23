// ruta: src/main/java/com/upsjb/ms4/dto/kafka/common/DomainEventEnvelopeDto.java
package com.upsjb.ms4.dto.kafka.common;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DomainEventEnvelopeDto<T>(
        UUID eventId,
        String eventType,
        String sourceService,
        String aggregateType,
        String aggregateId,

        @JsonProperty("schemaVersion")
        @JsonAlias({"eventVersion", "version"})
        Integer eventVersion,

        String producer,
        LocalDateTime occurredAt,
        String requestId,
        String correlationId,
        T payload,
        Map<String, Object> metadata
) {

    public Integer eventVersionSafe() {
        return eventVersion == null || eventVersion <= 0 ? 1 : eventVersion;
    }

    public String sourceServiceSafe() {
        if (sourceService != null && !sourceService.isBlank()) {
            return sourceService.trim();
        }

        if (producer != null && !producer.isBlank()) {
            return producer.trim();
        }

        return "unknown";
    }

    public String producerSafe() {
        if (producer != null && !producer.isBlank()) {
            return producer.trim();
        }

        return sourceServiceSafe();
    }

    public String aggregateIdSafe() {
        return aggregateId == null ? "" : aggregateId.trim();
    }

    public Map<String, Object> metadataSafe() {
        return metadata == null ? Map.of() : metadata;
    }
}