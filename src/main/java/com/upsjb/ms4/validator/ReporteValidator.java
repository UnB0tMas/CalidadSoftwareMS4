// ruta: src/main/java/com/upsjb/ms4/validator/ReporteValidator.java
package com.upsjb.ms4.validator;

import com.upsjb.ms4.dto.reporte.filter.ReporteEmpleadoCajaFilterDto;
import com.upsjb.ms4.dto.reporte.filter.ReporteFinancieroAdminFilterDto;
import com.upsjb.ms4.dto.reporte.filter.ReporteVentasAdminFilterDto;
import com.upsjb.ms4.dto.reporte.request.ReporteAdminFinancieroRequestDto;
import com.upsjb.ms4.dto.reporte.request.ReporteAdminVentasRequestDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ReporteValidator extends ValidatorSupport {

    public void validarReporteAdminVentasRequest(ReporteAdminVentasRequestDto request) {
        require(request, "La solicitud del reporte de ventas es obligatoria.");
        validarRangoFechasObligatorio(request.fechaDesde(), request.fechaHasta());
        validarIdOpcional(request.idEmpleadoMs2(), "El empleado MS2");
        validarIdOpcional(request.idClienteMs2(), "El cliente MS2");
        validarIdOpcional(request.idProductoMs3(), "El producto MS3");
        validarIdOpcional(request.idSkuMs3(), "El SKU MS3");
        validarIdOpcional(request.idCategoriaMs3(), "La categoría MS3");
    }

    public void validarReporteAdminFinancieroRequest(ReporteAdminFinancieroRequestDto request) {
        require(request, "La solicitud del reporte financiero es obligatoria.");
        validarRangoFechasObligatorio(request.fechaDesde(), request.fechaHasta());
    }

    public void validarVentasAdminFilter(ReporteVentasAdminFilterDto filter) {
        if (filter == null) {
            return;
        }

        validarRangoFechasOpcional(filter.fechaDesde(), filter.fechaHasta());
        validarIdOpcional(filter.idEmpleadoMs2(), "El empleado MS2");
        validarIdOpcional(filter.idClienteMs2(), "El cliente MS2");
        validarIdOpcional(filter.idProductoMs3(), "El producto MS3");
        validarIdOpcional(filter.idSkuMs3(), "El SKU MS3");
        validarIdOpcional(filter.idCategoriaMs3(), "La categoría MS3");
    }

    public void validarFinancieroAdminFilter(ReporteFinancieroAdminFilterDto filter) {
        if (filter == null) {
            return;
        }

        validarRangoFechasOpcional(filter.fechaDesde(), filter.fechaHasta());
    }

    public void validarEmpleadoCajaFilter(ReporteEmpleadoCajaFilterDto filter) {
        if (filter == null) {
            return;
        }

        validarRangoFechasOpcional(filter.fechaDesde(), filter.fechaHasta());
        validarIdOpcional(filter.idEmpleadoMs2(), "El empleado MS2");
        validarIdOpcional(filter.idUsuarioEmpleadoMs1(), "El usuario empleado MS1");
    }

    public void validarFecha(LocalDate fecha) {
        require(fecha, "La fecha del reporte es obligatoria.");
    }

    public void validarIdCaja(Long idCaja) {
        requirePositive(idCaja, "La caja");
    }

    private void validarRangoFechasObligatorio(LocalDate fechaDesde, LocalDate fechaHasta) {
        require(fechaDesde, "La fecha inicial es obligatoria.");
        require(fechaHasta, "La fecha final es obligatoria.");
        validarOrdenFechas(fechaDesde, fechaHasta);
    }

    private void validarRangoFechasOpcional(LocalDate fechaDesde, LocalDate fechaHasta) {
        validarOrdenFechas(fechaDesde, fechaHasta);
    }

    private void validarOrdenFechas(LocalDate fechaDesde, LocalDate fechaHasta) {
        if (fechaDesde != null && fechaHasta != null && fechaHasta.isBefore(fechaDesde)) {
            fail("La fecha final no puede ser anterior a la fecha inicial.");
        }
    }

    private void validarIdOpcional(Long value, String fieldName) {
        if (value != null) {
            requirePositive(value, fieldName);
        }
    }
}