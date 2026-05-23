// ruta: src/main/java/com/upsjb/ms4/dto/shared/filter/EstadoFilterDto.java
package com.upsjb.ms4.dto.shared.filter;

import jakarta.validation.constraints.Size;

public record EstadoFilterDto(

        Boolean estado,

        @Size(max = 60, message = "El estado funcional no debe superar 60 caracteres.")
        String estadoFuncional
) {
}