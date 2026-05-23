// ruta: src/main/java/com/upsjb/ms4/dto/config/response/ConfiguracionTributariaResponseDto.java
package com.upsjb.ms4.dto.config.response;

import com.upsjb.ms4.domain.enums.NombreImpuesto;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ConfiguracionTributariaResponseDto(
        Long id,
        String codigoVersion,
        NombreImpuesto nombreImpuesto,
        BigDecimal porcentaje,
        LocalDateTime fechaInicioVigencia,
        LocalDateTime fechaFinVigencia,
        Boolean vigente,
        String motivo,
        Long modificadoPorIdUsuarioMs1,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}