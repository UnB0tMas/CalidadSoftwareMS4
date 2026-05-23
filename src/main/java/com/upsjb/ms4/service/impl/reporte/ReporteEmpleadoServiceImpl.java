// ruta: src/main/java/com/upsjb/ms4/service/impl/reporte/ReporteEmpleadoServiceImpl.java
package com.upsjb.ms4.service.impl.reporte;

import com.upsjb.ms4.domain.entity.caja.Caja;
import com.upsjb.ms4.domain.entity.snapshot.EmpleadoSnapshotMs2;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.entity.venta.VentaDetalle;
import com.upsjb.ms4.domain.enums.EstadoVenta;
import com.upsjb.ms4.domain.enums.MetodoPago;
import com.upsjb.ms4.dto.reporte.filter.ReporteEmpleadoCajaFilterDto;
import com.upsjb.ms4.dto.reporte.response.ReporteEmpleadoCajaResponseDto;
import com.upsjb.ms4.dto.reporte.response.ReporteEmpleadoCierreCajaResponseDto;
import com.upsjb.ms4.dto.reporte.response.ReporteProductoVendidoDto;
import com.upsjb.ms4.dto.reporte.response.ReporteVentaPorMetodoPagoDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.dto.venta.filter.VentaFilterDto;
import com.upsjb.ms4.dto.venta.response.VentaResumenResponseDto;
import com.upsjb.ms4.mapper.reporte.ReporteMapper;
import com.upsjb.ms4.mapper.venta.VentaMapper;
import com.upsjb.ms4.policy.ReportePolicy;
import com.upsjb.ms4.repository.CajaRepository;
import com.upsjb.ms4.repository.EmpleadoSnapshotMs2Repository;
import com.upsjb.ms4.repository.VentaDetalleRepository;
import com.upsjb.ms4.repository.VentaRepository;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.service.contract.reporte.ReporteEmpleadoService;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.shared.pagination.PaginationService;
import com.upsjb.ms4.specification.VentaSpecification;
import com.upsjb.ms4.util.MoneyUtil;
import com.upsjb.ms4.validator.ReporteValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReporteEmpleadoServiceImpl implements ReporteEmpleadoService {

    private static final Logger log = LoggerFactory.getLogger(ReporteEmpleadoServiceImpl.class);

    private static final String RECURSO_CAJA = "Caja";
    private static final String RECURSO_EMPLEADO = "Empleado snapshot MS2";

    private static final EnumSet<EstadoVenta> ESTADOS_CONTABLES = EnumSet.of(
            EstadoVenta.PAGADA,
            EstadoVenta.CONFIRMADA,
            EstadoVenta.PENDIENTE_SYNC_STOCK
    );

    private final VentaRepository ventaRepository;
    private final VentaDetalleRepository ventaDetalleRepository;
    private final CajaRepository cajaRepository;
    private final EmpleadoSnapshotMs2Repository empleadoSnapshotRepository;
    private final ReporteMapper reporteMapper;
    private final VentaMapper ventaMapper;
    private final ReportePolicy reportePolicy;
    private final ReporteValidator reporteValidator;
    private final PaginationService paginationService;
    private final Clock clock;

    public ReporteEmpleadoServiceImpl(VentaRepository ventaRepository,
                                      VentaDetalleRepository ventaDetalleRepository,
                                      CajaRepository cajaRepository,
                                      EmpleadoSnapshotMs2Repository empleadoSnapshotRepository,
                                      ReporteMapper reporteMapper,
                                      VentaMapper ventaMapper,
                                      ReportePolicy reportePolicy,
                                      ReporteValidator reporteValidator,
                                      PaginationService paginationService,
                                      Clock clock) {
        this.ventaRepository = ventaRepository;
        this.ventaDetalleRepository = ventaDetalleRepository;
        this.cajaRepository = cajaRepository;
        this.empleadoSnapshotRepository = empleadoSnapshotRepository;
        this.reporteMapper = reporteMapper;
        this.ventaMapper = ventaMapper;
        this.reportePolicy = reportePolicy;
        this.reporteValidator = reporteValidator;
        this.paginationService = paginationService;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteEmpleadoCajaResponseDto obtenerReporteCajaHoy(AuthenticatedUserContext actor) {
        return obtenerReporteCajaPorFecha(LocalDate.now(clock), actor);
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteEmpleadoCajaResponseDto obtenerReporteCajaPorFecha(LocalDate fecha,
                                                                     AuthenticatedUserContext actor) {
        try {
            reportePolicy.authorizeReporteEmpleadoActual(actor);
            reporteValidator.validarFecha(fecha);

            EmpleadoSnapshotMs2 empleado = resolverEmpleadoParaReporte(actor);
            Caja caja = resolverCajaPorFecha(fecha);
            Long idUsuarioEmpleadoMs1 = empleado == null ? null : empleado.getIdUsuarioMs1();

            List<Venta> ventas = ventasEmpleadoPorFecha(fecha, idUsuarioEmpleadoMs1);
            List<Venta> anuladas = ventasEmpleadoPorFechaEstado(fecha, idUsuarioEmpleadoMs1, EstadoVenta.ANULADA);
            List<VentaDetalle> detalles = detallesVentas(ventas);

            BigDecimal totalEfectivo = sumVentas(
                    ventas.stream()
                            .filter(venta -> venta.getMetodoPagoPrincipal() == MetodoPago.EFECTIVO)
                            .toList(),
                    Venta::getTotal
            );

            BigDecimal totalTarjeta = sumVentas(
                    ventas.stream()
                            .filter(venta -> venta.getMetodoPagoPrincipal() != null && venta.getMetodoPagoPrincipal().esStripe())
                            .toList(),
                    Venta::getTotal
            );

            BigDecimal totalVendido = sumVentas(ventas, Venta::getTotal);

            return reporteMapper.toEmpleadoCajaResponse(
                    fecha,
                    fecha,
                    empleado,
                    caja,
                    totalEfectivo,
                    totalTarjeta,
                    totalVendido,
                    ventas.size(),
                    anuladas.size(),
                    productosVendidos(detalles)
            );
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Error técnico generando reporte de caja de empleado. fecha={}, actorIdUsuarioMs1={}",
                    fecha,
                    actor == null ? null : actor.idUsuarioMs1(),
                    ex);
            throw internalError("No se pudo generar el reporte de caja del empleado.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteEmpleadoCierreCajaResponseDto obtenerReporteCierreCaja(Long idCaja,
                                                                         AuthenticatedUserContext actor) {
        try {
            reporteValidator.validarIdCaja(idCaja);

            Caja caja = cajaRepository.findById(idCaja)
                    .filter(Caja::isActivo)
                    .orElseThrow(() -> NotFoundException.byId(RECURSO_CAJA, idCaja));

            reportePolicy.authorizeReporteEmpleado(actor, caja.getIdUsuarioAperturaMs1());

            int cantidadVentas = (int) ventaRepository.findByIdCajaAndEstadoTrueOrderByFechaVentaAsc(caja.getId())
                    .stream()
                    .filter(venta -> venta.getEstadoVenta() != null && ESTADOS_CONTABLES.contains(venta.getEstadoVenta()))
                    .count();

            return reporteMapper.toEmpleadoCierreCajaResponse(caja, cantidadVentas);
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Error técnico generando reporte de cierre de caja. idCaja={}, actorIdUsuarioMs1={}",
                    idCaja,
                    actor == null ? null : actor.idUsuarioMs1(),
                    ex);
            throw internalError("No se pudo generar el reporte de cierre de caja.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<VentaResumenResponseDto> listarVentasEmpleado(ReporteEmpleadoCajaFilterDto filter,
                                                                         PageRequestDto page,
                                                                         AuthenticatedUserContext actor) {
        try {
            reporteValidator.validarEmpleadoCajaFilter(filter);

            Long idUsuarioEmpleadoMs1 = actor != null && actor.isAdmin()
                    ? filter == null ? null : filter.idUsuarioEmpleadoMs1()
                    : actor == null ? null : actor.idUsuarioMs1();

            reportePolicy.authorizeReporteEmpleado(actor, idUsuarioEmpleadoMs1);

            VentaFilterDto ventaFilter = new VentaFilterDto(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    filter == null ? null : filter.idEmpleadoMs2(),
                    idUsuarioEmpleadoMs1,
                    null,
                    true,
                    inicioDia(filter == null ? null : filter.fechaDesde()),
                    finDia(filter == null ? null : filter.fechaHasta())
            );

            Page<Venta> ventas = ventaRepository.findAll(
                    VentaSpecification.build(ventaFilter),
                    paginationService.toPageable(page, "fechaVenta")
            );

            return paginationService.toPageResponse(ventas, ventaMapper::toResumenResponse);
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Error técnico listando ventas del empleado. actorIdUsuarioMs1={}",
                    actor == null ? null : actor.idUsuarioMs1(),
                    ex);
            throw internalError("No se pudieron listar las ventas del empleado.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteVentaPorMetodoPagoDto obtenerResumenPorMetodoPago(LocalDate fecha,
                                                                    AuthenticatedUserContext actor) {
        try {
            reportePolicy.authorizeReporteEmpleadoActual(actor);
            reporteValidator.validarFecha(fecha);

            EmpleadoSnapshotMs2 empleado = resolverEmpleadoParaReporte(actor);
            Long idUsuarioEmpleadoMs1 = empleado == null ? null : empleado.getIdUsuarioMs1();
            List<Venta> ventas = ventasEmpleadoPorFecha(fecha, idUsuarioEmpleadoMs1);

            return reporteMapper.toVentaPorMetodoPago(
                    null,
                    ventas.size(),
                    sumVentas(ventas, Venta::getTotal)
            );
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Error técnico generando resumen por método de pago del empleado. fecha={}, actorIdUsuarioMs1={}",
                    fecha,
                    actor == null ? null : actor.idUsuarioMs1(),
                    ex);
            throw internalError("No se pudo generar el resumen por método de pago del empleado.", ex);
        }
    }

    private EmpleadoSnapshotMs2 resolverEmpleadoParaReporte(AuthenticatedUserContext actor) {
        if (actor == null || actor.idUsuarioMs1() == null) {
            throw new NotFoundException("No se pudo resolver el actor autenticado para el reporte.");
        }

        return empleadoSnapshotRepository.findByIdUsuarioMs1AndEstadoTrue(actor.idUsuarioMs1())
                .orElseGet(() -> {
                    if (actor.isAdmin()) {
                        return null;
                    }

                    throw NotFoundException.byId(RECURSO_EMPLEADO, actor.idUsuarioMs1());
                });
    }

    private Caja resolverCajaPorFecha(LocalDate fecha) {
        return cajaRepository.findByFechaOperacionBetweenAndEstadoTrueOrderByFechaOperacionDesc(fecha, fecha)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private List<Venta> ventasEmpleadoPorFecha(LocalDate fecha, Long idUsuarioEmpleadoMs1) {
        return ventasEmpleadoPorFechaEstado(fecha, idUsuarioEmpleadoMs1, null)
                .stream()
                .filter(venta -> venta.getEstadoVenta() != null && ESTADOS_CONTABLES.contains(venta.getEstadoVenta()))
                .toList();
    }

    private List<Venta> ventasEmpleadoPorFechaEstado(LocalDate fecha,
                                                     Long idUsuarioEmpleadoMs1,
                                                     EstadoVenta estadoVenta) {
        VentaFilterDto filter = new VentaFilterDto(
                null,
                null,
                null,
                estadoVenta,
                null,
                null,
                null,
                null,
                null,
                null,
                idUsuarioEmpleadoMs1,
                null,
                true,
                inicioDia(fecha),
                finDia(fecha)
        );

        return ventaRepository.findAll(
                VentaSpecification.build(filter),
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "fechaVenta")
        );
    }

    private List<VentaDetalle> detallesVentas(List<Venta> ventas) {
        if (ventas == null || ventas.isEmpty()) {
            return List.of();
        }

        return ventaDetalleRepository.findByIdVentaInAndEstadoTrue(
                ventas.stream().map(Venta::getId).collect(Collectors.toSet())
        );
    }

    private List<ReporteProductoVendidoDto> productosVendidos(List<VentaDetalle> detalles) {
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
                .map(entry -> reporteMapper.toProductoVendido(
                        entry.getKey().idProductoMs3(),
                        entry.getKey().idSkuMs3(),
                        entry.getKey().codigoProducto(),
                        entry.getKey().codigoSku(),
                        entry.getKey().nombreProducto(),
                        entry.getKey().descripcionSku(),
                        entry.getValue().stream().mapToInt(detalle -> safeInt(detalle.getCantidad())).sum(),
                        sumDetalles(entry.getValue(), VentaDetalle::getSubtotal),
                        sumDetalles(entry.getValue(), VentaDetalle::getMontoDescuento),
                        sumDetalles(entry.getValue(), VentaDetalle::getIgvMonto),
                        sumDetalles(entry.getValue(), VentaDetalle::getTotalLinea)
                ))
                .sorted(Comparator.comparing(ReporteProductoVendidoDto::cantidadVendida).reversed())
                .limit(20)
                .toList();
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

    private LocalDateTime inicioDia(LocalDate fecha) {
        return fecha == null ? null : fecha.atStartOfDay();
    }

    private LocalDateTime finDia(LocalDate fecha) {
        return fecha == null ? null : LocalDateTime.of(fecha, LocalTime.MAX);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private BusinessException internalError(String message, RuntimeException ex) {
        return new BusinessException(
                ErrorCodes.INTERNAL_ERROR,
                message,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex
        );
    }

    private record ProductoKey(Long idProductoMs3,
                               Long idSkuMs3,
                               String codigoProducto,
                               String codigoSku,
                               String nombreProducto,
                               String descripcionSku) {
    }
}