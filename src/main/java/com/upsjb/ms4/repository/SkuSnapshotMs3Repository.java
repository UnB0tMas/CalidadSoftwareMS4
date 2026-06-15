package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.snapshot.SkuSnapshotMs3;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SkuSnapshotMs3Repository
        extends
        JpaRepository<SkuSnapshotMs3, Long>,
        JpaSpecificationExecutor<SkuSnapshotMs3> {

    Optional<SkuSnapshotMs3>
    findByIdSkuMs3(
            Long idSkuMs3
    );

    Optional<SkuSnapshotMs3>
    findByIdSkuMs3AndEstadoTrue(
            Long idSkuMs3
    );

    Optional<SkuSnapshotMs3>
    findByCodigoSkuIgnoreCase(
            String codigoSku
    );

    Optional<SkuSnapshotMs3>
    findByBarcodeIgnoreCase(
            String barcode
    );

    List<SkuSnapshotMs3>
    findAllByEventId(
            UUID eventId
    );

    boolean existsByEventId(
            UUID eventId
    );

    boolean existsByIdSkuMs3(
            Long idSkuMs3
    );

    Page<SkuSnapshotMs3>
    findByIdProductoMs3AndEstadoTrue(
            Long idProductoMs3,
            Pageable pageable
    );

    /*
     * Incluye SKU activos e inactivos porque
     * ms3.producto.snapshot.v2 es un reemplazo total.
     */
    List<SkuSnapshotMs3>
    findByIdProductoMs3(
            Long idProductoMs3
    );
}