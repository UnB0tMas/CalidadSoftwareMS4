// ruta: src/main/java/com/upsjb/ms4/dto/boleta/response/BoletaResponseDto.java
package com.upsjb.ms4.dto.boleta.response;

import com.upsjb.ms4.domain.enums.EstadoBoleta;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BoletaResponseDto(
        Long id,
        Long idVenta,
        Long idSerieBoleta,
        Long idConfiguracionEmpresaVersion,
        Long idConfiguracionTributariaVersion,
        Long idBoletaPlantillaVersion,
        String serie,
        Long numero,
        String codigoBoleta,
        LocalDateTime fechaEmision,
        String moneda,
        String rucEmisor,
        String razonSocialEmisor,
        String nombreComercialEmisor,
        String direccionFiscalEmisor,
        String telefonoEmisor,
        String correoEmisor,
        String logoUrlEmisor,
        String tipoDocumentoCliente,
        String numeroDocumentoCliente,
        String nombreCliente,
        String correoCliente,
        String telefonoCliente,
        String direccionCliente,
        BigDecimal subtotal,
        BigDecimal descuentoTotal,
        BigDecimal opGravada,
        BigDecimal opExonerada,
        BigDecimal opInafecta,
        BigDecimal igvPorcentaje,
        BigDecimal igvTotal,
        BigDecimal total,
        EstadoBoleta estadoBoleta,
        String hashPayload,
        String versionPlantilla,
        Boolean enviadoPorCorreo,
        LocalDateTime fechaUltimoEnvioCorreo,
        Integer cantidadEnviosCorreo,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}