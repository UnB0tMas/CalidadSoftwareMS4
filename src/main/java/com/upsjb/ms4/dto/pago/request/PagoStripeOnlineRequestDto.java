// ruta: src/main/java/com/upsjb/ms4/dto/pago/request/PagoStripeOnlineRequestDto.java
package com.upsjb.ms4.dto.pago.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PagoStripeOnlineRequestDto(

        @NotNull(message = "La venta es obligatoria.")
        @Positive(message = "La venta debe ser un identificador positivo.")
        Long idVenta,

        @Size(max = 500, message = "La URL de retorno no debe superar 500 caracteres.")
        String returnUrl
) {
}