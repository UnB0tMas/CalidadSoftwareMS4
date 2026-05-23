// ruta: src/main/java/com/upsjb/ms4/dto/config/response/AssetCloudinaryResponseDto.java
package com.upsjb.ms4.dto.config.response;

import com.upsjb.ms4.domain.enums.ResourceTypeCloudinary;
import java.time.LocalDateTime;

public record AssetCloudinaryResponseDto(
        Long id,
        String entidadOrigen,
        Long idEntidadOrigen,
        String tipoAsset,
        String nombreArchivo,
        String extension,
        String contentType,
        ResourceTypeCloudinary resourceType,
        String folder,
        String publicId,
        String secureUrl,
        String url,
        String versionCloudinary,
        Long bytes,
        String hashSha256,
        Long subidoPorIdUsuarioMs1,
        String requestId,
        String correlationId,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}