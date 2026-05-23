// ruta: src/main/java/com/upsjb/ms4/repository/BoletaPlantillaVersionRepository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.config.BoletaPlantillaVersion;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BoletaPlantillaVersionRepository extends
        JpaRepository<BoletaPlantillaVersion, Long>,
        JpaSpecificationExecutor<BoletaPlantillaVersion> {

    Optional<BoletaPlantillaVersion> findByCodigoVersionIgnoreCase(String codigoVersion);

    boolean existsByCodigoVersionIgnoreCase(String codigoVersion);

    Optional<BoletaPlantillaVersion> findFirstByVigenteTrueAndEstadoTrueOrderByFechaInicioVigenciaDesc();

    List<BoletaPlantillaVersion> findByVigenteTrueAndEstadoTrueOrderByFechaInicioVigenciaDesc();

    List<BoletaPlantillaVersion> findByFechaInicioVigenciaLessThanEqualAndEstadoTrueOrderByFechaInicioVigenciaDesc(
            LocalDateTime fecha
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p
            from BoletaPlantillaVersion p
            where p.id = :id
            """)
    Optional<BoletaPlantillaVersion> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p
            from BoletaPlantillaVersion p
            where p.vigente = true
              and p.estado = true
            order by p.fechaInicioVigencia desc
            """)
    List<BoletaPlantillaVersion> findVigentesForUpdate();
}