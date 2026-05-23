// ruta: src/main/java/com/upsjb/ms4/controller/admin/AdminAuditoriaController.java
package com.upsjb.ms4.controller.admin;

import com.upsjb.ms4.config.SwaggerSecurityConfig;
import com.upsjb.ms4.dto.auditoria.filter.AuditoriaFilterDto;
import com.upsjb.ms4.dto.auditoria.response.AuditoriaResponseDto;
import com.upsjb.ms4.dto.shared.ApiResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.auditoria.AuditoriaFuncionalService;
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
@RequestMapping(ApiPaths.ADMIN_AUDITORIA)
@Tag(name = "Admin - Auditoría", description = "Consulta administrativa de auditoría funcional.")
@SecurityRequirement(name = SwaggerSecurityConfig.BEARER_AUTH)
public class AdminAuditoriaController {

    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final ApiResponseFactory responseFactory;

    public AdminAuditoriaController(AuditoriaFuncionalService auditoriaFuncionalService,
                                    AuthenticatedUserResolver authenticatedUserResolver,
                                    ApiResponseFactory responseFactory) {
        this.auditoriaFuncionalService = auditoriaFuncionalService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    @Operation(summary = "Listar auditoría funcional")
    public ResponseEntity<ApiResponseDto<PageResponseDto<AuditoriaResponseDto>>> listarAuditoria(
            @ParameterObject @Valid @ModelAttribute AuditoriaFilterDto filter,
            @ParameterObject @Valid @ModelAttribute PageRequestDto page,
            HttpServletRequest servletRequest
    ) {
        authenticatedUserResolver.current();
        PageResponseDto<AuditoriaResponseDto> data = auditoriaFuncionalService.listar(filter, page);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Auditoría funcional consultada correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener auditoría funcional")
    public ResponseEntity<ApiResponseDto<AuditoriaResponseDto>> obtenerAuditoria(
            @PathVariable @Positive(message = "El id de auditoría debe ser positivo.") Long id,
            HttpServletRequest servletRequest
    ) {
        authenticatedUserResolver.current();
        AuditoriaResponseDto data = auditoriaFuncionalService.obtenerPorId(id);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Auditoría funcional consultada correctamente.",
                servletRequest
        ));
    }
}