// ruta: src/main/java/com/upsjb/ms4/service/impl/boleta/BoletaTemplateModelFactoryImpl.java
package com.upsjb.ms4.service.impl.boleta;

import com.upsjb.ms4.domain.entity.boleta.Boleta;
import com.upsjb.ms4.domain.entity.boleta.BoletaDetalle;
import com.upsjb.ms4.domain.entity.config.BoletaPlantillaVersion;
import com.upsjb.ms4.domain.entity.pago.Pago;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.enums.EstadoPago;
import com.upsjb.ms4.repository.BoletaDetalleRepository;
import com.upsjb.ms4.repository.PagoRepository;
import com.upsjb.ms4.service.contract.boleta.BoletaService;
import com.upsjb.ms4.service.contract.boleta.BoletaTemplateModelFactory;
import com.upsjb.ms4.validator.BoletaValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BoletaTemplateModelFactoryImpl implements BoletaTemplateModelFactory {

    private static final String DEFAULT_TEMPLATE_BOLETA = "boleta/boleta";
    private static final String DEFAULT_TEMPLATE_CORREO = "mail/boleta-compra";
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final BoletaService boletaService;
    private final BoletaDetalleRepository boletaDetalleRepository;
    private final PagoRepository pagoRepository;
    private final BoletaValidator boletaValidator;

    public BoletaTemplateModelFactoryImpl(BoletaService boletaService,
                                          BoletaDetalleRepository boletaDetalleRepository,
                                          PagoRepository pagoRepository,
                                          BoletaValidator boletaValidator) {
        this.boletaService = boletaService;
        this.boletaDetalleRepository = boletaDetalleRepository;
        this.pagoRepository = pagoRepository;
        this.boletaValidator = boletaValidator;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> construirModeloBoleta(Long idBoleta) {
        Boleta boleta = boletaService.resolverBoletaParaRender(idBoleta);
        List<BoletaDetalle> detalles = boletaDetalleRepository.findByIdBoletaAndEstadoTrueOrderByIdAsc(idBoleta);
        return construirModeloBoleta(boleta, detalles);
    }

    @Override
    public Map<String, Object> construirModeloBoleta(Boleta boleta, List<BoletaDetalle> detalles) {
        boletaValidator.validarBoletaParaRender(boleta);

        List<BoletaDetalle> detallesSafe = detalles == null ? List.of() : detalles;
        Venta venta = boleta.getVenta();
        Pago pago = resolverPagoAprobado(boleta.getIdVenta());

        Map<String, Object> model = new LinkedHashMap<>();
        model.put("boleta", boleta);
        model.put("detalles", detallesSafe);
        model.put("venta", venta);
        model.put("pago", pago);
        model.put("emisor", construirEmisor(boleta));
        model.put("cliente", construirCliente(boleta));
        model.put("totales", construirTotales(boleta));
        model.put("meta", construirMeta(boleta, venta, pago));
        model.put("codigoBoleta", boleta.getCodigoBoleta());
        model.put("fechaEmisionTexto", formatearFecha(boleta));
        model.put("templateBoleta", resolverRutaTemplateBoleta(boleta));
        model.put("templateCorreo", resolverRutaTemplateCorreo(boleta));
        return model;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> construirModeloCorreoBoleta(Long idBoleta) {
        Map<String, Object> model = construirModeloBoleta(idBoleta);
        Boleta boleta = (Boleta) model.get("boleta");

        model.put("saludo", "Hola " + resolverNombreCliente(boleta));
        model.put("asunto", "Tu boleta " + boleta.getCodigoBoleta());
        model.put("mensaje", "Adjuntamos tu boleta generada en PDF.");
        return model;
    }

    @Override
    public String resolverRutaTemplateBoleta(Boleta boleta) {
        if (boleta == null) {
            return DEFAULT_TEMPLATE_BOLETA;
        }

        BoletaPlantillaVersion plantilla = boleta.getBoletaPlantillaVersion();
        String ruta = plantilla == null ? null : plantilla.getRutaTemplateHtml();

        return normalizeTemplatePath(ruta, DEFAULT_TEMPLATE_BOLETA);
    }

    @Override
    public String resolverRutaTemplateCorreo(Boleta boleta) {
        if (boleta == null) {
            return DEFAULT_TEMPLATE_CORREO;
        }

        BoletaPlantillaVersion plantilla = boleta.getBoletaPlantillaVersion();
        String ruta = plantilla == null ? null : plantilla.getRutaTemplateMail();

        return normalizeTemplatePath(ruta, DEFAULT_TEMPLATE_CORREO);
    }

    private Pago resolverPagoAprobado(Long idVenta) {
        if (idVenta == null) {
            return null;
        }

        return pagoRepository.findFirstByIdVentaAndEstadoPagoInAndEstadoTrueOrderByCreatedAtDesc(
                idVenta,
                List.of(EstadoPago.APROBADO)
        ).orElse(null);
    }

    private Map<String, Object> construirEmisor(Boleta boleta) {
        Map<String, Object> emisor = new LinkedHashMap<>();
        emisor.put("ruc", boleta.getRucEmisor());
        emisor.put("razonSocial", boleta.getRazonSocialEmisor());
        emisor.put("nombreComercial", boleta.getNombreComercialEmisor());
        emisor.put("direccionFiscal", boleta.getDireccionFiscalEmisor());
        emisor.put("telefono", boleta.getTelefonoEmisor());
        emisor.put("correo", boleta.getCorreoEmisor());
        emisor.put("logoUrl", boleta.getLogoUrlEmisor());
        return emisor;
    }

    private Map<String, Object> construirCliente(Boleta boleta) {
        Map<String, Object> cliente = new LinkedHashMap<>();
        cliente.put("tipoDocumento", boleta.getTipoDocumentoCliente());
        cliente.put("numeroDocumento", boleta.getNumeroDocumentoCliente());
        cliente.put("nombre", boleta.getNombreCliente());
        cliente.put("correo", boleta.getCorreoCliente());
        cliente.put("telefono", boleta.getTelefonoCliente());
        cliente.put("direccion", boleta.getDireccionCliente());
        return cliente;
    }

    private Map<String, Object> construirTotales(Boleta boleta) {
        Map<String, Object> totales = new LinkedHashMap<>();
        totales.put("moneda", boleta.getMoneda());
        totales.put("subtotal", safeMoney(boleta.getSubtotal()));
        totales.put("descuentoTotal", safeMoney(boleta.getDescuentoTotal()));
        totales.put("opGravada", safeMoney(boleta.getOpGravada()));
        totales.put("opExonerada", safeMoney(boleta.getOpExonerada()));
        totales.put("opInafecta", safeMoney(boleta.getOpInafecta()));
        totales.put("igvPorcentaje", boleta.getIgvPorcentaje());
        totales.put("igvTotal", safeMoney(boleta.getIgvTotal()));
        totales.put("total", safeMoney(boleta.getTotal()));
        return totales;
    }

    private Map<String, Object> construirMeta(Boleta boleta, Venta venta, Pago pago) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("codigoBoleta", boleta.getCodigoBoleta());
        meta.put("fechaEmision", boleta.getFechaEmision());
        meta.put("fechaEmisionTexto", formatearFecha(boleta));
        meta.put("versionPlantilla", boleta.getVersionPlantilla());
        meta.put("hashPayload", boleta.getHashPayload());
        meta.put("canalVenta", venta == null ? null : venta.getCanalVenta());
        meta.put("codigoVenta", venta == null ? null : venta.getCodigoVenta());
        meta.put("metodoPago", pago == null ? boleta.getVenta() == null ? null : boleta.getVenta().getMetodoPagoPrincipal() : pago.getMetodoPago());
        return meta;
    }

    private String resolverNombreCliente(Boleta boleta) {
        if (boleta == null || boleta.getNombreCliente() == null || boleta.getNombreCliente().isBlank()) {
            return "cliente";
        }

        return boleta.getNombreCliente().trim();
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String formatearFecha(Boleta boleta) {
        return boleta.getFechaEmision() == null ? null : DATE_TIME_FORMAT.format(boleta.getFechaEmision());
    }

    private String normalizeTemplatePath(String templatePath, String fallback) {
        String value = templatePath == null || templatePath.isBlank() ? fallback : templatePath.trim();

        value = value.replace("\\", "/")
                .replaceAll("/{2,}", "/");

        value = stripPrefix(value, "classpath:/");
        value = stripPrefix(value, "/");
        value = stripPrefix(value, "src/main/resources/");
        value = stripPrefix(value, "templates/");
        value = stripSuffix(value, ".html");

        boletaValidator.validarTemplatePath(value);
        return value;
    }

    private String stripPrefix(String value, String prefix) {
        return value.startsWith(prefix) ? value.substring(prefix.length()) : value;
    }

    private String stripSuffix(String value, String suffix) {
        return value.endsWith(suffix) ? value.substring(0, value.length() - suffix.length()) : value;
    }
}