// ruta: src/main/java/com/upsjb/ms4/config/OutboxProperties.java
package com.upsjb.ms4.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "ms4.outbox")
public record OutboxProperties(
        Boolean enabled,
        Integer batchSize,
        Integer maxAttempts,
        Duration lockTtl,
        Duration publishTimeout,
        Long fixedDelayMs,
        String publisherId
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
        return lockTtl == null || lockTtl.isZero() || lockTtl.isNegative()
                ? Duration.ofMinutes(5)
                : lockTtl;
    }

    public Duration publishTimeoutSafe() {
        return publishTimeout == null || publishTimeout.isZero() || publishTimeout.isNegative()
                ? Duration.ofSeconds(10)
                : publishTimeout;
    }

    public long publishTimeoutMillis() {
        return publishTimeoutSafe().toMillis();
    }

    public long fixedDelayMsSafe() {
        return fixedDelayMs == null || fixedDelayMs <= 0 ? 5000L : fixedDelayMs;
    }

    public String publisherIdSafe() {
        return publisherId == null || publisherId.isBlank()
                ? "ms4-outbox-publisher"
                : publisherId.trim();
    }
}