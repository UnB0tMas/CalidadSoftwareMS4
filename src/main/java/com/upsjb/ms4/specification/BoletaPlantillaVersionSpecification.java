// ruta: src/main/java/com/upsjb/ms4/specification/BoletaPlantillaVersionSpecification.java
package com.upsjb.ms4.specification;

import com.upsjb.ms4.domain.entity.config.BoletaPlantillaVersion;
import com.upsjb.ms4.dto.config.filter.BoletaPlantillaFilterDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class BoletaPlantillaVersionSpecification extends SpecificationSupport {

    private BoletaPlantillaVersionSpecification() {
    }

    public static Specification<BoletaPlantillaVersion> build(BoletaPlantillaFilterDto filter) {
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
                    root.get("nombre"),
                    root.get("rutaTemplateHtml"),
                    root.get("rutaTemplateMail"),
                    root.get("descripcion"),
                    root.get("motivo")
            );

            addEqualIgnoreCase(predicates, cb, root.get("codigoVersion"), filter.codigoVersion());
            addEqual(predicates, cb, root.get("vigente"), filter.vigente());
            addEqual(predicates, cb, root.get("estado"), filter.estado());
            addRange(
                    predicates,
                    cb,
                    root.<LocalDateTime>get("fechaInicioVigencia"),
                    filter.fechaInicioDesde(),
                    filter.fechaInicioHasta()
            );

            return and(cb, predicates);
        };
    }
}