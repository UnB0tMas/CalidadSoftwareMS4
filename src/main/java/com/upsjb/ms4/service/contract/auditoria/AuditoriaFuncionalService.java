// ruta: src/main/java/com/upsjb/ms4/service/contract/auditoria/AuditoriaFuncionalService.java
package com.upsjb.ms4.service.contract.auditoria;

import com.upsjb.ms4.dto.auditoria.filter.AuditoriaFilterDto;
import com.upsjb.ms4.dto.auditoria.response.AuditoriaResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;

public interface AuditoriaFuncionalService {

    void registrarExito(String entidad,
                        Long entidadId,
                        String accion,
                        AuthenticatedUserContext actor,
                        Object detalle);

    void registrarErrorUsuario(String entidad,
                               Long entidadId,
                               String accion,
                               AuthenticatedUserContext actor,
                               String codigoError,
                               String mensaje);

    void registrarErrorTecnico(String entidad,
                               Long entidadId,
                               String accion,
                               AuthenticatedUserContext actor,
                               Exception exception);

    AuditoriaResponseDto obtenerPorId(Long idAuditoria);

    PageResponseDto<AuditoriaResponseDto> listar(AuditoriaFilterDto filter, PageRequestDto page);
}