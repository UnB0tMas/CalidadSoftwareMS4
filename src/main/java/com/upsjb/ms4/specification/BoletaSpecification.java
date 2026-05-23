// ruta: src/main/java/com/upsjb/ms4/specification/BoletaSpecification.java
package com.upsjb.ms4.specification;

import com.upsjb.ms4.domain.entity.boleta.Boleta;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.dto.boleta.filter.BoletaFilterDto;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class BoletaSpecification extends SpecificationSupport {

    private BoletaSpecification() {
    }

    public static Specification<Boleta> build(BoletaFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            addLikeAny(
                    predicates,
                    cb,
                    filter.search(),
                    root.get("codigoBoleta"),
                    root.get("serie"),
                    root.get("numeroDocumentoCliente"),
                    root.get("nombreCliente"),
                    root.get("correoCliente"),
                    root.get("rucEmisor"),
                    root.get("razonSocialEmisor")
            );

            addEqual(predicates, cb, root.get("idVenta"), filter.idVenta());
            addEqualIgnoreCase(predicates, cb, root.get("serie"), filter.serie());
            addEqual(predicates, cb, root.get("numero"), filter.numero());
            addEqualIgnoreCase(predicates, cb, root.get("codigoBoleta"), filter.codigoBoleta());
            addEqualIgnoreCase(predicates, cb, root.get("numeroDocumentoCliente"), filter.numeroDocumentoCliente());
            addEqualIgnoreCase(predicates, cb, root.get("correoCliente"), filter.correoCliente());
            addEqual(predicates, cb, root.get("estadoBoleta"), filter.estadoBoleta());
            addEqual(predicates, cb, root.get("enviadoPorCorreo"), filter.enviadoPorCorreo());
            addEqual(predicates, cb, root.get("estado"), filter.estado());
            addRange(predicates, cb, root.<LocalDateTime>get("fechaEmision"), filter.fechaDesde(), filter.fechaHasta());

            return and(cb, predicates);
        };
    }

    public static Specification<Boleta> delClienteUsuario(Long idUsuarioClienteMs1) {
        return (root, query, cb) -> {
            if (idUsuarioClienteMs1 == null) {
                return cb.disjunction();
            }

            Join<Boleta, Venta> venta = root.join("venta", JoinType.INNER);
            return cb.equal(venta.get("idUsuarioClienteMs1"), idUsuarioClienteMs1);
        };
    }

    public static Specification<Boleta> delEmpleadoUsuario(Long idUsuarioEmpleadoMs1) {
        return (root, query, cb) -> {
            if (idUsuarioEmpleadoMs1 == null) {
                return cb.disjunction();
            }

            Join<Boleta, Venta> venta = root.join("venta", JoinType.INNER);
            return cb.equal(venta.get("idUsuarioEmpleadoMs1"), idUsuarioEmpleadoMs1);
        };
    }
}