// ruta: src/main/java/com/upsjb/ms4/integration/stripe/StripeWebhookVerifier.java
package com.upsjb.ms4.integration.stripe;

import com.stripe.model.Event;
import com.stripe.net.Webhook;
import com.upsjb.ms4.shared.exception.StripePaymentException;
import org.springframework.stereotype.Component;

@Component
public class StripeWebhookVerifier {

    private final StripeClient stripeClient;
    private final StripeExceptionTranslator exceptionTranslator;

    public StripeWebhookVerifier(StripeClient stripeClient,
                                 StripeExceptionTranslator exceptionTranslator) {
        this.stripeClient = stripeClient;
        this.exceptionTranslator = exceptionTranslator;
    }

    public Event verify(String rawPayload, String stripeSignature) {
        validateInput(rawPayload, stripeSignature);
        stripeClient.validateWebhookConfiguration();

        try {
            return Webhook.constructEvent(rawPayload, stripeSignature.trim(), stripeClient.webhookSecret());
        } catch (Exception ex) {
            throw exceptionTranslator.translate(ex);
        }
    }

    private void validateInput(String rawPayload, String stripeSignature) {
        if (rawPayload == null || rawPayload.isBlank()) {
            throw new StripePaymentException("El payload crudo del webhook Stripe es obligatorio.");
        }

        if (stripeSignature == null || stripeSignature.isBlank()) {
            throw new StripePaymentException("El header Stripe-Signature es obligatorio.");
        }
    }
}