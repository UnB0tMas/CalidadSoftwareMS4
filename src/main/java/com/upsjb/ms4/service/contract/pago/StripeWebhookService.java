// ruta: src/main/java/com/upsjb/ms4/service/contract/pago/StripeWebhookService.java
package com.upsjb.ms4.service.contract.pago;

import com.upsjb.ms4.domain.entity.pago.StripeEvento;
import com.upsjb.ms4.dto.pago.response.StripeWebhookProcessResponseDto;

public interface StripeWebhookService {

    StripeWebhookProcessResponseDto procesarWebhook(String rawPayload, String stripeSignatureHeader);

    StripeEvento registrarEventoRecibido(String stripeEventId, String eventType, String rawPayload);

    StripeWebhookProcessResponseDto procesarPaymentIntentSucceeded(String paymentIntentId, String rawPayload);

    StripeWebhookProcessResponseDto procesarPaymentIntentPaymentFailed(String paymentIntentId, String rawPayload);

    StripeWebhookProcessResponseDto procesarEventoIgnorado(String stripeEventId, String eventType);

    boolean eventoYaProcesado(String stripeEventId);
}