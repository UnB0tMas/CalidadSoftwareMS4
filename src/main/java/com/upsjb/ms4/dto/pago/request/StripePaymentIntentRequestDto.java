// ruta: src/main/java/com/upsjb/ms4/dto/pago/request/StripePaymentIntentRequestDto.java
package com.upsjb.ms4.dto.pago.request;

import com.upsjb.ms4.domain.enums.MetodoPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record StripePaymentIntentRequestDto(

        @NotNull(message = "La venta es obligatoria.")
        @Positive(message = "La venta debe ser un identificador positivo.")
        Long idVenta,

        @NotNull(message = "El método de pago es obligatorio.")
        MetodoPago metodoPago,

        @Size(max = 500, message = "La URL de retorno no debe superar 500 caracteres.")
        String returnUrl
) {
}