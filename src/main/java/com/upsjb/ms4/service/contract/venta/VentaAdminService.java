package com.upsjb.ms4.service.contract.venta;

import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms4.dto.venta.response.VentaDetailResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;

public interface VentaAdminService {

    VentaDetailResponseDto anularVenta(Long idVenta,
                                       EstadoChangeRequestDto request,
                                       AuthenticatedUserContext actor);
}