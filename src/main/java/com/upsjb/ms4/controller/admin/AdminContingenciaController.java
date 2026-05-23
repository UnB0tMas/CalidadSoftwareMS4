// ruta: src/main/java/com/upsjb/ms4/controller/admin/AdminContingenciaController.java
package com.upsjb.ms4.controller.admin;

import com.upsjb.ms4.config.SwaggerSecurityConfig;
import com.upsjb.ms4.dto.contingencia.filter.InventarioEventoPendienteFilterDto;
import com.upsjb.ms4.dto.contingencia.request.ContingenciaActivarRequestDto;
import com.upsjb.ms4.dto.contingencia.request.ContingenciaFinalizarRequestDto;
import com.upsjb.ms4.dto.contingencia.response.ContingenciaReconciliacionResponseDto;
import com.upsjb.ms4.dto.contingencia.response.InventarioEventoPendienteResponseDto;
import com.upsjb.ms4.dto.contingencia.response.ModoContingenciaResponseDto;
import com.upsjb.ms4.dto.shared.ApiResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.contingencia.ContingenciaService;
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
@RequestMapping(ApiPaths.ADMIN_CONTINGENCIA)
@Tag(name = "Admin - Contingencia", description = "Gestión administrativa de contingencia y reconciliación de inventario.")
@SecurityRequirement(name = SwaggerSecurityConfig.BEARER_AUTH)
public class AdminContingenciaController {

    private final ContingenciaService contingenciaService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final ApiResponseFactory responseFactory;

    public AdminContingenciaController(ContingenciaService contingenciaService,
                                       AuthenticatedUserResolver authenticatedUserResolver,
                                       ApiResponseFactory responseFactory) {
        this.contingenciaService = contingenciaService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.responseFactory = responseFactory;
    }

    @PostMapping("/activar")
    @Operation(summary = "Activar modo contingencia")
    public ResponseEntity<ApiResponseDto<ModoContingenciaResponseDto>> activarContingencia(
            @Valid @RequestBody ContingenciaActivarRequestDto request,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        ModoContingenciaResponseDto data = contingenciaService.activarContingencia(request, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Modo contingencia activado correctamente.",
                servletRequest
        ));
    }

    @PostMapping("/finalizar")
    @Operation(summary = "Finalizar modo contingencia")
    public ResponseEntity<ApiResponseDto<ModoContingenciaResponseDto>> finalizarContingencia(
            @Valid @RequestBody ContingenciaFinalizarRequestDto request,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        ModoContingenciaResponseDto data = contingenciaService.finalizarContingencia(request, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Modo contingencia finalizado correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/actual")
    @Operation(summary = "Obtener contingencia actual")
    public ResponseEntity<ApiResponseDto<ModoContingenciaResponseDto>> obtenerContingenciaActual(
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        ModoContingenciaResponseDto data = contingenciaService.obtenerContingenciaActual(actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Contingencia actual consultada correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/eventos-pendientes")
    @Operation(summary = "Listar eventos pendientes de inventario")
    public ResponseEntity<ApiResponseDto<PageResponseDto<InventarioEventoPendienteResponseDto>>> listarEventosPendientes(
            @ParameterObject @Valid @ModelAttribute InventarioEventoPendienteFilterDto filter,
            @ParameterObject @Valid @ModelAttribute PageRequestDto page,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        PageResponseDto<InventarioEventoPendienteResponseDto> data =
                contingenciaService.listarEventosPendientes(filter, page, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Eventos pendientes de inventario consultados correctamente.",
                servletRequest
        ));
    }

    @PostMapping("/reconciliar")
    @Operation(summary = "Reconciliar eventos pendientes de inventario")
    public ResponseEntity<ApiResponseDto<ContingenciaReconciliacionResponseDto>> reconciliarEventosPendientes(
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        ContingenciaReconciliacionResponseDto data = contingenciaService.reconciliarEventosPendientes(actor);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseFactory.accepted(
                data,
                "Reconciliación de eventos pendientes solicitada correctamente.",
                servletRequest
        ));
    }
}