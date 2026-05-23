// ruta: src/main/java/com/upsjb/ms4/dto/lookup/LookupItemResponseDto.java
package com.upsjb.ms4.dto.lookup;

public record LookupItemResponseDto(
        Long id,
        String codigo,
        String label,
        String descripcion,
        Boolean activo
) {
}