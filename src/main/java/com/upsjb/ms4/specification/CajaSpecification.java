// ruta: src/main/java/com/upsjb/ms4/specification/CajaSpecification.java
package com.upsjb.ms4.specification;

import com.upsjb.ms4.domain.entity.caja.Caja;
import com.upsjb.ms4.dto.caja.filter.CajaFilterDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class CajaSpecification extends SpecificationSupport {

    private CajaSpecification() {
    }

    public static Specification<Caja> build(CajaFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            addLikeAny(
                    predicates,
                    cb,
                    filter.search(),
                    root.get("codigoCaja"),
                    root.get("observacionApertura"),
                    root.get("observacionCierre")
            );

            addEqualIgnoreCase(predicates, cb, root.get("codigoCaja"), filter.codigoCaja());
            addEqual(predicates, cb, root.get("estadoCaja"), filter.estadoCaja());
            addEqual(predicates, cb, root.get("idEmpleadoAperturaSnapshot"), filter.idEmpleadoAperturaSnapshot());
            addEqual(predicates, cb, root.get("idEmpleadoCierreSnapshot"), filter.idEmpleadoCierreSnapshot());
            addEqual(predicates, cb, root.get("estado"), filter.estado());
            addRange(predicates, cb, root.<LocalDate>get("fechaOperacion"), filter.fechaDesde(), filter.fechaHasta());

            return and(cb, predicates);
        };
    }
}