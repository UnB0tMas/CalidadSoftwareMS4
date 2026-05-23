// ruta: src/main/java/com/upsjb/ms4/repository/CajaMovimientoRepository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.caja.CajaMovimiento;
import com.upsjb.ms4.domain.enums.TipoMovimientoCaja;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface CajaMovimientoRepository extends
        JpaRepository<CajaMovimiento, Long>,
        JpaSpecificationExecutor<CajaMovimiento> {

    List<CajaMovimiento> findByIdCajaAndEstadoTrueOrderByCreatedAtAsc(Long idCaja);

    Page<CajaMovimiento> findByIdCajaAndEstadoTrue(Long idCaja, Pageable pageable);

    List<CajaMovimiento> findByIdVentaAndEstadoTrue(Long idVenta);

    List<CajaMovimiento> findByIdPagoAndEstadoTrue(Long idPago);

    Page<CajaMovimiento> findByActorIdUsuarioMs1AndEstadoTrue(Long actorIdUsuarioMs1, Pageable pageable);

    @Query("""
            select coalesce(sum(m.monto), 0)
            from CajaMovimiento m
            where m.idCaja = :idCaja
              and m.tipoMovimiento in :tipos
              and m.estado = true
            """)
    BigDecimal sumMontoByIdCajaAndTipos(
            @Param("idCaja") Long idCaja,
            @Param("tipos") Collection<TipoMovimientoCaja> tipos
    );

    @Query("""
            select count(m)
            from CajaMovimiento m
            where m.idCaja = :idCaja
              and m.tipoMovimiento = :tipo
              and m.estado = true
            """)
    long countByIdCajaAndTipoMovimiento(
            @Param("idCaja") Long idCaja,
            @Param("tipo") TipoMovimientoCaja tipo
    );
}