// ruta: src/main/java/com/upsjb/ms4/specification/ProductoSnapshotSpecification.java
package com.upsjb.ms4.specification;

import com.upsjb.ms4.domain.entity.snapshot.ProductoSnapshotMs3;
import com.upsjb.ms4.dto.snapshot.filter.ProductoVentaFilterDto;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class ProductoSnapshotSpecification
        extends SpecificationSupport {

    private ProductoSnapshotSpecification() {
    }

    public static Specification<ProductoSnapshotMs3> build(
            ProductoVentaFilterDto filter
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates =
                    new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            addLikeAny(
                    predicates,
                    cb,
                    filter.search(),
                    root.get("codigoProducto"),
                    root.get("nombre"),
                    root.get("slug"),
                    root.get("codigoCategoria"),
                    root.get("nombreCategoria"),
                    root.get("codigoCategoriaPadre"),
                    root.get("nombreCategoriaPadre"),
                    root.get("categoriaRutaCodigo"),
                    root.get("categoriaRutaNombre"),
                    root.get("nombreMarca"),
                    root.get("descripcionCorta"),
                    root.get("generoObjetivo"),
                    root.get("temporada"),
                    root.get("deporte")
            );

            addEqual(
                    predicates,
                    cb,
                    root.get("idProductoMs3"),
                    filter.idProductoMs3()
            );

            addEqualIgnoreCase(
                    predicates,
                    cb,
                    root.get("codigoProducto"),
                    filter.codigoProducto()
            );

            addEqualIgnoreCase(
                    predicates,
                    cb,
                    root.get("slug"),
                    filter.slug()
            );

            addEqual(
                    predicates,
                    cb,
                    root.get("idCategoriaMs3"),
                    filter.idCategoriaMs3()
            );

            addEqual(
                    predicates,
                    cb,
                    root.get("idCategoriaPadreMs3"),
                    filter.idCategoriaPadreMs3()
            );

            addEqual(
                    predicates,
                    cb,
                    root.get("nivelCategoria"),
                    filter.nivelCategoria()
            );

            addEqual(
                    predicates,
                    cb,
                    root.get("categoriaPermiteProductos"),
                    filter.categoriaPermiteProductos()
            );

            addEqual(
                    predicates,
                    cb,
                    root.get("categoriaEstado"),
                    filter.categoriaEstado()
            );

            addEqual(
                    predicates,
                    cb,
                    root.get("idMarcaMs3"),
                    filter.idMarcaMs3()
            );

            addEqualIgnoreCase(
                    predicates,
                    cb,
                    root.get("estadoRegistro"),
                    filter.estadoRegistro()
            );

            addEqualIgnoreCase(
                    predicates,
                    cb,
                    root.get("estadoPublicacion"),
                    filter.estadoPublicacion()
            );

            addEqualIgnoreCase(
                    predicates,
                    cb,
                    root.get("estadoVenta"),
                    filter.estadoVenta()
            );

            addEqual(
                    predicates,
                    cb,
                    root.get("visiblePublico"),
                    filter.visiblePublico()
            );

            addEqual(
                    predicates,
                    cb,
                    root.get("vendible"),
                    filter.vendible()
            );

            addEqual(
                    predicates,
                    cb,
                    root.get("estado"),
                    filter.estado()
            );

            return and(
                    cb,
                    predicates
            );
        };
    }

    public static Specification<ProductoSnapshotMs3>
    vendiblesPublicos() {
        return (root, query, cb) ->
                cb.and(
                        cb.isTrue(
                                root.get("estado")
                        ),
                        cb.isTrue(
                                root.get("categoriaEstado")
                        ),
                        cb.isTrue(
                                root.get(
                                        "categoriaPermiteProductos"
                                )
                        ),
                        cb.isTrue(
                                root.get("visiblePublico")
                        ),
                        cb.isTrue(
                                root.get("vendible")
                        )
                );
    }
}