package com.upsjb.ms4.controller.empleado;

import com.upsjb.ms4.config.SwaggerSecurityConfig;
import com.upsjb.ms4.dto.caja.filter.CajaMovimientoFilterDto;
import com.upsjb.ms4.dto.caja.request.CajaAjusteRequestDto;
import com.upsjb.ms4.dto.caja.request.CajaAperturaRequestDto;
import com.upsjb.ms4.dto.caja.request.CajaCierreRequestDto;
import com.upsjb.ms4.dto.caja.response.CajaCierreResponseDto;
import com.upsjb.ms4.dto.caja.response.CajaMovimientoResponseDto;
import com.upsjb.ms4.dto.caja.response.CajaResponseDto;
import com.upsjb.ms4.dto.shared.ApiResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.caja.CajaMovimientoService;
import com.upsjb.ms4.service.contract.caja.CajaService;
import com.upsjb.ms4.shared.constants.ApiPaths;
import com.upsjb.ms4.shared.response.ApiResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping(ApiPaths.EMPLEADO_CAJA)
@Tag(name = "Empleado - Caja", description = "Apertura, cierre, ajustes y consulta de caja del empleado.")
@SecurityRequirement(name = SwaggerSecurityConfig.BEARER_AUTH)
public class EmpleadoCajaController {

    private final CajaService cajaService;
    private final CajaMovimientoService cajaMovimientoService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final ApiResponseFactory responseFactory;

    public EmpleadoCajaController(CajaService cajaService,
                                  CajaMovimientoService cajaMovimientoService,
                                  AuthenticatedUserResolver authenticatedUserResolver,
                                  ApiResponseFactory responseFactory) {
        this.cajaService = cajaService;
        this.cajaMovimientoService = cajaMovimientoService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.responseFactory = responseFactory;
    }

    @PostMapping("/aperturar")
    @Operation(summary = "Abrir caja del día")
    public ResponseEntity<ApiResponseDto<CajaResponseDto>> abrirCaja(
            @Valid @RequestBody CajaAperturaRequestDto request,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        CajaResponseDto data = cajaService.abrirCaja(request, actor);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseFactory.created(
                data,
                "Caja abierta correctamente.",
                servletRequest
        ));
    }

    @PostMapping("/cerrar")
    @Operation(summary = "Cerrar caja del día")
    public ResponseEntity<ApiResponseDto<CajaCierreResponseDto>> cerrarCaja(
            @Valid @RequestBody CajaCierreRequestDto request,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        CajaCierreResponseDto data = cajaService.cerrarCaja(request, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Caja cerrada correctamente.",
                servletRequest
        ));
    }

    @PostMapping("/ajustes")
    @Operation(summary = "Registrar ajuste de caja")
    public ResponseEntity<ApiResponseDto<CajaResponseDto>> registrarAjuste(
            @Valid @RequestBody CajaAjusteRequestDto request,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        CajaResponseDto data = cajaService.registrarAjuste(request, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Ajuste de caja registrado correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/actual")
    @Operation(summary = "Obtener caja abierta actual")
    public ResponseEntity<ApiResponseDto<CajaResponseDto>> obtenerCajaActual(
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        CajaResponseDto data = cajaService.obtenerCajaActual(actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Caja actual consultada correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/movimientos")
    @Operation(summary = "Listar movimientos de la caja abierta actual")
    public ResponseEntity<ApiResponseDto<PageResponseDto<CajaMovimientoResponseDto>>> listarMovimientosCajaActual(
            @ParameterObject @Valid @ModelAttribute CajaMovimientoFilterDto filter,
            @ParameterObject @Valid @ModelAttribute PageRequestDto page,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        CajaResponseDto cajaActual = cajaService.obtenerCajaActual(actor);

        PageResponseDto<CajaMovimientoResponseDto> data =
                cajaMovimientoService.listarMovimientos(cajaActual.id(), filter, page);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Movimientos de la caja actual consultados correctamente.",
                servletRequest
        ));
    }
}