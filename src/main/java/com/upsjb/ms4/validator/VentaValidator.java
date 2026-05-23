// ruta: src/main/java/com/upsjb/ms4/validator/VentaValidator.java
package com.upsjb.ms4.validator;

import com.upsjb.ms4.domain.entity.caja.Caja;
import com.upsjb.ms4.domain.entity.snapshot.ClienteSnapshotMs2;
import com.upsjb.ms4.domain.entity.snapshot.EmpleadoSnapshotMs2;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.enums.CanalVenta;
import com.upsjb.ms4.domain.enums.EstadoCaja;
import com.upsjb.ms4.domain.enums.EstadoVenta;
import com.upsjb.ms4.domain.enums.MetodoPago;
import com.upsjb.ms4.dto.venta.request.VentaFisicaCreateRequestDto;
import com.upsjb.ms4.dto.venta.request.VentaOnlineCreateRequestDto;
import org.springframework.stereotype.Component;

@Component
public class VentaValidator extends ValidatorSupport {

    public void validarCrearVentaFisica(VentaFisicaCreateRequestDto request,
                                        ClienteSnapshotMs2 cliente,
                                        EmpleadoSnapshotMs2 empleado,
                                        Caja caja,
                                        MetodoPago metodoPago) {
        require(request, "La solicitud de venta física es obligatoria.");
        validarClienteObligatorio(cliente);
        validarEmpleadoVendedorActivo(empleado);
        validarCajaAbiertaParaVenta(caja);
        validarCanalMetodoPago(CanalVenta.FISICA, metodoPago);

        if (request.detalles() == null || request.detalles().isEmpty()) {
            fail("La venta física debe contener al menos un detalle.");
        }
    }

    public void validarCrearVentaFisica(VentaFisicaCreateRequestDto request,
                                        ClienteSnapshotMs2 cliente,
                                        EmpleadoSnapshotMs2 empleado,
                                        Caja caja) {
        require(request, "La solicitud de venta física es obligatoria.");
        validarCrearVentaFisica(request, cliente, empleado, caja, request.metodoPagoPrincipal());
    }

    public void validarCrearVentaOnline(VentaOnlineCreateRequestDto request,
                                        ClienteSnapshotMs2 cliente,
                                        MetodoPago metodoPago) {
        require(request, "La solicitud de venta online es obligatoria.");
        validarClienteObligatorio(cliente);
        validarCanalMetodoPago(CanalVenta.ONLINE, metodoPago);

        if (request.detalles() == null || request.detalles().isEmpty()) {
            fail("La venta online debe contener al menos un detalle.");
        }
    }

    public void validarCrearVentaOnline(VentaOnlineCreateRequestDto request,
                                        ClienteSnapshotMs2 cliente) {
        validarCrearVentaOnline(request, cliente, MetodoPago.TARJETA_ONLINE_STRIPE_SANDBOX);
    }

    public void validarVentaConfirmable(Venta venta) {
        require(venta, "La venta es obligatoria.");

        if (venta.getEstadoVenta() == null) {
            fail("La venta no tiene estado definido.");
        }

        if (venta.getEstadoVenta() == EstadoVenta.CONFIRMADA) {
            conflict("La venta ya se encuentra confirmada.");
        }

        if (venta.getEstadoVenta() == EstadoVenta.ANULADA || venta.getEstadoVenta() == EstadoVenta.RECHAZADA) {
            conflict("Una venta anulada o rechazada no puede confirmarse.");
        }

        if (venta.getCanalVenta() == null) {
            fail("La venta no tiene canal definido.");
        }

        if (venta.getIdClienteSnapshot() == null || venta.getIdClienteMs2() == null) {
            fail("La venta debe tener cliente asociado.");
        }

        requirePositive(venta.getTotal(), "El total de la venta");
    }

    public void validarVentaPagadaParaConfirmar(Venta venta) {
        validarVentaConfirmable(venta);

        if (venta.getEstadoVenta() != EstadoVenta.PAGADA && venta.getEstadoVenta() != EstadoVenta.PENDIENTE_SYNC_STOCK) {
            conflict("Solo una venta pagada puede confirmarse.");
        }
    }

    public void validarVentaAnulable(Venta venta) {
        require(venta, "La venta es obligatoria.");

        if (venta.getEstadoVenta() == EstadoVenta.ANULADA) {
            conflict("La venta ya se encuentra anulada.");
        }

        if (venta.getEstadoVenta() == EstadoVenta.RECHAZADA) {
            conflict("Una venta rechazada no puede anularse.");
        }
    }

    public void validarClienteObligatorio(ClienteSnapshotMs2 cliente) {
        require(cliente, "El cliente es obligatorio.");
        requireActive(cliente.getEstado(), "El snapshot del cliente no está activo.");
        requireActive(cliente.getClienteActivoMs2(), "El cliente no se encuentra activo en MS2.");

        if (cliente.getId() == null) {
            fail("El snapshot de cliente no tiene identificador local.");
        }

        if (cliente.getIdClienteMs2() == null) {
            fail("El snapshot de cliente no tiene idClienteMs2.");
        }

        if (cliente.getIdUsuarioMs1() == null) {
            fail("El snapshot de cliente no tiene idUsuarioMs1.");
        }
    }

    public void validarCanalMetodoPago(CanalVenta canalVenta, MetodoPago metodoPago) {
        require(canalVenta, "El canal de venta es obligatorio.");
        require(metodoPago, "El método de pago es obligatorio.");

        if (!metodoPago.permitidoParaCanal(canalVenta)) {
            fail("El método de pago no está permitido para el canal de venta.");
        }
    }

    private void validarEmpleadoVendedorActivo(EmpleadoSnapshotMs2 empleado) {
        require(empleado, "El empleado vendedor es obligatorio.");
        requireActive(empleado.getEstado(), "El snapshot del empleado no está activo.");
        requireActive(empleado.getEmpleadoActivoMs2(), "El empleado vendedor no se encuentra activo en MS2.");

        if (empleado.getIdEmpleadoMs2() == null || empleado.getIdUsuarioMs1() == null) {
            fail("El snapshot del empleado vendedor no tiene identificadores oficiales completos.");
        }
    }

    private void validarCajaAbiertaParaVenta(Caja caja) {
        require(caja, "La caja abierta es obligatoria para una venta física.");

        if (caja.getEstadoCaja() != EstadoCaja.ABIERTA) {
            conflict("La caja no se encuentra abierta.");
        }

        requireActive(caja.getEstado(), "La caja no se encuentra activa.");
    }
}