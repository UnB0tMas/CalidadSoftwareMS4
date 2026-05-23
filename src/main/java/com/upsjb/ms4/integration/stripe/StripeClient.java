// ruta: src/main/java/com/upsjb/ms4/integration/stripe/StripeClient.java
package com.upsjb.ms4.integration.stripe;

import com.stripe.Stripe;
import com.stripe.net.RequestOptions;
import com.upsjb.ms4.config.StripeProperties;
import com.upsjb.ms4.shared.exception.StripePaymentException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class StripeClient {

    private final StripeProperties properties;

    public StripeClient(StripeProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void configureIfEnabled() {
        if (!properties.enabledSafe()) {
            return;
        }

        validatePaymentIntentConfiguration();
        Stripe.apiKey = secretKey();
    }

    public String secretKey() {
        validateEnabled();
        validateSandbox();

        String value = properties.secretKeySafe();

        if (value == null) {
            throw new StripePaymentException("La secret key de Stripe Sandbox no está configurada.");
        }

        if (value.startsWith("sk_live_")) {
            throw new StripePaymentException("MS4 no permite llaves live de Stripe.");
        }

        return value;
    }

    public String publishableKey() {
        validatePaymentIntentConfiguration();
        return properties.publishableKeySafe();
    }

    public String currency() {
        String currency = properties.currencySafe();

        if (currency == null || currency.isBlank()) {
            return "pen";
        }

        return currency.trim().toLowerCase();
    }

    public String webhookSecret() {
        validateWebhookConfiguration();
        return properties.webhookSecretSafe();
    }

    public RequestOptions requestOptions(String idempotencyKey) {
        RequestOptions.RequestOptionsBuilder builder = RequestOptions.builder()
                .setApiKey(secretKey());

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            builder.setIdempotencyKey(idempotencyKey.trim());
        }

        return builder.build();
    }

    public void validatePaymentIntentConfiguration() {
        validateEnabled();
        validateSandbox();

        if (properties.secretKeySafe() == null) {
            throw new StripePaymentException("La secret key de Stripe Sandbox no está configurada.");
        }

        if (properties.publishableKeySafe() == null) {
            throw new StripePaymentException("La publishable key de Stripe Sandbox no está configurada.");
        }

        if (properties.secretKeySafe().startsWith("sk_live_")
                || properties.publishableKeySafe().startsWith("pk_live_")) {
            throw new StripePaymentException("MS4 no permite llaves live de Stripe.");
        }
    }

    public void validateWebhookConfiguration() {
        validateEnabled();
        validateSandbox();

        if (properties.webhookSecretSafe() == null) {
            throw new StripePaymentException("El webhook secret de Stripe Sandbox no está configurado.");
        }

        if (properties.secretKeySafe() != null && properties.secretKeySafe().startsWith("sk_live_")) {
            throw new StripePaymentException("MS4 no permite llaves live de Stripe.");
        }

        if (properties.publishableKeySafe() != null && properties.publishableKeySafe().startsWith("pk_live_")) {
            throw new StripePaymentException("MS4 no permite llaves live de Stripe.");
        }
    }

    public void validateSandboxConfiguration() {
        validatePaymentIntentConfiguration();
    }

    private void validateEnabled() {
        if (!properties.enabledSafe()) {
            throw new StripePaymentException("Stripe se encuentra deshabilitado.");
        }
    }

    private void validateSandbox() {
        if (!properties.sandbox()) {
            throw new StripePaymentException("MS4 solo permite Stripe en modo Sandbox/Test.");
        }
    }
}