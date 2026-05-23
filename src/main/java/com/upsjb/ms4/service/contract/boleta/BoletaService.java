// ruta: src/main/java/com/upsjb/ms4/service/contract/boleta/BoletaService.java
package com.upsjb.ms4.service.contract.boleta;

import com.upsjb.ms4.domain.entity.boleta.Boleta;
import com.upsjb.ms4.domain.enums.TipoCorreo;
import com.upsjb.ms4.dto.boleta.filter.BoletaFilterDto;
import com.upsjb.ms4.dto.boleta.response.BoletaDetailResponseDto;
import com.upsjb.ms4.dto.boleta.response.BoletaResponseDto;
import com.upsjb.ms4.dto.mail.response.CorreoOutboxResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;

public interface BoletaService {

    BoletaDetailResponseDto emitirBoletaPorVentaConfirmada(Long idVenta, AuthenticatedUserContext actor);

    BoletaDetailResponseDto obtenerDetalleAdmin(Long idBoleta, AuthenticatedUserContext actor);

    BoletaDetailResponseDto obtenerDetalleEmpleado(Long idBoleta, AuthenticatedUserContext actor);

    BoletaDetailResponseDto obtenerDetalleCliente(Long idBoleta, AuthenticatedUserContext actor);

    PageResponseDto<BoletaResponseDto> listarAdmin(BoletaFilterDto filter,
                                                   PageRequestDto page,
                                                   AuthenticatedUserContext actor);

    PageResponseDto<BoletaResponseDto> listarEmpleado(BoletaFilterDto filter,
                                                      PageRequestDto page,
                                                      AuthenticatedUserContext actor);

    PageResponseDto<BoletaResponseDto> listarCliente(BoletaFilterDto filter,
                                                     PageRequestDto page,
                                                     AuthenticatedUserContext actor);

    CorreoOutboxResponseDto programarCorreoBoleta(Long idBoleta,
                                                  TipoCorreo tipoCorreo,
                                                  AuthenticatedUserContext actor);

    void marcarBoletaEnviadaPorCorreo(Long idBoleta);

    Boleta resolverBoletaParaRender(Long idBoleta);
}