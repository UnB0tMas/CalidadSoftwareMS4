// ruta: src/main/java/com/upsjb/ms4/validator/StockDisponibilidadValidator.java
package com.upsjb.ms4.validator;

import com.upsjb.ms4.domain.entity.contingencia.ModoContingencia;
import com.upsjb.ms4.domain.entity.snapshot.StockSnapshotMs3;
import com.upsjb.ms4.domain.enums.EstadoContingencia;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class StockDisponibilidadValidator extends ValidatorSupport {

    public void validarStockDisponible(StockSnapshotMs3 stock, Integer cantidad) {
        require(stock, "El stock snapshot es obligatorio.");
        validarAlmacenActivo(stock);
        requirePositive(cantidad, "La cantidad solicitada");

        if (stock.getStockFisico() == null || stock.getStockFisico() < 0) {
            fail("El stock físico snapshot no puede ser negativo.");
        }

        if (stock.getStockReservado() == null || stock.getStockReservado() < 0) {
            fail("El stock reservado snapshot no puede ser negativo.");
        }

        if (stock.getStockDisponible() == null || stock.getStockDisponible() < 0) {
            fail("El stock disponible snapshot no puede ser negativo.");
        }

        if (stock.getStockDisponible() < cantidad) {
            conflict("Stock insuficiente para el SKU solicitado.");
        }
    }

    public void validarContingenciaSiMs3Caido(boolean ms3Disponible, ModoContingencia contingenciaActual) {
        if (ms3Disponible) {
            return;
        }

        if (contingenciaActual == null) {
            conflict("MS3 no está disponible y no existe contingencia activa autorizada para vender.");
        }

        if (contingenciaActual.getEstadoContingencia() != EstadoContingencia.ACTIVO) {
            conflict("MS3 no está disponible y la contingencia no está activa.");
        }

        if (!Boolean.TRUE.equals(contingenciaActual.getVentasPermitidas())) {
            conflict("La contingencia activa no permite ventas.");
        }

        if (!Boolean.TRUE.equals(contingenciaActual.getGuardarEventosPendientes())) {
            conflict("La contingencia activa no permite guardar eventos pendientes de inventario.");
        }
    }

    public void validarAlmacenActivo(StockSnapshotMs3 stock) {
        require(stock, "El stock snapshot es obligatorio.");
        requireActive(stock.getEstado(), "El almacén/stock no está activo para venta.");
        requirePositive(stock.getIdAlmacenMs3(), "El almacén MS3");
        requireText(stock.getCodigoAlmacen(), "El código de almacén es obligatorio.");
        requireText(stock.getNombreAlmacen(), "El nombre de almacén es obligatorio.");
    }

    public void validarStockSnapshotRecienteSegunPolitica(StockSnapshotMs3 stock,
                                                          Duration maxAntiguedad,
                                                          LocalDateTime now) {
        require(stock, "El stock snapshot es obligatorio.");

        if (maxAntiguedad == null) {
            return;
        }

        if (maxAntiguedad.isZero() || maxAntiguedad.isNegative()) {
            fail("La antigüedad máxima del snapshot de stock debe ser positiva.");
        }

        LocalDateTime referencia = now == null ? LocalDateTime.now() : now;

        if (stock.getFechaSincronizacion() == null) {
            fail("El stock snapshot no tiene fecha de sincronización.");
        }

        if (stock.getFechaSincronizacion().isBefore(referencia.minus(maxAntiguedad))) {
            conflict("El stock snapshot está desactualizado según la política vigente.");
        }
    }
}