// ruta: src/main/java/com/upsjb/ms4/service/contract/config/ConfiguracionTributariaService.java
package com.upsjb.ms4.service.contract.config;

import com.upsjb.ms4.domain.entity.config.ConfiguracionTributariaVersion;
import com.upsjb.ms4.dto.config.filter.ConfiguracionTributariaFilterDto;
import com.upsjb.ms4.dto.config.request.ConfiguracionTributariaRequestDto;
import com.upsjb.ms4.dto.config.response.ConfiguracionTributariaResponseDto;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;

public interface ConfiguracionTributariaService {

    ConfiguracionTributariaResponseDto crearNuevaVersionIgv(ConfiguracionTributariaRequestDto request,
                                                            AuthenticatedUserContext actor);

    ConfiguracionTributariaResponseDto obtenerIgvVigente();

    ConfiguracionTributariaResponseDto obtenerPorId(Long idVersion);

    PageResponseDto<ConfiguracionTributariaResponseDto> listarVersiones(ConfiguracionTributariaFilterDto filter,
                                                                        PageRequestDto page);

    ConfiguracionTributariaResponseDto activarVersionIgv(Long idVersion, AuthenticatedUserContext actor);

    ConfiguracionTributariaResponseDto cambiarEstado(Long idVersion,
                                                     EstadoChangeRequestDto request,
                                                     AuthenticatedUserContext actor);

    ConfiguracionTributariaVersion resolverIgvVigenteParaVenta();

    ConfiguracionTributariaVersion resolverVersionPorIdParaRender(Long idVersion);
}