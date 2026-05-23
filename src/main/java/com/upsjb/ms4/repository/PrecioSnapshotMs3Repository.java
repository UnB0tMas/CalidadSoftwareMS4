// ruta: src/main/java/com/upsjb/ms4/repository/PrecioSnapshotMs3Repository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.snapshot.PrecioSnapshotMs3;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PrecioSnapshotMs3Repository extends
        JpaRepository<PrecioSnapshotMs3, Long>,
        JpaSpecificationExecutor<PrecioSnapshotMs3> {

    Optional<PrecioSnapshotMs3> findByIdPrecioHistorialMs3(Long idPrecioHistorialMs3);

    Optional<PrecioSnapshotMs3> findByIdPrecioHistorialMs3AndEstadoTrue(Long idPrecioHistorialMs3);

    Optional<PrecioSnapshotMs3> findFirstByIdSkuMs3AndVigenteTrueAndEstadoTrueOrderByFechaInicioDesc(Long idSkuMs3);

    List<PrecioSnapshotMs3> findByIdSkuMs3AndEstadoTrueOrderByFechaInicioDesc(Long idSkuMs3);

    Page<PrecioSnapshotMs3> findByIdProductoMs3AndEstadoTrue(Long idProductoMs3, Pageable pageable);

    Optional<PrecioSnapshotMs3> findByEventId(UUID eventId);

    boolean existsByEventId(UUID eventId);
}