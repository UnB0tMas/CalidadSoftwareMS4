// ruta: src/main/java/com/upsjb/ms4/dto/reporte/response/ReporteAdminFinancieroResponseDto.java
package com.upsjb.ms4.dto.reporte.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReporteAdminFinancieroResponseDto(
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        BigDecimal ventasBrutas,
        BigDecimal ventasNetas,
        BigDecimal descuentoTotal,
        BigDecimal igvTotal,
        BigDecimal totalEfectivo,
        BigDecimal totalTarjeta,
        BigDecimal totalAnulado,
        Integer cantidadVentas,
        Integer cantidadPagos,
        ReporteGananciaEstimadaDto gananciaEstimada,
        List<ReporteVentaPorMetodoPagoDto> ventasPorMetodoPago,
        List<ReporteVentaPorCanalDto> ventasPorCanal
) {
}