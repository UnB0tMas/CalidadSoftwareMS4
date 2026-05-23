// ruta: src/main/java/com/upsjb/ms4/dto/snapshot/filter/StockVentaFilterDto.java
package com.upsjb.ms4.dto.snapshot.filter;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record StockVentaFilterDto(

        @Size(max = 150, message = "El texto de búsqueda no debe superar 150 caracteres.")
        String search,

        @Positive(message = "El idStockMs3 debe ser positivo.")
        Long idStockMs3,

        @Positive(message = "El idSkuMs3 debe ser positivo.")
        Long idSkuMs3,

        @Positive(message = "El idProductoMs3 debe ser positivo.")
        Long idProductoMs3,

        @Positive(message = "El idAlmacenMs3 debe ser positivo.")
        Long idAlmacenMs3,

        @Size(max = 80, message = "El código SKU no debe superar 80 caracteres.")
        String codigoSku,

        @Size(max = 120, message = "El barcode no debe superar 120 caracteres.")
        String barcode,

        @Size(max = 80, message = "El código de producto no debe superar 80 caracteres.")
        String codigoProducto,

        @Size(max = 80, message = "El código de almacén no debe superar 80 caracteres.")
        String codigoAlmacen,

        Boolean bajoStock,

        Boolean sobreStock,

        Boolean conStockDisponible,

        Boolean estado
) {
}