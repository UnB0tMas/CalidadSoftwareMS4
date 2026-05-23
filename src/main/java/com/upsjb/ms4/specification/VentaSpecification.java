// ruta: src/main/java/com/upsjb/ms4/specification/VentaSpecification.java
package com.upsjb.ms4.specification;

import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.dto.venta.filter.VentaFilterDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class VentaSpecification extends SpecificationSupport {

    private VentaSpecification() {
    }

    public static Specification<Venta> build(VentaFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            addLikeAny(
                    predicates,
                    cb,
                    filter.search(),
                    root.get("codigoVenta"),
                    root.get("moneda"),
                    root.get("observacion"),
                    root.get("requestId"),
                    root.get("correlationId")
            );

            addEqualIgnoreCase(predicates, cb, root.get("codigoVenta"), filter.codigoVenta());
            addEqual(predicates, cb, root.get("canalVenta"), filter.canalVenta());
            addEqual(predicates, cb, root.get("estadoVenta"), filter.estadoVenta());
            addEqual(predicates, cb, root.get("metodoPagoPrincipal"), filter.metodoPagoPrincipal());
            addEqual(predicates, cb, root.get("idClienteSnapshot"), filter.idClienteSnapshot());
            addEqual(predicates, cb, root.get("idClienteMs2"), filter.idClienteMs2());
            addEqual(predicates, cb, root.get("idUsuarioClienteMs1"), filter.idUsuarioClienteMs1());
            addEqual(predicates, cb, root.get("idEmpleadoSnapshot"), filter.idEmpleadoSnapshot());
            addEqual(predicates, cb, root.get("idEmpleadoMs2"), filter.idEmpleadoMs2());
            addEqual(predicates, cb, root.get("idUsuarioEmpleadoMs1"), filter.idUsuarioEmpleadoMs1());
            addEqual(predicates, cb, root.get("idCaja"), filter.idCaja());
            addEqual(predicates, cb, root.get("estado"), filter.estado());
            addRange(predicates, cb, root.<LocalDateTime>get("fechaVenta"), filter.fechaDesde(), filter.fechaHasta());

            return and(cb, predicates);
        };
    }

    public static Specification<Venta> delClienteUsuario(Long idUsuarioClienteMs1) {
        return (root, query, cb) -> idUsuarioClienteMs1 == null
                ? cb.disjunction()
                : cb.equal(root.get("idUsuarioClienteMs1"), idUsuarioClienteMs1);
    }

    public static Specification<Venta> delEmpleadoUsuario(Long idUsuarioEmpleadoMs1) {
        return (root, query, cb) -> idUsuarioEmpleadoMs1 == null
                ? cb.disjunction()
                : cb.equal(root.get("idUsuarioEmpleadoMs1"), idUsuarioEmpleadoMs1);
    }
}