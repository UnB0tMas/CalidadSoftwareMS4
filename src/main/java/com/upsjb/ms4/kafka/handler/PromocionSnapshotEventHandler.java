// ruta: src/main/java/com/upsjb/ms4/kafka/handler/PromocionSnapshotEventHandler.java
package com.upsjb.ms4.kafka.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms4.domain.entity.snapshot.PromocionSkuDescuentoSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.PromocionSnapshotMs3;
import com.upsjb.ms4.domain.enums.TipoDescuento;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.dto.kafka.ms3.PromocionSkuDescuentoPayloadDto;
import com.upsjb.ms4.dto.kafka.ms3.PromocionSnapshotPayloadDto;
import com.upsjb.ms4.kafka.support.KafkaConsumedEventFactory;
import com.upsjb.ms4.kafka.support.KafkaEnvelopeReader;
import com.upsjb.ms4.repository.KafkaEventoConsumidoRepository;
import com.upsjb.ms4.repository.PromocionSkuDescuentoSnapshotMs3Repository;
import com.upsjb.ms4.repository.PromocionSnapshotMs3Repository;
import com.upsjb.ms4.shared.exception.KafkaPublishException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PromocionSnapshotEventHandler {

    private final KafkaEnvelopeReader envelopeReader;
    private final KafkaConsumedEventFactory consumedEventFactory;
    private final ObjectMapper objectMapper;
    private final PromocionSnapshotMs3Repository promocionRepository;
    private final PromocionSkuDescuentoSnapshotMs3Repository descuentoRepository;
    private final KafkaEventoConsumidoRepository consumedRepository;

    public PromocionSnapshotEventHandler(
            KafkaEnvelopeReader envelopeReader,
            KafkaConsumedEventFactory consumedEventFactory,
            ObjectMapper objectMapper,
            PromocionSnapshotMs3Repository promocionRepository,
            PromocionSkuDescuentoSnapshotMs3Repository descuentoRepository,
            KafkaEventoConsumidoRepository consumedRepository
    ) {
        this.envelopeReader = envelopeReader;
        this.consumedEventFactory = consumedEventFactory;
        this.objectMapper = objectMapper;
        this.promocionRepository = promocionRepository;
        this.descuentoRepository = descuentoRepository;
        this.consumedRepository = consumedRepository;
    }

    @Transactional
    public void handle(String topic, String key, Integer partition, Long offset, String rawJson) {
        DomainEventEnvelopeDto<PromocionSnapshotPayloadDto> envelope =
                envelopeReader.read(rawJson, PromocionSnapshotPayloadDto.class);

        if (alreadyConsumed(topic, partition, offset, envelope)) {
            return;
        }

        PromocionSnapshotPayloadDto payload = envelope.payload();
        validate(payload);

        PromocionSnapshotMs3 entity = promocionRepository
                .findByIdPromocionVersionMs3(payload.idPromocionVersion())
                .orElseGet(PromocionSnapshotMs3::new);

        entity.setIdPromocionMs3(payload.idPromocion());
        entity.setCodigoPromocion(payload.codigo());
        entity.setNombre(payload.nombre());
        entity.setDescripcion(payload.descripcion());
        entity.setCreadoPorIdUsuarioMs1(payload.creadoPorIdUsuarioMs1());
        entity.setIdPromocionVersionMs3(payload.idPromocionVersion());
        entity.setFechaInicio(payload.fechaInicio());
        entity.setFechaFin(payload.fechaFin());
        entity.setEstadoPromocion(payload.estadoPromocion());
        entity.setVisiblePublico(Boolean.TRUE.equals(payload.visiblePublico()));
        entity.setVigente(Boolean.TRUE.equals(payload.vigente()));
        entity.setMotivo(payload.motivo());
        entity.setDescuentosJson(toJson(payload.descuentos()));
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

        promocionRepository.save(entity);
        upsertDescuentos(payload.descuentos());
        consumedRepository.save(consumedEventFactory.processed(topic, key, partition, offset, rawJson, envelope));
    }

    private void upsertDescuentos(List<PromocionSkuDescuentoPayloadDto> descuentos) {
        if (descuentos == null || descuentos.isEmpty()) {
            return;
        }

        for (PromocionSkuDescuentoPayloadDto payload : descuentos) {
            if (payload == null || payload.idPromocionSkuDescuentoVersion() == null) {
                continue;
            }

            PromocionSkuDescuentoSnapshotMs3 entity = descuentoRepository
                    .findByIdPromocionSkuDescuentoVersionMs3(payload.idPromocionSkuDescuentoVersion())
                    .orElseGet(PromocionSkuDescuentoSnapshotMs3::new);

            entity.setIdPromocionSkuDescuentoVersionMs3(payload.idPromocionSkuDescuentoVersion());
            entity.setIdPromocionVersionMs3(payload.idPromocionVersion());
            entity.setIdPromocionMs3(payload.idPromocion());
            entity.setIdSkuMs3(payload.idSku());
            entity.setCodigoSku(payload.codigoSku());
            entity.setIdProductoMs3(payload.idProducto());
            entity.setCodigoProducto(payload.codigoProducto());
            entity.setNombreProducto(payload.nombreProducto());
            entity.setTipoDescuento(parseTipoDescuento(payload.tipoDescuento()));
            entity.setValorDescuento(payload.valorDescuento());
            entity.setPrecioFinalEstimado(payload.precioFinalEstimado());
            entity.setMargenEstimado(payload.margenEstimado());
            entity.setLimiteUnidades(payload.limiteUnidades());
            entity.setPrioridad(payload.prioridad());
            entity.setPayloadJson(toJson(payload));
            entity.setEstado(activeOrDefault(payload.estado()));

            descuentoRepository.save(entity);
        }
    }

    private boolean alreadyConsumed(String topic, Integer partition, Long offset, DomainEventEnvelopeDto<?> envelope) {
        return consumedRepository.existsByEventId(envelope.eventId())
                || consumedRepository.existsByTopicAndPartitionAndOffset(topic, partition, offset);
    }

    private void validate(PromocionSnapshotPayloadDto payload) {
        if (payload == null || payload.idPromocionVersion() == null || payload.idPromocion() == null) {
            throw new KafkaPublishException("El snapshot de promoción MS3 no contiene identificadores obligatorios.");
        }
    }

    private TipoDescuento parseTipoDescuento(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return TipoDescuento.fromCode(value);
    }

    private Boolean activeOrDefault(Boolean estado) {
        return estado == null || Boolean.TRUE.equals(estado);
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new KafkaPublishException("No se pudo serializar descuento de promoción MS3.", ex);
        }
    }
}