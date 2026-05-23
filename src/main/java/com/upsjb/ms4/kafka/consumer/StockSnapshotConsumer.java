// ruta: src/main/java/com/upsjb/ms4/kafka/consumer/StockSnapshotConsumer.java
package com.upsjb.ms4.kafka.consumer;

import com.upsjb.ms4.kafka.handler.StockSnapshotEventHandler;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class StockSnapshotConsumer {

    private final StockSnapshotEventHandler handler;

    public StockSnapshotConsumer(StockSnapshotEventHandler handler) {
        this.handler = handler;
    }

    @KafkaListener(
            topics = "${ms4.kafka.topics.ms3-stock-snapshot:ms3.stock.snapshot.v1}",
            groupId = "${spring.kafka.consumer.group-id:ms4-snapshot-consumer}"
    )
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        handler.handle(record.topic(), record.key(), record.partition(), record.offset(), record.value());
        acknowledgment.acknowledge();
    }
}