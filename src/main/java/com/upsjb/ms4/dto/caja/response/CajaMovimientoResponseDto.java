// ruta: src/main/java/com/upsjb/ms4/dto/caja/response/CajaMovimientoResponseDto.java
package com.upsjb.ms4.dto.caja.response;

import com.upsjb.ms4.domain.enums.TipoMovimientoCaja;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CajaMovimientoResponseDto(
        Long id,
        Long idCaja,
        String codigoCaja,
        Long idVenta,
        String codigoVenta,
        Long idPago,
        String codigoPago,
        TipoMovimientoCaja tipoMovimiento,
        BigDecimal monto,
        String descripcion,
        Long actorIdUsuarioMs1,
        String actorRol,
        String requestId,
        String correlationId,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}