// ruta: src/main/java/com/upsjb/ms4/dto/snapshot/response/ProductoVentaResponseDto.java
package com.upsjb.ms4.dto.snapshot.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductoVentaResponseDto(
        Long id,
        Long idProductoMs3,
        String codigoProducto,
        String nombre,
        String slug,
        Long idTipoProductoMs3,
        String codigoTipoProducto,
        String nombreTipoProducto,
        Long idCategoriaMs3,
        String codigoCategoria,
        String nombreCategoria,
        String slugCategoria,
        Long idMarcaMs3,
        String codigoMarca,
        String nombreMarca,
        String slugMarca,
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
        String atributosJson,
        String skusJson,
        String imagenesJson,
        UUID eventId,
        String eventType,
        String aggregateId,
        Integer eventVersion,
        LocalDateTime occurredAt,
        LocalDateTime fechaSincronizacion,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}