// ruta: src/main/java/com/upsjb/ms4/dto/kafka/ms3/StockSnapshotPayloadDto.java
package com.upsjb.ms4.dto.kafka.ms3;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StockSnapshotPayloadDto(
        Long idStock,
        Long idSku,
        String codigoSku,
        String barcode,
        Long idProducto,
        String codigoProducto,
        String nombreProducto,
        Long idAlmacen,
        String codigoAlmacen,
        String nombreAlmacen,
        Integer stockFisico,
        Integer stockReservado,
        Integer stockDisponible,
        Integer stockMinimo,
        Integer stockMaximo,
        BigDecimal costoPromedioActual,
        BigDecimal ultimoCostoCompra,
        Boolean bajoStock,
        Boolean sobreStock,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}