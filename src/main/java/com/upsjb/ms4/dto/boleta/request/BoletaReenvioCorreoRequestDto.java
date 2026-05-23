// ruta: src/main/java/com/upsjb/ms4/dto/boleta/request/BoletaReenvioCorreoRequestDto.java
package com.upsjb.ms4.dto.boleta.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record BoletaReenvioCorreoRequestDto(


        @Email(message = "El correo destino debe tener formato válido.")
        @Size(max = 180, message = "El correo destino no debe superar 180 caracteres.")
        String correoDestino,

        @Size(max = 250, message = "El nombre destino no debe superar 250 caracteres.")
        String nombreDestino,

        @Size(max = 500, message = "La observación no debe superar 500 caracteres.")
        String observacion
) {
}