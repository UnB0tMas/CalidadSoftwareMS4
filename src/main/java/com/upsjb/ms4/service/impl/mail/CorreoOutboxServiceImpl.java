// ruta: src/main/java/com/upsjb/ms4/service/impl/mail/CorreoOutboxServiceImpl.java
package com.upsjb.ms4.service.impl.mail;

import com.upsjb.ms4.config.CorreoOutboxProperties;
import com.upsjb.ms4.domain.entity.mail.CorreoOutbox;
import com.upsjb.ms4.domain.enums.EstadoCorreo;
import com.upsjb.ms4.domain.enums.TipoCorreo;
import com.upsjb.ms4.dto.mail.filter.CorreoOutboxFilterDto;
import com.upsjb.ms4.dto.mail.request.CorreoOutboxReintentoRequestDto;
import com.upsjb.ms4.dto.mail.response.CorreoOutboxResponseDto;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.mapper.mail.CorreoOutboxMapper;
import com.upsjb.ms4.policy.CorreoOutboxPolicy;
import com.upsjb.ms4.repository.CorreoOutboxRepository;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.auditoria.AuditoriaFuncionalService;
import com.upsjb.ms4.service.contract.mail.CorreoOutboxService;
import com.upsjb.ms4.shared.audit.AuditContextHolder;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.shared.pagination.PaginationService;
import com.upsjb.ms4.specification.CorreoOutboxSpecification;
import com.upsjb.ms4.validator.CorreoOutboxValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
public class CorreoOutboxServiceImpl implements CorreoOutboxService {

    private static final Logger log = LoggerFactory.getLogger(CorreoOutboxServiceImpl.class);

    private static final String RECURSO_CORREO_OUTBOX = "Correo outbox";
    private static final String ENTIDAD_CORREO_OUTBOX = "CORREO_OUTBOX";
    private static final int MAX_BATCH_SIZE_ABSOLUTO = 200;

    private final CorreoOutboxRepository correoOutboxRepository;
    private final CorreoOutboxMapper correoOutboxMapper;
    private final CorreoOutboxValidator correoOutboxValidator;
    private final CorreoOutboxPolicy correoOutboxPolicy;
    private final PaginationService paginationService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final CorreoOutboxProperties properties;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final Clock clock;

    public CorreoOutboxServiceImpl(CorreoOutboxRepository correoOutboxRepository,
                                   CorreoOutboxMapper correoOutboxMapper,
                                   CorreoOutboxValidator correoOutboxValidator,
                                   CorreoOutboxPolicy correoOutboxPolicy,
                                   PaginationService paginationService,
                                   AuthenticatedUserResolver authenticatedUserResolver,
                                   CorreoOutboxProperties properties,
                                   AuditoriaFuncionalService auditoriaFuncionalService,
                                   Clock clock) {
        this.correoOutboxRepository = correoOutboxRepository;
        this.correoOutboxMapper = correoOutboxMapper;
        this.correoOutboxValidator = correoOutboxValidator;
        this.correoOutboxPolicy = correoOutboxPolicy;
        this.paginationService = paginationService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.properties = properties;
        this.auditoriaFuncionalService = auditoriaFuncionalService;
        this.clock = clock;
    }

