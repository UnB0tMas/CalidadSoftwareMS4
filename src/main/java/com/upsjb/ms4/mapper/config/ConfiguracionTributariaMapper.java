// ruta: src/main/java/com/upsjb/ms4/mapper/config/ConfiguracionTributariaMapper.java
package com.upsjb.ms4.mapper.config;

import com.upsjb.ms4.domain.entity.config.ConfiguracionTributariaVersion;
import com.upsjb.ms4.dto.config.request.ConfiguracionTributariaRequestDto;
import com.upsjb.ms4.dto.config.response.ConfiguracionTributariaResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ConfiguracionTributariaMapper {

    public ConfiguracionTributariaResponseDto toResponse(ConfiguracionTributariaVersion entity) {
        if (entity == null) return null;

        return new ConfiguracionTributariaResponseDto(
                entity.getId(),
                entity.getCodigoVersion(),
                entity.getNombreImpuesto(),
                entity.getPorcentaje(),
                entity.getFechaInicioVigencia(),
                entity.getFechaFinVigencia(),
                entity.getVigente(),
                entity.getMotivo(),
                entity.getModificadoPorIdUsuarioMs1(),
                entity.getEstado(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public ConfiguracionTributariaResponseDto toDetailResponse(ConfiguracionTributariaVersion entity) {
        return toResponse(entity);
    }

    public ConfiguracionTributariaVersion toEntity(ConfiguracionTributariaRequestDto request) {
        if (request == null) return null;

        ConfiguracionTributariaVersion entity = new ConfiguracionTributariaVersion();
        updateEntity(entity, request);
        return entity;
    }

    public void updateEntity(ConfiguracionTributariaVersion entity, ConfiguracionTributariaRequestDto request) {
        if (entity == null || request == null) return;

        entity.setNombreImpuesto(request.nombreImpuesto());
        entity.setPorcentaje(request.porcentaje());
        entity.setFechaInicioVigencia(request.fechaInicioVigencia());
        entity.setFechaFinVigencia(request.fechaFinVigencia());
        entity.setVigente(request.vigente());
        entity.setMotivo(request.motivo());
    }
}