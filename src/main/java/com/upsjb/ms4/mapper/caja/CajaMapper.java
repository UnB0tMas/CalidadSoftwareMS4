// ruta: src/main/java/com/upsjb/ms4/mapper/caja/CajaMapper.java
package com.upsjb.ms4.mapper.caja;

import com.upsjb.ms4.domain.entity.caja.Caja;
import com.upsjb.ms4.domain.entity.snapshot.EmpleadoSnapshotMs2;
import com.upsjb.ms4.dto.caja.response.CajaCierreResponseDto;
import com.upsjb.ms4.dto.caja.response.CajaDetailResponseDto;
import com.upsjb.ms4.dto.caja.response.CajaMovimientoResponseDto;
import com.upsjb.ms4.dto.caja.response.CajaResponseDto;
import com.upsjb.ms4.dto.caja.response.CajaResumenDiaResponseDto;
import com.upsjb.ms4.dto.lookup.CajaAbiertaLookupResponseDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CajaMapper {

    public CajaResponseDto toResponse(Caja entity) {
        if (entity == null) {
            return null;
        }

        return new CajaResponseDto(
                entity.getId(),
                entity.getCodigoCaja(),
                entity.getFechaOperacion(),
                entity.getEstadoCaja(),
                entity.getMontoInicial(),
                entity.getMontoEsperadoEfectivo(),
                entity.getMontoRealEfectivo(),
                entity.getMontoTarjeta(),
                entity.getMontoTotalVendido(),
                entity.getDiferencia(),
                entity.getIdEmpleadoAperturaSnapshot(),
                entity.getIdUsuarioAperturaMs1(),
                nombreEmpleado(entity.getEmpleadoAperturaSnapshot()),
                entity.getFechaApertura(),
                entity.getIdEmpleadoCierreSnapshot(),
                entity.getIdUsuarioCierreMs1(),
                nombreEmpleado(entity.getEmpleadoCierreSnapshot()),
                entity.getFechaCierre(),
                entity.getObservacionApertura(),
                entity.getObservacionCierre(),
                entity.getEstado(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public CajaDetailResponseDto toDetailResponse(Caja entity, List<CajaMovimientoResponseDto> movimientos) {
        return new CajaDetailResponseDto(
                toResponse(entity),
                movimientos == null ? List.of() : movimientos
        );
    }

    public CajaCierreResponseDto toCierreResponse(Caja entity, Integer cantidadVentas, Integer cantidadMovimientos) {
        if (entity == null) {
            return null;
        }

        return new CajaCierreResponseDto(
                entity.getId(),
                entity.getCodigoCaja(),
                entity.getMontoInicial(),
                entity.getMontoEsperadoEfectivo(),
                entity.getMontoRealEfectivo(),
                entity.getMontoTarjeta(),
                entity.getMontoTotalVendido(),
                entity.getDiferencia(),
                cantidadVentas,
                cantidadMovimientos,
                entity.getFechaCierre(),
                entity.getObservacionCierre()
        );
    }

    public CajaResumenDiaResponseDto toResumenDiaResponse(
            Caja entity,
            Integer cantidadVentas,
            Integer cantidadPagosEfectivo,
            Integer cantidadPagosTarjeta
    ) {
        if (entity == null) {
            return null;
        }

        return new CajaResumenDiaResponseDto(
                entity.getId(),
                entity.getCodigoCaja(),
                entity.getFechaOperacion(),
                entity.getEstadoCaja(),
                entity.getMontoInicial(),
                totalEfectivoVendido(entity),
                entity.getMontoTarjeta(),
                entity.getMontoTotalVendido(),
                entity.getMontoEsperadoEfectivo(),
                entity.getMontoRealEfectivo(),
                entity.getDiferencia(),
                cantidadVentas,
                cantidadPagosEfectivo,
                cantidadPagosTarjeta
        );
    }

    public CajaAbiertaLookupResponseDto toLookup(Caja entity) {
        if (entity == null) {
            return null;
        }

        return new CajaAbiertaLookupResponseDto(
                entity.getId(),
                entity.getCodigoCaja(),
                entity.getFechaOperacion(),
                entity.getEstadoCaja(),
                entity.getMontoInicial(),
                entity.getMontoEsperadoEfectivo(),
                entity.getMontoTarjeta(),
                entity.getMontoTotalVendido(),
                entity.getIdEmpleadoAperturaSnapshot(),
                entity.getIdUsuarioAperturaMs1(),
                nombreEmpleado(entity.getEmpleadoAperturaSnapshot()),
                entity.getFechaApertura()
        );
    }

    private BigDecimal totalEfectivoVendido(Caja entity) {
        if (entity.getMontoEsperadoEfectivo() == null) {
            return null;
        }

        if (entity.getMontoInicial() == null) {
            return entity.getMontoEsperadoEfectivo();
        }

        return entity.getMontoEsperadoEfectivo().subtract(entity.getMontoInicial());
    }

    private String nombreEmpleado(EmpleadoSnapshotMs2 empleado) {
        if (empleado == null) {
            return null;
        }

        return firstNonBlank(
                empleado.getNombreCompleto(),
                joinNombre(empleado.getNombres(), empleado.getApePaterno(), empleado.getApeMaterno()),
                empleado.getCodigoEmpleado()
        );
    }

    private String joinNombre(String nombres, String apePaterno, String apeMaterno) {
        StringBuilder builder = new StringBuilder();

        append(builder, nombres);
        append(builder, apePaterno);
        append(builder, apeMaterno);

        return builder.isEmpty() ? null : builder.toString();
    }

    private void append(StringBuilder builder, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        if (!builder.isEmpty()) {
            builder.append(' ');
        }

        builder.append(value.trim());
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }

        return null;
    }
}