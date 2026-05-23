// ruta: src/main/java/com/upsjb/ms4/policy/PolicySupport.java
package com.upsjb.ms4.policy;

import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.roles.SecurityRoles;
import com.upsjb.ms4.shared.exception.ForbiddenException;

import java.util.Objects;

abstract class PolicySupport {

    protected void requireAuthenticated(AuthenticatedUserContext actor) {
        if (actor == null || actor.idUsuarioMs1() == null) {
            deny("Usuario no autenticado.");
        }
    }

    protected void requireAdmin(AuthenticatedUserContext actor) {
        requireAuthenticated(actor);

        if (!isAdmin(actor)) {
            deny("Solo ADMIN puede ejecutar esta acción.");
        }
    }

    protected void requireEmpleado(AuthenticatedUserContext actor) {
        requireAuthenticated(actor);

        if (!isEmpleado(actor)) {
            deny("Solo EMPLEADO puede ejecutar esta acción.");
        }
    }

    protected void requireEmpleadoOrAdmin(AuthenticatedUserContext actor) {
        requireAuthenticated(actor);

        if (!isEmpleado(actor) && !isAdmin(actor)) {
            deny("Solo EMPLEADO o ADMIN puede ejecutar esta acción.");
        }
    }

    protected void requireCliente(AuthenticatedUserContext actor) {
        requireAuthenticated(actor);

        if (!isCliente(actor)) {
            deny("Solo CLIENTE puede ejecutar esta acción.");
        }
    }

    protected void requireClienteOrAdmin(AuthenticatedUserContext actor) {
        requireAuthenticated(actor);

        if (!isCliente(actor) && !isAdmin(actor)) {
            deny("Solo CLIENTE o ADMIN puede ejecutar esta acción.");
        }
    }

    protected void requireAnyOfficialRole(AuthenticatedUserContext actor) {
        requireAuthenticated(actor);

        if (!isAdmin(actor) && !isEmpleado(actor) && !isCliente(actor)) {
            deny("No tiene un rol válido para ejecutar esta acción.");
        }
    }

    protected boolean isAdmin(AuthenticatedUserContext actor) {
        return actor != null && actor.hasRole(SecurityRoles.ADMIN);
    }

    protected boolean isEmpleado(AuthenticatedUserContext actor) {
        return actor != null && actor.hasRole(SecurityRoles.EMPLEADO);
    }

    protected boolean isCliente(AuthenticatedUserContext actor) {
        return actor != null && actor.hasRole(SecurityRoles.CLIENTE);
    }

    protected void requireOwnerOrAdmin(AuthenticatedUserContext actor, Long ownerIdUsuarioMs1) {
        requireAuthenticated(actor);

        if (isAdmin(actor)) {
            return;
        }

        if (ownerIdUsuarioMs1 == null || !Objects.equals(actor.idUsuarioMs1(), ownerIdUsuarioMs1)) {
            deny("No tiene permiso para acceder a este recurso.");
        }
    }

    protected void requireClienteOwnerOrAdmin(AuthenticatedUserContext actor, Long idUsuarioClienteMs1) {
        requireAuthenticated(actor);

        if (isAdmin(actor)) {
            return;
        }

        if (!isCliente(actor) || idUsuarioClienteMs1 == null || !Objects.equals(actor.idUsuarioMs1(), idUsuarioClienteMs1)) {
            deny("No tiene permiso para acceder a este recurso de cliente.");
        }
    }

    protected void requireEmployeeOwnerOrAdmin(AuthenticatedUserContext actor, Long idUsuarioEmpleadoMs1) {
        requireAuthenticated(actor);

        if (isAdmin(actor)) {
            return;
        }

        if (!isEmpleado(actor) || idUsuarioEmpleadoMs1 == null || !Objects.equals(actor.idUsuarioMs1(), idUsuarioEmpleadoMs1)) {
            deny("No tiene permiso para acceder a este recurso de empleado.");
        }
    }

    protected void deny(String message) {
        throw new ForbiddenException(message == null || message.isBlank()
                ? "No tiene permiso para ejecutar esta acción."
                : message);
    }
}