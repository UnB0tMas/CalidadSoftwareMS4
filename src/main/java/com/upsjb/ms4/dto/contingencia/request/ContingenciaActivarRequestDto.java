// ruta: src/main/java/com/upsjb/ms4/dto/contingencia/request/ContingenciaActivarRequestDto.java
package com.upsjb.ms4.dto.contingencia.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ContingenciaActivarRequestDto(

        @NotBlank(message = "El servicio afectado es obligatorio.")
        @Size(max = 40, message = "El servicio afectado no debe superar 40 caracteres.")
        String servicioAfectado,

        @NotBlank(message = "El motivo es obligatorio.")
        @Size(max = 500, message = "El motivo no debe superar 500 caracteres.")
        String motivo,

        @NotNull(message = "Debe indicar si las ventas estarán permitidas.")
        Boolean ventasPermitidas,

        @NotNull(message = "Debe indicar si se guardarán eventos pendientes.")
        Boolean guardarEventosPendientes,

        @Size(max = 500, message = "La observación no debe superar 500 caracteres.")
        String observacion
) {
}