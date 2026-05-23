// ruta: src/main/java/com/upsjb/ms4/dto/pago/response/PagoResponseDto.java
package com.upsjb.ms4.dto.pago.response;

import com.upsjb.ms4.domain.enums.EstadoPago;
import com.upsjb.ms4.domain.enums.MetodoPago;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoResponseDto(
        Long id,
        Long idVenta,
        String codigoPago,
        MetodoPago metodoPago,
        EstadoPago estadoPago,
        String moneda,
        BigDecimal monto,
        String stripePaymentIntentId,
        String stripeChargeId,
        String stripeStatus,
        LocalDateTime fechaPago,
        LocalDateTime fechaConfirmacion,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}