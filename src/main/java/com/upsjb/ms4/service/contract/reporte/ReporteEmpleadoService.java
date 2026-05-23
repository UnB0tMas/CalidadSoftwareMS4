// ruta: src/main/java/com/upsjb/ms4/service/contract/reporte/ReporteEmpleadoService.java
package com.upsjb.ms4.service.contract.reporte;

import com.upsjb.ms4.dto.reporte.filter.ReporteEmpleadoCajaFilterDto;
import com.upsjb.ms4.dto.reporte.response.ReporteEmpleadoCajaResponseDto;
import com.upsjb.ms4.dto.reporte.response.ReporteEmpleadoCierreCajaResponseDto;
import com.upsjb.ms4.dto.reporte.response.ReporteVentaPorMetodoPagoDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.dto.venta.response.VentaResumenResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;

import java.time.LocalDate;

public interface ReporteEmpleadoService {

    ReporteEmpleadoCajaResponseDto obtenerReporteCajaHoy(AuthenticatedUserContext actor);

    ReporteEmpleadoCajaResponseDto obtenerReporteCajaPorFecha(LocalDate fecha,
                                                              AuthenticatedUserContext actor);

    ReporteEmpleadoCierreCajaResponseDto obtenerReporteCierreCaja(Long idCaja,
                                                                  AuthenticatedUserContext actor);

    PageResponseDto<VentaResumenResponseDto> listarVentasEmpleado(ReporteEmpleadoCajaFilterDto filter,
                                                                  PageRequestDto page,
                                                                  AuthenticatedUserContext actor);

    ReporteVentaPorMetodoPagoDto obtenerResumenPorMetodoPago(LocalDate fecha,
                                                             AuthenticatedUserContext actor);
}