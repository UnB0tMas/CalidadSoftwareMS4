// ruta: src/main/java/com/upsjb/ms4/dto/reporte/response/ReporteVentaPorMetodoPagoDto.java
package com.upsjb.ms4.dto.reporte.response;

import com.upsjb.ms4.domain.enums.MetodoPago;
import java.math.BigDecimal;

public record ReporteVentaPorMetodoPagoDto(
        MetodoPago metodoPago,
        Integer cantidadVentas,
        BigDecimal totalVendido
) {
}