// ruta: src/main/java/com/upsjb/ms4/validator/KafkaEventoConsumidoValidator.java
package com.upsjb.ms4.validator;

import com.upsjb.ms4.domain.entity.kafka.KafkaEventoConsumido;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class KafkaEventoConsumidoValidator extends ValidatorSupport {

    public void validarEventId(UUID eventId) {
        require(eventId, "El eventId es obligatorio.");
    }

    public void validarRegistroRecibido(DomainEventEnvelopeDto<?> envelope,
                                        String topic,
                                        String payloadJson) {
        require(envelope, "El envelope Kafka es obligatorio.");
        require(envelope.eventId(), "El eventId es obligatorio.");
        requireText(envelope.eventType(), "El eventType es obligatorio.");
        requireText(envelope.sourceServiceSafe(), "El sourceService es obligatorio.");
        requireText(envelope.aggregateType(), "El aggregateType es obligatorio.");
        requireText(envelope.aggregateIdSafe(), "El aggregateId es obligatorio.");
        requirePositive(envelope.eventVersionSafe(), "La versión del evento");
        require(envelope.occurredAt(), "La fecha de ocurrencia del evento es obligatoria.");
        requireText(topic, "El topic Kafka es obligatorio.");
        requireJson(payloadJson, "El payloadJson");
    }

    public void validarEventoExistente(KafkaEventoConsumido evento) {
        require(evento, "El evento Kafka consumido es obligatorio.");
    }

    public void validarMotivoIgnorado(String motivo) {
        requireText(motivo, "El motivo para ignorar el evento es obligatorio.");
        requireMaxLength(motivo, 4000, "El motivo para ignorar el evento");
    }

    public void validarError(Exception exception) {
        require(exception, "La excepción del procesamiento Kafka es obligatoria.");
    }
}