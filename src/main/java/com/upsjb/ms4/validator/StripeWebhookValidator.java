// ruta: src/main/java/com/upsjb/ms4/validator/StripeWebhookValidator.java
package com.upsjb.ms4.validator;

import com.upsjb.ms4.domain.entity.pago.Pago;
import com.upsjb.ms4.domain.entity.pago.StripeEvento;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class StripeWebhookValidator extends ValidatorSupport {

    private static final Set<String> EVENTOS_SOPORTADOS = Set.of(
            "payment_intent.succeeded",
            "payment_intent.payment_failed",
            "payment_intent.canceled",
            "charge.refunded"
    );

    public void validarFirma(String rawPayload, String stripeSignature) {
        requireText(rawPayload, "El payload crudo del webhook Stripe es obligatorio.");
        requireText(stripeSignature, "El header Stripe-Signature es obligatorio.");
    }

    public void validarEventoRecibido(String stripeEventId, String eventType, String rawPayload) {
        requireText(stripeEventId, "El id del evento Stripe es obligatorio.");
        requireText(eventType, "El tipo de evento Stripe es obligatorio.");
        requireText(rawPayload, "El payload crudo del webhook Stripe es obligatorio.");
        requireMaxLength(stripeEventId, 120, "El id del evento Stripe");
        requireMaxLength(eventType, 120, "El tipo de evento Stripe");
        requireJson(rawPayload, "El payload crudo del webhook Stripe");
    }

    public void validarEventoSoportado(String eventType) {
        requireText(eventType, "El tipo de evento Stripe es obligatorio.");

        if (!esEventoSoportado(eventType)) {
            fail("Evento Stripe no soportado: " + eventType);
        }
    }

    public boolean esEventoSoportado(String eventType) {
        return eventType != null && EVENTOS_SOPORTADOS.contains(eventType.trim());
    }

    public void validarIdempotencia(StripeEvento eventoExistente) {
        if (eventoExistente != null) {
            conflict("El evento Stripe ya fue recibido previamente.");
        }
    }

    public void validarPaymentIntentExiste(String paymentIntentId, Pago pago) {
        requireText(paymentIntentId, "El paymentIntentId es obligatorio.");

        if (pago == null) {
            conflict("No existe pago asociado al paymentIntent recibido.");
        }
    }
}