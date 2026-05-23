// ruta: src/main/java/com/upsjb/ms4/kafka/consumer/PromocionSnapshotConsumer.java
package com.upsjb.ms4.kafka.consumer;

import com.upsjb.ms4.kafka.handler.PromocionSnapshotEventHandler;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class PromocionSnapshotConsumer {

    private final PromocionSnapshotEventHandler handler;

    public PromocionSnapshotConsumer(PromocionSnapshotEventHandler handler) {
        this.handler = handler;
    }

    @KafkaListener(
            topics = "${ms4.kafka.topics.ms3-promocion-snapshot:ms3.promocion.snapshot.v1}",
            groupId = "${spring.kafka.consumer.group-id:ms4-snapshot-consumer}"
    )
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        handler.handle(record.topic(), record.key(), record.partition(), record.offset(), record.value());
        acknowledgment.acknowledge();
    }
}