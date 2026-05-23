// ruta: src/main/java/com/upsjb/ms4/controller/admin/AdminVentaController.java
package com.upsjb.ms4.controller.admin;

import com.upsjb.ms4.config.SwaggerSecurityConfig;
import com.upsjb.ms4.dto.shared.ApiResponseDto;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.dto.venta.filter.VentaFilterDto;
import com.upsjb.ms4.dto.venta.response.VentaDetailResponseDto;
import com.upsjb.ms4.dto.venta.response.VentaResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.venta.VentaAdminService;
import com.upsjb.ms4.service.contract.venta.VentaConsultaService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping(ApiPaths.ADMIN_VENTAS)
@Tag(name = "Admin - Ventas", description = "Consulta y anulación administrativa de ventas.")
@SecurityRequirement(name = SwaggerSecurityConfig.BEARER_AUTH)
public class AdminVentaController {

    private final VentaConsultaService ventaConsultaService;
    private final VentaAdminService ventaAdminService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final ApiResponseFactory responseFactory;

    public AdminVentaController(VentaConsultaService ventaConsultaService,
                                VentaAdminService ventaAdminService,
                                AuthenticatedUserResolver authenticatedUserResolver,
                                ApiResponseFactory responseFactory) {
        this.ventaConsultaService = ventaConsultaService;
        this.ventaAdminService = ventaAdminService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    @Operation(summary = "Listar ventas administrativamente")
    public ResponseEntity<ApiResponseDto<PageResponseDto<VentaResponseDto>>> listarVentas(
            @ParameterObject @Valid @ModelAttribute VentaFilterDto filter,
            @ParameterObject @Valid @ModelAttribute PageRequestDto page,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        PageResponseDto<VentaResponseDto> data = ventaConsultaService.listarVentasAdmin(filter, page, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Ventas consultadas correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/{idVenta}")
    @Operation(summary = "Obtener detalle administrativo de venta")
    public ResponseEntity<ApiResponseDto<VentaDetailResponseDto>> obtenerVenta(
            @PathVariable @Positive(message = "El idVenta debe ser positivo.") Long idVenta,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        VentaDetailResponseDto data = ventaConsultaService.obtenerDetalleAdmin(idVenta, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Venta consultada correctamente.",
                servletRequest
        ));
    }

    @PostMapping("/{idVenta}/anular")
    @Operation(summary = "Anular venta administrativamente")
    public ResponseEntity<ApiResponseDto<VentaDetailResponseDto>> anularVenta(
            @PathVariable @Positive(message = "El idVenta debe ser positivo.") Long idVenta,
            @Valid @RequestBody EstadoChangeRequestDto request,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        VentaDetailResponseDto data = ventaAdminService.anularVenta(idVenta, request, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Venta anulada correctamente.",
                servletRequest
        ));
    }
}