// ruta: src/main/java/com/upsjb/ms4/specification/InventarioEventoPendienteSpecification.java
package com.upsjb.ms4.specification;

import com.upsjb.ms4.domain.entity.kafka.InventarioEventoPendienteMs4;
import com.upsjb.ms4.dto.contingencia.filter.InventarioEventoPendienteFilterDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class InventarioEventoPendienteSpecification extends SpecificationSupport {

    private InventarioEventoPendienteSpecification() {
    }

    public static Specification<InventarioEventoPendienteMs4> build(InventarioEventoPendienteFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            addLikeAny(
                    predicates,
                    cb,
                    filter.search(),
                    root.get("codigoVenta"),
                    root.get("topicDestino"),
                    root.get("idempotencyKey"),
                    root.get("ultimoError"),
                    root.get("requestId"),
                    root.get("correlationId")
            );

            addEqual(predicates, cb, root.get("idVenta"), filter.idVenta());
            addEqualIgnoreCase(predicates, cb, root.get("codigoVenta"), filter.codigoVenta());
            addEqual(predicates, cb, root.get("idVentaDetalle"), filter.idVentaDetalle());
            addEqual(predicates, cb, root.get("tipoEvento"), filter.tipoEvento());
            addEqualIgnoreCase(predicates, cb, root.get("topicDestino"), filter.topicDestino());
            addEqual(predicates, cb, root.get("estadoSincronizacion"), filter.estadoSincronizacion());
            addEqualIgnoreCase(predicates, cb, root.get("idempotencyKey"), filter.idempotencyKey());
            addEqual(predicates, cb, root.get("estado"), filter.estado());
            addRange(predicates, cb, root.<LocalDateTime>get("fechaCreacion"), filter.fechaCreacionDesde(), filter.fechaCreacionHasta());
            addRange(predicates, cb, root.<LocalDateTime>get("fechaSincronizacion"), filter.fechaSincronizacionDesde(), filter.fechaSincronizacionHasta());

            return and(cb, predicates);
        };
    }
}