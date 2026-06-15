package com.upsjb.ms4.dto.kafka.ms3;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SkuAtributoSnapshotPayloadDto(
        Long idSkuAtributoValor,
        Long idAtributo,
        String codigoAtributo,
        String nombreAtributo,
        String tipoDato,
        String unidadMedida,
        Boolean requeridoBase,
        Boolean requeridoCategoria,
        Boolean requerido,
        Boolean filtrable,
        Boolean visiblePublico,
        String valorTexto,
        BigDecimal valorNumero,
        Boolean valorBoolean,
        LocalDate valorFecha,
        String valorDisplay,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}