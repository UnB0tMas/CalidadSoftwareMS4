// ruta: src/main/java/com/upsjb/ms4/dto/lookup/SkuLookupResponseDto.java
package com.upsjb.ms4.dto.lookup;

public record SkuLookupResponseDto(
        Long id,
        Long idSkuMs3,
        Long idProductoMs3,
        String codigoProducto,
        String codigoSku,
        String barcode,
        String color,
        String talla,
        String material,
        String modelo,
        String estadoSku,
        Boolean estado
) {
}