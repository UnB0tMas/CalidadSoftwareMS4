// ruta: src/main/java/com/upsjb/ms4/policy/ContingenciaPolicy.java
package com.upsjb.ms4.policy;

import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class ContingenciaPolicy extends PolicySupport {

    public void authorizeActivarContingencia(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }

    public void authorizeFinalizarContingencia(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }

    public void authorizeReconciliar(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }

    public void authorizeConsultarContingencia(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }

    public void authorizeConsultarEventosPendientes(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }
}