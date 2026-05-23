// ruta: src/main/java/com/upsjb/ms4/policy/PagoPolicy.java
package com.upsjb.ms4.policy;

import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class PagoPolicy extends PolicySupport {

    public void authorizeRegistrarPagoEfectivo(AuthenticatedUserContext actor, Venta venta) {
        requireEmpleadoOrAdmin(actor);
        requireVentaResuelta(venta);

        if (isAdmin(actor)) {
            return;
        }

        requireEmployeeOwnerOrAdmin(actor, venta.getIdUsuarioEmpleadoMs1());
    }

    public void authorizeCrearPaymentIntentOnline(AuthenticatedUserContext actor, Venta venta) {
        requireClienteOrAdmin(actor);
        requireVentaResuelta(venta);
        requireClienteOwnerOrAdmin(actor, venta.getIdUsuarioClienteMs1());
    }

    public void authorizeCrearPaymentIntentPresencial(AuthenticatedUserContext actor, Venta venta) {
        requireEmpleadoOrAdmin(actor);
        requireVentaResuelta(venta);

        if (isAdmin(actor)) {
            return;
        }

        requireEmployeeOwnerOrAdmin(actor, venta.getIdUsuarioEmpleadoMs1());
    }

    public void authorizeObtenerPago(AuthenticatedUserContext actor, Venta venta) {
        requireAuthenticated(actor);
        requireVentaResuelta(venta);

        if (isAdmin(actor)) {
            return;
        }

        if (isCliente(actor)) {
            requireClienteOwnerOrAdmin(actor, venta.getIdUsuarioClienteMs1());
            return;
        }

        if (isEmpleado(actor)) {
            requireEmployeeOwnerOrAdmin(actor, venta.getIdUsuarioEmpleadoMs1());
            return;
        }

        deny("No tiene permiso para consultar el pago.");
    }

    public void authorizeListarPagos(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }

    private void requireVentaResuelta(Venta venta) {
        if (venta == null) {
            deny("La venta no fue resuelta para autorizar el pago.");
        }
    }
}