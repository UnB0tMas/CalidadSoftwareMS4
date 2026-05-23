// ruta: src/main/java/com/upsjb/ms4/dto/reporte/response/ReporteProductoVendidoDto.java
package com.upsjb.ms4.dto.reporte.response;

import java.math.BigDecimal;

public record ReporteProductoVendidoDto(
        Long idProductoMs3,
        Long idSkuMs3,
        String codigoProducto,
        String codigoSku,
        String nombreProducto,
        String descripcionSku,
        Integer cantidadVendida,
        BigDecimal subtotal,
        BigDecimal descuentoTotal,
        BigDecimal igvTotal,
        BigDecimal totalVendido
) {
}