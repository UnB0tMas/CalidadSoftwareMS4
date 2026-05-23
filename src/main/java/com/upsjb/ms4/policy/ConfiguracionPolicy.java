// ruta: src/main/java/com/upsjb/ms4/policy/ConfiguracionPolicy.java
package com.upsjb.ms4.policy;

import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class ConfiguracionPolicy extends PolicySupport {

    public void authorizeGestionEmpresa(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }

    public void authorizeGestionTributaria(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }

    public void authorizeGestionSerie(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }

    public void authorizeGestionPlantilla(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }

    public void authorizeConsultarConfiguracion(AuthenticatedUserContext actor) {
        requireAnyOfficialRole(actor);
    }

    public void authorizeGestionAssetVisual(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }
}