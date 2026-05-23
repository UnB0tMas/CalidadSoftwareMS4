// ruta: src/main/java/com/upsjb/ms4/dto/config/filter/ConfiguracionTributariaFilterDto.java
package com.upsjb.ms4.dto.config.filter;

import com.upsjb.ms4.domain.enums.NombreImpuesto;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ConfiguracionTributariaFilterDto(

        @Size(max = 150, message = "El texto de búsqueda no debe superar 150 caracteres.")
        String search,

        NombreImpuesto nombreImpuesto,

        Boolean vigente,

        Boolean estado,

        LocalDateTime fechaInicioDesde,

        LocalDateTime fechaInicioHasta
) {
}