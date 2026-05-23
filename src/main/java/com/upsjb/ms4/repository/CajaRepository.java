// ruta: src/main/java/com/upsjb/ms4/repository/CajaRepository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.caja.Caja;
import com.upsjb.ms4.domain.enums.EstadoCaja;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CajaRepository extends
        JpaRepository<Caja, Long>,
        JpaSpecificationExecutor<Caja> {

    Optional<Caja> findByCodigoCajaIgnoreCase(String codigoCaja);

    Optional<Caja> findByCodigoCajaIgnoreCaseAndEstadoTrue(String codigoCaja);

    Optional<Caja> findByFechaOperacionAndEstadoCajaAndEstadoTrue(LocalDate fechaOperacion, EstadoCaja estadoCaja);

    Optional<Caja> findFirstByEstadoCajaAndEstadoTrueOrderByFechaAperturaDesc(EstadoCaja estadoCaja);

    boolean existsByFechaOperacionAndEstadoCajaAndEstadoTrue(LocalDate fechaOperacion, EstadoCaja estadoCaja);

    List<Caja> findByFechaOperacionBetweenAndEstadoTrueOrderByFechaOperacionDesc(
            LocalDate fechaInicio,
            LocalDate fechaFin
    );

    Page<Caja> findByEstadoCajaAndEstadoTrue(EstadoCaja estadoCaja, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select c
            from Caja c
            where c.id = :id
            """)
    Optional<Caja> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select c
            from Caja c
            where c.fechaOperacion = :fechaOperacion
              and c.estadoCaja = :estadoCaja
              and c.estado = true
            """)
    Optional<Caja> findByFechaOperacionAndEstadoCajaForUpdate(
            @Param("fechaOperacion") LocalDate fechaOperacion,
            @Param("estadoCaja") EstadoCaja estadoCaja
    );
}