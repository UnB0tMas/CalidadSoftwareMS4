// ruta: src/main/java/com/upsjb/ms4/util/IdempotencyKeyGenerator.java
package com.upsjb.ms4.util;

import com.upsjb.ms4.domain.enums.TipoComandoStock;

import java.util.Locale;
import java.util.UUID;

public final class IdempotencyKeyGenerator {

    private IdempotencyKeyGenerator() {
    }

    public static String stockCommand(Long idVentaMs4,
                                      Long idSkuMs3,
                                      TipoComandoStock tipoComandoStock) {
        requirePositive(idVentaMs4, "El id de venta MS4");
        requirePositive(idSkuMs3, "El id de SKU MS3");
        requireTipoComando(tipoComandoStock);

        return "MS4-VENTA-%s-SKU-%s-%s".formatted(
                idVentaMs4,
                idSkuMs3,
                normalizarTipoStock(tipoComandoStock)
        );
    }

    public static String stockCommand(String codigoVenta,
                                      Long idSkuMs3,
                                      TipoComandoStock tipoComandoStock) {
        String codigo = requireText(codigoVenta, "El código de venta");
        requirePositive(idSkuMs3, "El id de SKU MS3");
        requireTipoComando(tipoComandoStock);

        return "MS4-VENTA-%s-SKU-%s-%s".formatted(
                sanitize(codigo),
                idSkuMs3,
                normalizarTipoStock(tipoComandoStock)
        );
    }

    public static String stockCommandDetalle(Long idVentaMs4,
                                             Long idVentaDetalleMs4,
                                             Long idSkuMs3,
                                             TipoComandoStock tipoComandoStock) {
        requirePositive(idVentaMs4, "El id de venta MS4");
        requirePositive(idVentaDetalleMs4, "El id de detalle de venta MS4");
        requirePositive(idSkuMs3, "El id de SKU MS3");
        requireTipoComando(tipoComandoStock);

        return "MS4-VENTA-%s-DET-%s-SKU-%s-%s".formatted(
                idVentaMs4,
                idVentaDetalleMs4,
                idSkuMs3,
                normalizarTipoStock(tipoComandoStock)
        );
    }

    public static String kafkaConsumed(String sourceService, String topic, UUID eventId) {
        return kafkaConsumed(sourceService, topic, eventId == null ? null : eventId.toString());
    }

    public static String kafkaConsumed(String sourceService, String topic, String eventId) {
        return join("KAFKA", sourceService, topic, eventId);
    }

    public static String stripeEvent(String stripeEventId) {
        return join("STRIPE", stripeEventId);
    }

    public static String stripePaymentIntent(Long idVentaMs4, String metodoPago) {
        requirePositive(idVentaMs4, "El id de venta MS4");
        return join("MS4", "STRIPE", "PI", "VENTA", idVentaMs4, metodoPago);
    }

    public static String correoOutbox(String tipoCorreo, Long idEntidadOrigen) {
        return join("CORREO", tipoCorreo, idEntidadOrigen);
    }

    public static String outbox(String aggregateType, String aggregateId, String eventType) {
        return join("OUTBOX", aggregateType, aggregateId, eventType);
    }

    public static String join(Object... parts) {
        if (parts == null || parts.length == 0) {
            throw new IllegalArgumentException("Debe indicar partes para generar la llave de idempotencia.");
        }

        StringBuilder builder = new StringBuilder();

        for (Object part : parts) {
            if (part == null || String.valueOf(part).isBlank()) {
                continue;
            }

            if (!builder.isEmpty()) {
                builder.append("-");
            }

            builder.append(sanitize(String.valueOf(part)));
        }

        if (builder.isEmpty()) {
            throw new IllegalArgumentException("La llave de idempotencia no puede quedar vacía.");
        }

        return builder.toString();
    }

    private static void requireTipoComando(TipoComandoStock tipoComandoStock) {
        if (tipoComandoStock == null) {
            throw new IllegalArgumentException("El tipo de comando de stock es obligatorio.");
        }
    }

    private static void requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " debe ser positivo.");
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " es obligatorio.");
        }

        return value.trim();
    }

    private static String normalizarTipoStock(TipoComandoStock tipo) {
        return switch (tipo) {
            case RESERVAR_STOCK -> "RESERVAR";
            case CONFIRMAR_VENTA -> "CONFIRMAR";
            case LIBERAR_RESERVA -> "LIBERAR";
            case ANULAR_VENTA -> "ANULAR";
            case RECONCILIAR_STOCK -> "RECONCILIAR";
        };
    }

    private static String sanitize(String value) {
        String sanitized = value.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9_-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        if (sanitized.isBlank()) {
            throw new IllegalArgumentException("Una parte de la llave de idempotencia quedó vacía.");
        }

        return sanitized;
    }
}