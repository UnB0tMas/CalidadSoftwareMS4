// ruta: src/main/java/com/upsjb/ms4/controller/admin/AdminSnapshotController.java
package com.upsjb.ms4.controller.admin;

import com.upsjb.ms4.config.SwaggerSecurityConfig;
import com.upsjb.ms4.dto.shared.ApiResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.dto.snapshot.filter.ClienteSnapshotFilterDto;
import com.upsjb.ms4.dto.snapshot.filter.EmpleadoSnapshotFilterDto;
import com.upsjb.ms4.dto.snapshot.filter.ProductoVentaFilterDto;
import com.upsjb.ms4.dto.snapshot.filter.StockVentaFilterDto;
import com.upsjb.ms4.dto.snapshot.response.ClienteSnapshotResponseDto;
import com.upsjb.ms4.dto.snapshot.response.EmpleadoSnapshotResponseDto;
import com.upsjb.ms4.dto.snapshot.response.ProductoVentaResponseDto;
import com.upsjb.ms4.dto.snapshot.response.StockVentaResponseDto;
import com.upsjb.ms4.policy.SnapshotPolicy;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.snapshot.CatalogoVentaSnapshotService;
import com.upsjb.ms4.service.contract.snapshot.ClienteSnapshotService;
import com.upsjb.ms4.service.contract.snapshot.EmpleadoSnapshotService;
import com.upsjb.ms4.service.contract.snapshot.StockSnapshotService;
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

@RestController
@Validated
@RequestMapping(ApiPaths.ADMIN_SNAPSHOTS)
@Tag(name = "Admin - Snapshots", description = "Consulta administrativa de snapshots locales MS2/MS3.")
@SecurityRequirement(name = SwaggerSecurityConfig.BEARER_AUTH)
public class AdminSnapshotController {

    private final ClienteSnapshotService clienteSnapshotService;
    private final EmpleadoSnapshotService empleadoSnapshotService;
    private final CatalogoVentaSnapshotService catalogoVentaSnapshotService;
    private final StockSnapshotService stockSnapshotService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final SnapshotPolicy snapshotPolicy;
    private final ApiResponseFactory responseFactory;

    public AdminSnapshotController(ClienteSnapshotService clienteSnapshotService,
                                   EmpleadoSnapshotService empleadoSnapshotService,
                                   CatalogoVentaSnapshotService catalogoVentaSnapshotService,
                                   StockSnapshotService stockSnapshotService,
                                   AuthenticatedUserResolver authenticatedUserResolver,
                                   SnapshotPolicy snapshotPolicy,
                                   ApiResponseFactory responseFactory) {
        this.clienteSnapshotService = clienteSnapshotService;
        this.empleadoSnapshotService = empleadoSnapshotService;
        this.catalogoVentaSnapshotService = catalogoVentaSnapshotService;
        this.stockSnapshotService = stockSnapshotService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.snapshotPolicy = snapshotPolicy;
        this.responseFactory = responseFactory;
    }

    @GetMapping("/clientes")
    @Operation(summary = "Listar snapshots de clientes")
    public ResponseEntity<ApiResponseDto<PageResponseDto<ClienteSnapshotResponseDto>>> listarClientesSnapshot(
            @ParameterObject @Valid @ModelAttribute ClienteSnapshotFilterDto filter,
            @ParameterObject @Valid @ModelAttribute PageRequestDto page,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        snapshotPolicy.authorizeConsultarSnapshotsAdmin(actor);

        PageResponseDto<ClienteSnapshotResponseDto> data = clienteSnapshotService.listar(filter, page);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Snapshots de clientes consultados correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/empleados")
    @Operation(summary = "Listar snapshots de empleados")
    public ResponseEntity<ApiResponseDto<PageResponseDto<EmpleadoSnapshotResponseDto>>> listarEmpleadosSnapshot(
            @ParameterObject @Valid @ModelAttribute EmpleadoSnapshotFilterDto filter,
            @ParameterObject @Valid @ModelAttribute PageRequestDto page,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        snapshotPolicy.authorizeConsultarSnapshotsAdmin(actor);

        PageResponseDto<EmpleadoSnapshotResponseDto> data = empleadoSnapshotService.listar(filter, page);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Snapshots de empleados consultados correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/productos")
    @Operation(summary = "Listar snapshots de productos disponibles para venta")
    public ResponseEntity<ApiResponseDto<PageResponseDto<ProductoVentaResponseDto>>> listarProductosSnapshot(
            @ParameterObject @Valid @ModelAttribute ProductoVentaFilterDto filter,
            @ParameterObject @Valid @ModelAttribute PageRequestDto page,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        snapshotPolicy.authorizeConsultarSnapshotsAdmin(actor);

        PageResponseDto<ProductoVentaResponseDto> data =
                catalogoVentaSnapshotService.listarProductosVendibles(filter, page);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Snapshots de productos consultados correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/stocks")
    @Operation(summary = "Listar snapshots de stock")
    public ResponseEntity<ApiResponseDto<PageResponseDto<StockVentaResponseDto>>> listarStocksSnapshot(
            @ParameterObject @Valid @ModelAttribute StockVentaFilterDto filter,
            @ParameterObject @Valid @ModelAttribute PageRequestDto page,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        snapshotPolicy.authorizeConsultarSnapshotsAdmin(actor);

        PageResponseDto<StockVentaResponseDto> data = stockSnapshotService.listar(filter, page);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Snapshots de stock consultados correctamente.",
                servletRequest
        ));
    }
}