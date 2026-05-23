// ruta: src/main/java/com/upsjb/ms4/repository/ConfiguracionTributariaVersionRepository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.config.ConfiguracionTributariaVersion;
import com.upsjb.ms4.domain.enums.NombreImpuesto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConfiguracionTributariaVersionRepository extends
        JpaRepository<ConfiguracionTributariaVersion, Long>,
        JpaSpecificationExecutor<ConfiguracionTributariaVersion> {

    Optional<ConfiguracionTributariaVersion> findByCodigoVersionIgnoreCase(String codigoVersion);

    boolean existsByCodigoVersionIgnoreCase(String codigoVersion);

    Optional<ConfiguracionTributariaVersion> findFirstByNombreImpuestoAndVigenteTrueAndEstadoTrueOrderByFechaInicioVigenciaDesc(
            NombreImpuesto nombreImpuesto
    );

    List<ConfiguracionTributariaVersion> findByNombreImpuestoAndVigenteTrueAndEstadoTrueOrderByFechaInicioVigenciaDesc(
            NombreImpuesto nombreImpuesto
    );

    List<ConfiguracionTributariaVersion> findByNombreImpuestoAndFechaInicioVigenciaLessThanEqualAndEstadoTrueOrderByFechaInicioVigenciaDesc(
            NombreImpuesto nombreImpuesto,
            LocalDateTime fecha
    );

    boolean existsByNombreImpuestoAndVigenteTrueAndEstadoTrue(NombreImpuesto nombreImpuesto);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select c
            from ConfiguracionTributariaVersion c
            where c.id = :id
            """)
    Optional<ConfiguracionTributariaVersion> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select c
            from ConfiguracionTributariaVersion c
            where c.nombreImpuesto = :nombreImpuesto
              and c.vigente = true
              and c.estado = true
            order by c.fechaInicioVigencia desc
            """)
    List<ConfiguracionTributariaVersion> findVigentesByNombreImpuestoForUpdate(
            @Param("nombreImpuesto") NombreImpuesto nombreImpuesto
    );
}