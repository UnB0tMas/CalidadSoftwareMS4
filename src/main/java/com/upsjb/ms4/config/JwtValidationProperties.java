// ruta: src/main/java/com/upsjb/ms4/config/JwtValidationProperties.java
package com.upsjb.ms4.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@ConfigurationProperties(prefix = "ms4.security.jwt")
public record JwtValidationProperties(
        Boolean enabled,
        Boolean requireEmail,
        Boolean requireTokenType,
        String requiredAudience,
        String requiredIssuer,
        Set<String> allowedRoles,
        Set<String> acceptedTokenTypes
) {

    private static final Set<String> DEFAULT_ALLOWED_ROLES = Set.of(
            "ADMIN",
            "EMPLEADO",
            "CLIENTE"
    );

    private static final Set<String> DEFAULT_ACCEPTED_TOKEN_TYPES = Set.of(
            "access",
            "access_token",
            "bearer"
    );

    public boolean enabledSafe() {
        return enabled == null || enabled;
    }

    public boolean requireEmailSafe() {
        return requireEmail == null || requireEmail;
    }

    public boolean requireTokenTypeSafe() {
        return Boolean.TRUE.equals(requireTokenType);
    }

    public String requiredAudienceSafe() {
        return trimToNull(requiredAudience);
    }

    public String requiredIssuerSafe() {
        return trimToNull(requiredIssuer);
    }

    public Set<String> allowedRolesSafe() {
        return normalizeUpperOrDefault(allowedRoles, DEFAULT_ALLOWED_ROLES);
    }

    public Set<String> acceptedTokenTypesSafe() {
        if (acceptedTokenTypes == null || acceptedTokenTypes.isEmpty()) {
            return DEFAULT_ACCEPTED_TOKEN_TYPES;
        }

        Set<String> values = acceptedTokenTypes.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());

        return values.isEmpty() ? DEFAULT_ACCEPTED_TOKEN_TYPES : values;
    }

    private Set<String> normalizeUpperOrDefault(Set<String> values, Set<String> fallback) {
        if (values == null || values.isEmpty()) {
            return fallback;
        }

        Set<String> normalized = values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(JwtValidationProperties::normalizeRole)
                .collect(Collectors.toUnmodifiableSet());

        return normalized.isEmpty() ? fallback : normalized;
    }

    private static String normalizeRole(String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return normalized.startsWith("ROLE_") ? normalized.substring(5) : normalized;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}