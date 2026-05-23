// ruta: src/main/java/com/upsjb/ms4/kafka/consumer/MovimientoInventarioConsumer.java
package com.upsjb.ms4.kafka.consumer;

import com.upsjb.ms4.kafka.handler.MovimientoInventarioEventHandler;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class MovimientoInventarioConsumer {

    private final MovimientoInventarioEventHandler handler;

    public MovimientoInventarioConsumer(MovimientoInventarioEventHandler handler) {
        this.handler = handler;
    }

    @KafkaListener(
            topics = "${ms4.kafka.topics.ms3-movimiento-inventario:ms3.movimiento-inventario.v1}",
            groupId = "${spring.kafka.consumer.group-id:ms4-snapshot-consumer}"
    )
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        handler.handle(record.topic(), record.key(), record.partition(), record.offset(), record.value());
        acknowledgment.acknowledge();
    }
}