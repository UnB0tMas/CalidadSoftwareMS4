// ruta: src/main/java/com/upsjb/ms4/repository/ConfiguracionEmpresaVersionRepository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.config.ConfiguracionEmpresaVersion;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConfiguracionEmpresaVersionRepository extends
        JpaRepository<ConfiguracionEmpresaVersion, Long>,
        JpaSpecificationExecutor<ConfiguracionEmpresaVersion> {

    Optional<ConfiguracionEmpresaVersion> findByCodigoVersionIgnoreCase(String codigoVersion);

    boolean existsByCodigoVersionIgnoreCase(String codigoVersion);

    Optional<ConfiguracionEmpresaVersion> findFirstByVigenteTrueAndEstadoTrueOrderByFechaInicioVigenciaDesc();

    List<ConfiguracionEmpresaVersion> findByVigenteTrueAndEstadoTrueOrderByFechaInicioVigenciaDesc();

    List<ConfiguracionEmpresaVersion> findByFechaInicioVigenciaLessThanEqualAndEstadoTrueOrderByFechaInicioVigenciaDesc(
            LocalDateTime fecha
    );

    boolean existsByVigenteTrueAndEstadoTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ConfiguracionEmpresaVersion c where c.id = ?1")
    Optional<ConfiguracionEmpresaVersion> findByIdForUpdate(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select c
            from ConfiguracionEmpresaVersion c
            where c.vigente = true
              and c.estado = true
            order by c.fechaInicioVigencia desc
            """)
    List<ConfiguracionEmpresaVersion> findVigentesActivasForUpdate();
}