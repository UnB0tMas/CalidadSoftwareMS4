// ruta: src/main/java/com/upsjb/ms4/dto/kafka/ms3/ProductoImagenSnapshotPayloadDto.java
package com.upsjb.ms4.dto.kafka.ms3;

import java.time.LocalDateTime;

public record ProductoImagenSnapshotPayloadDto(
        Long idImagen,
        Long idProducto,
        Long idSku,
        String codigoSku,
        String cloudinaryAssetId,
        String cloudinaryPublicId,
        Long cloudinaryVersion,
        String secureUrl,
        String url,
        String resourceType,
        String format,
        Long bytes,
        Integer width,
        Integer height,
        String folder,
        String originalFilename,
        String altText,
        String titulo,
        Integer orden,
        Boolean principal,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}