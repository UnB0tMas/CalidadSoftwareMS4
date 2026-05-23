package com.upsjb.ms4.controller.empleado;

import com.upsjb.ms4.config.SwaggerSecurityConfig;
import com.upsjb.ms4.dto.shared.ApiResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.dto.snapshot.filter.ProductoVentaFilterDto;
import com.upsjb.ms4.dto.snapshot.filter.SkuVentaFilterDto;
import com.upsjb.ms4.dto.snapshot.filter.StockVentaFilterDto;
import com.upsjb.ms4.dto.snapshot.response.ProductoVentaResponseDto;
import com.upsjb.ms4.dto.snapshot.response.SkuVentaResponseDto;
import com.upsjb.ms4.dto.snapshot.response.StockVentaResponseDto;
import com.upsjb.ms4.dto.venta.request.VentaCalculoPreviewRequestDto;
import com.upsjb.ms4.dto.venta.response.VentaCalculoPreviewResponseDto;
import com.upsjb.ms4.policy.SnapshotPolicy;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.snapshot.CatalogoVentaSnapshotService;
import com.upsjb.ms4.service.contract.snapshot.StockSnapshotService;
import com.upsjb.ms4.service.contract.venta.VentaFisicaService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping(ApiPaths.EMPLEADO_CATALOGO)
@Tag(name = "Empleado - Catálogo", description = "Consulta de catálogo y stock snapshot para venta física.")
@SecurityRequirement(name = SwaggerSecurityConfig.BEARER_AUTH)
public class EmpleadoCatalogoController {

    private final CatalogoVentaSnapshotService catalogoVentaSnapshotService;
    private final StockSnapshotService stockSnapshotService;
    private final VentaFisicaService ventaFisicaService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final SnapshotPolicy snapshotPolicy;
    private final ApiResponseFactory responseFactory;

    public EmpleadoCatalogoController(CatalogoVentaSnapshotService catalogoVentaSnapshotService,
                                      StockSnapshotService stockSnapshotService,
                                      VentaFisicaService ventaFisicaService,
                                      AuthenticatedUserResolver authenticatedUserResolver,
                                      SnapshotPolicy snapshotPolicy,
                                      ApiResponseFactory responseFactory) {
        this.catalogoVentaSnapshotService = catalogoVentaSnapshotService;
        this.stockSnapshotService = stockSnapshotService;
        this.ventaFisicaService = ventaFisicaService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.snapshotPolicy = snapshotPolicy;
        this.responseFactory = responseFactory;
    }

    @GetMapping("/productos")
    @Operation(summary = "Listar productos vendibles para venta física")
    public ResponseEntity<ApiResponseDto<PageResponseDto<ProductoVentaResponseDto>>> listarProductosVenta(
            @ParameterObject @Valid @ModelAttribute ProductoVentaFilterDto filter,
            @ParameterObject @Valid @ModelAttribute PageRequestDto page,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        snapshotPolicy.authorizeConsultarSnapshots(actor);

        PageResponseDto<ProductoVentaResponseDto> data =
                catalogoVentaSnapshotService.listarProductosVendibles(filter, page);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Productos para venta física consultados correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/skus")
    @Operation(summary = "Listar SKUs vendibles para venta física")
    public ResponseEntity<ApiResponseDto<PageResponseDto<SkuVentaResponseDto>>> listarSkusVenta(
            @ParameterObject @Valid @ModelAttribute SkuVentaFilterDto filter,
            @ParameterObject @Valid @ModelAttribute PageRequestDto page,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        snapshotPolicy.authorizeConsultarSnapshots(actor);

        PageResponseDto<SkuVentaResponseDto> data =
                catalogoVentaSnapshotService.listarSkusVendibles(filter, page);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "SKUs para venta física consultados correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/stocks")
    @Operation(summary = "Listar stock snapshot para venta física")
    public ResponseEntity<ApiResponseDto<PageResponseDto<StockVentaResponseDto>>> listarStocksVenta(
            @ParameterObject @Valid @ModelAttribute StockVentaFilterDto filter,
            @ParameterObject @Valid @ModelAttribute PageRequestDto page,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        snapshotPolicy.authorizeConsultarSnapshots(actor);

        PageResponseDto<StockVentaResponseDto> data = stockSnapshotService.listar(filter, page);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Stock disponible consultado correctamente.",
                servletRequest
        ));
    }

    @PostMapping("/ventas/preview")
    @Operation(summary = "Previsualizar venta física")
    public ResponseEntity<ApiResponseDto<VentaCalculoPreviewResponseDto>> previsualizarVentaFisica(
            @Valid @RequestBody VentaCalculoPreviewRequestDto request,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        VentaCalculoPreviewResponseDto data = ventaFisicaService.previsualizarVentaFisica(request, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Previsualización de venta física calculada correctamente.",
                servletRequest
        ));
    }
}