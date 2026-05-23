// ruta: src/main/java/com/upsjb/ms4/dto/kafka/ms3/ProductoSnapshotPayloadDto.java
package com.upsjb.ms4.dto.kafka.ms3;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ProductoSnapshotPayloadDto(
        Long idProducto,
        String codigoProducto,
        String nombre,
        String slug,
        Long idTipoProducto,
        String codigoTipoProducto,
        String nombreTipoProducto,
        Long idCategoria,
        String codigoCategoria,
        String nombreCategoria,
        String slugCategoria,
        Long idMarca,
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
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<Map<String, Object>> atributos,
        List<SkuSnapshotPayloadDto> skus,
        List<ProductoImagenSnapshotPayloadDto> imagenes
) {
}