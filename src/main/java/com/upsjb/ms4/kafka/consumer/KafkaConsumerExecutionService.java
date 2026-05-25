package com.upsjb.ms4.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumerExecutionService {

    private final KafkaConsumerErrorHandler errorHandler;

    public KafkaConsumerExecutionService(KafkaConsumerErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public void consume(
            ConsumerRecord<String, String> record,
            Acknowledgment acknowledgment,
            KafkaRecordHandler handler
    ) {
        try {
            if (record == null) {
                throw new IllegalArgumentException("El record Kafka recibido por MS4 es obligatorio.");
            }

            if (record.value() == null || record.value().isBlank()) {
                throw new IllegalArgumentException("El payload Kafka recibido por MS4 está vacío.");
            }

            handler.handle(
                    record.topic(),
                    record.key(),
                    record.partition(),
                    record.offset(),
                    record.value()
            );

            acknowledgment.acknowledge();
        } catch (Exception ex) {
            errorHandler.handle(record, ex);
            acknowledgment.acknowledge();
        }
    }
}