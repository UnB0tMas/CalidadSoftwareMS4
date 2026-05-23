// ruta: src/main/java/com/upsjb/ms4/dto/caja/request/CajaAperturaRequestDto.java
package com.upsjb.ms4.dto.caja.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CajaAperturaRequestDto(

        @NotNull(message = "El monto inicial es obligatorio.")
        @DecimalMin(value = "0.00", message = "El monto inicial no puede ser negativo.")
        @Digits(integer = 16, fraction = 2, message = "El monto inicial debe tener máximo 16 enteros y 2 decimales.")
        BigDecimal montoInicial,

        @Size(max = 500, message = "La observación de apertura no debe superar 500 caracteres.")
        String observacionApertura
) {
}