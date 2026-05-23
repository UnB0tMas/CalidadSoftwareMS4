// ruta: src/main/java/com/upsjb/ms4/validator/BoletaValidator.java
package com.upsjb.ms4.validator;

import com.upsjb.ms4.domain.entity.boleta.Boleta;
import com.upsjb.ms4.domain.entity.config.SerieBoleta;
import com.upsjb.ms4.domain.entity.mail.CorreoOutbox;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.entity.venta.VentaDetalle;
import com.upsjb.ms4.domain.enums.EstadoVenta;
import com.upsjb.ms4.domain.enums.TipoCorreo;
import com.upsjb.ms4.dto.boleta.filter.BoletaFilterDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BoletaValidator extends ValidatorSupport {

    public void validarIdBoleta(Long idBoleta) {
        requirePositive(idBoleta, "La boleta");
    }

    public void validarVentaConfirmada(Venta venta) {
        require(venta, "La venta es obligatoria.");

        if (venta.getEstadoVenta() != EstadoVenta.CONFIRMADA) {
            fail("Solo una venta confirmada puede generar boleta.");
        }

        requirePositive(venta.getTotal(), "El total de la venta");
    }

    public void validarBoletaNoDuplicadaPorVenta(Boleta boletaExistente) {
        if (boletaExistente != null) {
            conflict("La venta ya tiene una boleta emitida.");
        }
    }

    public void validarDetallesVentaParaBoleta(List<VentaDetalle> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            fail("La venta debe tener al menos un detalle para emitir boleta.");
        }

        for (VentaDetalle detalle : detalles) {
            require(detalle, "El detalle de venta es obligatorio.");
            requirePositive(detalle.getCantidad(), "La cantidad del detalle");
            requirePositive(detalle.getIdProductoMs3(), "El producto MS3 del detalle");
            requirePositive(detalle.getIdSkuMs3(), "El SKU MS3 del detalle");
            requireText(detalle.getCodigoProducto(), "El código de producto del detalle es obligatorio.");
            requireText(detalle.getCodigoSku(), "El código SKU del detalle es obligatorio.");
            requireText(detalle.getNombreProducto(), "El nombre de producto del detalle es obligatorio.");
            requirePositive(detalle.getPrecioUnitarioFinal(), "El precio unitario final del detalle");
            requirePositive(detalle.getTotalLinea(), "El total de línea del detalle");
            requireJson(detalle.getPayloadProductoSnapshotJson(), "El payload de producto snapshot");
            requireJson(detalle.getPayloadSkuSnapshotJson(), "El payload de SKU snapshot");
            requireJson(detalle.getPayloadPrecioSnapshotJson(), "El payload de precio snapshot");
        }
    }

    public void validarSerieDisponible(SerieBoleta serieBoleta) {
        require(serieBoleta, "La serie de boleta es obligatoria.");
        requireActive(serieBoleta.getEstado(), "La serie de boleta no está activa.");

        if (serieBoleta.getSerie() == null || !serieBoleta.getSerie().matches("^B\\d{3}$")) {
            fail("La serie debe tener formato B001.");
        }

        if (serieBoleta.getNumeroActual() == null || serieBoleta.getNumeroInicio() == null) {
            fail("La serie de boleta tiene numeración inválida.");
        }

        if (serieBoleta.getNumeroActual() < serieBoleta.getNumeroInicio() - 1) {
            fail("El número actual de la serie no puede ser menor al número inicial menos uno.");
        }

        if (serieBoleta.getNumeroFin() != null && serieBoleta.getNumeroActual() >= serieBoleta.getNumeroFin()) {
            conflict("La serie de boleta agotó su rango numérico.");
        }
    }

    public void validarCorreoCliente(String correoCliente) {
        requireEmail(correoCliente, "El correo del cliente");
    }

    public void validarTipoCorreoBoleta(TipoCorreo tipoCorreo) {
        require(tipoCorreo, "El tipo de correo de boleta es obligatorio.");

        if (!tipoCorreo.esBoleta()) {
            fail("El tipo de correo indicado no corresponde a una boleta.");
        }
    }

    public void validarFiltro(BoletaFilterDto filter) {
        if (filter == null) {
            return;
        }

        requireMaxLength(filter.search(), 150, "El texto de búsqueda");
        requireMaxLength(filter.serie(), 20, "La serie");
        requireMaxLength(filter.codigoBoleta(), 80, "El código de boleta");
        requireMaxLength(filter.numeroDocumentoCliente(), 30, "El número de documento del cliente");
        requireMaxLength(filter.correoCliente(), 180, "El correo del cliente");
        requireDateRange(filter.fechaDesde(), filter.fechaHasta(), "El rango de emisión de boleta");

        if (filter.idVenta() != null && filter.idVenta() <= 0) {
            fail("El id de venta debe ser positivo.");
        }

        if (filter.numero() != null && filter.numero() <= 0) {
            fail("El número de boleta debe ser positivo.");
        }
    }

    public void validarBoletaParaRender(Boleta boleta) {
        require(boleta, "La boleta es obligatoria.");
        requirePositive(boleta.getId(), "La boleta");
        requireText(boleta.getCodigoBoleta(), "El código de boleta es obligatorio.");
        requireText(boleta.getPayloadJson(), "El payload de la boleta es obligatorio.");
        requireText(boleta.getVersionPlantilla(), "La versión de plantilla es obligatoria.");
    }

    public void validarTemplatePath(String templatePath) {
        requireText(templatePath, "La ruta de plantilla es obligatoria.");
        requireMaxLength(templatePath, 255, "La ruta de plantilla");

        String value = templatePath.trim();

        if (value.contains("..") || value.contains("\r") || value.contains("\n")) {
            fail("La ruta de plantilla no es válida.");
        }
    }

    public void validarHtmlParaPdf(String html) {
        requireText(html, "El HTML de la boleta es obligatorio para generar PDF.");
    }

    public void validarObservacionReenvio(String observacion) {
        requireMaxLength(observacion, 500, "La observación de reenvío");
    }

    public void validarCorreoOutboxBoleta(CorreoOutbox correoOutbox) {
        require(correoOutbox, "El correo outbox es obligatorio.");
        validarTipoCorreoBoleta(correoOutbox.getTipoCorreo());
        requirePositive(correoOutbox.getIdBoleta(), "La boleta del correo");
        validarCorreoCliente(correoOutbox.getDestinatarioEmail());
        requireText(correoOutbox.getAsunto(), "El asunto del correo es obligatorio.");
    }
}