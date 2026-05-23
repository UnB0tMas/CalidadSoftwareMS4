// ruta: src/main/java/com/upsjb/ms4/repository/StripeEventoRepository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.pago.StripeEvento;
import com.upsjb.ms4.domain.enums.EstadoKafkaProcesamiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface StripeEventoRepository extends
        JpaRepository<StripeEvento, Long>,
        JpaSpecificationExecutor<StripeEvento> {

    Optional<StripeEvento> findByStripeEventId(String stripeEventId);

    Optional<StripeEvento> findByStripeEventIdAndEstadoTrue(String stripeEventId);

    boolean existsByStripeEventId(String stripeEventId);

    Page<StripeEvento> findByStripePaymentIntentIdAndEstadoTrue(String stripePaymentIntentId, Pageable pageable);

    Page<StripeEvento> findByEstadoProcesamientoAndEstadoTrue(
            EstadoKafkaProcesamiento estadoProcesamiento,
            Pageable pageable
    );
}