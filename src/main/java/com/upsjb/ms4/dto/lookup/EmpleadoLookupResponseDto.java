// ruta: src/main/java/com/upsjb/ms4/dto/lookup/EmpleadoLookupResponseDto.java
package com.upsjb.ms4.dto.lookup;

public record EmpleadoLookupResponseDto(
        Long id,
        Long idEmpleadoMs2,
        Long idUsuarioMs1,
        String codigoEmpleado,
        String nombreCompleto,
        String tipoDocumento,
        String numeroDocumento,
        String correo,
        String areaCodigo,
        String areaNombre,
        Boolean empleadoActivoMs2,
        Boolean estado
) {
}