// ruta: src/main/java/com/upsjb/ms4/mapper/contingencia/ContingenciaMapper.java
package com.upsjb.ms4.mapper.contingencia;

import com.upsjb.ms4.domain.entity.contingencia.ModoContingencia;
import com.upsjb.ms4.domain.entity.kafka.InventarioEventoPendienteMs4;
import com.upsjb.ms4.dto.contingencia.response.ContingenciaReconciliacionResponseDto;
import com.upsjb.ms4.dto.contingencia.response.InventarioEventoPendienteResponseDto;
import com.upsjb.ms4.dto.contingencia.response.ModoContingenciaResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ContingenciaMapper {

    public ModoContingenciaResponseDto toModoResponse(ModoContingencia entity) {
        if (entity == null) {
            return null;
        }

        return new ModoContingenciaResponseDto(
                entity.getId(),
                entity.getServicioAfectado(),
                entity.getEstadoContingencia(),
                entity.getFechaInicio(),
                entity.getFechaFin(),
                entity.getActivadoPorIdUsuarioMs1(),
                entity.getActivadoPorRol(),
                entity.getMotivo(),
                entity.getVentasPermitidas(),
                entity.getGuardarEventosPendientes(),
                entity.getTotalEventosPendientes(),
                entity.getObservacion(),
                entity.getEstado(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public InventarioEventoPendienteResponseDto toEventoPendienteResponse(InventarioEventoPendienteMs4 entity) {
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

    public ContingenciaReconciliacionResponseDto toReconciliacionResponse(long totalReintentables,
                                                                          long totalReprogramados,
                                                                          long totalPendientesPosteriores) {
        String mensaje = totalReprogramados == 1
                ? "Se reprogramó 1 evento de inventario pendiente."
                : "Se reprogramaron " + totalReprogramados + " eventos de inventario pendientes.";

        return new ContingenciaReconciliacionResponseDto(
                totalReintentables,
                totalReprogramados,
                totalPendientesPosteriores,
                mensaje
        );
    }
}