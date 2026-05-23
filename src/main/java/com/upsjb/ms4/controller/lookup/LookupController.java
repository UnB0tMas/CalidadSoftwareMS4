package com.upsjb.ms4.controller.lookup;

import com.upsjb.ms4.config.SwaggerSecurityConfig;
import com.upsjb.ms4.dto.lookup.AlmacenLookupResponseDto;
import com.upsjb.ms4.dto.lookup.CajaAbiertaLookupResponseDto;
import com.upsjb.ms4.dto.lookup.ClienteLookupResponseDto;
import com.upsjb.ms4.dto.lookup.EmpleadoLookupResponseDto;
import com.upsjb.ms4.dto.lookup.LookupFilterDto;
import com.upsjb.ms4.dto.lookup.ProductoLookupResponseDto;
import com.upsjb.ms4.dto.lookup.SerieBoletaLookupResponseDto;
import com.upsjb.ms4.dto.lookup.SkuLookupResponseDto;
import com.upsjb.ms4.dto.shared.ApiResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.lookup.LookupService;
import com.upsjb.ms4.shared.constants.ApiPaths;
import com.upsjb.ms4.shared.response.ApiResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping(ApiPaths.LOOKUPS)
@Tag(name = "Lookups", description = "Lookups legibles para selección UX de clientes, empleados, catálogo, almacenes, series y caja.")
@SecurityRequirement(name = SwaggerSecurityConfig.BEARER_AUTH)
public class LookupController {

    private final LookupService lookupService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final ApiResponseFactory responseFactory;

    public LookupController(LookupService lookupService,
                            AuthenticatedUserResolver authenticatedUserResolver,
                            ApiResponseFactory responseFactory) {
        this.lookupService = lookupService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.responseFactory = responseFactory;
    }

    @GetMapping("/clientes")
    @Operation(summary = "Buscar clientes sincronizados desde MS2")
    public ResponseEntity<ApiResponseDto<List<ClienteLookupResponseDto>>> clientes(
            @ParameterObject @Valid @ModelAttribute LookupFilterDto filter,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        List<ClienteLookupResponseDto> data = lookupService.clientes(filter, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Clientes disponibles consultados correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/empleados")
    @Operation(summary = "Buscar empleados sincronizados desde MS2")
    public ResponseEntity<ApiResponseDto<List<EmpleadoLookupResponseDto>>> empleados(
            @ParameterObject @Valid @ModelAttribute LookupFilterDto filter,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        List<EmpleadoLookupResponseDto> data = lookupService.empleados(filter, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Empleados disponibles consultados correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/productos")
    @Operation(summary = "Buscar productos vendibles sincronizados desde MS3")
    public ResponseEntity<ApiResponseDto<List<ProductoLookupResponseDto>>> productos(
            @ParameterObject @Valid @ModelAttribute LookupFilterDto filter,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        List<ProductoLookupResponseDto> data = lookupService.productos(filter, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Productos disponibles consultados correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/skus")
    @Operation(summary = "Buscar SKUs sincronizados desde MS3")
    public ResponseEntity<ApiResponseDto<List<SkuLookupResponseDto>>> skus(
            @ParameterObject @Valid @ModelAttribute LookupFilterDto filter,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        List<SkuLookupResponseDto> data = lookupService.skus(filter, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "SKUs disponibles consultados correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/almacenes")
    @Operation(summary = "Buscar almacenes disponibles desde snapshots de stock MS3")
    public ResponseEntity<ApiResponseDto<List<AlmacenLookupResponseDto>>> almacenes(
            @ParameterObject @Valid @ModelAttribute LookupFilterDto filter,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        List<AlmacenLookupResponseDto> data = lookupService.almacenes(filter, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Almacenes disponibles consultados correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/series-boleta")
    @Operation(summary = "Buscar series de boleta disponibles")
    public ResponseEntity<ApiResponseDto<List<SerieBoletaLookupResponseDto>>> seriesBoleta(
            @ParameterObject @Valid @ModelAttribute LookupFilterDto filter,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        List<SerieBoletaLookupResponseDto> data = lookupService.seriesBoleta(filter, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Series de boleta disponibles consultadas correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/caja-abierta")
    @Operation(summary = "Obtener caja abierta del día")
    public ResponseEntity<ApiResponseDto<CajaAbiertaLookupResponseDto>> cajaAbiertaHoy(
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        CajaAbiertaLookupResponseDto data = lookupService.cajaAbiertaHoy(actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Caja abierta del día consultada correctamente.",
                servletRequest
        ));
    }
}