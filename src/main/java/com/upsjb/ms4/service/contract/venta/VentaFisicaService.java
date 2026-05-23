// ruta: src/main/java/com/upsjb/ms4/service/contract/venta/VentaFisicaService.java
package com.upsjb.ms4.service.contract.venta;

import com.upsjb.ms4.dto.pago.request.PagoEfectivoRequestDto;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms4.dto.venta.request.VentaCalculoPreviewRequestDto;
import com.upsjb.ms4.dto.venta.request.VentaFisicaCreateRequestDto;
import com.upsjb.ms4.dto.venta.response.VentaCalculoPreviewResponseDto;
import com.upsjb.ms4.dto.venta.response.VentaDetailResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;

public interface VentaFisicaService {

    VentaCalculoPreviewResponseDto previsualizarVentaFisica(VentaCalculoPreviewRequestDto request,
                                                            AuthenticatedUserContext actor);

    VentaDetailResponseDto crearVentaFisicaConPagoEfectivo(VentaFisicaCreateRequestDto request,
                                                           PagoEfectivoRequestDto pagoRequest,
                                                           AuthenticatedUserContext actor);

    VentaDetailResponseDto crearVentaFisicaPendientePago(VentaFisicaCreateRequestDto request,
                                                         AuthenticatedUserContext actor);

    VentaDetailResponseDto confirmarVentaFisicaConPagoEfectivo(Long idVenta,
                                                               PagoEfectivoRequestDto request,
                                                               AuthenticatedUserContext actor);

    VentaDetailResponseDto confirmarVentaFisicaPagadaStripe(Long idVenta,
                                                            String stripePaymentIntentId,
                                                            AuthenticatedUserContext actor);

    VentaDetailResponseDto anularVentaFisica(Long idVenta,
                                             EstadoChangeRequestDto request,
                                             AuthenticatedUserContext actor);
}