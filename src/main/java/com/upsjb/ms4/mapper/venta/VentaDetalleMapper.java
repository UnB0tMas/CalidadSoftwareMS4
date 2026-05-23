// ruta: src/main/java/com/upsjb/ms4/mapper/venta/VentaDetalleMapper.java
package com.upsjb.ms4.mapper.venta;

import com.upsjb.ms4.domain.entity.venta.VentaDetalle;
import com.upsjb.ms4.dto.venta.response.VentaDetalleResponseDto;
import org.springframework.stereotype.Component;

@Component
public class VentaDetalleMapper {

    public VentaDetalleResponseDto toResponse(VentaDetalle entity) {
        if (entity == null) return null;

        return new VentaDetalleResponseDto(
                entity.getId(),
                entity.getIdVenta(),
                entity.getIdProductoMs3(),
                entity.getIdSkuMs3(),
                entity.getIdAlmacenMs3(),
                entity.getCodigoProducto(),
                entity.getCodigoSku(),
                entity.getCodigoAlmacen(),
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
                entity.getStockSnapshotFisico(),
                entity.getStockSnapshotReservado(),
                entity.getStockSnapshotDisponible(),
                entity.getEstado(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public VentaDetalleResponseDto toDetailResponse(VentaDetalle entity) {
        return toResponse(entity);
    }
}