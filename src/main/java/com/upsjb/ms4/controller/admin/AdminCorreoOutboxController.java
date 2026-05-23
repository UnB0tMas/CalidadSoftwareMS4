// ruta: src/main/java/com/upsjb/ms4/controller/admin/AdminCorreoOutboxController.java
package com.upsjb.ms4.controller.admin;

import com.upsjb.ms4.config.SwaggerSecurityConfig;
import com.upsjb.ms4.dto.mail.filter.CorreoOutboxFilterDto;
import com.upsjb.ms4.dto.mail.request.CorreoOutboxReintentoRequestDto;
import com.upsjb.ms4.dto.mail.response.CorreoOutboxResponseDto;
import com.upsjb.ms4.dto.shared.ApiResponseDto;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.mail.CorreoOutboxService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping(ApiPaths.ADMIN_CORREOS_OUTBOX)
@Tag(name = "Admin - Outbox", description = "Gestión administrativa de correo outbox.")
@SecurityRequirement(name = SwaggerSecurityConfig.BEARER_AUTH)
public class AdminCorreoOutboxController {

    private final CorreoOutboxService correoOutboxService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final ApiResponseFactory responseFactory;

    public AdminCorreoOutboxController(CorreoOutboxService correoOutboxService,
                                       AuthenticatedUserResolver authenticatedUserResolver,
                                       ApiResponseFactory responseFactory) {
        this.correoOutboxService = correoOutboxService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    @Operation(summary = "Listar correos Outbox")
    public ResponseEntity<ApiResponseDto<PageResponseDto<CorreoOutboxResponseDto>>> listarCorreos(
            @ParameterObject @Valid @ModelAttribute CorreoOutboxFilterDto filter,
            @ParameterObject @Valid @ModelAttribute PageRequestDto page,
            HttpServletRequest servletRequest
    ) {
        authenticatedUserResolver.current();
        PageResponseDto<CorreoOutboxResponseDto> data = correoOutboxService.listar(filter, page);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Correos Outbox consultados correctamente.",
                servletRequest
        ));
    }

    @PostMapping("/{id}/reintentar")
    @Operation(summary = "Reintentar correo Outbox")
    public ResponseEntity<ApiResponseDto<CorreoOutboxResponseDto>> reintentarCorreo(
            @PathVariable @Positive(message = "El id del correo Outbox debe ser positivo.") Long id,
            @Valid @RequestBody(required = false) CorreoOutboxReintentoRequestDto request,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        CorreoOutboxResponseDto data = correoOutboxService.reintentar(id, request, actor);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseFactory.accepted(
                data,
                "Correo Outbox reprogramado correctamente.",
                servletRequest
        ));
    }

    @PostMapping("/{id}/descartar")
    @Operation(summary = "Descartar correo Outbox")
    public ResponseEntity<ApiResponseDto<CorreoOutboxResponseDto>> descartarCorreo(
            @PathVariable @Positive(message = "El id del correo Outbox debe ser positivo.") Long id,
            @Valid @RequestBody EstadoChangeRequestDto request,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        CorreoOutboxResponseDto data = correoOutboxService.descartar(id, request, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Correo Outbox descartado correctamente.",
                servletRequest
        ));
    }
}