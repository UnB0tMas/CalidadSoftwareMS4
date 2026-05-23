// ruta: src/main/java/com/upsjb/ms4/service/contract/venta/VentaCalculoService.java
package com.upsjb.ms4.service.contract.venta;

import com.upsjb.ms4.domain.entity.config.ConfiguracionTributariaVersion;
import com.upsjb.ms4.domain.entity.snapshot.PrecioSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.PromocionSkuDescuentoSnapshotMs3;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.entity.venta.VentaDetalle;
import com.upsjb.ms4.domain.enums.CanalVenta;
import com.upsjb.ms4.dto.venta.request.VentaCalculoPreviewRequestDto;
import com.upsjb.ms4.dto.venta.request.VentaDetalleRequestDto;
import com.upsjb.ms4.dto.venta.response.VentaCalculoPreviewResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VentaCalculoService {

    VentaCalculoPreviewResponseDto calcularPreview(VentaCalculoPreviewRequestDto request,
                                                   CanalVenta canalVenta,
                                                   AuthenticatedUserContext actor);

    VentaCalculoResultado calcularVenta(List<VentaDetalleRequestDto> detalles,
                                        CanalVenta canalVenta,
                                        LocalDateTime fechaOperacion);

    VentaLineaCalculada calcularLinea(VentaDetalleRequestDto detalle,
                                      ConfiguracionTributariaVersion igv,
                                      LocalDateTime fechaOperacion);

    BigDecimal calcularDescuentoLinea(PrecioSnapshotMs3 precio,
                                      Optional<PromocionSkuDescuentoSnapshotMs3> promocion,
                                      Integer cantidad);

    BigDecimal calcularIgvLinea(BigDecimal baseGravada, BigDecimal porcentajeIgv);

    void validarResultadoNoNegativo(VentaCalculoResultado resultado);

    VentaDetalle construirDetalleCongelado(Venta venta, VentaLineaCalculada linea);
}