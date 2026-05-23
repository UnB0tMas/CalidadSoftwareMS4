// ruta: src/main/java/com/upsjb/ms4/dto/reporte/response/ReporteVentaPorEmpleadoDto.java
package com.upsjb.ms4.dto.reporte.response;

import java.math.BigDecimal;

public record ReporteVentaPorEmpleadoDto(
        Long idEmpleadoMs2,
        Long idUsuarioEmpleadoMs1,
        String codigoEmpleado,
        String nombreEmpleado,
        Integer cantidadVentas,
        BigDecimal totalVendido,
        BigDecimal totalEfectivo,
        BigDecimal totalTarjeta
) {
}