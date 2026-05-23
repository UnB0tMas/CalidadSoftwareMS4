// ruta: src/main/java/com/upsjb/ms4/dto/reporte/filter/ReporteFinancieroAdminFilterDto.java
package com.upsjb.ms4.dto.reporte.filter;

import com.upsjb.ms4.domain.enums.CanalVenta;
import com.upsjb.ms4.domain.enums.MetodoPago;
import java.time.LocalDate;

public record ReporteFinancieroAdminFilterDto(
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        CanalVenta canalVenta,
        MetodoPago metodoPago
) {
}