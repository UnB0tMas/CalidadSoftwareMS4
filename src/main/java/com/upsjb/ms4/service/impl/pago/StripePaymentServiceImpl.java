// ruta: src/main/java/com/upsjb/ms4/service/impl/pago/StripePaymentServiceImpl.java
package com.upsjb.ms4.service.impl.pago;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.PaymentIntent;
import com.upsjb.ms4.domain.entity.pago.Pago;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.enums.EstadoPago;
import com.upsjb.ms4.domain.enums.MetodoPago;
import com.upsjb.ms4.dto.lookup.LookupItemResponseDto;
import com.upsjb.ms4.dto.pago.request.PagoStripeOnlineRequestDto;
import com.upsjb.ms4.dto.pago.request.PagoStripePresencialRequestDto;
import com.upsjb.ms4.dto.pago.response.PagoResponseDto;
import com.upsjb.ms4.dto.pago.response.StripePaymentIntentResponseDto;
import com.upsjb.ms4.integration.stripe.StripeClient;
import com.upsjb.ms4.integration.stripe.StripePaymentIntentClient;
import com.upsjb.ms4.policy.PagoPolicy;
import com.upsjb.ms4.repository.PagoRepository;
import com.upsjb.ms4.repository.VentaRepository;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.service.contract.pago.PagoService;
import com.upsjb.ms4.service.contract.pago.StripePaymentService;
import com.upsjb.ms4.shared.audit.AuditRegistrar;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.shared.exception.ConflictException;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.shared.exception.StripePaymentException;
import com.upsjb.ms4.validator.PagoValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StripePaymentServiceImpl implements StripePaymentService {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentServiceImpl.class);

    private static final String RECURSO_VENTA = "Venta";
    private static final String ENTIDAD_STRIPE = "STRIPE_PAYMENT";

    private final VentaRepository ventaRepository;
    private final PagoRepository pagoRepository;
    private final PagoService pagoService;
    private final PagoValidator pagoValidator;
    private final PagoPolicy pagoPolicy;
    private final StripeClient stripeClient;
    private final StripePaymentIntentClient stripePaymentIntentClient;
    private final AuditRegistrar auditRegistrar;
    private final ObjectMapper objectMapper;

    public StripePaymentServiceImpl(VentaRepository ventaRepository,
                                    PagoRepository pagoRepository,
                                    PagoService pagoService,
                                    PagoValidator pagoValidator,
                                    PagoPolicy pagoPolicy,
                                    StripeClient stripeClient,
                                    StripePaymentIntentClient stripePaymentIntentClient,
                                    AuditRegistrar auditRegistrar,
                                    ObjectMapper objectMapper) {
        this.ventaRepository = ventaRepository;
        this.pagoRepository = pagoRepository;
        this.pagoService = pagoService;
        this.pagoValidator = pagoValidator;
        this.pagoPolicy = pagoPolicy;
        this.stripeClient = stripeClient;
        this.stripePaymentIntentClient = stripePaymentIntentClient;
        this.auditRegistrar = auditRegistrar;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public StripePaymentIntentResponseDto crearPaymentIntentOnline(PagoStripeOnlineRequestDto request,
                                                                   AuthenticatedUserContext actor) {
        try {
            if (request == null) {
                throw new StripePaymentException("La solicitud de PaymentIntent online es obligatoria.");
            }

            Venta venta = resolverVentaActiva(request.idVenta());

            pagoPolicy.authorizeCrearPaymentIntentOnline(actor, venta);
            pagoValidator.validarPagoStripeOnline(venta, request);

            StripePaymentIntentResponseDto existente = resolverPaymentIntentExistente(venta, actor);
            if (existente != null) {
                return existente;
            }

            PaymentIntent paymentIntent = crearPaymentIntent(
                    venta,
                    MetodoPago.TARJETA_ONLINE_STRIPE_SANDBOX,
                    "Pago online venta " + venta.getCodigoVenta(),
                    request.returnUrl()
            );

            PagoResponseDto pago = pagoService.registrarPagoStripePendiente(
                    venta.getId(),
                    MetodoPago.TARJETA_ONLINE_STRIPE_SANDBOX,
                    paymentIntent.getId(),
                    venta.getTotal(),
                    paymentIntentPayloadJson(paymentIntent, venta, MetodoPago.TARJETA_ONLINE_STRIPE_SANDBOX)
            );

            auditRegistrar.registrarExito(
                    ENTIDAD_STRIPE,
                    paymentIntent.getId(),
                    "CREAR_PAYMENT_INTENT_ONLINE",
                    detalleJson(Map.of(
                            "idVenta", venta.getId(),
                            "codigoVenta", venta.getCodigoVenta(),
                            "idPago", pago.id()
                    ))
            );

            return construirResponse(pago, paymentIntent);
        } catch (BusinessException ex) {
            auditRegistrar.registrarErrorUsuario(
                    ENTIDAD_STRIPE,
                    request == null || request.idVenta() == null ? null : String.valueOf(request.idVenta()),
                    "CREAR_PAYMENT_INTENT_ONLINE",
                    detalleJson(Map.of("codigoError", ex.getCode(), "mensaje", ex.getMessage()))
            );
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Error técnico creando PaymentIntent online. idVenta={}, actorIdUsuarioMs1={}",
                    request == null ? null : request.idVenta(),
                    actor == null ? null : actor.idUsuarioMs1(),
                    ex);
            auditRegistrar.registrarErrorTecnico(
                    ENTIDAD_STRIPE,
                    request == null || request.idVenta() == null ? null : String.valueOf(request.idVenta()),
                    "CREAR_PAYMENT_INTENT_ONLINE",
                    detalleJson(Map.of("mensaje", safeMessage(ex)))
            );
            throw internalError("No se pudo crear el PaymentIntent online.", ex);
        }
    }

    @Override
    @Transactional
    public StripePaymentIntentResponseDto crearPaymentIntentPresencial(Long idVenta,
                                                                       PagoStripePresencialRequestDto request,
                                                                       AuthenticatedUserContext actor) {
        try {
            Venta venta = resolverVentaActiva(idVenta);

            pagoPolicy.authorizeCrearPaymentIntentPresencial(actor, venta);
            pagoValidator.validarPagoStripePresencial(venta, request);

            StripePaymentIntentResponseDto existente = resolverPaymentIntentExistente(venta, actor);
            if (existente != null) {
                return existente;
            }

            PaymentIntent paymentIntent = crearPaymentIntent(
                    venta,
                    MetodoPago.TARJETA_PRESENCIAL_STRIPE_SANDBOX,
                    firstNonBlank(request == null ? null : request.descripcion(), "Pago presencial venta " + venta.getCodigoVenta()),
                    request == null ? null : request.returnUrl()
            );

            PagoResponseDto pago = pagoService.registrarPagoStripePendiente(
                    venta.getId(),
                    MetodoPago.TARJETA_PRESENCIAL_STRIPE_SANDBOX,
                    paymentIntent.getId(),
                    venta.getTotal(),
                    paymentIntentPayloadJson(paymentIntent, venta, MetodoPago.TARJETA_PRESENCIAL_STRIPE_SANDBOX)
            );

            auditRegistrar.registrarExito(
                    ENTIDAD_STRIPE,
                    paymentIntent.getId(),
                    "CREAR_PAYMENT_INTENT_PRESENCIAL",
                    detalleJson(Map.of(
                            "idVenta", venta.getId(),
                            "codigoVenta", venta.getCodigoVenta(),
                            "idPago", pago.id()
                    ))
            );

            return construirResponse(pago, paymentIntent);
        } catch (BusinessException ex) {
            auditRegistrar.registrarErrorUsuario(
                    ENTIDAD_STRIPE,
                    idVenta == null ? null : String.valueOf(idVenta),
                    "CREAR_PAYMENT_INTENT_PRESENCIAL",
                    detalleJson(Map.of("codigoError", ex.getCode(), "mensaje", ex.getMessage()))
            );
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Error técnico creando PaymentIntent presencial. idVenta={}, actorIdUsuarioMs1={}",
                    idVenta,
                    actor == null ? null : actor.idUsuarioMs1(),
                    ex);
            auditRegistrar.registrarErrorTecnico(
                    ENTIDAD_STRIPE,
                    idVenta == null ? null : String.valueOf(idVenta),
                    "CREAR_PAYMENT_INTENT_PRESENCIAL",
                    detalleJson(Map.of("mensaje", safeMessage(ex)))
            );
            throw internalError("No se pudo crear el PaymentIntent presencial.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StripePaymentIntentResponseDto obtenerEstadoPaymentIntent(String paymentIntentId,
                                                                     AuthenticatedUserContext actor) {
        Pago pago = pagoService.resolverPagoPorPaymentIntent(paymentIntentId);
        Venta venta = resolverVentaActiva(pago.getIdVenta());

        pagoPolicy.authorizeObtenerPago(actor, venta);

        PaymentIntent paymentIntent = stripePaymentIntentClient.retrieve(paymentIntentId);

        return new StripePaymentIntentResponseDto(
                pago.getIdVenta(),
                pago.getId(),
                pago.getCodigoPago(),
                pago.getMetodoPago(),
                pago.getStripePaymentIntentId(),
                paymentIntent.getClientSecret(),
                stripeClient.publishableKey(),
                paymentIntent.getStatus(),
                pago.getMoneda(),
                pago.getMonto()
        );
    }

    @Override
    public LookupItemResponseDto obtenerPublishableKeySandbox() {
        validarModoSandboxActivo();

        return new LookupItemResponseDto(
                null,
                "STRIPE_PUBLISHABLE_KEY_SANDBOX",
                stripeClient.publishableKey(),
                "Clave pública Stripe Sandbox para frontend.",
                true
        );
    }

    @Override
    public void validarModoSandboxActivo() {
        stripeClient.validatePaymentIntentConfiguration();
    }

    @Override
    @Transactional
    public void cancelarPaymentIntent(String paymentIntentId, String motivo) {
        try {
            PaymentIntent paymentIntent = stripePaymentIntentClient.cancel(paymentIntentId);

            try {
                pagoService.rechazarPagoStripe(
                        paymentIntentId,
                        firstNonBlank(paymentIntent.getStatus(), "canceled"),
                        firstNonBlank(motivo, "PaymentIntent cancelado desde MS4."),
                        paymentIntentCanceladoPayloadJson(paymentIntent, motivo)
                );
            } catch (NotFoundException ex) {
                log.warn("PaymentIntent cancelado sin pago local asociado. paymentIntentId={}", paymentIntentId);
            }

            auditRegistrar.registrarExito(
                    ENTIDAD_STRIPE,
                    paymentIntentId,
                    "CANCELAR_PAYMENT_INTENT",
                    detalleJson(Map.of(
                            "paymentIntentId", paymentIntentId,
                            "motivo", motivo == null ? "" : motivo
                    ))
            );
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Error técnico cancelando PaymentIntent. paymentIntentId={}", paymentIntentId, ex);
            throw internalError("No se pudo cancelar el PaymentIntent.", ex);
        }
    }

    private StripePaymentIntentResponseDto resolverPaymentIntentExistente(Venta venta, AuthenticatedUserContext actor) {
        List<EstadoPago> estadosBloqueantes = List.of(EstadoPago.PENDIENTE, EstadoPago.APROBADO);

        Pago pagoExistente = pagoRepository
                .findFirstByIdVentaAndEstadoPagoInAndEstadoTrueOrderByCreatedAtDesc(venta.getId(), estadosBloqueantes)
                .orElse(null);

        if (pagoExistente == null) {
            return null;
        }

        pagoPolicy.authorizeObtenerPago(actor, venta);

        if (pagoExistente.getEstadoPago() == EstadoPago.APROBADO) {
            throw new ConflictException("La venta ya tiene un pago aprobado.");
        }

        if (pagoExistente.getStripePaymentIntentId() == null || pagoExistente.getStripePaymentIntentId().isBlank()) {
            throw new ConflictException("La venta ya tiene un pago pendiente no recuperable como PaymentIntent.");
        }

        PaymentIntent paymentIntent = stripePaymentIntentClient.retrieve(pagoExistente.getStripePaymentIntentId());

        return new StripePaymentIntentResponseDto(
                pagoExistente.getIdVenta(),
                pagoExistente.getId(),
                pagoExistente.getCodigoPago(),
                pagoExistente.getMetodoPago(),
                pagoExistente.getStripePaymentIntentId(),
                paymentIntent.getClientSecret(),
                stripeClient.publishableKey(),
                paymentIntent.getStatus(),
                pagoExistente.getMoneda(),
                pagoExistente.getMonto()
        );
    }

    private PaymentIntent crearPaymentIntent(Venta venta,
                                             MetodoPago metodoPago,
                                             String descripcion,
                                             String returnUrl) {
        return stripePaymentIntentClient.create(new StripePaymentIntentClient.CreatePaymentIntentCommand(
                venta.getId(),
                venta.getCodigoVenta(),
                venta.getCanalVenta().getCode(),
                metodoPago.getCode(),
                venta.getTotal(),
                venta.getMoneda(),
                descripcion,
                metadata(venta, metodoPago, returnUrl)
        ));
    }

    private Map<String, String> metadata(Venta venta, MetodoPago metodoPago, String returnUrl) {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("idempotencyKey", "MS4-STRIPE-PI-VENTA-" + venta.getId() + "-" + metodoPago.getCode());
        metadata.put("idVentaMs4", String.valueOf(venta.getId()));
        metadata.put("codigoVenta", venta.getCodigoVenta());
        metadata.put("canalVenta", venta.getCanalVenta().getCode());
        metadata.put("metodoPago", metodoPago.getCode());

        if (returnUrl != null && !returnUrl.isBlank()) {
            metadata.put("returnUrl", returnUrl.trim());
        }

        return metadata;
    }

    private StripePaymentIntentResponseDto construirResponse(PagoResponseDto pago, PaymentIntent paymentIntent) {
        return new StripePaymentIntentResponseDto(
                pago.idVenta(),
                pago.id(),
                pago.codigoPago(),
                pago.metodoPago(),
                paymentIntent.getId(),
                paymentIntent.getClientSecret(),
                stripeClient.publishableKey(),
                paymentIntent.getStatus(),
                pago.moneda(),
                pago.monto()
        );
    }

    private Venta resolverVentaActiva(Long idVenta) {
        pagoValidator.validarIdVenta(idVenta);

        return ventaRepository.findByIdAndEstadoTrue(idVenta)
                .orElseThrow(() -> NotFoundException.byId(RECURSO_VENTA, idVenta));
    }

    private String paymentIntentPayloadJson(PaymentIntent paymentIntent, Venta venta, MetodoPago metodoPago) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("paymentIntentId", paymentIntent.getId());
        payload.put("clientSecretGenerado", paymentIntent.getClientSecret() != null);
        payload.put("status", paymentIntent.getStatus());
        payload.put("amount", paymentIntent.getAmount());
        payload.put("currency", paymentIntent.getCurrency());
        payload.put("idVenta", venta.getId());
        payload.put("codigoVenta", venta.getCodigoVenta());
        payload.put("metodoPago", metodoPago.getCode());

        return detalleJson(payload);
    }

    private String paymentIntentCanceladoPayloadJson(PaymentIntent paymentIntent, String motivo) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("paymentIntentId", paymentIntent.getId());
        payload.put("status", paymentIntent.getStatus());
        payload.put("motivo", motivo == null ? "" : motivo);
        return detalleJson(payload);
    }

    private String detalleJson(Object detalle) {
        try {
            return objectMapper.writeValueAsString(detalle == null ? Map.of() : detalle);
        } catch (JsonProcessingException ex) {
            return "{\"detalle\":\"no_serializable\"}";
        }
    }

    private String firstNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred.trim();
        }

        return fallback == null ? null : fallback.trim();
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