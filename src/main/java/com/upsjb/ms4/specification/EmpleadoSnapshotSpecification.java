// ruta: src/main/java/com/upsjb/ms4/specification/EmpleadoSnapshotSpecification.java
package com.upsjb.ms4.specification;

import com.upsjb.ms4.domain.entity.snapshot.EmpleadoSnapshotMs2;
import com.upsjb.ms4.dto.snapshot.filter.EmpleadoSnapshotFilterDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class EmpleadoSnapshotSpecification extends SpecificationSupport {

    private EmpleadoSnapshotSpecification() {
    }

    public static Specification<EmpleadoSnapshotMs2> build(EmpleadoSnapshotFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            addLikeAny(
                    predicates,
                    cb,
                    filter.search(),
                    root.get("codigoEmpleado"),
                    root.get("nombreCompleto"),
                    root.get("nombres"),
                    root.get("apePaterno"),
                    root.get("apeMaterno"),
                    root.get("correo"),
                    root.get("telefonoPrincipal"),
                    root.get("numeroDocumento"),
                    root.get("areaCodigo"),
                    root.get("areaNombre")
            );

            addEqual(predicates, cb, root.get("idEmpleadoMs2"), filter.idEmpleadoMs2());
            addEqual(predicates, cb, root.get("idUsuarioMs1"), filter.idUsuarioMs1());
            addEqualIgnoreCase(predicates, cb, root.get("codigoEmpleado"), filter.codigoEmpleado());
            addEqualIgnoreCase(predicates, cb, root.get("tipoDocumento"), filter.tipoDocumento());
            addEqualIgnoreCase(predicates, cb, root.get("numeroDocumento"), filter.numeroDocumento());
            addEqualIgnoreCase(predicates, cb, root.get("areaCodigo"), filter.areaCodigo());
            addEqual(predicates, cb, root.get("empleadoActivoMs2"), filter.empleadoActivoMs2());
            addEqual(predicates, cb, root.get("estado"), filter.estado());

            return and(cb, predicates);
        };
    }
}