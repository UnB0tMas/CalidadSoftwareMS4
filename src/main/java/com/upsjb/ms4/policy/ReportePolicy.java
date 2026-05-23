// ruta: src/main/java/com/upsjb/ms4/policy/ReportePolicy.java
package com.upsjb.ms4.policy;

import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class ReportePolicy extends PolicySupport {

    public void authorizeReporteEmpleado(AuthenticatedUserContext actor, Long idUsuarioEmpleadoMs1) {
        requireEmpleadoOrAdmin(actor);

        if (!isAdmin(actor) && idUsuarioEmpleadoMs1 != null) {
            requireEmployeeOwnerOrAdmin(actor, idUsuarioEmpleadoMs1);
        }
    }

    public void authorizeReporteEmpleadoActual(AuthenticatedUserContext actor) {
        requireEmpleadoOrAdmin(actor);
    }

    public void authorizeReporteAdmin(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }
}