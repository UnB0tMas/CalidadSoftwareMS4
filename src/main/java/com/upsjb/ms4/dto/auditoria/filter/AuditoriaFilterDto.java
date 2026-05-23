// ruta: src/main/java/com/upsjb/ms4/dto/auditoria/filter/AuditoriaFilterDto.java
package com.upsjb.ms4.dto.auditoria.filter;

import com.upsjb.ms4.domain.enums.ResultadoAuditoria;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record AuditoriaFilterDto(

        @Size(max = 150, message = "El texto de búsqueda no debe superar 150 caracteres.")
        String search,

        @Size(max = 80, message = "La entidad no debe superar 80 caracteres.")
        String entidad,

        @Size(max = 120, message = "El identificador de entidad no debe superar 120 caracteres.")
        String entidadId,

        @Size(max = 100, message = "La acción no debe superar 100 caracteres.")
        String accion,

        ResultadoAuditoria resultado,

        Long actorIdUsuarioMs1,

        @Size(max = 40, message = "El rol del actor no debe superar 40 caracteres.")
        String actorRol,

        @Size(max = 180, message = "El username del actor no debe superar 180 caracteres.")
        String actorUsername,

        @Size(max = 100, message = "El requestId no debe superar 100 caracteres.")
        String requestId,

        @Size(max = 100, message = "El correlationId no debe superar 100 caracteres.")
        String correlationId,

        Boolean estado,

        LocalDateTime fechaDesde,

        LocalDateTime fechaHasta
) {
}