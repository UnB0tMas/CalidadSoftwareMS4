package com.upsjb.ms4.kafka.outbox;

import com.upsjb.ms4.config.OutboxProperties;
import com.upsjb.ms4.domain.entity.kafka.EventoDominioOutbox;
import com.upsjb.ms4.domain.enums.EstadoOutbox;
import com.upsjb.ms4.repository.EventoDominioOutboxRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxLockService {

    private final EventoDominioOutboxRepository repository;
    private final OutboxProperties properties;
    private final Clock clock;

    public OutboxLockService(
            EventoDominioOutboxRepository repository,
            OutboxProperties properties,
            Clock clock
    ) {
        this.repository = repository;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public List<EventoDominioOutbox> claimPending() {
        String lockedBy =
                properties.publisherIdSafe();

        LocalDateTime now =
                LocalDateTime.now(clock);

        LocalDateTime expiredBefore =
                now.minus(
                        properties.lockTtlSafe()
                );

        List<EventoDominioOutbox> events =
                repository.findPublishableForUpdate(
                        List.of(
                                EstadoOutbox.PENDIENTE,
                                EstadoOutbox.ERROR
                        ),
                        EstadoOutbox.PUBLICADO,
                        expiredBefore,
                        PageRequest.of(
                                0,
                                properties.batchSizeSafe()
                        )
                );

        for (EventoDominioOutbox event : events) {
            event.setStatus(
                    EstadoOutbox.PUBLICANDO
            );
            event.setLockedBy(lockedBy);
            event.setLockedAt(now);

            if (event.getAttempts() == null) {
                event.setAttempts(0);
            }

            if (
                    event.getMaxAttempts() == null
                            || event.getMaxAttempts() <= 0
            ) {
                event.setMaxAttempts(
                        properties.maxAttemptsSafe()
                );
            }
        }

        return repository.saveAll(events);
    }

    @Transactional
    public void markPublished(
            Long id
    ) {
        EventoDominioOutbox event =
                resolverEvento(id);

        event.setStatus(
                EstadoOutbox.PUBLICADO
        );
        event.setPublishedAt(
                LocalDateTime.now(clock)
        );
        event.setLockedBy(null);
        event.setLockedAt(null);
        event.setLastError(null);

        repository.save(event);
    }

    @Transactional
    public void markError(
            Long id,
            String error
    ) {
        EventoDominioOutbox event =
                resolverEvento(id);

        int attempts =
                event.getAttempts() == null
                        ? 0
                        : event.getAttempts();

        int maxAttempts =
                event.getMaxAttempts() == null
                        || event.getMaxAttempts() <= 0
                        ? properties.maxAttemptsSafe()
                        : event.getMaxAttempts();

        int nextAttempts =
                attempts + 1;

        event.setAttempts(nextAttempts);

        event.setStatus(
                nextAttempts >= maxAttempts
                        ? EstadoOutbox.ERROR
                        : EstadoOutbox.PENDIENTE
        );

        event.setLastError(
                truncate(
                        error,
                        4000
                )
        );
        event.setLockedBy(null);
        event.setLockedAt(null);

        repository.save(event);
    }

    private EventoDominioOutbox resolverEvento(
            Long id
    ) {
        if (
                id == null
                        || id <= 0
        ) {
            throw new IllegalArgumentException(
                    "El id del evento Outbox debe ser positivo."
            );
        }

        return repository
                .findById(id)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Evento Outbox no encontrado: "
                                                + id
                                )
                );
    }

    private String truncate(
            String value,
            int maxLength
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.length() <= maxLength
                ? normalized
                : normalized.substring(
                0,
                maxLength
        );
    }
}