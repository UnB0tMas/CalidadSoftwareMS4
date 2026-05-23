// ruta: src/main/java/com/upsjb/ms4/controller/empleado/EmpleadoClienteController.java
package com.upsjb.ms4.controller.empleado;

import com.upsjb.ms4.config.SwaggerSecurityConfig;
import com.upsjb.ms4.dto.lookup.ClienteLookupResponseDto;
import com.upsjb.ms4.dto.lookup.LookupFilterDto;
import com.upsjb.ms4.dto.shared.ApiResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.dto.snapshot.filter.ClienteSnapshotFilterDto;
import com.upsjb.ms4.dto.snapshot.response.ClienteSnapshotResponseDto;
import com.upsjb.ms4.policy.SnapshotPolicy;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.snapshot.ClienteSnapshotService;
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
@RequestMapping(ApiPaths.EMPLEADO_CLIENTES)
@Tag(name = "Empleado - Clientes", description = "Consulta de clientes sincronizados desde MS2 para venta física.")
@SecurityRequirement(name = SwaggerSecurityConfig.BEARER_AUTH)
public class EmpleadoClienteController {

    private final ClienteSnapshotService clienteSnapshotService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final SnapshotPolicy snapshotPolicy;
    private final ApiResponseFactory responseFactory;

    public EmpleadoClienteController(ClienteSnapshotService clienteSnapshotService,
                                     AuthenticatedUserResolver authenticatedUserResolver,
                                     SnapshotPolicy snapshotPolicy,
                                     ApiResponseFactory responseFactory) {
        this.clienteSnapshotService = clienteSnapshotService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.snapshotPolicy = snapshotPolicy;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    @Operation(summary = "Listar clientes sincronizados desde MS2")
    public ResponseEntity<ApiResponseDto<PageResponseDto<ClienteSnapshotResponseDto>>> listarClientesSnapshot(
            @ParameterObject @Valid @ModelAttribute ClienteSnapshotFilterDto filter,
            @ParameterObject @Valid @ModelAttribute PageRequestDto page,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        snapshotPolicy.authorizeConsultarSnapshots(actor);

        PageResponseDto<ClienteSnapshotResponseDto> data = clienteSnapshotService.listar(filter, page);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Clientes sincronizados consultados correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/lookup")
    @Operation(summary = "Buscar clientes para selector de venta física")
    public ResponseEntity<ApiResponseDto<PageResponseDto<ClienteLookupResponseDto>>> lookupClientes(
            @ParameterObject @Valid @ModelAttribute LookupFilterDto filter,
            @ParameterObject @Valid @ModelAttribute PageRequestDto page,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        snapshotPolicy.authorizeConsultarSnapshots(actor);

        PageResponseDto<ClienteLookupResponseDto> data = clienteSnapshotService.lookup(filter, page);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Lookup de clientes consultado correctamente.",
                servletRequest
        ));
    }
}