// ruta: src/main/java/com/upsjb/ms4/policy/CorreoOutboxPolicy.java
package com.upsjb.ms4.policy;

import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class CorreoOutboxPolicy extends PolicySupport {

    public void authorizeListarCorreos(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }

    public void authorizeReintentarCorreo(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }

    public void authorizeDescartarCorreo(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }
}