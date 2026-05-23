// ruta: src/main/java/com/upsjb/ms4/validator/CorreoOutboxValidator.java
package com.upsjb.ms4.validator;

import com.upsjb.ms4.domain.entity.mail.CorreoOutbox;
import com.upsjb.ms4.domain.enums.EstadoCorreo;
import com.upsjb.ms4.domain.enums.TipoCorreo;
import com.upsjb.ms4.dto.mail.filter.CorreoOutboxFilterDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CorreoOutboxValidator extends ValidatorSupport {

    public void validarIdCorreoOutbox(Long idCorreoOutbox) {
        requirePositive(idCorreoOutbox, "El correo outbox");
    }

    public void validarProgramacion(TipoCorreo tipoCorreo,
                                    String destinatarioEmail,
                                    String asunto,
                                    LocalDateTime fechaProgramada) {
        require(tipoCorreo, "El tipo de correo es obligatorio.");
        validarDestinatario(destinatarioEmail);
        requireText(asunto, "El asunto del correo es obligatorio.");
        requireMaxLength(asunto, 250, "El asunto del correo");
        require(fechaProgramada, "La fecha programada del correo es obligatoria.");
    }

    public void validarEntidadOrigen(String entidadOrigen, Long idEntidadOrigen) {
        requireText(entidadOrigen, "La entidad origen del correo es obligatoria.");
        requireMaxLength(entidadOrigen, 80, "La entidad origen del correo");

        if (idEntidadOrigen != null && idEntidadOrigen <= 0) {
            fail("El id de entidad origen debe ser positivo.");
        }
    }

    public void validarDestinatario(String email) {
        requireEmail(email, "El destinatario del correo");
    }

    public void validarFiltro(CorreoOutboxFilterDto filter) {
        if (filter == null) {
            return;
        }

        requireMaxLength(filter.search(), 150, "El texto de búsqueda");
        requireMaxLength(filter.entidadOrigen(), 80, "La entidad origen");
        requireMaxLength(filter.destinatarioEmail(), 180, "El destinatario del correo");

        if (filter.idEntidadOrigen() != null && filter.idEntidadOrigen() <= 0) {
            fail("El id de entidad origen debe ser positivo.");
        }

        if (filter.idBoleta() != null && filter.idBoleta() <= 0) {
            fail("El id de boleta debe ser positivo.");
        }

        requireDateRange(filter.fechaProgramadaDesde(), filter.fechaProgramadaHasta(), "El rango de programación de correo");
        requireDateRange(filter.fechaEnvioDesde(), filter.fechaEnvioHasta(), "El rango de envío de correo");
    }

    public void validarReintento(CorreoOutbox correo) {
        require(correo, "El correo outbox es obligatorio.");

        if (correo.getEstadoCorreo() == EstadoCorreo.ENVIADO) {
            conflict("No se puede reintentar un correo ya enviado.");
        }

        if (correo.getEstadoCorreo() == EstadoCorreo.ENVIANDO) {
            conflict("No se puede reintentar un correo que está en proceso de envío.");
        }

        if (correo.getEstadoCorreo() == EstadoCorreo.DESCARTADO) {
            conflict("No se puede reintentar un correo descartado.");
        }
    }

    public void validarNoDescartarCorreoEnviando(CorreoOutbox correo) {
        require(correo, "El correo outbox es obligatorio.");

        if (correo.getEstadoCorreo() == EstadoCorreo.ENVIANDO) {
            conflict("No se puede descartar un correo en proceso de envío.");
        }

        if (correo.getEstadoCorreo() == EstadoCorreo.ENVIADO) {
            conflict("No se puede descartar un correo ya enviado.");
        }

        if (correo.getEstadoCorreo() == EstadoCorreo.DESCARTADO) {
            conflict("El correo ya se encuentra descartado.");
        }
    }

    public void validarWorker(String workerId) {
        requireText(workerId, "El identificador del worker de correo es obligatorio.");
        requireMaxLength(workerId, 120, "El identificador del worker de correo");
    }

    public void validarBatchSize(int batchSize) {
        if (batchSize <= 0) {
            fail("El tamaño de lote de correo debe ser positivo.");
        }
    }

    public void validarProcesable(CorreoOutbox correo) {
        require(correo, "El correo outbox es obligatorio.");

        if (correo.getEstadoCorreo() != EstadoCorreo.PENDIENTE && correo.getEstadoCorreo() != EstadoCorreo.ERROR) {
            conflict("Solo se pueden procesar correos pendientes o con error.");
        }

        if (correo.getMaxAttempts() != null
                && correo.getAttempts() != null
                && correo.getAttempts() >= correo.getMaxAttempts()) {
            conflict("El correo superó el número máximo de intentos.");
        }
    }

    public void validarMarcarEnviado(CorreoOutbox correo) {
        require(correo, "El correo outbox es obligatorio.");

        if (correo.getEstadoCorreo() == EstadoCorreo.DESCARTADO) {
            conflict("No se puede marcar como enviado un correo descartado.");
        }
    }
}