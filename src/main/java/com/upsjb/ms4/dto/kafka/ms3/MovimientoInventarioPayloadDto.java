// ruta: src/main/java/com/upsjb/ms4/dto/kafka/ms3/MovimientoInventarioPayloadDto.java
package com.upsjb.ms4.dto.kafka.ms3;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoInventarioPayloadDto(
        Long idMovimiento,
        String codigoMovimiento,
        Long idSku,
        String codigoSku,
        Long idProducto,
        String codigoProducto,
        String nombreProducto,
        Long idAlmacen,
        String codigoAlmacen,
        String nombreAlmacen,
        Long idCompraDetalle,
        Long idReservaStock,
        String codigoReserva,
        String tipoMovimiento,
        String motivoMovimiento,
        Integer cantidad,
        BigDecimal costoUnitario,
        BigDecimal costoTotal,
        Integer stockAnterior,
        Integer stockNuevo,
        String referenciaTipo,
        String referenciaIdExterno,
        String observacion,
        Long actorIdUsuarioMs1,
        Long actorIdEmpleadoMs2,
        String actorRol,
        String requestId,
        String correlationId,
        String estadoMovimiento,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}