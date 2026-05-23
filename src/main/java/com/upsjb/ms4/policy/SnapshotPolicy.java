// ruta: src/main/java/com/upsjb/ms4/policy/SnapshotPolicy.java
package com.upsjb.ms4.policy;

import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class SnapshotPolicy extends PolicySupport {

    public void authorizeConsultarSnapshots(AuthenticatedUserContext actor) {
        requireEmpleadoOrAdmin(actor);
    }

    public void authorizeConsultarSnapshotsAdmin(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }

    public void authorizeConsultarCatalogoVenta(AuthenticatedUserContext actor) {
        requireAnyOfficialRole(actor);
    }

    public void authorizeProcesarSnapshotKafka() {

    }
}