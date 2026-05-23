// ruta: src/main/java/com/upsjb/ms4/security/roles/SecurityRoles.java
package com.upsjb.ms4.security.roles;

import java.util.Locale;
import java.util.Set;

public final class SecurityRoles {

    private SecurityRoles() {
    }

    public static final String ADMIN = "ADMIN";
    public static final String EMPLEADO = "EMPLEADO";
    public static final String CLIENTE = "CLIENTE";

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_EMPLEADO = "ROLE_EMPLEADO";
    public static final String ROLE_CLIENTE = "ROLE_CLIENTE";

    public static final Set<String> ALL = Set.of(ADMIN, EMPLEADO, CLIENTE);

    public static boolean isSupportedRole(String role) {
        return ALL.contains(normalize(role));
    }

    public static String normalize(String role) {
        if (role == null || role.isBlank()) {
            return "";
        }

        String value = role.trim().toUpperCase(Locale.ROOT);
        return value.startsWith("ROLE_") ? value.substring(5) : value;
    }

    public static String asRoleAuthority(String role) {
        String normalized = normalize(role);
        return normalized.isBlank() ? "" : "ROLE_" + normalized;
    }
}