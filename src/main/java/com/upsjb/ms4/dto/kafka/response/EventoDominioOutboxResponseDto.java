// ruta: src/main/java/com/upsjb/ms4/dto/kafka/response/EventoDominioOutboxResponseDto.java
package com.upsjb.ms4.dto.kafka.response;

import com.upsjb.ms4.domain.enums.EstadoOutbox;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventoDominioOutboxResponseDto(
        Long id,
        UUID eventId,
        String aggregateType,
        String aggregateId,
        String topic,
        String eventKey,
        String eventType,
        String payloadJson,
        EstadoOutbox status,
        Integer attempts,
        Integer maxAttempts,
        String lastError,
        String lockedBy,
        LocalDateTime lockedAt,
        LocalDateTime publishedAt,
        String requestId,
        String correlationId,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}