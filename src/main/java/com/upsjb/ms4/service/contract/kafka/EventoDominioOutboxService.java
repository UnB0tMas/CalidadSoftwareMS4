// ruta: src/main/java/com/upsjb/ms4/service/contract/kafka/EventoDominioOutboxService.java
package com.upsjb.ms4.service.contract.kafka;

import com.upsjb.ms4.domain.entity.kafka.EventoDominioOutbox;
import com.upsjb.ms4.dto.kafka.filter.EventoDominioOutboxFilterDto;
import com.upsjb.ms4.dto.kafka.ms4.Ms4StockCommandEventDto;
import com.upsjb.ms4.dto.kafka.response.EventoDominioOutboxResponseDto;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;

import java.util.List;

public interface EventoDominioOutboxService {

    EventoDominioOutbox crearEvento(String aggregateType,
                                    String aggregateId,
                                    String topic,
                                    String eventType,
                                    Object payload,
                                    AuthenticatedUserContext actor);

    EventoDominioOutbox crearEventoStockCommand(Ms4StockCommandEventDto event,
                                                AuthenticatedUserContext actor);

    PageResponseDto<EventoDominioOutboxResponseDto> listar(EventoDominioOutboxFilterDto filter,
                                                           PageRequestDto page);

    EventoDominioOutboxResponseDto reintentar(Long idOutbox, AuthenticatedUserContext actor);

    EventoDominioOutboxResponseDto descartar(Long idOutbox,
                                             EstadoChangeRequestDto request,
                                             AuthenticatedUserContext actor);

    List<EventoDominioOutbox> reclamarBatchPendiente(String workerId, int batchSize);

    void marcarPublicando(Long idOutbox, String workerId);

    void marcarPublicado(Long idOutbox);

    void marcarError(Long idOutbox, String errorDetalle);
}