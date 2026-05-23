// ruta: src/main/java/com/upsjb/ms4/repository/PromocionSnapshotMs3Repository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.snapshot.PromocionSnapshotMs3;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface PromocionSnapshotMs3Repository extends
        JpaRepository<PromocionSnapshotMs3, Long>,
        JpaSpecificationExecutor<PromocionSnapshotMs3> {

    Optional<PromocionSnapshotMs3> findByIdPromocionVersionMs3(Long idPromocionVersionMs3);

    Optional<PromocionSnapshotMs3> findByIdPromocionVersionMs3AndEstadoTrue(Long idPromocionVersionMs3);

    Optional<PromocionSnapshotMs3> findByIdPromocionMs3(Long idPromocionMs3);

    Optional<PromocionSnapshotMs3> findFirstByIdPromocionMs3AndVigenteTrueAndEstadoTrueOrderByFechaInicioDesc(
            Long idPromocionMs3
    );

    Optional<PromocionSnapshotMs3> findByCodigoPromocionIgnoreCase(String codigoPromocion);

    Optional<PromocionSnapshotMs3> findByEventId(UUID eventId);

    boolean existsByEventId(UUID eventId);

    Page<PromocionSnapshotMs3> findByVigenteTrueAndVisiblePublicoTrueAndEstadoTrue(Pageable pageable);
}