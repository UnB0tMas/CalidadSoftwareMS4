// ruta: src/main/java/com/upsjb/ms4/repository/EventoDominioOutboxRepository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.kafka.EventoDominioOutbox;
import com.upsjb.ms4.domain.enums.EstadoOutbox;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventoDominioOutboxRepository extends
        JpaRepository<EventoDominioOutbox, Long>,
        JpaSpecificationExecutor<EventoDominioOutbox> {

    Optional<EventoDominioOutbox> findByEventId(UUID eventId);

    Optional<EventoDominioOutbox> findByEventIdAndEstadoTrue(UUID eventId);

    boolean existsByEventId(UUID eventId);

    Optional<EventoDominioOutbox> findByAggregateTypeAndAggregateIdAndEventTypeAndEstadoTrue(
            String aggregateType,
            String aggregateId,
            String eventType
    );

    Page<EventoDominioOutbox> findByStatusAndEstadoTrue(EstadoOutbox status, Pageable pageable);

    Page<EventoDominioOutbox> findByAggregateTypeAndAggregateIdAndEstadoTrue(
            String aggregateType,
            String aggregateId,
            Pageable pageable
    );

    List<EventoDominioOutbox> findByStatusInAndEstadoTrueOrderByCreatedAtAsc(
            Collection<EstadoOutbox> statuses,
            Pageable pageable
    );

    long countByStatusAndEstadoTrue(EstadoOutbox status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select e
            from EventoDominioOutbox e
            where e.status in :statuses
              and e.estado = true
              and (e.lockedAt is null or e.lockedAt < :expiredBefore)
              and (e.attempts is null or e.maxAttempts is null or e.attempts < e.maxAttempts)
            order by e.createdAt asc
            """)
    List<EventoDominioOutbox> findPublishableForUpdate(
            @Param("statuses") Collection<EstadoOutbox> statuses,
            @Param("expiredBefore") LocalDateTime expiredBefore,
            Pageable pageable
    );
}