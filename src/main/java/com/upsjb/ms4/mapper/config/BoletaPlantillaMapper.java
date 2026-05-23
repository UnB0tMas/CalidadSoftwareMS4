// ruta: src/main/java/com/upsjb/ms4/mapper/config/BoletaPlantillaMapper.java
package com.upsjb.ms4.mapper.config;

import com.upsjb.ms4.domain.entity.config.BoletaPlantillaVersion;
import com.upsjb.ms4.dto.config.request.BoletaPlantillaRequestDto;
import com.upsjb.ms4.dto.config.response.BoletaPlantillaResponseDto;
import org.springframework.stereotype.Component;

@Component
public class BoletaPlantillaMapper {

    public BoletaPlantillaResponseDto toResponse(BoletaPlantillaVersion entity) {
        if (entity == null) {
            return null;
        }

        return new BoletaPlantillaResponseDto(
                entity.getId(),
                entity.getCodigoVersion(),
                entity.getNombre(),
                entity.getRutaTemplateHtml(),
                entity.getRutaTemplateMail(),
                entity.getDescripcion(),
                entity.getFechaInicioVigencia(),
                entity.getFechaFinVigencia(),
                entity.getVigente(),
                entity.getCreadoPorIdUsuarioMs1(),
                entity.getMotivo(),
                entity.getEstado(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public BoletaPlantillaResponseDto toDetailResponse(BoletaPlantillaVersion entity) {
        return toResponse(entity);
    }

    public BoletaPlantillaVersion toEntity(BoletaPlantillaRequestDto request) {
        if (request == null) {
            return null;
        }

        BoletaPlantillaVersion entity = new BoletaPlantillaVersion();
        updateEntity(entity, request);
        return entity;
    }

    public void updateEntity(BoletaPlantillaVersion entity, BoletaPlantillaRequestDto request) {
        if (entity == null || request == null) {
            return;
        }

        entity.setNombre(request.nombre());
        entity.setRutaTemplateHtml(request.rutaTemplateHtml());
        entity.setRutaTemplateMail(request.rutaTemplateMail());
        entity.setDescripcion(request.descripcion());
        entity.setFechaInicioVigencia(request.fechaInicioVigencia());
        entity.setFechaFinVigencia(request.fechaFinVigencia());
        entity.setVigente(request.vigente());
        entity.setMotivo(request.motivo());
    }
}