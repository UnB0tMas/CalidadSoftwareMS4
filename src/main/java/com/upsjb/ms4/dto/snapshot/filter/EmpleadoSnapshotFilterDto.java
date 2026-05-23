// ruta: src/main/java/com/upsjb/ms4/dto/snapshot/filter/EmpleadoSnapshotFilterDto.java
package com.upsjb.ms4.dto.snapshot.filter;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EmpleadoSnapshotFilterDto(

        @Size(max = 150, message = "El texto de búsqueda no debe superar 150 caracteres.")
        String search,

        @Positive(message = "El idEmpleadoMs2 debe ser positivo.")
        Long idEmpleadoMs2,

        @Positive(message = "El idUsuarioMs1 debe ser positivo.")
        Long idUsuarioMs1,

        @Size(max = 80, message = "El código de empleado no debe superar 80 caracteres.")
        String codigoEmpleado,

        @Size(max = 30, message = "El tipo de documento no debe superar 30 caracteres.")
        String tipoDocumento,

        @Size(max = 30, message = "El número de documento no debe superar 30 caracteres.")
        String numeroDocumento,

        @Size(max = 80, message = "El código de área no debe superar 80 caracteres.")
        String areaCodigo,

        Boolean empleadoActivoMs2,

        Boolean estado
) {
}