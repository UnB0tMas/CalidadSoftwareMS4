package com.upsjb.ms4.controller.admin;

import com.upsjb.ms4.config.SwaggerSecurityConfig;
import com.upsjb.ms4.dto.config.filter.ConfiguracionEmpresaFilterDto;
import com.upsjb.ms4.dto.config.request.AssetCloudinaryUploadRequestDto;
import com.upsjb.ms4.dto.config.request.ConfiguracionEmpresaRequestDto;
import com.upsjb.ms4.dto.config.response.AssetCloudinaryResponseDto;
import com.upsjb.ms4.dto.config.response.ConfiguracionEmpresaResponseDto;
import com.upsjb.ms4.dto.shared.ApiResponseDto;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.config.AssetCloudinaryService;
import com.upsjb.ms4.service.contract.config.ConfiguracionEmpresaService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Validated
@RequestMapping(ApiPaths.ADMIN_CONFIGURACION + "/empresa")
@Tag(name = "Admin - Configuración", description = "Gestión de configuración empresarial y assets visuales.")
@SecurityRequirement(name = SwaggerSecurityConfig.BEARER_AUTH)
public class AdminConfiguracionEmpresaController {

    private final ConfiguracionEmpresaService configuracionEmpresaService;
    private final AssetCloudinaryService assetCloudinaryService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final ApiResponseFactory responseFactory;

    public AdminConfiguracionEmpresaController(ConfiguracionEmpresaService configuracionEmpresaService,
                                               AssetCloudinaryService assetCloudinaryService,
                                               AuthenticatedUserResolver authenticatedUserResolver,
                                               ApiResponseFactory responseFactory) {
        this.configuracionEmpresaService = configuracionEmpresaService;
        this.assetCloudinaryService = assetCloudinaryService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.responseFactory = responseFactory;
    }

    @PostMapping("/versiones")
    @Operation(summary = "Crear nueva versión de configuración empresarial")
    public ResponseEntity<ApiResponseDto<ConfiguracionEmpresaResponseDto>> crearNuevaVersion(
            @Valid @RequestBody ConfiguracionEmpresaRequestDto request,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        ConfiguracionEmpresaResponseDto data = configuracionEmpresaService.crearNuevaVersion(request, actor);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseFactory.created(
                data,
                "Versión de configuración empresarial creada correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/vigente")
    @Operation(summary = "Obtener configuración empresarial vigente")
    public ResponseEntity<ApiResponseDto<ConfiguracionEmpresaResponseDto>> obtenerVigente(
            HttpServletRequest servletRequest
    ) {
        authenticatedUserResolver.current();
        ConfiguracionEmpresaResponseDto data = configuracionEmpresaService.obtenerVersionVigente();

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Configuración empresarial vigente consultada correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/versiones")
    @Operation(summary = "Listar versiones de configuración empresarial")
    public ResponseEntity<ApiResponseDto<PageResponseDto<ConfiguracionEmpresaResponseDto>>> listarVersiones(
            @ParameterObject @Valid @ModelAttribute ConfiguracionEmpresaFilterDto filter,
            @ParameterObject @Valid @ModelAttribute PageRequestDto page,
            HttpServletRequest servletRequest
    ) {
        authenticatedUserResolver.current();
        PageResponseDto<ConfiguracionEmpresaResponseDto> data =
                configuracionEmpresaService.listarVersiones(filter, page);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Versiones de configuración empresarial consultadas correctamente.",
                servletRequest
        ));
    }

    @PostMapping("/versiones/{id}/activar")
    @Operation(summary = "Activar versión de configuración empresarial")
    public ResponseEntity<ApiResponseDto<ConfiguracionEmpresaResponseDto>> activarVersion(
            @PathVariable("id") @Positive(message = "El id de versión debe ser positivo.") Long id,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        ConfiguracionEmpresaResponseDto data = configuracionEmpresaService.activarVersion(id, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Versión de configuración empresarial activada correctamente.",
                servletRequest
        ));
    }

    @PatchMapping("/versiones/{id}/estado")
    @Operation(summary = "Cambiar estado de versión de configuración empresarial")
    public ResponseEntity<ApiResponseDto<ConfiguracionEmpresaResponseDto>> cambiarEstado(
            @PathVariable("id") @Positive(message = "El id de versión debe ser positivo.") Long id,
            @Valid @RequestBody EstadoChangeRequestDto request,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        ConfiguracionEmpresaResponseDto data = configuracionEmpresaService.cambiarEstado(id, request, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Estado de versión empresarial actualizado correctamente.",
                servletRequest
        ));
    }

    @PostMapping(value = "/assets", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir asset visual de configuración empresarial")
    public ResponseEntity<ApiResponseDto<AssetCloudinaryResponseDto>> subirAssetVisual(
            @Valid @ModelAttribute AssetCloudinaryUploadRequestDto request,
            @RequestPart("file") MultipartFile file,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        AssetCloudinaryResponseDto data = assetCloudinaryService.subirAssetVisual(request, file, actor);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseFactory.created(
                data,
                "Asset visual subido correctamente.",
                servletRequest
        ));
    }
}