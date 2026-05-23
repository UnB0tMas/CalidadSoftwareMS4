// ruta: src/main/java/com/upsjb/ms4/integration/stripe/StripeExceptionTranslator.java
package com.upsjb.ms4.integration.stripe;

import com.stripe.exception.ApiConnectionException;
import com.stripe.exception.ApiException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.IdempotencyException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.PermissionException;
import com.stripe.exception.RateLimitException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.upsjb.ms4.shared.exception.StripePaymentException;
import org.springframework.stereotype.Component;

@Component
public class StripeExceptionTranslator {

    public StripePaymentException translate(Exception ex) {
        if (ex instanceof StripePaymentException stripePaymentException) {
            return stripePaymentException;
        }

        if (ex instanceof CardException cardEx) {
            return new StripePaymentException(
                    "El pago fue rechazado por Stripe Sandbox: " + safe(cardEx.getCode()) + ".",
                    ex
            );
        }

        if (ex instanceof InvalidRequestException) {
            return new StripePaymentException("La solicitud enviada a Stripe Sandbox no es válida.", ex);
        }

        if (ex instanceof AuthenticationException) {
            return new StripePaymentException("Credenciales Stripe Sandbox inválidas.", ex);
        }

        if (ex instanceof PermissionException) {
            return new StripePaymentException("La cuenta Stripe Sandbox no tiene permisos para esta operación.", ex);
        }

        if (ex instanceof ApiConnectionException) {
            return new StripePaymentException("No se pudo conectar con Stripe Sandbox.", ex);
        }

        if (ex instanceof RateLimitException) {
            return new StripePaymentException("Stripe Sandbox limitó temporalmente la solicitud.", ex);
        }

        if (ex instanceof IdempotencyException) {
            return new StripePaymentException("Stripe Sandbox detectó una inconsistencia de idempotencia.", ex);
        }

        if (ex instanceof SignatureVerificationException) {
            return new StripePaymentException("La firma del webhook Stripe no es válida.", ex);
        }

        if (ex instanceof ApiException) {
            return new StripePaymentException("Stripe Sandbox devolvió un error técnico controlado.", ex);
        }

        if (ex instanceof StripeException) {
            return new StripePaymentException("Stripe Sandbox devolvió un error controlado.", ex);
        }

        return new StripePaymentException("Ocurrió un error inesperado al procesar Stripe Sandbox.", ex);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "sin_codigo" : value.trim();
    }
}