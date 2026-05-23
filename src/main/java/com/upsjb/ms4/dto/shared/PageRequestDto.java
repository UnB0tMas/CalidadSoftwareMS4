// ruta: src/main/java/com/upsjb/ms4/dto/shared/PageRequestDto.java
package com.upsjb.ms4.dto.shared;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PageRequestDto(

        @Min(value = 0, message = "La página no puede ser negativa.")
        Integer page,

        @Min(value = 1, message = "El tamaño mínimo de página es 1.")
        @Max(value = 100, message = "El tamaño máximo de página es 100.")
        Integer size,

        @Size(max = 80, message = "El campo de ordenamiento no debe superar 80 caracteres.")
        @Pattern(
                regexp = "^[a-zA-Z0-9_.]+$",
                message = "El campo de ordenamiento solo puede contener letras, números, punto o guion bajo."
        )
        String sortBy,

        @Pattern(
                regexp = "(?i)^(ASC|DESC)$",
                message = "La dirección de ordenamiento debe ser ASC o DESC."
        )
        String sortDirection
) {
}