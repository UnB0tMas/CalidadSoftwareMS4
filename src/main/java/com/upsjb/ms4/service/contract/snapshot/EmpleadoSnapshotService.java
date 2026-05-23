// ruta: src/main/java/com/upsjb/ms4/service/contract/snapshot/EmpleadoSnapshotService.java
package com.upsjb.ms4.service.contract.snapshot;

import com.upsjb.ms4.domain.entity.snapshot.EmpleadoSnapshotMs2;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.dto.kafka.ms2.EmpleadoSnapshotPayloadDto;
import com.upsjb.ms4.dto.lookup.EmpleadoLookupResponseDto;
import com.upsjb.ms4.dto.lookup.LookupFilterDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.dto.snapshot.filter.EmpleadoSnapshotFilterDto;
import com.upsjb.ms4.dto.snapshot.response.EmpleadoSnapshotResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;

public interface EmpleadoSnapshotService {

    void procesarSnapshotKafka(DomainEventEnvelopeDto<EmpleadoSnapshotPayloadDto> envelope, String payloadJson);

    EmpleadoSnapshotResponseDto obtenerPorId(Long idSnapshot);

    EmpleadoSnapshotResponseDto obtenerPorUsuarioMs1(Long idUsuarioMs1);

    PageResponseDto<EmpleadoSnapshotResponseDto> listar(EmpleadoSnapshotFilterDto filter, PageRequestDto page);

    PageResponseDto<EmpleadoLookupResponseDto> lookup(LookupFilterDto filter, PageRequestDto page);

    EmpleadoSnapshotMs2 resolverActivoPorUsuarioMs1(Long idUsuarioMs1);

    EmpleadoSnapshotMs2 resolverActivoPorId(Long idSnapshot);

    void validarEmpleadoPuedeVender(AuthenticatedUserContext actor);
}