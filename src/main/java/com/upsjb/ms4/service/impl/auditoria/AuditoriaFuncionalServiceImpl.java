// ruta: src/main/java/com/upsjb/ms4/service/impl/auditoria/AuditoriaFuncionalServiceImpl.java
package com.upsjb.ms4.service.impl.auditoria;

import com.upsjb.ms4.domain.entity.auditoria.AuditoriaFuncional;
import com.upsjb.ms4.domain.enums.ResultadoAuditoria;
import com.upsjb.ms4.dto.auditoria.filter.AuditoriaFilterDto;
import com.upsjb.ms4.dto.auditoria.response.AuditoriaResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.mapper.auditoria.AuditoriaMapper;
import com.upsjb.ms4.policy.AuditoriaPolicy;
import com.upsjb.ms4.repository.AuditoriaFuncionalRepository;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.auditoria.AuditoriaFuncionalService;
import com.upsjb.ms4.shared.audit.AuditEventFactory;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.shared.pagination.PaginationService;
import com.upsjb.ms4.specification.AuditoriaSpecification;
import com.upsjb.ms4.util.JsonUtil;
import com.upsjb.ms4.validator.AuditoriaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AuditoriaFuncionalServiceImpl implements AuditoriaFuncionalService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaFuncionalServiceImpl.class);

    private static final String RECURSO_AUDITORIA = "Auditoría funcional";

    private final AuditoriaFuncionalRepository auditoriaRepository;
    private final AuditoriaMapper auditoriaMapper;
    private final AuditoriaPolicy auditoriaPolicy;
    private final AuditoriaValidator auditoriaValidator;
    private final PaginationService paginationService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final AuditEventFactory auditEventFactory;

    public AuditoriaFuncionalServiceImpl(AuditoriaFuncionalRepository auditoriaRepository,
                                         AuditoriaMapper auditoriaMapper,
                                         AuditoriaPolicy auditoriaPolicy,
                                         AuditoriaValidator auditoriaValidator,
                                         PaginationService paginationService,
                                         AuthenticatedUserResolver authenticatedUserResolver,
                                         AuditEventFactory auditEventFactory) {
        this.auditoriaRepository = auditoriaRepository;
        this.auditoriaMapper = auditoriaMapper;
        this.auditoriaPolicy = auditoriaPolicy;
        this.auditoriaValidator = auditoriaValidator;
        this.paginationService = paginationService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.auditEventFactory = auditEventFactory;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarExito(String entidad,
                               Long entidadId,
                               String accion,
                               AuthenticatedUserContext actor,
                               Object detalle) {
        registrar(entidad, entidadId, accion, ResultadoAuditoria.EXITOSO, actor, detalle);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarErrorUsuario(String entidad,
                                      Long entidadId,
                                      String accion,
                                      AuthenticatedUserContext actor,
                                      String codigoError,
                                      String mensaje) {
        Map<String, Object> detalle = new LinkedHashMap<>();
        putIfPresent(detalle, "codigoError", codigoError);
        putIfPresent(detalle, "mensaje", mensaje);

        registrar(entidad, entidadId, accion, ResultadoAuditoria.ERROR_USUARIO, actor, detalle);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarErrorTecnico(String entidad,
                                      Long entidadId,
                                      String accion,
                                      AuthenticatedUserContext actor,
                                      Exception exception) {
        Map<String, Object> detalle = new LinkedHashMap<>();

        if (exception != null) {
            detalle.put("exceptionClass", exception.getClass().getName());
            putIfPresent(detalle, "exceptionMessage", exception.getMessage());

            Throwable cause = exception.getCause();
            if (cause != null) {
                detalle.put("causeClass", cause.getClass().getName());
                putIfPresent(detalle, "causeMessage", cause.getMessage());
            }

            log.error(
                    "Error técnico auditado. entidad={}, entidadId={}, accion={}, actorIdUsuarioMs1={}",
                    entidad,
                    entidadId,
                    accion,
                    actor == null ? null : actor.idUsuarioMs1(),
                    exception
            );
        }

        registrar(entidad, entidadId, accion, ResultadoAuditoria.ERROR_TECNICO, actor, detalle);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditoriaResponseDto obtenerPorId(Long idAuditoria) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        auditoriaPolicy.authorizeObtenerAuditoria(actor);
        auditoriaValidator.validarIdAuditoria(idAuditoria);

        try {
            AuditoriaFuncional auditoria = auditoriaRepository.findByIdAndEstadoTrue(idAuditoria)
                    .orElseThrow(() -> NotFoundException.byId(RECURSO_AUDITORIA, idAuditoria));

            return auditoriaMapper.toDetailResponse(auditoria);
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico obteniendo auditoría funcional. idAuditoria={}, actorIdUsuarioMs1={}, actorRol={}",
                    idAuditoria,
                    actor.idUsuarioMs1(),
                    actor.rol(),
                    ex
            );
            throw internalError("No se pudo consultar la auditoría funcional.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<AuditoriaResponseDto> listar(AuditoriaFilterDto filter, PageRequestDto page) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        auditoriaPolicy.authorizeConsultarAuditoria(actor);
        auditoriaValidator.validarFiltro(filter);

        try {
            Page<AuditoriaFuncional> auditorias = auditoriaRepository.findAll(
                    AuditoriaSpecification.build(filter),
                    paginationService.toPageable(page, "createdAt")
            );

            return paginationService.toPageResponse(auditorias, auditoriaMapper::toResponse);
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico listando auditoría funcional. actorIdUsuarioMs1={}, actorRol={}, search={}, resultado={}, requestId={}, correlationId={}",
                    actor.idUsuarioMs1(),
                    actor.rol(),
                    filter == null ? null : filter.search(),
                    filter == null ? null : filter.resultado(),
                    filter == null ? null : filter.requestId(),
                    filter == null ? null : filter.correlationId(),
                    ex
            );
            throw internalError("No se pudo listar la auditoría funcional.", ex);
        }
    }

    private void registrar(String entidad,
                           Long entidadId,
                           String accion,
                           ResultadoAuditoria resultado,
                           AuthenticatedUserContext actor,
                           Object detalle) {
        String entidadIdText = entidadId == null ? null : String.valueOf(entidadId);

        try {
            String detalleJson = toDetalleJson(detalle);
            auditoriaValidator.validarRegistro(entidad, entidadId, accion, resultado, actor, detalleJson);

            AuditoriaFuncional auditoria = auditEventFactory.create(
                    entidad,
                    entidadIdText,
                    accion,
                    resultado,
                    actor,
                    detalleJson
            );

            auditoriaRepository.save(auditoria);
        } catch (BusinessException ex) {
            log.warn(
                    "No se registró auditoría funcional por inconsistencia de datos. entidad={}, entidadId={}, accion={}, resultado={}, actorIdUsuarioMs1={}, motivo={}",
                    entidad,
                    entidadId,
                    accion,
                    resultado,
                    actor == null ? null : actor.idUsuarioMs1(),
                    ex.getMessage()
            );
        } catch (RuntimeException ex) {
            log.error(
                    "No se pudo registrar auditoría funcional. entidad={}, entidadId={}, accion={}, resultado={}, actorIdUsuarioMs1={}",
                    entidad,
                    entidadId,
                    accion,
                    resultado,
                    actor == null ? null : actor.idUsuarioMs1(),
                    ex
            );
        }
    }

    private String toDetalleJson(Object detalle) {
        if (detalle == null) {
            return null;
        }

        if (detalle instanceof CharSequence text) {
            String value = text.toString();
            if (value.isBlank()) {
                return null;
            }

            return JsonUtil.toJson(Map.of("detalle", value.trim()));
        }

        return JsonUtil.toJson(detalle);
    }

    private void putIfPresent(Map<String, Object> target, String key, String value) {
        if (target == null || key == null || key.isBlank()) {
            return;
        }

        if (value != null && !value.isBlank()) {
            target.put(key, value.trim());
        }
    }

    private BusinessException internalError(String message, RuntimeException ex) {
        return new BusinessException(
                ErrorCodes.INTERNAL_ERROR,
                message,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex
        );
    }
}