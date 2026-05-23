// ruta: src/main/java/com/upsjb/ms4/service/contract/snapshot/ClienteSnapshotService.java
package com.upsjb.ms4.service.contract.snapshot;

import com.upsjb.ms4.domain.entity.snapshot.ClienteSnapshotMs2;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.dto.kafka.ms2.ClienteSnapshotPayloadDto;
import com.upsjb.ms4.dto.lookup.ClienteLookupResponseDto;
import com.upsjb.ms4.dto.lookup.LookupFilterDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.dto.snapshot.filter.ClienteSnapshotFilterDto;
import com.upsjb.ms4.dto.snapshot.response.ClienteSnapshotResponseDto;

public interface ClienteSnapshotService {

    void procesarSnapshotKafka(DomainEventEnvelopeDto<ClienteSnapshotPayloadDto> envelope, String payloadJson);

    ClienteSnapshotResponseDto obtenerPorId(Long idSnapshot);

    ClienteSnapshotResponseDto obtenerPorUsuarioMs1(Long idUsuarioMs1);

    PageResponseDto<ClienteSnapshotResponseDto> listar(ClienteSnapshotFilterDto filter, PageRequestDto page);

    PageResponseDto<ClienteLookupResponseDto> lookup(LookupFilterDto filter, PageRequestDto page);

    ClienteSnapshotMs2 resolverActivoPorId(Long idSnapshot);

    ClienteSnapshotMs2 resolverActivoPorUsuarioMs1(Long idUsuarioMs1);

    boolean existeClienteActivoPorUsuarioMs1(Long idUsuarioMs1);
}