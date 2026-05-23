// ruta: src/main/java/com/upsjb/ms4/validator/CajaValidator.java
package com.upsjb.ms4.validator;

import com.upsjb.ms4.domain.entity.caja.Caja;
import com.upsjb.ms4.domain.entity.pago.Pago;
import com.upsjb.ms4.domain.entity.snapshot.EmpleadoSnapshotMs2;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.enums.EstadoCaja;
import com.upsjb.ms4.domain.enums.EstadoPago;
import com.upsjb.ms4.domain.enums.EstadoVenta;
import com.upsjb.ms4.domain.enums.MetodoPago;
import com.upsjb.ms4.dto.caja.filter.CajaFilterDto;
import com.upsjb.ms4.dto.caja.filter.CajaMovimientoFilterDto;
import com.upsjb.ms4.dto.caja.request.CajaAjusteRequestDto;
import com.upsjb.ms4.dto.caja.request.CajaAperturaRequestDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;

@Component
public class CajaValidator extends ValidatorSupport {

    public void validarIdCaja(Long idCaja) {
        requirePositive(idCaja, "La caja");
    }

    public void validarApertura(CajaAperturaRequestDto request) {
        require(request, "La solicitud de apertura de caja es obligatoria.");
        requireNotNegative(request.montoInicial(), "El monto inicial");
        requireMaxLength(request.observacionApertura(), 500, "La observación de apertura");
    }

    public void validarEmpleadoActivoParaCaja(EmpleadoSnapshotMs2 empleado) {
        require(empleado, "El empleado autenticado no existe en el snapshot local de MS2.");
        requireActive(empleado.getEstado(), "El empleado no está activo en MS4.");

        if (!Boolean.TRUE.equals(empleado.getEmpleadoActivoMs2())) {
            fail("El empleado no está activo en MS2.");
        }

        requirePositive(empleado.getIdEmpleadoMs2(), "El empleado MS2");
        requirePositive(empleado.getIdUsuarioMs1(), "El usuario MS1 del empleado");
    }

    public void validarCajaNoDuplicadaPorDia(Caja cajaAbiertaExistente, LocalDate fechaOperacion) {
        require(fechaOperacion, "La fecha de operación es obligatoria.");

        if (cajaAbiertaExistente != null && cajaAbiertaExistente.getEstadoCaja() == EstadoCaja.ABIERTA) {
            conflict("Ya existe una caja abierta para el día de operación.");
        }
    }

    public void validarCajaAbiertaParaVenta(Caja caja) {
        require(caja, "La caja es obligatoria para una venta física.");
        requireActive(caja.getEstado(), "La caja no está activa.");

        if (caja.getEstadoCaja() != EstadoCaja.ABIERTA) {
            conflict("La caja no se encuentra abierta.");
        }
    }

    public void validarCajaActiva(Caja caja) {
        require(caja, "La caja es obligatoria.");
        requireActive(caja.getEstado(), "La caja no está activa.");
    }

    public void validarAjuste(Caja caja, CajaAjusteRequestDto request) {
        validarCajaAbiertaParaVenta(caja);
        require(request, "La solicitud de ajuste es obligatoria.");
        requirePositive(request.monto(), "El monto del ajuste");
        requireText(request.descripcion(), "La descripción del ajuste es obligatoria.");
        requireMaxLength(request.descripcion(), 500, "La descripción del ajuste");
    }

    public void validarMovimientoApertura(Caja caja) {
        validarCajaActiva(caja);

        if (caja.getEstadoCaja() != EstadoCaja.ABIERTA) {
            conflict("El movimiento de apertura solo puede registrarse sobre una caja abierta.");
        }

        requireNotNegative(caja.getMontoInicial(), "El monto inicial");
    }

    public void validarMovimientoCierre(Caja caja) {
        validarCajaActiva(caja);

        if (caja.getEstadoCaja() != EstadoCaja.CERRADA) {
            conflict("El movimiento de cierre solo puede registrarse sobre una caja cerrada.");
        }

        requireNotNegative(caja.getMontoRealEfectivo(), "El monto real en efectivo");
    }

    public void validarMovimientoVenta(Caja caja, Venta venta, Pago pago, MetodoPago metodoEsperado) {
        validarCajaAbiertaParaVenta(caja);
        require(venta, "La venta es obligatoria para registrar movimiento de caja.");
        require(pago, "El pago es obligatorio para registrar movimiento de caja.");
        require(metodoEsperado, "El método de pago esperado es obligatorio.");

        requirePositive(venta.getId(), "La venta");
        requirePositive(pago.getId(), "El pago");
        requirePositive(venta.getIdCaja(), "La caja asociada a la venta");

        if (!Objects.equals(caja.getId(), venta.getIdCaja())) {
            fail("La venta no pertenece a la caja indicada.");
        }

        if (venta.getEstadoVenta() != EstadoVenta.CONFIRMADA) {
            conflict("Solo una venta confirmada puede registrar movimiento de caja.");
        }

        if (pago.getEstadoPago() != EstadoPago.APROBADO) {
            conflict("Solo un pago aprobado puede registrar movimiento de caja.");
        }

        if (pago.getMetodoPago() != metodoEsperado) {
            conflict("El método de pago no corresponde al tipo de movimiento de caja.");
        }

        if (!Objects.equals(venta.getId(), pago.getIdVenta())) {
            fail("El pago no pertenece a la venta indicada.");
        }

        requirePositive(pago.getMonto(), "El monto del pago");
    }

    public void validarFiltro(CajaFilterDto filter) {
        if (filter == null) {
            return;
        }

        requireMaxLength(filter.search(), 150, "El texto de búsqueda");
        requireMaxLength(filter.codigoCaja(), 80, "El código de caja");
        validarRangoFecha(filter.fechaDesde(), filter.fechaHasta(), "El rango de fecha de operación");

        if (filter.idEmpleadoAperturaSnapshot() != null && filter.idEmpleadoAperturaSnapshot() <= 0) {
            fail("El empleado de apertura debe ser positivo.");
        }

        if (filter.idEmpleadoCierreSnapshot() != null && filter.idEmpleadoCierreSnapshot() <= 0) {
            fail("El empleado de cierre debe ser positivo.");
        }
    }

    public void validarFiltroMovimiento(Long idCaja, CajaMovimientoFilterDto filter) {
        requirePositive(idCaja, "La caja");

        if (filter == null) {
            return;
        }

        if (filter.idCaja() != null && !Objects.equals(filter.idCaja(), idCaja)) {
            fail("El filtro idCaja no coincide con la caja solicitada.");
        }

        requireMaxLength(filter.search(), 150, "El texto de búsqueda");
        requireMaxLength(filter.actorRol(), 40, "El rol del actor");
        requireDateRange(filter.fechaDesde(), filter.fechaHasta(), "El rango de fecha de movimiento");

        if (filter.idVenta() != null && filter.idVenta() <= 0) {
            fail("La venta debe ser un identificador positivo.");
        }

        if (filter.idPago() != null && filter.idPago() <= 0) {
            fail("El pago debe ser un identificador positivo.");
        }

        if (filter.actorIdUsuarioMs1() != null && filter.actorIdUsuarioMs1() <= 0) {
            fail("El actor debe ser un identificador positivo.");
        }
    }

    private void validarRangoFecha(LocalDate inicio, LocalDate fin, String fieldName) {
        if (inicio != null && fin != null && fin.isBefore(inicio)) {
            fail(fieldName + " no puede tener fecha fin anterior a fecha inicio.");
        }
    }
}