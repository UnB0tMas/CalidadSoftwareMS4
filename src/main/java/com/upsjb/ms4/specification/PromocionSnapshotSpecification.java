// ruta: src/main/java/com/upsjb/ms4/specification/PromocionSnapshotSpecification.java
package com.upsjb.ms4.specification;

import com.upsjb.ms4.domain.entity.snapshot.PromocionSnapshotMs3;
import com.upsjb.ms4.dto.snapshot.filter.PromocionVentaFilterDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class PromocionSnapshotSpecification extends SpecificationSupport {

    private PromocionSnapshotSpecification() {
    }

    public static Specification<PromocionSnapshotMs3> build(PromocionVentaFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            addLikeAny(
                    predicates,
                    cb,
                    filter.search(),
                    root.get("codigoPromocion"),
                    root.get("nombre"),
                    root.get("descripcion"),
                    root.get("estadoPromocion"),
                    root.get("motivo")
            );

            addEqual(predicates, cb, root.get("idPromocionMs3"), filter.idPromocionMs3());
            addEqual(predicates, cb, root.get("idPromocionVersionMs3"), filter.idPromocionVersionMs3());
            addEqualIgnoreCase(predicates, cb, root.get("codigoPromocion"), filter.codigoPromocion());
            addEqualIgnoreCase(predicates, cb, root.get("estadoPromocion"), filter.estadoPromocion());
            addEqual(predicates, cb, root.get("visiblePublico"), filter.visiblePublico());
            addEqual(predicates, cb, root.get("vigente"), filter.vigente());
            addEqual(predicates, cb, root.get("estado"), filter.estado());
            addRange(predicates, cb, root.<LocalDateTime>get("fechaInicio"), filter.fechaDesde(), filter.fechaHasta());

            return and(cb, predicates);
        };
    }

    public static Specification<PromocionSnapshotMs3> vigentesPublicas(LocalDateTime fechaOperacion) {
        return (root, query, cb) -> {
            LocalDateTime fecha = fechaOperacion == null ? LocalDateTime.now() : fechaOperacion;

            return cb.and(
                    cb.isTrue(root.get("estado")),
                    cb.isTrue(root.get("vigente")),
                    cb.isTrue(root.get("visiblePublico")),
                    cb.lessThanOrEqualTo(root.get("fechaInicio"), fecha),
                    cb.greaterThanOrEqualTo(root.get("fechaFin"), fecha)
            );
        };
    }
}