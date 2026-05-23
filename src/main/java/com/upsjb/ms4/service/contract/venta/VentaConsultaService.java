// ruta: src/main/java/com/upsjb/ms4/service/contract/venta/VentaConsultaService.java
package com.upsjb.ms4.service.contract.venta;

import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.dto.venta.filter.VentaFilterDto;
import com.upsjb.ms4.dto.venta.response.VentaDetailResponseDto;
import com.upsjb.ms4.dto.venta.response.VentaResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;

public interface VentaConsultaService {

    PageResponseDto<VentaResponseDto> listarVentasAdmin(VentaFilterDto filter,
                                                        PageRequestDto page,
                                                        AuthenticatedUserContext actor);

    PageResponseDto<VentaResponseDto> listarVentasEmpleado(VentaFilterDto filter,
                                                           PageRequestDto page,
                                                           AuthenticatedUserContext actor);

    PageResponseDto<VentaResponseDto> listarVentasCliente(VentaFilterDto filter,
                                                          PageRequestDto page,
                                                          AuthenticatedUserContext actor);

    VentaDetailResponseDto obtenerDetalleAdmin(Long idVenta, AuthenticatedUserContext actor);

    VentaDetailResponseDto obtenerDetalleEmpleado(Long idVenta, AuthenticatedUserContext actor);

    VentaDetailResponseDto obtenerDetalleCliente(Long idVenta, AuthenticatedUserContext actor);

    Venta resolverVentaParaProcesoInterno(Long idVenta);
}