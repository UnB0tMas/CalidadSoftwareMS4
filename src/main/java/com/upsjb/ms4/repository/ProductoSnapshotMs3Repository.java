// ruta: src/main/java/com/upsjb/ms4/repository/ProductoSnapshotMs3Repository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.snapshot.ProductoSnapshotMs3;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ProductoSnapshotMs3Repository extends
        JpaRepository<ProductoSnapshotMs3, Long>,
        JpaSpecificationExecutor<ProductoSnapshotMs3> {

    Optional<ProductoSnapshotMs3> findByIdProductoMs3(Long idProductoMs3);

    Optional<ProductoSnapshotMs3> findByIdProductoMs3AndEstadoTrue(Long idProductoMs3);

    Optional<ProductoSnapshotMs3> findByCodigoProductoIgnoreCase(String codigoProducto);

    Optional<ProductoSnapshotMs3> findBySlugIgnoreCase(String slug);

    Optional<ProductoSnapshotMs3> findByEventId(UUID eventId);

    boolean existsByEventId(UUID eventId);

    boolean existsByIdProductoMs3(Long idProductoMs3);

    Page<ProductoSnapshotMs3> findByEstadoTrueAndVisiblePublicoTrueAndVendibleTrue(Pageable pageable);

    Page<ProductoSnapshotMs3> findByIdCategoriaMs3AndEstadoTrueAndVisiblePublicoTrueAndVendibleTrue(
            Long idCategoriaMs3,
            Pageable pageable
    );

    Page<ProductoSnapshotMs3> findByIdMarcaMs3AndEstadoTrueAndVisiblePublicoTrueAndVendibleTrue(
            Long idMarcaMs3,
            Pageable pageable
    );
}