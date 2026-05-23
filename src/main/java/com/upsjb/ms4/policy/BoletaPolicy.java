// ruta: src/main/java/com/upsjb/ms4/policy/BoletaPolicy.java
package com.upsjb.ms4.policy;

import com.upsjb.ms4.domain.entity.boleta.Boleta;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class BoletaPolicy extends PolicySupport {

    public void authorizeVerBoleta(AuthenticatedUserContext actor, Boleta boleta) {
        requireAuthenticated(actor);

        if (boleta == null) {
            deny("La boleta no fue resuelta para autorización.");
        }

        if (isAdmin(actor)) {
            return;
        }

        Venta venta = boleta.getVenta();

        if (venta == null) {
            deny("La venta asociada a la boleta es obligatoria para autorizar.");
        }

        authorizeVerBoleta(
                actor,
                venta.getIdUsuarioClienteMs1(),
                venta.getIdUsuarioEmpleadoMs1()
        );
    }

    public void authorizeVerBoleta(AuthenticatedUserContext actor,
                                   Long idUsuarioClienteMs1,
                                   Long idUsuarioEmpleadoMs1) {
        requireAuthenticated(actor);

        if (isAdmin(actor)) {
            return;
        }

        if (isCliente(actor)) {
            requireClienteOwnerOrAdmin(actor, idUsuarioClienteMs1);
            return;
        }

        if (isEmpleado(actor)) {
            requireEmployeeOwnerOrAdmin(actor, idUsuarioEmpleadoMs1);
            return;
        }

        deny("No tiene permiso para consultar la boleta.");
    }

    public void authorizeGenerarPdf(AuthenticatedUserContext actor, Boleta boleta) {
        authorizeVerBoleta(actor, boleta);
    }

    public void authorizeGenerarPdf(AuthenticatedUserContext actor,
                                    Long idUsuarioClienteMs1,
                                    Long idUsuarioEmpleadoMs1) {
        authorizeVerBoleta(actor, idUsuarioClienteMs1, idUsuarioEmpleadoMs1);
    }

    public void authorizeReenviarCorreo(AuthenticatedUserContext actor, Boleta boleta) {
        authorizeVerBoleta(actor, boleta);
    }

    public void authorizeReenviarCorreo(AuthenticatedUserContext actor,
                                        Long idUsuarioClienteMs1,
                                        Long idUsuarioEmpleadoMs1) {
        authorizeVerBoleta(actor, idUsuarioClienteMs1, idUsuarioEmpleadoMs1);
    }

    public void authorizeVerBoletaAdmin(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }

    public void authorizeListarBoletasAdmin(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }

    public void authorizeListarMisBoletasCliente(AuthenticatedUserContext actor) {
        requireCliente(actor);
    }

    public void authorizeListarBoletasEmpleado(AuthenticatedUserContext actor) {
        requireEmpleadoOrAdmin(actor);
    }
}