// ruta: src/main/java/com/upsjb/ms4/dto/boleta/response/BoletaDetalleResponseDto.java
package com.upsjb.ms4.dto.boleta.response;

import com.upsjb.ms4.domain.enums.TipoDescuento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BoletaDetalleResponseDto(
        Long id,
        Long idBoleta,
        Long idVentaDetalle,
        Long idProductoMs3,
        Long idSkuMs3,
        String codigoProducto,
        String codigoSku,
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
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}