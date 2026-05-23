// ruta: src/main/java/com/upsjb/ms4/dto/kafka/ms3/SkuSnapshotPayloadDto.java
package com.upsjb.ms4.dto.kafka.ms3;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record SkuSnapshotPayloadDto(
        Long idSku,
        Long idProducto,
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
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<Map<String, Object>> atributos
) {
}