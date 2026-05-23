// ruta: src/main/java/com/upsjb/ms4/dto/reporte/filter/ReporteVentasAdminFilterDto.java
package com.upsjb.ms4.dto.reporte.filter;

import com.upsjb.ms4.domain.enums.CanalVenta;
import com.upsjb.ms4.domain.enums.MetodoPago;
import java.time.LocalDate;

public record ReporteVentasAdminFilterDto(
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        CanalVenta canalVenta,
        MetodoPago metodoPago,
        Long idEmpleadoMs2,
        Long idClienteMs2,
        Long idCategoriaMs3,
        Long idProductoMs3,
        Long idSkuMs3
) {
}