// ruta: src/main/java/com/upsjb/ms4/service/impl/config/BoletaPlantillaServiceImpl.java
package com.upsjb.ms4.service.impl.config;

import com.upsjb.ms4.domain.entity.config.BoletaPlantillaVersion;
import com.upsjb.ms4.dto.config.filter.BoletaPlantillaFilterDto;
import com.upsjb.ms4.dto.config.request.BoletaPlantillaRequestDto;
import com.upsjb.ms4.dto.config.response.BoletaPlantillaResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.mapper.config.BoletaPlantillaMapper;
import com.upsjb.ms4.policy.ConfiguracionPolicy;
import com.upsjb.ms4.repository.BoletaPlantillaVersionRepository;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.service.contract.auditoria.AuditoriaFuncionalService;
import com.upsjb.ms4.service.contract.config.BoletaPlantillaService;
import com.upsjb.ms4.shared.audit.AuditContextHolder;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.shared.exception.ConflictException;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.shared.pagination.PaginationService;
import com.upsjb.ms4.specification.BoletaPlantillaVersionSpecification;
import com.upsjb.ms4.util.CodigoGenerator;
import com.upsjb.ms4.validator.BoletaPlantillaValidator;
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
public class BoletaPlantillaServiceImpl implements BoletaPlantillaService {

    private static final Logger log = LoggerFactory.getLogger(BoletaPlantillaServiceImpl.class);

    private static final String RECURSO = "Plantilla de boleta";
    private static final String ENTIDAD = "BOLETA_PLANTILLA_VERSION";

    private final BoletaPlantillaVersionRepository repository;
    private final BoletaPlantillaMapper mapper;
    private final BoletaPlantillaValidator validator;
    private final ConfiguracionPolicy policy;
    private final PaginationService paginationService;
    private final AuditoriaFuncionalService auditoria;
    private final Clock clock;

