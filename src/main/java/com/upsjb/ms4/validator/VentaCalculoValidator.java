// ruta: src/main/java/com/upsjb/ms4/validator/VentaCalculoValidator.java
package com.upsjb.ms4.validator;

import com.upsjb.ms4.domain.entity.config.ConfiguracionTributariaVersion;
import com.upsjb.ms4.domain.entity.snapshot.PrecioSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.PromocionSnapshotMs3;
import com.upsjb.ms4.dto.venta.request.VentaCalculoPreviewRequestDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class VentaCalculoValidator extends ValidatorSupport {

    public void validarPreviewRequest(VentaCalculoPreviewRequestDto request) {
        require(request, "La solicitud de previsualización es obligatoria.");

        if (request.detalles() == null || request.detalles().isEmpty()) {
            fail("Debe enviar al menos un producto para calcular la venta.");
        }

        if (request.detalles().size() > 100) {
            fail("La previsualización no puede superar 100 detalles.");
        }

        if (request.moneda() != null && !request.moneda().matches("^[A-Za-z]{3}$")) {
            fail("La moneda debe tener formato ISO de 3 letras, por ejemplo PEN.");
        }
    }

    public void validarTotalesNoNegativos(BigDecimal subtotal,
                                          BigDecimal descuento,
                                          BigDecimal igv,
                                          BigDecimal total) {
        requireNotNegative(subtotal, "El subtotal");
        requireNotNegative(descuento, "El descuento");
        requireNotNegative(igv, "El IGV");
        requirePositive(total, "El total de la venta");

        BigDecimal subtotalMenosDescuento = subtotal.subtract(descuento);

        if (subtotalMenosDescuento.signum() < 0) {
            fail("El descuento no puede superar el subtotal.");
        }
    }

    public void validarPrecioVigente(PrecioSnapshotMs3 precio, LocalDateTime fechaOperacion) {
        require(precio, "El precio vigente es obligatorio.");
        requireActive(precio.getEstado(), "El precio snapshot no está activo.");
        requireActive(precio.getVigente(), "El precio no se encuentra vigente.");
        requirePositive(precio.getPrecioVenta(), "El precio de venta");

        LocalDateTime fecha = fechaOperacion == null ? LocalDateTime.now() : fechaOperacion;

        if (precio.getFechaInicio() != null && fecha.isBefore(precio.getFechaInicio())) {
            fail("El precio todavía no está vigente.");
        }

        if (precio.getFechaFin() != null && fecha.isAfter(precio.getFechaFin())) {
            fail("El precio ya no está vigente.");
        }
    }

    public void validarPromocionVigente(PromocionSnapshotMs3 promocion, LocalDateTime fechaOperacion) {
        if (promocion == null) {
            return;
        }

        requireActive(promocion.getEstado(), "La promoción snapshot no está activa.");
        requireActive(promocion.getVigente(), "La promoción no se encuentra vigente.");

        LocalDateTime fecha = fechaOperacion == null ? LocalDateTime.now() : fechaOperacion;

        if (promocion.getFechaInicio() != null && fecha.isBefore(promocion.getFechaInicio())) {
            fail("La promoción todavía no ha iniciado.");
        }

        if (promocion.getFechaFin() != null && fecha.isAfter(promocion.getFechaFin())) {
            fail("La promoción ya finalizó.");
        }
    }

    public void validarIgvVigente(ConfiguracionTributariaVersion igv, LocalDateTime fechaOperacion) {
        require(igv, "La configuración tributaria vigente es obligatoria.");
        requireActive(igv.getEstado(), "La configuración tributaria no está activa.");
        requireActive(igv.getVigente(), "La configuración tributaria no está vigente.");
        requirePercentRange(igv.getPorcentaje(), "El porcentaje de IGV");

        LocalDateTime fecha = fechaOperacion == null ? LocalDateTime.now() : fechaOperacion;

        if (igv.getFechaInicioVigencia() != null && fecha.isBefore(igv.getFechaInicioVigencia())) {
            fail("La configuración tributaria todavía no está vigente.");
        }

        if (igv.getFechaFinVigencia() != null && fecha.isAfter(igv.getFechaFinVigencia())) {
            fail("La configuración tributaria ya no está vigente.");
        }
    }
}