// ruta: src/main/java/com/upsjb/ms4/service/contract/config/BoletaPlantillaService.java
package com.upsjb.ms4.service.contract.config;

import com.upsjb.ms4.domain.entity.config.BoletaPlantillaVersion;
import com.upsjb.ms4.dto.config.filter.BoletaPlantillaFilterDto;
import com.upsjb.ms4.dto.config.request.BoletaPlantillaRequestDto;
import com.upsjb.ms4.dto.config.response.BoletaPlantillaResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;

public interface BoletaPlantillaService {

    BoletaPlantillaResponseDto crearNuevaVersion(BoletaPlantillaRequestDto request,
                                                 AuthenticatedUserContext actor);

    BoletaPlantillaResponseDto obtenerVersionVigente();

    BoletaPlantillaResponseDto obtenerPorId(Long idVersion);

    PageResponseDto<BoletaPlantillaResponseDto> listarVersiones(BoletaPlantillaFilterDto filter,
                                                                PageRequestDto page);

    BoletaPlantillaResponseDto activarVersion(Long idVersion, AuthenticatedUserContext actor);

    BoletaPlantillaVersion resolverPlantillaVigenteParaEmision();

    BoletaPlantillaVersion resolverPlantillaPorIdParaRender(Long idVersion);
}