    public BoletaPlantillaServiceImpl(BoletaPlantillaVersionRepository repository,
                                      BoletaPlantillaMapper mapper,
                                      BoletaPlantillaValidator validator,
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
    public BoletaPlantillaResponseDto crearNuevaVersion(BoletaPlantillaRequestDto request,
                                                        AuthenticatedUserContext actor) {
        try {
            policy.authorizeGestionPlantilla(actor);
            validator.validarNuevaVersion(request);

            List<BoletaPlantillaVersion> vigentesActuales = repository.findVigentesForUpdate();

            if (Boolean.FALSE.equals(request.vigente()) && vigentesActuales.isEmpty()) {
                throw new ConflictException("La primera plantilla de boleta debe quedar vigente.");
            }

            BoletaPlantillaVersion entity = mapper.toEntity(request);
            entity.setCodigoVersion(generarCodigoVersion());
            entity.setNombre(normalizarTexto(request.nombre()));
            entity.setRutaTemplateHtml(normalizarRuta(request.rutaTemplateHtml()));
            entity.setRutaTemplateMail(normalizarRuta(request.rutaTemplateMail()));
            entity.setDescripcion(normalizarTexto(request.descripcion()));
            entity.setMotivo(normalizarTexto(request.motivo()));
            entity.setFechaInicioVigencia(request.fechaInicioVigencia() == null
                    ? LocalDateTime.now(clock)
                    : request.fechaInicioVigencia());
            entity.setCreadoPorIdUsuarioMs1(actor.idUsuarioMs1());
            entity.setEstado(true);

            if (Boolean.TRUE.equals(entity.getVigente())) {
                cerrarVigentes(vigentesActuales, null);
            }

            entity = repository.saveAndFlush(entity);
            registrarExito("CREAR_VERSION_PLANTILLA_BOLETA", entity, actor, detalle(entity));
            return mapper.toResponse(entity);
        } catch (DataIntegrityViolationException ex) {
            ConflictException conflict = new ConflictException(
                    "No se pudo crear la plantilla de boleta porque ya existe un dato único duplicado.",
                    ex
            );
            registrarErrorUsuario("CREAR_VERSION_PLANTILLA_BOLETA", null, actor, conflict);
            throw conflict;
        } catch (BusinessException ex) {
            registrarErrorUsuario("CREAR_VERSION_PLANTILLA_BOLETA", null, actor, ex);
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico creando plantilla de boleta. actor={}, requestId={}, correlationId={}",
                    actorId(actor),
                    audit().requestId(),
                    audit().correlationId(),
                    ex
            );
            registrarErrorTecnico("CREAR_VERSION_PLANTILLA_BOLETA", null, actor, ex);
            throw internalError("No se pudo crear la plantilla de boleta.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BoletaPlantillaResponseDto obtenerVersionVigente() {
        return mapper.toResponse(resolverPlantillaVigenteParaEmision());
    }

    @Override
    @Transactional(readOnly = true)
    public BoletaPlantillaResponseDto obtenerPorId(Long idVersion) {
        return mapper.toDetailResponse(resolverPorId(idVersion));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<BoletaPlantillaResponseDto> listarVersiones(BoletaPlantillaFilterDto filter,
                                                                       PageRequestDto page) {
        validator.validarFiltro(filter);

        Page<BoletaPlantillaVersion> result = repository.findAll(
                BoletaPlantillaVersionSpecification.build(filter),
                paginationService.toPageable(page, "fechaInicioVigencia")
        );

        return paginationService.toPageResponse(result, mapper::toResponse);
    }

    @Override
    @Transactional
    public BoletaPlantillaResponseDto activarVersion(Long idVersion, AuthenticatedUserContext actor) {
        try {
            policy.authorizeGestionPlantilla(actor);

            BoletaPlantillaVersion version = resolverPorIdForUpdate(idVersion);
            validator.validarActivacion(version);

            List<BoletaPlantillaVersion> vigentesActuales = repository.findVigentesForUpdate();
            cerrarVigentes(vigentesActuales, version.getId());

            version.setVigente(true);
            version.setFechaInicioVigencia(version.getFechaInicioVigencia() == null
                    ? LocalDateTime.now(clock)
                    : version.getFechaInicioVigencia());
            version.setFechaFinVigencia(null);
            version = repository.saveAndFlush(version);

            registrarExito("ACTIVAR_VERSION_PLANTILLA_BOLETA", version, actor, detalle(version));
            return mapper.toResponse(version);
        } catch (DataIntegrityViolationException ex) {
            ConflictException conflict = new ConflictException(
                    "No se pudo activar la plantilla de boleta por conflicto de integridad.",
                    ex
            );
            registrarErrorUsuario("ACTIVAR_VERSION_PLANTILLA_BOLETA", idVersion, actor, conflict);
            throw conflict;
        } catch (BusinessException ex) {
            registrarErrorUsuario("ACTIVAR_VERSION_PLANTILLA_BOLETA", idVersion, actor, ex);
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico activando plantilla de boleta. idVersion={}, actor={}, requestId={}, correlationId={}",
                    idVersion,
                    actorId(actor),
                    audit().requestId(),
                    audit().correlationId(),
                    ex
            );
            registrarErrorTecnico("ACTIVAR_VERSION_PLANTILLA_BOLETA", idVersion, actor, ex);
            throw internalError("No se pudo activar la plantilla de boleta.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BoletaPlantillaVersion resolverPlantillaVigenteParaEmision() {
        List<BoletaPlantillaVersion> vigentes = repository.findByVigenteTrueAndEstadoTrueOrderByFechaInicioVigenciaDesc();

        if (vigentes.isEmpty()) {
            throw new NotFoundException("No existe plantilla de boleta vigente.");
        }

        if (vigentes.size() > 1) {
            log.warn("Se detectaron múltiples plantillas de boleta vigentes activas. total={}", vigentes.size());
        }

        return vigentes.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public BoletaPlantillaVersion resolverPlantillaPorIdParaRender(Long idVersion) {
        return resolverPorId(idVersion);
    }

    private BoletaPlantillaVersion resolverPorId(Long id) {
        if (id == null || id <= 0) {
            throw new NotFoundException(RECURSO + " no encontrada con id: " + id);
        }

        return repository.findById(id)
                .orElseThrow(() -> NotFoundException.byId(RECURSO, id));
    }

    private BoletaPlantillaVersion resolverPorIdForUpdate(Long id) {
        if (id == null || id <= 0) {
            throw new NotFoundException(RECURSO + " no encontrada con id: " + id);
        }

        return repository.findByIdForUpdate(id)
                .orElseThrow(() -> NotFoundException.byId(RECURSO, id));
    }

    private void cerrarVigentes(List<BoletaPlantillaVersion> vigentes, Long exceptId) {
        LocalDateTime now = LocalDateTime.now(clock);

        for (BoletaPlantillaVersion actual : vigentes) {
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
            String code = CodigoGenerator.boletaPlantillaVersion(sequence);
            if (!repository.existsByCodigoVersionIgnoreCase(code)) {
                return code;
            }
        }

        throw new ConflictException("No se pudo generar un código único de plantilla de boleta.");
    }

    private Map<String, Object> detalle(BoletaPlantillaVersion entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("codigoVersion", entity.getCodigoVersion());
        map.put("nombre", entity.getNombre());
        map.put("vigente", entity.getVigente());
        return map;
    }

    private void registrarExito(String accion, BoletaPlantillaVersion entity, AuthenticatedUserContext actor, Object detalle) {
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

    private String normalizarTexto(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizarRuta(String value) {
        return value == null || value.isBlank() ? null : value.trim().replace("\\", "/");
    }

    private AuditContextHolder.AuditContext audit() {
        return AuditContextHolder.getOrEmpty();
    }

    private Long actorId(AuthenticatedUserContext actor) {
        return actor == null ? null : actor.idUsuarioMs1();
    }
}