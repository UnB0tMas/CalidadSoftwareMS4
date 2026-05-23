// ruta: src/main/java/com/upsjb/ms4/dto/reporte/request/ReporteEmpleadoCajaRequestDto.java
package com.upsjb.ms4.dto.reporte.request;

import java.time.LocalDate;

public record ReporteEmpleadoCajaRequestDto(
        LocalDate fechaDesde,
        LocalDate fechaHasta
) {
}