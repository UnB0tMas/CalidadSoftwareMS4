package com.upsjb.ms4.controller.admin;

import com.upsjb.ms4.config.SwaggerSecurityConfig;
import com.upsjb.ms4.dto.reporte.request.ReporteAdminFinancieroRequestDto;
import com.upsjb.ms4.dto.reporte.request.ReporteAdminVentasRequestDto;
import com.upsjb.ms4.dto.reporte.response.ReporteAdminFinancieroResponseDto;
import com.upsjb.ms4.dto.reporte.response.ReporteAdminVentasResponseDto;
import com.upsjb.ms4.dto.shared.ApiResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.reporte.ReporteAdminService;
import com.upsjb.ms4.shared.constants.ApiPaths;
import com.upsjb.ms4.shared.response.ApiResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping(ApiPaths.ADMIN_REPORTES)
@Tag(name = "Admin - Reportes", description = "Reportes administrativos de ventas y finanzas.")
@SecurityRequirement(name = SwaggerSecurityConfig.BEARER_AUTH)
public class AdminReporteController {

    private final ReporteAdminService reporteAdminService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final ApiResponseFactory responseFactory;

    public AdminReporteController(ReporteAdminService reporteAdminService,
                                  AuthenticatedUserResolver authenticatedUserResolver,
                                  ApiResponseFactory responseFactory) {
        this.reporteAdminService = reporteAdminService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.responseFactory = responseFactory;
    }

    @PostMapping("/ventas")
    @Operation(summary = "Generar reporte administrativo de ventas")
    public ResponseEntity<ApiResponseDto<ReporteAdminVentasResponseDto>> generarReporteVentas(
            @Valid @RequestBody ReporteAdminVentasRequestDto request,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        ReporteAdminVentasResponseDto data = reporteAdminService.generarReporteVentas(request, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Reporte de ventas generado correctamente.",
                servletRequest
        ));
    }

    @PostMapping("/financiero")
    @Operation(summary = "Generar reporte financiero administrativo")
    public ResponseEntity<ApiResponseDto<ReporteAdminFinancieroResponseDto>> generarReporteFinanciero(
            @Valid @RequestBody ReporteAdminFinancieroRequestDto request,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        ReporteAdminFinancieroResponseDto data = reporteAdminService.generarReporteFinanciero(request, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Reporte financiero generado correctamente.",
                servletRequest
        ));
    }
}