// ruta: src/main/java/com/upsjb/ms4/service/contract/reporte/ReporteAdminService.java
package com.upsjb.ms4.service.contract.reporte;

import com.upsjb.ms4.dto.reporte.filter.ReporteFinancieroAdminFilterDto;
import com.upsjb.ms4.dto.reporte.filter.ReporteVentasAdminFilterDto;
import com.upsjb.ms4.dto.reporte.request.ReporteAdminFinancieroRequestDto;
import com.upsjb.ms4.dto.reporte.request.ReporteAdminVentasRequestDto;
import com.upsjb.ms4.dto.reporte.response.ReporteAdminFinancieroResponseDto;
import com.upsjb.ms4.dto.reporte.response.ReporteAdminVentasResponseDto;
import com.upsjb.ms4.dto.reporte.response.ReporteGananciaEstimadaDto;
import com.upsjb.ms4.dto.reporte.response.ReporteProductoVendidoDto;
import com.upsjb.ms4.dto.reporte.response.ReporteVentaPorCanalDto;
import com.upsjb.ms4.dto.reporte.response.ReporteVentaPorCategoriaDto;
import com.upsjb.ms4.dto.reporte.response.ReporteVentaPorEmpleadoDto;
import com.upsjb.ms4.dto.reporte.response.ReporteVentaPorMetodoPagoDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;

import java.util.List;

public interface ReporteAdminService {

    ReporteAdminVentasResponseDto generarReporteVentas(ReporteAdminVentasRequestDto request,
                                                       AuthenticatedUserContext actor);

    ReporteAdminFinancieroResponseDto generarReporteFinanciero(ReporteAdminFinancieroRequestDto request,
                                                               AuthenticatedUserContext actor);

    List<ReporteProductoVendidoDto> obtenerProductosMasVendidos(ReporteVentasAdminFilterDto filter);

    List<ReporteVentaPorEmpleadoDto> obtenerVentasPorEmpleado(ReporteVentasAdminFilterDto filter);

    List<ReporteVentaPorMetodoPagoDto> obtenerVentasPorMetodoPago(ReporteVentasAdminFilterDto filter);

    List<ReporteVentaPorCanalDto> obtenerVentasPorCanal(ReporteVentasAdminFilterDto filter);

    List<ReporteVentaPorCategoriaDto> obtenerVentasPorCategoria(ReporteVentasAdminFilterDto filter);

    ReporteGananciaEstimadaDto calcularGananciaEstimada(ReporteFinancieroAdminFilterDto filter);
}