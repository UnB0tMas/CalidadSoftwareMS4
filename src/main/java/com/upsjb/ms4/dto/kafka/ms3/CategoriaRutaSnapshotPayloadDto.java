package com.upsjb.ms4.dto.kafka.ms3;

public record CategoriaRutaSnapshotPayloadDto(
        Long idCategoria,
        String codigo,
        String nombre,
        String slug,
        Integer nivel,
        Integer orden,
        Boolean permiteProductos,
        Boolean estado
) {
}