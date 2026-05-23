// ruta: src/main/java/com/upsjb/ms4/service/impl/config/SerieBoletaServiceImpl.java
package com.upsjb.ms4.service.impl.config;

import com.upsjb.ms4.domain.entity.config.SerieBoleta;
import com.upsjb.ms4.dto.config.filter.SerieBoletaFilterDto;
import com.upsjb.ms4.dto.config.request.SerieBoletaCreateRequestDto;
import com.upsjb.ms4.dto.config.response.SerieBoletaResponseDto;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.mapper.config.SerieBoletaMapper;
import com.upsjb.ms4.policy.ConfiguracionPolicy;
import com.upsjb.ms4.repository.SerieBoletaRepository;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.service.contract.auditoria.AuditoriaFuncionalService;
import com.upsjb.ms4.service.contract.config.NumeroBoletaReservado;
import com.upsjb.ms4.service.contract.config.SerieBoletaService;
import com.upsjb.ms4.shared.audit.AuditContextHolder;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.shared.exception.ConflictException;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.shared.pagination.PaginationService;
import com.upsjb.ms4.specification.SerieBoletaSpecification;
import com.upsjb.ms4.util.CodigoGenerator;
import com.upsjb.ms4.validator.SerieBoletaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SerieBoletaServiceImpl implements SerieBoletaService {

    private static final Logger log = LoggerFactory.getLogger(SerieBoletaServiceImpl.class);

    private static final String RECURSO = "Serie de boleta";
    private static final String ENTIDAD = "SERIE_BOLETA";

    private final SerieBoletaRepository repository;
    private final SerieBoletaMapper mapper;
    private final SerieBoletaValidator validator;
    private final ConfiguracionPolicy policy;
    private final PaginationService paginationService;
    private final AuditoriaFuncionalService auditoria;

    public SerieBoletaServiceImpl(SerieBoletaRepository repository,
                                  SerieBoletaMapper mapper,
                                  SerieBoletaValidator validator,
                                  ConfiguracionPolicy policy,
                                  PaginationService paginationService,
                                  AuditoriaFuncionalService auditoria) {
        this.repository = repository;
        this.mapper = mapper;
        this.validator = validator;
        this.policy = policy;
        this.paginationService = paginationService;
        this.auditoria = auditoria;
    }

    @Override
    @Transactional
    public SerieBoletaResponseDto crearSerie(SerieBoletaCreateRequestDto request, AuthenticatedUserContext actor) {
        try {
            policy.authorizeGestionSerie(actor);

            String serieNormalizada = request == null || request.serie() == null
                    ? null
                    : request.serie().trim().toUpperCase();

            SerieBoleta existente = serieNormalizada == null
                    ? null
                    : repository.findBySerieIgnoreCase(serieNormalizada).orElse(null);

            validator.validarCrearSerie(request, existente);

            SerieBoleta entity = mapper.toEntity(request);
            entity.setSerie(serieNormalizada);
            entity.setNumeroActual(request.numeroInicio() - 1);
            entity.setCreadoPorIdUsuarioMs1(actor.idUsuarioMs1());
            entity.setEstado(true);

            entity = repository.saveAndFlush(entity);
            registrarExito("CREAR_SERIE_BOLETA", entity, actor, detalle(entity));

            return mapper.toResponse(entity);
        } catch (DataIntegrityViolationException ex) {
            ConflictException conflict = new ConflictException(
                    "No se pudo crear la serie de boleta porque ya existe un dato único duplicado.",
                    ex
            );
            registrarErrorUsuario("CREAR_SERIE_BOLETA", null, actor, conflict);
            throw conflict;
        } catch (BusinessException ex) {
            registrarErrorUsuario("CREAR_SERIE_BOLETA", null, actor, ex);
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico creando serie de boleta. actor={}, requestId={}, correlationId={}",
                    actorId(actor),
                    audit().requestId(),
                    audit().correlationId(),
                    ex
            );
            registrarErrorTecnico("CREAR_SERIE_BOLETA", null, actor, ex);
            throw internalError("No se pudo crear la serie de boleta.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SerieBoletaResponseDto obtenerPorId(Long idSerie) {
        return mapper.toDetailResponse(resolverPorId(idSerie));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<SerieBoletaResponseDto> listar(SerieBoletaFilterDto filter, PageRequestDto page) {
        validator.validarFiltro(filter);

        Page<SerieBoleta> result = repository.findAll(
                SerieBoletaSpecification.build(filter),
                paginationService.toPageable(page, "serie")
        );

        return paginationService.toPageResponse(result, mapper::toResponse);
    }

    @Override
    @Transactional
    public SerieBoletaResponseDto cambiarEstado(Long idSerie,
                                                EstadoChangeRequestDto request,
                                                AuthenticatedUserContext actor) {
        try {
            policy.authorizeGestionSerie(actor);

            SerieBoleta serie = repository.findByIdForUpdate(idSerie)
                    .orElseThrow(() -> NotFoundException.byId(RECURSO, idSerie));

            validator.validarCambioEstado(serie, request);

            if (Boolean.FALSE.equals(request.estado()) && esUnicaSerieDisponible(serie)) {
                throw new ConflictException("No se puede inactivar la única serie de boleta disponible para emisión.");
            }

            if (Boolean.TRUE.equals(request.estado())) {
                serie.activar();
            } else {
                serie.inactivar();
            }

            serie = repository.saveAndFlush(serie);
            registrarExito(
                    "CAMBIAR_ESTADO_SERIE_BOLETA",
                    serie,
                    actor,
                    Map.of("motivo", request.motivo().trim())
            );

            return mapper.toResponse(serie);
        } catch (DataIntegrityViolationException ex) {
            ConflictException conflict = new ConflictException(
                    "No se pudo cambiar el estado de la serie de boleta por conflicto de integridad.",
                    ex
            );
            registrarErrorUsuario("CAMBIAR_ESTADO_SERIE_BOLETA", idSerie, actor, conflict);
            throw conflict;
        } catch (BusinessException ex) {
            registrarErrorUsuario("CAMBIAR_ESTADO_SERIE_BOLETA", idSerie, actor, ex);
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico cambiando estado de serie de boleta. idSerie={}, actor={}, requestId={}, correlationId={}",
                    idSerie,
                    actorId(actor),
                    audit().requestId(),
                    audit().correlationId(),
                    ex
            );
            registrarErrorTecnico("CAMBIAR_ESTADO_SERIE_BOLETA", idSerie, actor, ex);
            throw internalError("No se pudo cambiar el estado de la serie de boleta.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SerieBoleta resolverSerieActivaParaEmision() {
        return repository.findByEstadoTrueOrderBySerieAsc()
                .stream()
                .filter(validator::estaDisponibleParaEmision)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No existe una serie de boleta activa con numeración disponible."));
    }

    @Override
    @Transactional
    public NumeroBoletaReservado reservarSiguienteNumero(Long idSerie, AuthenticatedUserContext actor) {
        if (actor != null) {
            policy.authorizeConsultarConfiguracion(actor);
        }

        SerieBoleta serie = idSerie == null
                ? resolverSerieDisponibleForUpdate()
                : repository.findByIdForUpdate(idSerie)
                .orElseThrow(() -> NotFoundException.byId(RECURSO, idSerie));

        validator.validarSerieDisponibleParaEmision(serie);

        Long siguiente = serie.getNumeroActual() + 1;
        serie.setNumeroActual(siguiente);
        repository.saveAndFlush(serie);

        return new NumeroBoletaReservado(
                serie.getId(),
                serie.getSerie(),
                siguiente,
                CodigoGenerator.boleta(serie.getSerie(), siguiente)
        );
    }

    @Override
    @Transactional
    public void confirmarNumeroUsado(Long idSerie, Long numeroReservado) {
        SerieBoleta serie = repository.findByIdForUpdate(idSerie)
                .orElseThrow(() -> NotFoundException.byId(RECURSO, idSerie));

        validator.validarNumeroReservado(numeroReservado);

        if (serie.getNumeroActual() < numeroReservado) {
            serie.setNumeroActual(numeroReservado);
            repository.saveAndFlush(serie);
        }
    }

    @Override
    @Transactional
    public void liberarNumeroReservadoSiFalla(Long idSerie, Long numeroReservado) {
        if (numeroReservado == null || numeroReservado <= 0) {
            return;
        }

        SerieBoleta serie = repository.findByIdForUpdate(idSerie)
                .orElseThrow(() -> NotFoundException.byId(RECURSO, idSerie));

        if (serie.getNumeroActual().equals(numeroReservado)) {
            Long minimoPermitido = serie.getNumeroInicio() - 1;
            serie.setNumeroActual(Math.max(minimoPermitido, numeroReservado - 1));
            repository.saveAndFlush(serie);
        }
    }

    private SerieBoleta resolverPorId(Long id) {
        if (id == null || id <= 0) {
            throw new NotFoundException(RECURSO + " no encontrada con id: " + id);
        }

        return repository.findById(id)
                .orElseThrow(() -> NotFoundException.byId(RECURSO, id));
    }

    private SerieBoleta resolverSerieDisponibleForUpdate() {
        return repository.findActivasForUpdate()
                .stream()
                .filter(validator::estaDisponibleParaEmision)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No existe una serie de boleta activa con numeración disponible."));
    }

    private boolean esUnicaSerieDisponible(SerieBoleta serieActual) {
        if (!validator.estaDisponibleParaEmision(serieActual)) {
            return false;
        }

        List<SerieBoleta> disponibles = repository.findByEstadoTrueOrderBySerieAsc()
                .stream()
                .filter(validator::estaDisponibleParaEmision)
                .toList();

        return disponibles.size() == 1 && disponibles.get(0).getId().equals(serieActual.getId());
    }

    private Map<String, Object> detalle(SerieBoleta entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("serie", entity.getSerie());
        map.put("numeroActual", entity.getNumeroActual());
        map.put("numeroInicio", entity.getNumeroInicio());
        map.put("numeroFin", entity.getNumeroFin());
        return map;
    }

    private void registrarExito(String accion, SerieBoleta entity, AuthenticatedUserContext actor, Object detalle) {
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