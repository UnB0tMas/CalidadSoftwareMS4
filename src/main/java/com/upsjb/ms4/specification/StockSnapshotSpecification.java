// ruta: src/main/java/com/upsjb/ms4/specification/StockSnapshotSpecification.java
package com.upsjb.ms4.specification;

import com.upsjb.ms4.domain.entity.snapshot.StockSnapshotMs3;
import com.upsjb.ms4.dto.snapshot.filter.StockVentaFilterDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class StockSnapshotSpecification extends SpecificationSupport {

    private StockSnapshotSpecification() {
    }

    public static Specification<StockSnapshotMs3> build(StockVentaFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            addLikeAny(
                    predicates,
                    cb,
                    filter.search(),
                    root.get("codigoSku"),
                    root.get("barcode"),
                    root.get("codigoProducto"),
                    root.get("nombreProducto"),
                    root.get("codigoAlmacen"),
                    root.get("nombreAlmacen")
            );

            addEqual(predicates, cb, root.get("idStockMs3"), filter.idStockMs3());
            addEqual(predicates, cb, root.get("idSkuMs3"), filter.idSkuMs3());
            addEqual(predicates, cb, root.get("idProductoMs3"), filter.idProductoMs3());
            addEqual(predicates, cb, root.get("idAlmacenMs3"), filter.idAlmacenMs3());
            addEqualIgnoreCase(predicates, cb, root.get("codigoSku"), filter.codigoSku());
            addEqualIgnoreCase(predicates, cb, root.get("barcode"), filter.barcode());
            addEqualIgnoreCase(predicates, cb, root.get("codigoProducto"), filter.codigoProducto());
            addEqualIgnoreCase(predicates, cb, root.get("codigoAlmacen"), filter.codigoAlmacen());
            addEqual(predicates, cb, root.get("bajoStock"), filter.bajoStock());
            addEqual(predicates, cb, root.get("sobreStock"), filter.sobreStock());
            addEqual(predicates, cb, root.get("estado"), filter.estado());

            if (filter.conStockDisponible() != null) {
                if (Boolean.TRUE.equals(filter.conStockDisponible())) {
                    predicates.add(cb.greaterThan(root.get("stockDisponible"), 0));
                } else {
                    predicates.add(cb.lessThanOrEqualTo(root.get("stockDisponible"), 0));
                }
            }

            return and(cb, predicates);
        };
    }

    public static Specification<StockSnapshotMs3> conStockDisponible() {
        return (root, query, cb) -> cb.and(
                cb.isTrue(root.get("estado")),
                cb.greaterThan(root.get("stockDisponible"), 0)
        );
    }
}