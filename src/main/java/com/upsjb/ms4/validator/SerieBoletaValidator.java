// ruta: src/main/java/com/upsjb/ms4/validator/SerieBoletaValidator.java
package com.upsjb.ms4.validator;

import com.upsjb.ms4.domain.entity.config.SerieBoleta;
import com.upsjb.ms4.dto.config.filter.SerieBoletaFilterDto;
import com.upsjb.ms4.dto.config.request.SerieBoletaCreateRequestDto;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import org.springframework.stereotype.Component;

@Component
public class SerieBoletaValidator extends ValidatorSupport {

    public void validarCrearSerie(SerieBoletaCreateRequestDto request, SerieBoleta existente) {
        require(request, "La solicitud de serie es obligatoria.");

        if (request.serie() == null || !request.serie().trim().matches("^B\\d{3}$")) {
            fail("La serie debe tener formato B001.");
        }

        validarRangoNumerico(request.numeroInicio(), request.numeroFin());
        validarNoReutilizarSerie(existente);
    }

    public void validarRangoNumerico(Long numeroInicio, Long numeroFin) {
        requirePositive(numeroInicio, "El número inicial");

        if (numeroFin != null && numeroFin <= 0) {
            fail("El número final debe ser mayor a cero.");
        }

        if (numeroFin != null && numeroFin < numeroInicio) {
            fail("El número final no puede ser menor al número inicial.");
        }
    }

    public void validarNoReutilizarSerie(SerieBoleta existente) {
        if (existente != null) {
            conflict("La serie de boleta ya existe y no puede reutilizarse.");
        }
    }

    public void validarPuedeInactivar(SerieBoleta serieBoleta, boolean tieneBoletasEmitidas) {
        require(serieBoleta, "La serie de boleta es obligatoria.");

        if (!Boolean.TRUE.equals(serieBoleta.getEstado())) {
            conflict("La serie ya está inactiva.");
        }
    }

    public void validarCambioEstado(SerieBoleta serieBoleta, EstadoChangeRequestDto request) {
        require(serieBoleta, "La serie de boleta es obligatoria.");
        require(request, "La solicitud de cambio de estado es obligatoria.");
        require(request.estado(), "El estado es obligatorio.");
        requireText(request.motivo(), "El motivo es obligatorio para cambiar el estado.");
        requireMaxLength(request.motivo(), 500, "El motivo");

        if (Boolean.FALSE.equals(request.estado()) && !Boolean.TRUE.equals(serieBoleta.getEstado())) {
            conflict("La serie ya está inactiva.");
        }

        if (Boolean.TRUE.equals(request.estado()) && Boolean.TRUE.equals(serieBoleta.getEstado())) {
            conflict("La serie ya está activa.");
        }
    }

    public void validarFiltro(SerieBoletaFilterDto filter) {
        if (filter == null) {
            return;
        }

        requireMaxLength(filter.search(), 150, "El texto de búsqueda");

        if (filter.serie() != null && !filter.serie().isBlank() && !filter.serie().trim().matches("^B\\d{3}$")) {
            fail("La serie debe tener formato B001.");
        }
    }

    public void validarSerieDisponibleParaEmision(SerieBoleta serieBoleta) {
        require(serieBoleta, "La serie de boleta es obligatoria.");

        if (!Boolean.TRUE.equals(serieBoleta.getEstado())) {
            conflict("La serie de boleta no está activa.");
        }

        if (serieBoleta.getNumeroActual() == null || serieBoleta.getNumeroInicio() == null) {
            conflict("La serie de boleta tiene numeración inválida.");
        }

        if (serieBoleta.getNumeroActual() < serieBoleta.getNumeroInicio() - 1) {
            conflict("La numeración actual de la serie es inválida.");
        }

        if (serieBoleta.getNumeroFin() != null && serieBoleta.getNumeroActual() >= serieBoleta.getNumeroFin()) {
            conflict("La serie de boleta agotó su rango numérico.");
        }
    }

    public void validarNumeroReservado(Long numeroReservado) {
        if (numeroReservado == null || numeroReservado <= 0) {
            fail("El número reservado es obligatorio.");
        }
    }

    public boolean estaDisponibleParaEmision(SerieBoleta serieBoleta) {
        if (serieBoleta == null || !Boolean.TRUE.equals(serieBoleta.getEstado())) {
            return false;
        }

        if (serieBoleta.getNumeroActual() == null || serieBoleta.getNumeroInicio() == null) {
            return false;
        }

        if (serieBoleta.getNumeroActual() < serieBoleta.getNumeroInicio() - 1) {
            return false;
        }

        return serieBoleta.getNumeroFin() == null || serieBoleta.getNumeroActual() < serieBoleta.getNumeroFin();
    }
}