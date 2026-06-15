package com.upsjb.ms4.kafka.consumer;

import com.upsjb.ms4.kafka.handler.EmpleadoSnapshotEventHandler;
import com.upsjb.ms4.kafka.probe.KafkaFunctionalSnapshotProbeExecutor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class EmpleadoSnapshotConsumer {

    private final EmpleadoSnapshotEventHandler handler;
    private final KafkaConsumerExecutionService executionService;
    private final KafkaFunctionalSnapshotProbeExecutor
            functionalProbeExecutor;

    public EmpleadoSnapshotConsumer(
            EmpleadoSnapshotEventHandler handler,
            KafkaConsumerExecutionService executionService,
            KafkaFunctionalSnapshotProbeExecutor functionalProbeExecutor
    ) {
        this.handler = handler;
        this.executionService = executionService;
        this.functionalProbeExecutor = functionalProbeExecutor;
    }

    @KafkaListener(
            topics = "${ms4.kafka.topics.ms2-empleado-snapshot:ms2.empleado.snapshot.v1}",
            groupId = "${spring.kafka.consumer.group-id:ms4-snapshot-consumer}"
    )
    public void consume(
            ConsumerRecord<String, String> record,
            Acknowledgment acknowledgment
    ) {
        if (
                functionalProbeExecutor
                        .isEmployeeProbe(
                                record == null
                                        ? null
                                        : record.value()
                        )
        ) {
            functionalProbeExecutor
                    .executeEmployee(
                            record,
                            handler
                    );

            acknowledgment.acknowledge();
            return;
        }

        executionService.consume(
                record,
                acknowledgment,
                handler::handle
        );
    }
}