// ruta: src/main/java/com/upsjb/ms4/service/impl/kafka/InventarioEventoPendienteServiceImpl.java
package com.upsjb.ms4.service.impl.kafka;

import com.upsjb.ms4.config.KafkaTopicProperties;
import com.upsjb.ms4.domain.entity.kafka.InventarioEventoPendienteMs4;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.entity.venta.VentaDetalle;
import com.upsjb.ms4.domain.enums.EstadoSincronizacionInventario;
import com.upsjb.ms4.domain.enums.TipoComandoStock;
import com.upsjb.ms4.dto.contingencia.filter.InventarioEventoPendienteFilterDto;
import com.upsjb.ms4.dto.contingencia.request.StockSyncResultRequestDto;
import com.upsjb.ms4.dto.contingencia.response.ContingenciaReconciliacionResponseDto;
import com.upsjb.ms4.dto.contingencia.response.InventarioEventoPendienteResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.mapper.kafka.InventarioEventoPendienteMapper;
import com.upsjb.ms4.policy.ContingenciaPolicy;
import com.upsjb.ms4.repository.InventarioEventoPendienteMs4Repository;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.auditoria.AuditoriaFuncionalService;
import com.upsjb.ms4.service.contract.kafka.InventarioEventoPendienteService;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.shared.exception.ConflictException;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.shared.exception.ValidationException;
import com.upsjb.ms4.shared.pagination.PaginationService;
import com.upsjb.ms4.specification.InventarioEventoPendienteSpecification;
import com.upsjb.ms4.validator.InventarioEventoPendienteValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventarioEventoPendienteServiceImpl implements InventarioEventoPendienteService {

    private static final Logger log = LoggerFactory.getLogger(InventarioEventoPendienteServiceImpl.class);

    private static final String RECURSO_EVENTO_PENDIENTE = "Evento pendiente de inventario";
    private static final String ENTIDAD_EVENTO_PENDIENTE = "INVENTARIO_EVENTO_PENDIENTE_MS4";
    private static final int MAX_RECONCILIACION_BATCH = 500;

    private final InventarioEventoPendienteMs4Repository repository;
    private final InventarioEventoPendienteMapper mapper;
    private final InventarioEventoPendienteValidator validator;
    private final ContingenciaPolicy contingenciaPolicy;
    private final PaginationService paginationService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final KafkaTopicProperties kafkaTopicProperties;
    private final Clock clock;

    public InventarioEventoPendienteServiceImpl(InventarioEventoPendienteMs4Repository repository,
                                                InventarioEventoPendienteMapper mapper,
                                                InventarioEventoPendienteValidator validator,
                                                ContingenciaPolicy contingenciaPolicy,
                                                PaginationService paginationService,
                                                AuthenticatedUserResolver authenticatedUserResolver,
                                                AuditoriaFuncionalService auditoriaFuncionalService,
                                                KafkaTopicProperties kafkaTopicProperties,
                                                Clock clock) {
        this.repository = repository;
        this.mapper = mapper;
        this.validator = validator;
        this.contingenciaPolicy = contingenciaPolicy;
        this.paginationService = paginationService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.auditoriaFuncionalService = auditoriaFuncionalService;
        this.kafkaTopicProperties = kafkaTopicProperties;
        this.clock = clock;
    }

    @Override
    @Transactional
    public InventarioEventoPendienteResponseDto registrarPendiente(Venta venta,
                                                                   VentaDetalle detalle,
                                                                   TipoComandoStock tipoComando,
                                                                   String payloadJson) {
        validator.validarRegistroPendiente(venta, detalle, tipoComando, payloadJson);

        String idempotencyKey = buildIdempotencyKey(venta, detalle, tipoComando);

        InventarioEventoPendienteMs4 existente = repository.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existente != null) {
            if (Boolean.TRUE.equals(existente.getEstado())) {
                return mapper.toResponse(existente);
            }

            throw new ConflictException("Ya existe un evento pendiente inactivo con la misma clave de idempotencia.");
        }

        LocalDateTime now = LocalDateTime.now(clock);

        InventarioEventoPendienteMs4 entity = InventarioEventoPendienteMs4.builder()
                .idVenta(venta.getId())
                .codigoVenta(venta.getCodigoVenta().trim())
                .idVentaDetalle(detalle.getId())
                .tipoEvento(tipoComando)
                .topicDestino(resolveTopicDestino(tipoComando))
                .payloadJson(payloadJson.trim())
                .estadoSincronizacion(EstadoSincronizacionInventario.PENDIENTE)
                .idempotencyKey(idempotencyKey)
                .cantidadReintentos(0)
                .fechaCreacion(now)
                .requestId(trimToNull(venta.getRequestId()))
                .correlationId(trimToNull(venta.getCorrelationId()))
                .estado(true)
                .build();

        entity = repository.save(entity);

        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<InventarioEventoPendienteResponseDto> listar(InventarioEventoPendienteFilterDto filter,
                                                                        PageRequestDto page) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        contingenciaPolicy.authorizeConsultarEventosPendientes(actor);
        validator.validarFiltro(filter);

        Page<InventarioEventoPendienteMs4> result = repository.findAll(
                InventarioEventoPendienteSpecification.build(filter),
                paginationService.toPageable(page, "fechaCreacion")
        );

        return paginationService.toPageResponse(result, mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<InventarioEventoPendienteResponseDto> listarPendientesParaMs3(InventarioEventoPendienteFilterDto filter,
                                                                                         PageRequestDto page) {
        validator.validarFiltro(filter);

        Specification<InventarioEventoPendienteMs4> specification = Specification
                .where(InventarioEventoPendienteSpecification.build(filter))
                .and((root, query, cb) -> cb.and(
                        cb.isTrue(root.get("estado")),
                        root.get("estadoSincronizacion").in(estadosReintentables())
                ));

        Page<InventarioEventoPendienteMs4> result = repository.findAll(
                specification,
                paginationService.toPageable(page, "fechaCreacion")
        );

        return paginationService.toPageResponse(result, mapper::toResponse);
    }

    @Override
    @Transactional
    public InventarioEventoPendienteResponseDto marcarSincronizado(Long idPendiente, String detalleResultado) {
        InventarioEventoPendienteMs4 evento = resolverActivo(idPendiente);
        validator.validarMarcarSincronizado(evento);

        evento.setEstadoSincronizacion(EstadoSincronizacionInventario.SINCRONIZADO);
        evento.setFechaSincronizacion(LocalDateTime.now(clock));
        evento.setUltimoError(null);

        evento = repository.save(evento);

        auditoriaFuncionalService.registrarExito(
                ENTIDAD_EVENTO_PENDIENTE,
                evento.getId(),
                "MARCAR_EVENTO_INVENTARIO_SINCRONIZADO",
                null,
                detalleAuditoria(evento, detalleResultado)
        );

        return mapper.toResponse(evento);
    }

    @Override
    @Transactional
    public InventarioEventoPendienteResponseDto marcarError(Long idPendiente, String errorDetalle) {
        InventarioEventoPendienteMs4 evento = resolverActivo(idPendiente);
        validator.validarMarcarError(evento, errorDetalle);

        evento.setEstadoSincronizacion(EstadoSincronizacionInventario.ERROR);
        evento.setUltimoError(truncate(errorDetalle, 4000));
        evento.setFechaUltimoReintento(LocalDateTime.now(clock));

        evento = repository.save(evento);

        auditoriaFuncionalService.registrarExito(
                ENTIDAD_EVENTO_PENDIENTE,
                evento.getId(),
                "MARCAR_EVENTO_INVENTARIO_ERROR",
                null,
                detalleAuditoria(evento, errorDetalle)
        );

        return mapper.toResponse(evento);
    }

    @Override
    @Transactional
    public InventarioEventoPendienteResponseDto registrarResultadoSincronizacion(StockSyncResultRequestDto request) {
        validarResultadoSincronizacion(request);

        if (Boolean.TRUE.equals(request.sincronizado())) {
            return marcarSincronizado(request.idEventoPendiente(), request.detalleResultado());
        }

        return marcarError(
                request.idEventoPendiente(),
                firstNonBlank(request.errorDetalle(), request.detalleResultado())
        );
    }

    @Override
    @Transactional
    public InventarioEventoPendienteResponseDto reintentar(Long idPendiente, AuthenticatedUserContext actor) {
        try {
            contingenciaPolicy.authorizeReconciliar(actor);

            InventarioEventoPendienteMs4 evento = resolverActivo(idPendiente);
            validator.validarReintento(evento);

            evento.setEstadoSincronizacion(EstadoSincronizacionInventario.PENDIENTE);
            evento.setCantidadReintentos(nextRetry(evento.getCantidadReintentos()));
            evento.setFechaUltimoReintento(LocalDateTime.now(clock));
            evento.setUltimoError(null);

            evento = repository.save(evento);

            auditoriaFuncionalService.registrarExito(
                    ENTIDAD_EVENTO_PENDIENTE,
                    evento.getId(),
                    "REINTENTAR_EVENTO_INVENTARIO",
                    actor,
                    detalleAuditoria(evento, null)
            );

            return mapper.toResponse(evento);
        } catch (BusinessException ex) {
            auditoriaFuncionalService.registrarErrorUsuario(
                    ENTIDAD_EVENTO_PENDIENTE,
                    idPendiente,
                    "REINTENTAR_EVENTO_INVENTARIO",
                    actor,
                    ex.getCode(),
                    ex.getMessage()
            );
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Error técnico reintentando evento de inventario. idPendiente={}, actorIdUsuarioMs1={}",
                    idPendiente,
                    actor == null ? null : actor.idUsuarioMs1(),
                    ex);
            auditoriaFuncionalService.registrarErrorTecnico(
                    ENTIDAD_EVENTO_PENDIENTE,
                    idPendiente,
                    "REINTENTAR_EVENTO_INVENTARIO",
                    actor,
                    ex
            );
            throw internalError("No se pudo reintentar el evento pendiente de inventario.", ex);
        }
    }

    @Override
    @Transactional
    public ContingenciaReconciliacionResponseDto reconciliarPendientes(AuthenticatedUserContext actor) {
        try {
            contingenciaPolicy.authorizeReconciliar(actor);

            long totalReintentables = contarPendientesActivos();

            List<InventarioEventoPendienteMs4> eventos = repository.findPendientesForUpdate(
                    estadosReintentables(),
                    PageRequest.of(0, MAX_RECONCILIACION_BATCH)
            );

            LocalDateTime now = LocalDateTime.now(clock);

            for (InventarioEventoPendienteMs4 evento : eventos) {
                validator.validarReintento(evento);
                evento.setEstadoSincronizacion(EstadoSincronizacionInventario.PENDIENTE);
                evento.setCantidadReintentos(nextRetry(evento.getCantidadReintentos()));
                evento.setFechaUltimoReintento(now);
                evento.setUltimoError(null);
            }

            repository.saveAll(eventos);

            long totalPendientesPosteriores = contarPendientesActivos();

            auditoriaFuncionalService.registrarExito(
                    ENTIDAD_EVENTO_PENDIENTE,
                    null,
                    "RECONCILIAR_EVENTOS_INVENTARIO",
                    actor,
                    Map.of(
                            "totalReintentables", totalReintentables,
                            "totalReprogramados", eventos.size(),
                            "totalPendientesPosteriores", totalPendientesPosteriores
                    )
            );

            return new ContingenciaReconciliacionResponseDto(
                    totalReintentables,
                    eventos.size(),
                    totalPendientesPosteriores,
                    "Eventos pendientes de inventario reprogramados para reconciliación."
            );
        } catch (BusinessException ex) {
            auditoriaFuncionalService.registrarErrorUsuario(
                    ENTIDAD_EVENTO_PENDIENTE,
                    null,
                    "RECONCILIAR_EVENTOS_INVENTARIO",
                    actor,
                    ex.getCode(),
                    ex.getMessage()
            );
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Error técnico reconciliando eventos pendientes de inventario. actorIdUsuarioMs1={}",
                    actor == null ? null : actor.idUsuarioMs1(),
                    ex);
            auditoriaFuncionalService.registrarErrorTecnico(
                    ENTIDAD_EVENTO_PENDIENTE,
                    null,
                    "RECONCILIAR_EVENTOS_INVENTARIO",
                    actor,
                    ex
            );
            throw internalError("No se pudo reconciliar los eventos pendientes de inventario.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPendientesActivos() {
        return repository.countByEstadoSincronizacionInAndEstadoTrue(estadosReintentables());
    }

    private void validarResultadoSincronizacion(StockSyncResultRequestDto request) {
        if (request == null) {
            throw new ValidationException("El resultado de sincronización es obligatorio.");
        }

        if (request.idEventoPendiente() == null || request.idEventoPendiente() <= 0) {
            throw ValidationException.field(
                    "idEventoPendiente",
                    "El id del evento pendiente debe ser positivo."
            );
        }

        if (request.sincronizado() == null) {
            throw ValidationException.field(
                    "sincronizado",
                    "Debe indicar si el evento fue sincronizado."
            );
        }

        if (Boolean.FALSE.equals(request.sincronizado())
                && firstNonBlank(request.errorDetalle(), request.detalleResultado()) == null) {
            throw ValidationException.field(
                    "errorDetalle",
                    "El detalle de error es obligatorio cuando la sincronización no fue exitosa."
            );
        }
    }

    private InventarioEventoPendienteMs4 resolverActivo(Long idPendiente) {
        if (idPendiente == null || idPendiente <= 0) {
            throw NotFoundException.byId(RECURSO_EVENTO_PENDIENTE, idPendiente);
        }

        return repository.findById(idPendiente)
                .filter(InventarioEventoPendienteMs4::isActivo)
                .orElseThrow(() -> NotFoundException.byId(RECURSO_EVENTO_PENDIENTE, idPendiente));
    }

    private List<EstadoSincronizacionInventario> estadosReintentables() {
        return List.of(
                EstadoSincronizacionInventario.PENDIENTE,
                EstadoSincronizacionInventario.ERROR,
                EstadoSincronizacionInventario.REQUIERE_REVISION
        );
    }

    private String resolveTopicDestino(TipoComandoStock tipoComando) {
        if (tipoComando == TipoComandoStock.RECONCILIAR_STOCK) {
            return kafkaTopicProperties.stockReconciliationTopic();
        }

        return kafkaTopicProperties.stockCommandTopic();
    }

    private String buildIdempotencyKey(Venta venta, VentaDetalle detalle, TipoComandoStock tipoComando) {
        return "MS4-VENTA-"
                + venta.getId()
                + "-SKU-"
                + detalle.getIdSkuMs3()
                + "-"
                + idempotencySuffix(tipoComando);
    }

    private String idempotencySuffix(TipoComandoStock tipoComando) {
        return switch (tipoComando) {
            case RESERVAR_STOCK -> "RESERVAR";
            case CONFIRMAR_VENTA -> "CONFIRMAR";
            case LIBERAR_RESERVA -> "LIBERAR";
            case ANULAR_VENTA -> "ANULAR";
            case RECONCILIAR_STOCK -> "RECONCILIAR";
        };
    }

    private Integer nextRetry(Integer current) {
        return current == null ? 1 : current + 1;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String truncate(String value, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();
        return normalized.length() <= max ? normalized : normalized.substring(0, max);
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.trim();
        }

        if (second != null && !second.isBlank()) {
            return second.trim();
        }

        return null;
    }

    private Map<String, Object> detalleAuditoria(InventarioEventoPendienteMs4 evento, String detalleResultado) {
        Map<String, Object> detalle = new LinkedHashMap<>();
        detalle.put("idVenta", evento.getIdVenta());
        detalle.put("codigoVenta", evento.getCodigoVenta());
        detalle.put("idVentaDetalle", evento.getIdVentaDetalle());
        detalle.put("tipoEvento", evento.getTipoEvento());
        detalle.put("estadoSincronizacion", evento.getEstadoSincronizacion());
        detalle.put("idempotencyKey", evento.getIdempotencyKey());

        if (detalleResultado != null && !detalleResultado.isBlank()) {
            detalle.put("detalle", detalleResultado.trim());
        }

        return detalle;
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