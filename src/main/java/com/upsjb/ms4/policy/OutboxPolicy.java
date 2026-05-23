// ruta: src/main/java/com/upsjb/ms4/policy/OutboxPolicy.java
package com.upsjb.ms4.policy;

import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class OutboxPolicy extends PolicySupport {

    public void authorizeListarOutbox(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }

    public void authorizeReintentarOutbox(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }

    public void authorizeDescartarOutbox(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }
}