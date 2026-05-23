// ruta: src/main/java/com/upsjb/ms4/mapper/auditoria/AuditoriaMapper.java
package com.upsjb.ms4.mapper.auditoria;

import com.upsjb.ms4.domain.entity.auditoria.AuditoriaFuncional;
import com.upsjb.ms4.dto.auditoria.response.AuditoriaResponseDto;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaMapper {

    public AuditoriaResponseDto toResponse(AuditoriaFuncional entity) {
        if (entity == null) return null;

        return new AuditoriaResponseDto(
                entity.getId(),
                entity.getEntidad(),
                entity.getEntidadId(),
                entity.getAccion(),
                entity.getResultado(),
                entity.getActorIdUsuarioMs1(),
                entity.getActorRol(),
                entity.getActorUsername(),
                entity.getIp(),
                entity.getUserAgent(),
                entity.getRequestId(),
                entity.getCorrelationId(),
                entity.getDetalleJson(),
                entity.getEstado(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public AuditoriaResponseDto toDetailResponse(AuditoriaFuncional entity) {
        return toResponse(entity);
    }
}