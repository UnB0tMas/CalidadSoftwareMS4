// ruta: src/main/java/com/upsjb/ms4/dto/venta/response/VentaCalculoPreviewResponseDto.java
package com.upsjb.ms4.dto.venta.response;

import java.math.BigDecimal;
import java.util.List;

public record VentaCalculoPreviewResponseDto(
        String moneda,
        BigDecimal subtotal,
        BigDecimal descuentoTotal,
        BigDecimal opGravada,
        BigDecimal opExonerada,
        BigDecimal opInafecta,
        BigDecimal igvPorcentaje,
        BigDecimal igvTotal,
        BigDecimal total,
        Boolean stockSuficiente,
        List<VentaDetalleResponseDto> detalles,
        List<String> advertencias
) {
}