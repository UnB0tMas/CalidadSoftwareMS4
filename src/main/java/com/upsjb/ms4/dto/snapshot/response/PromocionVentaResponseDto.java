// ruta: src/main/java/com/upsjb/ms4/dto/snapshot/response/PromocionVentaResponseDto.java
package com.upsjb.ms4.dto.snapshot.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record PromocionVentaResponseDto(
        Long id,
        Long idPromocionMs3,
        String codigoPromocion,
        String nombre,
        String descripcion,
        Long creadoPorIdUsuarioMs1,
        Long idPromocionVersionMs3,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        String estadoPromocion,
        Boolean visiblePublico,
        Boolean vigente,
        String motivo,
        String descuentosJson,
        UUID eventId,
        String eventType,
        String aggregateId,
        Integer eventVersion,
        LocalDateTime occurredAt,
        LocalDateTime fechaSincronizacion,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}