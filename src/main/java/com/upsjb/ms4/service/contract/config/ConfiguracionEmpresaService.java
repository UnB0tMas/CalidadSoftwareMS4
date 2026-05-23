// ruta: src/main/java/com/upsjb/ms4/service/contract/config/ConfiguracionEmpresaService.java
package com.upsjb.ms4.service.contract.config;

import com.upsjb.ms4.domain.entity.config.ConfiguracionEmpresaVersion;
import com.upsjb.ms4.dto.config.filter.ConfiguracionEmpresaFilterDto;
import com.upsjb.ms4.dto.config.request.ConfiguracionEmpresaRequestDto;
import com.upsjb.ms4.dto.config.response.ConfiguracionEmpresaResponseDto;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;

public interface ConfiguracionEmpresaService {

    ConfiguracionEmpresaResponseDto crearNuevaVersion(ConfiguracionEmpresaRequestDto request,
                                                      AuthenticatedUserContext actor);

    ConfiguracionEmpresaResponseDto obtenerVersionVigente();

    ConfiguracionEmpresaResponseDto obtenerPorId(Long idVersion);

    PageResponseDto<ConfiguracionEmpresaResponseDto> listarVersiones(ConfiguracionEmpresaFilterDto filter,
                                                                     PageRequestDto page);

    ConfiguracionEmpresaResponseDto activarVersion(Long idVersion, AuthenticatedUserContext actor);

    ConfiguracionEmpresaResponseDto cambiarEstado(Long idVersion,
                                                  EstadoChangeRequestDto request,
                                                  AuthenticatedUserContext actor);

    ConfiguracionEmpresaVersion resolverVersionVigenteParaEmisionBoleta();

    ConfiguracionEmpresaVersion resolverVersionPorIdParaRender(Long idVersion);
}