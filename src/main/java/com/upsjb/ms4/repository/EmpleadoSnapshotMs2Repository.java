// ruta: src/main/java/com/upsjb/ms4/repository/EmpleadoSnapshotMs2Repository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.snapshot.EmpleadoSnapshotMs2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface EmpleadoSnapshotMs2Repository extends
        JpaRepository<EmpleadoSnapshotMs2, Long>,
        JpaSpecificationExecutor<EmpleadoSnapshotMs2> {

    Optional<EmpleadoSnapshotMs2> findByIdEmpleadoMs2(Long idEmpleadoMs2);

    Optional<EmpleadoSnapshotMs2> findByIdEmpleadoMs2AndEstadoTrue(Long idEmpleadoMs2);

    Optional<EmpleadoSnapshotMs2> findByIdUsuarioMs1(Long idUsuarioMs1);

    Optional<EmpleadoSnapshotMs2> findByIdUsuarioMs1AndEstadoTrue(Long idUsuarioMs1);

    Optional<EmpleadoSnapshotMs2> findByCodigoEmpleadoIgnoreCase(String codigoEmpleado);

    Optional<EmpleadoSnapshotMs2> findByCodigoEmpleadoIgnoreCaseAndEstadoTrue(String codigoEmpleado);

    Optional<EmpleadoSnapshotMs2> findByEventId(UUID eventId);

    boolean existsByEventId(UUID eventId);

    Page<EmpleadoSnapshotMs2> findByEstadoTrueAndEmpleadoActivoMs2True(Pageable pageable);
}