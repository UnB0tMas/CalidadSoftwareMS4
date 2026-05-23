// ruta: src/main/java/com/upsjb/ms4/repository/SkuSnapshotMs3Repository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.snapshot.SkuSnapshotMs3;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface SkuSnapshotMs3Repository extends
        JpaRepository<SkuSnapshotMs3, Long>,
        JpaSpecificationExecutor<SkuSnapshotMs3> {

    Optional<SkuSnapshotMs3> findByIdSkuMs3(Long idSkuMs3);

    Optional<SkuSnapshotMs3> findByIdSkuMs3AndEstadoTrue(Long idSkuMs3);

    Optional<SkuSnapshotMs3> findByCodigoSkuIgnoreCase(String codigoSku);

    Optional<SkuSnapshotMs3> findByBarcodeIgnoreCase(String barcode);

    Optional<SkuSnapshotMs3> findByEventId(UUID eventId);

    boolean existsByEventId(UUID eventId);

    boolean existsByIdSkuMs3(Long idSkuMs3);

    Page<SkuSnapshotMs3> findByIdProductoMs3AndEstadoTrue(Long idProductoMs3, Pageable pageable);
}