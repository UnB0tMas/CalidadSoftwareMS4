// ruta: src/main/java/com/upsjb/ms4/policy/AuditoriaPolicy.java
package com.upsjb.ms4.policy;

import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaPolicy extends PolicySupport {

    public void authorizeConsultarAuditoria(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }

    public void authorizeObtenerAuditoria(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }
}