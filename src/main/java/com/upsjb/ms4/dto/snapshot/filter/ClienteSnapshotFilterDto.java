// ruta: src/main/java/com/upsjb/ms4/dto/snapshot/filter/ClienteSnapshotFilterDto.java
package com.upsjb.ms4.dto.snapshot.filter;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ClienteSnapshotFilterDto(

        @Size(max = 150, message = "El texto de búsqueda no debe superar 150 caracteres.")
        String search,

        @Positive(message = "El idClienteMs2 debe ser positivo.")
        Long idClienteMs2,

        @Positive(message = "El idUsuarioMs1 debe ser positivo.")
        Long idUsuarioMs1,

        @Size(max = 30, message = "El tipo de cliente no debe superar 30 caracteres.")
        String tipoCliente,

        @Size(max = 30, message = "El tipo de documento no debe superar 30 caracteres.")
        String tipoDocumentoPersona,

        @Size(max = 30, message = "El número de documento no debe superar 30 caracteres.")
        String numeroDocumentoPersona,

        @Size(max = 20, message = "El RUC no debe superar 20 caracteres.")
        String ruc,

        Boolean clienteActivoMs2,

        Boolean estado
) {
}