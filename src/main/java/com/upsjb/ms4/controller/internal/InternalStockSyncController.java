package com.upsjb.ms4.controller.internal;

import com.upsjb.ms4.config.SwaggerSecurityConfig;
import com.upsjb.ms4.dto.contingencia.filter.InventarioEventoPendienteFilterDto;
import com.upsjb.ms4.dto.contingencia.request.StockSyncMarkErrorRequestDto;
import com.upsjb.ms4.dto.contingencia.request.StockSyncMarkSyncedRequestDto;
import com.upsjb.ms4.dto.contingencia.request.StockSyncResultRequestDto;
import com.upsjb.ms4.dto.contingencia.response.InventarioEventoPendienteResponseDto;
import com.upsjb.ms4.dto.shared.ApiResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.service.contract.kafka.InventarioEventoPendienteService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping(ApiPaths.INTERNAL)
@Tag(name = "Internal - Stock Sync", description = "Rutas internas MS3 ↔ MS4 protegidas con X-Internal-Service-Key.")
@SecurityRequirement(name = SwaggerSecurityConfig.INTERNAL_KEY_AUTH)
public class InternalStockSyncController {

    private final InventarioEventoPendienteService inventarioEventoPendienteService;
    private final ApiResponseFactory responseFactory;

    public InternalStockSyncController(InventarioEventoPendienteService inventarioEventoPendienteService,
                                       ApiResponseFactory responseFactory) {
        this.inventarioEventoPendienteService = inventarioEventoPendienteService;
        this.responseFactory = responseFactory;
    }

    @GetMapping("/stock-events/pending")
    @Operation(summary = "Listar eventos de stock pendientes para sincronización interna con MS3")
    public ResponseEntity<ApiResponseDto<PageResponseDto<InventarioEventoPendienteResponseDto>>> listarPendientes(
            @ParameterObject @Valid @ModelAttribute InventarioEventoPendienteFilterDto filter,
            @ParameterObject @Valid @ModelAttribute PageRequestDto page,
            HttpServletRequest servletRequest
    ) {
        PageResponseDto<InventarioEventoPendienteResponseDto> data =
                inventarioEventoPendienteService.listarPendientesParaMs3(filter, page);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Eventos pendientes de inventario consultados correctamente.",
                servletRequest
        ));
    }

    @PostMapping("/stock-events/{id}/mark-synced")
    @Operation(summary = "Marcar evento de stock como sincronizado desde MS3")
    public ResponseEntity<ApiResponseDto<InventarioEventoPendienteResponseDto>> marcarSincronizado(
            @PathVariable("id") @Positive(message = "El id del evento pendiente debe ser positivo.") Long id,
            @Valid @RequestBody(required = false) StockSyncMarkSyncedRequestDto request,
            HttpServletRequest servletRequest
    ) {
        String detalleResultado = request == null ? null : request.detalleResultado();
        InventarioEventoPendienteResponseDto data =
                inventarioEventoPendienteService.marcarSincronizado(id, detalleResultado);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Evento de inventario marcado como sincronizado correctamente.",
                servletRequest
        ));
    }

    @PostMapping("/stock-events/{id}/mark-error")
    @Operation(summary = "Marcar evento de stock con error desde MS3")
    public ResponseEntity<ApiResponseDto<InventarioEventoPendienteResponseDto>> marcarError(
            @PathVariable("id") @Positive(message = "El id del evento pendiente debe ser positivo.") Long id,
            @Valid @RequestBody StockSyncMarkErrorRequestDto request,
            HttpServletRequest servletRequest
    ) {
        InventarioEventoPendienteResponseDto data =
                inventarioEventoPendienteService.marcarError(id, request.errorDetalle());

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Evento de inventario marcado con error correctamente.",
                servletRequest
        ));
    }

    @PostMapping("/stock-sync/result")
    @Operation(summary = "Registrar resultado de sincronización de stock informado por MS3")
    public ResponseEntity<ApiResponseDto<InventarioEventoPendienteResponseDto>> registrarResultadoSincronizacion(
            @Valid @RequestBody StockSyncResultRequestDto request,
            HttpServletRequest servletRequest
    ) {
        InventarioEventoPendienteResponseDto data =
                inventarioEventoPendienteService.registrarResultadoSincronizacion(request);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Resultado de sincronización de inventario registrado correctamente.",
                servletRequest
        ));
    }
}