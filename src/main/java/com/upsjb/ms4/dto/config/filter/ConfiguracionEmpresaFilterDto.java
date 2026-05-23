// ruta: src/main/java/com/upsjb/ms4/dto/config/filter/ConfiguracionEmpresaFilterDto.java
package com.upsjb.ms4.dto.config.filter;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ConfiguracionEmpresaFilterDto(

        @Size(max = 150, message = "El texto de búsqueda no debe superar 150 caracteres.")
        String search,

        @Pattern(regexp = "^\\d{11}$", message = "El RUC debe tener 11 dígitos.")
        String ruc,

        Boolean vigente,

        Boolean estado,

        LocalDateTime fechaInicioDesde,

        LocalDateTime fechaInicioHasta
) {
}