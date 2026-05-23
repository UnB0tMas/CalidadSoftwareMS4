// ruta: src/main/java/com/upsjb/ms4/config/CorsProperties.java
package com.upsjb.ms4.config;

import com.upsjb.ms4.shared.constants.HeaderNames;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "ms4.cors")
public record CorsProperties(
        Boolean enabled,
        List<String> allowedOrigins,
        List<String> allowedOriginPatterns,
        List<String> allowedMethods,
        List<String> allowedHeaders,
        List<String> exposedHeaders,
        Boolean allowCredentials,
        Long maxAge
) {

    public boolean enabledSafe() {
        return enabled == null || enabled;
    }

    public List<String> allowedOriginsSafe() {
        return cleanOrDefault(allowedOrigins, List.of(
                "http://localhost:4200",
                "http://localhost:8080"
        ));
    }

    public List<String> allowedOriginPatternsSafe() {
        return cleanOrDefault(allowedOriginPatterns, List.of());
    }

    public List<String> allowedMethodsSafe() {
        return cleanOrDefault(allowedMethods, List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));
    }

    public List<String> allowedHeadersSafe() {
        return cleanOrDefault(allowedHeaders, List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                HeaderNames.REQUEST_ID,
                HeaderNames.CORRELATION_ID,
                HeaderNames.STRIPE_SIGNATURE
        ));
    }

    public List<String> exposedHeadersSafe() {
        return cleanOrDefault(exposedHeaders, List.of(
                HeaderNames.REQUEST_ID,
                HeaderNames.CORRELATION_ID,
                "Content-Disposition"
        ));
    }

    public boolean allowCredentialsSafe() {
        return allowCredentials == null || allowCredentials;
    }

    public long maxAgeSafe() {
        return maxAge == null || maxAge < 0 ? 3600L : maxAge;
    }

    private List<String> cleanOrDefault(List<String> values, List<String> fallback) {
        if (values == null || values.isEmpty()) {
            return fallback;
        }

        List<String> cleaned = values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toList();

        return cleaned.isEmpty() ? fallback : cleaned;
    }
}