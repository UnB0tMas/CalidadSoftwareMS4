// ruta: src/main/java/com/upsjb/ms4/kafka/outbox/OutboxLockService.java
package com.upsjb.ms4.kafka.outbox;

import com.upsjb.ms4.config.OutboxProperties;
import com.upsjb.ms4.domain.entity.kafka.EventoDominioOutbox;
import com.upsjb.ms4.domain.enums.EstadoOutbox;
import com.upsjb.ms4.repository.EventoDominioOutboxRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutboxLockService {

    private final EventoDominioOutboxRepository repository;
    private final OutboxProperties properties;

    public OutboxLockService(
            EventoDominioOutboxRepository repository,
            OutboxProperties properties
    ) {
        this.repository = repository;
        this.properties = properties;
    }

    @Transactional
    public List<EventoDominioOutbox> claimPending() {
        String lockedBy = properties.publisherIdSafe();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredBefore = now.minus(properties.lockTtlSafe());

        List<EventoDominioOutbox> events = repository.findPublishableForUpdate(
                List.of(EstadoOutbox.PENDIENTE, EstadoOutbox.ERROR),
                expiredBefore,
                PageRequest.of(0, properties.batchSizeSafe())
        );

        for (EventoDominioOutbox event : events) {
            event.setStatus(EstadoOutbox.PUBLICANDO);
            event.setLockedBy(lockedBy);
            event.setLockedAt(now);

            if (event.getAttempts() == null) {
                event.setAttempts(0);
            }

            if (event.getMaxAttempts() == null || event.getMaxAttempts() <= 0) {
                event.setMaxAttempts(properties.maxAttemptsSafe());
            }
        }

        return repository.saveAll(events);
    }

    @Transactional
    public void markPublished(Long id) {
        EventoDominioOutbox event = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento Outbox no encontrado: " + id));

        event.setStatus(EstadoOutbox.PUBLICADO);
        event.setPublishedAt(LocalDateTime.now());
        event.setLockedBy(null);
        event.setLockedAt(null);
        event.setLastError(null);

        repository.save(event);
    }

    @Transactional
    public void markError(Long id, String error) {
        EventoDominioOutbox event = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento Outbox no encontrado: " + id));

        int attempts = event.getAttempts() == null ? 0 : event.getAttempts();
        int maxAttempts = event.getMaxAttempts() == null || event.getMaxAttempts() <= 0
                ? properties.maxAttemptsSafe()
                : event.getMaxAttempts();

        int nextAttempts = attempts + 1;

        event.setAttempts(nextAttempts);
        event.setStatus(nextAttempts >= maxAttempts ? EstadoOutbox.ERROR : EstadoOutbox.PENDIENTE);
        event.setLastError(error);
        event.setLockedBy(null);
        event.setLockedAt(null);

        repository.save(event);
    }
}