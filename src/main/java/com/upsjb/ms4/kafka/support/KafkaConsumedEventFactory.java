// ruta: src/main/java/com/upsjb/ms4/kafka/support/KafkaConsumedEventFactory.java
package com.upsjb.ms4.kafka.support;

import com.upsjb.ms4.domain.entity.kafka.KafkaEventoConsumido;
import com.upsjb.ms4.domain.enums.EstadoKafkaProcesamiento;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class KafkaConsumedEventFactory {

    public KafkaEventoConsumido processed(
            String topic,
            String key,
            Integer partition,
            Long offset,
            String rawJson,
            DomainEventEnvelopeDto<?> envelope
    ) {
        return build(
                topic,
                key,
                partition,
                offset,
                rawJson,
                envelope,
                EstadoKafkaProcesamiento.PROCESADO,
                null
        );
    }

    public KafkaEventoConsumido ignored(
            String topic,
            String key,
            Integer partition,
            Long offset,
            String rawJson,
            DomainEventEnvelopeDto<?> envelope,
            String reason
    ) {
        return build(
                topic,
                key,
                partition,
                offset,
                rawJson,
                envelope,
                EstadoKafkaProcesamiento.IGNORADO,
                reason
        );
    }

    public KafkaEventoConsumido error(
            String topic,
            String key,
            Integer partition,
            Long offset,
            String rawJson,
            DomainEventEnvelopeDto<?> envelope,
            String error
    ) {
        return build(
                topic,
                key,
                partition,
                offset,
                rawJson,
                envelope,
                EstadoKafkaProcesamiento.ERROR,
                error
        );
    }

    private KafkaEventoConsumido build(
            String topic,
            String key,
            Integer partition,
            Long offset,
            String rawJson,
            DomainEventEnvelopeDto<?> envelope,
            EstadoKafkaProcesamiento status,
            String detail
    ) {
        KafkaEventoConsumido entity = new KafkaEventoConsumido();
        entity.setSourceService(envelope.sourceServiceSafe());
        entity.setTopic(topic);
        entity.setEventKey(key);
        entity.setPartition(partition);
        entity.setOffset(offset);
        entity.setEventId(envelope.eventId());
        entity.setEventType(envelope.eventType());
        entity.setAggregateType(envelope.aggregateType());
        entity.setAggregateId(envelope.aggregateIdSafe());
        entity.setEventVersion(envelope.eventVersionSafe());
        entity.setProducer(envelope.producerSafe());
        entity.setOccurredAt(envelope.occurredAt());
        entity.setProcessedAt(LocalDateTime.now());
        entity.setEstadoProcesamiento(status);
        entity.setPayloadJson(rawJson);
        entity.setLastError(detail);
        return entity;
    }
}