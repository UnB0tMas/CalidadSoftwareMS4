package com.upsjb.ms4.kafka.consumer;

import com.upsjb.ms4.kafka.handler.EmpleadoSnapshotEventHandler;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class EmpleadoSnapshotConsumer {

    private final EmpleadoSnapshotEventHandler handler;
    private final KafkaConsumerExecutionService executionService;

    public EmpleadoSnapshotConsumer(
            EmpleadoSnapshotEventHandler handler,
            KafkaConsumerExecutionService executionService
    ) {
        this.handler = handler;
        this.executionService = executionService;
    }

    @KafkaListener(
            topics = "${ms4.kafka.topics.ms2-empleado-snapshot:ms2.empleado.snapshot.v1}",
            groupId = "${spring.kafka.consumer.group-id:ms4-snapshot-consumer}"
    )
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        executionService.consume(record, acknowledgment, handler::handle);
    }
}