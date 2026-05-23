// ruta: src/main/java/com/upsjb/ms4/integration/stripe/StripePaymentIntentClient.java
package com.upsjb.ms4.integration.stripe;

import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.upsjb.ms4.shared.exception.StripePaymentException;
import com.upsjb.ms4.util.MoneyUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class StripePaymentIntentClient {

    private final StripeClient stripeClient;
    private final StripeExceptionTranslator exceptionTranslator;

    public StripePaymentIntentClient(StripeClient stripeClient,
                                     StripeExceptionTranslator exceptionTranslator) {
        this.stripeClient = stripeClient;
        this.exceptionTranslator = exceptionTranslator;
    }

    public PaymentIntent create(CreatePaymentIntentCommand command) {
        validateCreateCommand(command);
        stripeClient.validatePaymentIntentConfiguration();

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(toMinorUnits(command.monto()))
                    .setCurrency(resolveCurrency(command.moneda()))
                    .setDescription(trimToNull(command.descripcion()))
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .putAllMetadata(buildMetadata(command))
                    .build();

            return PaymentIntent.create(params, stripeClient.requestOptions(buildIdempotencyKey(command)));
        } catch (Exception ex) {
            throw exceptionTranslator.translate(ex);
        }
    }

    public PaymentIntent retrieve(String paymentIntentId) {
        stripeClient.validatePaymentIntentConfiguration();

        try {
            return PaymentIntent.retrieve(requirePaymentIntentId(paymentIntentId), stripeClient.requestOptions(null));
        } catch (Exception ex) {
            throw exceptionTranslator.translate(ex);
        }
    }

    public PaymentIntent cancel(String paymentIntentId) {
        stripeClient.validatePaymentIntentConfiguration();

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(
                    requirePaymentIntentId(paymentIntentId),
                    stripeClient.requestOptions(null)
            );

            return paymentIntent.cancel();
        } catch (Exception ex) {
            throw exceptionTranslator.translate(ex);
        }
    }

    private void validateCreateCommand(CreatePaymentIntentCommand command) {
        if (command == null) {
            throw new StripePaymentException("Los datos para crear el PaymentIntent son obligatorios.");
        }

        if (command.idVentaMs4() == null || command.idVentaMs4() <= 0) {
            throw new StripePaymentException("La venta asociada al PaymentIntent es obligatoria.");
        }

        if (command.codigoVenta() == null || command.codigoVenta().isBlank()) {
            throw new StripePaymentException("El código de venta es obligatorio para Stripe.");
        }

        if (command.canalVenta() == null || command.canalVenta().isBlank()) {
            throw new StripePaymentException("El canal de venta es obligatorio para Stripe.");
        }

        if (command.metodoPago() == null || command.metodoPago().isBlank()) {
            throw new StripePaymentException("El método de pago es obligatorio para Stripe.");
        }

        MoneyUtil.requirePositive(command.monto(), "El monto del PaymentIntent");
    }

    private Long toMinorUnits(BigDecimal amount) {
        try {
            return MoneyUtil.money(amount)
                    .movePointRight(2)
                    .longValueExact();
        } catch (ArithmeticException ex) {
            throw new StripePaymentException("El monto del PaymentIntent debe tener como máximo dos decimales.", ex);
        }
    }

    private String resolveCurrency(String currency) {
        String value = trimToNull(currency);
        return value == null ? stripeClient.currency() : value.toLowerCase();
    }

    private Map<String, String> buildMetadata(CreatePaymentIntentCommand command) {
        Map<String, String> metadata = new LinkedHashMap<>();

        metadata.put("idVentaMs4", String.valueOf(command.idVentaMs4()));
        metadata.put("codigoVenta", command.codigoVenta());
        metadata.put("canalVenta", command.canalVenta());
        metadata.put("metodoPago", command.metodoPago());
        metadata.put("sourceService", "ms-ventas-facturacion");

        if (command.metadata() != null) {
            command.metadata().forEach((key, value) -> {
                String normalizedKey = normalizeMetadataKey(key);
                String normalizedValue = normalizeMetadataValue(value);

                if (normalizedKey != null && normalizedValue != null) {
                    metadata.put(normalizedKey, normalizedValue);
                }
            });
        }

        return metadata;
    }

    private String buildIdempotencyKey(CreatePaymentIntentCommand command) {
        String customKey = command.metadata() == null ? null : command.metadata().get("idempotencyKey");

        if (customKey != null && !customKey.isBlank()) {
            return customKey.trim();
        }

        return "MS4-STRIPE-PI-VENTA-%s-%s".formatted(
                command.idVentaMs4(),
                command.metodoPago().trim().toUpperCase()
        );
    }

    private String requirePaymentIntentId(String paymentIntentId) {
        String value = trimToNull(paymentIntentId);

        if (value == null) {
            throw new StripePaymentException("El paymentIntentId es obligatorio.");
        }

        return value;
    }

    private String normalizeMetadataKey(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        if (normalized.length() > 40) {
            normalized = normalized.substring(0, 40);
        }

        return normalized;
    }

    private String normalizeMetadataValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        if (normalized.length() > 500) {
            normalized = normalized.substring(0, 500);
        }

        return normalized;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record CreatePaymentIntentCommand(
            Long idVentaMs4,
            String codigoVenta,
            String canalVenta,
            String metodoPago,
            BigDecimal monto,
            String moneda,
            String descripcion,
            Map<String, String> metadata
    ) {
    }
}