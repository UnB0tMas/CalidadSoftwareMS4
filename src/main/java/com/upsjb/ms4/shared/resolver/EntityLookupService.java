// ruta: src/main/java/com/upsjb/ms4/shared/resolver/EntityLookupService.java
package com.upsjb.ms4.shared.resolver;

import com.upsjb.ms4.shared.exception.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Supplier;

@Component
public class EntityLookupService {

    public <T> T findRequired(JpaRepository<T, Long> repository, Long id, String resourceName) {
        if (repository == null) {
            throw new IllegalArgumentException("El repositorio es obligatorio para resolver " + resourceName + ".");
        }

        if (id == null) {
            throw new NotFoundException(resource(resourceName) + " requiere un identificador.");
        }

        return repository.findById(id)
                .orElseThrow(() -> NotFoundException.byId(resource(resourceName), id));
    }

    public <T> T findRequired(Supplier<Optional<T>> supplier, String resourceName) {
        if (supplier == null) {
            throw new IllegalArgumentException("El supplier de búsqueda es obligatorio para resolver " + resourceName + ".");
        }

        return supplier.get()
                .orElseThrow(() -> new NotFoundException(resource(resourceName) + " no fue encontrado."));
    }

    public <T> T findOptional(JpaRepository<T, Long> repository, Long id) {
        if (repository == null || id == null) {
            return null;
        }

        return repository.findById(id).orElse(null);
    }

    private String resource(String resourceName) {
        return resourceName == null || resourceName.isBlank() ? "Recurso" : resourceName.trim();
    }
}