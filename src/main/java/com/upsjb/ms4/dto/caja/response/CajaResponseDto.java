// ruta: src/main/java/com/upsjb/ms4/dto/caja/response/CajaResponseDto.java
package com.upsjb.ms4.dto.caja.response;

import com.upsjb.ms4.domain.enums.EstadoCaja;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CajaResponseDto(
        Long id,
        String codigoCaja,
        LocalDate fechaOperacion,
        EstadoCaja estadoCaja,
        BigDecimal montoInicial,
        BigDecimal montoEsperadoEfectivo,
        BigDecimal montoRealEfectivo,
        BigDecimal montoTarjeta,
        BigDecimal montoTotalVendido,
        BigDecimal diferencia,
        Long idEmpleadoAperturaSnapshot,
        Long idUsuarioAperturaMs1,
        String empleadoAperturaNombre,
        LocalDateTime fechaApertura,
        Long idEmpleadoCierreSnapshot,
        Long idUsuarioCierreMs1,
        String empleadoCierreNombre,
        LocalDateTime fechaCierre,
        String observacionApertura,
        String observacionCierre,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}