    @Override
    @Transactional
    public CorreoOutboxResponseDto programarCorreo(TipoCorreo tipoCorreo,
                                                   String entidadOrigen,
                                                   Long idEntidadOrigen,
                                                   Long idBoleta,
                                                   String destinatarioEmail,
                                                   String destinatarioNombre,
                                                   String asunto,
                                                   AuthenticatedUserContext actor) {
        LocalDateTime now = LocalDateTime.now(clock);

        try {
            correoOutboxValidator.validarProgramacion(tipoCorreo, destinatarioEmail, asunto, now);
            correoOutboxValidator.validarEntidadOrigen(entidadOrigen, idEntidadOrigen);

            AuditContextHolder.AuditContext auditContext = AuditContextHolder.getOrEmpty();

            CorreoOutbox correo = CorreoOutbox.builder()
                    .eventId(UUID.randomUUID())
                    .tipoCorreo(tipoCorreo)
                    .entidadOrigen(entidadOrigen.trim())
                    .idEntidadOrigen(idEntidadOrigen)
                    .idBoleta(idBoleta)
                    .destinatarioEmail(destinatarioEmail.trim())
                    .destinatarioNombre(normalize(destinatarioNombre))
                    .asunto(asunto.trim())
                    .estadoCorreo(EstadoCorreo.PENDIENTE)
                    .attempts(0)
                    .maxAttempts(properties.maxAttemptsSafe())
                    .fechaProgramada(now)
                    .requestId(auditContext.requestId())
                    .correlationId(auditContext.correlationId())
                    .estado(true)
                    .build();

            correo = correoOutboxRepository.save(correo);
            return correoOutboxMapper.toResponse(correo);
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico programando correo outbox. tipoCorreo={}, entidadOrigen={}, idEntidadOrigen={}, idBoleta={}, destinatario={}, actorIdUsuarioMs1={}",
                    tipoCorreo,
                    entidadOrigen,
                    idEntidadOrigen,
                    idBoleta,
                    destinatarioEmail,
                    actor == null ? null : actor.idUsuarioMs1(),
                    ex
            );
            throw internalError("No se pudo programar el correo.", ex);
        }
    }

    @Override
    @Transactional
    public void programarAlertaAdministradores(TipoCorreo tipoCorreo,
                                               String asunto,
                                               String detalle,
                                               AuthenticatedUserContext actor) {
        String adminEmail = properties.adminAlertEmail();

        if (adminEmail == null || adminEmail.isBlank()) {
            log.warn(
                    "No se programó alerta administrativa porque ms4.correo-outbox.admin-alert-email no está configurado. tipoCorreo={}, asunto={}",
                    tipoCorreo,
                    asunto
            );
            return;
        }

        CorreoOutboxResponseDto correo = programarCorreo(
                tipoCorreo,
                "ALERTA_ADMINISTRATIVA",
                null,
                null,
                adminEmail,
                "Administración",
                asunto,
                actor
        );

        if (detalle != null && !detalle.isBlank()) {
            log.info(
                    "Alerta administrativa programada. idCorreoOutbox={}, tipoCorreo={}, detalle={}",
                    correo.id(),
                    tipoCorreo,
                    truncate(detalle, 500)
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<CorreoOutboxResponseDto> listar(CorreoOutboxFilterDto filter, PageRequestDto page) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        correoOutboxPolicy.authorizeListarCorreos(actor);
        correoOutboxValidator.validarFiltro(filter);

        try {
            Page<CorreoOutbox> result = correoOutboxRepository.findAll(
                    CorreoOutboxSpecification.build(filter),
                    paginationService.toPageable(page, "fechaProgramada")
            );

            return paginationService.toPageResponse(result, correoOutboxMapper::toResponse);
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico listando correos outbox. actorIdUsuarioMs1={}, actorRol={}, search={}, tipoCorreo={}, estadoCorreo={}, requestId={}, correlationId={}",
                    actor.idUsuarioMs1(),
                    actor.rol(),
                    filter == null ? null : filter.search(),
                    filter == null ? null : filter.tipoCorreo(),
                    filter == null ? null : filter.estadoCorreo(),
                    AuditContextHolder.getOrEmpty().requestId(),
                    AuditContextHolder.getOrEmpty().correlationId(),
                    ex
            );
            throw internalError("No se pudo listar los correos outbox.", ex);
        }
    }

    @Override
    @Transactional
    public CorreoOutboxResponseDto reintentar(Long idCorreoOutbox,
                                              CorreoOutboxReintentoRequestDto request,
                                              AuthenticatedUserContext actor) {
        try {
            correoOutboxPolicy.authorizeReintentarCorreo(actor);

            CorreoOutbox correo = resolverActivo(idCorreoOutbox);
            correoOutboxValidator.validarReintento(correo);

            correo.setEstadoCorreo(EstadoCorreo.PENDIENTE);
            correo.setAttempts(0);
            correo.setLastError(null);
            correo.setFechaProgramada(LocalDateTime.now(clock));

            correo = correoOutboxRepository.save(correo);

            auditoriaFuncionalService.registrarExito(
                    ENTIDAD_CORREO_OUTBOX,
                    correo.getId(),
                    "REINTENTAR_CORREO_OUTBOX",
                    actor,
                    request == null ? null : request.motivo()
            );

            return correoOutboxMapper.toResponse(correo);
        } catch (BusinessException ex) {
            auditoriaFuncionalService.registrarErrorUsuario(
                    ENTIDAD_CORREO_OUTBOX,
                    idCorreoOutbox,
                    "REINTENTAR_CORREO_OUTBOX",
                    actor,
                    ex.getCode(),
                    ex.getMessage()
            );
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico reintentando correo outbox. idCorreoOutbox={}, actorIdUsuarioMs1={}",
                    idCorreoOutbox,
                    actor == null ? null : actor.idUsuarioMs1(),
                    ex
            );
            auditoriaFuncionalService.registrarErrorTecnico(
                    ENTIDAD_CORREO_OUTBOX,
                    idCorreoOutbox,
                    "REINTENTAR_CORREO_OUTBOX",
                    actor,
                    ex
            );
            throw internalError("No se pudo reintentar el correo outbox.", ex);
        }
    }

    @Override
    @Transactional
    public CorreoOutboxResponseDto descartar(Long idCorreoOutbox,
                                             EstadoChangeRequestDto request,
                                             AuthenticatedUserContext actor) {
        try {
            correoOutboxPolicy.authorizeDescartarCorreo(actor);

            CorreoOutbox correo = resolverActivo(idCorreoOutbox);
            correoOutboxValidator.validarNoDescartarCorreoEnviando(correo);

            correo.setEstadoCorreo(EstadoCorreo.DESCARTADO);
            correo.setLastError(request == null ? "Correo descartado por administrador." : truncate(request.motivo(), 1000));

            correo = correoOutboxRepository.save(correo);

            auditoriaFuncionalService.registrarExito(
                    ENTIDAD_CORREO_OUTBOX,
                    correo.getId(),
                    "DESCARTAR_CORREO_OUTBOX",
                    actor,
                    request == null ? null : request.motivo()
            );

            return correoOutboxMapper.toResponse(correo);
        } catch (BusinessException ex) {
            auditoriaFuncionalService.registrarErrorUsuario(
                    ENTIDAD_CORREO_OUTBOX,
                    idCorreoOutbox,
                    "DESCARTAR_CORREO_OUTBOX",
                    actor,
                    ex.getCode(),
                    ex.getMessage()
            );
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico descartando correo outbox. idCorreoOutbox={}, actorIdUsuarioMs1={}",
                    idCorreoOutbox,
                    actor == null ? null : actor.idUsuarioMs1(),
                    ex
            );
            auditoriaFuncionalService.registrarErrorTecnico(
                    ENTIDAD_CORREO_OUTBOX,
                    idCorreoOutbox,
                    "DESCARTAR_CORREO_OUTBOX",
                    actor,
                    ex
            );
            throw internalError("No se pudo descartar el correo outbox.", ex);
        }
    }

    @Override
    @Transactional
    public List<CorreoOutbox> reclamarBatchPendiente(String workerId, int batchSize) {
        correoOutboxValidator.validarWorker(workerId);
        correoOutboxValidator.validarBatchSize(batchSize);

        int safeBatchSize = Math.min(Math.min(batchSize, properties.batchSizeSafe()), MAX_BATCH_SIZE_ABSOLUTO);
        Collection<EstadoCorreo> estados = List.of(EstadoCorreo.PENDIENTE, EstadoCorreo.ERROR);
        LocalDateTime now = LocalDateTime.now(clock);

        List<CorreoOutbox> correos = correoOutboxRepository.findProcessableForUpdate(
                estados,
                now,
                PageRequest.of(0, safeBatchSize)
        );

        correos.forEach(correo -> marcarReclamado(correo, now));
        correoOutboxRepository.saveAll(correos);

        if (!correos.isEmpty()) {
            log.debug("Batch de correos reclamado. workerId={}, cantidad={}", workerId, correos.size());
        }

        return correos;
    }

    @Override
    @Transactional
    public void marcarEnviando(Long idCorreoOutbox, String workerId) {
        correoOutboxValidator.validarWorker(workerId);
        CorreoOutbox correo = resolverActivo(idCorreoOutbox);

        if (correo.getEstadoCorreo() == EstadoCorreo.ENVIANDO) {
            return;
        }

        correoOutboxValidator.validarProcesable(correo);
        marcarReclamado(correo, LocalDateTime.now(clock));
        correoOutboxRepository.save(correo);
    }

    @Override
    @Transactional
    public void marcarEnviado(Long idCorreoOutbox) {
        CorreoOutbox correo = resolverActivo(idCorreoOutbox);
        correoOutboxValidator.validarMarcarEnviado(correo);

        if (correo.getEstadoCorreo() == EstadoCorreo.ENVIADO) {
            return;
        }

        correo.setEstadoCorreo(EstadoCorreo.ENVIADO);
        correo.setFechaEnvio(LocalDateTime.now(clock));
        correo.setLastError(null);

        correoOutboxRepository.save(correo);
    }

    @Override
    @Transactional
    public void marcarError(Long idCorreoOutbox, String errorDetalle) {
        CorreoOutbox correo = resolverActivo(idCorreoOutbox);

        if (correo.getEstadoCorreo() == EstadoCorreo.ENVIADO || correo.getEstadoCorreo() == EstadoCorreo.DESCARTADO) {
            log.warn(
                    "Se ignoró marca de error para correo outbox en estado final. idCorreoOutbox={}, estadoCorreo={}",
                    idCorreoOutbox,
                    correo.getEstadoCorreo()
            );
            return;
        }

        correo.setEstadoCorreo(EstadoCorreo.ERROR);
        correo.setLastError(truncate(errorDetalle, 4000));

        correoOutboxRepository.save(correo);
    }

    private void marcarReclamado(CorreoOutbox correo, LocalDateTime now) {
        int attempts = correo.getAttempts() == null ? 0 : correo.getAttempts();
        correo.setEstadoCorreo(EstadoCorreo.ENVIANDO);
        correo.setAttempts(attempts + 1);
        correo.setLastError(null);
        correo.setFechaProgramada(now);
    }

    private CorreoOutbox resolverActivo(Long idCorreoOutbox) {
        correoOutboxValidator.validarIdCorreoOutbox(idCorreoOutbox);

        return correoOutboxRepository.findById(idCorreoOutbox)
                .filter(CorreoOutbox::isActivo)
                .orElseThrow(() -> NotFoundException.byId(RECURSO_CORREO_OUTBOX, idCorreoOutbox));
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String truncate(String value, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();
        return normalized.length() <= max ? normalized : normalized.substring(0, max);
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