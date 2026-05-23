// ruta: src/main/java/com/upsjb/ms4/kafka/handler/PrecioSnapshotEventHandler.java
package com.upsjb.ms4.kafka.handler;

import com.upsjb.ms4.domain.entity.snapshot.PrecioSnapshotMs3;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.dto.kafka.ms3.PrecioSnapshotPayloadDto;
import com.upsjb.ms4.kafka.support.KafkaConsumedEventFactory;
import com.upsjb.ms4.kafka.support.KafkaEnvelopeReader;
import com.upsjb.ms4.repository.KafkaEventoConsumidoRepository;
import com.upsjb.ms4.repository.PrecioSnapshotMs3Repository;
import com.upsjb.ms4.shared.exception.KafkaPublishException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class PrecioSnapshotEventHandler {

    private final KafkaEnvelopeReader envelopeReader;
    private final KafkaConsumedEventFactory consumedEventFactory;
    private final PrecioSnapshotMs3Repository repository;
    private final KafkaEventoConsumidoRepository consumedRepository;

    public PrecioSnapshotEventHandler(
            KafkaEnvelopeReader envelopeReader,
            KafkaConsumedEventFactory consumedEventFactory,
            PrecioSnapshotMs3Repository repository,
            KafkaEventoConsumidoRepository consumedRepository
    ) {
        this.envelopeReader = envelopeReader;
        this.consumedEventFactory = consumedEventFactory;
        this.repository = repository;
        this.consumedRepository = consumedRepository;
    }

    @Transactional
    public void handle(String topic, String key, Integer partition, Long offset, String rawJson) {
        DomainEventEnvelopeDto<PrecioSnapshotPayloadDto> envelope =
                envelopeReader.read(rawJson, PrecioSnapshotPayloadDto.class);

        if (alreadyConsumed(topic, partition, offset, envelope)) {
            return;
        }

        PrecioSnapshotPayloadDto payload = envelope.payload();
        validate(payload);

        PrecioSnapshotMs3 entity = repository.findByIdPrecioHistorialMs3(payload.idPrecioHistorial())
                .orElseGet(PrecioSnapshotMs3::new);

        entity.setIdPrecioHistorialMs3(payload.idPrecioHistorial());
        entity.setIdSkuMs3(payload.idSku());
        entity.setCodigoSku(payload.codigoSku());
        entity.setIdProductoMs3(payload.idProducto());
        entity.setCodigoProducto(payload.codigoProducto());
        entity.setNombreProducto(payload.nombreProducto());
        entity.setPrecioVenta(payload.precioVenta());
        entity.setMoneda(payload.moneda());
        entity.setSimboloMoneda(payload.simboloMoneda());
        entity.setFechaInicio(payload.fechaInicio());
        entity.setFechaFin(payload.fechaFin());
        entity.setVigente(Boolean.TRUE.equals(payload.vigente()));
        entity.setMotivo(payload.motivo());
        entity.setCreadoPorIdUsuarioMs1(payload.creadoPorIdUsuarioMs1());
        entity.setEventId(envelope.eventId());
        entity.setEventType(envelope.eventType());
        entity.setAggregateId(envelope.aggregateIdSafe());
        entity.setEventVersion(envelope.eventVersionSafe());
        entity.setOccurredAt(envelope.occurredAt());
        entity.setRequestId(envelope.requestId());
        entity.setCorrelationId(envelope.correlationId());
        entity.setPayloadJson(rawJson);
        entity.setFechaSincronizacion(LocalDateTime.now());
        entity.setEstado(activeOrDefault(payload.estado()));

        repository.save(entity);
        consumedRepository.save(consumedEventFactory.processed(topic, key, partition, offset, rawJson, envelope));
    }

    private boolean alreadyConsumed(String topic, Integer partition, Long offset, DomainEventEnvelopeDto<?> envelope) {
        return consumedRepository.existsByEventId(envelope.eventId())
                || consumedRepository.existsByTopicAndPartitionAndOffset(topic, partition, offset);
    }

    private void validate(PrecioSnapshotPayloadDto payload) {
        if (payload == null || payload.idPrecioHistorial() == null || payload.idSku() == null) {
            throw new KafkaPublishException("El snapshot de precio MS3 no contiene identificadores obligatorios.");
        }

        if (payload.precioVenta() == null) {
            throw new KafkaPublishException("El snapshot de precio MS3 no contiene precioVenta.");
        }
    }

    private Boolean activeOrDefault(Boolean estado) {
        return estado == null || Boolean.TRUE.equals(estado);
    }
}