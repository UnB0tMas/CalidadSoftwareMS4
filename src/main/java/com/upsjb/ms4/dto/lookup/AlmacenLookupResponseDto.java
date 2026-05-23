// ruta: src/main/java/com/upsjb/ms4/dto/lookup/AlmacenLookupResponseDto.java
package com.upsjb.ms4.dto.lookup;

public record AlmacenLookupResponseDto(
        Long idAlmacenMs3,
        String codigoAlmacen,
        String nombreAlmacen,
        Boolean disponible
) {
}