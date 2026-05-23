// ruta: src/main/java/com/upsjb/ms4/service/contract/boleta/BoletaMailService.java
package com.upsjb.ms4.service.contract.boleta;

import com.upsjb.ms4.domain.entity.mail.CorreoOutbox;
import com.upsjb.ms4.dto.boleta.request.BoletaReenvioCorreoRequestDto;
import com.upsjb.ms4.dto.mail.response.CorreoOutboxResponseDto;
import com.upsjb.ms4.mail.model.EmailMessage;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;

public interface BoletaMailService {

    CorreoOutboxResponseDto programarEnvioBoletaCompraFisica(Long idBoleta, AuthenticatedUserContext actor);

    CorreoOutboxResponseDto programarEnvioBoletaCompraOnline(Long idBoleta, AuthenticatedUserContext actor);

    CorreoOutboxResponseDto programarReenvioBoleta(Long idBoleta,
                                                   BoletaReenvioCorreoRequestDto request,
                                                   AuthenticatedUserContext actor);

    void enviarBoletaDesdeOutbox(CorreoOutbox correoOutbox);

    EmailMessage construirEmailBoleta(CorreoOutbox correoOutbox);
}