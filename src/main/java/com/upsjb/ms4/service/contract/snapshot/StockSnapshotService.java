// ruta: src/main/java/com/upsjb/ms4/service/contract/snapshot/StockSnapshotService.java
package com.upsjb.ms4.service.contract.snapshot;

import com.upsjb.ms4.domain.entity.snapshot.StockSnapshotMs3;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.dto.kafka.ms3.StockSnapshotPayloadDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.dto.snapshot.filter.StockVentaFilterDto;
import com.upsjb.ms4.dto.snapshot.response.StockVentaResponseDto;

public interface StockSnapshotService {

    void procesarStockSnapshot(DomainEventEnvelopeDto<StockSnapshotPayloadDto> envelope, String payloadJson);

    StockVentaResponseDto obtenerStock(Long idSkuMs3, Long idAlmacenMs3);

    PageResponseDto<StockVentaResponseDto> listar(StockVentaFilterDto filter, PageRequestDto page);

    StockSnapshotMs3 resolverStockDisponible(Long idSkuMs3, Long idAlmacenMs3);

    void validarDisponibilidad(Long idSkuMs3, Long idAlmacenMs3, Integer cantidadSolicitada);

    void aplicarActualizacionLocalPorSnapshot(StockSnapshotPayloadDto payload, String payloadJson);

    boolean tieneStockDisponible(Long idSkuMs3, Long idAlmacenMs3, Integer cantidadSolicitada);
}