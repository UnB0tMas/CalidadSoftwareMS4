// ruta: src/main/java/com/upsjb/ms4/dto/reporte/response/ReporteAdminVentasResponseDto.java
package com.upsjb.ms4.dto.reporte.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReporteAdminVentasResponseDto(
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        BigDecimal ventasTotales,
        BigDecimal ventasNetas,
        BigDecimal ventasBrutas,
        BigDecimal descuentoTotal,
        BigDecimal igvTotal,
        Integer cantidadVentas,
        BigDecimal ticketPromedio,
        Integer ventasOnline,
        Integer ventasFisicas,
        List<ReporteProductoVendidoDto> productosMasVendidos,
        List<ReporteVentaPorEmpleadoDto> ventasPorEmpleado,
        List<ReporteVentaPorMetodoPagoDto> ventasPorMetodoPago,
        List<ReporteVentaPorCanalDto> ventasPorCanal,
        List<ReporteVentaPorCategoriaDto> ventasPorCategoria
) {
}