// ruta: src/main/java/com/upsjb/ms4/service/impl/reporte/ReporteAdminServiceImpl.java
package com.upsjb.ms4.service.impl.reporte;

import com.upsjb.ms4.domain.entity.pago.Pago;
import com.upsjb.ms4.domain.entity.snapshot.ProductoSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.StockSnapshotMs3;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.entity.venta.VentaDetalle;
import com.upsjb.ms4.domain.enums.CanalVenta;
import com.upsjb.ms4.domain.enums.EstadoPago;
import com.upsjb.ms4.domain.enums.EstadoVenta;
import com.upsjb.ms4.domain.enums.MetodoPago;
import com.upsjb.ms4.dto.pago.filter.PagoFilterDto;
import com.upsjb.ms4.dto.reporte.filter.ReporteFinancieroAdminFilterDto;
import com.upsjb.ms4.dto.reporte.filter.ReporteVentasAdminFilterDto;
import com.upsjb.ms4.dto.reporte.request.ReporteAdminFinancieroRequestDto;
import com.upsjb.ms4.dto.reporte.request.ReporteAdminVentasRequestDto;
import com.upsjb.ms4.dto.reporte.response.ReporteAdminFinancieroResponseDto;
import com.upsjb.ms4.dto.reporte.response.ReporteAdminVentasResponseDto;
import com.upsjb.ms4.dto.reporte.response.ReporteGananciaEstimadaDto;
import com.upsjb.ms4.dto.reporte.response.ReporteProductoVendidoDto;
import com.upsjb.ms4.dto.reporte.response.ReporteVentaPorCanalDto;
import com.upsjb.ms4.dto.reporte.response.ReporteVentaPorCategoriaDto;
import com.upsjb.ms4.dto.reporte.response.ReporteVentaPorEmpleadoDto;
import com.upsjb.ms4.dto.reporte.response.ReporteVentaPorMetodoPagoDto;
import com.upsjb.ms4.dto.venta.filter.VentaFilterDto;
import com.upsjb.ms4.mapper.reporte.ReporteMapper;
import com.upsjb.ms4.policy.ReportePolicy;
import com.upsjb.ms4.repository.PagoRepository;
import com.upsjb.ms4.repository.ProductoSnapshotMs3Repository;
import com.upsjb.ms4.repository.StockSnapshotMs3Repository;
import com.upsjb.ms4.repository.VentaDetalleRepository;
import com.upsjb.ms4.repository.VentaRepository;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.reporte.ReporteAdminService;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.specification.PagoSpecification;
import com.upsjb.ms4.specification.VentaSpecification;
import com.upsjb.ms4.util.MoneyUtil;
import com.upsjb.ms4.validator.ReporteValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReporteAdminServiceImpl implements ReporteAdminService {

    private static final Logger log = LoggerFactory.getLogger(ReporteAdminServiceImpl.class);

    private static final EnumSet<EstadoVenta> ESTADOS_CONTABLES = EnumSet.of(
            EstadoVenta.PAGADA,
            EstadoVenta.CONFIRMADA,
            EstadoVenta.PENDIENTE_SYNC_STOCK
    );

    private static final EnumSet<EstadoPago> ESTADOS_PAGO_CONTABLES = EnumSet.of(
            EstadoPago.APROBADO
    );

    private final VentaRepository ventaRepository;
    private final VentaDetalleRepository ventaDetalleRepository;
    private final PagoRepository pagoRepository;
    private final ProductoSnapshotMs3Repository productoSnapshotRepository;
    private final StockSnapshotMs3Repository stockSnapshotRepository;
    private final ReporteMapper reporteMapper;
    private final ReportePolicy reportePolicy;
    private final ReporteValidator reporteValidator;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    public ReporteAdminServiceImpl(VentaRepository ventaRepository,
                                   VentaDetalleRepository ventaDetalleRepository,
                                   PagoRepository pagoRepository,
                                   ProductoSnapshotMs3Repository productoSnapshotRepository,
                                   StockSnapshotMs3Repository stockSnapshotRepository,
                                   ReporteMapper reporteMapper,
                                   ReportePolicy reportePolicy,
                                   ReporteValidator reporteValidator,
                                   AuthenticatedUserResolver authenticatedUserResolver) {
        this.ventaRepository = ventaRepository;
        this.ventaDetalleRepository = ventaDetalleRepository;
        this.pagoRepository = pagoRepository;
        this.productoSnapshotRepository = productoSnapshotRepository;
        this.stockSnapshotRepository = stockSnapshotRepository;
        this.reporteMapper = reporteMapper;
        this.reportePolicy = reportePolicy;
        this.reporteValidator = reporteValidator;
        this.authenticatedUserResolver = authenticatedUserResolver;
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteAdminVentasResponseDto generarReporteVentas(ReporteAdminVentasRequestDto request,
                                                              AuthenticatedUserContext actor) {
        try {
            reportePolicy.authorizeReporteAdmin(actor);
            reporteValidator.validarReporteAdminVentasRequest(request);

            ReporteVentasAdminFilterDto filter = new ReporteVentasAdminFilterDto(
                    request.fechaDesde(),
                    request.fechaHasta(),
                    request.canalVenta(),
                    request.metodoPago(),
                    request.idEmpleadoMs2(),
                    request.idClienteMs2(),
                    request.idCategoriaMs3(),
                    request.idProductoMs3(),
                    request.idSkuMs3()
            );

            ReporteDataset dataset = datasetVentas(filter);
            List<Venta> ventas = dataset.ventas();
            List<VentaDetalle> detalles = dataset.detalles();

            BigDecimal ventasTotales = totalReporte(ventas, detalles, Venta::getTotal, VentaDetalle::getTotalLinea, filter);
            BigDecimal ventasBrutas = totalReporte(ventas, detalles, Venta::getSubtotal, VentaDetalle::getSubtotal, filter);
            BigDecimal descuentoTotal = totalReporte(ventas, detalles, Venta::getDescuentoTotal, VentaDetalle::getMontoDescuento, filter);
            BigDecimal igvTotal = totalReporte(ventas, detalles, Venta::getIgvTotal, VentaDetalle::getIgvMonto, filter);
            BigDecimal ventasNetas = MoneyUtil.subtract(MoneyUtil.subtract(ventasBrutas, descuentoTotal), BigDecimal.ZERO);
            int cantidadVentas = cantidadVentasReporte(ventas, detalles, filter);
            BigDecimal ticketPromedio = cantidadVentas == 0
                    ? MoneyUtil.ZERO
                    : ventasTotales.divide(BigDecimal.valueOf(cantidadVentas), 2, RoundingMode.HALF_UP);

            int ventasOnline = contarVentasPorCanal(ventas, detalles, filter, CanalVenta.ONLINE);
            int ventasFisicas = contarVentasPorCanal(ventas, detalles, filter, CanalVenta.FISICA);

            return reporteMapper.toAdminVentasResponse(
                    filter.fechaDesde(),
                    filter.fechaHasta(),
                    ventasTotales,
                    ventasNetas,
                    ventasBrutas,
                    descuentoTotal,
                    igvTotal,
                    cantidadVentas,
                    ticketPromedio,
                    ventasOnline,
                    ventasFisicas,
                    obtenerProductosMasVendidos(filter),
                    obtenerVentasPorEmpleado(filter),
                    obtenerVentasPorMetodoPago(filter),
                    obtenerVentasPorCanal(filter),
                    obtenerVentasPorCategoria(filter)
            );
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Error técnico generando reporte administrativo de ventas. actorIdUsuarioMs1={}",
                    actor == null ? null : actor.idUsuarioMs1(), ex);
            throw internalError("No se pudo generar el reporte administrativo de ventas.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteAdminFinancieroResponseDto generarReporteFinanciero(ReporteAdminFinancieroRequestDto request,
                                                                      AuthenticatedUserContext actor) {
        try {
            reportePolicy.authorizeReporteAdmin(actor);
            reporteValidator.validarReporteAdminFinancieroRequest(request);

            ReporteFinancieroAdminFilterDto filter = new ReporteFinancieroAdminFilterDto(
                    request.fechaDesde(),
                    request.fechaHasta(),
                    request.canalVenta(),
                    request.metodoPago()
            );

            ReporteVentasAdminFilterDto ventasFilter = new ReporteVentasAdminFilterDto(
                    filter.fechaDesde(),
                    filter.fechaHasta(),
                    filter.canalVenta(),
                    filter.metodoPago(),
                    null,
                    null,
                    null,
                    null,
                    null
            );

            List<Venta> ventasContables = ventasContables(ventasFilter);
            List<Venta> ventasAnuladas = ventasAnuladas(ventasFilter);
            List<Pago> pagos = pagosContables(filter);

            BigDecimal ventasBrutas = sumVentas(ventasContables, Venta::getSubtotal);
            BigDecimal ventasNetas = sumVentas(ventasContables, Venta::getTotal);
            BigDecimal descuentoTotal = sumVentas(ventasContables, Venta::getDescuentoTotal);
            BigDecimal igvTotal = sumVentas(ventasContables, Venta::getIgvTotal);
            BigDecimal totalEfectivo = sumPagosPorMetodo(pagos, MetodoPago.EFECTIVO);
            BigDecimal totalTarjeta = sumPagosTarjeta(pagos);
            BigDecimal totalAnulado = sumVentas(ventasAnuladas, Venta::getTotal);

            return reporteMapper.toAdminFinancieroResponse(
                    filter.fechaDesde(),
                    filter.fechaHasta(),
                    ventasBrutas,
                    ventasNetas,
                    descuentoTotal,
                    igvTotal,
                    totalEfectivo,
                    totalTarjeta,
                    totalAnulado,
                    ventasContables.size(),
                    pagos.size(),
                    calcularGananciaEstimada(filter),
                    obtenerVentasPorMetodoPago(ventasFilter),
                    obtenerVentasPorCanal(ventasFilter)
            );
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Error técnico generando reporte financiero administrativo. actorIdUsuarioMs1={}",
                    actor == null ? null : actor.idUsuarioMs1(), ex);
            throw internalError("No se pudo generar el reporte financiero administrativo.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteProductoVendidoDto> obtenerProductosMasVendidos(ReporteVentasAdminFilterDto filter) {
        reportePolicy.authorizeReporteAdmin(authenticatedUserResolver.current());
        reporteValidator.validarVentasAdminFilter(filter);

        List<VentaDetalle> detalles = datasetVentas(filter).detalles();

        return detalles.stream()
                .collect(Collectors.groupingBy(
                        detalle -> new ProductoKey(
                                detalle.getIdProductoMs3(),
                                detalle.getIdSkuMs3(),
                                detalle.getCodigoProducto(),
                                detalle.getCodigoSku(),
                                detalle.getNombreProducto(),
                                detalle.getDescripcionSku()
                        ),
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .entrySet()
                .stream()
                .map(entry -> toProductoVendido(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(ReporteProductoVendidoDto::cantidadVendida).reversed())
                .limit(20)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteVentaPorEmpleadoDto> obtenerVentasPorEmpleado(ReporteVentasAdminFilterDto filter) {
        reportePolicy.authorizeReporteAdmin(authenticatedUserResolver.current());
        reporteValidator.validarVentasAdminFilter(filter);

        ReporteDataset dataset = datasetVentas(filter);
        Map<Long, Venta> ventaById = dataset.ventas().stream()
                .collect(Collectors.toMap(Venta::getId, Function.identity(), (a, b) -> a));

        if (tieneFiltroDetalle(filter)) {
            return dataset.detalles().stream()
                    .collect(Collectors.groupingBy(detalle -> empleadoKey(ventaById.get(detalle.getIdVenta()))))
                    .entrySet()
                    .stream()
                    .map(entry -> toVentaPorEmpleadoDesdeDetalles(entry.getKey(), entry.getValue(), ventaById))
                    .sorted(Comparator.comparing(ReporteVentaPorEmpleadoDto::totalVendido).reversed())
                    .toList();
        }

        return dataset.ventas().stream()
                .collect(Collectors.groupingBy(this::empleadoKey))
                .entrySet()
                .stream()
                .map(entry -> toVentaPorEmpleadoDesdeVentas(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(ReporteVentaPorEmpleadoDto::totalVendido).reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteVentaPorMetodoPagoDto> obtenerVentasPorMetodoPago(ReporteVentasAdminFilterDto filter) {
        reportePolicy.authorizeReporteAdmin(authenticatedUserResolver.current());
        reporteValidator.validarVentasAdminFilter(filter);

        ReporteDataset dataset = datasetVentas(filter);
        Map<Long, Venta> ventaById = dataset.ventas().stream()
                .collect(Collectors.toMap(Venta::getId, Function.identity(), (a, b) -> a));

        if (tieneFiltroDetalle(filter)) {
            return dataset.detalles().stream()
                    .collect(Collectors.groupingBy(detalle -> metodoPago(ventaById.get(detalle.getIdVenta()))))
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getKey() != null)
                    .map(entry -> reporteMapper.toVentaPorMetodoPago(
                            entry.getKey(),
                            distinctVentas(entry.getValue()),
                            sumDetalles(entry.getValue(), VentaDetalle::getTotalLinea)
                    ))
                    .sorted(Comparator.comparing(ReporteVentaPorMetodoPagoDto::totalVendido).reversed())
                    .toList();
        }

        return dataset.ventas().stream()
                .filter(venta -> venta.getMetodoPagoPrincipal() != null)
                .collect(Collectors.groupingBy(Venta::getMetodoPagoPrincipal))
                .entrySet()
                .stream()
                .map(entry -> reporteMapper.toVentaPorMetodoPago(
                        entry.getKey(),
                        entry.getValue().size(),
                        sumVentas(entry.getValue(), Venta::getTotal)
                ))
                .sorted(Comparator.comparing(ReporteVentaPorMetodoPagoDto::totalVendido).reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteVentaPorCanalDto> obtenerVentasPorCanal(ReporteVentasAdminFilterDto filter) {
        reportePolicy.authorizeReporteAdmin(authenticatedUserResolver.current());
        reporteValidator.validarVentasAdminFilter(filter);

        ReporteDataset dataset = datasetVentas(filter);
        Map<Long, Venta> ventaById = dataset.ventas().stream()
                .collect(Collectors.toMap(Venta::getId, Function.identity(), (a, b) -> a));

        if (tieneFiltroDetalle(filter)) {
            return dataset.detalles().stream()
                    .collect(Collectors.groupingBy(detalle -> canalVenta(ventaById.get(detalle.getIdVenta()))))
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getKey() != null)
                    .map(entry -> reporteMapper.toVentaPorCanal(
                            entry.getKey(),
                            distinctVentas(entry.getValue()),
                            sumDetalles(entry.getValue(), VentaDetalle::getTotalLinea)
                    ))
                    .sorted(Comparator.comparing(ReporteVentaPorCanalDto::totalVendido).reversed())
                    .toList();
        }

        return dataset.ventas().stream()
                .collect(Collectors.groupingBy(Venta::getCanalVenta))
                .entrySet()
                .stream()
                .map(entry -> reporteMapper.toVentaPorCanal(
                        entry.getKey(),
                        entry.getValue().size(),
                        sumVentas(entry.getValue(), Venta::getTotal)
                ))
                .sorted(Comparator.comparing(ReporteVentaPorCanalDto::totalVendido).reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteVentaPorCategoriaDto> obtenerVentasPorCategoria(ReporteVentasAdminFilterDto filter) {
        reportePolicy.authorizeReporteAdmin(authenticatedUserResolver.current());
        reporteValidator.validarVentasAdminFilter(filter);

        ReporteDataset dataset = datasetVentas(filter);

        return dataset.detalles().stream()
                .collect(Collectors.groupingBy(this::categoriaKey))
                .entrySet()
                .stream()
                .map(entry -> reporteMapper.toVentaPorCategoria(
                        entry.getKey().idCategoriaMs3(),
                        entry.getKey().codigoCategoria(),
                        entry.getKey().nombreCategoria(),
                        entry.getValue().stream().mapToInt(detalle -> safeInt(detalle.getCantidad())).sum(),
                        distinctVentas(entry.getValue()),
                        sumDetalles(entry.getValue(), VentaDetalle::getTotalLinea)
                ))
                .sorted(Comparator.comparing(ReporteVentaPorCategoriaDto::totalVendido).reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteGananciaEstimadaDto calcularGananciaEstimada(ReporteFinancieroAdminFilterDto filter) {
        reportePolicy.authorizeReporteAdmin(authenticatedUserResolver.current());
        reporteValidator.validarFinancieroAdminFilter(filter);

        ReporteVentasAdminFilterDto ventasFilter = new ReporteVentasAdminFilterDto(
                filter == null ? null : filter.fechaDesde(),
                filter == null ? null : filter.fechaHasta(),
                filter == null ? null : filter.canalVenta(),
                filter == null ? null : filter.metodoPago(),
                null,
                null,
                null,
                null,
                null
        );

        List<VentaDetalle> detalles = datasetVentas(ventasFilter).detalles();
        BigDecimal totalVenta = sumDetalles(detalles, VentaDetalle::getTotalLinea);

        BigDecimal costoEstimado = MoneyUtil.ZERO;
        boolean costoSuficiente = true;

        for (VentaDetalle detalle : detalles) {
            BigDecimal costoUnitario = resolverCostoUnitario(detalle.getIdSkuMs3());

            if (costoUnitario == null) {
                costoSuficiente = false;
                continue;
            }

            costoEstimado = MoneyUtil.add(
                    costoEstimado,
                    costoUnitario.multiply(BigDecimal.valueOf(safeInt(detalle.getCantidad())))
            );
        }

        BigDecimal ganancia = MoneyUtil.subtract(totalVenta, costoEstimado);
        BigDecimal margen = MoneyUtil.isZero(totalVenta)
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : ganancia.multiply(BigDecimal.valueOf(100))
                .divide(totalVenta, 2, RoundingMode.HALF_UP);

        return reporteMapper.toGananciaEstimada(
                totalVenta,
                costoEstimado,
                ganancia,
                margen,
                costoSuficiente,
                costoSuficiente
                        ? "Ganancia estimada calculada con costos disponibles en snapshots de stock."
                        : "Ganancia estimada parcial: faltan costos en uno o más snapshots de stock."
        );
    }

    private ReporteDataset datasetVentas(ReporteVentasAdminFilterDto filter) {
        List<Venta> ventas = ventasContables(filter);
        List<VentaDetalle> detalles = detallesFiltrados(ventas, filter);

        if (tieneFiltroDetalle(filter)) {
            Set<Long> idsConDetalle = detalles.stream()
                    .map(VentaDetalle::getIdVenta)
                    .collect(Collectors.toSet());

            ventas = ventas.stream()
                    .filter(venta -> idsConDetalle.contains(venta.getId()))
                    .toList();
        }

        return new ReporteDataset(ventas, detalles);
    }

    private List<Venta> ventasContables(ReporteVentasAdminFilterDto filter) {
        return ventasBase(filter).stream()
                .filter(venta -> venta.getEstadoVenta() != null && ESTADOS_CONTABLES.contains(venta.getEstadoVenta()))
                .toList();
    }

    private List<Venta> ventasAnuladas(ReporteVentasAdminFilterDto filter) {
        return ventasBase(filter).stream()
                .filter(venta -> venta.getEstadoVenta() == EstadoVenta.ANULADA)
                .toList();
    }

    private List<Venta> ventasBase(ReporteVentasAdminFilterDto filter) {
        ReporteVentasAdminFilterDto safe = filter == null
                ? new ReporteVentasAdminFilterDto(null, null, null, null, null, null, null, null, null)
                : filter;

        VentaFilterDto ventaFilter = new VentaFilterDto(
                null,
                null,
                safe.canalVenta(),
                null,
                safe.metodoPago(),
                null,
                safe.idClienteMs2(),
                null,
                null,
                safe.idEmpleadoMs2(),
                null,
                null,
                true,
                inicioDia(safe.fechaDesde()),
                finDia(safe.fechaHasta())
        );

        return ventaRepository.findAll(
                VentaSpecification.build(ventaFilter),
                Sort.by(Sort.Direction.ASC, "fechaVenta")
        );
    }

    private List<VentaDetalle> detallesFiltrados(List<Venta> ventas, ReporteVentasAdminFilterDto filter) {
        if (ventas == null || ventas.isEmpty()) {
            return List.of();
        }

        Set<Long> idsVenta = ventas.stream()
                .map(Venta::getId)
                .collect(Collectors.toSet());

        Map<Long, Boolean> categoriaCache = new LinkedHashMap<>();

        return ventaDetalleRepository.findByIdVentaInAndEstadoTrue(idsVenta)
                .stream()
                .filter(detalle -> filter == null || filter.idProductoMs3() == null
                        || filter.idProductoMs3().equals(detalle.getIdProductoMs3()))
                .filter(detalle -> filter == null || filter.idSkuMs3() == null
                        || filter.idSkuMs3().equals(detalle.getIdSkuMs3()))
                .filter(detalle -> filter == null || filter.idCategoriaMs3() == null
                        || categoriaCoincide(detalle.getIdProductoMs3(), filter.idCategoriaMs3(), categoriaCache))
                .toList();
    }

    // ruta: src/main/java/com/upsjb/ms4/service/impl/reporte/ReporteAdminServiceImpl.java
    private List<Pago> pagosContables(ReporteFinancieroAdminFilterDto filter) {
        PagoFilterDto pagoFilter = new PagoFilterDto(
                null,
                null,
                null,
                filter == null ? null : filter.metodoPago(),
                null,
                null,
                true,
                inicioDia(filter == null ? null : filter.fechaDesde()),
                finDia(filter == null ? null : filter.fechaHasta())
        );

        return pagoRepository.findAll(PagoSpecification.build(pagoFilter))
                .stream()
                .filter(pago -> pago.getEstadoPago() != null && ESTADOS_PAGO_CONTABLES.contains(pago.getEstadoPago()))
                .toList();
    }

    private BigDecimal resolverCostoUnitario(Long idSkuMs3) {
        if (idSkuMs3 == null) {
            return null;
        }

        return stockSnapshotRepository.findByIdSkuMs3AndEstadoTrueOrderByStockDisponibleDesc(idSkuMs3)
                .stream()
                .map(stock -> firstMoney(stock.getCostoPromedioActual(), stock.getUltimoCostoCompra()))
                .filter(value -> value != null && value.compareTo(BigDecimal.ZERO) > 0)
                .findFirst()
                .orElse(null);
    }

    private boolean categoriaCoincide(Long idProductoMs3, Long idCategoriaMs3, Map<Long, Boolean> cache) {
        if (idProductoMs3 == null || idCategoriaMs3 == null) {
            return false;
        }

        return cache.computeIfAbsent(idProductoMs3, id -> productoSnapshotRepository
                .findByIdProductoMs3AndEstadoTrue(id)
                .map(producto -> idCategoriaMs3.equals(producto.getIdCategoriaMs3()))
                .orElse(false));
    }

    private CategoriaKey categoriaKey(VentaDetalle detalle) {
        ProductoSnapshotMs3 producto = detalle.getIdProductoMs3() == null
                ? null
                : productoSnapshotRepository.findByIdProductoMs3AndEstadoTrue(detalle.getIdProductoMs3()).orElse(null);

        if (producto == null) {
            return new CategoriaKey(null, null, "SIN_CATEGORIA");
        }

        return new CategoriaKey(
                producto.getIdCategoriaMs3(),
                producto.getCodigoCategoria(),
                firstNonBlank(producto.getNombreCategoria(), "SIN_CATEGORIA")
        );
    }

    private ReporteProductoVendidoDto toProductoVendido(ProductoKey key, List<VentaDetalle> detalles) {
        return reporteMapper.toProductoVendido(
                key.idProductoMs3(),
                key.idSkuMs3(),
                key.codigoProducto(),
                key.codigoSku(),
                key.nombreProducto(),
                key.descripcionSku(),
                detalles.stream().mapToInt(detalle -> safeInt(detalle.getCantidad())).sum(),
                sumDetalles(detalles, VentaDetalle::getSubtotal),
                sumDetalles(detalles, VentaDetalle::getMontoDescuento),
                sumDetalles(detalles, VentaDetalle::getIgvMonto),
                sumDetalles(detalles, VentaDetalle::getTotalLinea)
        );
    }

    private ReporteVentaPorEmpleadoDto toVentaPorEmpleadoDesdeVentas(EmpleadoKey key, List<Venta> ventas) {
        return reporteMapper.toVentaPorEmpleado(
                key.idEmpleadoMs2(),
                key.idUsuarioEmpleadoMs1(),
                key.codigoEmpleado(),
                key.nombreEmpleado(),
                ventas.size(),
                sumVentas(ventas, Venta::getTotal),
                sumVentas(ventas.stream()
                        .filter(venta -> venta.getMetodoPagoPrincipal() == MetodoPago.EFECTIVO)
                        .toList(), Venta::getTotal),
                sumVentas(ventas.stream()
                        .filter(venta -> venta.getMetodoPagoPrincipal() != null && venta.getMetodoPagoPrincipal().esStripe())
                        .toList(), Venta::getTotal)
        );
    }

    private ReporteVentaPorEmpleadoDto toVentaPorEmpleadoDesdeDetalles(EmpleadoKey key,
                                                                       List<VentaDetalle> detalles,
                                                                       Map<Long, Venta> ventaById) {
        return reporteMapper.toVentaPorEmpleado(
                key.idEmpleadoMs2(),
                key.idUsuarioEmpleadoMs1(),
                key.codigoEmpleado(),
                key.nombreEmpleado(),
                distinctVentas(detalles),
                sumDetalles(detalles, VentaDetalle::getTotalLinea),
                sumDetalles(detalles.stream()
                        .filter(detalle -> metodoPago(ventaById.get(detalle.getIdVenta())) == MetodoPago.EFECTIVO)
                        .toList(), VentaDetalle::getTotalLinea),
                sumDetalles(detalles.stream()
                        .filter(detalle -> {
                            MetodoPago metodoPago = metodoPago(ventaById.get(detalle.getIdVenta()));
                            return metodoPago != null && metodoPago.esStripe();
                        })
                        .toList(), VentaDetalle::getTotalLinea)
        );
    }

    private EmpleadoKey empleadoKey(Venta venta) {
        if (venta == null) {
            return new EmpleadoKey(null, null, null, "SIN_EMPLEADO");
        }

        return new EmpleadoKey(
                venta.getIdEmpleadoMs2(),
                venta.getIdUsuarioEmpleadoMs1(),
                venta.getEmpleadoSnapshot() == null ? null : venta.getEmpleadoSnapshot().getCodigoEmpleado(),
                venta.getEmpleadoSnapshot() == null
                        ? "SIN_EMPLEADO"
                        : firstNonBlank(
                        venta.getEmpleadoSnapshot().getNombreCompleto(),
                        venta.getEmpleadoSnapshot().getCodigoEmpleado(),
                        "SIN_EMPLEADO"
                )
        );
    }

    private BigDecimal totalReporte(List<Venta> ventas,
                                    List<VentaDetalle> detalles,
                                    Function<Venta, BigDecimal> ventaExtractor,
                                    Function<VentaDetalle, BigDecimal> detalleExtractor,
                                    ReporteVentasAdminFilterDto filter) {
        return tieneFiltroDetalle(filter)
                ? sumDetalles(detalles, detalleExtractor)
                : sumVentas(ventas, ventaExtractor);
    }

    private int cantidadVentasReporte(List<Venta> ventas, List<VentaDetalle> detalles, ReporteVentasAdminFilterDto filter) {
        return tieneFiltroDetalle(filter) ? distinctVentas(detalles) : ventas.size();
    }

    private int contarVentasPorCanal(List<Venta> ventas, List<VentaDetalle> detalles, ReporteVentasAdminFilterDto filter, CanalVenta canal) {
        if (tieneFiltroDetalle(filter)) {
            Map<Long, Venta> ventaById = ventas.stream()
                    .collect(Collectors.toMap(Venta::getId, Function.identity(), (a, b) -> a));

            return detalles.stream()
                    .filter(detalle -> canal == canalVenta(ventaById.get(detalle.getIdVenta())))
                    .map(VentaDetalle::getIdVenta)
                    .collect(Collectors.toSet())
                    .size();
        }

        return (int) ventas.stream()
                .filter(venta -> canal == venta.getCanalVenta())
                .count();
    }

    private boolean tieneFiltroDetalle(ReporteVentasAdminFilterDto filter) {
        return filter != null && (
                filter.idProductoMs3() != null
                        || filter.idSkuMs3() != null
                        || filter.idCategoriaMs3() != null
        );
    }

    private int distinctVentas(List<VentaDetalle> detalles) {
        return detalles.stream()
                .map(VentaDetalle::getIdVenta)
                .collect(Collectors.toSet())
                .size();
    }

    private MetodoPago metodoPago(Venta venta) {
        return venta == null ? null : venta.getMetodoPagoPrincipal();
    }

    private CanalVenta canalVenta(Venta venta) {
        return venta == null ? null : venta.getCanalVenta();
    }

    private BigDecimal sumVentas(List<Venta> ventas, Function<Venta, BigDecimal> extractor) {
        if (ventas == null || ventas.isEmpty()) {
            return MoneyUtil.ZERO;
        }

        return MoneyUtil.money(ventas.stream()
                .map(extractor)
                .map(MoneyUtil::nullToZero)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private BigDecimal sumDetalles(List<VentaDetalle> detalles, Function<VentaDetalle, BigDecimal> extractor) {
        if (detalles == null || detalles.isEmpty()) {
            return MoneyUtil.ZERO;
        }

        return MoneyUtil.money(detalles.stream()
                .map(extractor)
                .map(MoneyUtil::nullToZero)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private BigDecimal sumPagosPorMetodo(List<Pago> pagos, MetodoPago metodoPago) {
        return MoneyUtil.money(pagos.stream()
                .filter(pago -> pago.getMetodoPago() == metodoPago)
                .map(Pago::getMonto)
                .map(MoneyUtil::nullToZero)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private BigDecimal sumPagosTarjeta(List<Pago> pagos) {
        return MoneyUtil.money(pagos.stream()
                .filter(pago -> pago.getMetodoPago() != null && pago.getMetodoPago().esStripe())
                .map(Pago::getMonto)
                .map(MoneyUtil::nullToZero)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private LocalDateTime inicioDia(LocalDate fecha) {
        return fecha == null ? null : fecha.atStartOfDay();
    }

    private LocalDateTime finDia(LocalDate fecha) {
        return fecha == null ? null : LocalDateTime.of(fecha, LocalTime.MAX);
    }

    private BigDecimal firstMoney(BigDecimal preferred, BigDecimal fallback) {
        if (preferred != null && preferred.compareTo(BigDecimal.ZERO) > 0) {
            return MoneyUtil.money(preferred);
        }

        if (fallback != null && fallback.compareTo(BigDecimal.ZERO) > 0) {
            return MoneyUtil.money(fallback);
        }

        return null;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }

        return null;
    }

    private BusinessException internalError(String message, RuntimeException ex) {
        return new BusinessException(
                ErrorCodes.INTERNAL_ERROR,
                message,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex
        );
    }

    private record ReporteDataset(List<Venta> ventas, List<VentaDetalle> detalles) {
    }

    private record ProductoKey(Long idProductoMs3,
                               Long idSkuMs3,
                               String codigoProducto,
                               String codigoSku,
                               String nombreProducto,
                               String descripcionSku) {
    }

    private record EmpleadoKey(Long idEmpleadoMs2,
                               Long idUsuarioEmpleadoMs1,
                               String codigoEmpleado,
                               String nombreEmpleado) {
    }

    private record CategoriaKey(Long idCategoriaMs3,
                                String codigoCategoria,
                                String nombreCategoria) {
    }
}