// ruta: src/main/java/com/upsjb/ms4/dto/pago/request/PagoStripePresencialRequestDto.java
package com.upsjb.ms4.dto.pago.request;

import jakarta.validation.constraints.Size;

public record PagoStripePresencialRequestDto(

        @Size(max = 250, message = "La descripción no debe superar 250 caracteres.")
        String descripcion,

        @Size(max = 500, message = "La URL de retorno no debe superar 500 caracteres.")
        String returnUrl
) {
}