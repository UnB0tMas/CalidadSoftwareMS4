// ruta: src/main/java/com/upsjb/ms4/service/impl/pago/PagoServiceImpl.java
package com.upsjb.ms4.service.impl.pago;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms4.domain.entity.pago.Pago;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.enums.EstadoPago;
import com.upsjb.ms4.domain.enums.EstadoVenta;
import com.upsjb.ms4.domain.enums.MetodoPago;
import com.upsjb.ms4.dto.pago.filter.PagoFilterDto;
import com.upsjb.ms4.dto.pago.request.PagoEfectivoRequestDto;
import com.upsjb.ms4.dto.pago.response.PagoResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.mapper.pago.PagoMapper;
import com.upsjb.ms4.policy.PagoPolicy;
import com.upsjb.ms4.repository.PagoRepository;
import com.upsjb.ms4.repository.VentaRepository;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.pago.PagoService;
import com.upsjb.ms4.shared.audit.AuditRegistrar;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.shared.pagination.PaginationService;
import com.upsjb.ms4.specification.PagoSpecification;
import com.upsjb.ms4.util.CodigoGenerator;
import com.upsjb.ms4.util.MoneyUtil;
import com.upsjb.ms4.validator.PagoValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.UUID;

@Service
public class PagoServiceImpl implements PagoService {

    private static final Logger log = LoggerFactory.getLogger(PagoServiceImpl.class);

    private static final String RECURSO_PAGO = "Pago";
    private static final String RECURSO_VENTA = "Venta";
    private static final String ENTIDAD_PAGO = "PAGO";

    private final PagoRepository pagoRepository;
    private final VentaRepository ventaRepository;
    private final PagoMapper pagoMapper;
    private final PagoValidator pagoValidator;
    private final PagoPolicy pagoPolicy;
    private final PaginationService paginationService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final AuditRegistrar auditRegistrar;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public PagoServiceImpl(PagoRepository pagoRepository,
                           VentaRepository ventaRepository,
                           PagoMapper pagoMapper,
                           PagoValidator pagoValidator,
                           PagoPolicy pagoPolicy,
                           PaginationService paginationService,
                           AuthenticatedUserResolver authenticatedUserResolver,
                           AuditRegistrar auditRegistrar,
                           ObjectMapper objectMapper,
                           Clock clock) {
        this.pagoRepository = pagoRepository;
        this.ventaRepository = ventaRepository;
        this.pagoMapper = pagoMapper;
        this.pagoValidator = pagoValidator;
        this.pagoPolicy = pagoPolicy;
        this.paginationService = paginationService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.auditRegistrar = auditRegistrar;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    @Transactional
    public PagoResponseDto registrarPagoEfectivoAprobado(Long idVenta,
                                                         PagoEfectivoRequestDto request,
                                                         AuthenticatedUserContext actor) {
        try {
            Venta venta = resolverVentaActiva(idVenta);

            pagoPolicy.authorizeRegistrarPagoEfectivo(actor, venta);
            pagoValidator.validarPagoEfectivo(venta, request);
            pagoValidator.validarNoDoblePago(
                    pagoRepository.findByIdVentaAndEstadoTrueOrderByCreatedAtDesc(venta.getId())
            );

            LocalDateTime now = LocalDateTime.now(clock);

            Pago pago = Pago.builder()
                    .idVenta(venta.getId())
                    .codigoPago(generarCodigoPago())
                    .metodoPago(MetodoPago.EFECTIVO)
                    .estadoPago(EstadoPago.APROBADO)
                    .moneda(normalizeMoneda(venta.getMoneda()))
                    .monto(MoneyUtil.money(venta.getTotal()))
                    .fechaPago(now)
                    .fechaConfirmacion(now)
                    .payloadPasarelaJson(detalleJson(auditMap(
                            "tipo", "EFECTIVO",
                            "montoRecibido", request.montoRecibido(),
                            "observacion", request.observacion()
                    )))
                    .estado(true)
                    .build();

            pago = pagoRepository.save(pago);

            marcarVentaComoPagada(venta, MetodoPago.EFECTIVO);
            ventaRepository.save(venta);

            auditRegistrar.registrarExito(
                    ENTIDAD_PAGO,
                    String.valueOf(pago.getId()),
                    "REGISTRAR_PAGO_EFECTIVO_APROBADO",
                    detalleJson(detalleAuditoria(pago, venta))
            );

            return pagoMapper.toResponse(pago);
        } catch (BusinessException ex) {
            auditRegistrar.registrarErrorUsuario(
                    ENTIDAD_PAGO,
                    idVenta == null ? null : String.valueOf(idVenta),
                    "REGISTRAR_PAGO_EFECTIVO_APROBADO",
                    detalleJson(auditMap("codigoError", ex.getCode(), "mensaje", ex.getMessage()))
            );
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico registrando pago efectivo. idVenta={}, actorIdUsuarioMs1={}",
                    idVenta,
                    actor == null ? null : actor.idUsuarioMs1(),
                    ex
            );
            auditRegistrar.registrarErrorTecnico(
                    ENTIDAD_PAGO,
                    idVenta == null ? null : String.valueOf(idVenta),
                    "REGISTRAR_PAGO_EFECTIVO_APROBADO",
                    detalleJson(auditMap("mensaje", safeMessage(ex)))
            );
            throw internalError("No se pudo registrar el pago en efectivo.", ex);
        }
    }

