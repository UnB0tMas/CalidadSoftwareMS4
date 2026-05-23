// ruta: src/main/java/com/upsjb/ms4/config/CorreoOutboxProperties.java
package com.upsjb.ms4.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "ms4.correo-outbox")
public record CorreoOutboxProperties(
        Boolean enabled,
        Integer batchSize,
        Integer maxAttempts,
        Duration lockTtl,
        String processorId,
        String defaultFrom,
        String adminAlertEmail
) {

    public boolean enabledSafe() {
        return enabled == null || enabled;
    }

    public int batchSizeSafe() {
        return batchSize == null || batchSize <= 0 ? 50 : batchSize;
    }

    public int maxAttemptsSafe() {
        return maxAttempts == null || maxAttempts <= 0 ? 5 : maxAttempts;
    }

    public Duration lockTtlSafe() {
        return lockTtl == null ? Duration.ofMinutes(5) : lockTtl;
    }

    public String processorIdSafe() {
        return processorId == null || processorId.isBlank()
                ? "ms4-correo-processor"
                : processorId.trim();
    }
}