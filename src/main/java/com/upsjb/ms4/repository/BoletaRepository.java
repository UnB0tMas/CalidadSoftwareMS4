// ruta: src/main/java/com/upsjb/ms4/repository/BoletaRepository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.boleta.Boleta;
import com.upsjb.ms4.domain.enums.EstadoBoleta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BoletaRepository extends
        JpaRepository<Boleta, Long>,
        JpaSpecificationExecutor<Boleta> {

    Optional<Boleta> findByIdAndEstadoTrue(Long id);

    Optional<Boleta> findByIdVenta(Long idVenta);

    Optional<Boleta> findByIdVentaAndEstadoTrue(Long idVenta);

    boolean existsByIdVenta(Long idVenta);

    boolean existsByIdVentaAndEstadoTrue(Long idVenta);

    Optional<Boleta> findByCodigoBoletaIgnoreCase(String codigoBoleta);

    Optional<Boleta> findByCodigoBoletaIgnoreCaseAndEstadoTrue(String codigoBoleta);

    boolean existsByCodigoBoletaIgnoreCase(String codigoBoleta);

    Optional<Boleta> findBySerieIgnoreCaseAndNumeroAndEstadoTrue(String serie, Long numero);

    boolean existsBySerieIgnoreCaseAndNumero(String serie, Long numero);

    Page<Boleta> findByEstadoBoletaAndEstadoTrue(EstadoBoleta estadoBoleta, Pageable pageable);

    @Query("""
            select b
            from Boleta b
            join b.venta v
            where v.idUsuarioClienteMs1 = :idUsuarioClienteMs1
              and b.estado = true
            """)
    Page<Boleta> findByClienteUsuarioMs1(
            @Param("idUsuarioClienteMs1") Long idUsuarioClienteMs1,
            Pageable pageable
    );

    @Query("""
            select b
            from Boleta b
            join b.venta v
            where v.idUsuarioEmpleadoMs1 = :idUsuarioEmpleadoMs1
              and b.estado = true
            """)
    Page<Boleta> findByEmpleadoUsuarioMs1(
            @Param("idUsuarioEmpleadoMs1") Long idUsuarioEmpleadoMs1,
            Pageable pageable
    );
}