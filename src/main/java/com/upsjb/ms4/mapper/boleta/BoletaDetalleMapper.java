// ruta: src/main/java/com/upsjb/ms4/mapper/boleta/BoletaDetalleMapper.java
package com.upsjb.ms4.mapper.boleta;

import com.upsjb.ms4.domain.entity.boleta.BoletaDetalle;
import com.upsjb.ms4.dto.boleta.response.BoletaDetalleResponseDto;
import org.springframework.stereotype.Component;

@Component
public class BoletaDetalleMapper {

    public BoletaDetalleResponseDto toResponse(BoletaDetalle entity) {
        if (entity == null) return null;

        return new BoletaDetalleResponseDto(
                entity.getId(),
                entity.getIdBoleta(),
                entity.getIdVentaDetalle(),
                entity.getIdProductoMs3(),
                entity.getIdSkuMs3(),
                entity.getCodigoProducto(),
                entity.getCodigoSku(),
                entity.getNombreProducto(),
                entity.getDescripcionSku(),
                entity.getCantidad(),
                entity.getPrecioUnitarioBase(),
                entity.getPrecioUnitarioFinal(),
                entity.getSubtotal(),
                entity.getTipoDescuento(),
                entity.getValorDescuento(),
                entity.getMontoDescuento(),
                entity.getIdPromocionMs3(),
                entity.getIdPromocionVersionMs3(),
                entity.getCodigoPromocion(),
                entity.getIgvPorcentaje(),
                entity.getIgvMonto(),
                entity.getTotalLinea(),
                entity.getEstado(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public BoletaDetalleResponseDto toDetailResponse(BoletaDetalle entity) {
        return toResponse(entity);
    }
}