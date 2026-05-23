// ruta: src/main/java/com/upsjb/ms4/repository/CorreoOutboxRepository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.mail.CorreoOutbox;
import com.upsjb.ms4.domain.enums.EstadoCorreo;
import com.upsjb.ms4.domain.enums.TipoCorreo;
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

public interface CorreoOutboxRepository extends
        JpaRepository<CorreoOutbox, Long>,
        JpaSpecificationExecutor<CorreoOutbox> {

    Optional<CorreoOutbox> findByEventId(UUID eventId);

    Optional<CorreoOutbox> findByEventIdAndEstadoTrue(UUID eventId);

    boolean existsByEventId(UUID eventId);

    Page<CorreoOutbox> findByEstadoCorreoAndEstadoTrue(EstadoCorreo estadoCorreo, Pageable pageable);

    Page<CorreoOutbox> findByTipoCorreoAndEstadoTrue(TipoCorreo tipoCorreo, Pageable pageable);

    List<CorreoOutbox> findByIdBoletaAndEstadoTrueOrderByCreatedAtDesc(Long idBoleta);

    List<CorreoOutbox> findByEstadoCorreoInAndEstadoTrueOrderByFechaProgramadaAsc(
            Collection<EstadoCorreo> estados,
            Pageable pageable
    );

    long countByEstadoCorreoAndEstadoTrue(EstadoCorreo estadoCorreo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select c
            from CorreoOutbox c
            where c.estadoCorreo in :estados
              and c.estado = true
              and c.fechaProgramada <= :now
              and (c.attempts is null or c.maxAttempts is null or c.attempts < c.maxAttempts)
            order by c.fechaProgramada asc, c.createdAt asc
            """)
    List<CorreoOutbox> findProcessableForUpdate(
            @Param("estados") Collection<EstadoCorreo> estados,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );
}