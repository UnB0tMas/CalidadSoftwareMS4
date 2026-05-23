// ruta: src/main/java/com/upsjb/ms4/mapper/kafka/InventarioEventoPendienteMapper.java
package com.upsjb.ms4.mapper.kafka;

import com.upsjb.ms4.domain.entity.kafka.InventarioEventoPendienteMs4;
import com.upsjb.ms4.dto.contingencia.response.InventarioEventoPendienteResponseDto;
import org.springframework.stereotype.Component;

@Component
public class InventarioEventoPendienteMapper {

    public InventarioEventoPendienteResponseDto toResponse(InventarioEventoPendienteMs4 entity) {
        if (entity == null) {
            return null;
        }

        return new InventarioEventoPendienteResponseDto(
                entity.getId(),
                entity.getIdVenta(),
                entity.getCodigoVenta(),
                entity.getIdVentaDetalle(),
                entity.getTipoEvento(),
                entity.getTopicDestino(),
                entity.getPayloadJson(),
                entity.getEstadoSincronizacion(),
                entity.getIdempotencyKey(),
                entity.getCantidadReintentos(),
                entity.getUltimoError(),
                entity.getFechaCreacion(),
                entity.getFechaUltimoReintento(),
                entity.getFechaSincronizacion(),
                entity.getRequestId(),
                entity.getCorrelationId(),
                entity.getEstado(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}