// ruta: src/main/java/com/upsjb/ms4/mapper/kafka/EventoDominioOutboxMapper.java
package com.upsjb.ms4.mapper.kafka;

import com.upsjb.ms4.domain.entity.kafka.EventoDominioOutbox;
import com.upsjb.ms4.dto.kafka.response.EventoDominioOutboxResponseDto;
import org.springframework.stereotype.Component;

@Component
public class EventoDominioOutboxMapper {

    public EventoDominioOutboxResponseDto toResponse(EventoDominioOutbox entity) {
        if (entity == null) {
            return null;
        }

        return new EventoDominioOutboxResponseDto(
                entity.getId(),
                entity.getEventId(),
                entity.getAggregateType(),
                entity.getAggregateId(),
                entity.getTopic(),
                entity.getEventKey(),
                entity.getEventType(),
                entity.getPayloadJson(),
                entity.getStatus(),
                entity.getAttempts(),
                entity.getMaxAttempts(),
                entity.getLastError(),
                entity.getLockedBy(),
                entity.getLockedAt(),
                entity.getPublishedAt(),
                entity.getRequestId(),
                entity.getCorrelationId(),
                entity.getEstado(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}