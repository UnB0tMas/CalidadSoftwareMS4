// ruta: src/main/java/com/upsjb/ms4/dto/snapshot/response/SkuVentaResponseDto.java
package com.upsjb.ms4.dto.snapshot.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SkuVentaResponseDto(
        Long id,
        Long idSkuMs3,
        Long idProductoMs3,
        String codigoProducto,
        String codigoSku,
        String barcode,
        String color,
        String talla,
        String material,
        String modelo,
        Integer stockMinimo,
        Integer stockMaximo,
        BigDecimal pesoGramos,
        BigDecimal altoCm,
        BigDecimal anchoCm,
        BigDecimal largoCm,
        String estadoSku,
        String atributosJson,
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