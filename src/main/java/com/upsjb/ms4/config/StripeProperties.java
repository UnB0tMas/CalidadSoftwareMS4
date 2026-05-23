// ruta: src/main/java/com/upsjb/ms4/config/StripeProperties.java
package com.upsjb.ms4.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Locale;

@ConfigurationProperties(prefix = "stripe")
public record StripeProperties(
        Boolean enabled,
        String mode,
        String secretKey,
        String publishableKey,
        String webhookSecret,
        String currency
) {

    public boolean enabledSafe() {
        return enabled == null || enabled;
    }

    public String modeSafe() {
        String value = trimToNull(mode);
        return value == null ? "sandbox" : value.toLowerCase(Locale.ROOT);
    }

    public boolean sandbox() {
        return "sandbox".equals(modeSafe()) || "test".equals(modeSafe());
    }

    public String secretKeySafe() {
        return trimToNull(secretKey);
    }

    public String publishableKeySafe() {
        return trimToNull(publishableKey);
    }

    public String webhookSecretSafe() {
        return trimToNull(webhookSecret);
    }

    public String currencySafe() {
        String value = trimToNull(currency);
        return value == null ? "pen" : value.toLowerCase(Locale.ROOT);
    }

    public boolean paymentIntentConfigured() {
        return secretKeySafe() != null && publishableKeySafe() != null;
    }

    public boolean webhookConfigured() {
        return webhookSecretSafe() != null;
    }

    public void requireSandboxIfEnabled() {
        if (enabledSafe() && !sandbox()) {
            throw new IllegalStateException("MS4 solo permite Stripe en modo Sandbox/Test.");
        }

        if (enabledSafe() && secretKeySafe() != null && secretKeySafe().startsWith("sk_live_")) {
            throw new IllegalStateException("MS4 no permite llaves live de Stripe.");
        }

        if (enabledSafe() && publishableKeySafe() != null && publishableKeySafe().startsWith("pk_live_")) {
            throw new IllegalStateException("MS4 no permite llaves live de Stripe.");
        }
    }

    public void requirePaymentIntentConfiguredIfEnabled() {
        requireSandboxIfEnabled();

        if (enabledSafe() && !paymentIntentConfigured()) {
            throw new IllegalStateException(
                    "Stripe está habilitado, pero secretKey o publishableKey no están configurados."
            );
        }
    }

    public void requireWebhookConfiguredIfEnabled() {
        requireSandboxIfEnabled();

        if (enabledSafe() && !webhookConfigured()) {
            throw new IllegalStateException(
                    "Stripe está habilitado, pero webhookSecret no está configurado."
            );
        }
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}