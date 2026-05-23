// ruta: src/main/java/com/upsjb/ms4/service/impl/config/ConfiguracionEmpresaServiceImpl.java
package com.upsjb.ms4.service.impl.config;

import com.upsjb.ms4.domain.entity.config.AssetCloudinary;
import com.upsjb.ms4.domain.entity.config.ConfiguracionEmpresaVersion;
import com.upsjb.ms4.dto.config.filter.ConfiguracionEmpresaFilterDto;
import com.upsjb.ms4.dto.config.request.ConfiguracionEmpresaRequestDto;
import com.upsjb.ms4.dto.config.response.ConfiguracionEmpresaResponseDto;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.mapper.config.ConfiguracionEmpresaMapper;
import com.upsjb.ms4.policy.ConfiguracionPolicy;
import com.upsjb.ms4.repository.AssetCloudinaryRepository;
import com.upsjb.ms4.repository.ConfiguracionEmpresaVersionRepository;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.service.contract.auditoria.AuditoriaFuncionalService;
import com.upsjb.ms4.service.contract.config.ConfiguracionEmpresaService;
import com.upsjb.ms4.shared.audit.AuditContextHolder;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.shared.exception.ConflictException;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.shared.pagination.PaginationService;
import com.upsjb.ms4.specification.ConfiguracionEmpresaVersionSpecification;
import com.upsjb.ms4.util.CodigoGenerator;
import com.upsjb.ms4.validator.ConfiguracionEmpresaValidator;
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class ConfiguracionEmpresaServiceImpl implements ConfiguracionEmpresaService {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracionEmpresaServiceImpl.class);

    private static final String RECURSO = "Configuración empresarial";
    private static final String ENTIDAD = "CONFIGURACION_EMPRESA";

    private final ConfiguracionEmpresaVersionRepository repository;
    private final AssetCloudinaryRepository assetRepository;
    private final ConfiguracionEmpresaMapper mapper;
    private final ConfiguracionEmpresaValidator validator;
    private final ConfiguracionPolicy policy;
    private final PaginationService paginationService;
    private final AuditoriaFuncionalService auditoria;
    private final Clock clock;

    public ConfiguracionEmpresaServiceImpl(ConfiguracionEmpresaVersionRepository repository,
                                           AssetCloudinaryRepository assetRepository,
                                           ConfiguracionEmpresaMapper mapper,
                                           ConfiguracionEmpresaValidator validator,
                                           ConfiguracionPolicy policy,
                                           PaginationService paginationService,
                                           AuditoriaFuncionalService auditoria,
                                           Clock clock) {
        this.repository = repository;
        this.assetRepository = assetRepository;
        this.mapper = mapper;
        this.validator = validator;
        this.policy = policy;
        this.paginationService = paginationService;
        this.auditoria = auditoria;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ConfiguracionEmpresaResponseDto crearNuevaVersion(ConfiguracionEmpresaRequestDto request,
                                                             AuthenticatedUserContext actor) {
        try {
            policy.authorizeGestionEmpresa(actor);

            AssetCloudinary logoAsset = resolverLogo(request == null ? null : request.idLogoAsset());
            validator.validarNuevaVersion(request, logoAsset);

            List<ConfiguracionEmpresaVersion> vigentesActuales = repository.findVigentesActivasForUpdate();

            ConfiguracionEmpresaVersion entity = mapper.toEntity(request);
            entity.setCodigoVersion(generarCodigoVersion());
            entity.setRuc(normalizarTexto(request.ruc()));
            entity.setRazonSocial(normalizarTexto(request.razonSocial()));
            entity.setNombreComercial(normalizarTexto(request.nombreComercial()));
            entity.setDireccionFiscal(normalizarTexto(request.direccionFiscal()));
            entity.setTelefono(normalizarTexto(request.telefono()));
            entity.setCorreo(normalizarLower(request.correo()));
            entity.setWeb(normalizarTexto(request.web()));
            entity.setColorPrimario(normalizarTexto(request.colorPrimario()));
            entity.setColorSecundario(normalizarTexto(request.colorSecundario()));
            entity.setMensajePieBoleta(normalizarTexto(request.mensajePieBoleta()));
            entity.setTerminosCondiciones(normalizarTexto(request.terminosCondiciones()));
            entity.setPoliticaCambios(normalizarTexto(request.politicaCambios()));
            entity.setMotivo(normalizarTexto(request.motivo()));
            entity.setFechaInicioVigencia(request.fechaInicioVigencia() == null
                    ? LocalDateTime.now(clock)
                    : request.fechaInicioVigencia());
            entity.setFechaFinVigencia(null);
            entity.setVigente(vigentesActuales.isEmpty());
            entity.setModificadoPorIdUsuarioMs1(actor.idUsuarioMs1());
            entity.setEstado(true);

            aplicarLogo(entity, logoAsset);

            entity = repository.saveAndFlush(entity);
            registrarExito("CREAR_VERSION_EMPRESA", entity, actor, detalle(entity));

            return mapper.toResponse(entity);
        } catch (DataIntegrityViolationException ex) {
            ConflictException conflict = new ConflictException(
                    "No se pudo crear la versión empresarial porque ya existe un dato único duplicado.",
                    ex
            );
            registrarErrorUsuario("CREAR_VERSION_EMPRESA", null, actor, conflict);
            throw conflict;
        } catch (BusinessException ex) {
            registrarErrorUsuario("CREAR_VERSION_EMPRESA", null, actor, ex);
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico creando versión empresarial. actor={}, requestId={}, correlationId={}",
                    actorId(actor),
                    audit().requestId(),
                    audit().correlationId(),
                    ex
            );
            registrarErrorTecnico("CREAR_VERSION_EMPRESA", null, actor, ex);
            throw internalError("No se pudo crear la versión empresarial.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionEmpresaResponseDto obtenerVersionVigente() {
        return mapper.toResponse(resolverVersionVigenteParaEmisionBoleta());
    }

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionEmpresaResponseDto obtenerPorId(Long idVersion) {
        return mapper.toDetailResponse(resolverPorId(idVersion));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ConfiguracionEmpresaResponseDto> listarVersiones(ConfiguracionEmpresaFilterDto filter,
                                                                            PageRequestDto page) {
        validator.validarFiltro(filter);

        Page<ConfiguracionEmpresaVersion> result = repository.findAll(
                ConfiguracionEmpresaVersionSpecification.build(filter),
                paginationService.toPageable(page, "fechaInicioVigencia")
        );

        return paginationService.toPageResponse(result, mapper::toResponse);
    }

    @Override
    @Transactional
    public ConfiguracionEmpresaResponseDto activarVersion(Long idVersion, AuthenticatedUserContext actor) {
        try {
            policy.authorizeGestionEmpresa(actor);

            ConfiguracionEmpresaVersion version = resolverPorIdForUpdate(idVersion);
            AssetCloudinary logoAsset = resolverLogo(version.getIdLogoAsset());
            validator.validarActivacion(version, logoAsset);

            LocalDateTime now = LocalDateTime.now(clock);
            cerrarVigentesExcepto(version.getId(), now);

            version.setVigente(true);
            version.setFechaInicioVigencia(version.getFechaInicioVigencia() == null
                    ? now
                    : version.getFechaInicioVigencia());
            version.setFechaFinVigencia(null);
            version.setModificadoPorIdUsuarioMs1(actor.idUsuarioMs1());
            version = repository.saveAndFlush(version);

            registrarExito("ACTIVAR_VERSION_EMPRESA", version, actor, detalle(version));
            return mapper.toResponse(version);
        } catch (DataIntegrityViolationException ex) {
            ConflictException conflict = new ConflictException(
                    "No se pudo activar la versión empresarial por conflicto de integridad.",
                    ex
            );
            registrarErrorUsuario("ACTIVAR_VERSION_EMPRESA", idVersion, actor, conflict);
            throw conflict;
        } catch (BusinessException ex) {
            registrarErrorUsuario("ACTIVAR_VERSION_EMPRESA", idVersion, actor, ex);
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico activando versión empresarial. idVersion={}, actor={}, requestId={}, correlationId={}",
                    idVersion,
                    actorId(actor),
                    audit().requestId(),
                    audit().correlationId(),
                    ex
            );
            registrarErrorTecnico("ACTIVAR_VERSION_EMPRESA", idVersion, actor, ex);
            throw internalError("No se pudo activar la versión empresarial.", ex);
        }
    }

    @Override
    @Transactional
    public ConfiguracionEmpresaResponseDto cambiarEstado(Long idVersion,
                                                         EstadoChangeRequestDto request,
                                                         AuthenticatedUserContext actor) {
        try {
            policy.authorizeGestionEmpresa(actor);

            ConfiguracionEmpresaVersion version = resolverPorIdForUpdate(idVersion);
            validator.validarCambioEstado(version, request);

            if (Boolean.FALSE.equals(request.estado()) && Boolean.TRUE.equals(version.getVigente())) {
                throw new ConflictException("No se puede inactivar la configuración empresarial vigente.");
            }

            if (Boolean.TRUE.equals(request.estado())) {
                AssetCloudinary logoAsset = resolverLogo(version.getIdLogoAsset());
                validator.validarAssetLogo(version.getIdLogoAsset(), logoAsset);
                version.activar();
            } else {
                version.inactivar();
            }

            version = repository.saveAndFlush(version);
            registrarExito(
                    "CAMBIAR_ESTADO_VERSION_EMPRESA",
                    version,
                    actor,
                    Map.of("motivo", request.motivo().trim())
            );

            return mapper.toResponse(version);
        } catch (DataIntegrityViolationException ex) {
            ConflictException conflict = new ConflictException(
                    "No se pudo cambiar el estado de la versión empresarial por conflicto de integridad.",
                    ex
            );
            registrarErrorUsuario("CAMBIAR_ESTADO_VERSION_EMPRESA", idVersion, actor, conflict);
            throw conflict;
        } catch (BusinessException ex) {
            registrarErrorUsuario("CAMBIAR_ESTADO_VERSION_EMPRESA", idVersion, actor, ex);
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico cambiando estado de versión empresarial. idVersion={}, actor={}, requestId={}, correlationId={}",
                    idVersion,
                    actorId(actor),
                    audit().requestId(),
                    audit().correlationId(),
                    ex
            );
            registrarErrorTecnico("CAMBIAR_ESTADO_VERSION_EMPRESA", idVersion, actor, ex);
            throw internalError("No se pudo cambiar el estado de la versión empresarial.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionEmpresaVersion resolverVersionVigenteParaEmisionBoleta() {
        List<ConfiguracionEmpresaVersion> vigentes =
                repository.findByVigenteTrueAndEstadoTrueOrderByFechaInicioVigenciaDesc();

        if (vigentes.isEmpty()) {
            throw new NotFoundException("No existe configuración empresarial vigente.");
        }

        if (vigentes.size() > 1) {
            log.warn("Se detectaron múltiples configuraciones empresariales vigentes activas. total={}", vigentes.size());
        }

        return vigentes.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionEmpresaVersion resolverVersionPorIdParaRender(Long idVersion) {
        return resolverPorId(idVersion);
    }

    private void cerrarVigentesExcepto(Long idVersionActivada, LocalDateTime fechaFin) {
        repository.findVigentesActivasForUpdate()
                .forEach(actual -> {
                    if (!Objects.equals(actual.getId(), idVersionActivada)) {
                        actual.setVigente(false);
                        actual.setFechaFinVigencia(actual.getFechaFinVigencia() == null
                                ? fechaFin
                                : actual.getFechaFinVigencia());
                        repository.save(actual);
                    }
                });
    }

    private ConfiguracionEmpresaVersion resolverPorId(Long id) {
        if (id == null || id <= 0) {
            throw new NotFoundException(RECURSO + " no encontrada con id: " + id);
        }

        return repository.findById(id)
                .orElseThrow(() -> NotFoundException.byId(RECURSO, id));
    }

    private ConfiguracionEmpresaVersion resolverPorIdForUpdate(Long id) {
        if (id == null || id <= 0) {
            throw new NotFoundException(RECURSO + " no encontrada con id: " + id);
        }

        return repository.findByIdForUpdate(id)
                .orElseThrow(() -> NotFoundException.byId(RECURSO, id));
    }

    private AssetCloudinary resolverLogo(Long idLogoAsset) {
        if (idLogoAsset == null) {
            return null;
        }

        return assetRepository.findById(idLogoAsset)
                .orElseThrow(() -> NotFoundException.byId("Asset Cloudinary", idLogoAsset));
    }

    private void aplicarLogo(ConfiguracionEmpresaVersion entity, AssetCloudinary logoAsset) {
        if (logoAsset == null) {
            entity.setLogoUrl(null);
            entity.setLogoPublicId(null);
            return;
        }

        entity.setLogoUrl(logoAsset.getSecureUrl());
        entity.setLogoPublicId(logoAsset.getPublicId());
    }

    private String generarCodigoVersion() {
        long base = repository.count() + 1L;

        for (long sequence = base; sequence < base + 1000L; sequence++) {
            String code = CodigoGenerator.configuracionEmpresaVersion(sequence);
            if (!repository.existsByCodigoVersionIgnoreCase(code)) {
                return code;
            }
        }

        throw new ConflictException("No se pudo generar un código único de versión empresarial.");
    }

    private Map<String, Object> detalle(ConfiguracionEmpresaVersion entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("codigoVersion", entity.getCodigoVersion());
        map.put("ruc", entity.getRuc());
        map.put("vigente", entity.getVigente());
        map.put("idLogoAsset", entity.getIdLogoAsset());
        return map;
    }

    private void registrarExito(String accion, ConfiguracionEmpresaVersion entity, AuthenticatedUserContext actor, Object detalle) {
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

    private String normalizarLower(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim().toLowerCase(Locale.ROOT);
    }

    private AuditContextHolder.AuditContext audit() {
        return AuditContextHolder.getOrEmpty();
    }

    private Long actorId(AuthenticatedUserContext actor) {
        return actor == null ? null : actor.idUsuarioMs1();
    }
}