package com.upsjb.ms4.controller.empleado;

import com.upsjb.ms4.config.SwaggerSecurityConfig;
import com.upsjb.ms4.dto.pago.request.PagoEfectivoRequestDto;
import com.upsjb.ms4.dto.pago.request.PagoStripePresencialRequestDto;
import com.upsjb.ms4.dto.pago.response.StripePaymentIntentResponseDto;
import com.upsjb.ms4.dto.shared.ApiResponseDto;
import com.upsjb.ms4.dto.venta.response.VentaDetailResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.pago.StripePaymentService;
import com.upsjb.ms4.service.contract.venta.VentaFisicaService;
import com.upsjb.ms4.shared.constants.ApiPaths;
import com.upsjb.ms4.shared.response.ApiResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping(ApiPaths.EMPLEADO_VENTAS)
@Tag(name = "Empleado - Pagos", description = "Pagos presenciales para ventas físicas.")
@SecurityRequirement(name = SwaggerSecurityConfig.BEARER_AUTH)
public class EmpleadoPagoController {

    private final VentaFisicaService ventaFisicaService;
    private final StripePaymentService stripePaymentService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final ApiResponseFactory responseFactory;

    public EmpleadoPagoController(VentaFisicaService ventaFisicaService,
                                  StripePaymentService stripePaymentService,
                                  AuthenticatedUserResolver authenticatedUserResolver,
                                  ApiResponseFactory responseFactory) {
        this.ventaFisicaService = ventaFisicaService;
        this.stripePaymentService = stripePaymentService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.responseFactory = responseFactory;
    }

    @PostMapping("/{idVenta}/pago-efectivo")
    @Operation(summary = "Registrar pago efectivo de venta física")
    public ResponseEntity<ApiResponseDto<VentaDetailResponseDto>> registrarPagoEfectivo(
            @PathVariable @Positive(message = "El idVenta debe ser positivo.") Long idVenta,
            @Valid @RequestBody PagoEfectivoRequestDto request,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        VentaDetailResponseDto data =
                ventaFisicaService.confirmarVentaFisicaConPagoEfectivo(idVenta, request, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Pago efectivo registrado y venta física confirmada correctamente.",
                servletRequest
        ));
    }

    @PostMapping("/{idVenta}/pago-tarjeta-stripe")
    @Operation(summary = "Crear PaymentIntent presencial en Stripe Sandbox")
    public ResponseEntity<ApiResponseDto<StripePaymentIntentResponseDto>> crearPaymentIntentPresencial(
            @PathVariable @Positive(message = "El idVenta debe ser positivo.") Long idVenta,
            @Valid @RequestBody PagoStripePresencialRequestDto request,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        StripePaymentIntentResponseDto data =
                stripePaymentService.crearPaymentIntentPresencial(idVenta, request, actor);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseFactory.created(
                data,
                "PaymentIntent presencial creado correctamente en Stripe Sandbox.",
                servletRequest
        ));
    }
}