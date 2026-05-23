// ruta: src/main/java/com/upsjb/ms4/mapper/caja/CajaMovimientoMapper.java
package com.upsjb.ms4.mapper.caja;

import com.upsjb.ms4.domain.entity.caja.CajaMovimiento;
import com.upsjb.ms4.dto.caja.response.CajaMovimientoResponseDto;
import org.springframework.stereotype.Component;

@Component
public class CajaMovimientoMapper {

    public CajaMovimientoResponseDto toResponse(CajaMovimiento entity) {
        if (entity == null) return null;

        return new CajaMovimientoResponseDto(
                entity.getId(),
                entity.getIdCaja(),
                entity.getCaja() != null ? entity.getCaja().getCodigoCaja() : null,
                entity.getIdVenta(),
                entity.getVenta() != null ? entity.getVenta().getCodigoVenta() : null,
                entity.getIdPago(),
                entity.getPago() != null ? entity.getPago().getCodigoPago() : null,
                entity.getTipoMovimiento(),
                entity.getMonto(),
                entity.getDescripcion(),
                entity.getActorIdUsuarioMs1(),
                entity.getActorRol(),
                entity.getRequestId(),
                entity.getCorrelationId(),
                entity.getEstado(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public CajaMovimientoResponseDto toDetailResponse(CajaMovimiento entity) {
        return toResponse(entity);
    }
}