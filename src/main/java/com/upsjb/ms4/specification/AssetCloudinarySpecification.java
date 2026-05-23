// ruta: src/main/java/com/upsjb/ms4/specification/AssetCloudinarySpecification.java
package com.upsjb.ms4.specification;

import com.upsjb.ms4.domain.entity.config.AssetCloudinary;
import com.upsjb.ms4.dto.config.filter.AssetCloudinaryFilterDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class AssetCloudinarySpecification extends SpecificationSupport {

    private AssetCloudinarySpecification() {
    }

    public static Specification<AssetCloudinary> build(AssetCloudinaryFilterDto filter) {
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
                    root.get("tipoAsset"),
                    root.get("nombreArchivo"),
                    root.get("extension"),
                    root.get("contentType"),
                    root.get("folder"),
                    root.get("publicId"),
                    root.get("secureUrl"),
                    root.get("hashSha256")
            );

            addEqualIgnoreCase(predicates, cb, root.get("entidadOrigen"), filter.entidadOrigen());
            addEqual(predicates, cb, root.get("idEntidadOrigen"), filter.idEntidadOrigen());
            addEqualIgnoreCase(predicates, cb, root.get("tipoAsset"), filter.tipoAsset());
            addEqual(predicates, cb, root.get("resourceType"), filter.resourceType());
            addEqualIgnoreCase(predicates, cb, root.get("folder"), filter.folder());
            addEqual(predicates, cb, root.get("estado"), filter.estado());

            return and(cb, predicates);
        };
    }
}