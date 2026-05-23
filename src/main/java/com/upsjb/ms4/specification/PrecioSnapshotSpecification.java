// ruta: src/main/java/com/upsjb/ms4/specification/PrecioSnapshotSpecification.java
package com.upsjb.ms4.specification;

import com.upsjb.ms4.domain.entity.snapshot.PrecioSnapshotMs3;
import com.upsjb.ms4.dto.snapshot.filter.PrecioVentaFilterDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class PrecioSnapshotSpecification extends SpecificationSupport {

    private PrecioSnapshotSpecification() {
    }

    public static Specification<PrecioSnapshotMs3> build(PrecioVentaFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            addEqual(predicates, cb, root.get("idPrecioHistorialMs3"), filter.idPrecioHistorialMs3());
            addEqual(predicates, cb, root.get("idSkuMs3"), filter.idSkuMs3());
            addEqual(predicates, cb, root.get("idProductoMs3"), filter.idProductoMs3());
            addEqualIgnoreCase(predicates, cb, root.get("codigoSku"), filter.codigoSku());
            addEqualIgnoreCase(predicates, cb, root.get("codigoProducto"), filter.codigoProducto());
            addEqualIgnoreCase(predicates, cb, root.get("moneda"), filter.moneda());
            addEqual(predicates, cb, root.get("vigente"), filter.vigente());
            addEqual(predicates, cb, root.get("estado"), filter.estado());
            addRange(predicates, cb, root.<LocalDateTime>get("fechaInicio"), filter.fechaInicioDesde(), filter.fechaInicioHasta());

            return and(cb, predicates);
        };
    }

    public static Specification<PrecioSnapshotMs3> vigentePorSku(Long idSkuMs3) {
        return (root, query, cb) -> {
            if (idSkuMs3 == null) {
                return cb.disjunction();
            }

            return cb.and(
                    cb.equal(root.get("idSkuMs3"), idSkuMs3),
                    cb.isTrue(root.get("vigente")),
                    cb.isTrue(root.get("estado"))
            );
        };
    }
}