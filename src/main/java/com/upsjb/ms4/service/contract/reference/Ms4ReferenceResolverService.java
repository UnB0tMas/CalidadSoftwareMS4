// ruta: src/main/java/com/upsjb/ms4/service/contract/reference/Ms4ReferenceResolverService.java
package com.upsjb.ms4.service.contract.reference;

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

import java.util.Optional;

public interface Ms4ReferenceResolverService {

    ClienteSnapshotMs2 resolverClienteActivo(Long idClienteSnapshot,
                                             Long idClienteMs2,
                                             Long idUsuarioMs1,
                                             String numeroDocumento,
                                             String ruc);

    ClienteSnapshotMs2 resolverClienteActivoPorUsuarioMs1(Long idUsuarioMs1);

    EmpleadoSnapshotMs2 resolverEmpleadoActivo(Long idEmpleadoSnapshot,
                                               Long idEmpleadoMs2,
                                               Long idUsuarioMs1,
                                               String codigoEmpleado);

    EmpleadoSnapshotMs2 resolverEmpleadoActivoPorUsuarioMs1(Long idUsuarioMs1);

    ProductoSnapshotMs3 resolverProductoActivo(Long idProductoMs3, String codigoProducto);

    SkuSnapshotMs3 resolverSkuActivo(Long idSkuMs3, String codigoSku, String barcode);

    PrecioSnapshotMs3 resolverPrecioVigentePorSku(Long idSkuMs3, String codigoSku);

    Optional<PromocionSnapshotMs3> resolverPromocionVigente(Long idPromocionMs3, String codigoPromocion);

    Optional<PromocionSkuDescuentoSnapshotMs3> resolverDescuentoPromocionPreferentePorSku(Long idSkuMs3,
                                                                                          String codigoSku);

    StockSnapshotMs3 resolverStockActivo(Long idStockMs3,
                                         Long idSkuMs3,
                                         Long idAlmacenMs3,
                                         String codigoSku,
                                         String codigoAlmacen);

    Caja resolverCajaActiva(Long idCaja, String codigoCaja);

    Caja resolverCajaAbiertaActual();

    Venta resolverVentaActiva(Long idVenta, String codigoVenta);

    Pago resolverPagoActivo(Long idPago, String codigoPago, String stripePaymentIntentId);

    SerieBoleta resolverSerieBoletaActiva(Long idSerie, String serie);

    ModoContingencia resolverContingenciaActiva(String servicioAfectado);

    InventarioEventoPendienteMs4 resolverEventoPendienteActivo(Long idEventoPendiente);
}