// ruta: src/main/java/com/upsjb/ms4/specification/PagoSpecification.java
package com.upsjb.ms4.specification;

import com.upsjb.ms4.domain.entity.pago.Pago;
import com.upsjb.ms4.dto.pago.filter.PagoFilterDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class PagoSpecification extends SpecificationSupport {

    private PagoSpecification() {
    }

    public static Specification<Pago> build(PagoFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            addLikeAny(
                    predicates,
                    cb,
                    filter.search(),
                    root.get("codigoPago"),
                    root.get("moneda"),
                    root.get("stripePaymentIntentId"),
                    root.get("stripeChargeId"),
                    root.get("stripeStatus")
            );

            addEqual(predicates, cb, root.get("idVenta"), filter.idVenta());
            addEqualIgnoreCase(predicates, cb, root.get("codigoPago"), filter.codigoPago());
            addEqual(predicates, cb, root.get("metodoPago"), filter.metodoPago());
            addEqual(predicates, cb, root.get("estadoPago"), filter.estadoPago());
            addEqualIgnoreCase(predicates, cb, root.get("stripePaymentIntentId"), filter.stripePaymentIntentId());
            addEqual(predicates, cb, root.get("estado"), filter.estado());
            addRange(predicates, cb, root.<LocalDateTime>get("fechaPago"), filter.fechaDesde(), filter.fechaHasta());

            return and(cb, predicates);
        };
    }
}