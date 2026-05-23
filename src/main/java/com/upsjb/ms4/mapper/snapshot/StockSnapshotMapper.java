// ruta: src/main/java/com/upsjb/ms4/mapper/snapshot/StockSnapshotMapper.java
package com.upsjb.ms4.mapper.snapshot;

import com.upsjb.ms4.domain.entity.snapshot.StockSnapshotMs3;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.dto.kafka.ms3.StockSnapshotPayloadDto;
import com.upsjb.ms4.dto.lookup.AlmacenLookupResponseDto;
import com.upsjb.ms4.dto.snapshot.response.StockVentaResponseDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class StockSnapshotMapper {

    public StockSnapshotMs3 toEntityFromPayload(
            StockSnapshotPayloadDto payload,
            DomainEventEnvelopeDto<?> envelope,
            String rawJson
    ) {
        if (payload == null || envelope == null) {
            return null;
        }

        StockSnapshotMs3 entity = new StockSnapshotMs3();
        updateFromPayload(entity, payload, envelope, rawJson);
        return entity;
    }

    public void updateFromPayload(
            StockSnapshotMs3 entity,
            StockSnapshotPayloadDto payload,
            DomainEventEnvelopeDto<?> envelope,
            String rawJson
    ) {
        if (entity == null || payload == null || envelope == null) {
            return;
        }

        entity.setIdStockMs3(payload.idStock());
        entity.setIdSkuMs3(payload.idSku());
        entity.setCodigoSku(trimToNull(payload.codigoSku()));
        entity.setBarcode(trimToNull(payload.barcode()));

        entity.setIdProductoMs3(payload.idProducto());
        entity.setCodigoProducto(trimToNull(payload.codigoProducto()));
        entity.setNombreProducto(trimToNull(payload.nombreProducto()));

        entity.setIdAlmacenMs3(payload.idAlmacen());
        entity.setCodigoAlmacen(trimToNull(payload.codigoAlmacen()));
        entity.setNombreAlmacen(trimToNull(payload.nombreAlmacen()));

        entity.setStockFisico(numberOrZero(payload.stockFisico()));
        entity.setStockReservado(numberOrZero(payload.stockReservado()));
        entity.setStockDisponible(numberOrZero(payload.stockDisponible()));
        entity.setStockMinimo(payload.stockMinimo());
        entity.setStockMaximo(payload.stockMaximo());

        entity.setCostoPromedioActual(payload.costoPromedioActual());
        entity.setUltimoCostoCompra(payload.ultimoCostoCompra());
        entity.setBajoStock(Boolean.TRUE.equals(payload.bajoStock()));
        entity.setSobreStock(Boolean.TRUE.equals(payload.sobreStock()));

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
    }

    public StockVentaResponseDto toResponse(StockSnapshotMs3 entity) {
        if (entity == null) {
            return null;
        }

        return new StockVentaResponseDto(
                entity.getId(),
                entity.getIdStockMs3(),
                entity.getIdSkuMs3(),
                entity.getCodigoSku(),
                entity.getBarcode(),
                entity.getIdProductoMs3(),
                entity.getCodigoProducto(),
                entity.getNombreProducto(),
                entity.getIdAlmacenMs3(),
                entity.getCodigoAlmacen(),
                entity.getNombreAlmacen(),
                entity.getStockFisico(),
                entity.getStockReservado(),
                entity.getStockDisponible(),
                entity.getStockMinimo(),
                entity.getStockMaximo(),
                entity.getCostoPromedioActual(),
                entity.getUltimoCostoCompra(),
                entity.getBajoStock(),
                entity.getSobreStock(),
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

    public AlmacenLookupResponseDto toAlmacenLookup(StockSnapshotMs3 entity) {
        if (entity == null) {
            return null;
        }

        return new AlmacenLookupResponseDto(
                entity.getIdAlmacenMs3(),
                entity.getCodigoAlmacen(),
                entity.getNombreAlmacen(),
                Boolean.TRUE.equals(entity.getEstado())
        );
    }

    private Integer numberOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private Boolean activeOrDefault(Boolean value) {
        return value == null || Boolean.TRUE.equals(value);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}