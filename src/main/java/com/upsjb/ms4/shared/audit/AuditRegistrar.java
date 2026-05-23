// ruta: src/main/java/com/upsjb/ms4/shared/audit/AuditRegistrar.java
package com.upsjb.ms4.shared.audit;

import com.upsjb.ms4.domain.entity.auditoria.AuditoriaFuncional;
import com.upsjb.ms4.repository.AuditoriaFuncionalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuditRegistrar {

    private static final Logger log = LoggerFactory.getLogger(AuditRegistrar.class);

    private final AuditoriaFuncionalRepository auditoriaRepository;
    private final AuditEventFactory auditEventFactory;

    public AuditRegistrar(AuditoriaFuncionalRepository auditoriaRepository,
                          AuditEventFactory auditEventFactory) {
        this.auditoriaRepository = auditoriaRepository;
        this.auditEventFactory = auditEventFactory;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(AuditoriaFuncional auditoria) {
        saveSafely(auditoria);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarExito(String entidad, String entidadId, String accion, String detalleJson) {
        saveSafely(auditEventFactory.exitoso(entidad, entidadId, accion, detalleJson));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarErrorUsuario(String entidad, String entidadId, String accion, String detalleJson) {
        saveSafely(auditEventFactory.errorUsuario(entidad, entidadId, accion, detalleJson));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarErrorTecnico(String entidad, String entidadId, String accion, String detalleJson) {
        saveSafely(auditEventFactory.errorTecnico(entidad, entidadId, accion, detalleJson));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarOk(String entidad, String entidadId, String accion, String detalleJson) {
        registrarExito(entidad, entidadId, accion, detalleJson);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarError(String entidad, String entidadId, String accion, String detalleJson) {
        registrarErrorTecnico(entidad, entidadId, accion, detalleJson);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarDenegado(String entidad, String entidadId, String accion, String detalleJson) {
        registrarErrorUsuario(entidad, entidadId, accion, detalleJson);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarAdvertencia(String entidad, String entidadId, String accion, String detalleJson) {
        registrarErrorUsuario(entidad, entidadId, accion, detalleJson);
    }

    private void saveSafely(AuditoriaFuncional auditoria) {
        if (auditoria == null) {
            return;
        }

        try {
            auditoriaRepository.save(auditoria);
        } catch (Exception ex) {
            log.error(
                    "No se pudo registrar auditoría funcional. entidad={}, entidadId={}, accion={}, resultado={}, requestId={}, correlationId={}",
                    auditoria.getEntidad(),
                    auditoria.getEntidadId(),
                    auditoria.getAccion(),
                    auditoria.getResultado(),
                    auditoria.getRequestId(),
                    auditoria.getCorrelationId(),
                    ex
            );
        }
    }
}