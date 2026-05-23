// ruta: src/main/java/com/upsjb/ms4/service/impl/config/AssetCloudinaryServiceImpl.java
package com.upsjb.ms4.service.impl.config;

import com.upsjb.ms4.domain.entity.config.AssetCloudinary;
import com.upsjb.ms4.dto.config.filter.AssetCloudinaryFilterDto;
import com.upsjb.ms4.dto.config.request.AssetCloudinaryUploadRequestDto;
import com.upsjb.ms4.dto.config.response.AssetCloudinaryResponseDto;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.integration.cloudinary.CloudinaryAssetClient;
import com.upsjb.ms4.mapper.config.AssetCloudinaryMapper;
import com.upsjb.ms4.policy.ConfiguracionPolicy;
import com.upsjb.ms4.repository.AssetCloudinaryRepository;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.service.contract.auditoria.AuditoriaFuncionalService;
import com.upsjb.ms4.service.contract.config.AssetCloudinaryService;
import com.upsjb.ms4.shared.audit.AuditContextHolder;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.shared.exception.ConflictException;
import com.upsjb.ms4.shared.exception.ExternalServiceException;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.shared.pagination.PaginationService;
import com.upsjb.ms4.specification.AssetCloudinarySpecification;
import com.upsjb.ms4.util.HashUtil;
import com.upsjb.ms4.validator.AssetCloudinaryValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class AssetCloudinaryServiceImpl implements AssetCloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(AssetCloudinaryServiceImpl.class);

    private static final String RECURSO = "Asset Cloudinary";
    private static final String ENTIDAD = "ASSET_CLOUDINARY";

    private final AssetCloudinaryRepository repository;
    private final AssetCloudinaryMapper mapper;
    private final AssetCloudinaryValidator validator;
    private final ConfiguracionPolicy policy;
    private final CloudinaryAssetClient cloudinaryAssetClient;
    private final PaginationService paginationService;
    private final AuditoriaFuncionalService auditoria;

    public AssetCloudinaryServiceImpl(AssetCloudinaryRepository repository,
                                      AssetCloudinaryMapper mapper,
                                      AssetCloudinaryValidator validator,
                                      ConfiguracionPolicy policy,
                                      CloudinaryAssetClient cloudinaryAssetClient,
                                      PaginationService paginationService,
                                      AuditoriaFuncionalService auditoria) {
        this.repository = repository;
        this.mapper = mapper;
        this.validator = validator;
        this.policy = policy;
        this.cloudinaryAssetClient = cloudinaryAssetClient;
        this.paginationService = paginationService;
        this.auditoria = auditoria;
    }

    @Override
    @Transactional
    public AssetCloudinaryResponseDto subirAssetVisual(AssetCloudinaryUploadRequestDto request,
                                                       MultipartFile file,
                                                       AuthenticatedUserContext actor) {
        CloudinaryAssetClient.CloudinaryUploadResult uploadResult = null;

        try {
            policy.authorizeGestionAssetVisual(actor);
            validator.validarUpload(request, file);

            String hash = calcularHash(file);
            validarHashDisponibleParaCrear(hash);

            uploadResult = cloudinaryAssetClient.uploadVisualAsset(
                    file,
                    normalizarFolder(request.folder()),
                    null,
                    false
            );

            AssetCloudinary entity = construirEntity(request, file, uploadResult, actor, hash);
            entity = repository.saveAndFlush(entity);

            registrarExito("SUBIR_ASSET_VISUAL", entity, actor, detalle(entity));
            return mapper.toResponse(entity);
        } catch (ExternalServiceException ex) {
            log.error(
                    "Error externo subiendo asset visual a Cloudinary. actor={}, requestId={}, correlationId={}",
                    actorId(actor),
                    audit().requestId(),
                    audit().correlationId(),
                    ex
            );
            registrarErrorTecnico("SUBIR_ASSET_VISUAL", null, actor, ex);
            throw ex;
        } catch (DataIntegrityViolationException ex) {
            eliminarAssetCloudinarySilencioso(uploadResult, "SUBIR_ASSET_VISUAL");
            ConflictException conflict = new ConflictException(
                    "No se pudo registrar el asset visual porque ya existe un dato único duplicado.",
                    ex
            );
            registrarErrorUsuario("SUBIR_ASSET_VISUAL", null, actor, conflict);
            throw conflict;
        } catch (BusinessException ex) {
            eliminarAssetCloudinarySilencioso(uploadResult, "SUBIR_ASSET_VISUAL");
            registrarErrorUsuario("SUBIR_ASSET_VISUAL", null, actor, ex);
            throw ex;
        } catch (RuntimeException ex) {
            eliminarAssetCloudinarySilencioso(uploadResult, "SUBIR_ASSET_VISUAL");
            log.error(
                    "Error técnico subiendo asset visual. actor={}, requestId={}, correlationId={}",
                    actorId(actor),
                    audit().requestId(),
                    audit().correlationId(),
                    ex
            );
            registrarErrorTecnico("SUBIR_ASSET_VISUAL", null, actor, ex);
            throw internalError("No se pudo subir el asset visual.", ex);
        }
    }

    @Override
    @Transactional
    public AssetCloudinaryResponseDto reemplazarAssetVisual(Long idAssetActual,
                                                            AssetCloudinaryUploadRequestDto request,
                                                            MultipartFile file,
                                                            AuthenticatedUserContext actor) {
        CloudinaryAssetClient.CloudinaryUploadResult uploadResult = null;

        try {
            policy.authorizeGestionAssetVisual(actor);
            validator.validarUpload(request, file);

            AssetCloudinary actual = resolverActivo(idAssetActual);
            String hash = calcularHash(file);
            validarHashDisponibleParaReemplazo(hash, actual.getId());

            uploadResult = cloudinaryAssetClient.uploadVisualAsset(
                    file,
                    normalizarFolder(request.folder()),
                    null,
                    false
            );

            actual.inactivar();
            repository.save(actual);

            AssetCloudinary nuevo = construirEntity(request, file, uploadResult, actor, hash);
            nuevo = repository.saveAndFlush(nuevo);

            registrarExito(
                    "REEMPLAZAR_ASSET_VISUAL",
                    nuevo,
                    actor,
                    Map.of("idAssetAnterior", actual.getId())
            );

            return mapper.toResponse(nuevo);
        } catch (ExternalServiceException ex) {
            log.error(
                    "Error externo reemplazando asset visual en Cloudinary. idAssetActual={}, actor={}, requestId={}, correlationId={}",
                    idAssetActual,
                    actorId(actor),
                    audit().requestId(),
                    audit().correlationId(),
                    ex
            );
            registrarErrorTecnico("REEMPLAZAR_ASSET_VISUAL", idAssetActual, actor, ex);
            throw ex;
        } catch (DataIntegrityViolationException ex) {
            eliminarAssetCloudinarySilencioso(uploadResult, "REEMPLAZAR_ASSET_VISUAL");
            ConflictException conflict = new ConflictException(
                    "No se pudo registrar el nuevo asset visual porque ya existe un dato único duplicado.",
                    ex
            );
            registrarErrorUsuario("REEMPLAZAR_ASSET_VISUAL", idAssetActual, actor, conflict);
            throw conflict;
        } catch (BusinessException ex) {
            eliminarAssetCloudinarySilencioso(uploadResult, "REEMPLAZAR_ASSET_VISUAL");
            registrarErrorUsuario("REEMPLAZAR_ASSET_VISUAL", idAssetActual, actor, ex);
            throw ex;
        } catch (RuntimeException ex) {
            eliminarAssetCloudinarySilencioso(uploadResult, "REEMPLAZAR_ASSET_VISUAL");
            log.error(
                    "Error técnico reemplazando asset visual. idAssetActual={}, actor={}, requestId={}, correlationId={}",
                    idAssetActual,
                    actorId(actor),
                    audit().requestId(),
                    audit().correlationId(),
                    ex
            );
            registrarErrorTecnico("REEMPLAZAR_ASSET_VISUAL", idAssetActual, actor, ex);
            throw internalError("No se pudo reemplazar el asset visual.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AssetCloudinaryResponseDto obtenerPorId(Long idAsset) {
        return mapper.toDetailResponse(resolverPorId(idAsset));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<AssetCloudinaryResponseDto> listar(AssetCloudinaryFilterDto filter, PageRequestDto page) {
        validator.validarFiltro(filter);

        Page<AssetCloudinary> result = repository.findAll(
                AssetCloudinarySpecification.build(filter),
                paginationService.toPageable(page, "createdAt")
        );

        return paginationService.toPageResponse(result, mapper::toResponse);
    }

    @Override
    @Transactional
    public AssetCloudinaryResponseDto cambiarEstado(Long idAsset,
                                                    EstadoChangeRequestDto request,
                                                    AuthenticatedUserContext actor) {
        try {
            policy.authorizeGestionAssetVisual(actor);

            AssetCloudinary asset = resolverPorId(idAsset);
            validator.validarCambioEstado(asset, request);

            if (Boolean.TRUE.equals(request.estado())) {
                asset.activar();
            } else {
                asset.inactivar();
            }

            asset = repository.saveAndFlush(asset);
            registrarExito(
                    "CAMBIAR_ESTADO_ASSET_VISUAL",
                    asset,
                    actor,
                    Map.of("motivo", request.motivo().trim())
            );

            return mapper.toResponse(asset);
        } catch (DataIntegrityViolationException ex) {
            ConflictException conflict = new ConflictException(
                    "No se pudo cambiar el estado del asset visual por conflicto de integridad.",
                    ex
            );
            registrarErrorUsuario("CAMBIAR_ESTADO_ASSET_VISUAL", idAsset, actor, conflict);
            throw conflict;
        } catch (BusinessException ex) {
            registrarErrorUsuario("CAMBIAR_ESTADO_ASSET_VISUAL", idAsset, actor, ex);
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico cambiando estado de asset visual. idAsset={}, actor={}, requestId={}, correlationId={}",
                    idAsset,
                    actorId(actor),
                    audit().requestId(),
                    audit().correlationId(),
                    ex
            );
            registrarErrorTecnico("CAMBIAR_ESTADO_ASSET_VISUAL", idAsset, actor, ex);
            throw internalError("No se pudo cambiar el estado del asset visual.", ex);
        }
    }

    @Override
    public void validarAssetVisualPermitido(MultipartFile file, String tipoAsset) {
        validator.validarAssetVisualPermitido(file, tipoAsset);
    }

    private AssetCloudinary construirEntity(AssetCloudinaryUploadRequestDto request,
                                            MultipartFile file,
                                            CloudinaryAssetClient.CloudinaryUploadResult result,
                                            AuthenticatedUserContext actor,
                                            String hash) {
        AuditContextHolder.AuditContext audit = audit();

        AssetCloudinary entity = mapper.toEntity(
                request,
                normalizarNombreArchivo(file.getOriginalFilename()),
                resolverExtension(file.getOriginalFilename()),
                normalizarContentType(file.getContentType()),
                result.publicId(),
                result.secureUrl(),
                result.url(),
                result.versionCloudinary(),
                result.bytes(),
                hash
        );

        entity.setEntidadOrigen(normalizarCodigo(request.entidadOrigen()));
        entity.setTipoAsset(normalizarCodigo(request.tipoAsset()));
        entity.setFolder(result.folder());
        entity.setResourceType(result.resourceType());
        entity.setHashSha256(hash);
        entity.setSubidoPorIdUsuarioMs1(actor.idUsuarioMs1());
        entity.setRequestId(audit.requestId());
        entity.setCorrelationId(audit.correlationId());
        entity.setEstado(true);

        return entity;
    }

    private AssetCloudinary resolverPorId(Long idAsset) {
        if (idAsset == null || idAsset <= 0) {
            throw new NotFoundException(RECURSO + " no encontrado con id: " + idAsset);
        }

        return repository.findById(idAsset)
                .orElseThrow(() -> NotFoundException.byId(RECURSO, idAsset));
    }

    private AssetCloudinary resolverActivo(Long idAsset) {
        AssetCloudinary asset = resolverPorId(idAsset);
        if (!asset.isActivo()) {
            throw new NotFoundException("El asset visual no se encuentra activo.");
        }
        return asset;
    }

    private void validarHashDisponibleParaCrear(String hash) {
        if (repository.existsByHashSha256AndEstadoTrue(hash)) {
            throw new ConflictException("Ya existe un asset visual activo con el mismo contenido.");
        }
    }

    private void validarHashDisponibleParaReemplazo(String hash, Long idAssetActual) {
        if (repository.existsByHashSha256AndEstadoTrueAndIdNot(hash, idAssetActual)) {
            throw new ConflictException("Ya existe otro asset visual activo con el mismo contenido.");
        }
    }

    private String calcularHash(MultipartFile file) {
        try {
            return HashUtil.sha256(file.getBytes());
        } catch (IOException ex) {
            throw new BusinessException(
                    ErrorCodes.INVALID_REQUEST,
                    "No se pudo leer el archivo para calcular su hash.",
                    HttpStatus.BAD_REQUEST,
                    ex
            );
        }
    }

    private void eliminarAssetCloudinarySilencioso(CloudinaryAssetClient.CloudinaryUploadResult result, String accion) {
        if (result == null || result.publicId() == null || result.publicId().isBlank()) {
            return;
        }

        try {
            cloudinaryAssetClient.deleteAsset(result.publicId(), result.resourceType());
        } catch (RuntimeException cleanupEx) {
            log.warn(
                    "No se pudo limpiar asset Cloudinary tras fallo transaccional. accion={}, publicId={}",
                    accion,
                    result.publicId(),
                    cleanupEx
            );
        }
    }

    private String normalizarNombreArchivo(String filename) {
        return filename == null || filename.isBlank() ? "asset-visual" : filename.trim();
    }

    private String resolverExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }

        String normalized = filename.trim();
        int index = normalized.lastIndexOf('.');
        return index < 0 ? "" : normalized.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String normalizarContentType(String contentType) {
        return contentType == null || contentType.isBlank()
                ? "application/octet-stream"
                : contentType.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizarCodigo(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizarFolder(String value) {
        return value == null ? null : value.trim();
    }

    private Map<String, Object> detalle(AssetCloudinary asset) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("publicId", asset.getPublicId());
        map.put("entidadOrigen", asset.getEntidadOrigen());
        map.put("tipoAsset", asset.getTipoAsset());
        map.put("secureUrl", asset.getSecureUrl());
        return map;
    }

    private void registrarExito(String accion, AssetCloudinary entity, AuthenticatedUserContext actor, Object detalle) {
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