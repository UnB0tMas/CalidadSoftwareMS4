// ruta: src/main/java/com/upsjb/ms4/dto/mail/request/CorreoOutboxReintentoRequestDto.java
package com.upsjb.ms4.dto.mail.request;

import jakarta.validation.constraints.Size;

public record CorreoOutboxReintentoRequestDto(

        @Size(max = 500, message = "El motivo no debe superar 500 caracteres.")
        String motivo
) {
}