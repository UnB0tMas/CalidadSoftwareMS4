// ruta: src/main/java/com/upsjb/ms4/repository/AuditoriaFuncionalRepository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.auditoria.AuditoriaFuncional;
import com.upsjb.ms4.domain.enums.ResultadoAuditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AuditoriaFuncionalRepository extends
        JpaRepository<AuditoriaFuncional, Long>,
        JpaSpecificationExecutor<AuditoriaFuncional> {

    Optional<AuditoriaFuncional> findByIdAndEstadoTrue(Long id);

    Page<AuditoriaFuncional> findByEntidadAndEstadoTrue(String entidad, Pageable pageable);

    Page<AuditoriaFuncional> findByAccionAndEstadoTrue(String accion, Pageable pageable);

    Page<AuditoriaFuncional> findByResultadoAndEstadoTrue(ResultadoAuditoria resultado, Pageable pageable);

    Page<AuditoriaFuncional> findByActorIdUsuarioMs1AndEstadoTrue(Long actorIdUsuarioMs1, Pageable pageable);

    Page<AuditoriaFuncional> findByRequestIdAndEstadoTrue(String requestId, Pageable pageable);

    Page<AuditoriaFuncional> findByCorrelationIdAndEstadoTrue(String correlationId, Pageable pageable);

    Page<AuditoriaFuncional> findByCreatedAtBetweenAndEstadoTrue(
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Pageable pageable
    );
}