// ruta: src/main/java/com/upsjb/ms4/dto/pago/filter/PagoFilterDto.java
package com.upsjb.ms4.dto.pago.filter;

import com.upsjb.ms4.domain.enums.EstadoPago;
import com.upsjb.ms4.domain.enums.MetodoPago;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record PagoFilterDto(

        @Size(max = 150, message = "El texto de búsqueda no debe superar 150 caracteres.")
        String search,

        @Positive(message = "El idVenta debe ser positivo.")
        Long idVenta,

        @Size(max = 80, message = "El código de pago no debe superar 80 caracteres.")
        String codigoPago,

        MetodoPago metodoPago,

        EstadoPago estadoPago,

        @Size(max = 120, message = "El PaymentIntent de Stripe no debe superar 120 caracteres.")
        String stripePaymentIntentId,

        Boolean estado,

        LocalDateTime fechaDesde,

        LocalDateTime fechaHasta
) {
}