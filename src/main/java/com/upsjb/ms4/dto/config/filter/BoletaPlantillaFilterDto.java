// ruta: src/main/java/com/upsjb/ms4/dto/config/filter/BoletaPlantillaFilterDto.java
package com.upsjb.ms4.dto.config.filter;

import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record BoletaPlantillaFilterDto(

        @Size(max = 150, message = "El texto de búsqueda no debe superar 150 caracteres.")
        String search,

        @Size(max = 50, message = "El código de versión no debe superar 50 caracteres.")
        String codigoVersion,

        Boolean vigente,

        Boolean estado,

        LocalDateTime fechaInicioDesde,

        LocalDateTime fechaInicioHasta
) {
}