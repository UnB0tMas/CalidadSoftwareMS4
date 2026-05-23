// ruta: src/main/java/com/upsjb/ms4/service/contract/pago/PagoService.java
package com.upsjb.ms4.service.contract.pago;

import com.upsjb.ms4.domain.entity.pago.Pago;
import com.upsjb.ms4.domain.enums.MetodoPago;
import com.upsjb.ms4.dto.pago.filter.PagoFilterDto;
import com.upsjb.ms4.dto.pago.request.PagoEfectivoRequestDto;
import com.upsjb.ms4.dto.pago.response.PagoResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;

import java.math.BigDecimal;

public interface PagoService {

    PagoResponseDto registrarPagoEfectivoAprobado(Long idVenta,
                                                  PagoEfectivoRequestDto request,
                                                  AuthenticatedUserContext actor);

    PagoResponseDto registrarPagoStripePendiente(Long idVenta,
                                                 MetodoPago metodoPago,
                                                 String paymentIntentId,
                                                 BigDecimal monto,
                                                 String payloadJson);

    PagoResponseDto confirmarPagoStripe(String paymentIntentId,
                                        String chargeId,
                                        String stripeStatus,
                                        String payloadJson);

    PagoResponseDto rechazarPagoStripe(String paymentIntentId,
                                       String stripeStatus,
                                       String motivo,
                                       String payloadJson);

    PagoResponseDto obtenerPorId(Long idPago);

    PagoResponseDto obtenerPorVenta(Long idVenta);

    PageResponseDto<PagoResponseDto> listar(PagoFilterDto filter, PageRequestDto page);

    Pago resolverPagoPorPaymentIntent(String paymentIntentId);
}