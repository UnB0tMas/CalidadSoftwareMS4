package com.upsjb.ms4.dto.kafka.ms3;

public record CategoriaAtributoSnapshotPayloadDto(
        Long idCategoriaAtributo,
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
        Integer orden,
        Boolean estado
) {
}