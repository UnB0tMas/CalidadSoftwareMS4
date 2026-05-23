// ruta: src/main/java/com/upsjb/ms4/service/impl/caja/CajaServiceImpl.java
package com.upsjb.ms4.service.impl.caja;

import com.upsjb.ms4.domain.entity.caja.Caja;
import com.upsjb.ms4.domain.entity.caja.CajaMovimiento;
import com.upsjb.ms4.domain.entity.snapshot.EmpleadoSnapshotMs2;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.enums.EstadoCaja;
import com.upsjb.ms4.domain.enums.MetodoPago;
import com.upsjb.ms4.domain.enums.TipoMovimientoCaja;
import com.upsjb.ms4.dto.caja.filter.CajaFilterDto;
import com.upsjb.ms4.dto.caja.request.CajaAjusteRequestDto;
import com.upsjb.ms4.dto.caja.request.CajaAperturaRequestDto;
import com.upsjb.ms4.dto.caja.request.CajaCierreRequestDto;
import com.upsjb.ms4.dto.caja.response.CajaCierreResponseDto;
import com.upsjb.ms4.dto.caja.response.CajaDetailResponseDto;
import com.upsjb.ms4.dto.caja.response.CajaMovimientoResponseDto;
import com.upsjb.ms4.dto.caja.response.CajaResponseDto;
import com.upsjb.ms4.dto.caja.response.CajaResumenDiaResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.mapper.caja.CajaMapper;
import com.upsjb.ms4.mapper.caja.CajaMovimientoMapper;
import com.upsjb.ms4.policy.CajaPolicy;
import com.upsjb.ms4.repository.CajaMovimientoRepository;
import com.upsjb.ms4.repository.CajaRepository;
import com.upsjb.ms4.repository.EmpleadoSnapshotMs2Repository;
import com.upsjb.ms4.repository.VentaRepository;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.service.contract.auditoria.AuditoriaFuncionalService;
import com.upsjb.ms4.service.contract.caja.CajaMovimientoService;
import com.upsjb.ms4.service.contract.caja.CajaService;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.shared.pagination.PaginationService;
import com.upsjb.ms4.specification.CajaSpecification;
import com.upsjb.ms4.util.CodigoGenerator;
import com.upsjb.ms4.util.MoneyUtil;
import com.upsjb.ms4.validator.CajaCierreValidator;
import com.upsjb.ms4.validator.CajaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CajaServiceImpl implements CajaService {

    private static final Logger log = LoggerFactory.getLogger(CajaServiceImpl.class);

    private static final String RECURSO_CAJA = "Caja";
    private static final String RECURSO_EMPLEADO = "Empleado snapshot MS2";
    private static final String ENTIDAD_CAJA = "CAJA";

    private final CajaRepository cajaRepository;
    private final CajaMovimientoRepository cajaMovimientoRepository;
    private final VentaRepository ventaRepository;
    private final EmpleadoSnapshotMs2Repository empleadoSnapshotRepository;
    private final CajaMapper cajaMapper;
    private final CajaMovimientoMapper cajaMovimientoMapper;
    private final CajaPolicy cajaPolicy;
    private final CajaValidator cajaValidator;
    private final CajaCierreValidator cajaCierreValidator;
    private final CajaMovimientoService cajaMovimientoService;
    private final PaginationService paginationService;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final Clock clock;

    public CajaServiceImpl(CajaRepository cajaRepository,
                           CajaMovimientoRepository cajaMovimientoRepository,
                           VentaRepository ventaRepository,
                           EmpleadoSnapshotMs2Repository empleadoSnapshotRepository,
                           CajaMapper cajaMapper,
                           CajaMovimientoMapper cajaMovimientoMapper,
                           CajaPolicy cajaPolicy,
                           CajaValidator cajaValidator,
                           CajaCierreValidator cajaCierreValidator,
                           CajaMovimientoService cajaMovimientoService,
                           PaginationService paginationService,
                           AuditoriaFuncionalService auditoriaFuncionalService,
                           Clock clock) {
        this.cajaRepository = cajaRepository;
        this.cajaMovimientoRepository = cajaMovimientoRepository;
        this.ventaRepository = ventaRepository;
        this.empleadoSnapshotRepository = empleadoSnapshotRepository;
        this.cajaMapper = cajaMapper;
        this.cajaMovimientoMapper = cajaMovimientoMapper;
        this.cajaPolicy = cajaPolicy;
        this.cajaValidator = cajaValidator;
        this.cajaCierreValidator = cajaCierreValidator;
        this.cajaMovimientoService = cajaMovimientoService;
        this.paginationService = paginationService;
        this.auditoriaFuncionalService = auditoriaFuncionalService;
        this.clock = clock;
    }

    @Override
    @Transactional
    public CajaResponseDto abrirCaja(CajaAperturaRequestDto request, AuthenticatedUserContext actor) {
        try {
            cajaPolicy.authorizeAbrirCaja(actor);
            cajaValidator.validarApertura(request);

            EmpleadoSnapshotMs2 empleado = resolverEmpleadoActivo(actor);
            LocalDate fechaOperacion = LocalDate.now(clock);

            Caja cajaAbiertaExistente = cajaRepository
                    .findByFechaOperacionAndEstadoCajaForUpdate(fechaOperacion, EstadoCaja.ABIERTA)
                    .orElse(null);

            cajaValidator.validarCajaNoDuplicadaPorDia(cajaAbiertaExistente, fechaOperacion);

            Caja caja = Caja.builder()
                    .codigoCaja(generarCodigoCaja(fechaOperacion))
                    .fechaOperacion(fechaOperacion)
                    .estadoCaja(EstadoCaja.ABIERTA)
                    .montoInicial(MoneyUtil.money(request.montoInicial()))
                    .montoEsperadoEfectivo(MoneyUtil.money(request.montoInicial()))
                    .montoRealEfectivo(null)
                    .montoTarjeta(MoneyUtil.ZERO)
                    .montoTotalVendido(MoneyUtil.ZERO)
                    .diferencia(null)
                    .idEmpleadoAperturaSnapshot(empleado.getId())
                    .idUsuarioAperturaMs1(actor.idUsuarioMs1())
                    .fechaApertura(LocalDateTime.now(clock))
                    .observacionApertura(normalize(request.observacionApertura()))
                    .estado(true)
                    .build();

            caja = cajaRepository.save(caja);
            cajaMovimientoService.registrarMovimientoApertura(caja, actor);

            registrarAuditoriaExito("ABRIR_CAJA", caja, actor, Map.of(
                    "codigoCaja", caja.getCodigoCaja(),
                    "fechaOperacion", caja.getFechaOperacion(),
                    "montoInicial", caja.getMontoInicial()
            ));

            return cajaMapper.toResponse(caja);
        } catch (BusinessException ex) {
            registrarAuditoriaErrorUsuario("ABRIR_CAJA", null, actor, ex);
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico abriendo caja. actorIdUsuarioMs1={}, actorRol={}",
                    actor == null ? null : actor.idUsuarioMs1(),
                    actor == null ? null : actor.rol(),
                    ex
            );
            registrarAuditoriaErrorTecnico("ABRIR_CAJA", null, actor, ex);
            throw internalError("No se pudo abrir la caja.", ex);
        }
    }

    @Override
    @Transactional
    public CajaCierreResponseDto cerrarCaja(CajaCierreRequestDto request, AuthenticatedUserContext actor) {
        Caja caja = null;

        try {
            EmpleadoSnapshotMs2 empleado = resolverEmpleadoActivo(actor);
            caja = resolverCajaActualAbiertaConLock();

            cajaPolicy.authorizeCerrarCaja(actor, caja);
            cajaCierreValidator.validarCierre(caja, request, caja.getFechaOperacion());

            recalcularTotales(caja);

            BigDecimal montoReal = MoneyUtil.money(request.montoRealEfectivo());
            caja.setMontoRealEfectivo(montoReal);
            caja.setDiferencia(MoneyUtil.subtract(montoReal, caja.getMontoEsperadoEfectivo()));
            caja.setIdEmpleadoCierreSnapshot(empleado.getId());
            caja.setIdUsuarioCierreMs1(actor.idUsuarioMs1());
            caja.setFechaCierre(LocalDateTime.now(clock));
            caja.setObservacionCierre(normalize(request.observacionCierre()));
            caja.setEstadoCaja(EstadoCaja.CERRADA);

            caja = cajaRepository.save(caja);
            cajaMovimientoService.registrarMovimientoCierre(caja, actor);

            int cantidadVentas = contarVentas(caja.getId());
            int cantidadMovimientos = contarMovimientos(caja.getId());

            registrarAuditoriaExito("CERRAR_CAJA", caja, actor, Map.of(
                    "codigoCaja", caja.getCodigoCaja(),
                    "montoEsperadoEfectivo", caja.getMontoEsperadoEfectivo(),
                    "montoRealEfectivo", caja.getMontoRealEfectivo(),
                    "diferencia", caja.getDiferencia()
            ));

            return cajaMapper.toCierreResponse(caja, cantidadVentas, cantidadMovimientos);
        } catch (BusinessException ex) {
            registrarAuditoriaErrorUsuario("CERRAR_CAJA", caja, actor, ex);
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico cerrando caja. idCaja={}, actorIdUsuarioMs1={}, actorRol={}",
                    caja == null ? null : caja.getId(),
                    actor == null ? null : actor.idUsuarioMs1(),
                    actor == null ? null : actor.rol(),
                    ex
            );
            registrarAuditoriaErrorTecnico("CERRAR_CAJA", caja, actor, ex);
            throw internalError("No se pudo cerrar la caja.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CajaResponseDto obtenerCajaActual(AuthenticatedUserContext actor) {
        cajaPolicy.authorizeVerCajaActual(actor);

        Caja caja = cajaRepository.findByFechaOperacionAndEstadoCajaAndEstadoTrue(
                        LocalDate.now(clock),
                        EstadoCaja.ABIERTA
                )
                .orElseThrow(() -> new NotFoundException("No existe una caja abierta para el día de operación actual."));

        cajaPolicy.authorizeVerCaja(actor, caja);
        return cajaMapper.toResponse(caja);
    }

    @Override
    @Transactional(readOnly = true)
    public CajaDetailResponseDto obtenerDetalle(Long idCaja, AuthenticatedUserContext actor) {
        Caja caja = resolverCajaActiva(idCaja);
        cajaPolicy.authorizeVerCaja(actor, caja);

        List<CajaMovimientoResponseDto> movimientos = cajaMovimientoRepository
                .findByIdCajaAndEstadoTrueOrderByCreatedAtAsc(caja.getId())
                .stream()
                .map(cajaMovimientoMapper::toResponse)
                .toList();

        return cajaMapper.toDetailResponse(caja, movimientos);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<CajaResponseDto> listar(CajaFilterDto filter,
                                                   PageRequestDto page,
                                                   AuthenticatedUserContext actor) {
        cajaPolicy.authorizeListarCajasAdmin(actor);
        cajaValidator.validarFiltro(filter);

        Page<Caja> cajas = cajaRepository.findAll(
                CajaSpecification.build(filter),
                paginationService.toPageable(page, "fechaOperacion")
        );

        return paginationService.toPageResponse(cajas, cajaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CajaResumenDiaResponseDto obtenerResumenDia(LocalDate fechaOperacion, AuthenticatedUserContext actor) {
        cajaPolicy.authorizeVerCajaActual(actor);

        LocalDate fecha = fechaOperacion == null ? LocalDate.now(clock) : fechaOperacion;

        Caja caja = cajaRepository.findByFechaOperacionBetweenAndEstadoTrueOrderByFechaOperacionDesc(fecha, fecha)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No existe caja para la fecha de operación indicada."));

        cajaPolicy.authorizeVerCaja(actor, caja);

        int cantidadVentas = contarVentas(caja.getId());
        int cantidadPagosEfectivo = contarVentasPorMetodo(caja.getId(), MetodoPago.EFECTIVO);
        int cantidadPagosTarjeta = contarVentasPorMetodo(caja.getId(), MetodoPago.TARJETA_PRESENCIAL_STRIPE_SANDBOX);

        return cajaMapper.toResumenDiaResponse(
                caja,
                cantidadVentas,
                cantidadPagosEfectivo,
                cantidadPagosTarjeta
        );
    }

    @Override
    @Transactional
    public CajaResponseDto registrarAjuste(CajaAjusteRequestDto request, AuthenticatedUserContext actor) {
        Caja caja = null;

        try {
            caja = resolverCajaActualAbiertaConLock();
            cajaPolicy.authorizeAjustarCaja(actor, caja);
            cajaValidator.validarAjuste(caja, request);

            cajaMovimientoService.registrarMovimientoAjuste(caja, request, actor);
            recalcularTotales(caja);

            caja = cajaRepository.save(caja);

            registrarAuditoriaExito("REGISTRAR_AJUSTE_CAJA", caja, actor, Map.of(
                    "codigoCaja", caja.getCodigoCaja(),
                    "monto", MoneyUtil.money(request.monto()),
                    "descripcion", request.descripcion()
            ));

            return cajaMapper.toResponse(caja);
        } catch (BusinessException ex) {
            registrarAuditoriaErrorUsuario("REGISTRAR_AJUSTE_CAJA", caja, actor, ex);
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico registrando ajuste de caja. idCaja={}, actorIdUsuarioMs1={}, actorRol={}",
                    caja == null ? null : caja.getId(),
                    actor == null ? null : actor.idUsuarioMs1(),
                    actor == null ? null : actor.rol(),
                    ex
            );
            registrarAuditoriaErrorTecnico("REGISTRAR_AJUSTE_CAJA", caja, actor, ex);
            throw internalError("No se pudo registrar el ajuste de caja.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Caja resolverCajaAbiertaParaVentaFisica() {
        Caja caja = cajaRepository.findByFechaOperacionAndEstadoCajaAndEstadoTrue(
                        LocalDate.now(clock),
                        EstadoCaja.ABIERTA
                )
                .orElseThrow(() -> new NotFoundException("No existe una caja abierta para registrar venta física."));

        cajaValidator.validarCajaAbiertaParaVenta(caja);
        return caja;
    }

    @Override
    @Transactional
    public void recalcularTotalesCaja(Long idCaja) {
        try {
            Caja caja = cajaRepository.findByIdForUpdate(idCaja)
                    .filter(Caja::isActivo)
                    .orElseThrow(() -> NotFoundException.byId(RECURSO_CAJA, idCaja));

            recalcularTotales(caja);
            cajaRepository.save(caja);
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Error técnico recalculando totales de caja. idCaja={}", idCaja, ex);
            throw internalError("No se pudo recalcular los totales de la caja.", ex);
        }
    }

    private Caja resolverCajaActualAbiertaConLock() {
        LocalDate fechaOperacion = LocalDate.now(clock);

        return cajaRepository.findByFechaOperacionAndEstadoCajaForUpdate(fechaOperacion, EstadoCaja.ABIERTA)
                .orElseThrow(() -> new NotFoundException("No existe una caja abierta para el día de operación actual."));
    }

    private Caja resolverCajaActiva(Long idCaja) {
        cajaValidator.validarIdCaja(idCaja);

        return cajaRepository.findById(idCaja)
                .filter(Caja::isActivo)
                .orElseThrow(() -> NotFoundException.byId(RECURSO_CAJA, idCaja));
    }

    private EmpleadoSnapshotMs2 resolverEmpleadoActivo(AuthenticatedUserContext actor) {
        if (actor == null || actor.idUsuarioMs1() == null) {
            throw new NotFoundException("No se pudo resolver el usuario autenticado para operar caja.");
        }

        EmpleadoSnapshotMs2 empleado = empleadoSnapshotRepository
                .findByIdUsuarioMs1AndEstadoTrue(actor.idUsuarioMs1())
                .orElseThrow(() -> new NotFoundException(
                        RECURSO_EMPLEADO + " no encontrado para el usuario autenticado."
                ));

        cajaValidator.validarEmpleadoActivoParaCaja(empleado);
        return empleado;
    }

    private void recalcularTotales(Caja caja) {
        cajaValidator.validarCajaActiva(caja);

        BigDecimal montoInicial = MoneyUtil.money(caja.getMontoInicial());
        BigDecimal ventasEfectivo = sumarMovimientos(caja.getId(), TipoMovimientoCaja.VENTA_EFECTIVO);
        BigDecimal ventasTarjeta = sumarMovimientos(caja.getId(), TipoMovimientoCaja.VENTA_TARJETA);
        BigDecimal ajustes = sumarMovimientos(caja.getId(), TipoMovimientoCaja.AJUSTE);
        BigDecimal anulaciones = sumarMovimientos(caja.getId(), TipoMovimientoCaja.ANULACION);

        BigDecimal esperadoEfectivo = MoneyUtil.subtract(
                MoneyUtil.add(MoneyUtil.add(montoInicial, ventasEfectivo), ajustes),
                anulaciones
        );

        BigDecimal totalVendido = MoneyUtil.subtract(
                MoneyUtil.add(ventasEfectivo, ventasTarjeta),
                anulaciones
        );

        caja.setMontoEsperadoEfectivo(MoneyUtil.max(esperadoEfectivo, MoneyUtil.ZERO));
        caja.setMontoTarjeta(MoneyUtil.money(ventasTarjeta));
        caja.setMontoTotalVendido(MoneyUtil.max(totalVendido, MoneyUtil.ZERO));

        if (caja.getMontoRealEfectivo() != null) {
            caja.setDiferencia(MoneyUtil.subtract(caja.getMontoRealEfectivo(), caja.getMontoEsperadoEfectivo()));
        }
    }

    private BigDecimal sumarMovimientos(Long idCaja, TipoMovimientoCaja tipo) {
        return MoneyUtil.money(cajaMovimientoRepository.sumMontoByIdCajaAndTipos(idCaja, List.of(tipo)));
    }

    private int contarVentas(Long idCaja) {
        return ventaRepository.findByIdCajaAndEstadoTrueOrderByFechaVentaAsc(idCaja).size();
    }

    private int contarVentasPorMetodo(Long idCaja, MetodoPago metodoPago) {
        return (int) ventaRepository.findByIdCajaAndEstadoTrueOrderByFechaVentaAsc(idCaja)
                .stream()
                .filter(venta -> venta.getMetodoPagoPrincipal() == metodoPago)
                .count();
    }

    private int contarMovimientos(Long idCaja) {
        return cajaMovimientoRepository.findByIdCajaAndEstadoTrueOrderByCreatedAtAsc(idCaja).size();
    }

    private String generarCodigoCaja(LocalDate fechaOperacion) {
        long base = cajaRepository.count() + 1L;

        for (long sequence = base; sequence < base + 1000L; sequence++) {
            String codigo = CodigoGenerator.caja(sequence, fechaOperacion);

            if (cajaRepository.findByCodigoCajaIgnoreCase(codigo).isEmpty()) {
                return codigo;
            }
        }

        throw new BusinessException(
                ErrorCodes.CONFLICT,
                "No se pudo generar un código único de caja.",
                HttpStatus.CONFLICT
        );
    }

    private void registrarAuditoriaExito(String accion,
                                         Caja caja,
                                         AuthenticatedUserContext actor,
                                         Object detalle) {
        auditoriaFuncionalService.registrarExito(
                ENTIDAD_CAJA,
                caja == null ? null : caja.getId(),
                accion,
                actor,
                detalle
        );
    }

    private void registrarAuditoriaErrorUsuario(String accion,
                                                Caja caja,
                                                AuthenticatedUserContext actor,
                                                BusinessException ex) {
        auditoriaFuncionalService.registrarErrorUsuario(
                ENTIDAD_CAJA,
                caja == null ? null : caja.getId(),
                accion,
                actor,
                ex.getCode(),
                ex.getMessage()
        );
    }

    private void registrarAuditoriaErrorTecnico(String accion,
                                                Caja caja,
                                                AuthenticatedUserContext actor,
                                                RuntimeException ex) {
        auditoriaFuncionalService.registrarErrorTecnico(
                ENTIDAD_CAJA,
                caja == null ? null : caja.getId(),
                accion,
                actor,
                ex
        );
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
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