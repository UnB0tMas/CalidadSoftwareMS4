// ruta: src/main/java/com/upsjb/ms4/dto/kafka/ms3/PromocionSnapshotPayloadDto.java
package com.upsjb.ms4.dto.kafka.ms3;

import java.time.LocalDateTime;
import java.util.List;

public record PromocionSnapshotPayloadDto(
        Long idPromocion,
        String codigo,
        String nombre,
        String descripcion,
        Long creadoPorIdUsuarioMs1,
        Long idPromocionVersion,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        String estadoPromocion,
        Boolean visiblePublico,
        Boolean vigente,
        String motivo,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<PromocionSkuDescuentoPayloadDto> descuentos
) {
}