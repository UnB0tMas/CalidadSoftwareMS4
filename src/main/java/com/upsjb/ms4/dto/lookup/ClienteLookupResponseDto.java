// ruta: src/main/java/com/upsjb/ms4/dto/lookup/ClienteLookupResponseDto.java
package com.upsjb.ms4.dto.lookup;

public record ClienteLookupResponseDto(
        Long id,
        Long idClienteMs2,
        Long idUsuarioMs1,
        String tipoCliente,
        String tipoDocumento,
        String numeroDocumento,
        String nombreCompleto,
        String razonSocial,
        String correoPrincipal,
        String telefonoPrincipal,
        String direccionPrincipal,
        Boolean clienteActivoMs2,
        Boolean estado
) {
}