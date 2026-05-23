// ruta: src/main/java/com/upsjb/ms4/repository/PromocionSkuDescuentoSnapshotMs3Repository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.snapshot.PromocionSkuDescuentoSnapshotMs3;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PromocionSkuDescuentoSnapshotMs3Repository extends
        JpaRepository<PromocionSkuDescuentoSnapshotMs3, Long>,
        JpaSpecificationExecutor<PromocionSkuDescuentoSnapshotMs3> {

    Optional<PromocionSkuDescuentoSnapshotMs3> findByIdPromocionSkuDescuentoVersionMs3(
            Long idPromocionSkuDescuentoVersionMs3
    );

    Optional<PromocionSkuDescuentoSnapshotMs3> findByIdPromocionSkuDescuentoVersionMs3AndEstadoTrue(
            Long idPromocionSkuDescuentoVersionMs3
    );

    List<PromocionSkuDescuentoSnapshotMs3> findByIdPromocionVersionMs3AndEstadoTrue(
            Long idPromocionVersionMs3
    );

    List<PromocionSkuDescuentoSnapshotMs3> findByIdSkuMs3AndEstadoTrueOrderByPrioridadAsc(
            Long idSkuMs3
    );

    Optional<PromocionSkuDescuentoSnapshotMs3> findFirstByIdSkuMs3AndEstadoTrueOrderByPrioridadAsc(
            Long idSkuMs3
    );

    Page<PromocionSkuDescuentoSnapshotMs3> findByIdPromocionMs3AndEstadoTrue(
            Long idPromocionMs3,
            Pageable pageable
    );
}