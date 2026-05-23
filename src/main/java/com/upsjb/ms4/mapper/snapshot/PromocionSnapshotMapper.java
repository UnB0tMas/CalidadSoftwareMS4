// ruta: src/main/java/com/upsjb/ms4/mapper/snapshot/PromocionSnapshotMapper.java
package com.upsjb.ms4.mapper.snapshot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms4.domain.entity.snapshot.PromocionSkuDescuentoSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.PromocionSnapshotMs3;
import com.upsjb.ms4.domain.enums.TipoDescuento;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.dto.kafka.ms3.PromocionSkuDescuentoPayloadDto;
import com.upsjb.ms4.dto.kafka.ms3.PromocionSnapshotPayloadDto;
import com.upsjb.ms4.dto.lookup.LookupItemResponseDto;
import com.upsjb.ms4.dto.snapshot.response.PromocionVentaResponseDto;
import com.upsjb.ms4.shared.exception.KafkaPublishException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PromocionSnapshotMapper {

    private final ObjectMapper objectMapper;

    public PromocionSnapshotMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PromocionSnapshotMs3 toEntityFromPayload(
            PromocionSnapshotPayloadDto payload,
            DomainEventEnvelopeDto<?> envelope,
            String rawJson
    ) {
        if (payload == null || envelope == null) {
            return null;
        }

        PromocionSnapshotMs3 entity = new PromocionSnapshotMs3();
        updateFromPayload(entity, payload, envelope, rawJson);
        return entity;
    }

    public void updateFromPayload(
            PromocionSnapshotMs3 entity,
            PromocionSnapshotPayloadDto payload,
            DomainEventEnvelopeDto<?> envelope,
            String rawJson
    ) {
        if (entity == null || payload == null || envelope == null) {
            return;
        }

        entity.setIdPromocionMs3(payload.idPromocion());
        entity.setCodigoPromocion(trimToNull(payload.codigo()));
        entity.setNombre(trimToNull(payload.nombre()));
        entity.setDescripcion(trimToNull(payload.descripcion()));
        entity.setCreadoPorIdUsuarioMs1(payload.creadoPorIdUsuarioMs1());
        entity.setIdPromocionVersionMs3(payload.idPromocionVersion());
        entity.setFechaInicio(payload.fechaInicio());
        entity.setFechaFin(payload.fechaFin());
        entity.setEstadoPromocion(trimToNull(payload.estadoPromocion()));
        entity.setVisiblePublico(Boolean.TRUE.equals(payload.visiblePublico()));
        entity.setVigente(Boolean.TRUE.equals(payload.vigente()));
        entity.setMotivo(trimToNull(payload.motivo()));
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

        /*
         * Estado lógico independiente de vigente/visiblePublico.
         * Una promoción vencida no debe borrarse ni desactivarse como snapshot histórico.
         */
        entity.setEstado(activeOrDefault(payload.estado()));
    }

    public PromocionSkuDescuentoSnapshotMs3 toDescuentoEntityFromPayload(PromocionSkuDescuentoPayloadDto payload) {
        if (payload == null) {
            return null;
        }

        PromocionSkuDescuentoSnapshotMs3 entity = new PromocionSkuDescuentoSnapshotMs3();
        updateDescuentoFromPayload(entity, payload);
        return entity;
    }

    public void updateDescuentoFromPayload(
            PromocionSkuDescuentoSnapshotMs3 entity,
            PromocionSkuDescuentoPayloadDto payload
    ) {
        if (entity == null || payload == null) {
            return;
        }

        entity.setIdPromocionSkuDescuentoVersionMs3(payload.idPromocionSkuDescuentoVersion());
        entity.setIdPromocionVersionMs3(payload.idPromocionVersion());
        entity.setIdPromocionMs3(payload.idPromocion());
        entity.setIdSkuMs3(payload.idSku());
        entity.setCodigoSku(trimToNull(payload.codigoSku()));
        entity.setIdProductoMs3(payload.idProducto());
        entity.setCodigoProducto(trimToNull(payload.codigoProducto()));
        entity.setNombreProducto(trimToNull(payload.nombreProducto()));
        entity.setTipoDescuento(parseTipoDescuento(payload.tipoDescuento()));
        entity.setValorDescuento(payload.valorDescuento());
        entity.setPrecioFinalEstimado(payload.precioFinalEstimado());
        entity.setMargenEstimado(payload.margenEstimado());
        entity.setLimiteUnidades(payload.limiteUnidades());
        entity.setPrioridad(payload.prioridad());
        entity.setPayloadJson(toJson(payload));
        entity.setEstado(activeOrDefault(payload.estado()));
    }

    public PromocionVentaResponseDto toResponse(PromocionSnapshotMs3 entity) {
        if (entity == null) {
            return null;
        }

        return new PromocionVentaResponseDto(
                entity.getId(),
                entity.getIdPromocionMs3(),
                entity.getCodigoPromocion(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getCreadoPorIdUsuarioMs1(),
                entity.getIdPromocionVersionMs3(),
                entity.getFechaInicio(),
                entity.getFechaFin(),
                entity.getEstadoPromocion(),
                entity.getVisiblePublico(),
                entity.getVigente(),
                entity.getMotivo(),
                entity.getDescuentosJson(),
                entity.getEventId(),
                entity.getEventType(),
                entity.getAggregateId(),
                entity.getEventVersion(),
                entity.getOccurredAt(),
                entity.getFechaSincronizacion(),
                entity.getEstado(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public LookupItemResponseDto toLookup(PromocionSnapshotMs3 entity) {
        if (entity == null) {
            return null;
        }

        return new LookupItemResponseDto(
                entity.getId(),
                entity.getCodigoPromocion(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getEstado()
        );
    }

    private TipoDescuento parseTipoDescuento(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : TipoDescuento.fromCode(normalized);
    }

    private Boolean activeOrDefault(Boolean value) {
        return value == null || Boolean.TRUE.equals(value);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new KafkaPublishException("No se pudo serializar una sección del snapshot de promoción MS3.", ex);
        }
    }
}