// ruta: src/main/java/com/upsjb/ms4/service/contract/pago/StripePaymentService.java
package com.upsjb.ms4.service.contract.pago;

import com.upsjb.ms4.dto.lookup.LookupItemResponseDto;
import com.upsjb.ms4.dto.pago.request.PagoStripeOnlineRequestDto;
import com.upsjb.ms4.dto.pago.request.PagoStripePresencialRequestDto;
import com.upsjb.ms4.dto.pago.response.StripePaymentIntentResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;

public interface StripePaymentService {

    StripePaymentIntentResponseDto crearPaymentIntentOnline(PagoStripeOnlineRequestDto request,
                                                            AuthenticatedUserContext actor);

    StripePaymentIntentResponseDto crearPaymentIntentPresencial(Long idVenta,
                                                                PagoStripePresencialRequestDto request,
                                                                AuthenticatedUserContext actor);

    StripePaymentIntentResponseDto obtenerEstadoPaymentIntent(String paymentIntentId,
                                                              AuthenticatedUserContext actor);

    LookupItemResponseDto obtenerPublishableKeySandbox();

    void validarModoSandboxActivo();

    void cancelarPaymentIntent(String paymentIntentId, String motivo);
}