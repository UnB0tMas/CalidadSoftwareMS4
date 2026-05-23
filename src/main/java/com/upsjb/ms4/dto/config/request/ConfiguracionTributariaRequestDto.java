// ruta: src/main/java/com/upsjb/ms4/dto/config/request/ConfiguracionTributariaRequestDto.java
package com.upsjb.ms4.dto.config.request;

import com.upsjb.ms4.domain.enums.NombreImpuesto;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ConfiguracionTributariaRequestDto(

        @NotNull(message = "El impuesto es obligatorio.")
        NombreImpuesto nombreImpuesto,

        @NotNull(message = "El porcentaje es obligatorio.")
        @DecimalMin(value = "0.00", message = "El porcentaje no puede ser negativo.")
        @DecimalMax(value = "100.00", message = "El porcentaje no puede superar 100.")
        BigDecimal porcentaje,

        LocalDateTime fechaInicioVigencia,

        LocalDateTime fechaFinVigencia,

        @NotNull(message = "El estado vigente es obligatorio.")
        Boolean vigente,

        @NotBlank(message = "El motivo es obligatorio.")
        @Size(max = 500, message = "El motivo no debe superar 500 caracteres.")
        String motivo
) {
}