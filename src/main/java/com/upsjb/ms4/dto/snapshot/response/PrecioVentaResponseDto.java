// ruta: src/main/java/com/upsjb/ms4/dto/snapshot/response/PrecioVentaResponseDto.java
package com.upsjb.ms4.dto.snapshot.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PrecioVentaResponseDto(
        Long id,
        Long idPrecioHistorialMs3,
        Long idSkuMs3,
        String codigoSku,
        Long idProductoMs3,
        String codigoProducto,
        String nombreProducto,
        BigDecimal precioVenta,
        String moneda,
        String simboloMoneda,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        Boolean vigente,
        String motivo,
        Long creadoPorIdUsuarioMs1,
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