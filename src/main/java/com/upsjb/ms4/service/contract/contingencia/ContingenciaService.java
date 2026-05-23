// ruta: src/main/java/com/upsjb/ms4/service/contract/contingencia/ContingenciaService.java
package com.upsjb.ms4.service.contract.contingencia;

import com.upsjb.ms4.domain.entity.contingencia.ModoContingencia;
import com.upsjb.ms4.dto.contingencia.filter.InventarioEventoPendienteFilterDto;
import com.upsjb.ms4.dto.contingencia.filter.ModoContingenciaFilterDto;
import com.upsjb.ms4.dto.contingencia.request.ContingenciaActivarRequestDto;
import com.upsjb.ms4.dto.contingencia.request.ContingenciaFinalizarRequestDto;
import com.upsjb.ms4.dto.contingencia.response.ContingenciaReconciliacionResponseDto;
import com.upsjb.ms4.dto.contingencia.response.InventarioEventoPendienteResponseDto;
import com.upsjb.ms4.dto.contingencia.response.ModoContingenciaResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;

public interface ContingenciaService {

    ModoContingenciaResponseDto activarContingencia(ContingenciaActivarRequestDto request,
                                                    AuthenticatedUserContext actor);

    ModoContingenciaResponseDto finalizarContingencia(ContingenciaFinalizarRequestDto request,
                                                      AuthenticatedUserContext actor);

    ModoContingenciaResponseDto obtenerContingenciaActual(AuthenticatedUserContext actor);

    PageResponseDto<ModoContingenciaResponseDto> listarContingencias(ModoContingenciaFilterDto filter,
                                                                     PageRequestDto page,
                                                                     AuthenticatedUserContext actor);

    PageResponseDto<InventarioEventoPendienteResponseDto> listarEventosPendientes(InventarioEventoPendienteFilterDto filter,
                                                                                  PageRequestDto page,
                                                                                  AuthenticatedUserContext actor);

    ContingenciaReconciliacionResponseDto reconciliarEventosPendientes(AuthenticatedUserContext actor);

    InventarioEventoPendienteResponseDto marcarEventoSincronizado(Long idEventoPendiente,
                                                                  AuthenticatedUserContext actor);

    InventarioEventoPendienteResponseDto marcarEventoError(Long idEventoPendiente,
                                                           String error,
                                                           AuthenticatedUserContext actor);

    void validarVentaPermitidaPorContingencia();

    ModoContingencia resolverContingenciaActiva();
}