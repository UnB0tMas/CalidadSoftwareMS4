// ruta: src/main/java/com/upsjb/ms4/specification/CajaMovimientoSpecification.java
package com.upsjb.ms4.specification;

import com.upsjb.ms4.domain.entity.caja.CajaMovimiento;
import com.upsjb.ms4.dto.caja.filter.CajaMovimientoFilterDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class CajaMovimientoSpecification extends SpecificationSupport {

    private CajaMovimientoSpecification() {
    }

    public static Specification<CajaMovimiento> build(CajaMovimientoFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            addLikeAny(
                    predicates,
                    cb,
                    filter.search(),
                    root.get("descripcion"),
                    root.get("actorRol"),
                    root.get("requestId"),
                    root.get("correlationId")
            );

            addEqual(predicates, cb, root.get("idCaja"), filter.idCaja());
            addEqual(predicates, cb, root.get("idVenta"), filter.idVenta());
            addEqual(predicates, cb, root.get("idPago"), filter.idPago());
            addEqual(predicates, cb, root.get("tipoMovimiento"), filter.tipoMovimiento());
            addEqual(predicates, cb, root.get("actorIdUsuarioMs1"), filter.actorIdUsuarioMs1());
            addEqualIgnoreCase(predicates, cb, root.get("actorRol"), filter.actorRol());
            addEqual(predicates, cb, root.get("estado"), filter.estado());
            addRange(predicates, cb, root.<LocalDateTime>get("createdAt"), filter.fechaDesde(), filter.fechaHasta());

            return and(cb, predicates);
        };
    }
}