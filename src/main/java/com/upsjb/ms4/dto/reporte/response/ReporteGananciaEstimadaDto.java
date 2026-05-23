// ruta: src/main/java/com/upsjb/ms4/dto/reporte/response/ReporteGananciaEstimadaDto.java
package com.upsjb.ms4.dto.reporte.response;

import java.math.BigDecimal;

public record ReporteGananciaEstimadaDto(
        BigDecimal totalVenta,
        BigDecimal costoEstimado,
        BigDecimal gananciaEstimada,
        BigDecimal margenEstimadoPorcentaje,
        Boolean costoSnapshotSuficiente,
        String observacion
) {
}