// ruta: src/main/java/com/upsjb/ms4/policy/LookupPolicy.java
package com.upsjb.ms4.policy;

import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class LookupPolicy extends PolicySupport {

    public void authorizeLookupClientes(AuthenticatedUserContext actor) {
        requireEmpleadoOrAdmin(actor);
    }

    public void authorizeLookupEmpleados(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }

    public void authorizeLookupCatalogo(AuthenticatedUserContext actor) {
        requireAnyOfficialRole(actor);
    }

    public void authorizeLookupCaja(AuthenticatedUserContext actor) {
        requireEmpleadoOrAdmin(actor);
    }

    public void authorizeLookupConfiguracion(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }

    public void authorizeLookupSeriesBoleta(AuthenticatedUserContext actor) {
        requireEmpleadoOrAdmin(actor);
    }
}