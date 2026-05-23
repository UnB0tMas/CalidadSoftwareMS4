// ruta: src/main/java/com/upsjb/ms4/dto/config/filter/SerieBoletaFilterDto.java
package com.upsjb.ms4.dto.config.filter;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SerieBoletaFilterDto(

        @Size(max = 150, message = "El texto de búsqueda no debe superar 150 caracteres.")
        String search,

        @Pattern(regexp = "^B\\d{3}$", message = "La serie debe tener formato B001.")
        String serie,

        Boolean estado
) {
}