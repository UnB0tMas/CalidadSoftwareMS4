// ruta: src/main/java/com/upsjb/ms4/dto/snapshot/response/ProductoVentaResponseDto.java
package com.upsjb.ms4.dto.snapshot.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductoVentaResponseDto(
        Long id,
        Boolean snapshotCompleto,
        LocalDateTime snapshotGeneradoAt,

        Long idProductoMs3,
        String codigoProducto,
        String nombre,
        String slug,

        Long idCategoriaMs3,
        String codigoCategoria,
        String nombreCategoria,
        String slugCategoria,
        Integer nivelCategoria,
        Integer ordenCategoria,
        Boolean categoriaPermiteProductos,
        Boolean categoriaEstado,

        Long idCategoriaPadreMs3,
        String codigoCategoriaPadre,
        String nombreCategoriaPadre,
        String slugCategoriaPadre,

        String categoriaRutaCodigo,
        String categoriaRutaNombre,
        String categoriaRutaJson,

        Long idMarcaMs3,
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
        String plantillaAtributosJson,
        String atributosJson,
        String skusJson,
        String imagenesJson,

        UUID eventId,
        String eventType,
        String aggregateId,
        Integer eventVersion,
        LocalDateTime occurredAt,
        String requestId,
        String correlationId,
        LocalDateTime fechaSincronizacion,

        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}