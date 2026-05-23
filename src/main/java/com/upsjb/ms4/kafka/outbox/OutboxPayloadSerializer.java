// ruta: src/main/java/com/upsjb/ms4/kafka/outbox/OutboxPayloadSerializer.java
package com.upsjb.ms4.kafka.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms4.shared.exception.KafkaPublishException;
import org.springframework.stereotype.Component;

@Component
public class OutboxPayloadSerializer {

    private final ObjectMapper objectMapper;

    public OutboxPayloadSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String serialize(Object payload) {
        if (payload == null) {
            throw new KafkaPublishException("El payload Outbox es obligatorio.");
        }

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new KafkaPublishException("No se pudo serializar payload Outbox.", ex);
        }
    }
}