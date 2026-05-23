// ruta: src/main/java/com/upsjb/ms4/mapper/config/ConfiguracionEmpresaMapper.java
package com.upsjb.ms4.mapper.config;

import com.upsjb.ms4.domain.entity.config.ConfiguracionEmpresaVersion;
import com.upsjb.ms4.dto.config.request.ConfiguracionEmpresaRequestDto;
import com.upsjb.ms4.dto.config.response.ConfiguracionEmpresaResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ConfiguracionEmpresaMapper {

    public ConfiguracionEmpresaResponseDto toResponse(ConfiguracionEmpresaVersion entity) {
        if (entity == null) return null;

        return new ConfiguracionEmpresaResponseDto(
                entity.getId(),
                entity.getCodigoVersion(),
                entity.getRuc(),
                entity.getRazonSocial(),
                entity.getNombreComercial(),
                entity.getDireccionFiscal(),
                entity.getTelefono(),
                entity.getCorreo(),
                entity.getWeb(),
                entity.getIdLogoAsset(),
                entity.getLogoUrl(),
                entity.getLogoPublicId(),
                entity.getColorPrimario(),
                entity.getColorSecundario(),
                entity.getMensajePieBoleta(),
                entity.getTerminosCondiciones(),
                entity.getPoliticaCambios(),
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

    public ConfiguracionEmpresaResponseDto toDetailResponse(ConfiguracionEmpresaVersion entity) {
        return toResponse(entity);
    }

    public ConfiguracionEmpresaVersion toEntity(ConfiguracionEmpresaRequestDto request) {
        if (request == null) return null;

        ConfiguracionEmpresaVersion entity = new ConfiguracionEmpresaVersion();
        updateEntity(entity, request);
        return entity;
    }

    public void updateEntity(ConfiguracionEmpresaVersion entity, ConfiguracionEmpresaRequestDto request) {
        if (entity == null || request == null) return;

        entity.setRuc(request.ruc());
        entity.setRazonSocial(request.razonSocial());
        entity.setNombreComercial(request.nombreComercial());
        entity.setDireccionFiscal(request.direccionFiscal());
        entity.setTelefono(request.telefono());
        entity.setCorreo(request.correo());
        entity.setWeb(request.web());
        entity.setIdLogoAsset(request.idLogoAsset());
        entity.setColorPrimario(request.colorPrimario());
        entity.setColorSecundario(request.colorSecundario());
        entity.setMensajePieBoleta(request.mensajePieBoleta());
        entity.setTerminosCondiciones(request.terminosCondiciones());
        entity.setPoliticaCambios(request.politicaCambios());
        entity.setFechaInicioVigencia(request.fechaInicioVigencia());
        entity.setMotivo(request.motivo());
    }
}