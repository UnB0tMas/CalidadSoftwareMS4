package com.upsjb.ms4.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumerExecutionService {

    public void consume(
            ConsumerRecord<String, String> record,
            Acknowledgment acknowledgment,
            KafkaRecordHandler handler
    ) {
        if (record == null) {
            throw new IllegalArgumentException(
                    "El record Kafka recibido por MS4 es obligatorio."
            );
        }

        if (
                record.value() == null
                        || record.value().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "El payload Kafka recibido por MS4 está vacío."
            );
        }

        if (acknowledgment == null) {
            throw new IllegalArgumentException(
                    "El acknowledgment Kafka de MS4 es obligatorio."
            );
        }

        if (handler == null) {
            throw new IllegalArgumentException(
                    "El handler Kafka de MS4 es obligatorio."
            );
        }

        /*
         * La excepción no debe capturarse aquí.
         *
         * Si el handler falla, la excepción se propaga al contenedor
         * y DefaultErrorHandler aplica la política de reintentos.
         *
         * El offset solo se confirma después de procesar correctamente
         * o después de que el error handler envíe el mensaje al DLT.
         */
        handler.handle(
                record.topic(),
                record.key(),
                record.partition(),
                record.offset(),
                record.value()
        );

        acknowledgment.acknowledge();
    }
}