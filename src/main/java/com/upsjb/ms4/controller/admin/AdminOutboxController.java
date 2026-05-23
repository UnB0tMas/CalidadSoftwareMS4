// ruta: src/main/java/com/upsjb/ms4/controller/admin/AdminOutboxController.java
package com.upsjb.ms4.controller.admin;

import com.upsjb.ms4.config.SwaggerSecurityConfig;
import com.upsjb.ms4.dto.kafka.filter.EventoDominioOutboxFilterDto;
import com.upsjb.ms4.dto.kafka.response.EventoDominioOutboxResponseDto;
import com.upsjb.ms4.dto.shared.ApiResponseDto;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.kafka.EventoDominioOutboxService;
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
@RequestMapping(ApiPaths.ADMIN_OUTBOX)
@Tag(name = "Admin - Outbox", description = "Gestión administrativa de eventos Outbox Kafka.")
@SecurityRequirement(name = SwaggerSecurityConfig.BEARER_AUTH)
public class AdminOutboxController {

    private final EventoDominioOutboxService eventoDominioOutboxService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final ApiResponseFactory responseFactory;

    public AdminOutboxController(EventoDominioOutboxService eventoDominioOutboxService,
                                 AuthenticatedUserResolver authenticatedUserResolver,
                                 ApiResponseFactory responseFactory) {
        this.eventoDominioOutboxService = eventoDominioOutboxService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    @Operation(summary = "Listar eventos Outbox")
    public ResponseEntity<ApiResponseDto<PageResponseDto<EventoDominioOutboxResponseDto>>> listarOutbox(
            @ParameterObject @Valid @ModelAttribute EventoDominioOutboxFilterDto filter,
            @ParameterObject @Valid @ModelAttribute PageRequestDto page,
            HttpServletRequest servletRequest
    ) {
        authenticatedUserResolver.current();
        PageResponseDto<EventoDominioOutboxResponseDto> data =
                eventoDominioOutboxService.listar(filter, page);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Eventos Outbox consultados correctamente.",
                servletRequest
        ));
    }

    @PostMapping("/{id}/reintentar")
    @Operation(summary = "Reintentar evento Outbox")
    public ResponseEntity<ApiResponseDto<EventoDominioOutboxResponseDto>> reintentarEvento(
            @PathVariable @Positive(message = "El id del evento Outbox debe ser positivo.") Long id,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        EventoDominioOutboxResponseDto data = eventoDominioOutboxService.reintentar(id, actor);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseFactory.accepted(
                data,
                "Evento Outbox reprogramado correctamente.",
                servletRequest
        ));
    }

    @PostMapping("/{id}/descartar")
    @Operation(summary = "Descartar evento Outbox")
    public ResponseEntity<ApiResponseDto<EventoDominioOutboxResponseDto>> descartarEvento(
            @PathVariable @Positive(message = "El id del evento Outbox debe ser positivo.") Long id,
            @Valid @RequestBody EstadoChangeRequestDto request,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        EventoDominioOutboxResponseDto data = eventoDominioOutboxService.descartar(id, request, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Evento Outbox descartado correctamente.",
                servletRequest
        ));
    }
}