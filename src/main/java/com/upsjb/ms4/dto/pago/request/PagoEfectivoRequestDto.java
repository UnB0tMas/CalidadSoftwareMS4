// ruta: src/main/java/com/upsjb/ms4/dto/pago/request/PagoEfectivoRequestDto.java
package com.upsjb.ms4.dto.pago.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PagoEfectivoRequestDto(

        @NotNull(message = "El monto recibido es obligatorio.")
        @DecimalMin(value = "0.01", message = "El monto recibido debe ser mayor a cero.")
        BigDecimal montoRecibido,

        @Size(max = 500, message = "La observación no debe superar 500 caracteres.")
        String observacion
) {
}