// ruta: src/main/java/com/upsjb/ms4/service/contract/kafka/KafkaEventoConsumidoService.java
package com.upsjb.ms4.service.contract.kafka;

import com.upsjb.ms4.domain.entity.kafka.KafkaEventoConsumido;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;

import java.util.UUID;

public interface KafkaEventoConsumidoService {

    boolean existeEvento(UUID eventId);

    KafkaEventoConsumido registrarRecibido(DomainEventEnvelopeDto<?> envelope,
                                           String topic,
                                           String payloadJson);

    void marcarProcesado(UUID eventId);

    void marcarIgnorado(UUID eventId, String motivo);

    void marcarError(UUID eventId, Exception exception);

    KafkaEventoConsumido resolverPorEventId(UUID eventId);
}