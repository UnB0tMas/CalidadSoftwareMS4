// ruta: src/main/java/com/upsjb/ms4/dto/auditoria/response/AuditoriaResponseDto.java
package com.upsjb.ms4.dto.auditoria.response;

import com.upsjb.ms4.domain.enums.ResultadoAuditoria;
import java.time.LocalDateTime;

public record AuditoriaResponseDto(
        Long id,
        String entidad,
        String entidadId,
        String accion,
        ResultadoAuditoria resultado,
        Long actorIdUsuarioMs1,
        String actorRol,
        String actorUsername,
        String ip,
        String userAgent,
        String requestId,
        String correlationId,
        String detalleJson,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}