    @Override
    @Transactional
    public PagoResponseDto registrarPagoStripePendiente(Long idVenta,
                                                        MetodoPago metodoPago,
                                                        String paymentIntentId,
                                                        java.math.BigDecimal monto,
                                                        String payloadJson) {
        try {
            Venta venta = resolverVentaActiva(idVenta);

            Pago existente = pagoRepository.findByStripePaymentIntentIdAndEstadoTrue(paymentIntentId)
                    .orElse(null);

            if (existente != null) {
                return pagoMapper.toResponse(existente);
            }

            pagoValidator.validarPagoStripePendiente(venta, metodoPago, paymentIntentId, monto, payloadJson);
            pagoValidator.validarNoDoblePago(
                    pagoRepository.findByIdVentaAndEstadoTrueOrderByCreatedAtDesc(venta.getId())
            );

            Pago pago = Pago.builder()
                    .idVenta(venta.getId())
                    .codigoPago(generarCodigoPago())
                    .metodoPago(metodoPago)
                    .estadoPago(EstadoPago.PENDIENTE)
                    .moneda(normalizeMoneda(venta.getMoneda()))
                    .monto(MoneyUtil.money(monto))
                    .stripePaymentIntentId(paymentIntentId.trim())
                    .stripeStatus("requires_payment_method")
                    .payloadPasarelaJson(trimToNull(payloadJson))
                    .estado(true)
                    .build();

            pago = pagoRepository.save(pago);

            venta.setMetodoPagoPrincipal(metodoPago);
            ventaRepository.save(venta);

            auditRegistrar.registrarExito(
                    ENTIDAD_PAGO,
                    String.valueOf(pago.getId()),
                    "REGISTRAR_PAGO_STRIPE_PENDIENTE",
                    detalleJson(detalleAuditoria(pago, venta))
            );

            return pagoMapper.toResponse(pago);
        } catch (BusinessException ex) {
            auditRegistrar.registrarErrorUsuario(
                    ENTIDAD_PAGO,
                    idVenta == null ? null : String.valueOf(idVenta),
                    "REGISTRAR_PAGO_STRIPE_PENDIENTE",
                    detalleJson(auditMap("codigoError", ex.getCode(), "mensaje", ex.getMessage()))
            );
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico registrando pago Stripe pendiente. idVenta={}, paymentIntentId={}",
                    idVenta,
                    paymentIntentId,
                    ex
            );
            auditRegistrar.registrarErrorTecnico(
                    ENTIDAD_PAGO,
                    idVenta == null ? null : String.valueOf(idVenta),
                    "REGISTRAR_PAGO_STRIPE_PENDIENTE",
                    detalleJson(auditMap("mensaje", safeMessage(ex)))
            );
            throw internalError("No se pudo registrar el pago Stripe pendiente.", ex);
        }
    }

    @Override
    @Transactional
    public PagoResponseDto confirmarPagoStripe(String paymentIntentId,
                                               String chargeId,
                                               String stripeStatus,
                                               String payloadJson) {
        try {
            Pago pago = resolverPagoPorPaymentIntent(paymentIntentId);

            if (pago.getEstadoPago() == EstadoPago.APROBADO) {
                return pagoMapper.toResponse(pago);
            }

            pagoValidator.validarConfirmarPagoStripe(pago, paymentIntentId, chargeId, stripeStatus, payloadJson);

            LocalDateTime now = LocalDateTime.now(clock);

            pago.setEstadoPago(EstadoPago.APROBADO);
            pago.setStripeChargeId(trimToNull(chargeId));
            pago.setStripeStatus(trimToNull(stripeStatus));
            pago.setPayloadPasarelaJson(trimToNull(payloadJson));
            pago.setFechaPago(now);
            pago.setFechaConfirmacion(now);

            pago = pagoRepository.save(pago);

            Venta venta = resolverVentaActiva(pago.getIdVenta());
            marcarVentaComoPagada(venta, pago.getMetodoPago());
            ventaRepository.save(venta);

            auditRegistrar.registrarExito(
                    ENTIDAD_PAGO,
                    String.valueOf(pago.getId()),
                    "CONFIRMAR_PAGO_STRIPE",
                    detalleJson(detalleAuditoria(pago, venta))
            );

            return pagoMapper.toResponse(pago);
        } catch (BusinessException ex) {
            auditRegistrar.registrarErrorUsuario(
                    ENTIDAD_PAGO,
                    paymentIntentId,
                    "CONFIRMAR_PAGO_STRIPE",
                    detalleJson(auditMap("codigoError", ex.getCode(), "mensaje", ex.getMessage()))
            );
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico confirmando pago Stripe. paymentIntentId={}, chargeId={}",
                    paymentIntentId,
                    chargeId,
                    ex
            );
            auditRegistrar.registrarErrorTecnico(
                    ENTIDAD_PAGO,
                    paymentIntentId,
                    "CONFIRMAR_PAGO_STRIPE",
                    detalleJson(auditMap("mensaje", safeMessage(ex)))
            );
            throw internalError("No se pudo confirmar el pago Stripe.", ex);
        }
    }

    @Override
    @Transactional
    public PagoResponseDto rechazarPagoStripe(String paymentIntentId,
                                              String stripeStatus,
                                              String motivo,
                                              String payloadJson) {
        try {
            Pago pago = resolverPagoPorPaymentIntent(paymentIntentId);

            if (pago.getEstadoPago() == EstadoPago.RECHAZADO) {
                return pagoMapper.toResponse(pago);
            }

            pagoValidator.validarRechazarPagoStripe(pago, paymentIntentId, stripeStatus, motivo, payloadJson);

            pago.setEstadoPago(EstadoPago.RECHAZADO);
            pago.setStripeStatus(trimToNull(stripeStatus));
            pago.setPayloadPasarelaJson(trimToNull(payloadJson));
            pago.setFechaConfirmacion(LocalDateTime.now(clock));

            pago = pagoRepository.save(pago);

            auditRegistrar.registrarExito(
                    ENTIDAD_PAGO,
                    String.valueOf(pago.getId()),
                    "RECHAZAR_PAGO_STRIPE",
                    detalleJson(auditMap(
                            "paymentIntentId", paymentIntentId,
                            "stripeStatus", stripeStatus,
                            "motivo", motivo
                    ))
            );

            return pagoMapper.toResponse(pago);
        } catch (BusinessException ex) {
            auditRegistrar.registrarErrorUsuario(
                    ENTIDAD_PAGO,
                    paymentIntentId,
                    "RECHAZAR_PAGO_STRIPE",
                    detalleJson(auditMap("codigoError", ex.getCode(), "mensaje", ex.getMessage()))
            );
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico rechazando pago Stripe. paymentIntentId={}, stripeStatus={}",
                    paymentIntentId,
                    stripeStatus,
                    ex
            );
            auditRegistrar.registrarErrorTecnico(
                    ENTIDAD_PAGO,
                    paymentIntentId,
                    "RECHAZAR_PAGO_STRIPE",
                    detalleJson(auditMap("mensaje", safeMessage(ex)))
            );
            throw internalError("No se pudo rechazar el pago Stripe.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponseDto obtenerPorId(Long idPago) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        pagoValidator.validarIdPago(idPago);

        Pago pago = pagoRepository.findById(idPago)
                .filter(Pago::isActivo)
                .orElseThrow(() -> NotFoundException.byId(RECURSO_PAGO, idPago));

        Venta venta = resolverVentaActiva(pago.getIdVenta());
        pagoPolicy.authorizeObtenerPago(actor, venta);

        return pagoMapper.toDetailResponse(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponseDto obtenerPorVenta(Long idVenta) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        pagoValidator.validarIdVenta(idVenta);

        Venta venta = resolverVentaActiva(idVenta);
        pagoPolicy.authorizeObtenerPago(actor, venta);

        Pago pago = pagoRepository.findByIdVentaAndEstadoTrueOrderByCreatedAtDesc(idVenta)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No se encontró pago activo para la venta: " + idVenta));

        return pagoMapper.toDetailResponse(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<PagoResponseDto> listar(PagoFilterDto filter, PageRequestDto page) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        pagoPolicy.authorizeListarPagos(actor);
        pagoValidator.validarFiltro(filter);

        Page<Pago> pagos = pagoRepository.findAll(
                PagoSpecification.build(filter),
                paginationService.toPageable(page, "fechaPago")
        );

        return paginationService.toPageResponse(pagos, pagoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Pago resolverPagoPorPaymentIntent(String paymentIntentId) {
        String normalized = trimToNull(paymentIntentId);

        if (normalized == null) {
            throw new NotFoundException("No se encontró pago porque el PaymentIntent es obligatorio.");
        }

        return pagoRepository.findByStripePaymentIntentIdAndEstadoTrue(normalized)
                .orElseThrow(() -> new NotFoundException(
                        "No se encontró pago activo asociado al PaymentIntent: " + normalized
                ));
    }

    private Venta resolverVentaActiva(Long idVenta) {
        pagoValidator.validarIdVenta(idVenta);

        return ventaRepository.findByIdAndEstadoTrue(idVenta)
                .orElseThrow(() -> NotFoundException.byId(RECURSO_VENTA, idVenta));
    }

    private void marcarVentaComoPagada(Venta venta, MetodoPago metodoPago) {
        if (venta.getEstadoVenta() != EstadoVenta.CONFIRMADA
                && venta.getEstadoVenta() != EstadoVenta.ANULADA
                && venta.getEstadoVenta() != EstadoVenta.RECHAZADA) {
            venta.setEstadoVenta(EstadoVenta.PAGADA);
        }

        venta.setMetodoPagoPrincipal(metodoPago);
    }

    private String generarCodigoPago() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String codigo = CodigoGenerator.pago(System.currentTimeMillis() + attempt);

            if (!pagoRepository.existsByCodigoPagoIgnoreCase(codigo)) {
                return codigo;
            }
        }

        return "PAG-" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
    }

    private String normalizeMoneda(String moneda) {
        return moneda == null || moneda.isBlank() ? "PEN" : moneda.trim().toUpperCase(Locale.ROOT);
    }

    private Map<String, Object> detalleAuditoria(Pago pago, Venta venta) {
        Map<String, Object> detalle = new LinkedHashMap<>();
        detalle.put("idPago", pago.getId());
        detalle.put("codigoPago", pago.getCodigoPago());
        detalle.put("idVenta", venta.getId());
        detalle.put("codigoVenta", venta.getCodigoVenta());
        detalle.put("metodoPago", pago.getMetodoPago());
        detalle.put("estadoPago", pago.getEstadoPago());
        detalle.put("monto", pago.getMonto());
        detalle.put("stripePaymentIntentId", pago.getStripePaymentIntentId());
        return detalle;
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

    private String detalleJson(Object detalle) {
        try {
            return objectMapper.writeValueAsString(detalle == null ? Map.of() : detalle);
        } catch (JsonProcessingException ex) {
            return "{\"detalle\":\"no_serializable\"}";
        }
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String safeMessage(Throwable throwable) {
        return throwable == null || throwable.getMessage() == null
                ? "Error técnico no especificado."
                : throwable.getMessage();
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