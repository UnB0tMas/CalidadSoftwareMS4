package com.upsjb.ms4.controller.admin;

import com.upsjb.ms4.config.SwaggerSecurityConfig;
import com.upsjb.ms4.dto.config.filter.ConfiguracionTributariaFilterDto;
import com.upsjb.ms4.dto.config.request.ConfiguracionTributariaRequestDto;
import com.upsjb.ms4.dto.config.response.ConfiguracionTributariaResponseDto;
import com.upsjb.ms4.dto.shared.ApiResponseDto;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.config.ConfiguracionTributariaService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping(ApiPaths.ADMIN_CONFIGURACION + "/tributaria/igv")
@Tag(name = "Admin - Configuración", description = "Gestión de configuración tributaria IGV.")
@SecurityRequirement(name = SwaggerSecurityConfig.BEARER_AUTH)
public class AdminConfiguracionTributariaController {

    private final ConfiguracionTributariaService configuracionTributariaService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final ApiResponseFactory responseFactory;

    public AdminConfiguracionTributariaController(ConfiguracionTributariaService configuracionTributariaService,
                                                  AuthenticatedUserResolver authenticatedUserResolver,
                                                  ApiResponseFactory responseFactory) {
        this.configuracionTributariaService = configuracionTributariaService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.responseFactory = responseFactory;
    }

    @PostMapping("/versiones")
    @Operation(summary = "Crear nueva versión de IGV")
    public ResponseEntity<ApiResponseDto<ConfiguracionTributariaResponseDto>> crearNuevaVersionIgv(
            @Valid @RequestBody ConfiguracionTributariaRequestDto request,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        ConfiguracionTributariaResponseDto data =
                configuracionTributariaService.crearNuevaVersionIgv(request, actor);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseFactory.created(
                data,
                "Versión de IGV creada correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/vigente")
    @Operation(summary = "Obtener IGV vigente")
    public ResponseEntity<ApiResponseDto<ConfiguracionTributariaResponseDto>> obtenerIgvVigente(
            HttpServletRequest servletRequest
    ) {
        authenticatedUserResolver.current();
        ConfiguracionTributariaResponseDto data = configuracionTributariaService.obtenerIgvVigente();

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "IGV vigente consultado correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/versiones")
    @Operation(summary = "Listar versiones de IGV")
    public ResponseEntity<ApiResponseDto<PageResponseDto<ConfiguracionTributariaResponseDto>>> listarVersiones(
            @ParameterObject @Valid @ModelAttribute ConfiguracionTributariaFilterDto filter,
            @ParameterObject @Valid @ModelAttribute PageRequestDto page,
            HttpServletRequest servletRequest
    ) {
        authenticatedUserResolver.current();
        PageResponseDto<ConfiguracionTributariaResponseDto> data =
                configuracionTributariaService.listarVersiones(filter, page);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Versiones de IGV consultadas correctamente.",
                servletRequest
        ));
    }

    @PostMapping("/versiones/{id}/activar")
    @Operation(summary = "Activar versión de IGV")
    public ResponseEntity<ApiResponseDto<ConfiguracionTributariaResponseDto>> activarVersionIgv(
            @PathVariable("id") @Positive(message = "El id de versión debe ser positivo.") Long id,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        ConfiguracionTributariaResponseDto data = configuracionTributariaService.activarVersionIgv(id, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Versión de IGV activada correctamente.",
                servletRequest
        ));
    }

    @PatchMapping("/versiones/{id}/estado")
    @Operation(summary = "Cambiar estado de versión de IGV")
    public ResponseEntity<ApiResponseDto<ConfiguracionTributariaResponseDto>> cambiarEstado(
            @PathVariable("id") @Positive(message = "El id de versión debe ser positivo.") Long id,
            @Valid @RequestBody EstadoChangeRequestDto request,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        ConfiguracionTributariaResponseDto data =
                configuracionTributariaService.cambiarEstado(id, request, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Estado de versión tributaria actualizado correctamente.",
                servletRequest
        ));
    }
}