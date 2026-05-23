// ruta: src/main/java/com/upsjb/ms4/validator/ConfiguracionTributariaValidator.java
package com.upsjb.ms4.validator;

import com.upsjb.ms4.domain.entity.config.ConfiguracionTributariaVersion;
import com.upsjb.ms4.domain.enums.NombreImpuesto;
import com.upsjb.ms4.dto.config.filter.ConfiguracionTributariaFilterDto;
import com.upsjb.ms4.dto.config.request.ConfiguracionTributariaRequestDto;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class ConfiguracionTributariaValidator extends ValidatorSupport {

    public void validarNuevaVersionIgv(ConfiguracionTributariaRequestDto request) {
        require(request, "La configuración tributaria es obligatoria.");

        if (request.nombreImpuesto() != NombreImpuesto.IGV) {
            fail("En esta etapa MS4 solo admite configuración tributaria IGV.");
        }

        validarPorcentaje(request.porcentaje());
        validarMotivo(request.motivo());
        validarRangoVigencia(request.fechaInicioVigencia(), request.fechaFinVigencia());
        validarVigenteConRango(request.vigente(), request.fechaFinVigencia());
    }

    public void validarPorcentaje(BigDecimal porcentaje) {
        requirePositive(porcentaje, "El porcentaje de IGV");
        requirePercentRange(porcentaje, "El porcentaje de IGV");
    }

    public void validarMotivo(String motivo) {
        requireText(motivo, "El motivo del cambio tributario es obligatorio.");
        requireMaxLength(motivo, 500, "El motivo del cambio tributario");
    }

    public void validarSolapamientoVigencias(boolean existeSolapamiento) {
        if (existeSolapamiento) {
            conflict("Existe solapamiento de vigencias tributarias.");
        }
    }

    public void validarRangoVigencia(LocalDateTime inicio, LocalDateTime fin) {
        requireDateRange(inicio, fin, "La vigencia tributaria");
    }

    public void validarFiltro(ConfiguracionTributariaFilterDto filter) {
        if (filter == null) {
            return;
        }

        requireMaxLength(filter.search(), 150, "El texto de búsqueda");
        validarRangoVigencia(filter.fechaInicioDesde(), filter.fechaInicioHasta());
    }

    public void validarActivacion(ConfiguracionTributariaVersion version) {
        require(version, "La versión tributaria es obligatoria.");

        if (version.getNombreImpuesto() != NombreImpuesto.IGV) {
            conflict("Solo puede activarse una versión IGV.");
        }

        requireActive(version.getEstado(), "La versión IGV no está activa.");

        if (Boolean.TRUE.equals(version.getVigente())) {
            conflict("La versión IGV ya se encuentra vigente.");
        }

        validarPorcentaje(version.getPorcentaje());
        validarRangoVigencia(version.getFechaInicioVigencia(), version.getFechaFinVigencia());
        validarVigenteConRango(true, version.getFechaFinVigencia());
    }

    public void validarCambioEstado(ConfiguracionTributariaVersion version, EstadoChangeRequestDto request) {
        require(version, "La versión tributaria es obligatoria.");
        require(request, "La solicitud de cambio de estado es obligatoria.");
        require(request.estado(), "El estado es obligatorio.");
        requireText(request.motivo(), "El motivo es obligatorio para cambiar el estado.");
        requireMaxLength(request.motivo(), 500, "El motivo");

        if (Boolean.FALSE.equals(request.estado()) && Boolean.TRUE.equals(version.getVigente())) {
            conflict("No se puede inactivar la configuración tributaria vigente.");
        }
    }

    private void validarVigenteConRango(Boolean vigente, LocalDateTime fechaFin) {
        require(vigente, "Debe indicar si la versión IGV será vigente.");

        if (Boolean.TRUE.equals(vigente) && fechaFin != null && fechaFin.isBefore(LocalDateTime.now())) {
            fail("Una versión IGV vigente no puede tener fecha fin en el pasado.");
        }
    }
}