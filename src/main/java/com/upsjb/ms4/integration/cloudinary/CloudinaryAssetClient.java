// ruta: src/main/java/com/upsjb/ms4/integration/cloudinary/CloudinaryAssetClient.java
package com.upsjb.ms4.integration.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.upsjb.ms4.config.CloudinaryProperties;
import com.upsjb.ms4.domain.enums.ResourceTypeCloudinary;
import com.upsjb.ms4.shared.exception.ExternalServiceException;
import com.upsjb.ms4.util.HashUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class CloudinaryAssetClient {

    private static final String PDF_EXTENSION = ".pdf";
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final CloudinaryProperties properties;
    private final CloudinaryExceptionTranslator exceptionTranslator;

    public CloudinaryAssetClient(CloudinaryProperties properties,
                                 CloudinaryExceptionTranslator exceptionTranslator) {
        this.properties = properties;
        this.exceptionTranslator = exceptionTranslator;
    }

    public CloudinaryUploadResult uploadVisualAsset(MultipartFile file,
                                                    String folder,
                                                    String publicId,
                                                    boolean overwrite) {
        validateConfiguration();
        validateVisualAsset(file);

        try {
            byte[] bytes = file.getBytes();
            String normalizedFolder = normalizeFolder(folder);
            String normalizedPublicId = normalizePublicId(publicId);

            Map<String, Object> options = new LinkedHashMap<>();
            options.put("folder", normalizedFolder);
            options.put("overwrite", overwrite);
            options.put("invalidate", overwrite);
            options.put("resource_type", ResourceTypeCloudinary.IMAGE.getCode());

            if (normalizedPublicId != null) {
                options.put("public_id", normalizedPublicId);
            } else {
                options.put("use_filename", true);
                options.put("unique_filename", true);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary().uploader().upload(bytes, options);

            return new CloudinaryUploadResult(
                    asString(result.get("public_id")),
                    asString(result.get("secure_url")),
                    asString(result.get("url")),
                    asString(result.get("version")),
                    asLong(result.get("bytes")),
                    ResourceTypeCloudinary.IMAGE,
                    normalizedFolder,
                    HashUtil.sha256(bytes)
            );
        } catch (Exception ex) {
            throw exceptionTranslator.translate(ex);
        }
    }

    public void deleteAsset(String publicId, ResourceTypeCloudinary resourceType) {
        validateConfiguration();

        String normalizedPublicId = normalizePublicId(publicId);

        if (normalizedPublicId == null) {
            throw new ExternalServiceException("El publicId de Cloudinary es obligatorio.");
        }

        try {
            ResourceTypeCloudinary safeResourceType = resourceType == null
                    ? ResourceTypeCloudinary.IMAGE
                    : resourceType;

            cloudinary().uploader().destroy(normalizedPublicId, ObjectUtils.asMap(
                    "resource_type", safeResourceType.getCode(),
                    "invalidate", true
            ));
        } catch (Exception ex) {
            throw exceptionTranslator.translate(ex);
        }
    }

    private Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", properties.cloudNameSafe(),
                "api_key", properties.apiKeySafe(),
                "api_secret", properties.apiSecretSafe(),
                "secure", true
        ));
    }

    private void validateConfiguration() {
        if (!properties.enabledSafe()) {
            throw new ExternalServiceException("Cloudinary se encuentra deshabilitado.");
        }

        if (!properties.configured()) {
            throw new ExternalServiceException("La configuración de Cloudinary está incompleta.");
        }
    }

    private void validateVisualAsset(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ExternalServiceException("El asset visual es obligatorio.");
        }

        String contentType = normalizeLower(file.getContentType());
        String filename = normalizeLower(file.getOriginalFilename());

        if (PDF_CONTENT_TYPE.equals(contentType) || filename.endsWith(PDF_EXTENSION)) {
            throw new ExternalServiceException("MS4 no permite subir PDFs de boleta a Cloudinary.");
        }

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ExternalServiceException("Cloudinary en MS4 solo acepta assets visuales de imagen.");
        }
    }

    private String normalizeFolder(String folder) {
        String value = folder == null || folder.isBlank()
                ? properties.folderBaseSafe()
                : folder.trim();

        String normalized = value.replace("\\", "/")
                .replaceAll("/{2,}", "/");

        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized.isBlank() ? properties.folderBaseSafe() : normalized;
    }

    private String normalizePublicId(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return null;
        }

        String normalized = publicId.trim()
                .replace("\\", "/")
                .replaceAll("/{2,}", "/");

        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        if (normalized.toLowerCase(Locale.ROOT).endsWith(PDF_EXTENSION)) {
            throw new ExternalServiceException("MS4 no permite publicId de PDF para assets Cloudinary.");
        }

        int lastSlash = normalized.lastIndexOf('/');
        int lastDot = normalized.lastIndexOf('.');

        if (lastDot > lastSlash) {
            normalized = normalized.substring(0, lastDot);
        }

        return normalized.isBlank() ? null : normalized;
    }

    private String normalizeLower(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }

        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }

        return Long.valueOf(String.valueOf(value));
    }

    public record CloudinaryUploadResult(
            String publicId,
            String secureUrl,
            String url,
            String versionCloudinary,
            Long bytes,
            ResourceTypeCloudinary resourceType,
            String folder,
            String hashSha256
    ) {
    }
}