// ruta: src/main/java/com/upsjb/ms4/dto/reporte/filter/ReporteEmpleadoCajaFilterDto.java
package com.upsjb.ms4.dto.reporte.filter;

import java.time.LocalDate;

public record ReporteEmpleadoCajaFilterDto(
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        Long idEmpleadoMs2,
        Long idUsuarioEmpleadoMs1
) {
}