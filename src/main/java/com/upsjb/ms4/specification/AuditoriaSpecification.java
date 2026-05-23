// ruta: src/main/java/com/upsjb/ms4/specification/AuditoriaSpecification.java
package com.upsjb.ms4.specification;

import com.upsjb.ms4.domain.entity.auditoria.AuditoriaFuncional;
import com.upsjb.ms4.dto.auditoria.filter.AuditoriaFilterDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class AuditoriaSpecification extends SpecificationSupport {

    private AuditoriaSpecification() {
    }

    public static Specification<AuditoriaFuncional> build(AuditoriaFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            addLikeAny(
                    predicates,
                    cb,
                    filter.search(),
                    root.get("entidad"),
                    root.get("entidadId"),
                    root.get("accion"),
                    root.get("actorRol"),
                    root.get("actorUsername"),
                    root.get("requestId"),
                    root.get("correlationId")
            );

            addEqualIgnoreCase(predicates, cb, root.get("entidad"), filter.entidad());
            addEqualIgnoreCase(predicates, cb, root.get("entidadId"), filter.entidadId());
            addEqualIgnoreCase(predicates, cb, root.get("accion"), filter.accion());
            addEqual(predicates, cb, root.get("resultado"), filter.resultado());
            addEqual(predicates, cb, root.get("actorIdUsuarioMs1"), filter.actorIdUsuarioMs1());
            addEqualIgnoreCase(predicates, cb, root.get("actorRol"), filter.actorRol());
            addEqualIgnoreCase(predicates, cb, root.get("actorUsername"), filter.actorUsername());
            addEqualIgnoreCase(predicates, cb, root.get("requestId"), filter.requestId());
            addEqualIgnoreCase(predicates, cb, root.get("correlationId"), filter.correlationId());
            addEqual(predicates, cb, root.get("estado"), filter.estado());
            addRange(predicates, cb, root.<LocalDateTime>get("createdAt"), filter.fechaDesde(), filter.fechaHasta());

            return and(cb, predicates);
        };
    }
}