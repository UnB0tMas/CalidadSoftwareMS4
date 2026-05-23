// ruta: src/main/java/com/upsjb/ms4/dto/mail/filter/CorreoOutboxFilterDto.java
package com.upsjb.ms4.dto.mail.filter;

import com.upsjb.ms4.domain.enums.EstadoCorreo;
import com.upsjb.ms4.domain.enums.TipoCorreo;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record CorreoOutboxFilterDto(
        @Size(max = 150, message = "El texto de búsqueda no debe superar 150 caracteres.")
        String search,
        TipoCorreo tipoCorreo,
        EstadoCorreo estadoCorreo,
        String entidadOrigen,
        Long idEntidadOrigen,
        Long idBoleta,
        String destinatarioEmail,
        Boolean estado,
        LocalDateTime fechaProgramadaDesde,
        LocalDateTime fechaProgramadaHasta,
        LocalDateTime fechaEnvioDesde,
        LocalDateTime fechaEnvioHasta
) {
}