// ruta: src/main/java/com/upsjb/ms4/dto/shared/filter/BaseFilterDto.java
package com.upsjb.ms4.dto.shared.filter;

import jakarta.validation.constraints.Size;

public record BaseFilterDto(

        @Size(max = 150, message = "El texto de búsqueda no debe superar 150 caracteres.")
        String search,

        Boolean estado
) {
}