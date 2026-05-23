// ruta: src/main/java/com/upsjb/ms4/dto/reporte/request/ReporteAdminFinancieroRequestDto.java
package com.upsjb.ms4.dto.reporte.request;

import com.upsjb.ms4.domain.enums.CanalVenta;
import com.upsjb.ms4.domain.enums.MetodoPago;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReporteAdminFinancieroRequestDto(
        @NotNull(message = "La fecha inicial es obligatoria.")
        LocalDate fechaDesde,

        @NotNull(message = "La fecha final es obligatoria.")
        LocalDate fechaHasta,

        CanalVenta canalVenta,
        MetodoPago metodoPago
) {
}