// ruta: src/main/java/com/upsjb/ms4/dto/pago/response/StripeWebhookProcessResponseDto.java
package com.upsjb.ms4.dto.pago.response;

public record StripeWebhookProcessResponseDto(
        String stripeEventId,
        String stripeEventType,
        String stripePaymentIntentId,
        Boolean procesado,
        Boolean duplicado,
        String resultado,
        String mensaje
) {
}