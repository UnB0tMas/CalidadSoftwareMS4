// ruta: src/main/java/com/upsjb/ms4/service/impl/contingencia/ContingenciaServiceImpl.java
package com.upsjb.ms4.service.impl.contingencia;

import com.upsjb.ms4.domain.entity.contingencia.ModoContingencia;
import com.upsjb.ms4.domain.entity.kafka.InventarioEventoPendienteMs4;
import com.upsjb.ms4.domain.enums.EstadoContingencia;
import com.upsjb.ms4.domain.enums.EstadoSincronizacionInventario;
import com.upsjb.ms4.domain.enums.TipoCorreo;
import com.upsjb.ms4.dto.contingencia.filter.InventarioEventoPendienteFilterDto;
import com.upsjb.ms4.dto.contingencia.filter.ModoContingenciaFilterDto;
import com.upsjb.ms4.dto.contingencia.request.ContingenciaActivarRequestDto;
import com.upsjb.ms4.dto.contingencia.request.ContingenciaFinalizarRequestDto;
import com.upsjb.ms4.dto.contingencia.response.ContingenciaReconciliacionResponseDto;
import com.upsjb.ms4.dto.contingencia.response.InventarioEventoPendienteResponseDto;
import com.upsjb.ms4.dto.contingencia.response.ModoContingenciaResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.mapper.contingencia.ContingenciaMapper;
import com.upsjb.ms4.policy.ContingenciaPolicy;
import com.upsjb.ms4.repository.InventarioEventoPendienteMs4Repository;
import com.upsjb.ms4.repository.ModoContingenciaRepository;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.service.contract.auditoria.AuditoriaFuncionalService;
import com.upsjb.ms4.service.contract.contingencia.ContingenciaService;
import com.upsjb.ms4.service.contract.mail.CorreoOutboxService;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.shared.exception.ConflictException;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.shared.pagination.PaginationService;
import com.upsjb.ms4.specification.InventarioEventoPendienteSpecification;
import com.upsjb.ms4.specification.ModoContingenciaSpecification;
import com.upsjb.ms4.validator.ContingenciaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ContingenciaServiceImpl implements ContingenciaService {

    private static final Logger log = LoggerFactory.getLogger(ContingenciaServiceImpl.class);

    private static final String ENTIDAD_CONTINGENCIA = "MODO_CONTINGENCIA";
    private static final String ENTIDAD_EVENTO_PENDIENTE = "INVENTARIO_EVENTO_PENDIENTE_MS4";
    private static final String RECURSO_EVENTO_PENDIENTE = "Evento pendiente de inventario";
    private static final String SERVICIO_MS3 = "MS3";
    private static final int MAX_RECONCILIACION_BATCH = 500;
    private static final int MAX_ERROR_LENGTH = 2000;

    private final ModoContingenciaRepository modoContingenciaRepository;
    private final InventarioEventoPendienteMs4Repository inventarioEventoPendienteRepository;
    private final ContingenciaMapper contingenciaMapper;
    private final ContingenciaValidator contingenciaValidator;
    private final ContingenciaPolicy contingenciaPolicy;
    private final PaginationService paginationService;
    private final CorreoOutboxService correoOutboxService;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final Clock clock;

    public ContingenciaServiceImpl(ModoContingenciaRepository modoContingenciaRepository,
                                   InventarioEventoPendienteMs4Repository inventarioEventoPendienteRepository,
                                   ContingenciaMapper contingenciaMapper,
                                   ContingenciaValidator contingenciaValidator,
                                   ContingenciaPolicy contingenciaPolicy,
                                   PaginationService paginationService,
                                   CorreoOutboxService correoOutboxService,
                                   AuditoriaFuncionalService auditoriaFuncionalService,
                                   Clock clock) {
        this.modoContingenciaRepository = modoContingenciaRepository;
        this.inventarioEventoPendienteRepository = inventarioEventoPendienteRepository;
        this.contingenciaMapper = contingenciaMapper;
        this.contingenciaValidator = contingenciaValidator;
        this.contingenciaPolicy = contingenciaPolicy;
        this.paginationService = paginationService;
        this.correoOutboxService = correoOutboxService;
        this.auditoriaFuncionalService = auditoriaFuncionalService;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ModoContingenciaResponseDto activarContingencia(ContingenciaActivarRequestDto request,
                                                           AuthenticatedUserContext actor) {
        try {
            contingenciaPolicy.authorizeActivarContingencia(actor);

            String servicioAfectado = normalizarServicio(request == null ? null : request.servicioAfectado());
            ModoContingencia activa = buscarContingenciaActivaPorServicio(servicioAfectado);

            contingenciaValidator.validarActivacion(request, activa);

            long eventosPendientes = contarEventosReintentables();
            LocalDateTime now = LocalDateTime.now(clock);

            ModoContingencia entity = ModoContingencia.builder()
                    .servicioAfectado(servicioAfectado)
                    .estadoContingencia(EstadoContingencia.ACTIVO)
                    .fechaInicio(now)
                    .activadoPorIdUsuarioMs1(actor.idUsuarioMs1())
                    .activadoPorRol(actor.rol())
                    .motivo(request.motivo().trim())
                    .ventasPermitidas(request.ventasPermitidas())
                    .guardarEventosPendientes(request.guardarEventosPendientes())
                    .totalEventosPendientes(Math.toIntExact(Math.min(eventosPendientes, Integer.MAX_VALUE)))
                    .observacion(trimToNull(request.observacion()))
                    .estado(true)
                    .build();

            entity = modoContingenciaRepository.save(entity);

            programarAlerta(
                    TipoCorreo.ALERTA_CONTINGENCIA_ACTIVADA,
                    "Contingencia activada en MS4",
                    "Se activó contingencia para " + entity.getServicioAfectado() + ". Motivo: " + entity.getMotivo(),
                    actor
            );

            registrarExito(ENTIDAD_CONTINGENCIA, entity.getId(), "ACTIVAR_CONTINGENCIA", actor, detalleContingencia(entity));
            return contingenciaMapper.toModoResponse(entity);
        } catch (BusinessException ex) {
            registrarErrorUsuario(ENTIDAD_CONTINGENCIA, null, "ACTIVAR_CONTINGENCIA", actor, ex);
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico activando contingencia. actorIdUsuarioMs1={}, servicio={}",
                    actor == null ? null : actor.idUsuarioMs1(),
                    request == null ? null : request.servicioAfectado(),
                    ex
            );
            registrarErrorTecnico(ENTIDAD_CONTINGENCIA, null, "ACTIVAR_CONTINGENCIA", actor, ex);
            throw internalError("No se pudo activar la contingencia.", ex);
        }
    }

    @Override
    @Transactional
    public ModoContingenciaResponseDto finalizarContingencia(ContingenciaFinalizarRequestDto request,
                                                             AuthenticatedUserContext actor) {
        try {
            contingenciaPolicy.authorizeFinalizarContingencia(actor);

            ModoContingencia actual = buscarContingenciaActivaPorServicio(SERVICIO_MS3);
            contingenciaValidator.validarFinalizacion(request, actual);

            actual.setEstadoContingencia(EstadoContingencia.FINALIZADO);
            actual.setFechaFin(LocalDateTime.now(clock));
            actual.setTotalEventosPendientes(Math.toIntExact(Math.min(contarEventosReintentables(), Integer.MAX_VALUE)));
            actual.setObservacion(unirObservacion(actual.getObservacion(), request.motivo(), request.observacion()));

            actual = modoContingenciaRepository.save(actual);

            programarAlerta(
                    TipoCorreo.ALERTA_CONTINGENCIA_FINALIZADA,
                    "Contingencia finalizada en MS4",
                    "Se finalizó contingencia para " + actual.getServicioAfectado() + ". Motivo: " + request.motivo(),
                    actor
            );

            registrarExito(ENTIDAD_CONTINGENCIA, actual.getId(), "FINALIZAR_CONTINGENCIA", actor, detalleContingencia(actual));
            return contingenciaMapper.toModoResponse(actual);
        } catch (BusinessException ex) {
            registrarErrorUsuario(ENTIDAD_CONTINGENCIA, null, "FINALIZAR_CONTINGENCIA", actor, ex);
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico finalizando contingencia. actorIdUsuarioMs1={}",
                    actor == null ? null : actor.idUsuarioMs1(),
                    ex
            );
            registrarErrorTecnico(ENTIDAD_CONTINGENCIA, null, "FINALIZAR_CONTINGENCIA", actor, ex);
            throw internalError("No se pudo finalizar la contingencia.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ModoContingenciaResponseDto obtenerContingenciaActual(AuthenticatedUserContext actor) {
        try {
            contingenciaPolicy.authorizeConsultarContingencia(actor);
            return contingenciaMapper.toModoResponse(resolverContingenciaActiva());
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico obteniendo contingencia actual. actorIdUsuarioMs1={}",
                    actor == null ? null : actor.idUsuarioMs1(),
                    ex
            );
            throw internalError("No se pudo consultar la contingencia actual.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ModoContingenciaResponseDto> listarContingencias(ModoContingenciaFilterDto filter,
                                                                            PageRequestDto page,
                                                                            AuthenticatedUserContext actor) {
        try {
            contingenciaPolicy.authorizeConsultarContingencia(actor);
            contingenciaValidator.validarFiltroContingencia(filter);

            Page<ModoContingencia> result = modoContingenciaRepository.findAll(
                    ModoContingenciaSpecification.build(filter),
                    paginationService.toPageable(page, "fechaInicio")
            );

            return paginationService.toPageResponse(result, contingenciaMapper::toModoResponse);
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico listando contingencias. actorIdUsuarioMs1={}, search={}, estadoContingencia={}",
                    actor == null ? null : actor.idUsuarioMs1(),
                    filter == null ? null : filter.search(),
                    filter == null ? null : filter.estadoContingencia(),
                    ex
            );
            throw internalError("No se pudo listar las contingencias.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<InventarioEventoPendienteResponseDto> listarEventosPendientes(InventarioEventoPendienteFilterDto filter,
                                                                                         PageRequestDto page,
                                                                                         AuthenticatedUserContext actor) {
        try {
            contingenciaPolicy.authorizeConsultarEventosPendientes(actor);
            contingenciaValidator.validarFiltroEventoPendiente(filter);

            Page<InventarioEventoPendienteMs4> result = inventarioEventoPendienteRepository.findAll(
                    InventarioEventoPendienteSpecification.build(filter),
                    paginationService.toPageable(page, "fechaCreacion")
            );

            return paginationService.toPageResponse(result, contingenciaMapper::toEventoPendienteResponse);
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico listando eventos pendientes de inventario. actorIdUsuarioMs1={}, codigoVenta={}, estadoSincronizacion={}",
                    actor == null ? null : actor.idUsuarioMs1(),
                    filter == null ? null : filter.codigoVenta(),
                    filter == null ? null : filter.estadoSincronizacion(),
                    ex
            );
            throw internalError("No se pudo listar los eventos pendientes de inventario.", ex);
        }
    }

    @Override
    @Transactional
    public ContingenciaReconciliacionResponseDto reconciliarEventosPendientes(AuthenticatedUserContext actor) {
        try {
            contingenciaPolicy.authorizeReconciliar(actor);

            ModoContingencia activa = buscarContingenciaActivaPorServicio(SERVICIO_MS3);
            long totalReintentables = contarEventosReintentables();
            contingenciaValidator.validarReconciliacion(activa, totalReintentables);

            List<InventarioEventoPendienteMs4> eventos = inventarioEventoPendienteRepository.findPendientesForUpdate(
                    List.of(EstadoSincronizacionInventario.ERROR, EstadoSincronizacionInventario.REQUIERE_REVISION),
                    PageRequest.of(0, MAX_RECONCILIACION_BATCH)
            );

            LocalDateTime now = LocalDateTime.now(clock);

            eventos.forEach(evento -> {
                evento.setEstadoSincronizacion(EstadoSincronizacionInventario.PENDIENTE);
                evento.setUltimoError(null);
                evento.setFechaUltimoReintento(now);
                evento.setCantidadReintentos(evento.getCantidadReintentos() == null ? 1 : evento.getCantidadReintentos() + 1);
            });

            inventarioEventoPendienteRepository.saveAll(eventos);

            long pendientesPosteriores = contarEventosReintentables();

            registrarExito(
                    ENTIDAD_EVENTO_PENDIENTE,
                    null,
                    "RECONCILIAR_EVENTOS_PENDIENTES",
                    actor,
                    Map.of(
                            "totalReintentables", totalReintentables,
                            "totalReprogramados", eventos.size(),
                            "totalPendientesPosteriores", pendientesPosteriores
                    )
            );

            return contingenciaMapper.toReconciliacionResponse(
                    totalReintentables,
                    eventos.size(),
                    pendientesPosteriores
            );
        } catch (BusinessException ex) {
            registrarErrorUsuario(ENTIDAD_EVENTO_PENDIENTE, null, "RECONCILIAR_EVENTOS_PENDIENTES", actor, ex);
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico reconciliando eventos pendientes. actorIdUsuarioMs1={}",
                    actor == null ? null : actor.idUsuarioMs1(),
                    ex
            );
            registrarErrorTecnico(ENTIDAD_EVENTO_PENDIENTE, null, "RECONCILIAR_EVENTOS_PENDIENTES", actor, ex);
            throw internalError("No se pudo reconciliar los eventos pendientes.", ex);
        }
    }

    @Override
    @Transactional
    public InventarioEventoPendienteResponseDto marcarEventoSincronizado(Long idEventoPendiente,
                                                                         AuthenticatedUserContext actor) {
        try {
            if (actor != null) {
                contingenciaPolicy.authorizeReconciliar(actor);
            }

            InventarioEventoPendienteMs4 evento = resolverEventoPendienteActivo(idEventoPendiente);
            contingenciaValidator.validarEventoSincronizable(evento);

            if (evento.getEstadoSincronizacion() == EstadoSincronizacionInventario.SINCRONIZADO) {
                return contingenciaMapper.toEventoPendienteResponse(evento);
            }

            evento.setEstadoSincronizacion(EstadoSincronizacionInventario.SINCRONIZADO);
            evento.setUltimoError(null);
            evento.setFechaSincronizacion(LocalDateTime.now(clock));

            evento = inventarioEventoPendienteRepository.save(evento);

            registrarExito(
                    ENTIDAD_EVENTO_PENDIENTE,
                    evento.getId(),
                    "MARCAR_EVENTO_INVENTARIO_SINCRONIZADO",
                    actor,
                    Map.of("idempotencyKey", evento.getIdempotencyKey())
            );

            return contingenciaMapper.toEventoPendienteResponse(evento);
        } catch (BusinessException ex) {
            registrarErrorUsuario(ENTIDAD_EVENTO_PENDIENTE, idEventoPendiente, "MARCAR_EVENTO_INVENTARIO_SINCRONIZADO", actor, ex);
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico marcando evento inventario sincronizado. idEventoPendiente={}",
                    idEventoPendiente,
                    ex
            );
            registrarErrorTecnico(ENTIDAD_EVENTO_PENDIENTE, idEventoPendiente, "MARCAR_EVENTO_INVENTARIO_SINCRONIZADO", actor, ex);
            throw internalError("No se pudo marcar el evento como sincronizado.", ex);
        }
    }

    @Override
    @Transactional
    public InventarioEventoPendienteResponseDto marcarEventoError(Long idEventoPendiente,
                                                                  String error,
                                                                  AuthenticatedUserContext actor) {
        try {
            if (actor != null) {
                contingenciaPolicy.authorizeReconciliar(actor);
            }

            contingenciaValidator.validarMarcadoError(error);

            InventarioEventoPendienteMs4 evento = resolverEventoPendienteActivo(idEventoPendiente);
            contingenciaValidator.validarEventoMarcableConError(evento);

            evento.setEstadoSincronizacion(EstadoSincronizacionInventario.ERROR);
            evento.setUltimoError(truncate(error, MAX_ERROR_LENGTH));
            evento.setFechaUltimoReintento(LocalDateTime.now(clock));

            evento = inventarioEventoPendienteRepository.save(evento);

            registrarExito(
                    ENTIDAD_EVENTO_PENDIENTE,
                    evento.getId(),
                    "MARCAR_EVENTO_INVENTARIO_ERROR",
                    actor,
                    Map.of(
                            "idempotencyKey", evento.getIdempotencyKey(),
                            "error", truncate(error, MAX_ERROR_LENGTH)
                    )
            );

            return contingenciaMapper.toEventoPendienteResponse(evento);
        } catch (BusinessException ex) {
            registrarErrorUsuario(ENTIDAD_EVENTO_PENDIENTE, idEventoPendiente, "MARCAR_EVENTO_INVENTARIO_ERROR", actor, ex);
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico marcando evento inventario con error. idEventoPendiente={}",
                    idEventoPendiente,
                    ex
            );
            registrarErrorTecnico(ENTIDAD_EVENTO_PENDIENTE, idEventoPendiente, "MARCAR_EVENTO_INVENTARIO_ERROR", actor, ex);
            throw internalError("No se pudo marcar el evento con error.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validarVentaPermitidaPorContingencia() {
        ModoContingencia activa = buscarContingenciaActivaPorServicio(SERVICIO_MS3);

        if (activa == null) {
            return;
        }

        if (!Boolean.TRUE.equals(activa.getVentasPermitidas())) {
            throw new ConflictException("Las ventas que impactan stock están bloqueadas por contingencia activa.");
        }

        if (!Boolean.TRUE.equals(activa.getGuardarEventosPendientes())) {
            throw new ConflictException("La contingencia activa no permite guardar eventos pendientes de inventario.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ModoContingencia resolverContingenciaActiva() {
        return modoContingenciaRepository
                .findFirstByServicioAfectadoAndEstadoContingenciaAndEstadoTrueOrderByFechaInicioDesc(
                        SERVICIO_MS3,
                        EstadoContingencia.ACTIVO
                )
                .orElseThrow(() -> new NotFoundException("No existe contingencia activa para MS3."));
    }

    private ModoContingencia buscarContingenciaActivaPorServicio(String servicioAfectado) {
        return modoContingenciaRepository
                .findFirstByServicioAfectadoAndEstadoContingenciaAndEstadoTrueOrderByFechaInicioDesc(
                        normalizarServicio(servicioAfectado),
                        EstadoContingencia.ACTIVO
                )
                .orElse(null);
    }

    private InventarioEventoPendienteMs4 resolverEventoPendienteActivo(Long idEventoPendiente) {
        if (idEventoPendiente == null || idEventoPendiente <= 0) {
            throw NotFoundException.byId(RECURSO_EVENTO_PENDIENTE, idEventoPendiente);
        }

        InventarioEventoPendienteMs4 evento = inventarioEventoPendienteRepository.findById(idEventoPendiente)
                .orElseThrow(() -> NotFoundException.byId(RECURSO_EVENTO_PENDIENTE, idEventoPendiente));

        if (!Boolean.TRUE.equals(evento.getEstado())) {
            throw NotFoundException.inactive(RECURSO_EVENTO_PENDIENTE, idEventoPendiente);
        }

        return evento;
    }

    private long contarEventosReintentables() {
        return inventarioEventoPendienteRepository.countByEstadoSincronizacionInAndEstadoTrue(
                List.of(
                        EstadoSincronizacionInventario.PENDIENTE,
                        EstadoSincronizacionInventario.ERROR,
                        EstadoSincronizacionInventario.REQUIERE_REVISION
                )
        );
    }

    private void programarAlerta(TipoCorreo tipoCorreo,
                                 String asunto,
                                 String detalle,
                                 AuthenticatedUserContext actor) {
        try {
            correoOutboxService.programarAlertaAdministradores(tipoCorreo, asunto, detalle, actor);
        } catch (RuntimeException ex) {
            log.warn(
                    "No se pudo programar alerta administrativa de contingencia. tipoCorreo={}, asunto={}",
                    tipoCorreo,
                    asunto,
                    ex
            );
        }
    }

    private String normalizarServicio(String value) {
        return value == null || value.isBlank()
                ? SERVICIO_MS3
                : value.trim().toUpperCase(Locale.ROOT);
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

    private String unirObservacion(String actual, String motivoFinalizacion, String observacionFinalizacion) {
        StringBuilder builder = new StringBuilder();

        if (actual != null && !actual.isBlank()) {
            builder.append(actual.trim());
        }

        if (motivoFinalizacion != null && !motivoFinalizacion.isBlank()) {
            appendSeparado(builder, "Finalización: " + motivoFinalizacion.trim());
        }

        if (observacionFinalizacion != null && !observacionFinalizacion.isBlank()) {
            appendSeparado(builder, observacionFinalizacion.trim());
        }

        String value = builder.toString();
        return value.length() > 500 ? value.substring(0, 500) : value;
    }

    private void appendSeparado(StringBuilder builder, String value) {
        if (!builder.isEmpty()) {
            builder.append(" | ");
        }
        builder.append(value);
    }

    private Map<String, Object> detalleContingencia(ModoContingencia entity) {
        Map<String, Object> detalle = new LinkedHashMap<>();
        detalle.put("servicioAfectado", entity.getServicioAfectado());
        detalle.put("estadoContingencia", entity.getEstadoContingencia());
        detalle.put("ventasPermitidas", entity.getVentasPermitidas());
        detalle.put("guardarEventosPendientes", entity.getGuardarEventosPendientes());
        detalle.put("totalEventosPendientes", entity.getTotalEventosPendientes());
        return detalle;
    }

    private void registrarExito(String entidad,
                                Long entidadId,
                                String accion,
                                AuthenticatedUserContext actor,
                                Object detalle) {
        auditoriaFuncionalService.registrarExito(entidad, entidadId, accion, actor, detalle);
    }

    private void registrarErrorUsuario(String entidad,
                                       Long entidadId,
                                       String accion,
                                       AuthenticatedUserContext actor,
                                       BusinessException ex) {
        auditoriaFuncionalService.registrarErrorUsuario(entidad, entidadId, accion, actor, ex.getCode(), ex.getMessage());
    }

    private void registrarErrorTecnico(String entidad,
                                       Long entidadId,
                                       String accion,
                                       AuthenticatedUserContext actor,
                                       Exception ex) {
        auditoriaFuncionalService.registrarErrorTecnico(entidad, entidadId, accion, actor, ex);
    }

    private BusinessException internalError(String message, RuntimeException ex) {
        return new BusinessException(ErrorCodes.INTERNAL_ERROR, message, HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }
}