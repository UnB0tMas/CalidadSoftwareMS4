// ruta: src/main/java/com/upsjb/ms4/repository/InventarioEventoPendienteMs4Repository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.kafka.InventarioEventoPendienteMs4;
import com.upsjb.ms4.domain.enums.EstadoSincronizacionInventario;
import com.upsjb.ms4.domain.enums.TipoComandoStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InventarioEventoPendienteMs4Repository extends
        JpaRepository<InventarioEventoPendienteMs4, Long>,
        JpaSpecificationExecutor<InventarioEventoPendienteMs4> {

    Optional<InventarioEventoPendienteMs4> findByIdempotencyKey(String idempotencyKey);

    Optional<InventarioEventoPendienteMs4> findByIdempotencyKeyAndEstadoTrue(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    List<InventarioEventoPendienteMs4> findByEstadoSincronizacionInAndEstadoTrueOrderByFechaCreacionAsc(
            Collection<EstadoSincronizacionInventario> estados,
            Pageable pageable
    );

    Page<InventarioEventoPendienteMs4> findByEstadoSincronizacionAndEstadoTrue(
            EstadoSincronizacionInventario estadoSincronizacion,
            Pageable pageable
    );

    List<InventarioEventoPendienteMs4> findByIdVentaAndEstadoTrueOrderByFechaCreacionAsc(Long idVenta);

    List<InventarioEventoPendienteMs4> findByCodigoVentaAndEstadoTrueOrderByFechaCreacionAsc(String codigoVenta);

    List<InventarioEventoPendienteMs4> findByTipoEventoAndEstadoSincronizacionAndEstadoTrue(
            TipoComandoStock tipoEvento,
            EstadoSincronizacionInventario estadoSincronizacion
    );

    long countByEstadoSincronizacionInAndEstadoTrue(Collection<EstadoSincronizacionInventario> estados);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select e
            from InventarioEventoPendienteMs4 e
            where e.estadoSincronizacion in :estados
              and e.estado = true
            order by e.fechaCreacion asc
            """)
    List<InventarioEventoPendienteMs4> findPendientesForUpdate(
            @Param("estados") Collection<EstadoSincronizacionInventario> estados,
            Pageable pageable
    );
}