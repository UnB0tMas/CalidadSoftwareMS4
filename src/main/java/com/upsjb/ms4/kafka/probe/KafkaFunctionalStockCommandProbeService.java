package com.upsjb.ms4.kafka.probe;

import com.upsjb.ms4.config.KafkaTopicProperties;
import com.upsjb.ms4.domain.entity.kafka.EventoDominioOutbox;
import com.upsjb.ms4.domain.enums.EstadoOutbox;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.dto.kafka.ms4.Ms4AlmacenPayloadDto;
import com.upsjb.ms4.dto.kafka.ms4.Ms4SkuPayloadDto;
import com.upsjb.ms4.dto.kafka.ms4.Ms4StockCommandEventDto;
import com.upsjb.ms4.dto.kafka.ms4.Ms4StockCommandPayloadDto;
import com.upsjb.ms4.kafka.producer.KafkaMessagePublisher;
import com.upsjb.ms4.repository.EventoDominioOutboxRepository;
import com.upsjb.ms4.service.contract.kafka.EventoDominioOutboxService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class KafkaFunctionalStockCommandProbeService {

    private static final String EVENT_TYPE =
            "VentaStockReservadoPendiente";

    private static final String AGGREGATE_TYPE =
            "STOCK";

    private static final int SCHEMA_VERSION =
            1;

    private final EventoDominioOutboxService outboxService;
    private final EventoDominioOutboxRepository outboxRepository;
    private final KafkaMessagePublisher kafkaMessagePublisher;
    private final KafkaTopicProperties topicProperties;
    private final KafkaProbeProperties probeProperties;
    private final PlatformTransactionManager transactionManager;
    private final Clock clock;

    public KafkaFunctionalStockCommandProbeService(
            EventoDominioOutboxService outboxService,
            EventoDominioOutboxRepository outboxRepository,
            KafkaMessagePublisher kafkaMessagePublisher,
            KafkaTopicProperties topicProperties,
            KafkaProbeProperties probeProperties,
            PlatformTransactionManager transactionManager,
            Clock clock
    ) {
        this.outboxService = outboxService;
        this.outboxRepository = outboxRepository;
        this.kafkaMessagePublisher = kafkaMessagePublisher;
        this.topicProperties = topicProperties;
        this.probeProperties = probeProperties;
        this.transactionManager = transactionManager;
        this.clock = clock;
    }

    public Publication publish(
            String probeId,
            KafkaFunctionalProbeRegistry.StockCandidate candidate
    ) {
        String normalizedProbeId =
                requireProbeId(
                        probeId
                );

        Objects.requireNonNull(
                candidate,
                "El candidato de stock para la prueba funcional es obligatorio."
        );

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(
                        transactionManager
                );

        transactionTemplate.setPropagationBehavior(
                TransactionDefinition
                        .PROPAGATION_REQUIRES_NEW
        );

        Publication publication =
                transactionTemplate.execute(
                        transactionStatus -> {
                            UUID eventId =
                                    UUID.randomUUID();

                            LocalDateTime occurredAt =
                                    LocalDateTime.now(
                                            clock
                                    );

                            String correlationId =
                                    KafkaFunctionalProbeSupport
                                            .correlationId(
                                                    normalizedProbeId
                                            );

                            String requestId =
                                    "REQ-"
                                            + normalizedProbeId;

                            String referenciaIdExterno =
                                    "KAFKA-FUNCTIONAL-"
                                            + normalizedProbeId;

                            String idempotencyKey =
                                    "KAFKA-FUNCTIONAL-IDEMPOTENCY-"
                                            + normalizedProbeId;

                            String eventKey =
                                    candidate
                                            .stockStreamKey();

                            Ms4StockCommandPayloadDto payload =
                                    new Ms4StockCommandPayloadDto(
                                            eventId.toString(),
                                            idempotencyKey,
                                            EVENT_TYPE,
                                            new Ms4SkuPayloadDto(
                                                    candidate.idSkuMs3(),
                                                    candidate.codigoSku(),
                                                    candidate.codigoSku(),
                                                    candidate.barcode()
                                            ),
                                            new Ms4AlmacenPayloadDto(
                                                    candidate.idAlmacenMs3(),
                                                    candidate.codigoAlmacen(),
                                                    candidate.codigoAlmacen(),
                                                    candidate.nombreAlmacen()
                                            ),
                                            "VENTA_MS4",
                                            referenciaIdExterno,
                                            1,
                                            "KFP-"
                                                    + eventId
                                                    .toString()
                                                    .substring(
                                                            0,
                                                            8
                                                    )
                                                    .toUpperCase(),
                                            null,
                                            null,
                                            "SISTEMA",
                                            occurredAt,
                                            occurredAt.plusMinutes(
                                                    10
                                            ),
                                            "Reserva temporal para validación funcional Kafka.",
                                            requestId,
                                            correlationId,
                                            """
                                            {
                                              "functionalProbe": true,
                                              "rollbackOnly": true
                                            }
                                            """
                                    );

                            DomainEventEnvelopeDto
                                    <Ms4StockCommandPayloadDto>
                                    envelope =
                                    new DomainEventEnvelopeDto<>(
                                            eventId,
                                            EVENT_TYPE,
                                            probeProperties
                                                    .getServiceName(),
                                            AGGREGATE_TYPE,
                                            referenciaIdExterno,
                                            SCHEMA_VERSION,
                                            probeProperties
                                                    .getServiceName(),
                                            occurredAt,
                                            requestId,
                                            correlationId,
                                            payload,
                                            Map.of(
                                                    "eventKey",
                                                    eventKey,
                                                    "functionalProbe",
                                                    true,
                                                    "rollbackOnly",
                                                    true
                                            )
                                    );

                            Ms4StockCommandEventDto event =
                                    new Ms4StockCommandEventDto(
                                            envelope
                                    );

                            EventoDominioOutbox outbox =
                                    outboxService
                                            .crearEventoStockCommand(
                                                    event,
                                                    null
                                            );

                            outboxRepository.flush();

                            outboxService.marcarPublicando(
                                    outbox.getId(),
                                    "kafka-functional-probe-"
                                            + normalizedProbeId
                            );

                            RecordMetadata metadata =
                                    kafkaMessagePublisher.publish(
                                            outbox.getTopic(),
                                            outbox.getEventKey(),
                                            outbox.getPayloadJson()
                                    );

                            outboxService.marcarPublicado(
                                    outbox.getId()
                            );

                            outboxRepository.flush();

                            EventoDominioOutbox verified =
                                    outboxRepository
                                            .findByEventId(
                                                    eventId
                                            )
                                            .orElseThrow(
                                                    () -> new IllegalStateException(
                                                            "No se encontró el Outbox funcional de MS4 después de publicarlo."
                                                    )
                                            );

                            if (
                                    verified.getStatus()
                                            != EstadoOutbox.PUBLICADO
                            ) {
                                throw new IllegalStateException(
                                        "El Outbox funcional de MS4 no alcanzó el estado PUBLICADO."
                                );
                            }

                            transactionStatus.setRollbackOnly();

                            return new Publication(
                                    normalizedProbeId,
                                    eventId,
                                    outbox.getTopic(),
                                    outbox.getEventKey(),
                                    referenciaIdExterno,
                                    idempotencyKey,
                                    candidate.idStockMs3(),
                                    candidate.idSkuMs3(),
                                    candidate.idAlmacenMs3(),
                                    metadata == null
                                            ? null
                                            : metadata.partition(),
                                    metadata == null
                                            ? null
                                            : metadata.offset(),
                                    correlationId
                            );
                        }
                );

        Publication safePublication =
                Objects.requireNonNull(
                        publication,
                        "No se obtuvo el resultado funcional de MS4."
                );

        if (
                outboxRepository.existsByEventId(
                        safePublication.eventId()
                )
        ) {
            throw new IllegalStateException(
                    "La prueba funcional MS4 dejó un evento Outbox persistido."
            );
        }

        return safePublication;
    }

    private String requireProbeId(
            String probeId
    ) {
        if (
                probeId == null
                        || probeId.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "El probeId funcional de MS4 es obligatorio."
            );
        }

        return probeId.trim();
    }

    public record Publication(
            String probeId,
            UUID eventId,
            String topic,
            String eventKey,
            String referenciaIdExterno,
            String idempotencyKey,
            Long idStockMs3,
            Long idSkuMs3,
            Long idAlmacenMs3,
            Integer partition,
            Long offset,
            String correlationId
    ) {
    }
}