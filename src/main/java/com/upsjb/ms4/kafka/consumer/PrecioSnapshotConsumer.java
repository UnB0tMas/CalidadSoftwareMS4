// ruta: src/main/java/com/upsjb/ms4/kafka/consumer/PrecioSnapshotConsumer.java
package com.upsjb.ms4.kafka.consumer;

import com.upsjb.ms4.kafka.handler.PrecioSnapshotEventHandler;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class PrecioSnapshotConsumer {

    private final PrecioSnapshotEventHandler handler;

    public PrecioSnapshotConsumer(PrecioSnapshotEventHandler handler) {
        this.handler = handler;
    }

    @KafkaListener(
            topics = "${ms4.kafka.topics.ms3-precio-snapshot:ms3.precio.snapshot.v1}",
            groupId = "${spring.kafka.consumer.group-id:ms4-snapshot-consumer}"
    )
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        handler.handle(record.topic(), record.key(), record.partition(), record.offset(), record.value());
        acknowledgment.acknowledge();
    }
}