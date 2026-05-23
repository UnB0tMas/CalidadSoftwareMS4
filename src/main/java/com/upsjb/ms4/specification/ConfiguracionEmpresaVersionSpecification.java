// ruta: src/main/java/com/upsjb/ms4/specification/ConfiguracionEmpresaVersionSpecification.java
package com.upsjb.ms4.specification;

import com.upsjb.ms4.domain.entity.config.ConfiguracionEmpresaVersion;
import com.upsjb.ms4.dto.config.filter.ConfiguracionEmpresaFilterDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class ConfiguracionEmpresaVersionSpecification extends SpecificationSupport {

    private ConfiguracionEmpresaVersionSpecification() {
    }

    public static Specification<ConfiguracionEmpresaVersion> build(ConfiguracionEmpresaFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            addLikeAny(
                    predicates,
                    cb,
                    filter.search(),
                    root.get("codigoVersion"),
                    root.get("ruc"),
                    root.get("razonSocial"),
                    root.get("nombreComercial"),
                    root.get("direccionFiscal"),
                    root.get("telefono"),
                    root.get("correo"),
                    root.get("web"),
                    root.get("motivo")
            );

            addEqualIgnoreCase(predicates, cb, root.get("ruc"), filter.ruc());
            addEqual(predicates, cb, root.get("vigente"), filter.vigente());
            addEqual(predicates, cb, root.get("estado"), filter.estado());
            addRange(predicates, cb, root.<LocalDateTime>get("fechaInicioVigencia"), filter.fechaInicioDesde(), filter.fechaInicioHasta());

            return and(cb, predicates);
        };
    }
}