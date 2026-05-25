package com.upsjb.ms4.kafka.outbox;

import com.upsjb.ms4.domain.entity.kafka.EventoDominioOutbox;
import com.upsjb.ms4.kafka.producer.KafkaMessagePublisher;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxLockService lockService;
    private final KafkaMessagePublisher publisher;

    public OutboxPublisher(
            OutboxLockService lockService,
            KafkaMessagePublisher publisher
    ) {
        this.lockService = lockService;
        this.publisher = publisher;
    }

    public void publishPending() {
        for (EventoDominioOutbox event : lockService.claimPending()) {
            publishOne(event);
        }
    }

    private void publishOne(EventoDominioOutbox event) {
        if (event == null || event.getId() == null) {
            return;
        }

        try {
            RecordMetadata metadata = publisher.publish(
                    event.getTopic(),
                    event.getEventKey(),
                    event.getPayloadJson()
            );

            lockService.markPublished(event.getId());

            log.info(
                    "Evento Outbox MS4 publicado. id={}, eventId={}, topic={}, key={}, partition={}, offset={}",
                    event.getId(),
                    event.getEventId(),
                    event.getTopic(),
                    event.getEventKey(),
                    metadata == null ? null : metadata.partition(),
                    metadata == null ? null : metadata.offset()
            );
        } catch (Exception ex) {
            log.error(
                    "Error publicando Outbox MS4. id={}, eventId={}, topic={}, error={}",
                    event.getId(),
                    event.getEventId(),
                    event.getTopic(),
                    ex.getMessage(),
                    ex
            );

            lockService.markError(event.getId(), ex.getMessage());
        }
    }
}