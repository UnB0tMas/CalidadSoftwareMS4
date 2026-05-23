// ruta: src/main/java/com/upsjb/ms4/dto/lookup/SerieBoletaLookupResponseDto.java
package com.upsjb.ms4.dto.lookup;

public record SerieBoletaLookupResponseDto(
        Long id,
        String serie,
        Long numeroActual,
        Long numeroInicio,
        Long numeroFin,
        Boolean estado
) {
}