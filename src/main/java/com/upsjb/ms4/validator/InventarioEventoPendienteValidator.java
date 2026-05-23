// ruta: src/main/java/com/upsjb/ms4/validator/InventarioEventoPendienteValidator.java
package com.upsjb.ms4.validator;

import com.upsjb.ms4.domain.entity.kafka.InventarioEventoPendienteMs4;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.entity.venta.VentaDetalle;
import com.upsjb.ms4.domain.enums.EstadoSincronizacionInventario;
import com.upsjb.ms4.domain.enums.TipoComandoStock;
import com.upsjb.ms4.dto.contingencia.filter.InventarioEventoPendienteFilterDto;
import org.springframework.stereotype.Component;

@Component
public class InventarioEventoPendienteValidator extends ValidatorSupport {

    public void validarRegistroPendiente(Venta venta,
                                         VentaDetalle detalle,
                                         TipoComandoStock tipoComando,
                                         String payloadJson) {
        require(venta, "La venta es obligatoria para registrar el evento pendiente.");
        requirePositive(venta.getId(), "La venta");
        requireText(venta.getCodigoVenta(), "El código de venta es obligatorio.");

        require(detalle, "El detalle de venta es obligatorio para registrar el evento pendiente.");
        requirePositive(detalle.getIdSkuMs3(), "El SKU MS3");
        requirePositive(detalle.getIdAlmacenMs3(), "El almacén MS3");
        requireText(detalle.getCodigoSku(), "El código SKU es obligatorio.");
        requireText(detalle.getCodigoAlmacen(), "El código de almacén es obligatorio.");
        requirePositive(detalle.getCantidad(), "La cantidad");

        require(tipoComando, "El tipo de comando de stock es obligatorio.");
        requireJson(payloadJson, "El payloadJson");
    }

    public void validarFiltro(InventarioEventoPendienteFilterDto filter) {
        if (filter == null) {
            return;
        }

        requireMaxLength(filter.search(), 150, "El texto de búsqueda");
        requireMaxLength(filter.codigoVenta(), 80, "El código de venta");
        requireMaxLength(filter.topicDestino(), 180, "El topic destino");
        requireMaxLength(filter.idempotencyKey(), 180, "La clave de idempotencia");

        if (filter.idVenta() != null && filter.idVenta() <= 0) {
            fail("El id de venta debe ser positivo.");
        }

        if (filter.idVentaDetalle() != null && filter.idVentaDetalle() <= 0) {
            fail("El id de detalle de venta debe ser positivo.");
        }

        requireDateRange(filter.fechaCreacionDesde(), filter.fechaCreacionHasta(), "El rango de creación");
        requireDateRange(filter.fechaSincronizacionDesde(), filter.fechaSincronizacionHasta(), "El rango de sincronización");
    }

    public void validarMarcarSincronizado(InventarioEventoPendienteMs4 evento) {
        require(evento, "El evento pendiente de inventario es obligatorio.");

        if (evento.getEstadoSincronizacion() == null) {
            fail("El evento pendiente no tiene estado de sincronización.");
        }
    }

    public void validarMarcarError(InventarioEventoPendienteMs4 evento, String errorDetalle) {
        require(evento, "El evento pendiente de inventario es obligatorio.");
        requireText(errorDetalle, "El detalle de error es obligatorio.");
        requireMaxLength(errorDetalle, 4000, "El detalle de error");

        if (evento.getEstadoSincronizacion() == EstadoSincronizacionInventario.SINCRONIZADO) {
            conflict("No se puede marcar con error un evento ya sincronizado.");
        }
    }

    public void validarReintento(InventarioEventoPendienteMs4 evento) {
        require(evento, "El evento pendiente de inventario es obligatorio.");

        if (evento.getEstadoSincronizacion() == EstadoSincronizacionInventario.SINCRONIZADO) {
            conflict("No se puede reintentar un evento ya sincronizado.");
        }

        if (evento.getEstadoSincronizacion() == EstadoSincronizacionInventario.ENVIADO) {
            conflict("No se puede reintentar un evento ya enviado mientras se espera confirmación.");
        }
    }
}