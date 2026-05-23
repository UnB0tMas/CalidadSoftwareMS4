package com.upsjb.ms4.specification;

import com.upsjb.ms4.domain.entity.snapshot.SkuSnapshotMs3;
import com.upsjb.ms4.dto.snapshot.filter.SkuVentaFilterDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class SkuSnapshotSpecification extends SpecificationSupport {

    private SkuSnapshotSpecification() {
    }

    public static Specification<SkuSnapshotMs3> build(SkuVentaFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            addLikeAny(
                    predicates,
                    cb,
                    filter.search(),
                    root.get("codigoProducto"),
                    root.get("codigoSku"),
                    root.get("barcode"),
                    root.get("color"),
                    root.get("talla"),
                    root.get("material"),
                    root.get("modelo"),
                    root.get("estadoSku")
            );

            addEqual(predicates, cb, root.get("idSkuMs3"), filter.idSkuMs3());
            addEqual(predicates, cb, root.get("idProductoMs3"), filter.idProductoMs3());
            addEqualIgnoreCase(predicates, cb, root.get("codigoProducto"), filter.codigoProducto());
            addEqualIgnoreCase(predicates, cb, root.get("codigoSku"), filter.codigoSku());
            addEqualIgnoreCase(predicates, cb, root.get("barcode"), filter.barcode());
            addEqualIgnoreCase(predicates, cb, root.get("color"), filter.color());
            addEqualIgnoreCase(predicates, cb, root.get("talla"), filter.talla());
            addEqualIgnoreCase(predicates, cb, root.get("estadoSku"), filter.estadoSku());
            addEqual(predicates, cb, root.get("estado"), filter.estado());

            return and(cb, predicates);
        };
    }

    public static Specification<SkuSnapshotMs3> vendibles(SkuVentaFilterDto filter) {
        return Specification
                .where(build(filter))
                .and((root, query, cb) -> cb.and(
                        cb.isTrue(root.get("estado"))
                ));
    }
}