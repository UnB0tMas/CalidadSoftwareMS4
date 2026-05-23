// ruta: src/main/java/com/upsjb/ms4/dto/caja/filter/CajaFilterDto.java
package com.upsjb.ms4.dto.caja.filter;

import com.upsjb.ms4.domain.enums.EstadoCaja;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CajaFilterDto(

        @Size(max = 150, message = "El texto de búsqueda no debe superar 150 caracteres.")
        String search,

        @Size(max = 80, message = "El código de caja no debe superar 80 caracteres.")
        @Pattern(
                regexp = "^[A-Z0-9_-]+$",
                message = "El código de caja solo puede contener mayúsculas, números, guion y guion bajo."
        )
        String codigoCaja,

        EstadoCaja estadoCaja,

        @Positive(message = "El empleado de apertura debe ser un identificador positivo.")
        Long idEmpleadoAperturaSnapshot,

        @Positive(message = "El empleado de cierre debe ser un identificador positivo.")
        Long idEmpleadoCierreSnapshot,

        Boolean estado,

        LocalDate fechaDesde,

        LocalDate fechaHasta
) {
}