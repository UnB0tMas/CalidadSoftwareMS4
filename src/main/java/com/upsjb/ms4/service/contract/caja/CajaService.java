// ruta: src/main/java/com/upsjb/ms4/service/contract/caja/CajaService.java
package com.upsjb.ms4.service.contract.caja;

import com.upsjb.ms4.domain.entity.caja.Caja;
import com.upsjb.ms4.dto.caja.filter.CajaFilterDto;
import com.upsjb.ms4.dto.caja.request.CajaAjusteRequestDto;
import com.upsjb.ms4.dto.caja.request.CajaAperturaRequestDto;
import com.upsjb.ms4.dto.caja.request.CajaCierreRequestDto;
import com.upsjb.ms4.dto.caja.response.CajaCierreResponseDto;
import com.upsjb.ms4.dto.caja.response.CajaDetailResponseDto;
import com.upsjb.ms4.dto.caja.response.CajaResponseDto;
import com.upsjb.ms4.dto.caja.response.CajaResumenDiaResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;

import java.time.LocalDate;

public interface CajaService {

    CajaResponseDto abrirCaja(CajaAperturaRequestDto request, AuthenticatedUserContext actor);

    CajaCierreResponseDto cerrarCaja(CajaCierreRequestDto request, AuthenticatedUserContext actor);

    CajaResponseDto obtenerCajaActual(AuthenticatedUserContext actor);

    CajaDetailResponseDto obtenerDetalle(Long idCaja, AuthenticatedUserContext actor);

    PageResponseDto<CajaResponseDto> listar(CajaFilterDto filter,
                                            PageRequestDto page,
                                            AuthenticatedUserContext actor);

    CajaResumenDiaResponseDto obtenerResumenDia(LocalDate fechaOperacion, AuthenticatedUserContext actor);

    CajaResponseDto registrarAjuste(CajaAjusteRequestDto request, AuthenticatedUserContext actor);

    Caja resolverCajaAbiertaParaVentaFisica();

    void recalcularTotalesCaja(Long idCaja);
}