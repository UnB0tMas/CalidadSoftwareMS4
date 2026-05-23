// ruta: src/main/java/com/upsjb/ms4/validator/CajaCierreValidator.java
package com.upsjb.ms4.validator;

import com.upsjb.ms4.domain.entity.caja.Caja;
import com.upsjb.ms4.domain.enums.EstadoCaja;
import com.upsjb.ms4.dto.caja.request.CajaCierreRequestDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CajaCierreValidator extends ValidatorSupport {

    public void validarCierre(Caja caja, CajaCierreRequestDto request, LocalDate fechaOperacion) {
        validarCajaNoCerrada(caja);
        validarMontoReal(request);
        validarCajaPerteneceADia(caja, fechaOperacion);
        requireMaxLength(request.observacionCierre(), 500, "La observación de cierre");
    }

    public void validarMontoReal(CajaCierreRequestDto request) {
        require(request, "La solicitud de cierre de caja es obligatoria.");
        requireNotNegative(request.montoRealEfectivo(), "El monto real en efectivo");
    }

    public void validarCajaPerteneceADia(Caja caja, LocalDate fechaOperacion) {
        require(caja, "La caja es obligatoria.");

        if (fechaOperacion == null) {
            return;
        }

        if (!fechaOperacion.equals(caja.getFechaOperacion())) {
            fail("La caja no pertenece al día de operación indicado.");
        }
    }

    public void validarCajaNoCerrada(Caja caja) {
        require(caja, "La caja es obligatoria.");
        requireActive(caja.getEstado(), "La caja no está activa.");

        if (caja.getEstadoCaja() == EstadoCaja.CERRADA) {
            conflict("La caja ya se encuentra cerrada.");
        }

        if (caja.getEstadoCaja() == EstadoCaja.ANULADA) {
            conflict("Una caja anulada no puede cerrarse.");
        }

        if (caja.getEstadoCaja() != EstadoCaja.ABIERTA) {
            conflict("Solo una caja abierta puede cerrarse.");
        }
    }
}