// ruta: src/main/java/com/upsjb/ms4/mapper/boleta/BoletaMapper.java
package com.upsjb.ms4.mapper.boleta;

import com.upsjb.ms4.domain.entity.boleta.Boleta;
import com.upsjb.ms4.dto.boleta.response.BoletaDetailResponseDto;
import com.upsjb.ms4.dto.boleta.response.BoletaDetalleResponseDto;
import com.upsjb.ms4.dto.boleta.response.BoletaPreviewResponseDto;
import com.upsjb.ms4.dto.boleta.response.BoletaResponseDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BoletaMapper {

    public BoletaResponseDto toResponse(Boleta entity) {
        if (entity == null) return null;

        return new BoletaResponseDto(
                entity.getId(),
                entity.getIdVenta(),
                entity.getIdSerieBoleta(),
                entity.getIdConfiguracionEmpresaVersion(),
                entity.getIdConfiguracionTributariaVersion(),
                entity.getIdBoletaPlantillaVersion(),
                entity.getSerie(),
                entity.getNumero(),
                entity.getCodigoBoleta(),
                entity.getFechaEmision(),
                entity.getMoneda(),
                entity.getRucEmisor(),
                entity.getRazonSocialEmisor(),
                entity.getNombreComercialEmisor(),
                entity.getDireccionFiscalEmisor(),
                entity.getTelefonoEmisor(),
                entity.getCorreoEmisor(),
                entity.getLogoUrlEmisor(),
                entity.getTipoDocumentoCliente(),
                entity.getNumeroDocumentoCliente(),
                entity.getNombreCliente(),
                entity.getCorreoCliente(),
                entity.getTelefonoCliente(),
                entity.getDireccionCliente(),
                entity.getSubtotal(),
                entity.getDescuentoTotal(),
                entity.getOpGravada(),
                entity.getOpExonerada(),
                entity.getOpInafecta(),
                entity.getIgvPorcentaje(),
                entity.getIgvTotal(),
                entity.getTotal(),
                entity.getEstadoBoleta(),
                entity.getHashPayload(),
                entity.getVersionPlantilla(),
                entity.getEnviadoPorCorreo(),
                entity.getFechaUltimoEnvioCorreo(),
                entity.getCantidadEnviosCorreo(),
                entity.getEstado(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public BoletaDetailResponseDto toDetailResponse(Boleta entity, List<BoletaDetalleResponseDto> detalles) {
        return new BoletaDetailResponseDto(
                toResponse(entity),
                detalles == null ? List.of() : detalles
        );
    }

    public BoletaPreviewResponseDto toPreviewResponse(Boleta entity, String html) {
        if (entity == null) return null;

        return new BoletaPreviewResponseDto(
                entity.getId(),
                entity.getCodigoBoleta(),
                html,
                entity.getVersionPlantilla()
        );
    }
}