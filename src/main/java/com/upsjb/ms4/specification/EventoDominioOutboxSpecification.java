// ruta: src/main/java/com/upsjb/ms4/specification/EventoDominioOutboxSpecification.java
package com.upsjb.ms4.specification;

import com.upsjb.ms4.domain.entity.kafka.EventoDominioOutbox;
import com.upsjb.ms4.domain.enums.EstadoOutbox;
import com.upsjb.ms4.dto.kafka.filter.EventoDominioOutboxFilterDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class EventoDominioOutboxSpecification extends SpecificationSupport {

    private EventoDominioOutboxSpecification() {
    }

    public static Specification<EventoDominioOutbox> build(EventoDominioOutboxFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            addLikeAny(
                    predicates,
                    cb,
                    filter.search(),
                    root.get("aggregateType"),
                    root.get("aggregateId"),
                    root.get("topic"),
                    root.get("eventKey"),
                    root.get("eventType"),
                    root.get("lastError"),
                    root.get("lockedBy"),
                    root.get("requestId"),
                    root.get("correlationId")
            );

            addEqual(predicates, cb, root.get("eventId"), filter.eventId());
            addEqualIgnoreCase(predicates, cb, root.get("aggregateType"), filter.aggregateType());
            addEqualIgnoreCase(predicates, cb, root.get("aggregateId"), filter.aggregateId());
            addEqualIgnoreCase(predicates, cb, root.get("topic"), filter.topic());
            addEqualIgnoreCase(predicates, cb, root.get("eventKey"), filter.eventKey());
            addEqualIgnoreCase(predicates, cb, root.get("eventType"), filter.eventType());
            addEqual(predicates, cb, root.get("status"), filter.status());
            addEqualIgnoreCase(predicates, cb, root.get("lockedBy"), filter.lockedBy());
            addEqual(predicates, cb, root.get("estado"), filter.estado());

            addRange(predicates, cb, root.<Integer>get("attempts"), filter.attemptsMin(), filter.attemptsMax());
            addRange(predicates, cb, root.<LocalDateTime>get("createdAt"), filter.fechaCreacionDesde(), filter.fechaCreacionHasta());
            addRange(predicates, cb, root.<LocalDateTime>get("publishedAt"), filter.fechaPublicacionDesde(), filter.fechaPublicacionHasta());
            addRange(predicates, cb, root.<LocalDateTime>get("lockedAt"), filter.fechaBloqueoDesde(), filter.fechaBloqueoHasta());

            if (filter.bloqueado() != null) {
                if (Boolean.TRUE.equals(filter.bloqueado())) {
                    predicates.add(cb.isNotNull(root.get("lockedAt")));
                } else {
                    predicates.add(cb.isNull(root.get("lockedAt")));
                }
            }

            if (filter.conError() != null) {
                if (Boolean.TRUE.equals(filter.conError())) {
                    predicates.add(cb.and(
                            cb.isNotNull(root.get("lastError")),
                            cb.notEqual(cb.trim(root.get("lastError")), "")
                    ));
                } else {
                    predicates.add(cb.or(
                            cb.isNull(root.get("lastError")),
                            cb.equal(cb.trim(root.get("lastError")), "")
                    ));
                }
            }

            return and(cb, predicates);
        };
    }

    public static Specification<EventoDominioOutbox> porEventId(UUID eventId) {
        return (root, query, cb) -> eventId == null
                ? cb.disjunction()
                : cb.equal(root.get("eventId"), eventId);
    }

    public static Specification<EventoDominioOutbox> porAggregate(String aggregateType, String aggregateId) {
        return (root, query, cb) -> {
            if (!hasText(aggregateType) || !hasText(aggregateId)) {
                return cb.disjunction();
            }

            return cb.and(
                    cb.equal(cb.lower(root.get("aggregateType")), aggregateType.trim().toLowerCase()),
                    cb.equal(cb.lower(root.get("aggregateId")), aggregateId.trim().toLowerCase())
            );
        };
    }

    public static Specification<EventoDominioOutbox> pendientesPublicacion() {
        return (root, query, cb) -> cb.and(
                cb.isTrue(root.get("estado")),
                root.get("status").in(EstadoOutbox.PENDIENTE, EstadoOutbox.ERROR)
        );
    }

    public static Specification<EventoDominioOutbox> publicados() {
        return (root, query, cb) -> cb.and(
                cb.isTrue(root.get("estado")),
                cb.equal(root.get("status"), EstadoOutbox.PUBLICADO)
        );
    }

    public static Specification<EventoDominioOutbox> conIntentosAgotados() {
        return (root, query, cb) -> cb.and(
                cb.isTrue(root.get("estado")),
                cb.greaterThanOrEqualTo(root.get("attempts"), root.get("maxAttempts"))
        );
    }
}