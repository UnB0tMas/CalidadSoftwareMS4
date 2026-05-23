// ruta: src/main/java/com/upsjb/ms4/dto/lookup/CajaAbiertaLookupResponseDto.java
package com.upsjb.ms4.dto.lookup;

import com.upsjb.ms4.domain.enums.EstadoCaja;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CajaAbiertaLookupResponseDto(
        Long id,
        String codigoCaja,
        LocalDate fechaOperacion,
        EstadoCaja estadoCaja,
        BigDecimal montoInicial,
        BigDecimal montoEsperadoEfectivo,
        BigDecimal montoTarjeta,
        BigDecimal montoTotalVendido,
        Long idEmpleadoAperturaSnapshot,
        Long idUsuarioAperturaMs1,
        String empleadoAperturaNombre,
        LocalDateTime fechaApertura
) {
}