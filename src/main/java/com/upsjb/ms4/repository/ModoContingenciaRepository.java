// ruta: src/main/java/com/upsjb/ms4/repository/ModoContingenciaRepository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.contingencia.ModoContingencia;
import com.upsjb.ms4.domain.enums.EstadoContingencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ModoContingenciaRepository extends
        JpaRepository<ModoContingencia, Long>,
        JpaSpecificationExecutor<ModoContingencia> {

    Optional<ModoContingencia> findFirstByEstadoContingenciaAndEstadoTrueOrderByFechaInicioDesc(
            EstadoContingencia estadoContingencia
    );

    Optional<ModoContingencia> findFirstByServicioAfectadoAndEstadoContingenciaAndEstadoTrueOrderByFechaInicioDesc(
            String servicioAfectado,
            EstadoContingencia estadoContingencia
    );

    boolean existsByEstadoContingenciaAndEstadoTrue(EstadoContingencia estadoContingencia);

    boolean existsByServicioAfectadoAndEstadoContingenciaAndEstadoTrue(
            String servicioAfectado,
            EstadoContingencia estadoContingencia
    );

    Page<ModoContingencia> findByEstadoContingenciaAndEstadoTrue(
            EstadoContingencia estadoContingencia,
            Pageable pageable
    );
}