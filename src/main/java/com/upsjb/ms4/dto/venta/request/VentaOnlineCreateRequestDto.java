// ruta: src/main/java/com/upsjb/ms4/dto/venta/request/VentaOnlineCreateRequestDto.java
package com.upsjb.ms4.dto.venta.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record VentaOnlineCreateRequestDto(

        @NotEmpty(message = "La venta debe tener al menos un detalle.")
        @Size(max = 100, message = "La venta no puede superar 100 detalles.")
        List<@Valid VentaDetalleRequestDto> detalles,

        @Size(max = 10, message = "La moneda no debe superar 10 caracteres.")
        @Pattern(regexp = "^[A-Za-z]{3}$", message = "La moneda debe tener formato ISO de 3 letras, por ejemplo PEN.")
        String moneda,

        @Size(max = 500, message = "La observación no debe superar 500 caracteres.")
        String observacion
) {
}