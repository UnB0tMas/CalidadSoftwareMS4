// ruta: src/main/java/com/upsjb/ms4/validator/SnapshotValidator.java
package com.upsjb.ms4.validator;

import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SnapshotValidator extends ValidatorSupport {

    public void validarEnvelope(DomainEventEnvelopeDto<?> envelope) {
        require(envelope, "El envelope Kafka es obligatorio.");
        require(envelope.eventId(), "El eventId es obligatorio.");
        requireText(envelope.eventType(), "El eventType es obligatorio.");
        requireText(envelope.sourceServiceSafe(), "El sourceService es obligatorio.");
        requireText(envelope.aggregateType(), "El aggregateType es obligatorio.");
        requireText(envelope.aggregateIdSafe(), "El aggregateId es obligatorio.");
        validarVersionEvento(envelope.eventVersionSafe(), null);
        require(envelope.occurredAt(), "La fecha del evento es obligatoria.");
        require(envelope.payload(), "El payload del evento es obligatorio.");
    }

    public void validarVersionEvento(Integer nuevaVersion, Integer versionActual) {
        if (nuevaVersion == null || nuevaVersion <= 0) {
            fail("La versión del evento debe ser mayor a cero.");
        }

        if (versionActual != null && nuevaVersion < versionActual) {
            conflict("No se puede aplicar un evento snapshot con versión anterior.");
        }
    }

    public void validarPayloadJson(String payloadJson) {
        requireJson(payloadJson, "El payloadJson");
    }

    public void validarEventoNoObsoleto(LocalDateTime nuevoOccurredAt, LocalDateTime actualOccurredAt) {
        require(nuevoOccurredAt, "La fecha del evento nuevo es obligatoria.");

        if (actualOccurredAt != null && nuevoOccurredAt.isBefore(actualOccurredAt)) {
            conflict("No se puede aplicar un evento snapshot obsoleto.");
        }
    }
}