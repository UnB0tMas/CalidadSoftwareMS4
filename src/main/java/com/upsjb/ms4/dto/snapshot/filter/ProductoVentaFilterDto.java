// ruta: src/main/java/com/upsjb/ms4/dto/snapshot/filter/ProductoVentaFilterDto.java
package com.upsjb.ms4.dto.snapshot.filter;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductoVentaFilterDto(

        @Size(
                max = 150,
                message = "El texto de búsqueda no debe superar 150 caracteres."
        )
        String search,

        @Positive(
                message = "El idProductoMs3 debe ser positivo."
        )
        Long idProductoMs3,

        @Size(
                max = 80,
                message = "El código de producto no debe superar 80 caracteres."
        )
        String codigoProducto,

        @Size(
                max = 250,
                message = "El slug no debe superar 250 caracteres."
        )
        String slug,

        @Positive(
                message = "El idCategoriaMs3 debe ser positivo."
        )
        Long idCategoriaMs3,

        @Positive(
                message = "El idCategoriaPadreMs3 debe ser positivo."
        )
        Long idCategoriaPadreMs3,

        @PositiveOrZero(
                message = "El nivel de categoría no puede ser negativo."
        )
        Integer nivelCategoria,

        Boolean categoriaPermiteProductos,

        Boolean categoriaEstado,

        @Positive(
                message = "El idMarcaMs3 debe ser positivo."
        )
        Long idMarcaMs3,

        @Size(
                max = 60,
                message = "El estado de registro no debe superar 60 caracteres."
        )
        String estadoRegistro,

        @Size(
                max = 60,
                message = "El estado de publicación no debe superar 60 caracteres."
        )
        String estadoPublicacion,

        @Size(
                max = 60,
                message = "El estado de venta no debe superar 60 caracteres."
        )
        String estadoVenta,

        Boolean visiblePublico,

        Boolean vendible,

        Boolean estado
) {
}