// ruta: src/main/java/com/upsjb/ms4/dto/config/response/ConfiguracionEmpresaResponseDto.java
package com.upsjb.ms4.dto.config.response;

import java.time.LocalDateTime;

public record ConfiguracionEmpresaResponseDto(
        Long id,
        String codigoVersion,
        String ruc,
        String razonSocial,
        String nombreComercial,
        String direccionFiscal,
        String telefono,
        String correo,
        String web,
        Long idLogoAsset,
        String logoUrl,
        String logoPublicId,
        String colorPrimario,
        String colorSecundario,
        String mensajePieBoleta,
        String terminosCondiciones,
        String politicaCambios,
        LocalDateTime fechaInicioVigencia,
        LocalDateTime fechaFinVigencia,
        Boolean vigente,
        String motivo,
        Long modificadoPorIdUsuarioMs1,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}