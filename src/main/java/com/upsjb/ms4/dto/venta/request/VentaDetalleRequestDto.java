// ruta: src/main/java/com/upsjb/ms4/dto/venta/request/VentaDetalleRequestDto.java
package com.upsjb.ms4.dto.venta.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record VentaDetalleRequestDto(

        @Positive(message = "El SKU debe ser un identificador positivo.")
        Long idSkuMs3,

        @Size(max = 80, message = "El código SKU no debe superar 80 caracteres.")
        String codigoSku,

        @Size(max = 120, message = "El código de barras no debe superar 120 caracteres.")
        String barcode,

        @Positive(message = "El almacén debe ser un identificador positivo.")
        Long idAlmacenMs3,

        @Size(max = 80, message = "El código de almacén no debe superar 80 caracteres.")
        String codigoAlmacen,

        @NotNull(message = "La cantidad es obligatoria.")
        @Positive(message = "La cantidad debe ser mayor a cero.")
        Integer cantidad
) {
}