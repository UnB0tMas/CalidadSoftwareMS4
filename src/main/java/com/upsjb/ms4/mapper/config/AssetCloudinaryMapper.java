// ruta: src/main/java/com/upsjb/ms4/mapper/config/AssetCloudinaryMapper.java
package com.upsjb.ms4.mapper.config;

import com.upsjb.ms4.domain.entity.config.AssetCloudinary;
import com.upsjb.ms4.dto.config.request.AssetCloudinaryUploadRequestDto;
import com.upsjb.ms4.dto.config.response.AssetCloudinaryResponseDto;
import org.springframework.stereotype.Component;

@Component
public class AssetCloudinaryMapper {

    public AssetCloudinaryResponseDto toResponse(AssetCloudinary entity) {
        if (entity == null) return null;

        return new AssetCloudinaryResponseDto(
                entity.getId(),
                entity.getEntidadOrigen(),
                entity.getIdEntidadOrigen(),
                entity.getTipoAsset(),
                entity.getNombreArchivo(),
                entity.getExtension(),
                entity.getContentType(),
                entity.getResourceType(),
                entity.getFolder(),
                entity.getPublicId(),
                entity.getSecureUrl(),
                entity.getUrl(),
                entity.getVersionCloudinary(),
                entity.getBytes(),
                entity.getHashSha256(),
                entity.getSubidoPorIdUsuarioMs1(),
                entity.getRequestId(),
                entity.getCorrelationId(),
                entity.getEstado(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public AssetCloudinaryResponseDto toDetailResponse(AssetCloudinary entity) {
        return toResponse(entity);
    }

    public AssetCloudinary toEntity(
            AssetCloudinaryUploadRequestDto request,
            String nombreArchivo,
            String extension,
            String contentType,
            String publicId,
            String secureUrl,
            String url,
            String versionCloudinary,
            Long bytes,
            String hashSha256
    ) {
        if (request == null) return null;

        AssetCloudinary entity = new AssetCloudinary();
        updateEntity(entity, request);
        entity.setNombreArchivo(nombreArchivo);
        entity.setExtension(extension);
        entity.setContentType(contentType);
        entity.setPublicId(publicId);
        entity.setSecureUrl(secureUrl);
        entity.setUrl(url);
        entity.setVersionCloudinary(versionCloudinary);
        entity.setBytes(bytes);
        entity.setHashSha256(hashSha256);
        return entity;
    }

    public void updateEntity(AssetCloudinary entity, AssetCloudinaryUploadRequestDto request) {
        if (entity == null || request == null) return;

        entity.setEntidadOrigen(request.entidadOrigen());
        entity.setIdEntidadOrigen(request.idEntidadOrigen());
        entity.setTipoAsset(request.tipoAsset());
        entity.setFolder(request.folder());
        entity.setResourceType(request.resourceType());
    }
}