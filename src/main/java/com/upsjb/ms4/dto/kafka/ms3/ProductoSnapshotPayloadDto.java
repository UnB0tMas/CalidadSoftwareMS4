package com.upsjb.ms4.dto.kafka.ms3;

import java.time.LocalDateTime;
import java.util.List;

public record ProductoSnapshotPayloadDto(

        Boolean snapshotCompleto,
        LocalDateTime snapshotGeneradoAt,

        Long idProducto,
        String codigoProducto,
        String nombre,
        String slug,

        Long idCategoria,
        String codigoCategoria,
        String nombreCategoria,
        String slugCategoria,
        Integer nivelCategoria,
        Integer ordenCategoria,
        Boolean categoriaPermiteProductos,
        Boolean categoriaEstado,

        Long idCategoriaPadre,
        String codigoCategoriaPadre,
        String nombreCategoriaPadre,
        String slugCategoriaPadre,

        String categoriaRutaCodigo,
        String categoriaRutaNombre,
        List<CategoriaRutaSnapshotPayloadDto> categoriaRuta,

        Long idMarca,
        String codigoMarca,
        String nombreMarca,
        String slugMarca,
        Boolean marcaEstado,

        String descripcionCorta,
        String descripcionLarga,
        String generoObjetivo,
        String temporada,
        String deporte,

        String estadoRegistro,
        String estadoPublicacion,
        String estadoVenta,
        Boolean visiblePublico,
        Boolean vendible,
        LocalDateTime fechaPublicacionInicio,
        LocalDateTime fechaPublicacionFin,
        String motivoEstado,

        String imagenPrincipalUrl,

        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        List<CategoriaAtributoSnapshotPayloadDto> plantillaAtributos,
        List<ProductoAtributoSnapshotPayloadDto> atributos,
        List<SkuSnapshotPayloadDto> skus,
        List<ProductoImagenSnapshotPayloadDto> imagenes

) {
}