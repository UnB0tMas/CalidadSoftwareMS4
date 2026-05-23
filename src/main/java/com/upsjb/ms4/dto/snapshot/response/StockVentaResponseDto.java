// ruta: src/main/java/com/upsjb/ms4/dto/snapshot/response/StockVentaResponseDto.java
package com.upsjb.ms4.dto.snapshot.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockVentaResponseDto(
        Long id,
        Long idStockMs3,
        Long idSkuMs3,
        String codigoSku,
        String barcode,
        Long idProductoMs3,
        String codigoProducto,
        String nombreProducto,
        Long idAlmacenMs3,
        String codigoAlmacen,
        String nombreAlmacen,
        Integer stockFisico,
        Integer stockReservado,
        Integer stockDisponible,
        Integer stockMinimo,
        Integer stockMaximo,
        BigDecimal costoPromedioActual,
        BigDecimal ultimoCostoCompra,
        Boolean bajoStock,
        Boolean sobreStock,
        UUID eventId,
        String eventType,
        String aggregateId,
        Integer eventVersion,
        LocalDateTime occurredAt,
        LocalDateTime fechaSincronizacion,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}