// ruta: src/main/java/com/upsjb/ms4/dto/contingencia/request/ContingenciaFinalizarRequestDto.java
package com.upsjb.ms4.dto.contingencia.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContingenciaFinalizarRequestDto(

        @NotBlank(message = "El motivo de finalización es obligatorio.")
        @Size(max = 500, message = "El motivo no debe superar 500 caracteres.")
        String motivo,

        @Size(max = 500, message = "La observación no debe superar 500 caracteres.")
        String observacion
) {
}