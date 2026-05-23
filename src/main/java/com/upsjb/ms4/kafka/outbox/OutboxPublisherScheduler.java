// ruta: src/main/java/com/upsjb/ms4/kafka/outbox/OutboxPublisherScheduler.java
package com.upsjb.ms4.kafka.outbox;

import com.upsjb.ms4.config.OutboxProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxPublisherScheduler {

    private final OutboxPublisher publisher;
    private final OutboxProperties properties;

    public OutboxPublisherScheduler(OutboxPublisher publisher,
                                    OutboxProperties properties) {
        this.publisher = publisher;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${ms4.outbox.fixed-delay-ms:5000}")
    public void publishPending() {
        if (properties.enabledSafe()) {
            publisher.publishPending();
        }
    }
}