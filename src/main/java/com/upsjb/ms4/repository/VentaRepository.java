// ruta: src/main/java/com/upsjb/ms4/repository/VentaRepository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.enums.CanalVenta;
import com.upsjb.ms4.domain.enums.EstadoVenta;
import com.upsjb.ms4.domain.enums.MetodoPago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface VentaRepository extends
        JpaRepository<Venta, Long>,
        JpaSpecificationExecutor<Venta> {

    Optional<Venta> findByCodigoVentaIgnoreCase(String codigoVenta);

    Optional<Venta> findByCodigoVentaIgnoreCaseAndEstadoTrue(String codigoVenta);

    boolean existsByCodigoVentaIgnoreCase(String codigoVenta);

    Optional<Venta> findByIdAndEstadoTrue(Long id);

    Optional<Venta> findByIdAndIdUsuarioClienteMs1AndEstadoTrue(Long id, Long idUsuarioClienteMs1);

    Optional<Venta> findByIdAndIdUsuarioEmpleadoMs1AndEstadoTrue(Long id, Long idUsuarioEmpleadoMs1);

    Page<Venta> findByIdUsuarioClienteMs1AndEstadoTrue(Long idUsuarioClienteMs1, Pageable pageable);

    Page<Venta> findByIdUsuarioEmpleadoMs1AndEstadoTrue(Long idUsuarioEmpleadoMs1, Pageable pageable);

    Page<Venta> findByEstadoVentaAndEstadoTrue(EstadoVenta estadoVenta, Pageable pageable);

    Page<Venta> findByCanalVentaAndEstadoTrue(CanalVenta canalVenta, Pageable pageable);

    List<Venta> findByIdCajaAndEstadoTrueOrderByFechaVentaAsc(Long idCaja);

    Page<Venta> findByIdCajaAndEstadoTrue(Long idCaja, Pageable pageable);

    @Query("""
            select coalesce(sum(v.total), 0)
            from Venta v
            where v.estado = true
              and v.estadoVenta in :estados
              and v.fechaVenta between :fechaInicio and :fechaFin
            """)
    BigDecimal sumTotalByEstadosAndFechaVentaBetween(
            @Param("estados") Collection<EstadoVenta> estados,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    @Query("""
            select coalesce(sum(v.total), 0)
            from Venta v
            where v.estado = true
              and v.estadoVenta in :estados
              and v.metodoPagoPrincipal = :metodoPago
              and v.fechaVenta between :fechaInicio and :fechaFin
            """)
    BigDecimal sumTotalByEstadosMetodoPagoAndFechaVentaBetween(
            @Param("estados") Collection<EstadoVenta> estados,
            @Param("metodoPago") MetodoPago metodoPago,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    long countByEstadoVentaAndEstadoTrue(EstadoVenta estadoVenta);

    long countByIdUsuarioEmpleadoMs1AndFechaVentaBetweenAndEstadoVentaAndEstadoTrue(
            Long idUsuarioEmpleadoMs1,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            EstadoVenta estadoVenta
    );
}