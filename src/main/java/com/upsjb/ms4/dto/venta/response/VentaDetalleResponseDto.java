// ruta: src/main/java/com/upsjb/ms4/dto/venta/response/VentaDetalleResponseDto.java
package com.upsjb.ms4.dto.venta.response;

import com.upsjb.ms4.domain.enums.TipoDescuento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VentaDetalleResponseDto(
        Long id,
        Long idVenta,
        Long idProductoMs3,
        Long idSkuMs3,
        Long idAlmacenMs3,
        String codigoProducto,
        String codigoSku,
        String codigoAlmacen,
        String nombreProducto,
        String descripcionSku,
        Integer cantidad,
        BigDecimal precioUnitarioBase,
        BigDecimal precioUnitarioFinal,
        BigDecimal subtotal,
        TipoDescuento tipoDescuento,
        BigDecimal valorDescuento,
        BigDecimal montoDescuento,
        Long idPromocionMs3,
        Long idPromocionVersionMs3,
        String codigoPromocion,
        BigDecimal igvPorcentaje,
        BigDecimal igvMonto,
        BigDecimal totalLinea,
        Integer stockSnapshotFisico,
        Integer stockSnapshotReservado,
        Integer stockSnapshotDisponible,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}