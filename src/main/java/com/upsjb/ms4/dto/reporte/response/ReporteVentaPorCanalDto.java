// ruta: src/main/java/com/upsjb/ms4/dto/reporte/response/ReporteVentaPorCanalDto.java
package com.upsjb.ms4.dto.reporte.response;

import com.upsjb.ms4.domain.enums.CanalVenta;
import java.math.BigDecimal;

public record ReporteVentaPorCanalDto(
        CanalVenta canalVenta,
        Integer cantidadVentas,
        BigDecimal totalVendido
) {
}