// ruta: src/main/java/com/upsjb/ms4/service/contract/mail/CorreoOutboxService.java
package com.upsjb.ms4.service.contract.mail;

import com.upsjb.ms4.domain.entity.mail.CorreoOutbox;
import com.upsjb.ms4.domain.enums.TipoCorreo;
import com.upsjb.ms4.dto.mail.filter.CorreoOutboxFilterDto;
import com.upsjb.ms4.dto.mail.request.CorreoOutboxReintentoRequestDto;
import com.upsjb.ms4.dto.mail.response.CorreoOutboxResponseDto;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;

import java.util.List;

public interface CorreoOutboxService {

    CorreoOutboxResponseDto programarCorreo(TipoCorreo tipoCorreo,
                                            String entidadOrigen,
                                            Long idEntidadOrigen,
                                            Long idBoleta,
                                            String destinatarioEmail,
                                            String destinatarioNombre,
                                            String asunto,
                                            AuthenticatedUserContext actor);

    void programarAlertaAdministradores(TipoCorreo tipoCorreo,
                                        String asunto,
                                        String detalle,
                                        AuthenticatedUserContext actor);

    PageResponseDto<CorreoOutboxResponseDto> listar(CorreoOutboxFilterDto filter, PageRequestDto page);

    CorreoOutboxResponseDto reintentar(Long idCorreoOutbox,
                                       CorreoOutboxReintentoRequestDto request,
                                       AuthenticatedUserContext actor);

    CorreoOutboxResponseDto descartar(Long idCorreoOutbox,
                                      EstadoChangeRequestDto request,
                                      AuthenticatedUserContext actor);

    List<CorreoOutbox> reclamarBatchPendiente(String workerId, int batchSize);

    void marcarEnviando(Long idCorreoOutbox, String workerId);

    void marcarEnviado(Long idCorreoOutbox);

    void marcarError(Long idCorreoOutbox, String errorDetalle);
}