// ruta: src/main/java/com/upsjb/ms4/policy/CajaPolicy.java
package com.upsjb.ms4.policy;

import com.upsjb.ms4.domain.entity.caja.Caja;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CajaPolicy extends PolicySupport {

    public void authorizeAbrirCaja(AuthenticatedUserContext actor) {
        requireEmpleadoOrAdmin(actor);
    }

    public void authorizeCerrarCaja(AuthenticatedUserContext actor, Caja caja) {
        requireEmpleadoOrAdmin(actor);

        if (caja == null) {
            deny("La caja no fue resuelta para autorización.");
        }
    }

    public void authorizeVerCaja(AuthenticatedUserContext actor, Caja caja) {
        requireEmpleadoOrAdmin(actor);

        if (caja == null) {
            deny("La caja no fue resuelta para autorización.");
        }

        if (isAdmin(actor)) {
            return;
        }

        if (caja.getEstadoCaja() != null && caja.getEstadoCaja().abierta()) {
            return;
        }

        if (Objects.equals(actor.idUsuarioMs1(), caja.getIdUsuarioAperturaMs1())
                || Objects.equals(actor.idUsuarioMs1(), caja.getIdUsuarioCierreMs1())) {
            return;
        }

        deny("No tiene permiso para consultar esta caja.");
    }

    public void authorizeAjustarCaja(AuthenticatedUserContext actor, Caja caja) {
        requireEmpleadoOrAdmin(actor);

        if (caja == null) {
            deny("La caja no fue resuelta para autorización.");
        }

        if (isAdmin(actor)) {
            return;
        }

        if (caja.getEstadoCaja() != null && caja.getEstadoCaja().abierta()) {
            return;
        }

        deny("Solo puede ajustar una caja abierta.");
    }

    public void authorizeVerCajaActual(AuthenticatedUserContext actor) {
        requireEmpleadoOrAdmin(actor);
    }

    public void authorizeListarCajasAdmin(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }

    public void authorizeVerCajaAdmin(AuthenticatedUserContext actor) {
        requireAdmin(actor);
    }

    public void authorizeRegistrarMovimientoCaja(AuthenticatedUserContext actor, Caja caja) {
        requireEmpleadoOrAdmin(actor);

        if (caja == null) {
            deny("La caja no fue resuelta para registrar movimiento.");
        }
    }

    public void authorizeListarMovimientosCaja(AuthenticatedUserContext actor, Caja caja) {
        authorizeVerCaja(actor, caja);
    }
}