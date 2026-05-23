// ruta: src/main/java/com/upsjb/ms4/dto/config/response/SerieBoletaResponseDto.java
package com.upsjb.ms4.dto.config.response;

import java.time.LocalDateTime;

public record SerieBoletaResponseDto(
        Long id,
        String serie,
        Long numeroActual,
        Long numeroInicio,
        Long numeroFin,
        Long creadoPorIdUsuarioMs1,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}