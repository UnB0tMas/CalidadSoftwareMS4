// ruta: src/main/java/com/upsjb/ms4/validator/BoletaPlantillaValidator.java
package com.upsjb.ms4.validator;

import com.upsjb.ms4.domain.entity.config.BoletaPlantillaVersion;
import com.upsjb.ms4.dto.config.filter.BoletaPlantillaFilterDto;
import com.upsjb.ms4.dto.config.request.BoletaPlantillaRequestDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class BoletaPlantillaValidator extends ValidatorSupport {

    public void validarNuevaVersion(BoletaPlantillaRequestDto request) {
        require(request, "La solicitud de plantilla de boleta es obligatoria.");
        requireText(request.nombre(), "El nombre de la plantilla es obligatorio.");
        requireText(request.rutaTemplateHtml(), "La ruta del template HTML es obligatoria.");
        requireText(request.rutaTemplateMail(), "La ruta del template de correo es obligatoria.");
        require(request.vigente(), "Debe indicar si la plantilla será vigente.");
        requireText(request.motivo(), "El motivo es obligatorio.");

        requireMaxLength(request.nombre(), 150, "El nombre");
        requireMaxLength(request.rutaTemplateHtml(), 255, "La ruta del template HTML");
        requireMaxLength(request.rutaTemplateMail(), 255, "La ruta del template de correo");
        requireMaxLength(request.descripcion(), 500, "La descripción");
        requireMaxLength(request.motivo(), 500, "El motivo");

        requireDateRange(request.fechaInicioVigencia(), request.fechaFinVigencia(), "La vigencia de plantilla");
        validarVigenteConRango(request.vigente(), request.fechaFinVigencia());
        validarRutaTemplate(request.rutaTemplateHtml(), "boleta/", "La ruta del template HTML");
        validarRutaTemplate(request.rutaTemplateMail(), "mail/", "La ruta del template de correo");
    }

    public void validarActivacion(BoletaPlantillaVersion version) {
        require(version, "La plantilla de boleta es obligatoria.");
        requireActive(version.getEstado(), "La plantilla de boleta no está activa.");

        if (Boolean.TRUE.equals(version.getVigente())) {
            conflict("La plantilla de boleta ya se encuentra vigente.");
        }

        requireText(version.getRutaTemplateHtml(), "La ruta del template HTML es obligatoria.");
        requireText(version.getRutaTemplateMail(), "La ruta del template de correo es obligatoria.");
        requireDateRange(version.getFechaInicioVigencia(), version.getFechaFinVigencia(), "La vigencia de plantilla");
        validarVigenteConRango(true, version.getFechaFinVigencia());
        validarRutaTemplate(version.getRutaTemplateHtml(), "boleta/", "La ruta del template HTML");
        validarRutaTemplate(version.getRutaTemplateMail(), "mail/", "La ruta del template de correo");
    }

    public void validarFiltro(BoletaPlantillaFilterDto filter) {
        if (filter == null) {
            return;
        }

        requireMaxLength(filter.search(), 150, "El texto de búsqueda");
        requireMaxLength(filter.codigoVersion(), 50, "El código de versión");
        requireDateRange(filter.fechaInicioDesde(), filter.fechaInicioHasta(), "El rango de vigencia");
    }

    private void validarVigenteConRango(Boolean vigente, LocalDateTime fechaFin) {
        if (Boolean.TRUE.equals(vigente) && fechaFin != null && fechaFin.isBefore(LocalDateTime.now())) {
            fail("Una plantilla vigente no puede tener fecha fin en el pasado.");
        }
    }

    private void validarRutaTemplate(String value, String expectedPrefix, String fieldName) {
        String normalized = value == null ? null : value.trim().replace("\\", "/");

        requireText(normalized, fieldName + " es obligatoria.");

        if (normalized.contains("..")) {
            fail(fieldName + " no puede contener rutas relativas.");
        }

        if (normalized.startsWith("/") || normalized.startsWith("http://") || normalized.startsWith("https://")) {
            fail(fieldName + " debe ser una ruta interna de templates.");
        }

        if (!normalized.startsWith(expectedPrefix)) {
            fail(fieldName + " debe iniciar con " + expectedPrefix);
        }
    }
}