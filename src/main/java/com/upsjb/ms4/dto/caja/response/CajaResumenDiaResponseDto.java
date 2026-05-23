// ruta: src/main/java/com/upsjb/ms4/dto/caja/response/CajaResumenDiaResponseDto.java
package com.upsjb.ms4.dto.caja.response;

import com.upsjb.ms4.domain.enums.EstadoCaja;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CajaResumenDiaResponseDto(
        Long idCaja,
        String codigoCaja,
        LocalDate fechaOperacion,
        EstadoCaja estadoCaja,
        BigDecimal montoInicial,
        BigDecimal totalEfectivo,
        BigDecimal totalTarjeta,
        BigDecimal totalVendido,
        BigDecimal montoEsperadoEfectivo,
        BigDecimal montoRealEfectivo,
        BigDecimal diferencia,
        Integer cantidadVentas,
        Integer cantidadPagosEfectivo,
        Integer cantidadPagosTarjeta
) {
}