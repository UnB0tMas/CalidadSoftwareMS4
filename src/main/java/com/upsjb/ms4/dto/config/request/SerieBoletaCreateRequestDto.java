// ruta: src/main/java/com/upsjb/ms4/dto/config/request/SerieBoletaCreateRequestDto.java
package com.upsjb.ms4.dto.config.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SerieBoletaCreateRequestDto(

        @NotBlank(message = "La serie es obligatoria.")
        @Pattern(regexp = "^B\\d{3}$", message = "La serie debe tener formato B001.")
        String serie,

        @NotNull(message = "El número inicial es obligatorio.")
        @Min(value = 1, message = "El número inicial debe ser mayor a cero.")
        Long numeroInicio,

        @Min(value = 1, message = "El número final debe ser mayor a cero.")
        Long numeroFin
) {
}