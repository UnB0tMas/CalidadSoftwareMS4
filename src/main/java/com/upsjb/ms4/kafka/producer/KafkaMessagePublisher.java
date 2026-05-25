package com.upsjb.ms4.kafka.producer;

import com.upsjb.ms4.config.OutboxProperties;
import com.upsjb.ms4.shared.exception.KafkaPublishException;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

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

    public RecordMetadata publish(String topic, String key, String payloadJson) {
        if (topic == null || topic.isBlank()) {
            throw new KafkaPublishException("El topic Kafka es obligatorio.");
        }

        if (payloadJson == null || payloadJson.isBlank()) {
            throw new KafkaPublishException("El payload Kafka es obligatorio.");
        }

        try {
            SendResult<String, String> result = kafkaTemplate
                    .send(topic.trim(), normalizeKey(key), payloadJson)
                    .get(outboxProperties.publishTimeoutMillis(), TimeUnit.MILLISECONDS);

            return result.getRecordMetadata();
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