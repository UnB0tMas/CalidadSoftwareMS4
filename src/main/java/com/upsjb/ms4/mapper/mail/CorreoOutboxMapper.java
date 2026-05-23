// ruta: src/main/java/com/upsjb/ms4/mapper/mail/CorreoOutboxMapper.java
package com.upsjb.ms4.mapper.mail;

import com.upsjb.ms4.domain.entity.mail.CorreoOutbox;
import com.upsjb.ms4.dto.mail.response.CorreoOutboxResponseDto;
import org.springframework.stereotype.Component;

@Component
public class CorreoOutboxMapper {

    public CorreoOutboxResponseDto toResponse(CorreoOutbox entity) {
        if (entity == null) return null;

        return new CorreoOutboxResponseDto(
                entity.getId(),
                entity.getEventId(),
                entity.getTipoCorreo(),
                entity.getEntidadOrigen(),
                entity.getIdEntidadOrigen(),
                entity.getIdBoleta(),
                entity.getBoleta() != null ? entity.getBoleta().getCodigoBoleta() : null,
                entity.getDestinatarioEmail(),
                entity.getDestinatarioNombre(),
                entity.getAsunto(),
                entity.getEstadoCorreo(),
                entity.getAttempts(),
                entity.getMaxAttempts(),
                entity.getLastError(),
                entity.getFechaProgramada(),
                entity.getFechaEnvio(),
                entity.getRequestId(),
                entity.getCorrelationId(),
                entity.getEstado(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public EmailContext toEmailContext(CorreoOutbox entity) {
        if (entity == null) return null;

        return new EmailContext(
                entity.getId(),
                entity.getTipoCorreo() != null ? entity.getTipoCorreo().getCode() : null,
                entity.getDestinatarioEmail(),
                entity.getDestinatarioNombre(),
                entity.getAsunto(),
                entity.getIdBoleta(),
                entity.getBoleta() != null ? entity.getBoleta().getCodigoBoleta() : null,
                entity.getRequestId(),
                entity.getCorrelationId()
        );
    }

    public CorreoOutboxResponseDto toRetryResponse(CorreoOutbox entity) {
        return toResponse(entity);
    }

    public record EmailContext(
            Long idCorreoOutbox,
            String tipoCorreo,
            String destinatarioEmail,
            String destinatarioNombre,
            String asunto,
            Long idBoleta,
            String codigoBoleta,
            String requestId,
            String correlationId
    ) {
    }
}