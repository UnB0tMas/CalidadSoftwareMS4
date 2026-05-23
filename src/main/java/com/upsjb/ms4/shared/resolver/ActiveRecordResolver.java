// ruta: src/main/java/com/upsjb/ms4/shared/resolver/ActiveRecordResolver.java
package com.upsjb.ms4.shared.resolver;

import com.upsjb.ms4.domain.entity.base.BaseEntity;
import com.upsjb.ms4.shared.exception.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public class ActiveRecordResolver {

    private final EntityLookupService entityLookupService;

    public ActiveRecordResolver(EntityLookupService entityLookupService) {
        this.entityLookupService = entityLookupService;
    }

    public <T extends BaseEntity> T findActiveRequired(JpaRepository<T, Long> repository,
                                                       Long id,
                                                       String resourceName) {
        T entity = entityLookupService.findRequired(repository, id, resourceName);
        return requireActive(entity, resourceName);
    }

    public <T extends BaseEntity> T findActiveOptional(JpaRepository<T, Long> repository,
                                                       Long id) {
        T entity = entityLookupService.findOptional(repository, id);
        return entity != null && entity.isActivo() ? entity : null;
    }

    public <T extends BaseEntity> T requireActive(T entity, String resourceName) {
        String resource = resourceName == null || resourceName.isBlank() ? "Recurso" : resourceName.trim();

        if (entity == null || entity.getId() == null) {
            throw new NotFoundException(resource + " no fue encontrado.");
        }

        if (!entity.isActivo()) {
            throw NotFoundException.inactive(resource, entity.getId());
        }

        return entity;
    }
}