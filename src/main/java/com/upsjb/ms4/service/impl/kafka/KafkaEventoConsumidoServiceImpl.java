// ruta: src/main/java/com/upsjb/ms4/service/impl/kafka/KafkaEventoConsumidoServiceImpl.java
package com.upsjb.ms4.service.impl.kafka;

import com.upsjb.ms4.domain.entity.kafka.KafkaEventoConsumido;
import com.upsjb.ms4.domain.enums.EstadoKafkaProcesamiento;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.repository.KafkaEventoConsumidoRepository;
import com.upsjb.ms4.service.contract.kafka.KafkaEventoConsumidoService;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.validator.KafkaEventoConsumidoValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class KafkaEventoConsumidoServiceImpl implements KafkaEventoConsumidoService {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventoConsumidoServiceImpl.class);

    private static final String RECURSO_EVENTO_CONSUMIDO = "Evento Kafka consumido";

    private final KafkaEventoConsumidoRepository repository;
    private final KafkaEventoConsumidoValidator validator;
    private final Clock clock;

    public KafkaEventoConsumidoServiceImpl(KafkaEventoConsumidoRepository repository,
                                           KafkaEventoConsumidoValidator validator,
                                           Clock clock) {
        this.repository = repository;
        this.validator = validator;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeEvento(UUID eventId) {
        if (eventId == null) {
            return false;
        }

        return repository.existsByEventId(eventId);
    }

    @Override
    @Transactional
    public KafkaEventoConsumido registrarRecibido(DomainEventEnvelopeDto<?> envelope,
                                                  String topic,
                                                  String payloadJson) {
        validator.validarRegistroRecibido(envelope, topic, payloadJson);

        KafkaEventoConsumido existente = repository.findByEventId(envelope.eventId()).orElse(null);
        if (existente != null) {
            return existente;
        }

        KafkaEventoConsumido entity = KafkaEventoConsumido.builder()
                .sourceService(envelope.sourceServiceSafe())
                .topic(topic.trim())
                .eventId(envelope.eventId())
                .eventType(envelope.eventType().trim())
                .aggregateType(envelope.aggregateType().trim())
                .aggregateId(envelope.aggregateIdSafe())
                .eventVersion(envelope.eventVersionSafe())
                .producer(envelope.producerSafe())
                .occurredAt(envelope.occurredAt())
                .estadoProcesamiento(EstadoKafkaProcesamiento.RECIBIDO)
                .payloadJson(payloadJson.trim())
                .estado(true)
                .build();

        try {
            return repository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            log.warn(
                    "Evento Kafka duplicado detectado durante registro concurrente. eventId={}, topic={}, eventType={}",
                    envelope.eventId(),
                    topic,
                    envelope.eventType()
            );
            return repository.findByEventId(envelope.eventId()).orElseThrow(() -> ex);
        }
    }

    @Override
    @Transactional
    public void marcarProcesado(UUID eventId) {
        KafkaEventoConsumido evento = resolverPorEventId(eventId);

        if (evento.getEstadoProcesamiento() == EstadoKafkaProcesamiento.PROCESADO) {
            return;
        }

        evento.setEstadoProcesamiento(EstadoKafkaProcesamiento.PROCESADO);
        evento.setProcessedAt(LocalDateTime.now(clock));
        evento.setLastError(null);

        repository.save(evento);
    }

    @Override
    @Transactional
    public void marcarIgnorado(UUID eventId, String motivo) {
        validator.validarMotivoIgnorado(motivo);

        KafkaEventoConsumido evento = resolverPorEventId(eventId);

        if (evento.getEstadoProcesamiento() == EstadoKafkaProcesamiento.PROCESADO) {
            return;
        }

        evento.setEstadoProcesamiento(EstadoKafkaProcesamiento.IGNORADO);
        evento.setProcessedAt(LocalDateTime.now(clock));
        evento.setLastError(truncate(motivo, 4000));

        repository.save(evento);
    }

    @Override
    @Transactional
    public void marcarError(UUID eventId, Exception exception) {
        validator.validarError(exception);

        KafkaEventoConsumido evento = resolverPorEventId(eventId);

        if (evento.getEstadoProcesamiento() == EstadoKafkaProcesamiento.PROCESADO
                || evento.getEstadoProcesamiento() == EstadoKafkaProcesamiento.IGNORADO) {
            log.warn(
                    "Se ignoró marca de error para evento Kafka en estado final. eventId={}, estado={}",
                    eventId,
                    evento.getEstadoProcesamiento()
            );
            return;
        }

        evento.setEstadoProcesamiento(EstadoKafkaProcesamiento.ERROR);
        evento.setProcessedAt(LocalDateTime.now(clock));
        evento.setLastError(truncate(errorMessage(exception), 4000));

        repository.save(evento);

        log.error(
                "Evento Kafka marcado con error. eventId={}, topic={}, eventType={}, aggregateType={}, aggregateId={}",
                evento.getEventId(),
                evento.getTopic(),
                evento.getEventType(),
                evento.getAggregateType(),
                evento.getAggregateId(),
                exception
        );
    }

    @Override
    @Transactional(readOnly = true)
    public KafkaEventoConsumido resolverPorEventId(UUID eventId) {
        validator.validarEventId(eventId);

        return repository.findByEventId(eventId)
                .filter(KafkaEventoConsumido::isActivo)
                .orElseThrow(() -> new NotFoundException(
                        RECURSO_EVENTO_CONSUMIDO + " no encontrado con eventId: " + eventId
                ));
    }

    private String errorMessage(Exception exception) {
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

    private String truncate(String value, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();
        return normalized.length() <= max ? normalized : normalized.substring(0, max);
    }
}