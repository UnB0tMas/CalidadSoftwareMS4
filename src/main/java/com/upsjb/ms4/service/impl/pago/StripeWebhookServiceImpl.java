package com.upsjb.ms4.service.impl.pago;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Event;
import com.upsjb.ms4.domain.entity.pago.StripeEvento;
import com.upsjb.ms4.domain.enums.EstadoKafkaProcesamiento;
import com.upsjb.ms4.domain.enums.MetodoPago;
import com.upsjb.ms4.dto.pago.response.PagoResponseDto;
import com.upsjb.ms4.dto.pago.response.StripeWebhookProcessResponseDto;
import com.upsjb.ms4.integration.stripe.StripeWebhookVerifier;
import com.upsjb.ms4.repository.StripeEventoRepository;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.roles.SecurityRoles;
import com.upsjb.ms4.service.contract.pago.PagoService;
import com.upsjb.ms4.service.contract.pago.StripeWebhookService;
import com.upsjb.ms4.service.contract.venta.VentaFisicaService;
import com.upsjb.ms4.service.contract.venta.VentaOnlineService;
import com.upsjb.ms4.shared.audit.AuditRegistrar;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.shared.exception.ValidationException;
import com.upsjb.ms4.validator.StripeWebhookValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Service
public class StripeWebhookServiceImpl implements StripeWebhookService {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookServiceImpl.class);

    private static final String ENTIDAD_STRIPE_WEBHOOK = "STRIPE_WEBHOOK";
    private static final String EVENT_PAYMENT_INTENT_SUCCEEDED = "payment_intent.succeeded";
    private static final String EVENT_PAYMENT_INTENT_FAILED = "payment_intent.payment_failed";
    private static final String EVENT_PAYMENT_INTENT_CANCELED = "payment_intent.canceled";
    private static final String EVENT_CHARGE_REFUNDED = "charge.refunded";
    private static final Long SYSTEM_ACTOR_ID_USUARIO_MS1 = 1L;

    private final StripeWebhookVerifier stripeWebhookVerifier;
    private final StripeEventoRepository stripeEventoRepository;
    private final PagoService pagoService;
    private final VentaOnlineService ventaOnlineService;
    private final VentaFisicaService ventaFisicaService;
    private final StripeWebhookValidator stripeWebhookValidator;
    private final AuditRegistrar auditRegistrar;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public StripeWebhookServiceImpl(StripeWebhookVerifier stripeWebhookVerifier,
                                    StripeEventoRepository stripeEventoRepository,
                                    PagoService pagoService,
                                    VentaOnlineService ventaOnlineService,
                                    VentaFisicaService ventaFisicaService,
                                    StripeWebhookValidator stripeWebhookValidator,
                                    AuditRegistrar auditRegistrar,
                                    ObjectMapper objectMapper,
                                    Clock clock) {
        this.stripeWebhookVerifier = stripeWebhookVerifier;
        this.stripeEventoRepository = stripeEventoRepository;
        this.pagoService = pagoService;
        this.ventaOnlineService = ventaOnlineService;
        this.ventaFisicaService = ventaFisicaService;
        this.stripeWebhookValidator = stripeWebhookValidator;
        this.auditRegistrar = auditRegistrar;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    @Transactional
    public StripeWebhookProcessResponseDto procesarWebhook(String rawPayload, String stripeSignatureHeader) {
        stripeWebhookValidator.validarFirma(rawPayload, stripeSignatureHeader);

        Event event = stripeWebhookVerifier.verify(rawPayload, stripeSignatureHeader);
        String stripeEventId = event.getId();
        String eventType = event.getType();

        if (eventoYaProcesado(stripeEventId)) {
            StripeEvento existente = stripeEventoRepository.findByStripeEventIdAndEstadoTrue(stripeEventId)
                    .orElse(null);

            return new StripeWebhookProcessResponseDto(
                    stripeEventId,
                    eventType,
                    existente == null ? null : existente.getStripePaymentIntentId(),
                    true,
                    true,
                    "DUPLICADO",
                    "El evento Stripe ya fue procesado previamente."
            );
        }

        StripeEvento stripeEvento = registrarEventoRecibido(stripeEventId, eventType, rawPayload);

        try {
            if (!stripeWebhookValidator.esEventoSoportado(eventType)) {
                return procesarEventoIgnorado(stripeEventId, eventType);
            }

            return switch (eventType) {
                case EVENT_PAYMENT_INTENT_SUCCEEDED -> procesarPaymentIntentSucceeded(
                        extractPaymentIntentId(rawPayload),
                        rawPayload
                );
                case EVENT_PAYMENT_INTENT_FAILED, EVENT_PAYMENT_INTENT_CANCELED -> procesarPaymentIntentPaymentFailed(
                        extractPaymentIntentId(rawPayload),
                        rawPayload
                );
                case EVENT_CHARGE_REFUNDED -> procesarEventoIgnorado(stripeEventId, eventType);
                default -> procesarEventoIgnorado(stripeEventId, eventType);
            };
        } catch (BusinessException ex) {
            marcarEventoError(stripeEvento, ex);
            auditRegistrar.registrarErrorUsuario(
                    ENTIDAD_STRIPE_WEBHOOK,
                    stripeEventId,
                    "PROCESAR_WEBHOOK_STRIPE",
                    detalleJson(auditMap("codigoError", ex.getCode(), "mensaje", ex.getMessage()))
            );

            return new StripeWebhookProcessResponseDto(
                    stripeEventId,
                    eventType,
                    stripeEvento.getStripePaymentIntentId(),
                    false,
                    false,
                    "ERROR_USUARIO",
                    ex.getMessage()
            );
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico procesando webhook Stripe. stripeEventId={}, eventType={}",
                    stripeEventId,
                    eventType,
                    ex
            );

            marcarEventoError(stripeEvento, ex);

            auditRegistrar.registrarErrorTecnico(
                    ENTIDAD_STRIPE_WEBHOOK,
                    stripeEventId,
                    "PROCESAR_WEBHOOK_STRIPE",
                    detalleJson(auditMap("mensaje", safeMessage(ex)))
            );

            return new StripeWebhookProcessResponseDto(
                    stripeEventId,
                    eventType,
                    stripeEvento.getStripePaymentIntentId(),
                    false,
                    false,
                    "ERROR_TECNICO",
                    "No se pudo procesar el webhook Stripe."
            );
        }
    }

    @Override
    @Transactional
    public StripeEvento registrarEventoRecibido(String stripeEventId, String eventType, String rawPayload) {
        stripeWebhookValidator.validarEventoRecibido(stripeEventId, eventType, rawPayload);

        String normalizedEventId = stripeEventId.trim();

        StripeEvento existente = stripeEventoRepository.findByStripeEventIdAndEstadoTrue(normalizedEventId)
                .orElse(null);

        if (existente != null) {
            return existente;
        }

        StripeEvento stripeEvento = StripeEvento.builder()
                .stripeEventId(normalizedEventId)
                .stripeEventType(eventType.trim())
                .stripePaymentIntentId(extractPaymentIntentIdQuietly(rawPayload))
                .estadoProcesamiento(EstadoKafkaProcesamiento.RECIBIDO)
                .payloadJson(rawPayload.trim())
                .fechaEvento(extractFechaEvento(rawPayload))
                .fechaRecepcion(LocalDateTime.now(clock))
                .estado(true)
                .build();

        try {
            return stripeEventoRepository.save(stripeEvento);
        } catch (DataIntegrityViolationException ex) {
            log.warn(
                    "Evento Stripe duplicado detectado durante registro concurrente. stripeEventId={}, eventType={}",
                    normalizedEventId,
                    eventType
            );
            return stripeEventoRepository.findByStripeEventIdAndEstadoTrue(normalizedEventId)
                    .orElseThrow(() -> ex);
        }
    }

    @Override
    @Transactional
    public StripeWebhookProcessResponseDto procesarPaymentIntentSucceeded(String paymentIntentId, String rawPayload) {
        String stripeEventId = extractStripeEventIdQuietly(rawPayload);
        String eventType = extractStripeEventTypeQuietly(rawPayload);

        PagoResponseDto pago = pagoService.confirmarPagoStripe(
                paymentIntentId,
                extractChargeId(rawPayload),
                firstNonBlank(extractPaymentIntentStatus(rawPayload), "succeeded"),
                rawPayload
        );

        confirmarVentaOnlineSiAplica(pago, paymentIntentId);
        confirmarVentaFisicaSiAplica(pago, paymentIntentId);

        StripeEvento stripeEvento = resolverEventoParaActualizar(stripeEventId);
        stripeEvento.setIdPago(pago.id());
        stripeEvento.setStripePaymentIntentId(paymentIntentId);
        stripeEvento.setEstadoProcesamiento(EstadoKafkaProcesamiento.PROCESADO);
        stripeEvento.setFechaProcesamiento(LocalDateTime.now(clock));
        stripeEvento.setErrorDetalle(null);
        stripeEventoRepository.save(stripeEvento);

        auditRegistrar.registrarExito(
                ENTIDAD_STRIPE_WEBHOOK,
                stripeEventId,
                "PROCESAR_PAYMENT_INTENT_SUCCEEDED",
                detalleJson(auditMap(
                        "paymentIntentId", paymentIntentId,
                        "idPago", pago.id(),
                        "idVenta", pago.idVenta(),
                        "metodoPago", pago.metodoPago()
                ))
        );

        return new StripeWebhookProcessResponseDto(
                stripeEventId,
                eventType,
                paymentIntentId,
                true,
                true,
                "PROCESADO",
                "Pago Stripe aprobado y venta asociada procesada correctamente."
        );
    }

    @Override
    @Transactional
    public StripeWebhookProcessResponseDto procesarPaymentIntentPaymentFailed(String paymentIntentId, String rawPayload) {
        String stripeEventId = extractStripeEventIdQuietly(rawPayload);
        String eventType = extractStripeEventTypeQuietly(rawPayload);
        String stripeStatus = firstNonBlank(extractPaymentIntentStatus(rawPayload), "failed");
        String motivo = extractFailureMessage(rawPayload);

        PagoResponseDto pago = pagoService.rechazarPagoStripe(
                paymentIntentId,
                stripeStatus,
                motivo,
                rawPayload
        );

        rechazarVentaOnlineSiAplica(pago, paymentIntentId, motivo);

        StripeEvento stripeEvento = resolverEventoParaActualizar(stripeEventId);
        stripeEvento.setIdPago(pago.id());
        stripeEvento.setStripePaymentIntentId(paymentIntentId);
        stripeEvento.setEstadoProcesamiento(EstadoKafkaProcesamiento.PROCESADO);
        stripeEvento.setFechaProcesamiento(LocalDateTime.now(clock));
        stripeEvento.setErrorDetalle(null);
        stripeEventoRepository.save(stripeEvento);

        auditRegistrar.registrarExito(
                ENTIDAD_STRIPE_WEBHOOK,
                stripeEventId,
                "PROCESAR_PAYMENT_INTENT_FAILED",
                detalleJson(auditMap(
                        "paymentIntentId", paymentIntentId,
                        "idPago", pago.id(),
                        "idVenta", pago.idVenta(),
                        "metodoPago", pago.metodoPago(),
                        "stripeStatus", stripeStatus
                ))
        );

        return new StripeWebhookProcessResponseDto(
                stripeEventId,
                eventType,
                paymentIntentId,
                true,
                false,
                "RECHAZADO",
                "Pago Stripe rechazado o cancelado correctamente."
        );
    }

    @Override
    @Transactional
    public StripeWebhookProcessResponseDto procesarEventoIgnorado(String stripeEventId, String eventType) {
        StripeEvento stripeEvento = stripeEventoRepository.findByStripeEventIdAndEstadoTrue(stripeEventId)
                .orElseGet(() -> stripeEventoRepository.save(StripeEvento.builder()
                        .stripeEventId(stripeEventId)
                        .stripeEventType(eventType)
                        .estadoProcesamiento(EstadoKafkaProcesamiento.RECIBIDO)
                        .payloadJson("{}")
                        .fechaRecepcion(LocalDateTime.now(clock))
                        .estado(true)
                        .build()));

        stripeEvento.setEstadoProcesamiento(EstadoKafkaProcesamiento.IGNORADO);
        stripeEvento.setFechaProcesamiento(LocalDateTime.now(clock));
        stripeEvento.setErrorDetalle("Evento Stripe ignorado por MS4: " + eventType);
        stripeEventoRepository.save(stripeEvento);

        auditRegistrar.registrarExito(
                ENTIDAD_STRIPE_WEBHOOK,
                stripeEventId,
                "IGNORAR_EVENTO_STRIPE",
                detalleJson(auditMap("eventType", eventType))
        );

        return new StripeWebhookProcessResponseDto(
                stripeEventId,
                eventType,
                stripeEvento.getStripePaymentIntentId(),
                true,
                false,
                "IGNORADO",
                "Evento Stripe recibido pero no requiere acción funcional en MS4."
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean eventoYaProcesado(String stripeEventId) {
        if (stripeEventId == null || stripeEventId.isBlank()) {
            return false;
        }

        return stripeEventoRepository.findByStripeEventIdAndEstadoTrue(stripeEventId.trim())
                .map(evento -> evento.getEstadoProcesamiento() == EstadoKafkaProcesamiento.PROCESADO
                        || evento.getEstadoProcesamiento() == EstadoKafkaProcesamiento.IGNORADO)
                .orElse(false);
    }

    private void confirmarVentaOnlineSiAplica(PagoResponseDto pago, String paymentIntentId) {
        if (pago == null || pago.metodoPago() != MetodoPago.TARJETA_ONLINE_STRIPE_SANDBOX) {
            return;
        }

        ventaOnlineService.confirmarVentaOnlinePagadaStripe(paymentIntentId);
    }

    private void rechazarVentaOnlineSiAplica(PagoResponseDto pago, String paymentIntentId, String motivo) {
        if (pago == null || pago.metodoPago() != MetodoPago.TARJETA_ONLINE_STRIPE_SANDBOX) {
            return;
        }

        ventaOnlineService.rechazarVentaOnlinePorStripe(paymentIntentId, motivo);
    }

    private void confirmarVentaFisicaSiAplica(PagoResponseDto pago, String paymentIntentId) {
        if (pago == null || pago.metodoPago() != MetodoPago.TARJETA_PRESENCIAL_STRIPE_SANDBOX) {
            return;
        }

        ventaFisicaService.confirmarVentaFisicaPagadaStripe(
                pago.idVenta(),
                paymentIntentId,
                systemActor()
        );
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

    private StripeEvento resolverEventoParaActualizar(String stripeEventId) {
        if (stripeEventId == null || stripeEventId.isBlank()) {
            throw new NotFoundException("No se pudo resolver el evento Stripe porque el eventId es obligatorio.");
        }

        return stripeEventoRepository.findByStripeEventIdAndEstadoTrue(stripeEventId)
                .orElseThrow(() -> new NotFoundException("Evento Stripe no encontrado: " + stripeEventId));
    }

    private void marcarEventoError(StripeEvento stripeEvento, Exception exception) {
        if (stripeEvento == null) {
            return;
        }

        if (stripeEvento.getEstadoProcesamiento() == EstadoKafkaProcesamiento.PROCESADO
                || stripeEvento.getEstadoProcesamiento() == EstadoKafkaProcesamiento.IGNORADO) {
            return;
        }

        stripeEvento.setEstadoProcesamiento(EstadoKafkaProcesamiento.ERROR);
        stripeEvento.setFechaProcesamiento(LocalDateTime.now(clock));
        stripeEvento.setErrorDetalle(truncate(errorMessage(exception), 4000));
        stripeEventoRepository.save(stripeEvento);
    }

    private String extractPaymentIntentId(String rawPayload) {
        String value = extractText(rawPayload, "data", "object", "id");

        if (value == null) {
            throw new ValidationException("No se pudo resolver paymentIntentId desde el webhook Stripe.");
        }

        return value;
    }

    private String extractPaymentIntentIdQuietly(String rawPayload) {
        try {
            return extractText(rawPayload, "data", "object", "id");
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private String extractStripeEventIdQuietly(String rawPayload) {
        String value = extractText(rawPayload, "id");
        return value == null ? "stripe-event-no-resuelto" : value;
    }

    private String extractStripeEventTypeQuietly(String rawPayload) {
        String value = extractText(rawPayload, "type");
        return value == null ? "unknown" : value;
    }

    private String extractPaymentIntentStatus(String rawPayload) {
        return extractText(rawPayload, "data", "object", "status");
    }

    private String extractChargeId(String rawPayload) {
        return extractText(rawPayload, "data", "object", "latest_charge");
    }

    private String extractFailureMessage(String rawPayload) {
        String message = extractText(rawPayload, "data", "object", "last_payment_error", "message");
        return firstNonBlank(message, "Stripe Sandbox informó que el pago no fue aprobado.");
    }

    private LocalDateTime extractFechaEvento(String rawPayload) {
        JsonNode root = readTree(rawPayload);
        JsonNode createdNode = root.path("created");

        if (createdNode.isNumber()) {
            return LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(createdNode.asLong()),
                    clock.getZone()
            );
        }

        return null;
    }

    private String extractText(String rawPayload, String... path) {
        JsonNode node = readTree(rawPayload);

        for (String item : path) {
            if (node == null || item == null) {
                return null;
            }
            node = node.path(item);
        }

        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }

        String value = node.asText(null);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private JsonNode readTree(String rawPayload) {
        try {
            return objectMapper.readTree(rawPayload);
        } catch (JsonProcessingException ex) {
            throw new ValidationException("El payload Stripe no tiene formato JSON válido.");
        }
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

    private String firstNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred.trim();
        }

        return fallback == null ? null : fallback.trim();
    }

    private String errorMessage(Exception exception) {
        if (exception == null) {
            return "Error no especificado.";
        }

        StringBuilder builder = new StringBuilder(exception.getClass().getName());

        if (exception.getMessage() != null && !exception.getMessage().isBlank()) {
            builder.append(": ").append(exception.getMessage().trim());
        }

        Throwable cause = exception.getCause();

        if (cause != null) {
            builder.append(" | cause=").append(cause.getClass().getName());

            if (cause.getMessage() != null && !cause.getMessage().isBlank()) {
                builder.append(": ").append(cause.getMessage().trim());
            }
        }

        return builder.toString();
    }

    private String safeMessage(Throwable throwable) {
        return throwable == null || throwable.getMessage() == null
                ? "Error técnico no especificado."
                : throwable.getMessage();
    }

    private String truncate(String value, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();
        return normalized.length() <= max ? normalized : normalized.substring(0, max);
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