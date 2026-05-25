package com.upsjb.ms4.kafka.outbox;

import com.upsjb.ms4.config.OutboxProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxPublisherScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherScheduler.class);

    private final OutboxPublisher publisher;
    private final OutboxProperties properties;

    public OutboxPublisherScheduler(
            OutboxPublisher publisher,
            OutboxProperties properties
    ) {
        this.publisher = publisher;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${ms4.outbox.fixed-delay-ms:5000}")
    public void publishPending() {
        if (!properties.enabledSafe()) {
            return;
        }

        try {
            publisher.publishPending();
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico ejecutando scheduler Outbox Kafka MS4. Continuará en la siguiente iteración.",
                    ex
            );
        }
    }
}