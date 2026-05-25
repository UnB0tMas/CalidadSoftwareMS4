package com.upsjb.ms4.kafka.consumer;

import com.upsjb.ms4.kafka.handler.PromocionSnapshotEventHandler;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class PromocionSnapshotConsumer {

    private final PromocionSnapshotEventHandler handler;
    private final KafkaConsumerExecutionService executionService;

    public PromocionSnapshotConsumer(
            PromocionSnapshotEventHandler handler,
            KafkaConsumerExecutionService executionService
    ) {
        this.handler = handler;
        this.executionService = executionService;
    }

    @KafkaListener(
            topics = "${ms4.kafka.topics.ms3-promocion-snapshot:ms3.promocion.snapshot.v1}",
            groupId = "${spring.kafka.consumer.group-id:ms4-snapshot-consumer}"
    )
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        executionService.consume(record, acknowledgment, handler::handle);
    }
}