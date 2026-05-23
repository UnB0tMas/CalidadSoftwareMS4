// ruta: src/main/java/com/upsjb/ms4/dto/snapshot/filter/PrecioVentaFilterDto.java
package com.upsjb.ms4.dto.snapshot.filter;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record PrecioVentaFilterDto(

        @Positive(message = "El idPrecioHistorialMs3 debe ser positivo.")
        Long idPrecioHistorialMs3,

        @Positive(message = "El idSkuMs3 debe ser positivo.")
        Long idSkuMs3,

        @Positive(message = "El idProductoMs3 debe ser positivo.")
        Long idProductoMs3,

        @Size(max = 80, message = "El código SKU no debe superar 80 caracteres.")
        String codigoSku,

        @Size(max = 80, message = "El código de producto no debe superar 80 caracteres.")
        String codigoProducto,

        @Size(max = 10, message = "La moneda no debe superar 10 caracteres.")
        String moneda,

        Boolean vigente,

        Boolean estado,

        LocalDateTime fechaInicioDesde,

        LocalDateTime fechaInicioHasta
) {
}