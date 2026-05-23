// ruta: src/main/java/com/upsjb/ms4/shared/audit/AuditEventFactory.java
package com.upsjb.ms4.shared.audit;

import com.upsjb.ms4.domain.entity.auditoria.AuditoriaFuncional;
import com.upsjb.ms4.domain.enums.ResultadoAuditoria;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class AuditEventFactory {

    public AuditoriaFuncional create(String entidad,
                                     String entidadId,
                                     String accion,
                                     ResultadoAuditoria resultado,
                                     String detalleJson) {
        return create(entidad, entidadId, accion, resultado, null, detalleJson);
    }

    public AuditoriaFuncional create(String entidad,
                                     String entidadId,
                                     String accion,
                                     ResultadoAuditoria resultado,
                                     AuthenticatedUserContext actor,
                                     String detalleJson) {
        AuditContextHolder.AuditContext context = AuditContextHolder.getOrEmpty();

        return AuditoriaFuncional.builder()
                .entidad(clean(entidad, "MS4"))
                .entidadId(clean(entidadId, null))
                .accion(clean(accion, "ACCION_NO_ESPECIFICADA"))
                .resultado(resultado == null ? ResultadoAuditoria.EXITOSO : resultado)
                .actorIdUsuarioMs1(resolveActorId(actor, context))
                .actorRol(clean(resolveActorRol(actor, context), null))
                .actorUsername(clean(resolveActorUsername(actor, context), null))
                .ip(clean(context.ip(), null))
                .userAgent(clean(context.userAgent(), null))
                .requestId(clean(context.requestId(), null))
                .correlationId(clean(context.correlationId(), null))
                .detalleJson(detalleJson)
                .estado(true)
                .build();
    }

    public AuditoriaFuncional exitoso(String entidad, String entidadId, String accion, String detalleJson) {
        return create(entidad, entidadId, accion, ResultadoAuditoria.EXITOSO, detalleJson);
    }

    public AuditoriaFuncional exitoso(String entidad,
                                      String entidadId,
                                      String accion,
                                      AuthenticatedUserContext actor,
                                      String detalleJson) {
        return create(entidad, entidadId, accion, ResultadoAuditoria.EXITOSO, actor, detalleJson);
    }

    public AuditoriaFuncional errorUsuario(String entidad, String entidadId, String accion, String detalleJson) {
        return create(entidad, entidadId, accion, ResultadoAuditoria.ERROR_USUARIO, detalleJson);
    }

    public AuditoriaFuncional errorUsuario(String entidad,
                                           String entidadId,
                                           String accion,
                                           AuthenticatedUserContext actor,
                                           String detalleJson) {
        return create(entidad, entidadId, accion, ResultadoAuditoria.ERROR_USUARIO, actor, detalleJson);
    }

    public AuditoriaFuncional errorTecnico(String entidad, String entidadId, String accion, String detalleJson) {
        return create(entidad, entidadId, accion, ResultadoAuditoria.ERROR_TECNICO, detalleJson);
    }

    public AuditoriaFuncional errorTecnico(String entidad,
                                           String entidadId,
                                           String accion,
                                           AuthenticatedUserContext actor,
                                           String detalleJson) {
        return create(entidad, entidadId, accion, ResultadoAuditoria.ERROR_TECNICO, actor, detalleJson);
    }

    public AuditoriaFuncional ok(String entidad, String entidadId, String accion, String detalleJson) {
        return exitoso(entidad, entidadId, accion, detalleJson);
    }

    public AuditoriaFuncional error(String entidad, String entidadId, String accion, String detalleJson) {
        return errorTecnico(entidad, entidadId, accion, detalleJson);
    }

    public AuditoriaFuncional denegado(String entidad, String entidadId, String accion, String detalleJson) {
        return errorUsuario(entidad, entidadId, accion, detalleJson);
    }

    public AuditoriaFuncional advertencia(String entidad, String entidadId, String accion, String detalleJson) {
        return errorUsuario(entidad, entidadId, accion, detalleJson);
    }

    private Long resolveActorId(AuthenticatedUserContext actor, AuditContextHolder.AuditContext context) {
        if (actor != null && actor.idUsuarioMs1() != null) {
            return actor.idUsuarioMs1();
        }

        return context.actorIdUsuarioMs1();
    }

    private String resolveActorRol(AuthenticatedUserContext actor, AuditContextHolder.AuditContext context) {
        if (actor != null && actor.rol() != null && !actor.rol().isBlank()) {
            return actor.rol();
        }

        return context.actorRol();
    }

    private String resolveActorUsername(AuthenticatedUserContext actor, AuditContextHolder.AuditContext context) {
        if (actor != null && actor.username() != null && !actor.username().isBlank()) {
            return actor.username();
        }

        return context.actorUsername();
    }

    private String clean(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value.trim();
    }
}