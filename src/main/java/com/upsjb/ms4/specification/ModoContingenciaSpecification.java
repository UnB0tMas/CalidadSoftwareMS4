// ruta: src/main/java/com/upsjb/ms4/specification/ModoContingenciaSpecification.java
package com.upsjb.ms4.specification;

import com.upsjb.ms4.domain.entity.contingencia.ModoContingencia;
import com.upsjb.ms4.dto.contingencia.filter.ModoContingenciaFilterDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class ModoContingenciaSpecification extends SpecificationSupport {

    private ModoContingenciaSpecification() {
    }

    public static Specification<ModoContingencia> build(ModoContingenciaFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            addLikeAny(
                    predicates,
                    cb,
                    filter.search(),
                    root.get("servicioAfectado"),
                    root.get("activadoPorRol"),
                    root.get("motivo"),
                    root.get("observacion")
            );

            addEqualIgnoreCase(predicates, cb, root.get("servicioAfectado"), filter.servicioAfectado());
            addEqual(predicates, cb, root.get("estadoContingencia"), filter.estadoContingencia());
            addEqual(predicates, cb, root.get("ventasPermitidas"), filter.ventasPermitidas());
            addEqual(predicates, cb, root.get("guardarEventosPendientes"), filter.guardarEventosPendientes());
            addEqual(predicates, cb, root.get("estado"), filter.estado());
            addRange(predicates, cb, root.<LocalDateTime>get("fechaInicio"), filter.fechaInicioDesde(), filter.fechaInicioHasta());
            addRange(predicates, cb, root.<LocalDateTime>get("fechaFin"), filter.fechaFinDesde(), filter.fechaFinHasta());

            return and(cb, predicates);
        };
    }
}