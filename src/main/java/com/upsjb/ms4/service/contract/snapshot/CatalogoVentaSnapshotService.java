// ruta: src/main/java/com/upsjb/ms4/service/contract/snapshot/CatalogoVentaSnapshotService.java
package com.upsjb.ms4.service.contract.snapshot;

import com.upsjb.ms4.domain.entity.snapshot.PrecioSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.PromocionSkuDescuentoSnapshotMs3;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.dto.kafka.ms3.PrecioSnapshotPayloadDto;
import com.upsjb.ms4.dto.kafka.ms3.ProductoSnapshotPayloadDto;
import com.upsjb.ms4.dto.kafka.ms3.PromocionSkuDescuentoPayloadDto;
import com.upsjb.ms4.dto.kafka.ms3.PromocionSnapshotPayloadDto;
import com.upsjb.ms4.dto.kafka.ms3.SkuSnapshotPayloadDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.dto.snapshot.filter.ProductoVentaFilterDto;
import com.upsjb.ms4.dto.snapshot.filter.SkuVentaFilterDto;
import com.upsjb.ms4.dto.snapshot.response.ProductoVentaResponseDto;
import com.upsjb.ms4.dto.snapshot.response.SkuVentaResponseDto;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CatalogoVentaSnapshotService {

    void procesarProductoSnapshot(DomainEventEnvelopeDto<ProductoSnapshotPayloadDto> envelope, String payloadJson);

    void procesarSkuSnapshot(DomainEventEnvelopeDto<SkuSnapshotPayloadDto> envelope, String payloadJson);

    void procesarPrecioSnapshot(DomainEventEnvelopeDto<PrecioSnapshotPayloadDto> envelope, String payloadJson);

    void procesarPromocionSnapshot(DomainEventEnvelopeDto<PromocionSnapshotPayloadDto> envelope, String payloadJson);

    void procesarPromocionSkuDescuentoSnapshot(
            DomainEventEnvelopeDto<PromocionSkuDescuentoPayloadDto> envelope,
            String payloadJson
    );

    ProductoVentaResponseDto obtenerProductoVendible(Long idProductoMs3);

    SkuVentaResponseDto obtenerSkuVendible(Long idSkuMs3);

    PageResponseDto<ProductoVentaResponseDto> listarProductosVendibles(
            ProductoVentaFilterDto filter,
            PageRequestDto page
    );

    PageResponseDto<SkuVentaResponseDto> listarSkusVendibles(
            SkuVentaFilterDto filter,
            PageRequestDto page
    );

    PrecioSnapshotMs3 resolverPrecioVigente(Long idSkuMs3);

    Optional<PromocionSkuDescuentoSnapshotMs3> resolverPromocionAplicable(
            Long idSkuMs3,
            Integer cantidad,
            LocalDateTime fechaOperacion
    );
}