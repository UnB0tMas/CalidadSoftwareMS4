// ruta: src/main/java/com/upsjb/ms4/service/impl/reference/Ms4ReferenceResolverServiceImpl.java
package com.upsjb.ms4.service.impl.reference;

import com.upsjb.ms4.domain.entity.caja.Caja;
import com.upsjb.ms4.domain.entity.config.SerieBoleta;
import com.upsjb.ms4.domain.entity.contingencia.ModoContingencia;
import com.upsjb.ms4.domain.entity.kafka.InventarioEventoPendienteMs4;
import com.upsjb.ms4.domain.entity.pago.Pago;
import com.upsjb.ms4.domain.entity.snapshot.ClienteSnapshotMs2;
import com.upsjb.ms4.domain.entity.snapshot.EmpleadoSnapshotMs2;
import com.upsjb.ms4.domain.entity.snapshot.PrecioSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.ProductoSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.PromocionSkuDescuentoSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.PromocionSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.SkuSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.StockSnapshotMs3;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.enums.EstadoCaja;
import com.upsjb.ms4.domain.enums.EstadoContingencia;
import com.upsjb.ms4.repository.CajaRepository;
import com.upsjb.ms4.repository.ClienteSnapshotMs2Repository;
import com.upsjb.ms4.repository.EmpleadoSnapshotMs2Repository;
import com.upsjb.ms4.repository.InventarioEventoPendienteMs4Repository;
import com.upsjb.ms4.repository.ModoContingenciaRepository;
import com.upsjb.ms4.repository.PagoRepository;
import com.upsjb.ms4.repository.PrecioSnapshotMs3Repository;
import com.upsjb.ms4.repository.ProductoSnapshotMs3Repository;
import com.upsjb.ms4.repository.PromocionSkuDescuentoSnapshotMs3Repository;
import com.upsjb.ms4.repository.PromocionSnapshotMs3Repository;
import com.upsjb.ms4.repository.SerieBoletaRepository;
import com.upsjb.ms4.repository.SkuSnapshotMs3Repository;
import com.upsjb.ms4.repository.StockSnapshotMs3Repository;
import com.upsjb.ms4.repository.VentaRepository;
import com.upsjb.ms4.service.contract.reference.Ms4ReferenceResolverService;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.shared.exception.ConflictException;
import com.upsjb.ms4.shared.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
public class Ms4ReferenceResolverServiceImpl implements Ms4ReferenceResolverService {

    private static final String CLIENTE = "Cliente snapshot MS2";
    private static final String EMPLEADO = "Empleado snapshot MS2";
    private static final String PRODUCTO = "Producto snapshot MS3";
    private static final String SKU = "SKU snapshot MS3";
    private static final String PRECIO = "Precio snapshot MS3";
    private static final String STOCK = "Stock snapshot MS3";
    private static final String CAJA = "Caja";
    private static final String VENTA = "Venta";
    private static final String PAGO = "Pago";
    private static final String SERIE_BOLETA = "Serie de boleta";
    private static final String EVENTO_PENDIENTE = "Evento pendiente de inventario";
    private static final String SERVICIO_MS3 = "MS3";

    private final ClienteSnapshotMs2Repository clienteRepository;
    private final EmpleadoSnapshotMs2Repository empleadoRepository;
    private final ProductoSnapshotMs3Repository productoRepository;
    private final SkuSnapshotMs3Repository skuRepository;
    private final PrecioSnapshotMs3Repository precioRepository;
    private final PromocionSnapshotMs3Repository promocionRepository;
    private final PromocionSkuDescuentoSnapshotMs3Repository descuentoRepository;
    private final StockSnapshotMs3Repository stockRepository;
    private final CajaRepository cajaRepository;
    private final VentaRepository ventaRepository;
    private final PagoRepository pagoRepository;
    private final SerieBoletaRepository serieBoletaRepository;
    private final ModoContingenciaRepository modoContingenciaRepository;
    private final InventarioEventoPendienteMs4Repository eventoPendienteRepository;
    private final Clock clock;

