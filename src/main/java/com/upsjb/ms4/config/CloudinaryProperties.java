// ruta: src/main/java/com/upsjb/ms4/config/CloudinaryProperties.java
package com.upsjb.ms4.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloudinary")
public record CloudinaryProperties(
        Boolean enabled,
        String cloudName,
        String apiKey,
        String apiSecret,
        String folderBase
) {

    public boolean enabledSafe() {
        return enabled == null || enabled;
    }

    public String cloudNameSafe() {
        return trimToNull(cloudName);
    }

    public String apiKeySafe() {
        return trimToNull(apiKey);
    }

    public String apiSecretSafe() {
        return trimToNull(apiSecret);
    }

    public String folderBaseSafe() {
        String value = trimToNull(folderBase);
        return value == null ? "ms4/assets" : normalizeFolder(value);
    }

    public boolean configured() {
        return cloudNameSafe() != null
                && apiKeySafe() != null
                && apiSecretSafe() != null;
    }

    public void requireConfiguredIfEnabled() {
        if (enabledSafe() && !configured()) {
            throw new IllegalStateException(
                    "Cloudinary está habilitado, pero cloudName, apiKey o apiSecret no están configurados."
            );
        }
    }

    private String normalizeFolder(String value) {
        String normalized = value.trim()
                .replace("\\", "/")
                .replaceAll("/{2,}", "/");

        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized.isBlank() ? "ms4/assets" : normalized;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}