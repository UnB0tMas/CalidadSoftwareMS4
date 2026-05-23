// ruta: src/main/java/com/upsjb/ms4/service/impl/config/ConfiguracionTributariaServiceImpl.java
package com.upsjb.ms4.service.impl.config;

import com.upsjb.ms4.domain.entity.config.ConfiguracionTributariaVersion;
import com.upsjb.ms4.domain.enums.NombreImpuesto;
import com.upsjb.ms4.dto.config.filter.ConfiguracionTributariaFilterDto;
import com.upsjb.ms4.dto.config.request.ConfiguracionTributariaRequestDto;
import com.upsjb.ms4.dto.config.response.ConfiguracionTributariaResponseDto;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.mapper.config.ConfiguracionTributariaMapper;
import com.upsjb.ms4.policy.ConfiguracionPolicy;
import com.upsjb.ms4.repository.ConfiguracionTributariaVersionRepository;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.service.contract.auditoria.AuditoriaFuncionalService;
import com.upsjb.ms4.service.contract.config.ConfiguracionTributariaService;
import com.upsjb.ms4.shared.audit.AuditContextHolder;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.shared.exception.ConflictException;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.shared.pagination.PaginationService;
import com.upsjb.ms4.specification.ConfiguracionTributariaVersionSpecification;
import com.upsjb.ms4.util.CodigoGenerator;
import com.upsjb.ms4.validator.ConfiguracionTributariaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfiguracionTributariaServiceImpl implements ConfiguracionTributariaService {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracionTributariaServiceImpl.class);

    private static final String RECURSO = "Configuración tributaria";
    private static final String ENTIDAD = "CONFIGURACION_TRIBUTARIA";

    private final ConfiguracionTributariaVersionRepository repository;
    private final ConfiguracionTributariaMapper mapper;
    private final ConfiguracionTributariaValidator validator;
    private final ConfiguracionPolicy policy;
    private final PaginationService paginationService;
    private final AuditoriaFuncionalService auditoria;
    private final Clock clock;

    public ConfiguracionTributariaServiceImpl(ConfiguracionTributariaVersionRepository repository,
                                              ConfiguracionTributariaMapper mapper,
                                              ConfiguracionTributariaValidator validator,
                                              ConfiguracionPolicy policy,
                                              PaginationService paginationService,
                                              AuditoriaFuncionalService auditoria,
                                              Clock clock) {
        this.repository = repository;
        this.mapper = mapper;
        this.validator = validator;
        this.policy = policy;
        this.paginationService = paginationService;
        this.auditoria = auditoria;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ConfiguracionTributariaResponseDto crearNuevaVersionIgv(ConfiguracionTributariaRequestDto request,
                                                                   AuthenticatedUserContext actor) {
        try {
            policy.authorizeGestionTributaria(actor);
            validator.validarNuevaVersionIgv(request);

            List<ConfiguracionTributariaVersion> vigentesActuales =
                    repository.findVigentesByNombreImpuestoForUpdate(NombreImpuesto.IGV);

            if (Boolean.FALSE.equals(request.vigente()) && vigentesActuales.isEmpty()) {
                throw new ConflictException("La primera configuración IGV debe quedar vigente.");
            }

            ConfiguracionTributariaVersion entity = mapper.toEntity(request);
            entity.setCodigoVersion(generarCodigoVersion());
            entity.setNombreImpuesto(NombreImpuesto.IGV);
            entity.setFechaInicioVigencia(request.fechaInicioVigencia() == null
                    ? LocalDateTime.now(clock)
                    : request.fechaInicioVigencia());
            entity.setModificadoPorIdUsuarioMs1(actor.idUsuarioMs1());
            entity.setEstado(true);

            if (Boolean.TRUE.equals(entity.getVigente())) {
                cerrarVigentes(vigentesActuales, null);
            }

            entity = repository.saveAndFlush(entity);
            registrarExito("CREAR_VERSION_IGV", entity, actor, detalle(entity));
            return mapper.toResponse(entity);
        } catch (DataIntegrityViolationException ex) {
            ConflictException conflict = new ConflictException(
                    "No se pudo crear la versión IGV porque ya existe un dato único duplicado.",
                    ex
            );
            registrarErrorUsuario("CREAR_VERSION_IGV", null, actor, conflict);
            throw conflict;
        } catch (BusinessException ex) {
            registrarErrorUsuario("CREAR_VERSION_IGV", null, actor, ex);
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico creando versión IGV. actor={}, requestId={}, correlationId={}",
                    actorId(actor),
                    audit().requestId(),
                    audit().correlationId(),
                    ex
            );
            registrarErrorTecnico("CREAR_VERSION_IGV", null, actor, ex);
            throw internalError("No se pudo crear la versión IGV.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionTributariaResponseDto obtenerIgvVigente() {
        return mapper.toResponse(resolverIgvVigenteParaVenta());
    }

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionTributariaResponseDto obtenerPorId(Long idVersion) {
        return mapper.toDetailResponse(resolverPorId(idVersion));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ConfiguracionTributariaResponseDto> listarVersiones(ConfiguracionTributariaFilterDto filter,
                                                                               PageRequestDto page) {
        validator.validarFiltro(filter);

        Page<ConfiguracionTributariaVersion> result = repository.findAll(
                ConfiguracionTributariaVersionSpecification.build(filter),
                paginationService.toPageable(page, "fechaInicioVigencia")
        );

        return paginationService.toPageResponse(result, mapper::toResponse);
    }

    @Override
    @Transactional
    public ConfiguracionTributariaResponseDto activarVersionIgv(Long idVersion, AuthenticatedUserContext actor) {
        try {
            policy.authorizeGestionTributaria(actor);

            ConfiguracionTributariaVersion version = resolverPorIdForUpdate(idVersion);
            validator.validarActivacion(version);

            List<ConfiguracionTributariaVersion> vigentesActuales =
                    repository.findVigentesByNombreImpuestoForUpdate(NombreImpuesto.IGV);

            cerrarVigentes(vigentesActuales, version.getId());

            version.setVigente(true);
            version.setFechaInicioVigencia(version.getFechaInicioVigencia() == null
                    ? LocalDateTime.now(clock)
                    : version.getFechaInicioVigencia());
            version.setFechaFinVigencia(null);
            version.setModificadoPorIdUsuarioMs1(actor.idUsuarioMs1());

            version = repository.saveAndFlush(version);
            registrarExito("ACTIVAR_VERSION_IGV", version, actor, detalle(version));
            return mapper.toResponse(version);
        } catch (DataIntegrityViolationException ex) {
            ConflictException conflict = new ConflictException(
                    "No se pudo activar la versión IGV por conflicto de integridad.",
                    ex
            );
            registrarErrorUsuario("ACTIVAR_VERSION_IGV", idVersion, actor, conflict);
            throw conflict;
        } catch (BusinessException ex) {
            registrarErrorUsuario("ACTIVAR_VERSION_IGV", idVersion, actor, ex);
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico activando versión IGV. idVersion={}, actor={}, requestId={}, correlationId={}",
                    idVersion,
                    actorId(actor),
                    audit().requestId(),
                    audit().correlationId(),
                    ex
            );
            registrarErrorTecnico("ACTIVAR_VERSION_IGV", idVersion, actor, ex);
            throw internalError("No se pudo activar la versión IGV.", ex);
        }
    }

    @Override
    @Transactional
    public ConfiguracionTributariaResponseDto cambiarEstado(Long idVersion,
                                                            EstadoChangeRequestDto request,
                                                            AuthenticatedUserContext actor) {
        try {
            policy.authorizeGestionTributaria(actor);

            ConfiguracionTributariaVersion version = resolverPorIdForUpdate(idVersion);
            validator.validarCambioEstado(version, request);

            if (Boolean.TRUE.equals(request.estado())) {
                version.activar();
            } else {
                version.inactivar();
            }

            version = repository.saveAndFlush(version);
            registrarExito(
                    "CAMBIAR_ESTADO_VERSION_IGV",
                    version,
                    actor,
                    Map.of("motivo", request.motivo().trim())
            );

            return mapper.toResponse(version);
        } catch (DataIntegrityViolationException ex) {
            ConflictException conflict = new ConflictException(
                    "No se pudo cambiar el estado de la versión IGV por conflicto de integridad.",
                    ex
            );
            registrarErrorUsuario("CAMBIAR_ESTADO_VERSION_IGV", idVersion, actor, conflict);
            throw conflict;
        } catch (BusinessException ex) {
            registrarErrorUsuario("CAMBIAR_ESTADO_VERSION_IGV", idVersion, actor, ex);
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico cambiando estado de versión IGV. idVersion={}, actor={}, requestId={}, correlationId={}",
                    idVersion,
                    actorId(actor),
                    audit().requestId(),
                    audit().correlationId(),
                    ex
            );
            registrarErrorTecnico("CAMBIAR_ESTADO_VERSION_IGV", idVersion, actor, ex);
            throw internalError("No se pudo cambiar el estado de la versión IGV.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionTributariaVersion resolverIgvVigenteParaVenta() {
        List<ConfiguracionTributariaVersion> vigentes =
                repository.findByNombreImpuestoAndVigenteTrueAndEstadoTrueOrderByFechaInicioVigenciaDesc(NombreImpuesto.IGV);

        if (vigentes.isEmpty()) {
            throw new NotFoundException("No existe configuración tributaria IGV vigente.");
        }

        if (vigentes.size() > 1) {
            log.warn("Se detectaron múltiples configuraciones IGV vigentes activas. total={}", vigentes.size());
        }

        return vigentes.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionTributariaVersion resolverVersionPorIdParaRender(Long idVersion) {
        return resolverPorId(idVersion);
    }

    private ConfiguracionTributariaVersion resolverPorId(Long id) {
        if (id == null || id <= 0) {
            throw new NotFoundException(RECURSO + " no encontrada con id: " + id);
        }

        return repository.findById(id)
                .orElseThrow(() -> NotFoundException.byId(RECURSO, id));
    }

    private ConfiguracionTributariaVersion resolverPorIdForUpdate(Long id) {
        if (id == null || id <= 0) {
            throw new NotFoundException(RECURSO + " no encontrada con id: " + id);
        }

        return repository.findByIdForUpdate(id)
                .orElseThrow(() -> NotFoundException.byId(RECURSO, id));
    }

    private void cerrarVigentes(List<ConfiguracionTributariaVersion> vigentes, Long exceptId) {
        LocalDateTime now = LocalDateTime.now(clock);

        for (ConfiguracionTributariaVersion actual : vigentes) {
            if (exceptId == null || !actual.getId().equals(exceptId)) {
                actual.setVigente(false);
                actual.setFechaFinVigencia(actual.getFechaFinVigencia() == null
                        ? now
                        : actual.getFechaFinVigencia());
                repository.save(actual);
            }
        }
    }

    private String generarCodigoVersion() {
        long base = repository.count() + 1L;

        for (long sequence = base; sequence < base + 1000L; sequence++) {
            String code = CodigoGenerator.configuracionTributariaVersion(sequence);
            if (!repository.existsByCodigoVersionIgnoreCase(code)) {
                return code;
            }
        }

        throw new ConflictException("No se pudo generar un código único de versión IGV.");
    }

    private Map<String, Object> detalle(ConfiguracionTributariaVersion entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("codigoVersion", entity.getCodigoVersion());
        map.put("nombreImpuesto", entity.getNombreImpuesto());
        map.put("porcentaje", entity.getPorcentaje());
        map.put("vigente", entity.getVigente());
        return map;
    }

    private void registrarExito(String accion, ConfiguracionTributariaVersion entity, AuthenticatedUserContext actor, Object detalle) {
        auditoria.registrarExito(ENTIDAD, entity == null ? null : entity.getId(), accion, actor, detalle);
    }

    private void registrarErrorUsuario(String accion, Long id, AuthenticatedUserContext actor, BusinessException ex) {
        auditoria.registrarErrorUsuario(ENTIDAD, id, accion, actor, ex.getCode(), ex.getMessage());
    }

    private void registrarErrorTecnico(String accion, Long id, AuthenticatedUserContext actor, Exception ex) {
        auditoria.registrarErrorTecnico(ENTIDAD, id, accion, actor, ex);
    }

    private BusinessException internalError(String message, RuntimeException ex) {
        return new BusinessException(ErrorCodes.INTERNAL_ERROR, message, HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }

    private AuditContextHolder.AuditContext audit() {
        return AuditContextHolder.getOrEmpty();
    }

    private Long actorId(AuthenticatedUserContext actor) {
        return actor == null ? null : actor.idUsuarioMs1();
    }
}