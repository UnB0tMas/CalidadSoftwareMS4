// ruta: src/main/java/com/upsjb/ms4/security/jwt/JwtClaimExtractor.java
package com.upsjb.ms4.security.jwt;

import com.upsjb.ms4.security.roles.SecurityRoles;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class JwtClaimExtractor {

    public Long idUsuarioMs1(Jwt jwt) {
        Object value = first(jwt, "id_usuario_ms1", "idUsuarioMs1", "id_usuario", "user_id", "uid");
        return toLong(value);
    }

    public String username(Jwt jwt) {
        Object value = first(jwt, "username", "preferred_username", "sub");
        return trimToNull(toStringValue(value));
    }

    public String email(Jwt jwt) {
        Object value = first(jwt, "email", "correo");
        return trimToNull(toStringValue(value));
    }

    public String rol(Jwt jwt) {
        Object directRole = first(jwt, "rol", "role");
        String normalizedDirectRole = normalizeRole(toStringValue(directRole));

        if (SecurityRoles.isSupportedRole(normalizedDirectRole)) {
            return normalizedDirectRole;
        }

        Object roles = first(jwt, "roles", "authorities");
        String roleFromCollection = roleFromCollection(roles);

        if (SecurityRoles.isSupportedRole(roleFromCollection)) {
            return roleFromCollection;
        }

        return normalizedDirectRole;
    }

    public String sid(Jwt jwt) {
        return trimToNull(toStringValue(first(jwt, "sid", "session_id")));
    }

    public String tokenType(Jwt jwt) {
        return trimToNull(toStringValue(first(jwt, "token_type", "tokenType", "typ", "type")));
    }

    public Set<String> authorities(Jwt jwt) {
        Set<String> result = new LinkedHashSet<>();

        addGrantedAuthorities(result, first(jwt, "authorities"));
        addGrantedAuthorities(result, first(jwt, "roles"));
        addScopes(result, first(jwt, "scope"));
        addScopes(result, first(jwt, "scp"));

        String role = rol(jwt);
        if (role != null && !role.isBlank()) {
            result.add(SecurityRoles.asRoleAuthority(role));
        }

        result.removeIf(value -> value == null || value.isBlank());
        return Set.copyOf(result);
    }

    private Object first(Jwt jwt, String... claimNames) {
        if (jwt == null || claimNames == null) {
            return null;
        }

        for (String claimName : claimNames) {
            Object value = jwt.getClaim(claimName);
            if (value != null) {
                return value;
            }
        }

        return null;
    }

    private String roleFromCollection(Object value) {
        for (String item : asStringList(value)) {
            String normalized = normalizeRole(item);
            if (SecurityRoles.isSupportedRole(normalized)) {
                return normalized;
            }
        }

        return null;
    }

    private void addGrantedAuthorities(Set<String> result, Object value) {
        for (String item : asStringList(value)) {
            addGrantedAuthority(result, item);
        }
    }

    private void addScopes(Set<String> result, Object value) {
        for (String item : asStringList(value)) {
            String normalized = item.trim();

            if (normalized.isBlank()) {
                continue;
            }

            if (normalized.toUpperCase(Locale.ROOT).startsWith("SCOPE_")) {
                result.add("SCOPE_" + normalized.substring(6));
            } else {
                result.add("SCOPE_" + normalized);
            }
        }
    }

    private void addGrantedAuthority(Set<String> result, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        String normalized = value.trim();
        String upper = normalized.toUpperCase(Locale.ROOT);

        if (upper.startsWith("ROLE_")) {
            result.add("ROLE_" + normalizeRole(normalized));
            return;
        }

        if (upper.startsWith("SCOPE_")) {
            result.add("SCOPE_" + normalized.substring(6));
            return;
        }

        result.add(SecurityRoles.asRoleAuthority(normalized));
    }

    private List<String> asStringList(Object value) {
        if (value == null) {
            return List.of();
        }

        List<String> result = new ArrayList<>();

        if (value instanceof String text) {
            for (String item : text.split("[,\\s]+")) {
                if (item != null && !item.isBlank()) {
                    result.add(item.trim());
                }
            }

            return result;
        }

        if (value instanceof Collection<?> collection) {
            for (Object item : collection) {
                String text = toStringValue(item);
                if (text != null && !text.isBlank()) {
                    result.add(text.trim());
                }
            }
        }

        return result;
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }

        String value = role.trim().toUpperCase(Locale.ROOT);
        return value.startsWith("ROLE_") ? value.substring(5) : value;
    }

    private Long toLong(Object value) {
        if (value instanceof Long id) {
            return id;
        }

        if (value instanceof Integer id) {
            return id.longValue();
        }

        if (value instanceof Number id) {
            return id.longValue();
        }

        if (value instanceof String id && !id.isBlank()) {
            try {
                return Long.valueOf(id.trim());
            } catch (NumberFormatException ex) {
                return null;
            }
        }

        return null;
    }

    private String toStringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}