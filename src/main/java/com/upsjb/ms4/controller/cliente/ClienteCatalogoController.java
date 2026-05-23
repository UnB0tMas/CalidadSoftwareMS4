package com.upsjb.ms4.controller.cliente;

import com.upsjb.ms4.config.SwaggerSecurityConfig;
import com.upsjb.ms4.dto.shared.ApiResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.dto.snapshot.filter.ProductoVentaFilterDto;
import com.upsjb.ms4.dto.snapshot.response.ProductoVentaResponseDto;
import com.upsjb.ms4.dto.snapshot.response.SkuVentaResponseDto;
import com.upsjb.ms4.dto.venta.request.VentaCalculoPreviewRequestDto;
import com.upsjb.ms4.dto.venta.response.VentaCalculoPreviewResponseDto;
import com.upsjb.ms4.policy.SnapshotPolicy;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.snapshot.CatalogoVentaSnapshotService;
import com.upsjb.ms4.service.contract.venta.VentaOnlineService;
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
@RequestMapping(ApiPaths.CLIENTE_CATALOGO)
@Tag(name = "Cliente - Catálogo", description = "Consulta de snapshots vendibles para compra online.")
@SecurityRequirement(name = SwaggerSecurityConfig.BEARER_AUTH)
public class ClienteCatalogoController {

    private final CatalogoVentaSnapshotService catalogoVentaSnapshotService;
    private final VentaOnlineService ventaOnlineService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final SnapshotPolicy snapshotPolicy;
    private final ApiResponseFactory responseFactory;

    public ClienteCatalogoController(CatalogoVentaSnapshotService catalogoVentaSnapshotService,
                                     VentaOnlineService ventaOnlineService,
                                     AuthenticatedUserResolver authenticatedUserResolver,
                                     SnapshotPolicy snapshotPolicy,
                                     ApiResponseFactory responseFactory) {
        this.catalogoVentaSnapshotService = catalogoVentaSnapshotService;
        this.ventaOnlineService = ventaOnlineService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.snapshotPolicy = snapshotPolicy;
        this.responseFactory = responseFactory;
    }

    @GetMapping("/productos")
    @Operation(summary = "Listar productos vendibles para compra online")
    public ResponseEntity<ApiResponseDto<PageResponseDto<ProductoVentaResponseDto>>> listarProductosVendibles(
            @ParameterObject @Valid @ModelAttribute ProductoVentaFilterDto filter,
            @ParameterObject @Valid @ModelAttribute PageRequestDto page,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        snapshotPolicy.authorizeConsultarCatalogoVenta(actor);

        PageResponseDto<ProductoVentaResponseDto> data =
                catalogoVentaSnapshotService.listarProductosVendibles(filter, page);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Productos vendibles consultados correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/productos/{idProductoMs3}")
    @Operation(summary = "Obtener producto vendible por identificador MS3")
    public ResponseEntity<ApiResponseDto<ProductoVentaResponseDto>> obtenerProductoVendible(
            @PathVariable @Positive(message = "El idProductoMs3 debe ser positivo.") Long idProductoMs3,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        snapshotPolicy.authorizeConsultarCatalogoVenta(actor);

        ProductoVentaResponseDto data = catalogoVentaSnapshotService.obtenerProductoVendible(idProductoMs3);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Producto vendible consultado correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/skus/{idSkuMs3}")
    @Operation(summary = "Obtener SKU vendible por identificador MS3")
    public ResponseEntity<ApiResponseDto<SkuVentaResponseDto>> obtenerSkuVendible(
            @PathVariable @Positive(message = "El idSkuMs3 debe ser positivo.") Long idSkuMs3,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        snapshotPolicy.authorizeConsultarCatalogoVenta(actor);

        SkuVentaResponseDto data = catalogoVentaSnapshotService.obtenerSkuVendible(idSkuMs3);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "SKU vendible consultado correctamente.",
                servletRequest
        ));
    }

    @PostMapping("/ventas/preview")
    @Operation(summary = "Previsualizar compra online")
    public ResponseEntity<ApiResponseDto<VentaCalculoPreviewResponseDto>> previsualizarCompraOnline(
            @Valid @RequestBody VentaCalculoPreviewRequestDto request,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        VentaCalculoPreviewResponseDto data = ventaOnlineService.previsualizarVentaOnline(request, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Previsualización de compra online calculada correctamente.",
                servletRequest
        ));
    }
}