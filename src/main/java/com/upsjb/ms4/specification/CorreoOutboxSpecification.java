// ruta: src/main/java/com/upsjb/ms4/specification/CorreoOutboxSpecification.java
package com.upsjb.ms4.specification;

import com.upsjb.ms4.domain.entity.mail.CorreoOutbox;
import com.upsjb.ms4.dto.mail.filter.CorreoOutboxFilterDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class CorreoOutboxSpecification extends SpecificationSupport {

    private CorreoOutboxSpecification() {
    }

    public static Specification<CorreoOutbox> build(CorreoOutboxFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            addLikeAny(
                    predicates,
                    cb,
                    filter.search(),
                    root.get("entidadOrigen"),
                    root.get("destinatarioEmail"),
                    root.get("destinatarioNombre"),
                    root.get("asunto"),
                    root.get("lastError"),
                    root.get("requestId"),
                    root.get("correlationId")
            );

            addEqual(predicates, cb, root.get("tipoCorreo"), filter.tipoCorreo());
            addEqual(predicates, cb, root.get("estadoCorreo"), filter.estadoCorreo());
            addEqualIgnoreCase(predicates, cb, root.get("entidadOrigen"), filter.entidadOrigen());
            addEqual(predicates, cb, root.get("idEntidadOrigen"), filter.idEntidadOrigen());
            addEqual(predicates, cb, root.get("idBoleta"), filter.idBoleta());
            addEqualIgnoreCase(predicates, cb, root.get("destinatarioEmail"), filter.destinatarioEmail());
            addEqual(predicates, cb, root.get("estado"), filter.estado());
            addRange(predicates, cb, root.<LocalDateTime>get("fechaProgramada"), filter.fechaProgramadaDesde(), filter.fechaProgramadaHasta());
            addRange(predicates, cb, root.<LocalDateTime>get("fechaEnvio"), filter.fechaEnvioDesde(), filter.fechaEnvioHasta());

            return and(cb, predicates);
        };
    }
}