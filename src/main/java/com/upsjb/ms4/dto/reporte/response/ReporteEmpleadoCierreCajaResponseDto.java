// ruta: src/main/java/com/upsjb/ms4/dto/reporte/response/ReporteEmpleadoCierreCajaResponseDto.java
package com.upsjb.ms4.dto.reporte.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReporteEmpleadoCierreCajaResponseDto(
        Long idCaja,
        String codigoCaja,
        LocalDate fechaOperacion,
        Long idEmpleadoAperturaSnapshot,
        String empleadoAperturaNombre,
        Long idEmpleadoCierreSnapshot,
        String empleadoCierreNombre,
        BigDecimal montoInicial,
        BigDecimal montoEsperadoEfectivo,
        BigDecimal montoRealEfectivo,
        BigDecimal montoTarjeta,
        BigDecimal montoTotalVendido,
        BigDecimal diferencia,
        Integer cantidadVentas,
        LocalDateTime fechaApertura,
        LocalDateTime fechaCierre
) {
}