// ruta: src/main/java/com/upsjb/ms4/service/contract/config/SerieBoletaService.java
package com.upsjb.ms4.service.contract.config;

import com.upsjb.ms4.domain.entity.config.SerieBoleta;
import com.upsjb.ms4.dto.config.filter.SerieBoletaFilterDto;
import com.upsjb.ms4.dto.config.request.SerieBoletaCreateRequestDto;
import com.upsjb.ms4.dto.config.response.SerieBoletaResponseDto;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;

public interface SerieBoletaService {

    SerieBoletaResponseDto crearSerie(SerieBoletaCreateRequestDto request, AuthenticatedUserContext actor);

    SerieBoletaResponseDto obtenerPorId(Long idSerie);

    PageResponseDto<SerieBoletaResponseDto> listar(SerieBoletaFilterDto filter, PageRequestDto page);

    SerieBoletaResponseDto cambiarEstado(Long idSerie,
                                         EstadoChangeRequestDto request,
                                         AuthenticatedUserContext actor);

    SerieBoleta resolverSerieActivaParaEmision();

    NumeroBoletaReservado reservarSiguienteNumero(Long idSerie, AuthenticatedUserContext actor);

    void confirmarNumeroUsado(Long idSerie, Long numeroReservado);

    void liberarNumeroReservadoSiFalla(Long idSerie, Long numeroReservado);
}