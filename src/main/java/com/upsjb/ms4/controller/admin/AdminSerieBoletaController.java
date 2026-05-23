// ruta: src/main/java/com/upsjb/ms4/controller/admin/AdminSerieBoletaController.java
package com.upsjb.ms4.controller.admin;

import com.upsjb.ms4.config.SwaggerSecurityConfig;
import com.upsjb.ms4.dto.config.filter.SerieBoletaFilterDto;
import com.upsjb.ms4.dto.config.request.SerieBoletaCreateRequestDto;
import com.upsjb.ms4.dto.config.response.SerieBoletaResponseDto;
import com.upsjb.ms4.dto.shared.ApiResponseDto;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.config.SerieBoletaService;
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
@RequestMapping(ApiPaths.ADMIN_SERIES_BOLETA)
@Tag(name = "Admin - Configuración", description = "Gestión administrativa de series de boleta.")
@SecurityRequirement(name = SwaggerSecurityConfig.BEARER_AUTH)
public class AdminSerieBoletaController {

    private final SerieBoletaService serieBoletaService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final ApiResponseFactory responseFactory;

    public AdminSerieBoletaController(SerieBoletaService serieBoletaService,
                                      AuthenticatedUserResolver authenticatedUserResolver,
                                      ApiResponseFactory responseFactory) {
        this.serieBoletaService = serieBoletaService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.responseFactory = responseFactory;
    }

    @PostMapping
    @Operation(summary = "Crear serie de boleta")
    public ResponseEntity<ApiResponseDto<SerieBoletaResponseDto>> crearSerie(
            @Valid @RequestBody SerieBoletaCreateRequestDto request,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        SerieBoletaResponseDto data = serieBoletaService.crearSerie(request, actor);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseFactory.created(
                data,
                "Serie de boleta creada correctamente.",
                servletRequest
        ));
    }

    @GetMapping
    @Operation(summary = "Listar series de boleta")
    public ResponseEntity<ApiResponseDto<PageResponseDto<SerieBoletaResponseDto>>> listarSeries(
            @ParameterObject @Valid @ModelAttribute SerieBoletaFilterDto filter,
            @ParameterObject @Valid @ModelAttribute PageRequestDto page,
            HttpServletRequest servletRequest
    ) {
        authenticatedUserResolver.current();
        PageResponseDto<SerieBoletaResponseDto> data = serieBoletaService.listar(filter, page);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Series de boleta consultadas correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener serie de boleta")
    public ResponseEntity<ApiResponseDto<SerieBoletaResponseDto>> obtenerSerie(
            @PathVariable @Positive(message = "El id de serie debe ser positivo.") Long id,
            HttpServletRequest servletRequest
    ) {
        authenticatedUserResolver.current();
        SerieBoletaResponseDto data = serieBoletaService.obtenerPorId(id);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Serie de boleta consultada correctamente.",
                servletRequest
        ));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de serie de boleta")
    public ResponseEntity<ApiResponseDto<SerieBoletaResponseDto>> cambiarEstado(
            @PathVariable @Positive(message = "El id de serie debe ser positivo.") Long id,
            @Valid @RequestBody EstadoChangeRequestDto request,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        SerieBoletaResponseDto data = serieBoletaService.cambiarEstado(id, request, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Estado de serie de boleta actualizado correctamente.",
                servletRequest
        ));
    }
}