// ruta: src/main/java/com/upsjb/ms4/validator/ContingenciaValidator.java
package com.upsjb.ms4.validator;

import com.upsjb.ms4.domain.entity.contingencia.ModoContingencia;
import com.upsjb.ms4.domain.entity.kafka.InventarioEventoPendienteMs4;
import com.upsjb.ms4.domain.enums.EstadoContingencia;
import com.upsjb.ms4.domain.enums.EstadoSincronizacionInventario;
import com.upsjb.ms4.dto.contingencia.filter.InventarioEventoPendienteFilterDto;
import com.upsjb.ms4.dto.contingencia.filter.ModoContingenciaFilterDto;
import com.upsjb.ms4.dto.contingencia.request.ContingenciaActivarRequestDto;
import com.upsjb.ms4.dto.contingencia.request.ContingenciaFinalizarRequestDto;
import org.springframework.stereotype.Component;

@Component
public class ContingenciaValidator extends ValidatorSupport {

    public void validarActivacion(ContingenciaActivarRequestDto request, ModoContingencia contingenciaActiva) {
        require(request, "La solicitud de activación de contingencia es obligatoria.");

        if (contingenciaActiva != null && contingenciaActiva.getEstadoContingencia() == EstadoContingencia.ACTIVO) {
            conflict("Ya existe una contingencia activa para el servicio afectado.");
        }

        requireText(request.servicioAfectado(), "El servicio afectado es obligatorio.");
        requireText(request.motivo(), "El motivo de contingencia es obligatorio.");
        require(request.ventasPermitidas(), "Debe indicar si las ventas estarán permitidas.");
        require(request.guardarEventosPendientes(), "Debe indicar si se guardarán eventos pendientes.");
        validarGuardarEventosPendientes(request.ventasPermitidas(), request.guardarEventosPendientes());
        requireMaxLength(request.servicioAfectado(), 40, "El servicio afectado");
        requireMaxLength(request.motivo(), 500, "El motivo de contingencia");
        requireMaxLength(request.observacion(), 500, "La observación de contingencia");
    }

    public void validarFinalizacion(ContingenciaFinalizarRequestDto request, ModoContingencia contingenciaActual) {
        require(request, "La solicitud de finalización es obligatoria.");
        require(contingenciaActual, "No existe contingencia actual para finalizar.");

        if (contingenciaActual.getEstadoContingencia() != EstadoContingencia.ACTIVO) {
            conflict("Solo una contingencia activa puede finalizarse.");
        }

        requireText(request.motivo(), "El motivo de finalización es obligatorio.");
        requireMaxLength(request.motivo(), 500, "El motivo de finalización");
        requireMaxLength(request.observacion(), 500, "La observación de finalización");
    }

    public void validarGuardarEventosPendientes(Boolean ventasPermitidas, Boolean guardarEventosPendientes) {
        if (Boolean.TRUE.equals(ventasPermitidas) && !Boolean.TRUE.equals(guardarEventosPendientes)) {
            fail("Si se permiten ventas en contingencia, deben guardarse eventos pendientes.");
        }
    }

    public void validarReconciliacion(ModoContingencia contingenciaActual, long eventosPendientes) {
        if (contingenciaActual != null && contingenciaActual.getEstadoContingencia() == EstadoContingencia.ACTIVO) {
            conflict("No se puede reconciliar mientras la contingencia sigue activa.");
        }

        if (eventosPendientes <= 0) {
            conflict("No existen eventos pendientes para reconciliar.");
        }
    }

    public void validarFiltroContingencia(ModoContingenciaFilterDto filter) {
        if (filter == null) {
            return;
        }

        requireMaxLength(filter.search(), 150, "El texto de búsqueda");
        requireMaxLength(filter.servicioAfectado(), 40, "El servicio afectado");
        requireDateRange(filter.fechaInicioDesde(), filter.fechaInicioHasta(), "El rango de inicio de contingencia");
        requireDateRange(filter.fechaFinDesde(), filter.fechaFinHasta(), "El rango de fin de contingencia");
    }

    public void validarFiltroEventoPendiente(InventarioEventoPendienteFilterDto filter) {
        if (filter == null) {
            return;
        }

        requireMaxLength(filter.search(), 150, "El texto de búsqueda");
        requireMaxLength(filter.codigoVenta(), 80, "El código de venta");
        requireMaxLength(filter.topicDestino(), 180, "El topic destino");
        requireMaxLength(filter.idempotencyKey(), 180, "La clave de idempotencia");
        requireDateRange(filter.fechaCreacionDesde(), filter.fechaCreacionHasta(), "El rango de creación");
        requireDateRange(filter.fechaSincronizacionDesde(), filter.fechaSincronizacionHasta(), "El rango de sincronización");

        if (filter.idVenta() != null && filter.idVenta() <= 0) {
            fail("El id de venta debe ser positivo.");
        }

        if (filter.idVentaDetalle() != null && filter.idVentaDetalle() <= 0) {
            fail("El id de detalle de venta debe ser positivo.");
        }
    }

    public void validarMarcadoError(String error) {
        requireText(error, "El detalle del error de sincronización es obligatorio.");
        requireMaxLength(error, 2000, "El detalle del error de sincronización");
    }

    public void validarEventoSincronizable(InventarioEventoPendienteMs4 evento) {
        require(evento, "El evento pendiente de inventario es obligatorio.");

        if (evento.getEstadoSincronizacion() == EstadoSincronizacionInventario.SINCRONIZADO) {
            return;
        }

        if (evento.getEstadoSincronizacion() == EstadoSincronizacionInventario.ENVIADO
                || evento.getEstadoSincronizacion() == EstadoSincronizacionInventario.PENDIENTE
                || evento.getEstadoSincronizacion() == EstadoSincronizacionInventario.ERROR
                || evento.getEstadoSincronizacion() == EstadoSincronizacionInventario.REQUIERE_REVISION) {
            return;
        }

        conflict("El evento pendiente no puede marcarse como sincronizado desde su estado actual.");
    }

    public void validarEventoMarcableConError(InventarioEventoPendienteMs4 evento) {
        require(evento, "El evento pendiente de inventario es obligatorio.");

        if (evento.getEstadoSincronizacion() == EstadoSincronizacionInventario.SINCRONIZADO) {
            conflict("No se puede marcar con error un evento de inventario ya sincronizado.");
        }
    }
}