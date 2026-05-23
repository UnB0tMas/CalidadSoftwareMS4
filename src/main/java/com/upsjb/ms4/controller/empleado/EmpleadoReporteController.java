package com.upsjb.ms4.controller.empleado;

import com.upsjb.ms4.config.SwaggerSecurityConfig;
import com.upsjb.ms4.dto.reporte.filter.ReporteEmpleadoCajaFilterDto;
import com.upsjb.ms4.dto.reporte.response.ReporteEmpleadoCajaResponseDto;
import com.upsjb.ms4.dto.reporte.response.ReporteEmpleadoCierreCajaResponseDto;
import com.upsjb.ms4.dto.shared.ApiResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.dto.venta.response.VentaResumenResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.reporte.ReporteEmpleadoService;
import com.upsjb.ms4.shared.constants.ApiPaths;
import com.upsjb.ms4.shared.response.ApiResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping(ApiPaths.EMPLEADO_REPORTES)
@Tag(name = "Empleado - Reportes", description = "Reportes operativos de caja y ventas del empleado.")
@SecurityRequirement(name = SwaggerSecurityConfig.BEARER_AUTH)
public class EmpleadoReporteController {

    private final ReporteEmpleadoService reporteEmpleadoService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final ApiResponseFactory responseFactory;

    public EmpleadoReporteController(ReporteEmpleadoService reporteEmpleadoService,
                                     AuthenticatedUserResolver authenticatedUserResolver,
                                     ApiResponseFactory responseFactory) {
        this.reporteEmpleadoService = reporteEmpleadoService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.responseFactory = responseFactory;
    }

    @GetMapping("/caja/hoy")
    @Operation(summary = "Obtener reporte de caja del día")
    public ResponseEntity<ApiResponseDto<ReporteEmpleadoCajaResponseDto>> obtenerReporteCajaHoy(
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        ReporteEmpleadoCajaResponseDto data = reporteEmpleadoService.obtenerReporteCajaHoy(actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Reporte de caja del día consultado correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/caja/cierre/{idCaja}")
    @Operation(summary = "Obtener reporte de cierre de caja")
    public ResponseEntity<ApiResponseDto<ReporteEmpleadoCierreCajaResponseDto>> obtenerReporteCierreCaja(
            @PathVariable @Positive(message = "El idCaja debe ser positivo.") Long idCaja,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        ReporteEmpleadoCierreCajaResponseDto data = reporteEmpleadoService.obtenerReporteCierreCaja(idCaja, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Reporte de cierre de caja consultado correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/ventas")
    @Operation(summary = "Obtener reporte paginado de ventas del empleado")
    public ResponseEntity<ApiResponseDto<PageResponseDto<VentaResumenResponseDto>>> obtenerReporteVentasEmpleado(
            @ParameterObject @Valid @ModelAttribute ReporteEmpleadoCajaFilterDto filter,
            @ParameterObject @Valid @ModelAttribute PageRequestDto page,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        PageResponseDto<VentaResumenResponseDto> data =
                reporteEmpleadoService.listarVentasEmpleado(filter, page, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Reporte de ventas del empleado consultado correctamente.",
                servletRequest
        ));
    }
}