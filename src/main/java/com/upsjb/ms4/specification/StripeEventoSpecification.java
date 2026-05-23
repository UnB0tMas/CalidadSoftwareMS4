// ruta: src/main/java/com/upsjb/ms4/specification/StripeEventoSpecification.java
package com.upsjb.ms4.specification;

import com.upsjb.ms4.domain.entity.pago.StripeEvento;
import com.upsjb.ms4.dto.pago.filter.StripeEventoFilterDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class StripeEventoSpecification extends SpecificationSupport {

    private StripeEventoSpecification() {
    }

    public static Specification<StripeEvento> build(StripeEventoFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            addLikeAny(
                    predicates,
                    cb,
                    filter.search(),
                    root.get("stripeEventId"),
                    root.get("stripeEventType"),
                    root.get("stripePaymentIntentId"),
                    root.get("errorDetalle")
            );

            addEqualIgnoreCase(predicates, cb, root.get("stripeEventId"), filter.stripeEventId());
            addEqualIgnoreCase(predicates, cb, root.get("stripeEventType"), filter.stripeEventType());
            addEqualIgnoreCase(predicates, cb, root.get("stripePaymentIntentId"), filter.stripePaymentIntentId());
            addEqual(predicates, cb, root.get("idPago"), filter.idPago());
            addEqual(predicates, cb, root.get("estadoProcesamiento"), filter.estadoProcesamiento());
            addEqual(predicates, cb, root.get("estado"), filter.estado());
            addRange(predicates, cb, root.<LocalDateTime>get("fechaRecepcion"), filter.fechaDesde(), filter.fechaHasta());

            return and(cb, predicates);
        };
    }
}