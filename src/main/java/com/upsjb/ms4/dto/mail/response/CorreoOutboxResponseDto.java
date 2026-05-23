// ruta: src/main/java/com/upsjb/ms4/dto/mail/response/CorreoOutboxResponseDto.java
package com.upsjb.ms4.dto.mail.response;

import com.upsjb.ms4.domain.enums.EstadoCorreo;
import com.upsjb.ms4.domain.enums.TipoCorreo;
import java.time.LocalDateTime;
import java.util.UUID;

public record CorreoOutboxResponseDto(
        Long id,
        UUID eventId,
        TipoCorreo tipoCorreo,
        String entidadOrigen,
        Long idEntidadOrigen,
        Long idBoleta,
        String codigoBoleta,
        String destinatarioEmail,
        String destinatarioNombre,
        String asunto,
        EstadoCorreo estadoCorreo,
        Integer attempts,
        Integer maxAttempts,
        String lastError,
        LocalDateTime fechaProgramada,
        LocalDateTime fechaEnvio,
        String requestId,
        String correlationId,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}