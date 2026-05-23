// ruta: src/main/java/com/upsjb/ms4/dto/caja/response/CajaCierreResponseDto.java
package com.upsjb.ms4.dto.caja.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CajaCierreResponseDto(
        Long idCaja,
        String codigoCaja,
        BigDecimal montoInicial,
        BigDecimal montoEsperadoEfectivo,
        BigDecimal montoRealEfectivo,
        BigDecimal montoTarjeta,
        BigDecimal montoTotalVendido,
        BigDecimal diferencia,
        Integer cantidadVentas,
        Integer cantidadMovimientos,
        LocalDateTime fechaCierre,
        String observacionCierre
) {
}