package com.upsjb.ms4.service.impl.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms4.config.KafkaTopicProperties;
import com.upsjb.ms4.config.OutboxProperties;
import com.upsjb.ms4.domain.entity.kafka.EventoDominioOutbox;
import com.upsjb.ms4.domain.enums.EstadoOutbox;
import com.upsjb.ms4.dto.kafka.filter.EventoDominioOutboxFilterDto;
import com.upsjb.ms4.dto.kafka.ms4.Ms4StockCommandEventDto;
import com.upsjb.ms4.dto.kafka.ms4.Ms4StockCommandPayloadDto;
import com.upsjb.ms4.dto.kafka.response.EventoDominioOutboxResponseDto;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.mapper.kafka.EventoDominioOutboxMapper;
import com.upsjb.ms4.policy.OutboxPolicy;
import com.upsjb.ms4.repository.EventoDominioOutboxRepository;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.auditoria.AuditoriaFuncionalService;
import com.upsjb.ms4.service.contract.kafka.EventoDominioOutboxService;
import com.upsjb.ms4.shared.audit.AuditContextHolder;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.shared.exception.KafkaPublishException;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.shared.pagination.PaginationService;
import com.upsjb.ms4.specification.EventoDominioOutboxSpecification;
import com.upsjb.ms4.util.JsonUtil;
import com.upsjb.ms4.validator.OutboxValidator;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventoDominioOutboxServiceImpl
        implements EventoDominioOutboxService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    EventoDominioOutboxServiceImpl.class
            );

    private static final String RECURSO_OUTBOX =
            "Evento dominio outbox";

    private static final String ENTIDAD_OUTBOX =
            "EVENTO_DOMINIO_OUTBOX";

    private static final String STOCK_AGGREGATE_TYPE =
            "STOCK";

    private static final int STOCK_COMMAND_SCHEMA_VERSION =
            1;

    private static final String STOCK_STREAM_PREFIX =
            "STOCK_STREAM:";

    private final EventoDominioOutboxRepository outboxRepository;
    private final EventoDominioOutboxMapper outboxMapper;
    private final OutboxValidator outboxValidator;
    private final OutboxPolicy outboxPolicy;
    private final PaginationService paginationService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final OutboxProperties outboxProperties;
    private final KafkaTopicProperties kafkaTopicProperties;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public EventoDominioOutboxServiceImpl(
            EventoDominioOutboxRepository outboxRepository,
            EventoDominioOutboxMapper outboxMapper,
            OutboxValidator outboxValidator,
            OutboxPolicy outboxPolicy,
            PaginationService paginationService,
            AuthenticatedUserResolver authenticatedUserResolver,
            AuditoriaFuncionalService auditoriaFuncionalService,
            OutboxProperties outboxProperties,
            KafkaTopicProperties kafkaTopicProperties,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.outboxRepository = outboxRepository;
        this.outboxMapper = outboxMapper;
        this.outboxValidator = outboxValidator;
        this.outboxPolicy = outboxPolicy;
        this.paginationService = paginationService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.auditoriaFuncionalService = auditoriaFuncionalService;
        this.outboxProperties = outboxProperties;
        this.kafkaTopicProperties = kafkaTopicProperties;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    @Transactional
    public EventoDominioOutbox crearEvento(
            String aggregateType,
            String aggregateId,
            String topic,
            String eventType,
            Object payload,
            AuthenticatedUserContext actor
    ) {
        String payloadJson =
                serializePayload(payload);

        outboxValidator.validarEvento(
                aggregateType,
                aggregateId,
                topic,
                eventType,
                payloadJson
        );

        AuditContextHolder.AuditContext auditContext =
                AuditContextHolder.getOrEmpty();

        EventoDominioOutbox evento =
                EventoDominioOutbox.builder()
                        .eventId(UUID.randomUUID())
                        .aggregateType(
                                aggregateType.trim()
                        )
                        .aggregateId(
                                aggregateId.trim()
                        )
                        .topic(
                                topic.trim()
                        )
                        .eventKey(
                                aggregateId.trim()
                        )
                        .eventType(
                                eventType.trim()
                        )
                        .payloadJson(payloadJson)
                        .status(
                                EstadoOutbox.PENDIENTE
                        )
                        .attempts(0)
                        .maxAttempts(
                                outboxProperties
                                        .maxAttemptsSafe()
                        )
                        .requestId(
                                firstNonBlank(
                                        auditContext.requestId(),
                                        actor == null
                                                ? null
                                                : actor.sid()
                                )
                        )
                        .correlationId(
                                auditContext.correlationId()
                        )
                        .estado(true)
                        .build();

        return outboxRepository.save(evento);
    }

    @Override
    @Transactional
    public EventoDominioOutbox crearEventoStockCommand(
            Ms4StockCommandEventDto event,
            AuthenticatedUserContext actor
    ) {
        validateStockCommandContract(event);

        Ms4StockCommandPayloadDto payload =
                event.payload();

        UUID eventId =
                event.envelope()
                        .eventId();

        String aggregateType =
                STOCK_AGGREGATE_TYPE;

        String aggregateId =
                firstNonBlank(
                        event.envelope()
                                .aggregateId(),
                        payload.idempotencyKey(),
                        payload.referenciaIdExterno()
                );

        String topic =
                kafkaTopicProperties
                        .stockCommandTopic();

        String eventType =
                event.envelope()
                        .eventType()
                        .trim();

        String eventKey =
                resolveStockCommandEventKey(
                        event,
                        payload
                );

        EventoDominioOutbox existing =
                outboxRepository
                        .findByEventId(eventId)
                        .orElse(null);

        if (existing == null) {
            existing =
                    outboxRepository
                            .findByAggregateTypeAndAggregateIdAndEventTypeAndEventKeyAndEstadoTrue(
                                    aggregateType,
                                    aggregateId,
                                    eventType,
                                    eventKey
                            )
                            .orElse(null);
        }

        if (existing != null) {
            validateExistingStockCommand(
                    existing,
                    aggregateType,
                    aggregateId,
                    topic,
                    eventType,
                    eventKey
            );

            return existing;
        }

        String payloadJson =
                serializePayload(event);

        outboxValidator.validarEvento(
                aggregateType,
                aggregateId,
                topic,
                eventType,
                payloadJson
        );

        AuditContextHolder.AuditContext auditContext =
                AuditContextHolder.getOrEmpty();

        EventoDominioOutbox evento =
                EventoDominioOutbox.builder()
                        .eventId(eventId)
                        .aggregateType(aggregateType)
                        .aggregateId(
                                aggregateId.trim()
                        )
                        .topic(
                                topic.trim()
                        )
                        .eventKey(eventKey)
                        .eventType(eventType)
                        .payloadJson(payloadJson)
                        .status(
                                EstadoOutbox.PENDIENTE
                        )
                        .attempts(0)
                        .maxAttempts(
                                outboxProperties
                                        .maxAttemptsSafe()
                        )
                        .requestId(
                                firstNonBlank(
                                        event.envelope()
                                                .requestId(),
                                        payload.requestId(),
                                        auditContext.requestId(),
                                        actor == null
                                                ? null
                                                : actor.sid()
                                )
                        )
                        .correlationId(
                                firstNonBlank(
                                        event.envelope()
                                                .correlationId(),
                                        payload.correlationId(),
                                        auditContext.correlationId()
                                )
                        )
                        .estado(true)
                        .build();

        return outboxRepository.save(evento);
    }

    private void validateStockCommandContract(
            Ms4StockCommandEventDto event
    ) {
        if (
                event == null
                        || event.envelope() == null
                        || event.payload() == null
        ) {
            throw new KafkaPublishException(
                    "El evento de comando de stock MS4 es obligatorio."
            );
        }

        if (event.envelope().eventId() == null) {
            throw new KafkaPublishException(
                    "El eventId del comando de stock MS4 es obligatorio."
            );
        }

        if (
                event.envelope()
                        .eventVersionSafe()
                        != STOCK_COMMAND_SCHEMA_VERSION
        ) {
            throw new KafkaPublishException(
                    "El contrato ms4.stock.command.v1 exige schemaVersion=1."
            );
        }

        if (
                !STOCK_AGGREGATE_TYPE.equalsIgnoreCase(
                        firstNonBlank(
                                event.envelope()
                                        .aggregateType()
                        )
                )
        ) {
            throw new KafkaPublishException(
                    "El aggregateType del comando de stock debe ser STOCK."
            );
        }

        Ms4StockCommandPayloadDto payload =
                event.payload();

        String envelopeEventType =
                firstNonBlank(
                        event.envelope()
                                .eventType()
                );

        String payloadEventType =
                firstNonBlank(
                        payload.eventType()
                );

        if (
                envelopeEventType == null
                        || payloadEventType == null
                        || !envelopeEventType
                        .equalsIgnoreCase(
                                payloadEventType
                        )
        ) {
            throw new KafkaPublishException(
                    "El eventType del envelope no coincide con el eventType del payload."
            );
        }

        String payloadEventId =
                firstNonBlank(
                        payload.eventId()
                );

        if (
                payloadEventId == null
                        || !event.envelope()
                        .eventId()
                        .toString()
                        .equalsIgnoreCase(
                                payloadEventId
                        )
        ) {
            throw new KafkaPublishException(
                    "El eventId del payload no coincide con el eventId del envelope."
            );
        }

        if (
                payload.idempotencyKey() == null
                        || payload.idempotencyKey()
                        .isBlank()
        ) {
            throw new KafkaPublishException(
                    "La idempotencyKey del comando de stock es obligatoria."
            );
        }

        if (
                payload.referenciaTipo() == null
                        || payload.referenciaTipo()
                        .isBlank()
                        || payload.referenciaIdExterno() == null
                        || payload.referenciaIdExterno()
                        .isBlank()
        ) {
            throw new KafkaPublishException(
                    "El comando de stock debe incluir tipo e identificador de referencia."
            );
        }

        if (
                payload.cantidad() == null
                        || payload.cantidad() <= 0
        ) {
            throw new KafkaPublishException(
                    "La cantidad del comando de stock debe ser mayor a cero."
            );
        }

        requirePositive(
                payload.sku() == null
                        ? null
                        : payload.sku()
                        .id(),
                "El idSku MS3"
        );

        requirePositive(
                payload.almacen() == null
                        ? null
                        : payload.almacen()
                        .id(),
                "El idAlmacen MS3"
        );

        if (
                event.envelope()
                        .occurredAt() == null
        ) {
            throw new KafkaPublishException(
                    "La fecha occurredAt del comando de stock es obligatoria."
            );
        }
    }

    private String resolveStockCommandEventKey(
            Ms4StockCommandEventDto event,
            Ms4StockCommandPayloadDto payload
    ) {
        Long idSkuMs3 =
                payload.sku()
                        .id();

        Long idAlmacenMs3 =
                payload.almacen()
                        .id();

        String expectedKey =
                STOCK_STREAM_PREFIX
                        + idSkuMs3
                        + ":"
                        + idAlmacenMs3;

        Object metadataKey =
                event.envelope()
                        .metadataSafe()
                        .get("eventKey");

        if (
                metadataKey != null
                        && !expectedKey.equals(
                        String.valueOf(
                                metadataKey
                        ).trim()
                )
        ) {
            throw new KafkaPublishException(
                    "La key Kafka del comando de stock no coincide con el SKU y almacén del payload."
            );
        }

        return expectedKey;
    }

    private void validateExistingStockCommand(
            EventoDominioOutbox existing,
            String aggregateType,
            String aggregateId,
            String topic,
            String eventType,
            String eventKey
    ) {
        if (!existing.isActivo()) {
            throw new KafkaPublishException(
                    "Ya existe un evento Outbox inactivo para la misma idempotencyKey."
            );
        }

        if (
                existing.getStatus()
                        == EstadoOutbox.DESCARTADO
        ) {
            throw new KafkaPublishException(
                    "El comando de stock ya existe, pero su evento Outbox fue descartado."
            );
        }

        boolean sameContract =
                Objects.equals(
                        normalize(existing.getAggregateType()),
                        normalize(aggregateType)
                )
                        && Objects.equals(
                        normalize(existing.getAggregateId()),
                        normalize(aggregateId)
                )
                        && Objects.equals(
                        normalize(existing.getTopic()),
                        normalize(topic)
                )
                        && Objects.equals(
                        normalize(existing.getEventType()),
                        normalize(eventType)
                )
                        && Objects.equals(
                        normalize(existing.getEventKey()),
                        normalize(eventKey)
                );

        if (!sameContract) {
            throw new KafkaPublishException(
                    "El eventId determinístico ya existe con un contrato Outbox diferente."
            );
        }
    }

    private void requirePositive(
            Long value,
            String fieldName
    ) {
        if (
                value == null
                        || value <= 0
        ) {
            throw new KafkaPublishException(
                    fieldName
                            + " debe ser positivo."
            );
        }
    }

    private String normalize(
            String value
    ) {
        return value == null
                ? null
                : value.trim();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<EventoDominioOutboxResponseDto> listar(
            EventoDominioOutboxFilterDto filter,
            PageRequestDto page
    ) {
        AuthenticatedUserContext actor =
                authenticatedUserResolver.current();

        outboxPolicy.authorizeListarOutbox(actor);
        outboxValidator.validarFiltro(filter);

        Page<EventoDominioOutbox> result =
                outboxRepository.findAll(
                        EventoDominioOutboxSpecification.build(
                                filter
                        ),
                        paginationService.toPageable(
                                page,
                                "createdAt"
                        )
                );

        return paginationService.toPageResponse(
                result,
                outboxMapper::toResponse
        );
    }

    @Override
    @Transactional
    public EventoDominioOutboxResponseDto reintentar(
            Long idOutbox,
            AuthenticatedUserContext actor
    ) {
        try {
            outboxPolicy.authorizeReintentarOutbox(
                    actor
            );

            EventoDominioOutbox evento =
                    resolverActivo(idOutbox);

            outboxValidator.validarReintento(
                    evento
            );

            evento.setStatus(
                    EstadoOutbox.PENDIENTE
            );
            evento.setAttempts(0);
            evento.setLastError(null);
            evento.setLockedBy(null);
            evento.setLockedAt(null);
            evento.setPublishedAt(null);

            evento =
                    outboxRepository.save(
                            evento
                    );

            auditoriaFuncionalService.registrarExito(
                    ENTIDAD_OUTBOX,
                    evento.getId(),
                    "REINTENTAR_OUTBOX",
                    actor,
                    detalleAuditoria(
                            evento,
                            null
                    )
            );

            return outboxMapper.toResponse(
                    evento
            );
        } catch (BusinessException ex) {
            auditoriaFuncionalService.registrarErrorUsuario(
                    ENTIDAD_OUTBOX,
                    idOutbox,
                    "REINTENTAR_OUTBOX",
                    actor,
                    ex.getCode(),
                    ex.getMessage()
            );

            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico reintentando evento outbox. idOutbox={}, actorIdUsuarioMs1={}",
                    idOutbox,
                    actor == null
                            ? null
                            : actor.idUsuarioMs1(),
                    ex
            );

            auditoriaFuncionalService.registrarErrorTecnico(
                    ENTIDAD_OUTBOX,
                    idOutbox,
                    "REINTENTAR_OUTBOX",
                    actor,
                    ex
            );

            throw internalError(
                    "No se pudo reintentar el evento outbox.",
                    ex
            );
        }
    }

    @Override
    @Transactional
    public EventoDominioOutboxResponseDto descartar(
            Long idOutbox,
            EstadoChangeRequestDto request,
            AuthenticatedUserContext actor
    ) {
        try {
            outboxPolicy.authorizeDescartarOutbox(
                    actor
            );

            EventoDominioOutbox evento =
                    resolverActivo(idOutbox);

            outboxValidator.validarDescartar(
                    evento
            );

            String motivo =
                    request == null
                            ? "Evento outbox descartado por administrador."
                            : request.motivo();

            evento.setStatus(
                    EstadoOutbox.DESCARTADO
            );
            evento.setLastError(
                    truncate(
                            motivo,
                            4000
                    )
            );
            evento.setLockedBy(null);
            evento.setLockedAt(null);

            if (
                    request != null
                            && request.estado() != null
            ) {
                evento.setEstado(
                        request.estado()
                );
            }

            evento =
                    outboxRepository.save(
                            evento
                    );

            auditoriaFuncionalService.registrarExito(
                    ENTIDAD_OUTBOX,
                    evento.getId(),
                    "DESCARTAR_OUTBOX",
                    actor,
                    detalleAuditoria(
                            evento,
                            motivo
                    )
            );

            return outboxMapper.toResponse(
                    evento
            );
        } catch (BusinessException ex) {
            auditoriaFuncionalService.registrarErrorUsuario(
                    ENTIDAD_OUTBOX,
                    idOutbox,
                    "DESCARTAR_OUTBOX",
                    actor,
                    ex.getCode(),
                    ex.getMessage()
            );

            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico descartando evento outbox. idOutbox={}, actorIdUsuarioMs1={}",
                    idOutbox,
                    actor == null
                            ? null
                            : actor.idUsuarioMs1(),
                    ex
            );

            auditoriaFuncionalService.registrarErrorTecnico(
                    ENTIDAD_OUTBOX,
                    idOutbox,
                    "DESCARTAR_OUTBOX",
                    actor,
                    ex
            );

            throw internalError(
                    "No se pudo descartar el evento outbox.",
                    ex
            );
        }
    }

    @Override
    @Transactional
    public List<EventoDominioOutbox> reclamarBatchPendiente(
            String workerId,
            int batchSize
    ) {
        String lockedBy =
                normalizeWorker(workerId);

        int safeBatchSize =
                Math.max(
                        1,
                        Math.min(
                                batchSize,
                                outboxProperties
                                        .batchSizeSafe()
                        )
                );

        LocalDateTime now =
                LocalDateTime.now(clock);

        LocalDateTime expiredBefore =
                now.minus(
                        outboxProperties
                                .lockTtlSafe()
                );

        List<EventoDominioOutbox> eventos =
                outboxRepository
                        .findPublishableForUpdate(
                                List.of(
                                        EstadoOutbox.PENDIENTE,
                                        EstadoOutbox.ERROR
                                ),
                                EstadoOutbox.PUBLICADO,
                                expiredBefore,
                                PageRequest.of(
                                        0,
                                        safeBatchSize
                                )
                        );

        for (EventoDominioOutbox evento : eventos) {
            evento.setStatus(
                    EstadoOutbox.PUBLICANDO
            );
            evento.setLockedBy(lockedBy);
            evento.setLockedAt(now);

            if (evento.getAttempts() == null) {
                evento.setAttempts(0);
            }

            if (
                    evento.getMaxAttempts() == null
                            || evento.getMaxAttempts() <= 0
            ) {
                evento.setMaxAttempts(
                        outboxProperties
                                .maxAttemptsSafe()
                );
            }

            evento.setAttempts(
                    evento.getAttempts() + 1
            );
        }

        return outboxRepository.saveAll(
                eventos
        );
    }

    @Override
    @Transactional
    public void marcarPublicando(
            Long idOutbox,
            String workerId
    ) {
        EventoDominioOutbox evento =
                resolverActivo(idOutbox);

        outboxValidator.validarPublicable(
                evento
        );

        evento.setStatus(
                EstadoOutbox.PUBLICANDO
        );
        evento.setLockedBy(
                normalizeWorker(workerId)
        );
        evento.setLockedAt(
                LocalDateTime.now(clock)
        );

        if (evento.getAttempts() == null) {
            evento.setAttempts(0);
        }

        if (
                evento.getMaxAttempts() == null
                        || evento.getMaxAttempts() <= 0
        ) {
            evento.setMaxAttempts(
                    outboxProperties
                            .maxAttemptsSafe()
            );
        }

        evento.setAttempts(
                evento.getAttempts() + 1
        );

        outboxRepository.save(evento);
    }

    @Override
    @Transactional
    public void marcarPublicado(
            Long idOutbox
    ) {
        EventoDominioOutbox evento =
                resolverActivo(idOutbox);

        evento.setStatus(
                EstadoOutbox.PUBLICADO
        );
        evento.setPublishedAt(
                LocalDateTime.now(clock)
        );
        evento.setLastError(null);
        evento.setLockedBy(null);
        evento.setLockedAt(null);

        outboxRepository.save(evento);
    }

    @Override
    @Transactional
    public void marcarError(
            Long idOutbox,
            String errorDetalle
    ) {
        EventoDominioOutbox evento =
                resolverActivo(idOutbox);

        if (evento.getAttempts() == null) {
            evento.setAttempts(0);
        }

        if (
                evento.getMaxAttempts() == null
                        || evento.getMaxAttempts() <= 0
        ) {
            evento.setMaxAttempts(
                    outboxProperties
                            .maxAttemptsSafe()
            );
        }

        if (
                evento.getStatus()
                        != EstadoOutbox.PUBLICANDO
        ) {
            evento.setAttempts(
                    evento.getAttempts() + 1
            );
        }

        evento.setStatus(
                EstadoOutbox.ERROR
        );
        evento.setLastError(
                truncate(
                        errorDetalle,
                        4000
                )
        );
        evento.setLockedBy(null);
        evento.setLockedAt(null);

        outboxRepository.save(evento);
    }

    private EventoDominioOutbox resolverActivo(
            Long idOutbox
    ) {
        outboxValidator.validarIdOutbox(
                idOutbox
        );

        return outboxRepository
                .findById(idOutbox)
                .filter(
                        EventoDominioOutbox::isActivo
                )
                .orElseThrow(
                        () ->
                                NotFoundException.byId(
                                        RECURSO_OUTBOX,
                                        idOutbox
                                )
                );
    }

    private String serializePayload(
            Object payload
    ) {
        if (payload == null) {
            throw new KafkaPublishException(
                    "El payload Outbox es obligatorio."
            );
        }

        if (payload instanceof CharSequence raw) {
            String value =
                    raw.toString();

            if (
                    value.isBlank()
                            || !JsonUtil.isValidJson(
                            value
                    )
            ) {
                throw new KafkaPublishException(
                        "El payload Outbox debe contener JSON válido."
                );
            }

            return value.trim();
        }

        try {
            return objectMapper.writeValueAsString(
                    payload
            );
        } catch (JsonProcessingException ex) {
            throw new KafkaPublishException(
                    "No se pudo serializar payload Outbox.",
                    ex
            );
        }
    }

    private String normalizeWorker(
            String workerId
    ) {
        return workerId == null
                || workerId.isBlank()
                ? outboxProperties
                .publisherIdSafe()
                : workerId.trim();
    }

    private String firstNonBlank(
            String... values
    ) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (
                    value != null
                            && !value.isBlank()
            ) {
                return value.trim();
            }
        }

        return null;
    }

    private String truncate(
            String value,
            int max
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.length() <= max
                ? normalized
                : normalized.substring(
                0,
                max
        );
    }

    private Map<String, Object> detalleAuditoria(
            EventoDominioOutbox evento,
            String motivo
    ) {
        Map<String, Object> detalle =
                new LinkedHashMap<>();

        detalle.put(
                "eventId",
                evento.getEventId()
        );
        detalle.put(
                "aggregateType",
                evento.getAggregateType()
        );
        detalle.put(
                "aggregateId",
                evento.getAggregateId()
        );
        detalle.put(
                "topic",
                evento.getTopic()
        );
        detalle.put(
                "eventType",
                evento.getEventType()
        );
        detalle.put(
                "status",
                evento.getStatus()
        );
        detalle.put(
                "attempts",
                evento.getAttempts()
        );
        detalle.put(
                "maxAttempts",
                evento.getMaxAttempts()
        );

        if (
                motivo != null
                        && !motivo.isBlank()
        ) {
            detalle.put(
                    "motivo",
                    motivo.trim()
            );
        }

        return detalle;
    }

    private BusinessException internalError(
            String message,
            RuntimeException ex
    ) {
        return new BusinessException(
                ErrorCodes.INTERNAL_ERROR,
                message,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex
        );
    }
}