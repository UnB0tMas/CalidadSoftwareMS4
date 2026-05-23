// ruta: src/main/java/com/upsjb/ms4/validator/PagoValidator.java
package com.upsjb.ms4.validator;

import com.upsjb.ms4.domain.entity.pago.Pago;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.enums.EstadoPago;
import com.upsjb.ms4.domain.enums.EstadoVenta;
import com.upsjb.ms4.domain.enums.MetodoPago;
import com.upsjb.ms4.dto.pago.filter.PagoFilterDto;
import com.upsjb.ms4.dto.pago.request.PagoEfectivoRequestDto;
import com.upsjb.ms4.dto.pago.request.PagoStripeOnlineRequestDto;
import com.upsjb.ms4.dto.pago.request.PagoStripePresencialRequestDto;
import com.upsjb.ms4.util.JsonUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class PagoValidator extends ValidatorSupport {

    public void validarIdPago(Long idPago) {
        requirePositive(idPago, "El pago");
    }

    public void validarIdVenta(Long idVenta) {
        requirePositive(idVenta, "La venta");
    }

    public void validarFiltro(PagoFilterDto filter) {
        if (filter == null) {
            return;
        }

        requireMaxLength(filter.search(), 150, "El texto de búsqueda");
        requireMaxLength(filter.codigoPago(), 80, "El código de pago");
        requireMaxLength(filter.stripePaymentIntentId(), 120, "El PaymentIntent de Stripe");
        requireDateRange(filter.fechaDesde(), filter.fechaHasta(), "El rango de fecha de pago");

        if (filter.idVenta() != null) {
            requirePositive(filter.idVenta(), "La venta");
        }
    }

    public void validarPagoEfectivo(Venta venta, PagoEfectivoRequestDto request) {
        require(venta, "La venta es obligatoria.");
        require(request, "La solicitud de pago en efectivo es obligatoria.");
        validarVentaPermitePago(venta);
        validarMontoEfectivoSuficiente(venta, request.montoRecibido());
        validarMetodoPago(venta, MetodoPago.EFECTIVO);
    }

    public void validarPagoStripePresencial(Venta venta, PagoStripePresencialRequestDto request) {
        require(venta, "La venta es obligatoria.");
        require(request, "La solicitud de pago presencial Stripe es obligatoria.");
        validarVentaPermitePago(venta);
        validarMontoVenta(venta);
        validarMetodoPago(venta, MetodoPago.TARJETA_PRESENCIAL_STRIPE_SANDBOX);
    }

    public void validarPagoStripeOnline(Venta venta, PagoStripeOnlineRequestDto request) {
        require(venta, "La venta es obligatoria.");
        require(request, "La solicitud de pago online Stripe es obligatoria.");
        requirePositive(request.idVenta(), "La venta");

        if (!request.idVenta().equals(venta.getId())) {
            fail("La venta indicada no coincide con la venta resuelta para pago online.");
        }

        validarVentaPermitePago(venta);
        validarMontoVenta(venta);
        validarMetodoPago(venta, MetodoPago.TARJETA_ONLINE_STRIPE_SANDBOX);
    }

    public void validarPagoStripePendiente(Venta venta,
                                           MetodoPago metodoPago,
                                           String paymentIntentId,
                                           BigDecimal monto,
                                           String payloadJson) {
        require(venta, "La venta es obligatoria.");
        require(metodoPago, "El método de pago es obligatorio.");
        requireText(paymentIntentId, "El PaymentIntent de Stripe es obligatorio.");
        requireMaxLength(paymentIntentId, 120, "El PaymentIntent de Stripe");
        validarVentaPermitePago(venta);
        validarMontoCoincideVenta(venta, monto);
        validarMetodoPago(venta, metodoPago);

        if (!metodoPago.esStripe()) {
            fail("El método de pago debe ser Stripe Sandbox.");
        }

        validarPayloadOpcional(payloadJson);
    }

    public void validarConfirmarPagoStripe(Pago pago,
                                           String paymentIntentId,
                                           String chargeId,
                                           String stripeStatus,
                                           String payloadJson) {
        require(pago, "El pago Stripe es obligatorio.");
        requireText(paymentIntentId, "El PaymentIntent de Stripe es obligatorio.");
        requireText(stripeStatus, "El estado Stripe es obligatorio.");
        requireMaxLength(paymentIntentId, 120, "El PaymentIntent de Stripe");
        requireMaxLength(chargeId, 120, "El chargeId de Stripe");
        requireMaxLength(stripeStatus, 80, "El estado Stripe");

        if (!pago.getMetodoPago().esStripe()) {
            fail("El pago no corresponde a un método Stripe Sandbox.");
        }

        if (pago.getEstadoPago() == EstadoPago.RECHAZADO
                || pago.getEstadoPago() == EstadoPago.ANULADO
                || pago.getEstadoPago() == EstadoPago.REEMBOLSADO) {
            conflict("El pago Stripe ya se encuentra en estado final no aprobable.");
        }

        validarPayloadOpcional(payloadJson);
    }

    public void validarRechazarPagoStripe(Pago pago,
                                          String paymentIntentId,
                                          String stripeStatus,
                                          String motivo,
                                          String payloadJson) {
        require(pago, "El pago Stripe es obligatorio.");
        requireText(paymentIntentId, "El PaymentIntent de Stripe es obligatorio.");
        requireText(stripeStatus, "El estado Stripe es obligatorio.");
        requireMaxLength(paymentIntentId, 120, "El PaymentIntent de Stripe");
        requireMaxLength(stripeStatus, 80, "El estado Stripe");
        requireMaxLength(motivo, 500, "El motivo de rechazo");

        if (!pago.getMetodoPago().esStripe()) {
            fail("El pago no corresponde a un método Stripe Sandbox.");
        }

        if (pago.getEstadoPago() == EstadoPago.APROBADO) {
            conflict("No se puede rechazar un pago Stripe ya aprobado.");
        }

        validarPayloadOpcional(payloadJson);
    }

    public void validarMontoCoincideVenta(Venta venta, BigDecimal monto) {
        require(venta, "La venta es obligatoria.");
        validarMontoVenta(venta);
        requirePositive(monto, "El monto del pago");

        if (venta.getTotal().compareTo(monto) != 0) {
            fail("El monto del pago no coincide con el total de la venta.");
        }
    }

    public void validarNoDoblePago(List<Pago> pagos) {
        if (pagos == null || pagos.isEmpty()) {
            return;
        }

        boolean existePagoBloqueante = pagos.stream()
                .filter(pago -> pago != null && pago.getEstadoPago() != null)
                .anyMatch(pago ->
                        pago.getEstadoPago() == EstadoPago.APROBADO
                                || pago.getEstadoPago() == EstadoPago.PENDIENTE
                );

        if (existePagoBloqueante) {
            conflict("La venta ya tiene un pago aprobado o pendiente.");
        }
    }

    public void validarMontoEfectivoSuficiente(Venta venta, BigDecimal montoRecibido) {
        require(venta, "La venta es obligatoria.");
        validarMontoVenta(venta);
        requirePositive(montoRecibido, "El monto recibido");

        if (montoRecibido.compareTo(venta.getTotal()) < 0) {
            fail("El monto recibido no cubre el total de la venta.");
        }
    }

    private void validarVentaPermitePago(Venta venta) {
        if (venta.getEstadoVenta() == null) {
            fail("La venta no tiene estado definido.");
        }

        if (venta.getEstadoVenta() == EstadoVenta.PAGADA
                || venta.getEstadoVenta() == EstadoVenta.CONFIRMADA
                || venta.getEstadoVenta() == EstadoVenta.ANULADA
                || venta.getEstadoVenta() == EstadoVenta.RECHAZADA) {
            conflict("La venta no permite registrar un nuevo pago.");
        }
    }

    private void validarMontoVenta(Venta venta) {
        requirePositive(venta.getTotal(), "El total de la venta");
    }

    private void validarMetodoPago(Venta venta, MetodoPago metodoPago) {
        require(venta.getCanalVenta(), "El canal de venta es obligatorio.");
        require(metodoPago, "El método de pago es obligatorio.");

        if (!metodoPago.permitidoParaCanal(venta.getCanalVenta())) {
            fail("El método de pago no está permitido para el canal de venta.");
        }
    }

    private void validarPayloadOpcional(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return;
        }

        if (!JsonUtil.isValidJson(payloadJson)) {
            fail("El payload de pasarela debe contener JSON válido.");
        }
    }
}