package com.upsjb.ms4.kafka.probe;

import com.upsjb.ms4.domain.entity.snapshot.EmpleadoSnapshotMs2;
import com.upsjb.ms4.domain.entity.snapshot.StockSnapshotMs3;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.dto.kafka.ms2.EmpleadoSnapshotPayloadDto;
import com.upsjb.ms4.dto.kafka.ms3.StockSnapshotPayloadDto;
import com.upsjb.ms4.kafka.handler.EmpleadoSnapshotEventHandler;
import com.upsjb.ms4.kafka.handler.StockSnapshotEventHandler;
import com.upsjb.ms4.kafka.support.KafkaEnvelopeReader;
import com.upsjb.ms4.repository.EmpleadoSnapshotMs2Repository;
import com.upsjb.ms4.repository.KafkaEventoConsumidoRepository;
import com.upsjb.ms4.repository.StockSnapshotMs3Repository;
import jakarta.persistence.EntityManager;
import java.util.Objects;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class KafkaFunctionalSnapshotProbeExecutor {

    private static final Logger log =
            LoggerFactory.getLogger(
                    KafkaFunctionalSnapshotProbeExecutor.class
            );

    private final KafkaEnvelopeReader envelopeReader;
    private final EmpleadoSnapshotMs2Repository
            empleadoRepository;
    private final StockSnapshotMs3Repository
            stockRepository;
    private final KafkaEventoConsumidoRepository
            consumedRepository;
    private final KafkaProbePublisher probePublisher;
    private final KafkaProbeProperties probeProperties;
    private final KafkaFunctionalProbeRegistry
            functionalRegistry;
    private final PlatformTransactionManager
            transactionManager;
    private final EntityManager entityManager;

    public KafkaFunctionalSnapshotProbeExecutor(
            KafkaEnvelopeReader envelopeReader,
            EmpleadoSnapshotMs2Repository empleadoRepository,
            StockSnapshotMs3Repository stockRepository,
            KafkaEventoConsumidoRepository consumedRepository,
            KafkaProbePublisher probePublisher,
            KafkaProbeProperties probeProperties,
            KafkaFunctionalProbeRegistry functionalRegistry,
            PlatformTransactionManager transactionManager,
            EntityManager entityManager
    ) {
        this.envelopeReader = envelopeReader;
        this.empleadoRepository = empleadoRepository;
        this.stockRepository = stockRepository;
        this.consumedRepository = consumedRepository;
        this.probePublisher = probePublisher;
        this.probeProperties = probeProperties;
        this.functionalRegistry = functionalRegistry;
        this.transactionManager = transactionManager;
        this.entityManager = entityManager;
    }

    public boolean isEmployeeProbe(
            String rawJson
    ) {
        try {
            DomainEventEnvelopeDto<EmpleadoSnapshotPayloadDto>
                    envelope =
                    envelopeReader.read(
                            rawJson,
                            EmpleadoSnapshotPayloadDto.class
                    );

            return KafkaFunctionalProbeSupport
                    .isFunctionalProbe(
                            envelope.correlationId()
                    );
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public boolean isStockProbe(
            String rawJson
    ) {
        try {
            DomainEventEnvelopeDto<StockSnapshotPayloadDto>
                    envelope =
                    envelopeReader.read(
                            rawJson,
                            StockSnapshotPayloadDto.class
                    );

            return KafkaFunctionalProbeSupport
                    .isFunctionalProbe(
                            envelope.correlationId()
                    );
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public void executeEmployee(
            ConsumerRecord<String, String> record,
            EmpleadoSnapshotEventHandler handler
    ) {
        DomainEventEnvelopeDto<EmpleadoSnapshotPayloadDto>
                envelope =
                envelopeReader.read(
                        record.value(),
                        EmpleadoSnapshotPayloadDto.class
                );

        String probeId =
                KafkaFunctionalProbeSupport
                        .extractProbeId(
                                envelope.correlationId()
                        );

        EmpleadoSnapshotPayloadDto payload =
                envelope.payload();

        TransactionTemplate transactionTemplate =
                rollbackTransactionTemplate();

        transactionTemplate.executeWithoutResult(
                transactionStatus -> {
                    handler.handle(
                            record.topic(),
                            record.key(),
                            record.partition(),
                            record.offset(),
                            record.value()
                    );

                    entityManager.flush();

                    EmpleadoSnapshotMs2 snapshot =
                            empleadoRepository
                                    .findByEventId(
                                            envelope.eventId()
                                    )
                                    .orElseThrow(
                                            () -> new IllegalStateException(
                                                    "MS4 no persistió el snapshot funcional de empleado."
                                            )
                                    );

                    if (
                            !Objects.equals(
                                    snapshot.getIdEmpleadoMs2(),
                                    payload.idEmpleado()
                            )
                                    || !Objects.equals(
                                    snapshot.getIdUsuarioMs1(),
                                    payload.idUsuarioMs1()
                            )
                    ) {
                        throw new IllegalStateException(
                                "El snapshot funcional de empleado persistido por MS4 no coincide con el payload de MS2."
                        );
                    }

                    if (
                            !consumedRepository
                                    .existsByEventId(
                                            envelope.eventId()
                                    )
                    ) {
                        throw new IllegalStateException(
                                "MS4 no registró el evento funcional de empleado como consumido."
                        );
                    }

                    /*
                     * Segunda entrega del mismo evento:
                     * debe retornar por idempotencia sin insertar nuevamente.
                     */
                    handler.handle(
                            record.topic(),
                            record.key(),
                            record.partition(),
                            record.offset(),
                            record.value()
                    );

                    entityManager.flush();

                    transactionStatus.setRollbackOnly();
                }
        );

        verifyEmployeeRollback(
                envelope
        );

        publishAck(
                probeId,
                probeProperties.getTargetMs2(),
                "MS4_TO_MS2_FUNCTIONAL_ACK",
                probeProperties.ms4ToMs2AckTopic(),
                record,
                "MS4 deserializó, validó, persistió e hizo flush del snapshot real de empleado; validó idempotencia y revirtió toda la transacción."
        );

        log.info(
                "[KAFKA-FUNCTIONAL-E2E][MS4] Empleado MS2 validado con contrato real. probeId={}, eventId={}, idEmpleadoMs2={}, rollback=OK",
                probeId,
                envelope.eventId(),
                payload.idEmpleado()
        );
    }

    public void executeStock(
            ConsumerRecord<String, String> record,
            StockSnapshotEventHandler handler
    ) {
        DomainEventEnvelopeDto<StockSnapshotPayloadDto>
                envelope =
                envelopeReader.read(
                        record.value(),
                        StockSnapshotPayloadDto.class
                );

        String probeId =
                KafkaFunctionalProbeSupport
                        .extractProbeId(
                                envelope.correlationId()
                        );

        StockSnapshotPayloadDto payload =
                envelope.payload();

        TransactionTemplate transactionTemplate =
                rollbackTransactionTemplate();

        transactionTemplate.executeWithoutResult(
                transactionStatus -> {
                    handler.handle(
                            record.topic(),
                            record.key(),
                            record.partition(),
                            record.offset(),
                            record.value()
                    );

                    entityManager.flush();

                    StockSnapshotMs3 snapshot =
                            stockRepository
                                    .findByEventId(
                                            envelope.eventId()
                                    )
                                    .orElseThrow(
                                            () -> new IllegalStateException(
                                                    "MS4 no persistió el snapshot funcional de stock."
                                            )
                                    );

                    if (
                            !Objects.equals(
                                    snapshot.getIdStockMs3(),
                                    payload.idStock()
                            )
                                    || !Objects.equals(
                                    snapshot.getIdSkuMs3(),
                                    payload.idSku()
                            )
                                    || !Objects.equals(
                                    snapshot.getIdAlmacenMs3(),
                                    payload.idAlmacen()
                            )
                                    || !Objects.equals(
                                    snapshot.getStockDisponible(),
                                    payload.stockDisponible()
                            )
                    ) {
                        throw new IllegalStateException(
                                "El snapshot funcional de stock persistido por MS4 no coincide con el payload de MS3."
                        );
                    }

                    if (
                            !consumedRepository
                                    .existsByEventId(
                                            envelope.eventId()
                                    )
                    ) {
                        throw new IllegalStateException(
                                "MS4 no registró el snapshot funcional de stock como consumido."
                        );
                    }

                    handler.handle(
                            record.topic(),
                            record.key(),
                            record.partition(),
                            record.offset(),
                            record.value()
                    );

                    entityManager.flush();

                    transactionStatus.setRollbackOnly();
                }
        );

        verifyStockRollback(
                envelope
        );

        KafkaFunctionalProbeRegistry.StockCandidate
                candidate =
                new KafkaFunctionalProbeRegistry.StockCandidate(
                        probeId,
                        envelope.eventId(),
                        payload.idStock(),
                        payload.idSku(),
                        payload.codigoSku(),
                        payload.barcode(),
                        payload.idProducto(),
                        payload.codigoProducto(),
                        payload.nombreProducto(),
                        payload.idAlmacen(),
                        payload.codigoAlmacen(),
                        payload.nombreAlmacen(),
                        payload.stockFisico(),
                        payload.stockReservado(),
                        payload.stockDisponible()
                );

        functionalRegistry.registerStockCandidate(
                candidate
        );

        publishAck(
                probeId,
                probeProperties.getTargetMs3(),
                "MS4_TO_MS3_FUNCTIONAL_ACK",
                probeProperties.ms4ToMs3AckTopic(),
                record,
                "MS4 deserializó, validó, persistió e hizo flush del snapshot real de stock; validó idempotencia y revirtió toda la transacción."
        );

        log.info(
                "[KAFKA-FUNCTIONAL-E2E][MS4] Stock MS3 validado con contrato real. probeId={}, eventId={}, idStockMs3={}, idSkuMs3={}, idAlmacenMs3={}, stockDisponible={}, rollback=OK",
                probeId,
                envelope.eventId(),
                payload.idStock(),
                payload.idSku(),
                payload.idAlmacen(),
                payload.stockDisponible()
        );
    }

    private void verifyEmployeeRollback(
            DomainEventEnvelopeDto<?> envelope
    ) {
        if (
                empleadoRepository.existsByEventId(
                        envelope.eventId()
                )
                        || consumedRepository.existsByEventId(
                        envelope.eventId()
                )
        ) {
            throw new IllegalStateException(
                    "La prueba funcional de empleado dejó registros persistidos en MS4."
            );
        }
    }

    private void verifyStockRollback(
            DomainEventEnvelopeDto<?> envelope
    ) {
        if (
                stockRepository.existsByEventId(
                        envelope.eventId()
                )
                        || consumedRepository.existsByEventId(
                        envelope.eventId()
                )
        ) {
            throw new IllegalStateException(
                    "La prueba funcional de stock dejó registros persistidos en MS4."
            );
        }
    }

    private void publishAck(
            String probeId,
            String targetService,
            String direction,
            String ackTopic,
            ConsumerRecord<String, String> record,
            String message
    ) {
        String ackKey =
                "functional-ack:"
                        + probeId;

        KafkaProbeAckPayload ack =
                KafkaProbeAckPayload.functional(
                        probeId,
                        probeProperties.getServiceName(),
                        targetService,
                        direction,
                        record.topic(),
                        record.key(),
                        message
                );

        RecordMetadata metadata =
                probePublisher.publishAck(
                        ack,
                        ackTopic,
                        ackKey
                );

        log.info(
                "[KAFKA-FUNCTIONAL-E2E][MS4] ACK funcional enviado. probeId={}, topic={}, key={}, partition={}, offset={}",
                probeId,
                ackTopic,
                ackKey,
                metadata == null
                        ? null
                        : metadata.partition(),
                metadata == null
                        ? null
                        : metadata.offset()
        );
    }

    private TransactionTemplate
    rollbackTransactionTemplate() {
        TransactionTemplate template =
                new TransactionTemplate(
                        transactionManager
                );

        template.setPropagationBehavior(
                TransactionDefinition
                        .PROPAGATION_REQUIRES_NEW
        );

        return template;
    }
}