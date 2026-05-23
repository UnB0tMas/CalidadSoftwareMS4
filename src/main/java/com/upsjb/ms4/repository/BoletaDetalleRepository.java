// ruta: src/main/java/com/upsjb/ms4/repository/BoletaDetalleRepository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.boleta.BoletaDetalle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface BoletaDetalleRepository extends
        JpaRepository<BoletaDetalle, Long>,
        JpaSpecificationExecutor<BoletaDetalle> {

    List<BoletaDetalle> findByIdBoletaAndEstadoTrueOrderByIdAsc(Long idBoleta);

    Page<BoletaDetalle> findByIdBoletaAndEstadoTrue(Long idBoleta, Pageable pageable);

    Optional<BoletaDetalle> findByIdVentaDetalle(Long idVentaDetalle);

    Optional<BoletaDetalle> findByIdVentaDetalleAndEstadoTrue(Long idVentaDetalle);

    boolean existsByIdVentaDetalle(Long idVentaDetalle);

    List<BoletaDetalle> findByIdSkuMs3AndEstadoTrueOrderByCreatedAtDesc(Long idSkuMs3);

    List<BoletaDetalle> findByIdPromocionMs3AndEstadoTrueOrderByCreatedAtDesc(Long idPromocionMs3);
}