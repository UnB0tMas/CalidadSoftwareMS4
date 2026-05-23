// ruta: src/main/java/com/upsjb/ms4/dto/venta/request/VentaFisicaCreateRequestDto.java
package com.upsjb.ms4.dto.venta.request;

import com.upsjb.ms4.domain.enums.MetodoPago;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record VentaFisicaCreateRequestDto(

        @Positive(message = "El cliente snapshot debe ser un identificador positivo.")
        Long idClienteSnapshot,

        @Positive(message = "El cliente MS2 debe ser un identificador positivo.")
        Long idClienteMs2,

        @Positive(message = "El usuario MS1 del cliente debe ser un identificador positivo.")
        Long idUsuarioClienteMs1,

        @Size(max = 30, message = "El número de documento no debe superar 30 caracteres.")
        String numeroDocumento,

        @Size(max = 20, message = "El RUC no debe superar 20 caracteres.")
        String ruc,

        @Positive(message = "La caja debe ser un identificador positivo.")
        Long idCaja,

        @Size(max = 80, message = "El código de caja no debe superar 80 caracteres.")
        String codigoCaja,

        @NotNull(message = "El método de pago principal es obligatorio.")
        MetodoPago metodoPagoPrincipal,

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