// ruta: src/main/java/com/upsjb/ms4/validator/OutboxValidator.java
package com.upsjb.ms4.validator;

import com.upsjb.ms4.domain.entity.kafka.EventoDominioOutbox;
import com.upsjb.ms4.domain.enums.EstadoOutbox;
import com.upsjb.ms4.dto.kafka.filter.EventoDominioOutboxFilterDto;
import org.springframework.stereotype.Component;

@Component
public class OutboxValidator extends ValidatorSupport {

    public void validarIdOutbox(Long idOutbox) {
        requirePositive(idOutbox, "El evento outbox");
    }

    public void validarEvento(String aggregateType,
                              String aggregateId,
                              String topic,
                              String eventType,
                              String payloadJson) {
        requireText(aggregateType, "El aggregateType es obligatorio.");
        requireText(aggregateId, "El aggregateId es obligatorio.");
        requireText(topic, "El topic es obligatorio.");
        requireText(eventType, "El eventType es obligatorio.");
        requireJson(payloadJson, "El payloadJson");
        requireMaxLength(aggregateType, 80, "El aggregateType");
        requireMaxLength(aggregateId, 120, "El aggregateId");
        requireMaxLength(topic, 180, "El topic");
        requireMaxLength(eventType, 120, "El eventType");
    }

    public void validarFiltro(EventoDominioOutboxFilterDto filter) {
        if (filter == null) {
            return;
        }

        requireMaxLength(filter.search(), 150, "El texto de búsqueda");
        requireMaxLength(filter.aggregateType(), 80, "El aggregateType");
        requireMaxLength(filter.aggregateId(), 120, "El aggregateId");
        requireMaxLength(filter.topic(), 180, "El topic");
        requireMaxLength(filter.eventKey(), 180, "El eventKey");
        requireMaxLength(filter.eventType(), 120, "El eventType");
        requireMaxLength(filter.lockedBy(), 120, "El lockedBy");

        if (filter.attemptsMin() != null && filter.attemptsMax() != null
                && filter.attemptsMax() < filter.attemptsMin()) {
            fail("El rango de intentos no puede tener máximo menor que mínimo.");
        }

        requireDateRange(filter.fechaCreacionDesde(), filter.fechaCreacionHasta(), "El rango de creación");
        requireDateRange(filter.fechaPublicacionDesde(), filter.fechaPublicacionHasta(), "El rango de publicación");
        requireDateRange(filter.fechaBloqueoDesde(), filter.fechaBloqueoHasta(), "El rango de bloqueo");
    }

    public void validarReintento(EventoDominioOutbox evento) {
        require(evento, "El evento outbox es obligatorio.");

        if (evento.getStatus() == EstadoOutbox.PUBLICADO) {
            conflict("No se puede reintentar un evento ya publicado.");
        }

        if (evento.getStatus() == EstadoOutbox.PUBLICANDO) {
            conflict("No se puede reintentar un evento que está publicándose.");
        }

        if (evento.getStatus() == EstadoOutbox.DESCARTADO) {
            conflict("No se puede reintentar un evento descartado.");
        }
    }

    public void validarDescartar(EventoDominioOutbox evento) {
        require(evento, "El evento outbox es obligatorio.");

        if (evento.getStatus() == EstadoOutbox.PUBLICADO) {
            conflict("No se puede descartar un evento ya publicado.");
        }

        if (evento.getStatus() == EstadoOutbox.PUBLICANDO) {
            conflict("No se puede descartar un evento que está publicándose.");
        }

        if (evento.getStatus() == EstadoOutbox.DESCARTADO) {
            conflict("El evento outbox ya se encuentra descartado.");
        }
    }

    public void validarPublicable(EventoDominioOutbox evento) {
        require(evento, "El evento outbox es obligatorio.");

        if (evento.getStatus() == EstadoOutbox.PUBLICADO) {
            conflict("El evento outbox ya fue publicado.");
        }

        if (evento.getStatus() == EstadoOutbox.DESCARTADO) {
            conflict("El evento outbox fue descartado.");
        }

        validarMaxAttempts(evento.getAttempts(), evento.getMaxAttempts());
    }

    public void validarMaxAttempts(Integer attempts, Integer maxAttempts) {
        if (attempts == null || maxAttempts == null) {
            return;
        }

        if (maxAttempts <= 0) {
            fail("El número máximo de intentos debe ser mayor a cero.");
        }

        if (attempts >= maxAttempts) {
            conflict("El evento outbox superó el número máximo de intentos.");
        }
    }
}