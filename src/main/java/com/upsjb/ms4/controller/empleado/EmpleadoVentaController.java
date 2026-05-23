package com.upsjb.ms4.controller.empleado;

import com.upsjb.ms4.config.SwaggerSecurityConfig;
import com.upsjb.ms4.dto.shared.ApiResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.dto.venta.filter.VentaFilterDto;
import com.upsjb.ms4.dto.venta.request.VentaFisicaCreateRequestDto;
import com.upsjb.ms4.dto.venta.response.VentaDetailResponseDto;
import com.upsjb.ms4.dto.venta.response.VentaResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.venta.VentaConsultaService;
import com.upsjb.ms4.service.contract.venta.VentaFisicaService;
import com.upsjb.ms4.shared.constants.ApiPaths;
import com.upsjb.ms4.shared.response.ApiResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
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
@RequestMapping(ApiPaths.EMPLEADO_VENTAS)
@Tag(name = "Empleado - Ventas", description = "Ventas físicas del empleado autenticado.")
@SecurityRequirement(name = SwaggerSecurityConfig.BEARER_AUTH)
public class EmpleadoVentaController {

    private final VentaFisicaService ventaFisicaService;
    private final VentaConsultaService ventaConsultaService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final ApiResponseFactory responseFactory;

    public EmpleadoVentaController(VentaFisicaService ventaFisicaService,
                                   VentaConsultaService ventaConsultaService,
                                   AuthenticatedUserResolver authenticatedUserResolver,
                                   ApiResponseFactory responseFactory) {
        this.ventaFisicaService = ventaFisicaService;
        this.ventaConsultaService = ventaConsultaService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.responseFactory = responseFactory;
    }

    @PostMapping
    @Operation(summary = "Crear venta física pendiente de pago")
    public ResponseEntity<ApiResponseDto<VentaDetailResponseDto>> crearVentaFisica(
            @Valid @RequestBody VentaFisicaCreateRequestDto request,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        VentaDetailResponseDto data = ventaFisicaService.crearVentaFisicaPendientePago(request, actor);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseFactory.created(
                data,
                "Venta física creada correctamente y pendiente de pago.",
                servletRequest
        ));
    }

    @GetMapping
    @Operation(summary = "Listar ventas del empleado autenticado")
    public ResponseEntity<ApiResponseDto<PageResponseDto<VentaResponseDto>>> listarMisVentas(
            @ParameterObject @Valid @ModelAttribute VentaFilterDto filter,
            @ParameterObject @Valid @ModelAttribute PageRequestDto page,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        PageResponseDto<VentaResponseDto> data = ventaConsultaService.listarVentasEmpleado(filter, page, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Ventas del empleado consultadas correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/{idVenta}")
    @Operation(summary = "Obtener detalle de venta autorizada")
    public ResponseEntity<ApiResponseDto<VentaDetailResponseDto>> obtenerVentaAutorizada(
            @PathVariable @Positive(message = "El idVenta debe ser positivo.") Long idVenta,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        VentaDetailResponseDto data = ventaConsultaService.obtenerDetalleEmpleado(idVenta, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Venta consultada correctamente.",
                servletRequest
        ));
    }
}