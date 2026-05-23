// ruta: src/main/java/com/upsjb/ms4/dto/pago/response/StripePaymentIntentResponseDto.java
package com.upsjb.ms4.dto.pago.response;

import com.upsjb.ms4.domain.enums.MetodoPago;
import java.math.BigDecimal;

public record StripePaymentIntentResponseDto(
        Long idVenta,
        Long idPago,
        String codigoPago,
        MetodoPago metodoPago,
        String paymentIntentId,
        String clientSecret,
        String publishableKey,
        String status,
        String moneda,
        BigDecimal monto
) {
}