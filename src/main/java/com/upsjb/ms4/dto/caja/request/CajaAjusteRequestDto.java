// ruta: src/main/java/com/upsjb/ms4/dto/caja/request/CajaAjusteRequestDto.java
package com.upsjb.ms4.dto.caja.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CajaAjusteRequestDto(

        @NotNull(message = "El monto del ajuste es obligatorio.")
        @DecimalMin(value = "0.01", message = "El monto del ajuste debe ser mayor a cero.")
        @Digits(integer = 16, fraction = 2, message = "El monto del ajuste debe tener máximo 16 enteros y 2 decimales.")
        BigDecimal monto,

        @NotBlank(message = "La descripción del ajuste es obligatoria.")
        @Size(max = 500, message = "La descripción no debe superar 500 caracteres.")
        String descripcion
) {
}