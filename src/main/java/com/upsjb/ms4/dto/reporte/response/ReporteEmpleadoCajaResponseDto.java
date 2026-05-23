// ruta: src/main/java/com/upsjb/ms4/dto/reporte/response/ReporteEmpleadoCajaResponseDto.java
package com.upsjb.ms4.dto.reporte.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReporteEmpleadoCajaResponseDto(
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        Long idEmpleadoMs2,
        Long idUsuarioEmpleadoMs1,
        String codigoEmpleado,
        String nombreEmpleado,
        Long idCaja,
        String codigoCaja,
        BigDecimal montoInicial,
        BigDecimal totalEfectivoVendido,
        BigDecimal totalTarjetaVendido,
        BigDecimal totalVendido,
        Integer cantidadTransacciones,
        Integer cantidadVentasAnuladas,
        BigDecimal diferenciaCaja,
        List<ReporteProductoVendidoDto> productosVendidos
) {
}