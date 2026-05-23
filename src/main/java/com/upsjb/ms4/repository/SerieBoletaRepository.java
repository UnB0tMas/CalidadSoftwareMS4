// ruta: src/main/java/com/upsjb/ms4/repository/SerieBoletaRepository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.config.SerieBoleta;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SerieBoletaRepository extends
        JpaRepository<SerieBoleta, Long>,
        JpaSpecificationExecutor<SerieBoleta> {

    Optional<SerieBoleta> findBySerieIgnoreCase(String serie);

    Optional<SerieBoleta> findBySerieIgnoreCaseAndEstadoTrue(String serie);

    boolean existsBySerieIgnoreCase(String serie);

    List<SerieBoleta> findByEstadoTrueOrderBySerieAsc();

    Optional<SerieBoleta> findFirstByEstadoTrueOrderBySerieAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s
            from SerieBoleta s
            where s.id = :id
            """)
    Optional<SerieBoleta> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s
            from SerieBoleta s
            where upper(s.serie) = upper(:serie)
            """)
    Optional<SerieBoleta> findBySerieForUpdate(@Param("serie") String serie);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s
            from SerieBoleta s
            where s.estado = true
            order by s.serie asc
            """)
    List<SerieBoleta> findActivasForUpdate();
}