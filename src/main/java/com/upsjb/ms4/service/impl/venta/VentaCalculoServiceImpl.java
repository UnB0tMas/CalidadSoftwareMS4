// ruta: src/main/java/com/upsjb/ms4/service/impl/venta/VentaCalculoServiceImpl.java
package com.upsjb.ms4.service.impl.venta;

import com.upsjb.ms4.domain.entity.config.ConfiguracionTributariaVersion;
import com.upsjb.ms4.domain.entity.snapshot.PrecioSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.ProductoSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.PromocionSkuDescuentoSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.PromocionSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.SkuSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.StockSnapshotMs3;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.entity.venta.VentaDetalle;
import com.upsjb.ms4.domain.enums.CanalVenta;
import com.upsjb.ms4.domain.enums.TipoDescuento;
import com.upsjb.ms4.dto.venta.request.VentaCalculoPreviewRequestDto;
import com.upsjb.ms4.dto.venta.request.VentaDetalleRequestDto;
import com.upsjb.ms4.dto.venta.response.VentaCalculoPreviewResponseDto;
import com.upsjb.ms4.dto.venta.response.VentaDetalleResponseDto;
import com.upsjb.ms4.policy.VentaPolicy;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.service.contract.config.ConfiguracionTributariaService;
import com.upsjb.ms4.service.contract.reference.Ms4ReferenceResolverService;
import com.upsjb.ms4.service.contract.venta.VentaCalculoResultado;
import com.upsjb.ms4.service.contract.venta.VentaCalculoService;
import com.upsjb.ms4.service.contract.venta.VentaLineaCalculada;
import com.upsjb.ms4.shared.exception.ValidationException;
import com.upsjb.ms4.util.JsonUtil;
import com.upsjb.ms4.util.MoneyUtil;
import com.upsjb.ms4.validator.VentaCalculoValidator;
import com.upsjb.ms4.validator.VentaDetalleValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VentaCalculoServiceImpl implements VentaCalculoService {

    private static final String MONEDA_DEFAULT = "PEN";

    private final Ms4ReferenceResolverService referenceResolver;
    private final ConfiguracionTributariaService configuracionTributariaService;
    private final VentaPolicy ventaPolicy;
    private final VentaCalculoValidator ventaCalculoValidator;
    private final VentaDetalleValidator ventaDetalleValidator;

    public VentaCalculoServiceImpl(Ms4ReferenceResolverService referenceResolver,
                                   ConfiguracionTributariaService configuracionTributariaService,
                                   VentaPolicy ventaPolicy,
                                   VentaCalculoValidator ventaCalculoValidator,
                                   VentaDetalleValidator ventaDetalleValidator) {
        this.referenceResolver = referenceResolver;
        this.configuracionTributariaService = configuracionTributariaService;
        this.ventaPolicy = ventaPolicy;
        this.ventaCalculoValidator = ventaCalculoValidator;
        this.ventaDetalleValidator = ventaDetalleValidator;
    }

    @Override
    @Transactional(readOnly = true)
    public VentaCalculoPreviewResponseDto calcularPreview(VentaCalculoPreviewRequestDto request,
                                                          CanalVenta canalVenta,
                                                          AuthenticatedUserContext actor) {
        ventaPolicy.authorizePreviewCalculo(actor, canalVenta);
        ventaCalculoValidator.validarPreviewRequest(request);

        VentaCalculoResultado resultado = calcularVenta(request.detalles(), canalVenta, LocalDateTime.now());

        return new VentaCalculoPreviewResponseDto(
                normalizarMoneda(request.moneda()),
                resultado.subtotal(),
                resultado.descuentoTotal(),
                resultado.opGravada(),
                resultado.opExonerada(),
                resultado.opInafecta(),
                resultado.igvPorcentaje(),
                resultado.igvTotal(),
                resultado.total(),
                resultado.stockSuficiente(),
                resultado.lineas().stream().map(this::toPreviewDetalle).toList(),
                resultado.advertencias()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public VentaCalculoResultado calcularVenta(List<VentaDetalleRequestDto> detalles,
                                               CanalVenta canalVenta,
                                               LocalDateTime fechaOperacion) {
        ventaDetalleValidator.validarSkuNoDuplicado(detalles);

        ConfiguracionTributariaVersion igv = configuracionTributariaService.resolverIgvVigenteParaVenta();
        ventaCalculoValidator.validarIgvVigente(igv, fechaOperacion);

        List<VentaLineaCalculada> lineas = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();

        for (VentaDetalleRequestDto detalle : detalles) {
            VentaLineaCalculada linea = calcularLinea(detalle, igv, fechaOperacion);
            lineas.add(linea);

            if (!Boolean.TRUE.equals(linea.stockSuficiente())) {
                advertencias.add("Stock insuficiente para SKU " + linea.sku().getCodigoSku()
                        + " en almacén " + linea.stock().getCodigoAlmacen()
                        + ". Disponible: " + linea.stock().getStockDisponible()
                        + ", solicitado: " + linea.cantidad() + ".");
            }
        }

        BigDecimal subtotal = MoneyUtil.sum(lineas.stream().map(VentaLineaCalculada::subtotal).toList());
        BigDecimal descuentoTotal = MoneyUtil.sum(lineas.stream().map(VentaLineaCalculada::montoDescuento).toList());
        BigDecimal igvTotal = MoneyUtil.sum(lineas.stream().map(VentaLineaCalculada::igvMonto).toList());
        BigDecimal opGravada = MoneyUtil.subtract(subtotal, descuentoTotal);
        BigDecimal total = MoneyUtil.add(opGravada, igvTotal);

        VentaCalculoResultado resultado = new VentaCalculoResultado(
                MONEDA_DEFAULT,
                subtotal,
                descuentoTotal,
                opGravada,
                MoneyUtil.ZERO,
                MoneyUtil.ZERO,
                igv.getPorcentaje(),
                igvTotal,
                total,
                lineas.stream().allMatch(linea -> Boolean.TRUE.equals(linea.stockSuficiente())),
                List.copyOf(lineas),
                List.copyOf(advertencias)
        );

        validarResultadoNoNegativo(resultado);
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public VentaLineaCalculada calcularLinea(VentaDetalleRequestDto detalle,
                                             ConfiguracionTributariaVersion igv,
                                             LocalDateTime fechaOperacion) {
        ventaDetalleValidator.validarDetalleRequest(detalle);
        ventaCalculoValidator.validarIgvVigente(igv, fechaOperacion);

        SkuSnapshotMs3 sku = referenceResolver.resolverSkuActivo(detalle.idSkuMs3(), detalle.codigoSku(), detalle.barcode());
        ProductoSnapshotMs3 producto = referenceResolver.resolverProductoActivo(sku.getIdProductoMs3(), sku.getCodigoProducto());
        PrecioSnapshotMs3 precio = referenceResolver.resolverPrecioVigentePorSku(sku.getIdSkuMs3(), sku.getCodigoSku());

        StockSnapshotMs3 stock = referenceResolver.resolverStockActivo(
                null,
                sku.getIdSkuMs3(),
                detalle.idAlmacenMs3(),
                sku.getCodigoSku(),
                detalle.codigoAlmacen()
        );

        Optional<PromocionSkuDescuentoSnapshotMs3> descuento = referenceResolver
                .resolverDescuentoPromocionPreferentePorSku(sku.getIdSkuMs3(), sku.getCodigoSku());

        Optional<PromocionSnapshotMs3> promocion = descuento
                .flatMap(value -> referenceResolver.resolverPromocionVigente(value.getIdPromocionMs3(), null));

        promocion.ifPresent(value -> ventaCalculoValidator.validarPromocionVigente(value, fechaOperacion));
        ventaCalculoValidator.validarPrecioVigente(precio, fechaOperacion);
        ventaDetalleValidator.validarDetalleVendible(producto, sku, precio, stock, detalle.cantidad());

        BigDecimal precioUnitarioBase = MoneyUtil.money(precio.getPrecioVenta());
        BigDecimal subtotal = MoneyUtil.multiply(precioUnitarioBase, detalle.cantidad());
        BigDecimal montoDescuento = calcularDescuentoLinea(precio, descuento, detalle.cantidad());
        BigDecimal baseGravada = MoneyUtil.subtract(subtotal, montoDescuento);
        BigDecimal igvMonto = calcularIgvLinea(baseGravada, igv.getPorcentaje());
        BigDecimal totalLinea = MoneyUtil.add(baseGravada, igvMonto);
        BigDecimal precioUnitarioFinal = MoneyUtil.divide(baseGravada, BigDecimal.valueOf(detalle.cantidad()));

        return new VentaLineaCalculada(
                producto,
                sku,
                precio,
                promocion,
                descuento,
                stock,
                detalle.cantidad(),
                precioUnitarioBase,
                precioUnitarioFinal,
                subtotal,
                descuento.map(PromocionSkuDescuentoSnapshotMs3::getTipoDescuento).orElse(null),
                descuento.map(PromocionSkuDescuentoSnapshotMs3::getValorDescuento).orElse(null),
                montoDescuento,
                igv.getPorcentaje(),
                igvMonto,
                totalLinea,
                stock.getStockDisponible() != null && stock.getStockDisponible() >= detalle.cantidad()
        );
    }

    @Override
    public BigDecimal calcularDescuentoLinea(PrecioSnapshotMs3 precio,
                                             Optional<PromocionSkuDescuentoSnapshotMs3> promocion,
                                             Integer cantidad) {
        if (promocion == null || promocion.isEmpty()) {
            return MoneyUtil.ZERO;
        }

        PromocionSkuDescuentoSnapshotMs3 descuento = promocion.get();
        BigDecimal subtotal = MoneyUtil.multiply(precio.getPrecioVenta(), cantidad);
        BigDecimal valor = MoneyUtil.nullToZero(descuento.getValorDescuento());

        BigDecimal monto = descuento.getTipoDescuento() == TipoDescuento.PORCENTAJE
                ? subtotal.multiply(valor).divide(BigDecimal.valueOf(100), MoneyUtil.MONEY_SCALE, MoneyUtil.ROUNDING)
                : valor.multiply(BigDecimal.valueOf(cantidad == null ? 0 : cantidad));

        return MoneyUtil.min(monto, subtotal);
    }

    @Override
    public BigDecimal calcularIgvLinea(BigDecimal baseGravada, BigDecimal porcentajeIgv) {
        return MoneyUtil.money(
                MoneyUtil.money(baseGravada)
                        .multiply(MoneyUtil.nullToZero(porcentajeIgv))
                        .divide(BigDecimal.valueOf(100), MoneyUtil.MONEY_SCALE, MoneyUtil.ROUNDING)
        );
    }

    @Override
    public void validarResultadoNoNegativo(VentaCalculoResultado resultado) {
        if (resultado == null) {
            throw new ValidationException("El resultado de cálculo de venta es obligatorio.");
        }

        ventaCalculoValidator.validarTotalesNoNegativos(
                resultado.subtotal(),
                resultado.descuentoTotal(),
                resultado.igvTotal(),
                resultado.total()
        );
    }

    @Override
    public VentaDetalle construirDetalleCongelado(Venta venta, VentaLineaCalculada linea) {
        if (venta == null || venta.getId() == null) {
            throw new ValidationException("La venta persistida es obligatoria para congelar el detalle.");
        }

        if (linea == null) {
            throw new ValidationException("La línea calculada es obligatoria para congelar el detalle.");
        }

        return VentaDetalle.builder()
                .idVenta(venta.getId())
                .idProductoMs3(linea.producto().getIdProductoMs3())
                .idSkuMs3(linea.sku().getIdSkuMs3())
                .idAlmacenMs3(linea.stock().getIdAlmacenMs3())
                .codigoProducto(linea.producto().getCodigoProducto())
                .codigoSku(linea.sku().getCodigoSku())
                .codigoAlmacen(linea.stock().getCodigoAlmacen())
                .nombreProducto(linea.producto().getNombre())
                .descripcionSku(linea.sku().getAtributosJson())
                .cantidad(linea.cantidad())
                .precioUnitarioBase(linea.precioUnitarioBase())
                .precioUnitarioFinal(linea.precioUnitarioFinal())
                .subtotal(linea.subtotal())
                .tipoDescuento(linea.tipoDescuento())
                .valorDescuento(linea.valorDescuento())
                .montoDescuento(linea.montoDescuento())
                .idPromocionMs3(linea.promocion().map(PromocionSnapshotMs3::getIdPromocionMs3).orElse(null))
                .idPromocionVersionMs3(linea.promocion().map(PromocionSnapshotMs3::getIdPromocionVersionMs3).orElse(null))
                .codigoPromocion(linea.promocion().map(PromocionSnapshotMs3::getCodigoPromocion).orElse(null))
                .igvPorcentaje(linea.igvPorcentaje())
                .igvMonto(linea.igvMonto())
                .totalLinea(linea.totalLinea())
                .stockSnapshotFisico(linea.stock().getStockFisico())
                .stockSnapshotReservado(linea.stock().getStockReservado())
                .stockSnapshotDisponible(linea.stock().getStockDisponible())
                .payloadProductoSnapshotJson(JsonUtil.toJson(linea.producto()))
                .payloadSkuSnapshotJson(JsonUtil.toJson(linea.sku()))
                .payloadPrecioSnapshotJson(JsonUtil.toJson(linea.precio()))
                .payloadPromocionSnapshotJson(linea.promocion().map(JsonUtil::toJson).orElse(null))
                .payloadStockSnapshotJson(JsonUtil.toJson(linea.stock()))
                .estado(true)
                .build();
    }

    private VentaDetalleResponseDto toPreviewDetalle(VentaLineaCalculada linea) {
        return new VentaDetalleResponseDto(
                null,
                null,
                linea.producto().getIdProductoMs3(),
                linea.sku().getIdSkuMs3(),
                linea.stock().getIdAlmacenMs3(),
                linea.producto().getCodigoProducto(),
                linea.sku().getCodigoSku(),
                linea.stock().getCodigoAlmacen(),
                linea.producto().getNombre(),
                linea.sku().getAtributosJson(),
                linea.cantidad(),
                linea.precioUnitarioBase(),
                linea.precioUnitarioFinal(),
                linea.subtotal(),
                linea.tipoDescuento(),
                linea.valorDescuento(),
                linea.montoDescuento(),
                linea.promocion().map(PromocionSnapshotMs3::getIdPromocionMs3).orElse(null),
                linea.promocion().map(PromocionSnapshotMs3::getIdPromocionVersionMs3).orElse(null),
                linea.promocion().map(PromocionSnapshotMs3::getCodigoPromocion).orElse(null),
                linea.igvPorcentaje(),
                linea.igvMonto(),
                linea.totalLinea(),
                linea.stock().getStockFisico(),
                linea.stock().getStockReservado(),
                linea.stock().getStockDisponible(),
                true,
                null,
                null
        );
    }

    private String normalizarMoneda(String moneda) {
        return moneda == null || moneda.isBlank() ? MONEDA_DEFAULT : moneda.trim().toUpperCase();
    }
}