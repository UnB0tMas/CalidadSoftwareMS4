// ruta: src/main/java/com/upsjb/ms4/service/impl/venta/VentaOnlineServiceImpl.java
package com.upsjb.ms4.service.impl.venta;

import com.upsjb.ms4.domain.entity.config.ConfiguracionTributariaVersion;
import com.upsjb.ms4.domain.entity.pago.Pago;
import com.upsjb.ms4.domain.entity.snapshot.ClienteSnapshotMs2;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.enums.CanalVenta;
import com.upsjb.ms4.domain.enums.EstadoPago;
import com.upsjb.ms4.domain.enums.EstadoVenta;
import com.upsjb.ms4.domain.enums.MetodoPago;
import com.upsjb.ms4.domain.enums.TipoCorreo;
import com.upsjb.ms4.dto.boleta.response.BoletaDetailResponseDto;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms4.dto.venta.request.VentaCalculoPreviewRequestDto;
import com.upsjb.ms4.dto.venta.request.VentaOnlineCreateRequestDto;
import com.upsjb.ms4.dto.venta.response.VentaCalculoPreviewResponseDto;
import com.upsjb.ms4.dto.venta.response.VentaDetailResponseDto;
import com.upsjb.ms4.policy.VentaPolicy;
import com.upsjb.ms4.repository.VentaDetalleRepository;
import com.upsjb.ms4.repository.VentaRepository;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.roles.SecurityRoles;
import com.upsjb.ms4.service.contract.auditoria.AuditoriaFuncionalService;
import com.upsjb.ms4.service.contract.boleta.BoletaService;
import com.upsjb.ms4.service.contract.config.ConfiguracionTributariaService;
import com.upsjb.ms4.service.contract.contingencia.ContingenciaService;
import com.upsjb.ms4.service.contract.pago.PagoService;
import com.upsjb.ms4.service.contract.reference.Ms4ReferenceResolverService;
import com.upsjb.ms4.service.contract.venta.VentaCalculoResultado;
import com.upsjb.ms4.service.contract.venta.VentaCalculoService;
import com.upsjb.ms4.service.contract.venta.VentaConsultaService;
import com.upsjb.ms4.service.contract.venta.VentaOnlineService;
import com.upsjb.ms4.service.contract.venta.VentaStockCommandService;
import com.upsjb.ms4.shared.audit.AuditContextHolder;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.shared.exception.ConflictException;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.shared.exception.ValidationException;
import com.upsjb.ms4.util.CodigoGenerator;
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class VentaOnlineServiceImpl implements VentaOnlineService {

    private static final Logger log = LoggerFactory.getLogger(VentaOnlineServiceImpl.class);

    private static final String RECURSO_VENTA = "Venta";
    private static final String ENTIDAD_VENTA = "VENTA";
    private static final String ACCION_CREAR_ONLINE = "VENTA_ONLINE_CREADA_PENDIENTE_PAGO";
    private static final String ACCION_CONFIRMAR_ONLINE = "VENTA_ONLINE_CONFIRMADA_STRIPE";
    private static final String ACCION_RECHAZAR_ONLINE = "VENTA_ONLINE_RECHAZADA_STRIPE";
    private static final String ACCION_ANULAR_ONLINE = "VENTA_ONLINE_ANULADA";
    private static final Long SYSTEM_ACTOR_ID_USUARIO_MS1 = 1L;

    private final VentaRepository ventaRepository;
    private final VentaDetalleRepository ventaDetalleRepository;
    private final VentaCalculoService ventaCalculoService;
    private final VentaConsultaService ventaConsultaService;
    private final VentaStockCommandService ventaStockCommandService;
    private final ConfiguracionTributariaService configuracionTributariaService;
    private final Ms4ReferenceResolverService referenceResolver;
    private final PagoService pagoService;
    private final BoletaService boletaService;
    private final ContingenciaService contingenciaService;
    private final VentaPolicy ventaPolicy;
    private final VentaValidator ventaValidator;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final Clock clock;

    public VentaOnlineServiceImpl(VentaRepository ventaRepository,
                                  VentaDetalleRepository ventaDetalleRepository,
                                  VentaCalculoService ventaCalculoService,
                                  VentaConsultaService ventaConsultaService,
                                  VentaStockCommandService ventaStockCommandService,
                                  ConfiguracionTributariaService configuracionTributariaService,
                                  Ms4ReferenceResolverService referenceResolver,
                                  PagoService pagoService,
                                  BoletaService boletaService,
                                  ContingenciaService contingenciaService,
                                  VentaPolicy ventaPolicy,
                                  VentaValidator ventaValidator,
                                  AuditoriaFuncionalService auditoriaFuncionalService,
                                  Clock clock) {
        this.ventaRepository = ventaRepository;
        this.ventaDetalleRepository = ventaDetalleRepository;
        this.ventaCalculoService = ventaCalculoService;
        this.ventaConsultaService = ventaConsultaService;
        this.ventaStockCommandService = ventaStockCommandService;
        this.configuracionTributariaService = configuracionTributariaService;
        this.referenceResolver = referenceResolver;
        this.pagoService = pagoService;
        this.boletaService = boletaService;
        this.contingenciaService = contingenciaService;
        this.ventaPolicy = ventaPolicy;
        this.ventaValidator = ventaValidator;
        this.auditoriaFuncionalService = auditoriaFuncionalService;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public VentaCalculoPreviewResponseDto previsualizarVentaOnline(VentaCalculoPreviewRequestDto request,
                                                                   AuthenticatedUserContext actor) {
        try {
            return ventaCalculoService.calcularPreview(
                    request,
                    CanalVenta.ONLINE,
                    actor
            );
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            AuditContextHolder.AuditContext auditContext = AuditContextHolder.getOrEmpty();
            log.error(
                    "Error técnico previsualizando venta online. actorIdUsuarioMs1={}, requestId={}, correlationId={}",
                    actor == null ? null : actor.idUsuarioMs1(),
                    auditContext.requestId(),
                    auditContext.correlationId(),
                    ex
            );
            throw internalError("No se pudo previsualizar la compra online.", ex);
        }
    }

    @Override
    @Transactional
    public VentaDetailResponseDto crearVentaOnlinePendientePago(VentaOnlineCreateRequestDto request,
                                                                AuthenticatedUserContext actor) {
        try {
            ventaPolicy.authorizeCrearVentaOnline(actor);
            contingenciaService.validarVentaPermitidaPorContingencia();

            ClienteSnapshotMs2 cliente = referenceResolver.resolverClienteActivoPorUsuarioMs1(actor.idUsuarioMs1());
            ventaValidator.validarCrearVentaOnline(request, cliente, MetodoPago.TARJETA_ONLINE_STRIPE_SANDBOX);

            LocalDateTime now = LocalDateTime.now(clock);
            ConfiguracionTributariaVersion igv = configuracionTributariaService.resolverIgvVigenteParaVenta();

            VentaCalculoResultado calculo = ventaCalculoService.calcularVenta(
                    request.detalles(),
                    CanalVenta.ONLINE,
                    now
            );

            if (!Boolean.TRUE.equals(calculo.stockSuficiente())) {
                throw new ConflictException("No existe stock suficiente para completar la venta online.");
            }

            Venta venta = Venta.builder()
                    .codigoVenta(generarCodigoVenta())
                    .canalVenta(CanalVenta.ONLINE)
                    .estadoVenta(EstadoVenta.PENDIENTE_PAGO)
                    .idClienteSnapshot(cliente.getId())
                    .idClienteMs2(cliente.getIdClienteMs2())
                    .idUsuarioClienteMs1(cliente.getIdUsuarioMs1())
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
                    .metodoPagoPrincipal(MetodoPago.TARJETA_ONLINE_STRIPE_SANDBOX)
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
                    ACCION_CREAR_ONLINE,
                    actor,
                    auditMap(
                            "codigoVenta", venta.getCodigoVenta(),
                            "canalVenta", venta.getCanalVenta(),
                            "estadoVenta", venta.getEstadoVenta(),
                            "total", venta.getTotal()
                    )
            );

            return ventaConsultaService.obtenerDetalleCliente(venta.getId(), actor);
        } catch (BusinessException ex) {
            auditoriaFuncionalService.registrarErrorUsuario(
                    ENTIDAD_VENTA,
                    null,
                    ACCION_CREAR_ONLINE,
                    actor,
                    ex.getCode(),
                    ex.getMessage()
            );
            throw ex;
        } catch (RuntimeException ex) {
            AuditContextHolder.AuditContext auditContext = AuditContextHolder.getOrEmpty();
            log.error(
                    "Error técnico creando venta online pendiente de pago. actorIdUsuarioMs1={}, requestId={}, correlationId={}",
                    actor == null ? null : actor.idUsuarioMs1(),
                    auditContext.requestId(),
                    auditContext.correlationId(),
                    ex
            );
            auditoriaFuncionalService.registrarErrorTecnico(
                    ENTIDAD_VENTA,
                    null,
                    ACCION_CREAR_ONLINE,
                    actor,
                    ex
            );
            throw internalError("No se pudo crear la venta online.", ex);
        }
    }

    @Override
    @Transactional
    public VentaDetailResponseDto confirmarVentaOnlinePagadaStripe(String stripePaymentIntentId) {
        AuthenticatedUserContext systemActor = systemActor();

        try {
            String paymentIntentId = validarPaymentIntentId(stripePaymentIntentId);
            Pago pago = pagoService.resolverPagoPorPaymentIntent(paymentIntentId);
            Venta venta = resolverVentaActiva(pago.getIdVenta());

            validarVentaOnline(venta);
            validarPagoAprobadoOnline(pago, venta, paymentIntentId);

            if (venta.getEstadoVenta() == EstadoVenta.CONFIRMADA) {
                return ventaConsultaService.obtenerDetalleAdmin(venta.getId(), systemActor);
            }

            ventaValidator.validarVentaPagadaParaConfirmar(venta);

            venta.setEstadoVenta(EstadoVenta.CONFIRMADA);
            venta.setMetodoPagoPrincipal(pago.getMetodoPago());
            venta = ventaRepository.save(venta);

            BoletaDetailResponseDto boleta = boletaService.emitirBoletaPorVentaConfirmada(venta.getId(), systemActor);
            if (boleta != null && boleta.boleta() != null && boleta.boleta().id() != null) {
                boletaService.programarCorreoBoleta(
                        boleta.boleta().id(),
                        TipoCorreo.BOLETA_COMPRA_ONLINE,
                        systemActor
                );
            }

            ventaStockCommandService.registrarComandosConfirmacionStock(venta, systemActor);

            auditoriaFuncionalService.registrarExito(
                    ENTIDAD_VENTA,
                    venta.getId(),
                    ACCION_CONFIRMAR_ONLINE,
                    systemActor,
                    auditMap(
                            "codigoVenta", venta.getCodigoVenta(),
                            "stripePaymentIntentId", paymentIntentId,
                            "idPago", pago.getId(),
                            "estadoVenta", venta.getEstadoVenta()
                    )
            );

            return ventaConsultaService.obtenerDetalleAdmin(venta.getId(), systemActor);
        } catch (BusinessException ex) {
            auditoriaFuncionalService.registrarErrorUsuario(
                    ENTIDAD_VENTA,
                    null,
                    ACCION_CONFIRMAR_ONLINE,
                    systemActor,
                    ex.getCode(),
                    ex.getMessage()
            );
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico confirmando venta online pagada por Stripe. paymentIntentId={}",
                    stripePaymentIntentId,
                    ex
            );
            auditoriaFuncionalService.registrarErrorTecnico(
                    ENTIDAD_VENTA,
                    null,
                    ACCION_CONFIRMAR_ONLINE,
                    systemActor,
                    ex
            );
            throw internalError("No se pudo confirmar la venta online pagada por Stripe.", ex);
        }
    }

    @Override
    @Transactional
    public VentaDetailResponseDto rechazarVentaOnlinePorStripe(String stripePaymentIntentId, String motivo) {
        AuthenticatedUserContext systemActor = systemActor();

        try {
            String paymentIntentId = validarPaymentIntentId(stripePaymentIntentId);
            Pago pago = pagoService.resolverPagoPorPaymentIntent(paymentIntentId);
            Venta venta = resolverVentaActiva(pago.getIdVenta());

            validarVentaOnline(venta);
            validarPagoOnlinePerteneceAVenta(pago, venta, paymentIntentId);

            if (pago.getEstadoPago() == EstadoPago.APROBADO || venta.getEstadoVenta() == EstadoVenta.CONFIRMADA) {
                auditoriaFuncionalService.registrarExito(
                        ENTIDAD_VENTA,
                        venta.getId(),
                        ACCION_RECHAZAR_ONLINE,
                        systemActor,
                        auditMap(
                                "codigoVenta", venta.getCodigoVenta(),
                                "stripePaymentIntentId", paymentIntentId,
                                "idPago", pago.getId(),
                                "estadoVenta", venta.getEstadoVenta(),
                                "estadoPago", pago.getEstadoPago(),
                                "resultado", "IGNORADO_POR_PAGO_APROBADO"
                        )
                );

                return ventaConsultaService.obtenerDetalleAdmin(venta.getId(), systemActor);
            }

            if (pago.getEstadoPago() != EstadoPago.RECHAZADO) {
                pagoService.rechazarPagoStripe(
                        paymentIntentId,
                        "payment_failed",
                        truncar(normalizarTexto(motivo), 500),
                        null
                );
                pago = pagoService.resolverPagoPorPaymentIntent(paymentIntentId);
            }

            if (venta.getEstadoVenta() != EstadoVenta.RECHAZADA
                    && venta.getEstadoVenta() != EstadoVenta.ANULADA) {
                venta.setEstadoVenta(EstadoVenta.RECHAZADA);
                venta = ventaRepository.save(venta);
            }

            auditoriaFuncionalService.registrarExito(
                    ENTIDAD_VENTA,
                    venta.getId(),
                    ACCION_RECHAZAR_ONLINE,
                    systemActor,
                    auditMap(
                            "codigoVenta", venta.getCodigoVenta(),
                            "stripePaymentIntentId", paymentIntentId,
                            "idPago", pago.getId(),
                            "motivo", normalizarTexto(motivo),
                            "estadoVenta", venta.getEstadoVenta(),
                            "estadoPago", pago.getEstadoPago()
                    )
            );

            return ventaConsultaService.obtenerDetalleAdmin(venta.getId(), systemActor);
        } catch (BusinessException ex) {
            auditoriaFuncionalService.registrarErrorUsuario(
                    ENTIDAD_VENTA,
                    null,
                    ACCION_RECHAZAR_ONLINE,
                    systemActor,
                    ex.getCode(),
                    ex.getMessage()
            );
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico rechazando venta online por Stripe. paymentIntentId={}",
                    stripePaymentIntentId,
                    ex
            );
            auditoriaFuncionalService.registrarErrorTecnico(
                    ENTIDAD_VENTA,
                    null,
                    ACCION_RECHAZAR_ONLINE,
                    systemActor,
                    ex
            );
            throw internalError("No se pudo rechazar la venta online por Stripe.", ex);
        }
    }

    @Override
    @Transactional
    public VentaDetailResponseDto anularVentaOnline(Long idVenta,
                                                    EstadoChangeRequestDto request,
                                                    AuthenticatedUserContext actor) {
        try {
            String motivo = validarMotivoAnulacion(request);
            Venta venta = resolverVentaActiva(idVenta);
            validarVentaOnline(venta);

            ventaPolicy.authorizeAnularVenta(actor, venta);
            ventaValidator.validarVentaAnulable(venta);

            EstadoVenta estadoAnterior = venta.getEstadoVenta();
            venta.setEstadoVenta(EstadoVenta.ANULADA);
            venta.setObservacion(anexarMotivo(venta.getObservacion(), motivo));
            venta = ventaRepository.save(venta);

            if (estadoAnterior == EstadoVenta.CONFIRMADA || estadoAnterior == EstadoVenta.PENDIENTE_SYNC_STOCK) {
                ventaStockCommandService.registrarComandosAnulacionStock(venta, actor);
            }

            auditoriaFuncionalService.registrarExito(
                    ENTIDAD_VENTA,
                    venta.getId(),
                    ACCION_ANULAR_ONLINE,
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
                    ACCION_ANULAR_ONLINE,
                    actor,
                    ex.getCode(),
                    ex.getMessage()
            );
            throw ex;
        } catch (RuntimeException ex) {
            AuditContextHolder.AuditContext auditContext = AuditContextHolder.getOrEmpty();
            log.error(
                    "Error técnico anulando venta online. idVenta={}, actorIdUsuarioMs1={}, requestId={}, correlationId={}",
                    idVenta,
                    actor == null ? null : actor.idUsuarioMs1(),
                    auditContext.requestId(),
                    auditContext.correlationId(),
                    ex
            );
            auditoriaFuncionalService.registrarErrorTecnico(
                    ENTIDAD_VENTA,
                    idVenta,
                    ACCION_ANULAR_ONLINE,
                    actor,
                    ex
            );
            throw internalError("No se pudo anular la venta online.", ex);
        }
    }

    private Venta resolverVentaActiva(Long idVenta) {
        if (idVenta == null || idVenta <= 0) {
            throw new ValidationException("El id de venta debe ser positivo.");
        }

        return ventaRepository.findByIdAndEstadoTrue(idVenta)
                .orElseThrow(() -> NotFoundException.byId(RECURSO_VENTA, idVenta));
    }

    private void validarVentaOnline(Venta venta) {
        if (venta == null) {
            throw new ValidationException("La venta es obligatoria.");
        }

        if (venta.getCanalVenta() != CanalVenta.ONLINE) {
            throw new ConflictException("La venta indicada no corresponde al canal online.");
        }
    }

    private void validarPagoAprobadoOnline(Pago pago, Venta venta, String paymentIntentId) {
        validarPagoOnlinePerteneceAVenta(pago, venta, paymentIntentId);

        if (pago.getEstadoPago() != EstadoPago.APROBADO) {
            throw new ConflictException("El pago Stripe online todavía no se encuentra aprobado.");
        }
    }

    private void validarPagoOnlinePerteneceAVenta(Pago pago, Venta venta, String paymentIntentId) {
        if (pago == null) {
            throw new ValidationException("El pago Stripe es obligatorio.");
        }

        if (venta == null) {
            throw new ValidationException("La venta asociada al pago es obligatoria.");
        }

        if (!Objects.equals(pago.getIdVenta(), venta.getId())) {
            throw new ConflictException("El pago Stripe no corresponde a la venta resuelta.");
        }

        if (pago.getMetodoPago() != MetodoPago.TARJETA_ONLINE_STRIPE_SANDBOX) {
            throw new ConflictException("El pago indicado no corresponde a una compra online con Stripe Sandbox.");
        }

        if (!paymentIntentId.equals(pago.getStripePaymentIntentId())) {
            throw new ConflictException("El PaymentIntent no coincide con el pago resuelto.");
        }

        validarMontoPagoContraVenta(pago.getMonto(), venta.getTotal());
    }

    private void validarMontoPagoContraVenta(BigDecimal montoPago, BigDecimal totalVenta) {
        if (montoPago == null || totalVenta == null) {
            throw new ValidationException("El pago y la venta deben tener monto definido.");
        }

        if (montoPago.compareTo(totalVenta) != 0) {
            throw new ConflictException("El monto del pago Stripe no coincide con el total de la venta.");
        }
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
        if (request == null || request.motivo() == null || request.motivo().isBlank()) {
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

        String prefijo = "Anulación online: ";
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

    private AuthenticatedUserContext systemActor() {
        return new AuthenticatedUserContext(
                SYSTEM_ACTOR_ID_USUARIO_MS1,
                "stripe-webhook-ms4",
                "system@ms4.local",
                SecurityRoles.ADMIN,
                Set.of(SecurityRoles.ROLE_ADMIN),
                null,
                "system"
        );
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