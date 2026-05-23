// ruta: src/main/java/com/upsjb/ms4/dto/reporte/request/ReporteAdminVentasRequestDto.java
package com.upsjb.ms4.dto.reporte.request;

import com.upsjb.ms4.domain.enums.CanalVenta;
import com.upsjb.ms4.domain.enums.MetodoPago;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReporteAdminVentasRequestDto(
        @NotNull(message = "La fecha inicial es obligatoria.")
        LocalDate fechaDesde,

        @NotNull(message = "La fecha final es obligatoria.")
        LocalDate fechaHasta,

        CanalVenta canalVenta,
        MetodoPago metodoPago,
        Long idEmpleadoMs2,
        Long idClienteMs2,
        Long idProductoMs3,
        Long idSkuMs3,
        Long idCategoriaMs3
) {
}