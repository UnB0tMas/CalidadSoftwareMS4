// ruta: src/main/java/com/upsjb/ms4/policy/VentaPolicy.java
package com.upsjb.ms4.policy;

import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.enums.CanalVenta;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class VentaPolicy extends PolicySupport {

    public void authorizeCrearVentaFisica(AuthenticatedUserContext actor) {
        requireEmpleadoOrAdmin(actor);
    }

    public void authorizeCrearVentaOnline(AuthenticatedUserContext actor) {
        requireCliente(actor);
    }

    public void authorizePreviewCalculo(AuthenticatedUserContext actor, CanalVenta canalVenta) {
        requireAuthenticated(actor);

        if (canalVenta == null) {
            deny("El canal de venta es obligatorio para autorizar la previsualización.");
        }

        if (canalVenta.isOnline()) {
            if (!isCliente(actor) && !isAdmin(actor)) {
                deny("Solo CLIENTE puede previsualizar una compra online.");
            }
            return;
        }

        if (canalVenta.isFisica()) {
            if (!isEmpleado(actor) && !isAdmin(actor)) {
                deny("Solo EMPLEADO o ADMIN puede previsualizar una venta física.");
            }
            return;
        }

        deny("Canal de venta no autorizado.");
    }

    public void authorizeVerVenta(AuthenticatedUserContext actor, Venta venta) {
        requireAuthenticated(actor);

        if (venta == null) {
            deny("La venta no fue resuelta para autorización.");
        }

        authorizeVerVenta(
                actor,
                venta.getIdUsuarioClienteMs1(),
                venta.getIdUsuarioEmpleadoMs1()
        );
    }

    public void authorizeVerVenta(AuthenticatedUserContext actor,
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

        deny("No tiene permiso para consultar la venta.");
    }

    public void authorizeAnularVenta(AuthenticatedUserContext actor, Venta venta) {
        requireAdmin(actor);

        if (venta == null) {
            deny("La venta no fue resuelta para autorización.");
        }
    }

    public void authorizeListarVentasAdmin(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }

    public void authorizeListarMisVentasCliente(AuthenticatedUserContext actor) {
        requireCliente(actor);
    }

    public void authorizeListarMisVentasEmpleado(AuthenticatedUserContext actor) {
        requireEmpleadoOrAdmin(actor);
    }
}