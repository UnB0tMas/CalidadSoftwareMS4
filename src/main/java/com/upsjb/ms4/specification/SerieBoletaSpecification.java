// ruta: src/main/java/com/upsjb/ms4/specification/SerieBoletaSpecification.java
package com.upsjb.ms4.specification;

import com.upsjb.ms4.domain.entity.config.SerieBoleta;
import com.upsjb.ms4.dto.config.filter.SerieBoletaFilterDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class SerieBoletaSpecification extends SpecificationSupport {

    private SerieBoletaSpecification() {
    }

    public static Specification<SerieBoleta> build(SerieBoletaFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            addLikeAny(predicates, cb, filter.search(), root.get("serie"));
            addEqualIgnoreCase(predicates, cb, root.get("serie"), filter.serie());
            addEqual(predicates, cb, root.get("estado"), filter.estado());

            return and(cb, predicates);
        };
    }
}