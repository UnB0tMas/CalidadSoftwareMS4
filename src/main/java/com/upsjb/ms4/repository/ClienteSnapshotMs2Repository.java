// ruta: src/main/java/com/upsjb/ms4/repository/ClienteSnapshotMs2Repository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.snapshot.ClienteSnapshotMs2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ClienteSnapshotMs2Repository extends
        JpaRepository<ClienteSnapshotMs2, Long>,
        JpaSpecificationExecutor<ClienteSnapshotMs2> {

    Optional<ClienteSnapshotMs2> findByIdClienteMs2(Long idClienteMs2);

    Optional<ClienteSnapshotMs2> findByIdClienteMs2AndEstadoTrue(Long idClienteMs2);

    Optional<ClienteSnapshotMs2> findByIdUsuarioMs1(Long idUsuarioMs1);

    Optional<ClienteSnapshotMs2> findByIdUsuarioMs1AndEstadoTrue(Long idUsuarioMs1);

    Optional<ClienteSnapshotMs2> findByEventId(UUID eventId);

    boolean existsByEventId(UUID eventId);

    boolean existsByIdClienteMs2(Long idClienteMs2);

    boolean existsByIdClienteMs2AndEstadoTrue(Long idClienteMs2);

    Optional<ClienteSnapshotMs2> findFirstByNumeroDocumentoPersonaAndEstadoTrueOrderByFechaSincronizacionDesc(
            String numeroDocumentoPersona
    );

    Optional<ClienteSnapshotMs2> findFirstByRucAndEstadoTrueOrderByFechaSincronizacionDesc(String ruc);

    Page<ClienteSnapshotMs2> findByEstadoTrueAndClienteActivoMs2True(Pageable pageable);
}