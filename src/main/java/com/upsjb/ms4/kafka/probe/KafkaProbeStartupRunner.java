package com.upsjb.ms4.kafka.probe;

import com.upsjb.ms4.config.KafkaTopicProperties;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaProbeStartupRunner {

    private static final Logger log =
            LoggerFactory.getLogger(
                    KafkaProbeStartupRunner.class
            );

    private static final String DIRECTION =
            "MS4_TO_MS3_FUNCTIONAL";

    private final KafkaProbeProperties properties;
    private final KafkaTopicProperties topicProperties;
    private final KafkaProbeRegistry registry;
    private final KafkaFunctionalProbeRegistry
            functionalRegistry;
    private final KafkaFunctionalStockCommandProbeService
            functionalProbeService;

    private final AtomicBoolean startupExecuted =
            new AtomicBoolean(false);

    public KafkaProbeStartupRunner(
            KafkaProbeProperties properties,
            KafkaTopicProperties topicProperties,
            KafkaProbeRegistry registry,
            KafkaFunctionalProbeRegistry functionalRegistry,
            KafkaFunctionalStockCommandProbeService functionalProbeService
    ) {
        this.properties = properties;
        this.topicProperties = topicProperties;
        this.registry = registry;
        this.functionalRegistry = functionalRegistry;
        this.functionalProbeService = functionalProbeService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (
                !properties.isEnabled()
                        || !properties.isRunOnStartup()
        ) {
            log.info(
                    "[KAFKA-FUNCTIONAL-E2E][MS4] Prueba deshabilitada por configuración."
            );
            return;
        }

        if (!startupExecuted.compareAndSet(false, true)) {
            return;
        }

        sleep(
                properties.safeInitialDelayMs()
        );

        String probeId =
                newProbeId();

        try {
            runProbe(probeId);
        } catch (RuntimeException ex) {
            printFailure(
                    probeId,
                    ex
            );

            if (properties.isFailOnTimeout()) {
                throw ex;
            }
        } finally {
            functionalRegistry.clear();
        }
    }

    public String runManualProbe() {
        String probeId =
                newProbeId();

        try {
            runProbe(probeId);
            return probeId;
        } finally {
            functionalRegistry.clear();
        }
    }

    private void runProbe(
            String probeId
    ) {
        KafkaFunctionalProbeRegistry.StockCandidate
                candidate =
                awaitStockCandidate();

        String topic =
                topicProperties
                        .stockCommandTopic();

        String expectedKey =
                candidate.stockStreamKey();

        registry.markPending(
                probeId,
                DIRECTION,
                topic,
                expectedKey,
                1
        );

        KafkaFunctionalStockCommandProbeService.Publication
                publication;

        try {
            publication =
                    functionalProbeService.publish(
                            probeId,
                            candidate
                    );
        } catch (RuntimeException ex) {
            registry.markFailed(
                    probeId,
                    DIRECTION,
                    topic,
                    expectedKey,
                    1,
                    ex.getMessage()
            );

            throw ex;
        }

        if (!registry.isAcked(probeId)) {
            registry.markPending(
                    probeId,
                    DIRECTION,
                    publication.topic(),
                    publication.eventKey(),
                    1
            );
        }

        log.info(
                "[KAFKA-FUNCTIONAL-E2E][MS4] Comando real de reserva enviado hacia MS3. probeId={}, eventId={}, idStockMs3={}, idSkuMs3={}, idAlmacenMs3={}, topic={}, key={}, partition={}, offset={}",
                probeId,
                publication.eventId(),
                publication.idStockMs3(),
                publication.idSkuMs3(),
                publication.idAlmacenMs3(),
                publication.topic(),
                publication.eventKey(),
                publication.partition(),
                publication.offset()
        );

        awaitAck(
                probeId,
                publication
        );

        printSuccess(
                publication,
                candidate
        );
    }

    private KafkaFunctionalProbeRegistry.StockCandidate
    awaitStockCandidate() {
        for (
                int attempt = 1;
                attempt <= properties.safeMaxAttempts();
                attempt++
        ) {
            KafkaFunctionalProbeRegistry.StockCandidate
                    candidate =
                    functionalRegistry
                            .currentStockCandidate()
                            .orElse(null);

            if (candidate != null) {
                return candidate;
            }

            log.info(
                    "[KAFKA-FUNCTIONAL-E2E][MS4] Esperando snapshot funcional real enviado por MS3. attempt={}/{}",
                    attempt,
                    properties.safeMaxAttempts()
            );

            sleep(
                    properties.safeRetryDelayMs()
            );
        }

        throw new IllegalStateException(
                "MS4 no recibió un snapshot funcional de stock desde MS3."
        );
    }

    private void awaitAck(
            String probeId,
            KafkaFunctionalStockCommandProbeService.Publication
                    publication
    ) {
        for (
                int attempt = 1;
                attempt <= properties.safeMaxAttempts();
                attempt++
        ) {
            if (registry.isAcked(probeId)) {
                return;
            }

            log.info(
                    "[KAFKA-FUNCTIONAL-E2E][MS4] Esperando confirmación funcional de MS3. probeId={}, attempt={}/{}",
                    probeId,
                    attempt,
                    properties.safeMaxAttempts()
            );

            sleep(
                    properties.safeRetryDelayMs()
            );
        }

        String message =
                "MS3 no confirmó el procesamiento funcional del comando "
                        + publication.eventId();

        registry.markFailed(
                probeId,
                DIRECTION,
                publication.topic(),
                publication.eventKey(),
                properties.safeMaxAttempts(),
                message
        );

        throw new IllegalStateException(
                message
        );
    }

    private void printSuccess(
            KafkaFunctionalStockCommandProbeService.Publication
                    publication,
            KafkaFunctionalProbeRegistry.StockCandidate
                    candidate
    ) {
        log.info(
                """
                
                ========================================================================
                [KAFKA-FUNCTIONAL-E2E][MS4] RESULTADO=APROBADO
                probeId={}
                sourceStockEventId={}
                stockCommandEventId={}
                flujoEntrada=MS3 -> OUTBOX REAL -> KAFKA -> MS4
                flujoSalida=MS4 -> OUTBOX REAL -> KAFKA -> MS3
                topicSalida={}
                eventKey={}
                snapshotStockRealConsumido=OK
                comandoReservaRealPublicado=OK
                reservaRealProcesadaMS3=OK
                cambioStockValidadoMS3=OK
                idempotenciaMS3=OK
                rollbackMS3=OK
                rollbackMS4=OK
                residuosBaseDatos=NINGUNO
                listoContextoReal=true
                ========================================================================
                """,
                publication.probeId(),
                candidate.sourceEventId(),
                publication.eventId(),
                publication.topic(),
                publication.eventKey()
        );
    }

    private void printFailure(
            String probeId,
            RuntimeException ex
    ) {
        log.error(
                """
                
                ========================================================================
                [KAFKA-FUNCTIONAL-E2E][MS4] RESULTADO=FALLIDO
                probeId={}
                flujo=MS3 <-> MS4
                listoContextoReal=false
                error={}
                Se aplicó rollback a toda escritura funcional de la prueba.
                ========================================================================
                """,
                probeId,
                ex.getMessage(),
                ex
        );
    }

    private String newProbeId() {
        String timestamp =
                DateTimeFormatter.ISO_INSTANT
                        .format(
                                Instant.now()
                        )
                        .replace(":", "")
                        .replace(".", "")
                        .replace("-", "");

        return "MS4-"
                + timestamp
                + "-"
                + UUID.randomUUID()
                .toString()
                .substring(
                        0,
                        8
                );
    }

    private void sleep(
            long millis
    ) {
        if (millis <= 0) {
            return;
        }

        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread()
                    .interrupt();

            throw new IllegalStateException(
                    "La prueba funcional Kafka MS4 fue interrumpida.",
                    ex
            );
        }
    }
}