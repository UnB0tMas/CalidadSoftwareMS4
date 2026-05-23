// ruta: src/main/java/com/upsjb/ms4/dto/caja/request/CajaCierreRequestDto.java
package com.upsjb.ms4.dto.caja.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CajaCierreRequestDto(

        @NotNull(message = "El monto real en efectivo es obligatorio.")
        @DecimalMin(value = "0.00", message = "El monto real en efectivo no puede ser negativo.")
        @Digits(integer = 16, fraction = 2, message = "El monto real en efectivo debe tener máximo 16 enteros y 2 decimales.")
        BigDecimal montoRealEfectivo,

        @Size(max = 500, message = "La observación de cierre no debe superar 500 caracteres.")
        String observacionCierre
) {
}