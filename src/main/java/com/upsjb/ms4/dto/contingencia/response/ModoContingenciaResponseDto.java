// ruta: src/main/java/com/upsjb/ms4/dto/contingencia/response/ModoContingenciaResponseDto.java
package com.upsjb.ms4.dto.contingencia.response;

import com.upsjb.ms4.domain.enums.EstadoContingencia;
import java.time.LocalDateTime;

public record ModoContingenciaResponseDto(
        Long id,
        String servicioAfectado,
        EstadoContingencia estadoContingencia,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        Long activadoPorIdUsuarioMs1,
        String activadoPorRol,
        String motivo,
        Boolean ventasPermitidas,
        Boolean guardarEventosPendientes,
        Integer totalEventosPendientes,
        String observacion,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}