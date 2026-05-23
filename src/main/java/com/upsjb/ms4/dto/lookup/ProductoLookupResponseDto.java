// ruta: src/main/java/com/upsjb/ms4/dto/lookup/ProductoLookupResponseDto.java
package com.upsjb.ms4.dto.lookup;

public record ProductoLookupResponseDto(
        Long id,
        Long idProductoMs3,
        String codigoProducto,
        String nombre,
        String slug,
        String nombreCategoria,
        String nombreMarca,
        String estadoPublicacion,
        String estadoVenta,
        Boolean visiblePublico,
        Boolean vendible,
        Boolean estado
) {
}