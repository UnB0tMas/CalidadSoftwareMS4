// ruta: src/main/java/com/upsjb/ms4/kafka/producer/KafkaMessagePublisher.java
package com.upsjb.ms4.kafka.producer;

import com.upsjb.ms4.config.OutboxProperties;
import com.upsjb.ms4.shared.exception.KafkaPublishException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class KafkaMessagePublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxProperties outboxProperties;

    public KafkaMessagePublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            OutboxProperties outboxProperties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.outboxProperties = outboxProperties;
    }

    public void publish(String topic, String key, String payloadJson) {
        if (topic == null || topic.isBlank()) {
            throw new KafkaPublishException("El topic Kafka es obligatorio.");
        }

        if (payloadJson == null || payloadJson.isBlank()) {
            throw new KafkaPublishException("El payload Kafka es obligatorio.");
        }

        try {
            kafkaTemplate
                    .send(topic.trim(), normalizeKey(key), payloadJson)
                    .get(outboxProperties.publishTimeoutMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new KafkaPublishException("La publicación Kafka fue interrumpida.", ex);
        } catch (Exception ex) {
            throw new KafkaPublishException("No se pudo publicar mensaje Kafka en topic: " + topic, ex);
        }
    }

    private String normalizeKey(String key) {
        return key == null || key.isBlank() ? null : key.trim();
    }
}