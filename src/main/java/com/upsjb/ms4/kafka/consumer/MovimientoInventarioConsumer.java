package com.upsjb.ms4.kafka.consumer;

import com.upsjb.ms4.kafka.handler.MovimientoInventarioEventHandler;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class MovimientoInventarioConsumer {

    private final MovimientoInventarioEventHandler handler;
    private final KafkaConsumerExecutionService executionService;

    public MovimientoInventarioConsumer(
            MovimientoInventarioEventHandler handler,
            KafkaConsumerExecutionService executionService
    ) {
        this.handler = handler;
        this.executionService = executionService;
    }

    @KafkaListener(
            topics = "${ms4.kafka.topics.ms3-movimiento-inventario:ms3.movimiento-inventario.v1}",
            groupId = "${spring.kafka.consumer.group-id:ms4-snapshot-consumer}"
    )
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        executionService.consume(record, acknowledgment, handler::handle);
    }
}