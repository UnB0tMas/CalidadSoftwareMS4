// ruta: src/main/java/com/upsjb/ms4/specification/ClienteSnapshotSpecification.java
package com.upsjb.ms4.specification;

import com.upsjb.ms4.domain.entity.snapshot.ClienteSnapshotMs2;
import com.upsjb.ms4.dto.snapshot.filter.ClienteSnapshotFilterDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class ClienteSnapshotSpecification extends SpecificationSupport {

    private ClienteSnapshotSpecification() {
    }

    public static Specification<ClienteSnapshotMs2> build(ClienteSnapshotFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            addLikeAny(
                    predicates,
                    cb,
                    filter.search(),
                    root.get("nombreCompleto"),
                    root.get("nombres"),
                    root.get("apePaterno"),
                    root.get("apeMaterno"),
                    root.get("razonSocial"),
                    root.get("nombreComercial"),
                    root.get("correoPrincipal"),
                    root.get("telefonoPrincipal"),
                    root.get("numeroDocumentoPersona"),
                    root.get("ruc")
            );

            addEqual(predicates, cb, root.get("idClienteMs2"), filter.idClienteMs2());
            addEqual(predicates, cb, root.get("idUsuarioMs1"), filter.idUsuarioMs1());
            addEqualIgnoreCase(predicates, cb, root.get("tipoCliente"), filter.tipoCliente());
            addEqualIgnoreCase(predicates, cb, root.get("tipoDocumentoPersona"), filter.tipoDocumentoPersona());
            addEqualIgnoreCase(predicates, cb, root.get("numeroDocumentoPersona"), filter.numeroDocumentoPersona());
            addEqualIgnoreCase(predicates, cb, root.get("ruc"), filter.ruc());
            addEqual(predicates, cb, root.get("clienteActivoMs2"), filter.clienteActivoMs2());
            addEqual(predicates, cb, root.get("estado"), filter.estado());

            return and(cb, predicates);
        };
    }
}