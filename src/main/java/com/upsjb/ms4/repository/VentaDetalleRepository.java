// ruta: src/main/java/com/upsjb/ms4/repository/VentaDetalleRepository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.venta.VentaDetalle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface VentaDetalleRepository extends
        JpaRepository<VentaDetalle, Long>,
        JpaSpecificationExecutor<VentaDetalle> {

    List<VentaDetalle> findByIdVentaAndEstadoTrueOrderByIdAsc(Long idVenta);

    Page<VentaDetalle> findByIdVentaAndEstadoTrue(Long idVenta, Pageable pageable);

    List<VentaDetalle> findByIdVentaInAndEstadoTrue(Collection<Long> idsVenta);

    Optional<VentaDetalle> findByIdAndIdVentaAndEstadoTrue(Long id, Long idVenta);

    List<VentaDetalle> findByIdSkuMs3AndEstadoTrueOrderByCreatedAtDesc(Long idSkuMs3);

    List<VentaDetalle> findByIdPromocionMs3AndEstadoTrueOrderByCreatedAtDesc(Long idPromocionMs3);

    @Query("""
            select d.idSkuMs3,
                   d.codigoSku,
                   d.nombreProducto,
                   sum(d.cantidad),
                   sum(d.totalLinea)
            from VentaDetalle d
            where d.estado = true
              and d.idVenta in :idsVenta
            group by d.idSkuMs3, d.codigoSku, d.nombreProducto
            order by sum(d.cantidad) desc
            """)
    List<Object[]> resumenProductosVendidosByIdsVenta(@Param("idsVenta") Collection<Long> idsVenta);
}