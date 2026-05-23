// ruta: src/main/java/com/upsjb/ms4/dto/lookup/LookupFilterDto.java
package com.upsjb.ms4.dto.lookup;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record LookupFilterDto(

        @Size(max = 150, message = "El texto de búsqueda no debe superar 150 caracteres.")
        String search,

        Boolean soloActivos,

        @Min(value = 1, message = "El límite mínimo es 1.")
        @Max(value = 50, message = "El límite máximo es 50.")
        Integer limit
) {
}