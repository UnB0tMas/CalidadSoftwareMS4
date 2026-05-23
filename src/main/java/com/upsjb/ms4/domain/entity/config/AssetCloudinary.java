package com.upsjb.ms4.domain.entity.config;

import com.upsjb.ms4.domain.entity.base.BaseEntity;
import com.upsjb.ms4.domain.enums.ResourceTypeCloudinary;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "asset_cloudinary")
public class AssetCloudinary extends BaseEntity {

    @Column(name = "entidad_origen", nullable = false, length = 80)
    private String entidadOrigen;

    @Column(name = "id_entidad_origen")
    private Long idEntidadOrigen;

    @Column(name = "tipo_asset", nullable = false, length = 60)
    private String tipoAsset;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "extension", nullable = false, length = 20)
    private String extension;

    @Column(name = "content_type", nullable = false, length = 120)
    private String contentType;

    @Convert(converter = ResourceTypeCloudinary.ResourceTypeCloudinaryConverter.class)
    @Column(name = "resource_type", nullable = false, length = 20)
    private ResourceTypeCloudinary resourceType;

    @Column(name = "folder", nullable = false, length = 255)
    private String folder;

    @Column(name = "public_id", nullable = false, unique = true, length = 500)
    private String publicId;

    @Column(name = "secure_url", nullable = false, length = 1000)
    private String secureUrl;

    @Column(name = "url", length = 1000)
    private String url;

    @Column(name = "version_cloudinary", length = 80)
    private String versionCloudinary;

    @Column(name = "bytes")
    private Long bytes;

    @Column(name = "hash_sha256", length = 128)
    private String hashSha256;

    @Column(name = "subido_por_id_usuario_ms1")
    private Long subidoPorIdUsuarioMs1;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;
}