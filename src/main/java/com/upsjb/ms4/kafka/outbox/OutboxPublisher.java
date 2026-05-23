// ruta: src/main/java/com/upsjb/ms4/kafka/outbox/OutboxPublisher.java
package com.upsjb.ms4.kafka.outbox;

import com.upsjb.ms4.domain.entity.kafka.EventoDominioOutbox;
import com.upsjb.ms4.kafka.producer.KafkaMessagePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxLockService lockService;
    private final KafkaMessagePublisher publisher;

    public OutboxPublisher(OutboxLockService lockService,
                           KafkaMessagePublisher publisher) {
        this.lockService = lockService;
        this.publisher = publisher;
    }

    public void publishPending() {
        for (EventoDominioOutbox event : lockService.claimPending()) {
            try {
                publisher.publish(event.getTopic(), event.getEventKey(), event.getPayloadJson());
                lockService.markPublished(event.getId());
            } catch (Exception ex) {
                log.error("Error publicando Outbox id={}, eventId={}, topic={}: {}",
                        event.getId(), event.getEventId(), event.getTopic(), ex.getMessage(), ex);
                lockService.markError(event.getId(), ex.getMessage());
            }
        }
    }
}