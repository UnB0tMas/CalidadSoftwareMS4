// ruta: src/main/java/com/upsjb/ms4/repository/StockSnapshotMs3Repository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.snapshot.StockSnapshotMs3;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockSnapshotMs3Repository extends
        JpaRepository<StockSnapshotMs3, Long>,
        JpaSpecificationExecutor<StockSnapshotMs3> {

    Optional<StockSnapshotMs3> findByIdStockMs3(Long idStockMs3);

    Optional<StockSnapshotMs3> findByIdStockMs3AndEstadoTrue(Long idStockMs3);

    Optional<StockSnapshotMs3> findByIdSkuMs3AndIdAlmacenMs3(Long idSkuMs3, Long idAlmacenMs3);

    Optional<StockSnapshotMs3> findByIdSkuMs3AndIdAlmacenMs3AndEstadoTrue(Long idSkuMs3, Long idAlmacenMs3);

    List<StockSnapshotMs3> findByIdSkuMs3AndEstadoTrueOrderByStockDisponibleDesc(Long idSkuMs3);

    List<StockSnapshotMs3> findByIdProductoMs3AndEstadoTrueOrderByNombreAlmacenAsc(Long idProductoMs3);

    Page<StockSnapshotMs3> findByIdAlmacenMs3AndEstadoTrue(Long idAlmacenMs3, Pageable pageable);

    Page<StockSnapshotMs3> findByStockDisponibleGreaterThanAndEstadoTrue(Integer stockDisponible, Pageable pageable);

    Optional<StockSnapshotMs3> findByEventId(UUID eventId);

    boolean existsByEventId(UUID eventId);
}