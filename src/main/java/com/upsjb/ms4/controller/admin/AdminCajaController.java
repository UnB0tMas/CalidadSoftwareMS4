package com.upsjb.ms4.controller.admin;

import com.upsjb.ms4.config.SwaggerSecurityConfig;
import com.upsjb.ms4.dto.caja.filter.CajaFilterDto;
import com.upsjb.ms4.dto.caja.filter.CajaMovimientoFilterDto;
import com.upsjb.ms4.dto.caja.response.CajaDetailResponseDto;
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
@RequestMapping(ApiPaths.ADMIN_CAJAS)
@Tag(name = "Admin - Caja", description = "Consulta administrativa de cajas y movimientos.")
@SecurityRequirement(name = SwaggerSecurityConfig.BEARER_AUTH)
public class AdminCajaController {

    private final CajaService cajaService;
    private final CajaMovimientoService cajaMovimientoService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final ApiResponseFactory responseFactory;

    public AdminCajaController(CajaService cajaService,
                               CajaMovimientoService cajaMovimientoService,
                               AuthenticatedUserResolver authenticatedUserResolver,
                               ApiResponseFactory responseFactory) {
        this.cajaService = cajaService;
        this.cajaMovimientoService = cajaMovimientoService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    @Operation(summary = "Listar cajas administrativamente")
    public ResponseEntity<ApiResponseDto<PageResponseDto<CajaResponseDto>>> listarCajas(
            @ParameterObject @Valid @ModelAttribute CajaFilterDto filter,
            @ParameterObject @Valid @ModelAttribute PageRequestDto page,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        PageResponseDto<CajaResponseDto> data = cajaService.listar(filter, page, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Cajas consultadas correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/{idCaja}")
    @Operation(summary = "Obtener detalle administrativo de caja")
    public ResponseEntity<ApiResponseDto<CajaDetailResponseDto>> obtenerCaja(
            @PathVariable @Positive(message = "El idCaja debe ser positivo.") Long idCaja,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        CajaDetailResponseDto data = cajaService.obtenerDetalle(idCaja, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Caja consultada correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/{idCaja}/movimientos")
    @Operation(summary = "Listar movimientos de una caja")
    public ResponseEntity<ApiResponseDto<PageResponseDto<CajaMovimientoResponseDto>>> listarMovimientos(
            @PathVariable @Positive(message = "El idCaja debe ser positivo.") Long idCaja,
            @ParameterObject @Valid @ModelAttribute CajaMovimientoFilterDto filter,
            @ParameterObject @Valid @ModelAttribute PageRequestDto page,
            HttpServletRequest servletRequest
    ) {
        authenticatedUserResolver.current();
        PageResponseDto<CajaMovimientoResponseDto> data =
                cajaMovimientoService.listarMovimientos(idCaja, filter, page);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Movimientos de caja consultados correctamente.",
                servletRequest
        ));
    }
}