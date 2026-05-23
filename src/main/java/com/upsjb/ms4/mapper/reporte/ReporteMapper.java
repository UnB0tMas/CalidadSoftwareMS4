// ruta: src/main/java/com/upsjb/ms4/mapper/reporte/ReporteMapper.java
package com.upsjb.ms4.mapper.reporte;

import com.upsjb.ms4.domain.entity.caja.Caja;
import com.upsjb.ms4.domain.entity.snapshot.EmpleadoSnapshotMs2;
import com.upsjb.ms4.dto.reporte.response.ReporteAdminFinancieroResponseDto;
import com.upsjb.ms4.dto.reporte.response.ReporteAdminVentasResponseDto;
import com.upsjb.ms4.dto.reporte.response.ReporteEmpleadoCajaResponseDto;
import com.upsjb.ms4.dto.reporte.response.ReporteEmpleadoCierreCajaResponseDto;
import com.upsjb.ms4.dto.reporte.response.ReporteGananciaEstimadaDto;
import com.upsjb.ms4.dto.reporte.response.ReporteProductoVendidoDto;
import com.upsjb.ms4.dto.reporte.response.ReporteVentaPorCanalDto;
import com.upsjb.ms4.dto.reporte.response.ReporteVentaPorCategoriaDto;
import com.upsjb.ms4.dto.reporte.response.ReporteVentaPorEmpleadoDto;
import com.upsjb.ms4.dto.reporte.response.ReporteVentaPorMetodoPagoDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class ReporteMapper {

    public ReporteAdminVentasResponseDto toAdminVentasResponse(LocalDate fechaDesde,
                                                               LocalDate fechaHasta,
                                                               BigDecimal ventasTotales,
                                                               BigDecimal ventasNetas,
                                                               BigDecimal ventasBrutas,
                                                               BigDecimal descuentoTotal,
                                                               BigDecimal igvTotal,
                                                               Integer cantidadVentas,
                                                               BigDecimal ticketPromedio,
                                                               Integer ventasOnline,
                                                               Integer ventasFisicas,
                                                               List<ReporteProductoVendidoDto> productosMasVendidos,
                                                               List<ReporteVentaPorEmpleadoDto> ventasPorEmpleado,
                                                               List<ReporteVentaPorMetodoPagoDto> ventasPorMetodoPago,
                                                               List<ReporteVentaPorCanalDto> ventasPorCanal,
                                                               List<ReporteVentaPorCategoriaDto> ventasPorCategoria) {
        return new ReporteAdminVentasResponseDto(
                fechaDesde,
                fechaHasta,
                ventasTotales,
                ventasNetas,
                ventasBrutas,
                descuentoTotal,
                igvTotal,
                cantidadVentas,
                ticketPromedio,
                ventasOnline,
                ventasFisicas,
                safeList(productosMasVendidos),
                safeList(ventasPorEmpleado),
                safeList(ventasPorMetodoPago),
                safeList(ventasPorCanal),
                safeList(ventasPorCategoria)
        );
    }

    public ReporteAdminFinancieroResponseDto toAdminFinancieroResponse(LocalDate fechaDesde,
                                                                       LocalDate fechaHasta,
                                                                       BigDecimal ventasBrutas,
                                                                       BigDecimal ventasNetas,
                                                                       BigDecimal descuentoTotal,
                                                                       BigDecimal igvTotal,
                                                                       BigDecimal totalEfectivo,
                                                                       BigDecimal totalTarjeta,
                                                                       BigDecimal totalAnulado,
                                                                       Integer cantidadVentas,
                                                                       Integer cantidadPagos,
                                                                       ReporteGananciaEstimadaDto gananciaEstimada,
                                                                       List<ReporteVentaPorMetodoPagoDto> ventasPorMetodoPago,
                                                                       List<ReporteVentaPorCanalDto> ventasPorCanal) {
        return new ReporteAdminFinancieroResponseDto(
                fechaDesde,
                fechaHasta,
                ventasBrutas,
                ventasNetas,
                descuentoTotal,
                igvTotal,
                totalEfectivo,
                totalTarjeta,
                totalAnulado,
                cantidadVentas,
                cantidadPagos,
                gananciaEstimada,
                safeList(ventasPorMetodoPago),
                safeList(ventasPorCanal)
        );
    }

    public ReporteEmpleadoCajaResponseDto toEmpleadoCajaResponse(LocalDate fechaDesde,
                                                                 LocalDate fechaHasta,
                                                                 EmpleadoSnapshotMs2 empleado,
                                                                 Caja caja,
                                                                 BigDecimal totalEfectivo,
                                                                 BigDecimal totalTarjeta,
                                                                 BigDecimal totalVendido,
                                                                 Integer cantidadTransacciones,
                                                                 Integer cantidadVentasAnuladas,
                                                                 List<ReporteProductoVendidoDto> productosVendidos) {
        return new ReporteEmpleadoCajaResponseDto(
                fechaDesde,
                fechaHasta,
                empleado == null ? null : empleado.getIdEmpleadoMs2(),
                empleado == null ? null : empleado.getIdUsuarioMs1(),
                empleado == null ? null : empleado.getCodigoEmpleado(),
                nombreEmpleado(empleado),
                caja == null ? null : caja.getId(),
                caja == null ? null : caja.getCodigoCaja(),
                caja == null ? null : caja.getMontoInicial(),
                totalEfectivo,
                totalTarjeta,
                totalVendido,
                cantidadTransacciones,
                cantidadVentasAnuladas,
                caja == null ? null : caja.getDiferencia(),
                safeList(productosVendidos)
        );
    }

    public ReporteEmpleadoCierreCajaResponseDto toEmpleadoCierreCajaResponse(Caja caja,
                                                                             Integer cantidadVentas) {
        if (caja == null) {
            return null;
        }

        return new ReporteEmpleadoCierreCajaResponseDto(
                caja.getId(),
                caja.getCodigoCaja(),
                caja.getFechaOperacion(),
                caja.getIdEmpleadoAperturaSnapshot(),
                nombreEmpleado(caja.getEmpleadoAperturaSnapshot()),
                caja.getIdEmpleadoCierreSnapshot(),
                nombreEmpleado(caja.getEmpleadoCierreSnapshot()),
                caja.getMontoInicial(),
                caja.getMontoEsperadoEfectivo(),
                caja.getMontoRealEfectivo(),
                caja.getMontoTarjeta(),
                caja.getMontoTotalVendido(),
                caja.getDiferencia(),
                cantidadVentas,
                caja.getFechaApertura(),
                caja.getFechaCierre()
        );
    }

    public ReporteProductoVendidoDto toProductoVendido(Long idProductoMs3,
                                                       Long idSkuMs3,
                                                       String codigoProducto,
                                                       String codigoSku,
                                                       String nombreProducto,
                                                       String descripcionSku,
                                                       Integer cantidadVendida,
                                                       BigDecimal subtotal,
                                                       BigDecimal descuentoTotal,
                                                       BigDecimal igvTotal,
                                                       BigDecimal totalVendido) {
        return new ReporteProductoVendidoDto(
                idProductoMs3,
                idSkuMs3,
                codigoProducto,
                codigoSku,
                nombreProducto,
                descripcionSku,
                cantidadVendida,
                subtotal,
                descuentoTotal,
                igvTotal,
                totalVendido
        );
    }

    public ReporteVentaPorEmpleadoDto toVentaPorEmpleado(Long idEmpleadoMs2,
                                                         Long idUsuarioEmpleadoMs1,
                                                         String codigoEmpleado,
                                                         String nombreEmpleado,
                                                         Integer cantidadVentas,
                                                         BigDecimal totalVendido,
                                                         BigDecimal totalEfectivo,
                                                         BigDecimal totalTarjeta) {
        return new ReporteVentaPorEmpleadoDto(
                idEmpleadoMs2,
                idUsuarioEmpleadoMs1,
                codigoEmpleado,
                nombreEmpleado,
                cantidadVentas,
                totalVendido,
                totalEfectivo,
                totalTarjeta
        );
    }

    public ReporteVentaPorMetodoPagoDto toVentaPorMetodoPago(com.upsjb.ms4.domain.enums.MetodoPago metodoPago,
                                                             Integer cantidadVentas,
                                                             BigDecimal totalVendido) {
        return new ReporteVentaPorMetodoPagoDto(
                metodoPago,
                cantidadVentas,
                totalVendido
        );
    }

    public ReporteVentaPorCanalDto toVentaPorCanal(com.upsjb.ms4.domain.enums.CanalVenta canalVenta,
                                                   Integer cantidadVentas,
                                                   BigDecimal totalVendido) {
        return new ReporteVentaPorCanalDto(
                canalVenta,
                cantidadVentas,
                totalVendido
        );
    }

    public ReporteVentaPorCategoriaDto toVentaPorCategoria(Long idCategoriaMs3,
                                                           String codigoCategoria,
                                                           String nombreCategoria,
                                                           Integer cantidadProductosVendidos,
                                                           Integer cantidadVentas,
                                                           BigDecimal totalVendido) {
        return new ReporteVentaPorCategoriaDto(
                idCategoriaMs3,
                codigoCategoria,
                nombreCategoria,
                cantidadProductosVendidos,
                cantidadVentas,
                totalVendido
        );
    }

    public ReporteGananciaEstimadaDto toGananciaEstimada(BigDecimal totalVenta,
                                                         BigDecimal costoEstimado,
                                                         BigDecimal gananciaEstimada,
                                                         BigDecimal margenEstimadoPorcentaje,
                                                         Boolean costoSnapshotSuficiente,
                                                         String observacion) {
        return new ReporteGananciaEstimadaDto(
                totalVenta,
                costoEstimado,
                gananciaEstimada,
                margenEstimadoPorcentaje,
                costoSnapshotSuficiente,
                observacion
        );
    }

    private String nombreEmpleado(EmpleadoSnapshotMs2 empleado) {
        if (empleado == null) {
            return null;
        }

        return firstNonBlank(
                empleado.getNombreCompleto(),
                joinNombre(empleado.getNombres(), empleado.getApePaterno(), empleado.getApeMaterno()),
                empleado.getCodigoEmpleado()
        );
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }

        return null;
    }

    private String joinNombre(String nombres, String apePaterno, String apeMaterno) {
        String joined = String.join(
                " ",
                nombres == null ? "" : nombres.trim(),
                apePaterno == null ? "" : apePaterno.trim(),
                apeMaterno == null ? "" : apeMaterno.trim()
        ).trim();

        return joined.isBlank() ? null : joined;
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }
}