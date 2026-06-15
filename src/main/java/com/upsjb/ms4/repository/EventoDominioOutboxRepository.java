package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.kafka.EventoDominioOutbox;
import com.upsjb.ms4.domain.enums.EstadoOutbox;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventoDominioOutboxRepository
        extends
        JpaRepository<EventoDominioOutbox, Long>,
        JpaSpecificationExecutor<EventoDominioOutbox> {

    Optional<EventoDominioOutbox> findByEventId(
            UUID eventId
    );

    Optional<EventoDominioOutbox>
    findByEventIdAndEstadoTrue(
            UUID eventId
    );

    boolean existsByEventId(
            UUID eventId
    );

    Optional<EventoDominioOutbox>
    findByAggregateTypeAndAggregateIdAndEventTypeAndEstadoTrue(
            String aggregateType,
            String aggregateId,
            String eventType
    );

    Optional<EventoDominioOutbox>
    findByAggregateTypeAndAggregateIdAndEventTypeAndEventKeyAndEstadoTrue(
            String aggregateType,
            String aggregateId,
            String eventType,
            String eventKey
    );

    Page<EventoDominioOutbox>
    findByStatusAndEstadoTrue(
            EstadoOutbox status,
            Pageable pageable
    );

    Page<EventoDominioOutbox>
    findByAggregateTypeAndAggregateIdAndEstadoTrue(
            String aggregateType,
            String aggregateId,
            Pageable pageable
    );

    List<EventoDominioOutbox>
    findByStatusInAndEstadoTrueOrderByCreatedAtAsc(
            Collection<EstadoOutbox> statuses,
            Pageable pageable
    );

    long countByStatusAndEstadoTrue(
            EstadoOutbox status
    );

    default List<EventoDominioOutbox>
    findPublishableForUpdate(
            Collection<EstadoOutbox> statuses,
            EstadoOutbox publishedStatus,
            LocalDateTime expiredBefore,
            Pageable pageable
    ) {
        return findPublishableForUpdateInternal(
                statuses,
                EstadoOutbox.PUBLICANDO,
                List.of(
                        publishedStatus,
                        EstadoOutbox.DESCARTADO
                ),
                expiredBefore,
                pageable
        );
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select event
            from EventoDominioOutbox event
            where event.estado = true
              and (
                    (
                        event.status in :statuses
                        and (
                            event.lockedAt is null
                            or event.lockedAt < :expiredBefore
                        )
                    )
                    or (
                        event.status = :publishingStatus
                        and event.lockedAt < :expiredBefore
                    )
              )
              and (
                    event.attempts is null
                    or event.maxAttempts is null
                    or event.attempts < event.maxAttempts
              )
              and not exists (
                    select previous.id
                    from EventoDominioOutbox previous
                    where previous.estado = true
                      and previous.topic = event.topic
                      and previous.eventKey = event.eventKey
                      and previous.id < event.id
                      and previous.status not in :terminalStatuses
              )
            order by event.createdAt asc, event.id asc
            """)
    List<EventoDominioOutbox>
    findPublishableForUpdateInternal(
            @Param("statuses")
            Collection<EstadoOutbox> statuses,

            @Param("publishingStatus")
            EstadoOutbox publishingStatus,

            @Param("terminalStatuses")
            Collection<EstadoOutbox> terminalStatuses,

            @Param("expiredBefore")
            LocalDateTime expiredBefore,

            Pageable pageable
    );
}