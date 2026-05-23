// ruta: src/main/java/com/upsjb/ms4/mapper/snapshot/SkuSnapshotMapper.java
package com.upsjb.ms4.mapper.snapshot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms4.domain.entity.snapshot.SkuSnapshotMs3;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.dto.kafka.ms3.SkuSnapshotPayloadDto;
import com.upsjb.ms4.dto.lookup.SkuLookupResponseDto;
import com.upsjb.ms4.dto.snapshot.response.SkuVentaResponseDto;
import com.upsjb.ms4.shared.exception.KafkaPublishException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SkuSnapshotMapper {

    private final ObjectMapper objectMapper;

    public SkuSnapshotMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SkuSnapshotMs3 toEntityFromPayload(
            SkuSnapshotPayloadDto payload,
            DomainEventEnvelopeDto<?> envelope,
            String rawJson
    ) {
        if (payload == null || envelope == null) {
            return null;
        }

        SkuSnapshotMs3 entity = new SkuSnapshotMs3();
        updateFromPayload(entity, payload, envelope, rawJson);
        return entity;
    }

    public void updateFromPayload(
            SkuSnapshotMs3 entity,
            SkuSnapshotPayloadDto payload,
            DomainEventEnvelopeDto<?> envelope,
            String rawJson
    ) {
        if (entity == null || payload == null || envelope == null) {
            return;
        }

        entity.setIdSkuMs3(payload.idSku());
        entity.setIdProductoMs3(payload.idProducto());
        entity.setCodigoProducto(trimToNull(payload.codigoProducto()));
        entity.setCodigoSku(trimToNull(payload.codigoSku()));
        entity.setBarcode(trimToNull(payload.barcode()));
        entity.setColor(trimToNull(payload.color()));
        entity.setTalla(trimToNull(payload.talla()));
        entity.setMaterial(trimToNull(payload.material()));
        entity.setModelo(trimToNull(payload.modelo()));
        entity.setStockMinimo(payload.stockMinimo());
        entity.setStockMaximo(payload.stockMaximo());
        entity.setPesoGramos(payload.pesoGramos());
        entity.setAltoCm(payload.altoCm());
        entity.setAnchoCm(payload.anchoCm());
        entity.setLargoCm(payload.largoCm());
        entity.setEstadoSku(trimToNull(payload.estadoSku()));
        entity.setAtributosJson(toJson(payload.atributos()));

        entity.setEventId(envelope.eventId());
        entity.setEventType(envelope.eventType());
        entity.setAggregateId(resolveAggregateId(payload, envelope));
        entity.setEventVersion(envelope.eventVersionSafe());
        entity.setOccurredAt(envelope.occurredAt());
        entity.setRequestId(envelope.requestId());
        entity.setCorrelationId(envelope.correlationId());
        entity.setPayloadJson(rawJson);
        entity.setFechaSincronizacion(LocalDateTime.now());
        entity.setEstado(activeOrDefault(payload.estado()));
    }

    public SkuVentaResponseDto toResponse(SkuSnapshotMs3 entity) {
        if (entity == null) {
            return null;
        }

        return new SkuVentaResponseDto(
                entity.getId(),
                entity.getIdSkuMs3(),
                entity.getIdProductoMs3(),
                entity.getCodigoProducto(),
                entity.getCodigoSku(),
                entity.getBarcode(),
                entity.getColor(),
                entity.getTalla(),
                entity.getMaterial(),
                entity.getModelo(),
                entity.getStockMinimo(),
                entity.getStockMaximo(),
                entity.getPesoGramos(),
                entity.getAltoCm(),
                entity.getAnchoCm(),
                entity.getLargoCm(),
                entity.getEstadoSku(),
                entity.getAtributosJson(),
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

    public SkuLookupResponseDto toLookup(SkuSnapshotMs3 entity) {
        if (entity == null) {
            return null;
        }

        return new SkuLookupResponseDto(
                entity.getId(),
                entity.getIdSkuMs3(),
                entity.getIdProductoMs3(),
                entity.getCodigoProducto(),
                entity.getCodigoSku(),
                entity.getBarcode(),
                entity.getColor(),
                entity.getTalla(),
                entity.getMaterial(),
                entity.getModelo(),
                entity.getEstadoSku(),
                entity.getEstado()
        );
    }

    private String resolveAggregateId(SkuSnapshotPayloadDto payload, DomainEventEnvelopeDto<?> envelope) {
        if (payload.idSku() != null) {
            return String.valueOf(payload.idSku());
        }

        return envelope.aggregateIdSafe();
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
            throw new KafkaPublishException("No se pudo serializar atributos del snapshot de SKU MS3.", ex);
        }
    }
}