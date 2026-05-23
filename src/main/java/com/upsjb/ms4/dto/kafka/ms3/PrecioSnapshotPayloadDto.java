// ruta: src/main/java/com/upsjb/ms4/dto/kafka/ms3/PrecioSnapshotPayloadDto.java
package com.upsjb.ms4.dto.kafka.ms3;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PrecioSnapshotPayloadDto(
        Long idPrecioHistorial,
        Long idSku,
        String codigoSku,
        Long idProducto,
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
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}