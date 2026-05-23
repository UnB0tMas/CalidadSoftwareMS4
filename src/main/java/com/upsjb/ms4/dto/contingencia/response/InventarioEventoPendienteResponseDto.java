// ruta: src/main/java/com/upsjb/ms4/dto/contingencia/response/InventarioEventoPendienteResponseDto.java
package com.upsjb.ms4.dto.contingencia.response;

import com.upsjb.ms4.domain.enums.EstadoSincronizacionInventario;
import com.upsjb.ms4.domain.enums.TipoComandoStock;
import java.time.LocalDateTime;

public record InventarioEventoPendienteResponseDto(
        Long id,
        Long idVenta,
        String codigoVenta,
        Long idVentaDetalle,
        TipoComandoStock tipoEvento,
        String topicDestino,
        String payloadJson,
        EstadoSincronizacionInventario estadoSincronizacion,
        String idempotencyKey,
        Integer cantidadReintentos,
        String ultimoError,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaUltimoReintento,
        LocalDateTime fechaSincronizacion,
        String requestId,
        String correlationId,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}