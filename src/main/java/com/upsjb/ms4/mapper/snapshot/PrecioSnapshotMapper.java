// ruta: src/main/java/com/upsjb/ms4/mapper/snapshot/PrecioSnapshotMapper.java
package com.upsjb.ms4.mapper.snapshot;

import com.upsjb.ms4.domain.entity.snapshot.PrecioSnapshotMs3;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.dto.kafka.ms3.PrecioSnapshotPayloadDto;
import com.upsjb.ms4.dto.lookup.LookupItemResponseDto;
import com.upsjb.ms4.dto.snapshot.response.PrecioVentaResponseDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PrecioSnapshotMapper {

    public PrecioSnapshotMs3 toEntityFromPayload(
            PrecioSnapshotPayloadDto payload,
            DomainEventEnvelopeDto<?> envelope,
            String rawJson
    ) {
        if (payload == null || envelope == null) {
            return null;
        }

        PrecioSnapshotMs3 entity = new PrecioSnapshotMs3();
        updateFromPayload(entity, payload, envelope, rawJson);
        return entity;
    }

    public void updateFromPayload(
            PrecioSnapshotMs3 entity,
            PrecioSnapshotPayloadDto payload,
            DomainEventEnvelopeDto<?> envelope,
            String rawJson
    ) {
        if (entity == null || payload == null || envelope == null) {
            return;
        }

        entity.setIdPrecioHistorialMs3(payload.idPrecioHistorial());
        entity.setIdSkuMs3(payload.idSku());
        entity.setCodigoSku(trimToNull(payload.codigoSku()));
        entity.setIdProductoMs3(payload.idProducto());
        entity.setCodigoProducto(trimToNull(payload.codigoProducto()));
        entity.setNombreProducto(trimToNull(payload.nombreProducto()));
        entity.setPrecioVenta(payload.precioVenta());
        entity.setMoneda(trimToNull(payload.moneda()));
        entity.setSimboloMoneda(trimToNull(payload.simboloMoneda()));
        entity.setFechaInicio(payload.fechaInicio());
        entity.setFechaFin(payload.fechaFin());
        entity.setVigente(Boolean.TRUE.equals(payload.vigente()));
        entity.setMotivo(trimToNull(payload.motivo()));
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

        /*
         * El histórico de precios debe permanecer activo si MS3 lo conserva activo,
         * aunque vigente=false. Vigencia no es estado lógico.
         */
        entity.setEstado(activeOrDefault(payload.estado()));
    }

    public PrecioVentaResponseDto toResponse(PrecioSnapshotMs3 entity) {
        if (entity == null) {
            return null;
        }

        return new PrecioVentaResponseDto(
                entity.getId(),
                entity.getIdPrecioHistorialMs3(),
                entity.getIdSkuMs3(),
                entity.getCodigoSku(),
                entity.getIdProductoMs3(),
                entity.getCodigoProducto(),
                entity.getNombreProducto(),
                entity.getPrecioVenta(),
                entity.getMoneda(),
                entity.getSimboloMoneda(),
                entity.getFechaInicio(),
                entity.getFechaFin(),
                entity.getVigente(),
                entity.getMotivo(),
                entity.getCreadoPorIdUsuarioMs1(),
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

    public LookupItemResponseDto toLookup(PrecioSnapshotMs3 entity) {
        if (entity == null) {
            return null;
        }

        return new LookupItemResponseDto(
                entity.getId(),
                entity.getCodigoSku(),
                buildLabel(entity),
                entity.getMotivo(),
                entity.getEstado()
        );
    }

    private String buildLabel(PrecioSnapshotMs3 entity) {
        String nombre = trimToNull(entity.getNombreProducto());
        String precio = entity.getPrecioVenta() == null ? "SIN_PRECIO" : entity.getPrecioVenta().toPlainString();
        String moneda = trimToNull(entity.getMoneda());

        if (nombre == null) {
            return precio + (moneda == null ? "" : " " + moneda);
        }

        return nombre + " - " + precio + (moneda == null ? "" : " " + moneda);
    }

    private Boolean activeOrDefault(Boolean value) {
        return value == null || Boolean.TRUE.equals(value);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}