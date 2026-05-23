// ruta: src/main/java/com/upsjb/ms4/dto/kafka/ms3/PromocionSkuDescuentoPayloadDto.java
package com.upsjb.ms4.dto.kafka.ms3;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PromocionSkuDescuentoPayloadDto(
        Long idPromocionSkuDescuentoVersion,
        Long idPromocionVersion,
        Long idPromocion,
        Long idSku,
        String codigoSku,
        Long idProducto,
        String codigoProducto,
        String nombreProducto,
        String tipoDescuento,
        BigDecimal valorDescuento,
        BigDecimal precioFinalEstimado,
        BigDecimal margenEstimado,
        Integer limiteUnidades,
        Integer prioridad,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}