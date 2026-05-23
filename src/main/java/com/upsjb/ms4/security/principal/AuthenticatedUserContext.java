// ruta: src/main/java/com/upsjb/ms4/security/principal/AuthenticatedUserContext.java
package com.upsjb.ms4.security.principal;

import com.upsjb.ms4.security.roles.SecurityRoles;

import java.util.Set;

public record AuthenticatedUserContext(
        Long idUsuarioMs1,
        String username,
        String email,
        String rol,
        Set<String> authorities,
        String sid,
        String tokenType
) {

    public AuthenticatedUserContext {
        username = trimToNull(username);
        email = trimToNull(email);
        rol = SecurityRoles.normalize(rol);
        authorities = authorities == null ? Set.of() : Set.copyOf(authorities);
        sid = trimToNull(sid);
        tokenType = trimToNull(tokenType);
    }

    public boolean hasRole(String expectedRole) {
        String normalized = SecurityRoles.normalize(expectedRole);

        if (normalized.isBlank()) {
            return false;
        }

        if (SecurityRoles.normalize(rol).equals(normalized)) {
            return true;
        }

        String expectedAuthority = SecurityRoles.asRoleAuthority(normalized);

        return authorities.stream()
                .map(SecurityRoles::asRoleAuthority)
                .anyMatch(expectedAuthority::equals);
    }

    public boolean hasAnyRole(String... expectedRoles) {
        if (expectedRoles == null || expectedRoles.length == 0) {
            return false;
        }

        for (String expectedRole : expectedRoles) {
            if (hasRole(expectedRole)) {
                return true;
            }
        }

        return false;
    }

    public boolean isAdmin() {
        return hasRole(SecurityRoles.ADMIN);
    }

    public boolean isEmpleado() {
        return hasRole(SecurityRoles.EMPLEADO);
    }

    public boolean isCliente() {
        return hasRole(SecurityRoles.CLIENTE);
    }

    public String actorLabel() {
        return username == null || username.isBlank()
                ? "usuario-ms1-" + idUsuarioMs1
                : username;
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}