    public Ms4ReferenceResolverServiceImpl(ClienteSnapshotMs2Repository clienteRepository,
                                           EmpleadoSnapshotMs2Repository empleadoRepository,
                                           ProductoSnapshotMs3Repository productoRepository,
                                           SkuSnapshotMs3Repository skuRepository,
                                           PrecioSnapshotMs3Repository precioRepository,
                                           PromocionSnapshotMs3Repository promocionRepository,
                                           PromocionSkuDescuentoSnapshotMs3Repository descuentoRepository,
                                           StockSnapshotMs3Repository stockRepository,
                                           CajaRepository cajaRepository,
                                           VentaRepository ventaRepository,
                                           PagoRepository pagoRepository,
                                           SerieBoletaRepository serieBoletaRepository,
                                           ModoContingenciaRepository modoContingenciaRepository,
                                           InventarioEventoPendienteMs4Repository eventoPendienteRepository,
                                           Clock clock) {
        this.clienteRepository = clienteRepository;
        this.empleadoRepository = empleadoRepository;
        this.productoRepository = productoRepository;
        this.skuRepository = skuRepository;
        this.precioRepository = precioRepository;
        this.promocionRepository = promocionRepository;
        this.descuentoRepository = descuentoRepository;
        this.stockRepository = stockRepository;
        this.cajaRepository = cajaRepository;
        this.ventaRepository = ventaRepository;
        this.pagoRepository = pagoRepository;
        this.serieBoletaRepository = serieBoletaRepository;
        this.modoContingenciaRepository = modoContingenciaRepository;
        this.eventoPendienteRepository = eventoPendienteRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteSnapshotMs2 resolverClienteActivo(Long idClienteSnapshot,
                                                    Long idClienteMs2,
                                                    Long idUsuarioMs1,
                                                    String numeroDocumento,
                                                    String ruc) {
        validarAlMenosUnCriterio(CLIENTE, idClienteSnapshot, idClienteMs2, idUsuarioMs1, numeroDocumento, ruc);

        ClienteSnapshotMs2 cliente = null;

        if (idClienteSnapshot != null) {
            cliente = clienteRepository.findById(idClienteSnapshot)
                    .orElseThrow(() -> NotFoundException.byId(CLIENTE, idClienteSnapshot));
        } else if (idClienteMs2 != null) {
            cliente = clienteRepository.findByIdClienteMs2AndEstadoTrue(idClienteMs2)
                    .orElseThrow(() -> new NotFoundException("No existe cliente activo con id MS2: " + idClienteMs2));
        } else if (idUsuarioMs1 != null) {
            cliente = resolverClienteActivoPorUsuarioMs1(idUsuarioMs1);
        } else if (hasText(numeroDocumento)) {
            cliente = clienteRepository.findFirstByNumeroDocumentoPersonaAndEstadoTrueOrderByFechaSincronizacionDesc(numeroDocumento.trim())
                    .orElseThrow(() -> new NotFoundException("No existe cliente activo con documento: " + numeroDocumento));
        } else if (hasText(ruc)) {
            cliente = clienteRepository.findFirstByRucAndEstadoTrueOrderByFechaSincronizacionDesc(ruc.trim())
                    .orElseThrow(() -> new NotFoundException("No existe cliente activo con RUC: " + ruc));
        }

        validarClienteActivo(cliente);
        validarConsistenciaCliente(cliente, idClienteMs2, idUsuarioMs1, numeroDocumento, ruc);
        return cliente;
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteSnapshotMs2 resolverClienteActivoPorUsuarioMs1(Long idUsuarioMs1) {
        requirePositive(idUsuarioMs1, "El id de usuario MS1 del cliente");

        ClienteSnapshotMs2 cliente = clienteRepository.findByIdUsuarioMs1AndEstadoTrue(idUsuarioMs1)
                .orElseThrow(() -> new NotFoundException("No existe cliente activo para el usuario MS1: " + idUsuarioMs1));

        validarClienteActivo(cliente);
        return cliente;
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoSnapshotMs2 resolverEmpleadoActivo(Long idEmpleadoSnapshot,
                                                      Long idEmpleadoMs2,
                                                      Long idUsuarioMs1,
                                                      String codigoEmpleado) {
        validarAlMenosUnCriterio(EMPLEADO, idEmpleadoSnapshot, idEmpleadoMs2, idUsuarioMs1, codigoEmpleado);

        EmpleadoSnapshotMs2 empleado;

        if (idEmpleadoSnapshot != null) {
            empleado = empleadoRepository.findById(idEmpleadoSnapshot)
                    .orElseThrow(() -> NotFoundException.byId(EMPLEADO, idEmpleadoSnapshot));
        } else if (idEmpleadoMs2 != null) {
            empleado = empleadoRepository.findByIdEmpleadoMs2AndEstadoTrue(idEmpleadoMs2)
                    .orElseThrow(() -> new NotFoundException("No existe empleado activo con id MS2: " + idEmpleadoMs2));
        } else if (idUsuarioMs1 != null) {
            empleado = resolverEmpleadoActivoPorUsuarioMs1(idUsuarioMs1);
        } else {
            empleado = empleadoRepository.findByCodigoEmpleadoIgnoreCaseAndEstadoTrue(codigoEmpleado.trim())
                    .orElseThrow(() -> new NotFoundException("No existe empleado activo con código: " + codigoEmpleado));
        }

        validarEmpleadoActivo(empleado);
        validarConsistenciaEmpleado(empleado, idEmpleadoMs2, idUsuarioMs1, codigoEmpleado);
        return empleado;
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoSnapshotMs2 resolverEmpleadoActivoPorUsuarioMs1(Long idUsuarioMs1) {
        requirePositive(idUsuarioMs1, "El id de usuario MS1 del empleado");

        EmpleadoSnapshotMs2 empleado = empleadoRepository.findByIdUsuarioMs1AndEstadoTrue(idUsuarioMs1)
                .orElseThrow(() -> new NotFoundException("No existe empleado activo para el usuario MS1: " + idUsuarioMs1));

        validarEmpleadoActivo(empleado);
        return empleado;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoSnapshotMs3 resolverProductoActivo(Long idProductoMs3, String codigoProducto) {
        validarAlMenosUnCriterio(PRODUCTO, idProductoMs3, codigoProducto);

        ProductoSnapshotMs3 producto = idProductoMs3 != null
                ? productoRepository.findByIdProductoMs3AndEstadoTrue(idProductoMs3)
                .orElseThrow(() -> new NotFoundException("No existe producto activo con id MS3: " + idProductoMs3))
                : productoRepository.findByCodigoProductoIgnoreCase(codigoProducto.trim())
                .orElseThrow(() -> new NotFoundException("No existe producto con código: " + codigoProducto));

        validarActivo(producto.getEstado(), PRODUCTO);
        validarSiTextoCoincide(codigoProducto, producto.getCodigoProducto(), "El código de producto no coincide con el producto resuelto.");
        return producto;
    }

    @Override
    @Transactional(readOnly = true)
    public SkuSnapshotMs3 resolverSkuActivo(Long idSkuMs3, String codigoSku, String barcode) {
        validarAlMenosUnCriterio(SKU, idSkuMs3, codigoSku, barcode);

        SkuSnapshotMs3 sku;

        if (idSkuMs3 != null) {
            sku = skuRepository.findByIdSkuMs3AndEstadoTrue(idSkuMs3)
                    .orElseThrow(() -> new NotFoundException("No existe SKU activo con id MS3: " + idSkuMs3));
        } else if (hasText(codigoSku)) {
            sku = skuRepository.findByCodigoSkuIgnoreCase(codigoSku.trim())
                    .orElseThrow(() -> new NotFoundException("No existe SKU con código: " + codigoSku));
        } else {
            sku = skuRepository.findByBarcodeIgnoreCase(barcode.trim())
                    .orElseThrow(() -> new NotFoundException("No existe SKU con barcode: " + barcode));
        }

        validarActivo(sku.getEstado(), SKU);
        validarSiTextoCoincide(codigoSku, sku.getCodigoSku(), "El código SKU no coincide con el SKU resuelto.");
        validarSiTextoCoincide(barcode, sku.getBarcode(), "El barcode no coincide con el SKU resuelto.");
        return sku;
    }

    @Override
    @Transactional(readOnly = true)
    public PrecioSnapshotMs3 resolverPrecioVigentePorSku(Long idSkuMs3, String codigoSku) {
        SkuSnapshotMs3 sku = resolverSkuActivo(idSkuMs3, codigoSku, null);

        return precioRepository.findFirstByIdSkuMs3AndVigenteTrueAndEstadoTrueOrderByFechaInicioDesc(sku.getIdSkuMs3())
                .orElseThrow(() -> new NotFoundException("No existe precio vigente para el SKU: " + sku.getCodigoSku()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PromocionSnapshotMs3> resolverPromocionVigente(Long idPromocionMs3, String codigoPromocion) {
        if (idPromocionMs3 == null && !hasText(codigoPromocion)) {
            return Optional.empty();
        }

        PromocionSnapshotMs3 promocion = idPromocionMs3 != null
                ? promocionRepository.findFirstByIdPromocionMs3AndVigenteTrueAndEstadoTrueOrderByFechaInicioDesc(idPromocionMs3)
                .orElse(null)
                : promocionRepository.findByCodigoPromocionIgnoreCase(codigoPromocion.trim()).orElse(null);

        if (promocion == null || !Boolean.TRUE.equals(promocion.getEstado()) || !Boolean.TRUE.equals(promocion.getVigente())) {
            return Optional.empty();
        }

        validarSiTextoCoincide(codigoPromocion, promocion.getCodigoPromocion(), "El código de promoción no coincide con la promoción resuelta.");
        return Optional.of(promocion);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PromocionSkuDescuentoSnapshotMs3> resolverDescuentoPromocionPreferentePorSku(Long idSkuMs3,
                                                                                                 String codigoSku) {
        SkuSnapshotMs3 sku = resolverSkuActivo(idSkuMs3, codigoSku, null);
        return descuentoRepository.findFirstByIdSkuMs3AndEstadoTrueOrderByPrioridadAsc(sku.getIdSkuMs3());
    }

    @Override
    @Transactional(readOnly = true)
    public StockSnapshotMs3 resolverStockActivo(Long idStockMs3,
                                                Long idSkuMs3,
                                                Long idAlmacenMs3,
                                                String codigoSku,
                                                String codigoAlmacen) {
        if (idStockMs3 != null) {
            StockSnapshotMs3 stock = stockRepository.findByIdStockMs3AndEstadoTrue(idStockMs3)
                    .orElseThrow(() -> new NotFoundException("No existe stock activo con id MS3: " + idStockMs3));

            validarConsistenciaStock(stock, idSkuMs3, idAlmacenMs3, codigoSku, codigoAlmacen);
            return stock;
        }

        SkuSnapshotMs3 sku = resolverSkuActivo(idSkuMs3, codigoSku, null);

        StockSnapshotMs3 stock;

        if (idAlmacenMs3 != null) {
            stock = stockRepository.findByIdSkuMs3AndIdAlmacenMs3AndEstadoTrue(sku.getIdSkuMs3(), idAlmacenMs3)
                    .orElseThrow(() -> new NotFoundException("No existe stock activo para el SKU y almacén indicados."));
        } else if (hasText(codigoAlmacen)) {
            stock = stockRepository.findByIdSkuMs3AndEstadoTrueOrderByStockDisponibleDesc(sku.getIdSkuMs3())
                    .stream()
                    .filter(item -> equalsText(item.getCodigoAlmacen(), codigoAlmacen))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("No existe stock activo para el SKU y código de almacén indicados."));
        } else {
            throw new BusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "Debe indicar almacén por idAlmacenMs3 o código de almacén.",
                    HttpStatus.BAD_REQUEST
            );
        }

        validarActivo(stock.getEstado(), STOCK);
        return stock;
    }

    @Override
    @Transactional(readOnly = true)
    public Caja resolverCajaActiva(Long idCaja, String codigoCaja) {
        validarAlMenosUnCriterio(CAJA, idCaja, codigoCaja);

        Caja caja = idCaja != null
                ? cajaRepository.findById(idCaja).orElseThrow(() -> NotFoundException.byId(CAJA, idCaja))
                : cajaRepository.findByCodigoCajaIgnoreCaseAndEstadoTrue(codigoCaja.trim())
                .orElseThrow(() -> new NotFoundException("No existe caja activa con código: " + codigoCaja));

        validarActivo(caja.getEstado(), CAJA);
        validarSiTextoCoincide(codigoCaja, caja.getCodigoCaja(), "El código de caja no coincide con la caja resuelta.");
        return caja;
    }

    @Override
    @Transactional(readOnly = true)
    public Caja resolverCajaAbiertaActual() {
        LocalDate today = LocalDate.now(clock);

        return cajaRepository.findByFechaOperacionAndEstadoCajaAndEstadoTrue(today, EstadoCaja.ABIERTA)
                .orElseThrow(() -> new NotFoundException("No existe caja abierta para la fecha actual."));
    }

    @Override
    @Transactional(readOnly = true)
    public Venta resolverVentaActiva(Long idVenta, String codigoVenta) {
        validarAlMenosUnCriterio(VENTA, idVenta, codigoVenta);

        Venta venta = idVenta != null
                ? ventaRepository.findByIdAndEstadoTrue(idVenta).orElseThrow(() -> NotFoundException.byId(VENTA, idVenta))
                : ventaRepository.findByCodigoVentaIgnoreCaseAndEstadoTrue(codigoVenta.trim())
                .orElseThrow(() -> new NotFoundException("No existe venta activa con código: " + codigoVenta));

        validarSiTextoCoincide(codigoVenta, venta.getCodigoVenta(), "El código de venta no coincide con la venta resuelta.");
        return venta;
    }

    @Override
    @Transactional(readOnly = true)
    public Pago resolverPagoActivo(Long idPago, String codigoPago, String stripePaymentIntentId) {
        validarAlMenosUnCriterio(PAGO, idPago, codigoPago, stripePaymentIntentId);

        Pago pago;

        if (idPago != null) {
            pago = pagoRepository.findById(idPago).orElseThrow(() -> NotFoundException.byId(PAGO, idPago));
        } else if (hasText(codigoPago)) {
            pago = pagoRepository.findByCodigoPagoIgnoreCaseAndEstadoTrue(codigoPago.trim())
                    .orElseThrow(() -> new NotFoundException("No existe pago activo con código: " + codigoPago));
        } else {
            pago = pagoRepository.findByStripePaymentIntentIdAndEstadoTrue(stripePaymentIntentId.trim())
                    .orElseThrow(() -> new NotFoundException("No existe pago activo para el PaymentIntent indicado."));
        }

        validarActivo(pago.getEstado(), PAGO);
        validarSiTextoCoincide(codigoPago, pago.getCodigoPago(), "El código de pago no coincide con el pago resuelto.");
        validarSiTextoCoincide(stripePaymentIntentId, pago.getStripePaymentIntentId(), "El PaymentIntent no coincide con el pago resuelto.");
        return pago;
    }

    @Override
    @Transactional(readOnly = true)
    public SerieBoleta resolverSerieBoletaActiva(Long idSerie, String serie) {
        validarAlMenosUnCriterio(SERIE_BOLETA, idSerie, serie);

        SerieBoleta entity = idSerie != null
                ? serieBoletaRepository.findById(idSerie).orElseThrow(() -> NotFoundException.byId(SERIE_BOLETA, idSerie))
                : serieBoletaRepository.findBySerieIgnoreCaseAndEstadoTrue(serie.trim())
                .orElseThrow(() -> new NotFoundException("No existe serie de boleta activa: " + serie));

        validarActivo(entity.getEstado(), SERIE_BOLETA);
        validarSiTextoCoincide(serie, entity.getSerie(), "La serie indicada no coincide con la serie resuelta.");
        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public ModoContingencia resolverContingenciaActiva(String servicioAfectado) {
        String servicio = hasText(servicioAfectado)
                ? servicioAfectado.trim().toUpperCase(Locale.ROOT)
                : SERVICIO_MS3;

        return modoContingenciaRepository
                .findFirstByServicioAfectadoAndEstadoContingenciaAndEstadoTrueOrderByFechaInicioDesc(
                        servicio,
                        EstadoContingencia.ACTIVO
                )
                .orElseThrow(() -> new NotFoundException("No existe contingencia activa para el servicio: " + servicio));
    }

    @Override
    @Transactional(readOnly = true)
    public InventarioEventoPendienteMs4 resolverEventoPendienteActivo(Long idEventoPendiente) {
        requirePositive(idEventoPendiente, "El id del evento pendiente");

        InventarioEventoPendienteMs4 evento = eventoPendienteRepository.findById(idEventoPendiente)
                .orElseThrow(() -> NotFoundException.byId(EVENTO_PENDIENTE, idEventoPendiente));

        validarActivo(evento.getEstado(), EVENTO_PENDIENTE);
        return evento;
    }

    private void validarClienteActivo(ClienteSnapshotMs2 cliente) {
        validarActivo(cliente.getEstado(), CLIENTE);

        if (!Boolean.TRUE.equals(cliente.getClienteActivoMs2())) {
            throw new ConflictException("El cliente snapshot no está activo en MS2.");
        }
    }

    private void validarEmpleadoActivo(EmpleadoSnapshotMs2 empleado) {
        validarActivo(empleado.getEstado(), EMPLEADO);

        if (!Boolean.TRUE.equals(empleado.getEmpleadoActivoMs2())) {
            throw new ConflictException("El empleado snapshot no está activo en MS2.");
        }
    }

    private void validarActivo(Boolean estado, String recurso) {
        if (!Boolean.TRUE.equals(estado)) {
            throw new NotFoundException(recurso + " no se encuentra activo.");
        }
    }

    private void validarConsistenciaCliente(ClienteSnapshotMs2 cliente,
                                            Long idClienteMs2,
                                            Long idUsuarioMs1,
                                            String numeroDocumento,
                                            String ruc) {
        if (idClienteMs2 != null && !Objects.equals(cliente.getIdClienteMs2(), idClienteMs2)) {
            throw new ConflictException("El idClienteMs2 no coincide con el cliente resuelto.");
        }

        if (idUsuarioMs1 != null && !Objects.equals(cliente.getIdUsuarioMs1(), idUsuarioMs1)) {
            throw new ConflictException("El idUsuarioMs1 no coincide con el cliente resuelto.");
        }

        validarSiTextoCoincide(numeroDocumento, cliente.getNumeroDocumentoPersona(), "El documento no coincide con el cliente resuelto.");
        validarSiTextoCoincide(ruc, cliente.getRuc(), "El RUC no coincide con el cliente resuelto.");
    }

    private void validarConsistenciaEmpleado(EmpleadoSnapshotMs2 empleado,
                                             Long idEmpleadoMs2,
                                             Long idUsuarioMs1,
                                             String codigoEmpleado) {
        if (idEmpleadoMs2 != null && !Objects.equals(empleado.getIdEmpleadoMs2(), idEmpleadoMs2)) {
            throw new ConflictException("El idEmpleadoMs2 no coincide con el empleado resuelto.");
        }

        if (idUsuarioMs1 != null && !Objects.equals(empleado.getIdUsuarioMs1(), idUsuarioMs1)) {
            throw new ConflictException("El idUsuarioMs1 no coincide con el empleado resuelto.");
        }

        validarSiTextoCoincide(codigoEmpleado, empleado.getCodigoEmpleado(), "El código de empleado no coincide con el empleado resuelto.");
    }

    private void validarConsistenciaStock(StockSnapshotMs3 stock,
                                          Long idSkuMs3,
                                          Long idAlmacenMs3,
                                          String codigoSku,
                                          String codigoAlmacen) {
        if (idSkuMs3 != null && !Objects.equals(stock.getIdSkuMs3(), idSkuMs3)) {
            throw new ConflictException("El idSkuMs3 no coincide con el stock resuelto.");
        }

        if (idAlmacenMs3 != null && !Objects.equals(stock.getIdAlmacenMs3(), idAlmacenMs3)) {
            throw new ConflictException("El idAlmacenMs3 no coincide con el stock resuelto.");
        }

        validarSiTextoCoincide(codigoSku, stock.getCodigoSku(), "El código SKU no coincide con el stock resuelto.");
        validarSiTextoCoincide(codigoAlmacen, stock.getCodigoAlmacen(), "El código de almacén no coincide con el stock resuelto.");
    }

    private void validarSiTextoCoincide(String solicitado, String real, String mensaje) {
        if (!hasText(solicitado)) {
            return;
        }

        if (!equalsText(solicitado, real)) {
            throw new ConflictException(mensaje);
        }
    }

    private void validarAlMenosUnCriterio(String recurso, Object... values) {
        if (values == null) {
            throw criterioInsuficiente(recurso);
        }

        for (Object value : values) {
            if (value instanceof String text && hasText(text)) {
                return;
            }

            if (value instanceof Long number && number > 0) {
                return;
            }
        }

        throw criterioInsuficiente(recurso);
    }

    private BusinessException criterioInsuficiente(String recurso) {
        return new BusinessException(
                ErrorCodes.VALIDATION_ERROR,
                "Debe indicar al menos un criterio válido para resolver " + recurso + ".",
                HttpStatus.BAD_REQUEST
        );
    }

    private void requirePositive(Long value, String field) {
        if (value == null || value <= 0) {
            throw new BusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    field + " debe ser positivo.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private boolean equalsText(String left, String right) {
        if (!hasText(left) || !hasText(right)) {
            return false;
        }

        return left.trim().equalsIgnoreCase(right.trim());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}