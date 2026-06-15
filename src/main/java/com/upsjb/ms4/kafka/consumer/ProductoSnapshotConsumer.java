package com.upsjb.ms4.kafka.consumer;

import com.upsjb.ms4.kafka.handler.ProductoSnapshotEventHandler;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProductoSnapshotConsumer {

    private final ProductoSnapshotEventHandler handler;

    public ProductoSnapshotConsumer(
            ProductoSnapshotEventHandler handler
    ) {
        this.handler = handler;
    }

    @KafkaListener(
            topics = "${ms4.kafka.topics.ms3-producto-snapshot:ms3.producto.snapshot.v2}",
            groupId = "${spring.kafka.consumer.group-id:ms4-snapshot-consumer}",
            containerFactory = "ms3KafkaListenerContainerFactory"
    )
    public void consume(
            ConsumerRecord<String, String> record
    ) throws Exception {
        validateRecord(record);

        handler.handle(
                record.topic(),
                record.key(),
                record.partition(),
                record.offset(),
                record.value()
        );
    }

    private void validateRecord(
            ConsumerRecord<String, String> record
    ) {
        if (record == null) {
            throw new IllegalArgumentException(
                    "El registro Kafka recibido desde MS3 es obligatorio."
            );
        }

        if (
                record.value() == null
                        || record.value().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "El payload Kafka recibido desde MS3 está vacío."
            );
        }
    }
}