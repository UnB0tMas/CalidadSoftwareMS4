// ruta: src/main/java/com/upsjb/ms4/validator/AuditoriaValidator.java
package com.upsjb.ms4.validator;

import com.upsjb.ms4.domain.enums.ResultadoAuditoria;
import com.upsjb.ms4.dto.auditoria.filter.AuditoriaFilterDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.util.JsonUtil;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaValidator extends ValidatorSupport {

    public void validarIdAuditoria(Long idAuditoria) {
        requirePositive(idAuditoria, "La auditoría funcional");
    }

    public void validarRegistro(String entidad,
                                Long entidadId,
                                String accion,
                                ResultadoAuditoria resultado,
                                AuthenticatedUserContext actor,
                                String detalleJson) {
        requireText(entidad, "La entidad auditada es obligatoria.");
        requireMaxLength(entidad, 80, "La entidad auditada");

        if (entidadId != null && entidadId <= 0) {
            fail("El identificador de entidad auditada debe ser positivo.");
        }

        requireText(accion, "La acción auditada es obligatoria.");
        requireMaxLength(accion, 100, "La acción auditada");

        require(resultado, "El resultado de auditoría es obligatorio.");

        if (actor != null) {
            if (actor.idUsuarioMs1() != null && actor.idUsuarioMs1() <= 0) {
                fail("El actor de auditoría debe tener un idUsuarioMs1 válido.");
            }

            requireMaxLength(actor.rol(), 40, "El rol del actor");
            requireMaxLength(actor.username(), 180, "El username del actor");
        }

        if (!isBlank(detalleJson) && !JsonUtil.isValidJson(detalleJson)) {
            fail("El detalle de auditoría debe contener JSON válido.");
        }
    }

    public void validarFiltro(AuditoriaFilterDto filter) {
        if (filter == null) {
            return;
        }

        requireMaxLength(filter.search(), 150, "El texto de búsqueda");
        requireMaxLength(filter.entidad(), 80, "La entidad");
        requireMaxLength(filter.entidadId(), 120, "El identificador de entidad");
        requireMaxLength(filter.accion(), 100, "La acción");
        requireMaxLength(filter.actorRol(), 40, "El rol del actor");
        requireMaxLength(filter.actorUsername(), 180, "El username del actor");
        requireMaxLength(filter.requestId(), 100, "El requestId");
        requireMaxLength(filter.correlationId(), 100, "El correlationId");

        if (filter.actorIdUsuarioMs1() != null && filter.actorIdUsuarioMs1() <= 0) {
            fail("El actorIdUsuarioMs1 debe ser positivo.");
        }

        requireDateRange(filter.fechaDesde(), filter.fechaHasta(), "El rango de fechas de auditoría");
    }
}