// ruta: src/main/java/com/upsjb/ms4/dto/kafka/ms4/Ms4StockCommandPayloadDto.java
package com.upsjb.ms4.dto.kafka.ms4;

import java.time.LocalDateTime;

public record Ms4StockCommandPayloadDto(
        String eventId,
        String idempotencyKey,
        String eventType,
        Ms4SkuPayloadDto sku,
        Ms4AlmacenPayloadDto almacen,
        String referenciaTipo,
        String referenciaIdExterno,
        Integer cantidad,
        String codigoReserva,
        Long actorIdUsuarioMs1,
        Long actorIdEmpleadoMs2,
        String actorRol,
        LocalDateTime occurredAt,
        LocalDateTime expiresAt,
        String motivo,
        String requestId,
        String correlationId,
        String metadataJson
) {
}