// ruta: src/main/java/com/upsjb/ms4/specification/ConfiguracionTributariaVersionSpecification.java
package com.upsjb.ms4.specification;

import com.upsjb.ms4.domain.entity.config.ConfiguracionTributariaVersion;
import com.upsjb.ms4.dto.config.filter.ConfiguracionTributariaFilterDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class ConfiguracionTributariaVersionSpecification extends SpecificationSupport {

    private ConfiguracionTributariaVersionSpecification() {
    }

    public static Specification<ConfiguracionTributariaVersion> build(ConfiguracionTributariaFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            addLikeAny(
                    predicates,
                    cb,
                    filter.search(),
                    root.get("codigoVersion"),
                    root.get("motivo")
            );

            addEqual(predicates, cb, root.get("nombreImpuesto"), filter.nombreImpuesto());
            addEqual(predicates, cb, root.get("vigente"), filter.vigente());
            addEqual(predicates, cb, root.get("estado"), filter.estado());
            addRange(predicates, cb, root.<LocalDateTime>get("fechaInicioVigencia"), filter.fechaInicioDesde(), filter.fechaInicioHasta());

            return and(cb, predicates);
        };
    }
}