// ruta: src/main/java/com/upsjb/ms4/repository/PagoRepository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.pago.Pago;
import com.upsjb.ms4.domain.enums.EstadoPago;
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

public interface PagoRepository extends
        JpaRepository<Pago, Long>,
        JpaSpecificationExecutor<Pago> {

    Optional<Pago> findByCodigoPagoIgnoreCase(String codigoPago);

    Optional<Pago> findByCodigoPagoIgnoreCaseAndEstadoTrue(String codigoPago);

    boolean existsByCodigoPagoIgnoreCase(String codigoPago);

    List<Pago> findByIdVentaAndEstadoTrueOrderByCreatedAtDesc(Long idVenta);

    Optional<Pago> findFirstByIdVentaAndEstadoPagoInAndEstadoTrueOrderByCreatedAtDesc(
            Long idVenta,
            Collection<EstadoPago> estados
    );

    Optional<Pago> findByStripePaymentIntentId(String stripePaymentIntentId);

    Optional<Pago> findByStripePaymentIntentIdAndEstadoTrue(String stripePaymentIntentId);

    boolean existsByStripePaymentIntentId(String stripePaymentIntentId);

    Page<Pago> findByEstadoPagoAndEstadoTrue(EstadoPago estadoPago, Pageable pageable);

    Page<Pago> findByMetodoPagoAndEstadoTrue(MetodoPago metodoPago, Pageable pageable);

    @Query("""
            select coalesce(sum(p.monto), 0)
            from Pago p
            where p.estado = true
              and p.estadoPago in :estados
              and p.metodoPago = :metodoPago
              and p.fechaPago between :fechaInicio and :fechaFin
            """)
    BigDecimal sumMontoByEstadosMetodoPagoAndFechaPagoBetween(
            @Param("estados") Collection<EstadoPago> estados,
            @Param("metodoPago") MetodoPago metodoPago,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );
}