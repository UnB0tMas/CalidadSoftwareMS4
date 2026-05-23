// ruta: src/main/java/com/upsjb/ms4/service/contract/venta/VentaOnlineService.java
package com.upsjb.ms4.service.contract.venta;

import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms4.dto.venta.request.VentaCalculoPreviewRequestDto;
import com.upsjb.ms4.dto.venta.request.VentaOnlineCreateRequestDto;
import com.upsjb.ms4.dto.venta.response.VentaCalculoPreviewResponseDto;
import com.upsjb.ms4.dto.venta.response.VentaDetailResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;

public interface VentaOnlineService {

    VentaCalculoPreviewResponseDto previsualizarVentaOnline(VentaCalculoPreviewRequestDto request,
                                                            AuthenticatedUserContext actor);

    VentaDetailResponseDto crearVentaOnlinePendientePago(VentaOnlineCreateRequestDto request,
                                                         AuthenticatedUserContext actor);

    VentaDetailResponseDto confirmarVentaOnlinePagadaStripe(String stripePaymentIntentId);

    VentaDetailResponseDto rechazarVentaOnlinePorStripe(String stripePaymentIntentId, String motivo);

    VentaDetailResponseDto anularVentaOnline(Long idVenta,
                                             EstadoChangeRequestDto request,
                                             AuthenticatedUserContext actor);
}