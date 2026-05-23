package com.upsjb.ms4.domain.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;

public enum ResourceTypeCloudinary {

    IMAGE("image", "Imagen"),
    RAW("raw", "Recurso raw"),
    VIDEO("video", "Video");

    private final String code;
    private final String label;

    ResourceTypeCloudinary(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static ResourceTypeCloudinary fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El resource_type de Cloudinary es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Resource type Cloudinary no válido: " + code));
    }

    @Converter(autoApply = false)
    public static class ResourceTypeCloudinaryConverter implements AttributeConverter<ResourceTypeCloudinary, String> {

        @Override
        public String convertToDatabaseColumn(ResourceTypeCloudinary attribute) {
            return attribute == null ? null : attribute.getCode();
        }

        @Override
        public ResourceTypeCloudinary convertToEntityAttribute(String dbData) {
            return dbData == null ? null : ResourceTypeCloudinary.fromCode(dbData);
        }
    }
}