package com.upsjb.ms4.service.impl.venta;

import com.upsjb.ms4.domain.entity.caja.Caja;
import com.upsjb.ms4.domain.entity.config.ConfiguracionTributariaVersion;
import com.upsjb.ms4.domain.entity.pago.Pago;
import com.upsjb.ms4.domain.entity.snapshot.ClienteSnapshotMs2;
import com.upsjb.ms4.domain.entity.snapshot.EmpleadoSnapshotMs2;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.enums.CanalVenta;
import com.upsjb.ms4.domain.enums.EstadoCaja;
import com.upsjb.ms4.domain.enums.EstadoPago;
import com.upsjb.ms4.domain.enums.EstadoVenta;
import com.upsjb.ms4.domain.enums.MetodoPago;
import com.upsjb.ms4.domain.enums.TipoCorreo;
import com.upsjb.ms4.dto.boleta.response.BoletaDetailResponseDto;
import com.upsjb.ms4.dto.pago.request.PagoEfectivoRequestDto;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms4.dto.venta.request.VentaCalculoPreviewRequestDto;
import com.upsjb.ms4.dto.venta.request.VentaFisicaCreateRequestDto;
import com.upsjb.ms4.dto.venta.response.VentaCalculoPreviewResponseDto;
import com.upsjb.ms4.dto.venta.response.VentaDetailResponseDto;
import com.upsjb.ms4.policy.VentaPolicy;
import com.upsjb.ms4.repository.CajaRepository;
import com.upsjb.ms4.repository.PagoRepository;
import com.upsjb.ms4.repository.VentaDetalleRepository;
import com.upsjb.ms4.repository.VentaRepository;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.service.contract.auditoria.AuditoriaFuncionalService;
import com.upsjb.ms4.service.contract.boleta.BoletaService;
import com.upsjb.ms4.service.contract.caja.CajaMovimientoService;
import com.upsjb.ms4.service.contract.config.ConfiguracionTributariaService;
import com.upsjb.ms4.service.contract.contingencia.ContingenciaService;
import com.upsjb.ms4.service.contract.pago.PagoService;
import com.upsjb.ms4.service.contract.reference.Ms4ReferenceResolverService;
import com.upsjb.ms4.service.contract.venta.VentaCalculoResultado;
import com.upsjb.ms4.service.contract.venta.VentaCalculoService;
import com.upsjb.ms4.service.contract.venta.VentaConsultaService;
import com.upsjb.ms4.service.contract.venta.VentaFisicaService;
import com.upsjb.ms4.service.contract.venta.VentaStockCommandService;
import com.upsjb.ms4.shared.audit.AuditContextHolder;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.shared.exception.ConflictException;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.shared.exception.ValidationException;
import com.upsjb.ms4.util.CodigoGenerator;
import com.upsjb.ms4.util.MoneyUtil;
import com.upsjb.ms4.validator.VentaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class VentaFisicaServiceImpl implements VentaFisicaService {

    private static final Logger log = LoggerFactory.getLogger(VentaFisicaServiceImpl.class);

    private static final String RECURSO_VENTA = "Venta";
    private static final String RECURSO_CAJA = "Caja";
    private static final String ENTIDAD_VENTA = "VENTA";
    private static final String ACCION_PREVISUALIZAR_FISICA = "VENTA_FISICA_PREVISUALIZADA";
    private static final String ACCION_CREAR_FISICA_PENDIENTE = "VENTA_FISICA_CREADA_PENDIENTE_PAGO";
    private static final String ACCION_CONFIRMAR_FISICA_EFECTIVO = "VENTA_FISICA_CONFIRMADA_EFECTIVO";
    private static final String ACCION_CONFIRMAR_FISICA_STRIPE = "VENTA_FISICA_CONFIRMADA_STRIPE";
    private static final String ACCION_ANULAR_FISICA = "VENTA_FISICA_ANULADA";

    private final VentaRepository ventaRepository;
    private final VentaDetalleRepository ventaDetalleRepository;
    private final PagoRepository pagoRepository;
    private final CajaRepository cajaRepository;
    private final VentaCalculoService ventaCalculoService;
    private final VentaConsultaService ventaConsultaService;
    private final VentaStockCommandService ventaStockCommandService;
    private final ConfiguracionTributariaService configuracionTributariaService;
    private final Ms4ReferenceResolverService referenceResolver;
    private final PagoService pagoService;
    private final BoletaService boletaService;
    private final CajaMovimientoService cajaMovimientoService;
    private final ContingenciaService contingenciaService;
    private final VentaPolicy ventaPolicy;
    private final VentaValidator ventaValidator;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final Clock clock;

    public VentaFisicaServiceImpl(VentaRepository ventaRepository,
                                  VentaDetalleRepository ventaDetalleRepository,
                                  PagoRepository pagoRepository,
                                  CajaRepository cajaRepository,
                                  VentaCalculoService ventaCalculoService,
                                  VentaConsultaService ventaConsultaService,
                                  VentaStockCommandService ventaStockCommandService,
                                  ConfiguracionTributariaService configuracionTributariaService,
                                  Ms4ReferenceResolverService referenceResolver,
                                  PagoService pagoService,
                                  BoletaService boletaService,
                                  CajaMovimientoService cajaMovimientoService,
                                  ContingenciaService contingenciaService,
                                  VentaPolicy ventaPolicy,
                                  VentaValidator ventaValidator,
                                  AuditoriaFuncionalService auditoriaFuncionalService,
                                  Clock clock) {
        this.ventaRepository = ventaRepository;
        this.ventaDetalleRepository = ventaDetalleRepository;
        this.pagoRepository = pagoRepository;
        this.cajaRepository = cajaRepository;
        this.ventaCalculoService = ventaCalculoService;
        this.ventaConsultaService = ventaConsultaService;
        this.ventaStockCommandService = ventaStockCommandService;
        this.configuracionTributariaService = configuracionTributariaService;
        this.referenceResolver = referenceResolver;
        this.pagoService = pagoService;
        this.boletaService = boletaService;
        this.cajaMovimientoService = cajaMovimientoService;
        this.contingenciaService = contingenciaService;
        this.ventaPolicy = ventaPolicy;
        this.ventaValidator = ventaValidator;
        this.auditoriaFuncionalService = auditoriaFuncionalService;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public VentaCalculoPreviewResponseDto previsualizarVentaFisica(VentaCalculoPreviewRequestDto request,
                                                                   AuthenticatedUserContext actor) {
        try {
            return ventaCalculoService.calcularPreview(request, CanalVenta.FISICA, actor);
        } catch (BusinessException ex) {
            auditoriaFuncionalService.registrarErrorUsuario(
                    ENTIDAD_VENTA,
                    null,
                    ACCION_PREVISUALIZAR_FISICA,
                    actor,
                    ex.getCode(),
                    ex.getMessage()
            );
            throw ex;
        } catch (RuntimeException ex) {
            AuditContextHolder.AuditContext auditContext = AuditContextHolder.getOrEmpty();
            log.error(
                    "Error técnico previsualizando venta física. actorIdUsuarioMs1={}, requestId={}, correlationId={}",
                    actor == null ? null : actor.idUsuarioMs1(),
                    auditContext.requestId(),
                    auditContext.correlationId(),
                    ex
            );
            auditoriaFuncionalService.registrarErrorTecnico(
                    ENTIDAD_VENTA,
                    null,
                    ACCION_PREVISUALIZAR_FISICA,
                    actor,
                    ex
            );
            throw internalError("No se pudo previsualizar la venta física.", ex);
        }
    }

    @Override
    @Transactional
    public VentaDetailResponseDto crearVentaFisicaConPagoEfectivo(VentaFisicaCreateRequestDto request,
                                                                  PagoEfectivoRequestDto pagoRequest,
                                                                  AuthenticatedUserContext actor) {
        validarMetodoSolicitud(request, MetodoPago.EFECTIVO);
        VentaDetailResponseDto pendiente = crearVentaFisicaPendientePago(request, actor);

        if (pendiente == null || pendiente.venta() == null || pendiente.venta().id() == null) {
            throw new BusinessException(
                    ErrorCodes.INTERNAL_ERROR,
                    "No se pudo resolver la venta física creada para registrar el pago en efectivo.",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        return confirmarVentaFisicaConPagoEfectivo(pendiente.venta().id(), pagoRequest, actor);
    }

    @Override
    @Transactional
    public VentaDetailResponseDto crearVentaFisicaPendientePago(VentaFisicaCreateRequestDto request,
                                                                AuthenticatedUserContext actor) {
        try {
            ventaPolicy.authorizeCrearVentaFisica(actor);
            contingenciaService.validarVentaPermitidaPorContingencia();

            MetodoPago metodoPago = resolverMetodoPagoFisico(request);
            ClienteSnapshotMs2 cliente = referenceResolver.resolverClienteActivo(
                    request.idClienteSnapshot(),
                    request.idClienteMs2(),
                    request.idUsuarioClienteMs1(),
                    request.numeroDocumento(),
                    request.ruc()
            );
            EmpleadoSnapshotMs2 empleado = referenceResolver.resolverEmpleadoActivoPorUsuarioMs1(actor.idUsuarioMs1());
            Caja caja = resolverCajaParaVenta(request);

            ventaValidator.validarCrearVentaFisica(request, cliente, empleado, caja, metodoPago);

            LocalDateTime now = LocalDateTime.now(clock);
            ConfiguracionTributariaVersion igv = configuracionTributariaService.resolverIgvVigenteParaVenta();
            VentaCalculoResultado calculo = ventaCalculoService.calcularVenta(
                    request.detalles(),
                    CanalVenta.FISICA,
                    now
            );

            if (!Boolean.TRUE.equals(calculo.stockSuficiente())) {
                throw new ConflictException("No existe stock suficiente para completar la venta física.");
            }

            Venta venta = Venta.builder()
                    .codigoVenta(generarCodigoVenta())
                    .canalVenta(CanalVenta.FISICA)
                    .estadoVenta(EstadoVenta.PENDIENTE_PAGO)
                    .idClienteSnapshot(cliente.getId())
                    .idClienteMs2(cliente.getIdClienteMs2())
                    .idUsuarioClienteMs1(cliente.getIdUsuarioMs1())
                    .idEmpleadoSnapshot(empleado.getId())
                    .idEmpleadoMs2(empleado.getIdEmpleadoMs2())
                    .idUsuarioEmpleadoMs1(empleado.getIdUsuarioMs1())
                    .idCaja(caja.getId())
                    .idConfiguracionTributariaVersion(igv.getId())
                    .moneda(normalizarMoneda(request.moneda()))
                    .subtotal(calculo.subtotal())
                    .descuentoTotal(calculo.descuentoTotal())
                    .opGravada(calculo.opGravada())
                    .opExonerada(calculo.opExonerada())
                    .opInafecta(calculo.opInafecta())
                    .igvPorcentaje(calculo.igvPorcentaje())
                    .igvTotal(calculo.igvTotal())
                    .total(calculo.total())
                    .metodoPagoPrincipal(metodoPago)
                    .fechaVenta(now)
                    .observacion(normalizarTexto(request.observacion()))
                    .requestId(AuditContextHolder.getOrEmpty().requestId())
                    .correlationId(AuditContextHolder.getOrEmpty().correlationId())
                    .estado(true)
                    .build();

            venta = ventaRepository.save(venta);

            final Venta ventaPersistida = venta;
            var detalles = calculo.lineas()
                    .stream()
                    .map(linea -> ventaCalculoService.construirDetalleCongelado(ventaPersistida, linea))
                    .toList();

            ventaDetalleRepository.saveAll(detalles);

            auditoriaFuncionalService.registrarExito(
                    ENTIDAD_VENTA,
                    venta.getId(),
                    ACCION_CREAR_FISICA_PENDIENTE,
                    actor,
                    auditMap(
                            "codigoVenta", venta.getCodigoVenta(),
                            "canalVenta", venta.getCanalVenta(),
                            "estadoVenta", venta.getEstadoVenta(),
                            "idCaja", venta.getIdCaja(),
                            "metodoPago", venta.getMetodoPagoPrincipal(),
                            "total", venta.getTotal()
                    )
            );

            return ventaConsultaService.obtenerDetalleEmpleado(venta.getId(), actor);
        } catch (BusinessException ex) {
            auditoriaFuncionalService.registrarErrorUsuario(
                    ENTIDAD_VENTA,
                    null,
                    ACCION_CREAR_FISICA_PENDIENTE,
                    actor,
                    ex.getCode(),
                    ex.getMessage()
            );
            throw ex;
        } catch (RuntimeException ex) {
            AuditContextHolder.AuditContext auditContext = AuditContextHolder.getOrEmpty();
            log.error(
                    "Error técnico creando venta física pendiente de pago. actorIdUsuarioMs1={}, requestId={}, correlationId={}",
                    actor == null ? null : actor.idUsuarioMs1(),
                    auditContext.requestId(),
                    auditContext.correlationId(),
                    ex
            );
            auditoriaFuncionalService.registrarErrorTecnico(
                    ENTIDAD_VENTA,
                    null,
                    ACCION_CREAR_FISICA_PENDIENTE,
                    actor,
                    ex
            );
            throw internalError("No se pudo crear la venta física.", ex);
        }
    }

    @Override
    @Transactional
    public VentaDetailResponseDto confirmarVentaFisicaConPagoEfectivo(Long idVenta,
                                                                      PagoEfectivoRequestDto request,
                                                                      AuthenticatedUserContext actor) {
        try {
            Venta venta = resolverVentaActiva(idVenta);
            validarVentaFisica(venta);

            ventaPolicy.authorizeCrearVentaFisica(actor);
            ventaPolicy.authorizeVerVenta(actor, venta);

            if (venta.getEstadoVenta() == EstadoVenta.CONFIRMADA) {
                return ventaConsultaService.obtenerDetalleEmpleado(venta.getId(), actor);
            }

            validarMetodoVenta(venta, MetodoPago.EFECTIVO);

            pagoService.registrarPagoEfectivoAprobado(venta.getId(), request, actor);
            Pago pago = resolverPagoAprobadoVenta(venta.getId(), MetodoPago.EFECTIVO);
            venta = resolverVentaActiva(venta.getId());

            ventaValidator.validarVentaPagadaParaConfirmar(venta);
            venta.setEstadoVenta(EstadoVenta.CONFIRMADA);
            venta.setMetodoPagoPrincipal(MetodoPago.EFECTIVO);
            venta = ventaRepository.save(venta);

            BoletaDetailResponseDto boleta = emitirYProgramarBoletaFisica(venta, actor);
            Caja caja = resolverCajaParaActualizar(venta.getIdCaja());
            actualizarTotalesCajaPorPago(caja, pago);
            cajaMovimientoService.registrarMovimientoVentaEfectivo(caja, venta, pago, actor);
            ventaStockCommandService.registrarComandosConfirmacionStock(venta, actor);

            auditoriaFuncionalService.registrarExito(
                    ENTIDAD_VENTA,
                    venta.getId(),
                    ACCION_CONFIRMAR_FISICA_EFECTIVO,
                    actor,
                    auditMap(
                            "codigoVenta", venta.getCodigoVenta(),
                            "idPago", pago.getId(),
                            "idBoleta", boletaId(boleta),
                            "idCaja", venta.getIdCaja(),
                            "estadoVenta", venta.getEstadoVenta(),
                            "total", venta.getTotal()
                    )
            );

            return ventaConsultaService.obtenerDetalleEmpleado(venta.getId(), actor);
        } catch (BusinessException ex) {
            auditoriaFuncionalService.registrarErrorUsuario(
                    ENTIDAD_VENTA,
                    idVenta,
                    ACCION_CONFIRMAR_FISICA_EFECTIVO,
                    actor,
                    ex.getCode(),
                    ex.getMessage()
            );
            throw ex;
        } catch (RuntimeException ex) {
            AuditContextHolder.AuditContext auditContext = AuditContextHolder.getOrEmpty();
            log.error(
                    "Error técnico confirmando venta física con efectivo. idVenta={}, actorIdUsuarioMs1={}, requestId={}, correlationId={}",
                    idVenta,
                    actor == null ? null : actor.idUsuarioMs1(),
                    auditContext.requestId(),
                    auditContext.correlationId(),
                    ex
            );
            auditoriaFuncionalService.registrarErrorTecnico(
                    ENTIDAD_VENTA,
                    idVenta,
                    ACCION_CONFIRMAR_FISICA_EFECTIVO,
                    actor,
                    ex
            );
            throw internalError("No se pudo confirmar la venta física con pago en efectivo.", ex);
        }
    }

    @Override
    @Transactional
    public VentaDetailResponseDto confirmarVentaFisicaPagadaStripe(Long idVenta,
                                                                   String stripePaymentIntentId,
                                                                   AuthenticatedUserContext actor) {
        try {
            String paymentIntentId = validarPaymentIntentId(stripePaymentIntentId);
            Venta venta = resolverVentaActiva(idVenta);
            validarVentaFisica(venta);

            ventaPolicy.authorizeCrearVentaFisica(actor);
            ventaPolicy.authorizeVerVenta(actor, venta);

            if (venta.getEstadoVenta() == EstadoVenta.CONFIRMADA) {
                return ventaConsultaService.obtenerDetalleEmpleado(venta.getId(), actor);
            }

            Pago pago = pagoService.resolverPagoPorPaymentIntent(paymentIntentId);
            validarPagoAprobadoStripePresencial(pago, venta, paymentIntentId);

            venta = resolverVentaActiva(venta.getId());
            ventaValidator.validarVentaPagadaParaConfirmar(venta);
            venta.setEstadoVenta(EstadoVenta.CONFIRMADA);
            venta.setMetodoPagoPrincipal(MetodoPago.TARJETA_PRESENCIAL_STRIPE_SANDBOX);
            venta = ventaRepository.save(venta);

            BoletaDetailResponseDto boleta = emitirYProgramarBoletaFisica(venta, actor);
            Caja caja = resolverCajaParaActualizar(venta.getIdCaja());
            actualizarTotalesCajaPorPago(caja, pago);
            cajaMovimientoService.registrarMovimientoVentaTarjeta(caja, venta, pago, actor);
            ventaStockCommandService.registrarComandosConfirmacionStock(venta, actor);

            auditoriaFuncionalService.registrarExito(
                    ENTIDAD_VENTA,
                    venta.getId(),
                    ACCION_CONFIRMAR_FISICA_STRIPE,
                    actor,
                    auditMap(
                            "codigoVenta", venta.getCodigoVenta(),
                            "stripePaymentIntentId", paymentIntentId,
                            "idPago", pago.getId(),
                            "idBoleta", boletaId(boleta),
                            "idCaja", venta.getIdCaja(),
                            "estadoVenta", venta.getEstadoVenta(),
                            "total", venta.getTotal()
                    )
            );

            return ventaConsultaService.obtenerDetalleEmpleado(venta.getId(), actor);
        } catch (BusinessException ex) {
            auditoriaFuncionalService.registrarErrorUsuario(
                    ENTIDAD_VENTA,
                    idVenta,
                    ACCION_CONFIRMAR_FISICA_STRIPE,
                    actor,
                    ex.getCode(),
                    ex.getMessage()
            );
            throw ex;
        } catch (RuntimeException ex) {
            AuditContextHolder.AuditContext auditContext = AuditContextHolder.getOrEmpty();
            log.error(
                    "Error técnico confirmando venta física pagada por Stripe. idVenta={}, paymentIntentId={}, actorIdUsuarioMs1={}, requestId={}, correlationId={}",
                    idVenta,
                    stripePaymentIntentId,
                    actor == null ? null : actor.idUsuarioMs1(),
                    auditContext.requestId(),
                    auditContext.correlationId(),
                    ex
            );
            auditoriaFuncionalService.registrarErrorTecnico(
                    ENTIDAD_VENTA,
                    idVenta,
                    ACCION_CONFIRMAR_FISICA_STRIPE,
                    actor,
                    ex
            );
            throw internalError("No se pudo confirmar la venta física pagada por Stripe.", ex);
        }
    }

    @Override
    @Transactional
    public VentaDetailResponseDto anularVentaFisica(Long idVenta,
                                                    EstadoChangeRequestDto request,
                                                    AuthenticatedUserContext actor) {
        try {
            String motivo = validarMotivoAnulacion(request);
            Venta venta = resolverVentaActiva(idVenta);
            validarVentaFisica(venta);

            ventaPolicy.authorizeAnularVenta(actor, venta);
            ventaValidator.validarVentaAnulable(venta);

            EstadoVenta estadoAnterior = venta.getEstadoVenta();
            venta.setEstadoVenta(EstadoVenta.ANULADA);
            venta.setObservacion(anexarMotivo(venta.getObservacion(), motivo));
            venta = ventaRepository.save(venta);

            if (requiereComandoAnulacionStock(estadoAnterior)) {
                ventaStockCommandService.registrarComandosAnulacionStock(venta, actor);
            }

            auditoriaFuncionalService.registrarExito(
                    ENTIDAD_VENTA,
                    venta.getId(),
                    ACCION_ANULAR_FISICA,
                    actor,
                    auditMap(
                            "codigoVenta", venta.getCodigoVenta(),
                            "estadoAnterior", estadoAnterior,
                            "estadoNuevo", venta.getEstadoVenta(),
                            "motivo", motivo
                    )
            );

            return ventaConsultaService.obtenerDetalleAdmin(venta.getId(), actor);
        } catch (BusinessException ex) {
            auditoriaFuncionalService.registrarErrorUsuario(
                    ENTIDAD_VENTA,
                    idVenta,
                    ACCION_ANULAR_FISICA,
                    actor,
                    ex.getCode(),
                    ex.getMessage()
            );
            throw ex;
        } catch (RuntimeException ex) {
            AuditContextHolder.AuditContext auditContext = AuditContextHolder.getOrEmpty();
            log.error(
                    "Error técnico anulando venta física. idVenta={}, actorIdUsuarioMs1={}, requestId={}, correlationId={}",
                    idVenta,
                    actor == null ? null : actor.idUsuarioMs1(),
                    auditContext.requestId(),
                    auditContext.correlationId(),
                    ex
            );
            auditoriaFuncionalService.registrarErrorTecnico(
                    ENTIDAD_VENTA,
                    idVenta,
                    ACCION_ANULAR_FISICA,
                    actor,
                    ex
            );
            throw internalError("No se pudo anular la venta física.", ex);
        }
    }

    private MetodoPago resolverMetodoPagoFisico(VentaFisicaCreateRequestDto request) {
        if (request == null || request.metodoPagoPrincipal() == null) {
            throw new ValidationException("El método de pago de la venta física es obligatorio.");
        }

        MetodoPago metodoPago = request.metodoPagoPrincipal();
        if (!metodoPago.permitidoParaCanal(CanalVenta.FISICA)) {
            throw new ValidationException("El método de pago no está permitido para una venta física.");
        }

        return metodoPago;
    }

    private void validarMetodoSolicitud(VentaFisicaCreateRequestDto request, MetodoPago metodoEsperado) {
        MetodoPago metodoPago = resolverMetodoPagoFisico(request);
        if (metodoPago != metodoEsperado) {
            throw new ValidationException("La operación solicitada requiere método de pago " + metodoEsperado.getLabel() + ".");
        }
    }

    private Caja resolverCajaParaVenta(VentaFisicaCreateRequestDto request) {
        if (request.idCaja() != null || hasText(request.codigoCaja())) {
            return referenceResolver.resolverCajaActiva(request.idCaja(), request.codigoCaja());
        }

        return referenceResolver.resolverCajaAbiertaActual();
    }

    private Venta resolverVentaActiva(Long idVenta) {
        if (idVenta == null || idVenta <= 0) {
            throw new ValidationException("El id de venta debe ser positivo.");
        }

        return ventaRepository.findByIdAndEstadoTrue(idVenta)
                .orElseThrow(() -> NotFoundException.byId(RECURSO_VENTA, idVenta));
    }

    private Caja resolverCajaParaActualizar(Long idCaja) {
        if (idCaja == null || idCaja <= 0) {
            throw new ValidationException("La venta física debe tener una caja asociada.");
        }

        Caja caja = cajaRepository.findByIdForUpdate(idCaja)
                .filter(Caja::isActivo)
                .orElseThrow(() -> NotFoundException.byId(RECURSO_CAJA, idCaja));

        if (caja.getEstadoCaja() != EstadoCaja.ABIERTA) {
            throw new ConflictException("La caja asociada a la venta ya no se encuentra abierta.");
        }

        return caja;
    }

    private Pago resolverPagoAprobadoVenta(Long idVenta, MetodoPago metodoPago) {
        return pagoRepository.findFirstByIdVentaAndEstadoPagoInAndEstadoTrueOrderByCreatedAtDesc(
                        idVenta,
                        List.of(EstadoPago.APROBADO)
                )
                .filter(pago -> pago.getMetodoPago() == metodoPago)
                .orElseThrow(() -> new NotFoundException(
                        "No existe pago aprobado " + metodoPago.getLabel() + " para la venta indicada."
                ));
    }

    private void validarVentaFisica(Venta venta) {
        if (venta == null) {
            throw new ValidationException("La venta es obligatoria.");
        }

        if (venta.getCanalVenta() != CanalVenta.FISICA) {
            throw new ConflictException("La venta indicada no corresponde al canal físico.");
        }
    }

    private void validarMetodoVenta(Venta venta, MetodoPago metodoEsperado) {
        if (venta.getMetodoPagoPrincipal() != metodoEsperado) {
            throw new ConflictException("La venta no corresponde al método de pago esperado: " + metodoEsperado.getLabel() + ".");
        }
    }

    private void validarPagoAprobadoStripePresencial(Pago pago, Venta venta, String paymentIntentId) {
        if (pago == null) {
            throw new ValidationException("El pago Stripe es obligatorio.");
        }

        if (!Objects.equals(pago.getIdVenta(), venta.getId())) {
            throw new ConflictException("El pago Stripe no corresponde a la venta física resuelta.");
        }

        if (pago.getMetodoPago() != MetodoPago.TARJETA_PRESENCIAL_STRIPE_SANDBOX) {
            throw new ConflictException("El pago indicado no corresponde a tarjeta presencial Stripe Sandbox.");
        }

        if (!paymentIntentId.equals(pago.getStripePaymentIntentId())) {
            throw new ConflictException("El PaymentIntent no coincide con el pago resuelto.");
        }

        if (pago.getEstadoPago() != EstadoPago.APROBADO) {
            throw new ConflictException("El pago Stripe presencial todavía no se encuentra aprobado.");
        }

        validarMontoPagoContraVenta(pago.getMonto(), venta.getTotal());
    }

    private void validarMontoPagoContraVenta(BigDecimal montoPago, BigDecimal totalVenta) {
        if (montoPago == null || totalVenta == null) {
            throw new ValidationException("El pago y la venta deben tener monto definido.");
        }

        if (MoneyUtil.money(montoPago).compareTo(MoneyUtil.money(totalVenta)) != 0) {
            throw new ConflictException("El monto del pago no coincide con el total de la venta.");
        }
    }

    private BoletaDetailResponseDto emitirYProgramarBoletaFisica(Venta venta, AuthenticatedUserContext actor) {
        BoletaDetailResponseDto boleta = boletaService.emitirBoletaPorVentaConfirmada(venta.getId(), actor);

        if (boleta != null && boleta.boleta() != null && boleta.boleta().id() != null) {
            boletaService.programarCorreoBoleta(
                    boleta.boleta().id(),
                    TipoCorreo.BOLETA_COMPRA_FISICA,
                    actor
            );
        }

        return boleta;
    }

    private void actualizarTotalesCajaPorPago(Caja caja, Pago pago) {
        BigDecimal monto = MoneyUtil.money(pago.getMonto());

        if (pago.getMetodoPago() == MetodoPago.EFECTIVO) {
            caja.setMontoEsperadoEfectivo(MoneyUtil.add(caja.getMontoEsperadoEfectivo(), monto));
        } else if (pago.getMetodoPago() == MetodoPago.TARJETA_PRESENCIAL_STRIPE_SANDBOX) {
            caja.setMontoTarjeta(MoneyUtil.add(caja.getMontoTarjeta(), monto));
        } else {
            throw new ConflictException("El método de pago no corresponde a una venta física de caja.");
        }

        caja.setMontoTotalVendido(MoneyUtil.add(caja.getMontoTotalVendido(), monto));
        cajaRepository.save(caja);
    }

    private boolean requiereComandoAnulacionStock(EstadoVenta estadoAnterior) {
        return estadoAnterior == EstadoVenta.CONFIRMADA
                || estadoAnterior == EstadoVenta.PENDIENTE_SYNC_STOCK
                || estadoAnterior == EstadoVenta.ERROR_STOCK;
    }

    private Long boletaId(BoletaDetailResponseDto boleta) {
        return boleta == null || boleta.boleta() == null ? null : boleta.boleta().id();
    }

    private String validarPaymentIntentId(String stripePaymentIntentId) {
        String value = normalizarTexto(stripePaymentIntentId);

        if (value == null) {
            throw new ValidationException("El PaymentIntent de Stripe es obligatorio.");
        }

        if (value.length() > 120) {
            throw new ValidationException("El PaymentIntent de Stripe no debe superar 120 caracteres.");
        }

        return value;
    }

    private String validarMotivoAnulacion(EstadoChangeRequestDto request) {
        if (request == null) {
            throw new ValidationException("El motivo de anulación es obligatorio.");
        }

        if (request.estado() != null && Boolean.TRUE.equals(request.estado())) {
            throw new ValidationException("Para anular una venta física el estado solicitado debe ser falso.");
        }

        if (request.motivo() == null || request.motivo().isBlank()) {
            throw new ValidationException("El motivo de anulación es obligatorio.");
        }

        String motivo = request.motivo().trim();

        if (motivo.length() > 500) {
            throw new ValidationException("El motivo de anulación no debe superar 500 caracteres.");
        }

        return motivo;
    }

    private String generarCodigoVenta() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String codigo = CodigoGenerator.venta(System.currentTimeMillis() + attempt);

            if (!ventaRepository.existsByCodigoVentaIgnoreCase(codigo)) {
                return codigo;
            }
        }

        return "VEN-" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
    }

    private String normalizarMoneda(String moneda) {
        return moneda == null || moneda.isBlank() ? "PEN" : moneda.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizarTexto(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String anexarMotivo(String observacionActual, String motivo) {
        String motivoNormalizado = normalizarTexto(motivo);

        if (motivoNormalizado == null) {
            return observacionActual;
        }

        String prefijo = "Anulación física: ";
        if (observacionActual == null || observacionActual.isBlank()) {
            return truncar(prefijo + motivoNormalizado, 500);
        }

        return truncar(observacionActual.trim() + " | " + prefijo + motivoNormalizado, 500);
    }

    private String truncar(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private Map<String, Object> auditMap(Object... entries) {
        Map<String, Object> detalle = new LinkedHashMap<>();

        if (entries == null) {
            return detalle;
        }

        for (int i = 0; i + 1 < entries.length; i += 2) {
            Object key = entries[i];
            if (key != null) {
                detalle.put(String.valueOf(key), entries[i + 1]);
            }
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