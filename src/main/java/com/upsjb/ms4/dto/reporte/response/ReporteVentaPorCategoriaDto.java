// ruta: src/main/java/com/upsjb/ms4/dto/reporte/response/ReporteVentaPorCategoriaDto.java
package com.upsjb.ms4.dto.reporte.response;

import java.math.BigDecimal;

public record ReporteVentaPorCategoriaDto(
        Long idCategoriaMs3,
        String codigoCategoria,
        String nombreCategoria,
        Integer cantidadProductosVendidos,
        Integer cantidadVentas,
        BigDecimal totalVendido
) {
}