// ruta: src/main/java/com/upsjb/ms4/dto/pago/filter/StripeEventoFilterDto.java
package com.upsjb.ms4.dto.pago.filter;

import com.upsjb.ms4.domain.enums.EstadoKafkaProcesamiento;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record StripeEventoFilterDto(

        @Size(max = 150, message = "El texto de búsqueda no debe superar 150 caracteres.")
        String search,

        @Size(max = 120, message = "El id del evento Stripe no debe superar 120 caracteres.")
        String stripeEventId,

        @Size(max = 120, message = "El tipo de evento Stripe no debe superar 120 caracteres.")
        String stripeEventType,

        @Size(max = 120, message = "El PaymentIntent de Stripe no debe superar 120 caracteres.")
        String stripePaymentIntentId,

        @Positive(message = "El idPago debe ser positivo.")
        Long idPago,

        EstadoKafkaProcesamiento estadoProcesamiento,

        Boolean estado,

        LocalDateTime fechaDesde,

        LocalDateTime fechaHasta
) {
}