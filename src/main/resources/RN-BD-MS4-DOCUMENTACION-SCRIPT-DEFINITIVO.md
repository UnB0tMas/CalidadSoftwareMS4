1. Resumen de entendimiento

Revisé los 3 archivos compartidos y el código unificado real del MS4. La RN de estructura actualizada prevalece cuando hay contradicción y define que MS4 trabaja con venta directa, sin carrito/checkout, con boleta, PDF en vivo, Stripe Sandbox, snapshots MS2/MS3 y comandos de stock por Outbox. También define que los controllers solo deben recibir HTTP, validar superficialmente, resolver actor, delegar a service y responder con `ApiResponseFactory`.

El problema concreto encontrado es que `AdminVentaController.java` existe, pero está vacío. Por eso no expone las rutas administrativas de ventas que la RN exige: listar ventas, obtener venta y anular venta. El resto de controllers del bloque solicitado ya sigue el patrón real del proyecto y no requiere regeneración.

---

2. Archivos revisados

* `RN-MS4-VENTAS-FACTURACION-DEFINITIVA.md`
* `RN-ESTRUCTURA-CLASES-MS4-DEFINITIVA-ACTUALIZADA.md`
* `codigo_unificado_springboot.txt`
* `src/main/java/com/upsjb/ms4/controller/admin/AdminVentaController.java`
* `src/main/java/com/upsjb/ms4/controller/admin/AdminBoletaController.java`
* `src/main/java/com/upsjb/ms4/controller/admin/AdminCajaController.java`
* `src/main/java/com/upsjb/ms4/controller/admin/AdminReporteController.java`
* `src/main/java/com/upsjb/ms4/controller/admin/AdminConfiguracionEmpresaController.java`
* `src/main/java/com/upsjb/ms4/controller/admin/AdminConfiguracionTributariaController.java`
* `src/main/java/com/upsjb/ms4/controller/admin/AdminSerieBoletaController.java`
* `src/main/java/com/upsjb/ms4/controller/admin/AdminOutboxController.java`
* `src/main/java/com/upsjb/ms4/controller/admin/AdminCorreoOutboxController.java`
* `src/main/java/com/upsjb/ms4/controller/admin/AdminContingenciaController.java`
* `src/main/java/com/upsjb/ms4/controller/admin/AdminSnapshotController.java`
* `src/main/java/com/upsjb/ms4/controller/admin/AdminAuditoriaController.java`
* `src/main/java/com/upsjb/ms4/controller/lookup/LookupController.java`
* `src/main/java/com/upsjb/ms4/controller/internal/InternalStockSyncController.java`
* `src/main/java/com/upsjb/ms4/controller/webhook/StripeWebhookController.java`
* `src/main/java/com/upsjb/ms4/service/contract/venta/VentaAdminService.java`
* `src/main/java/com/upsjb/ms4/service/contract/venta/VentaConsultaService.java`
* `src/main/java/com/upsjb/ms4/service/impl/venta/VentaAdminServiceImpl.java`
* `src/main/java/com/upsjb/ms4/service/impl/venta/VentaConsultaServiceImpl.java`
* `src/main/java/com/upsjb/ms4/shared/constants/ApiPaths.java`
* `src/main/java/com/upsjb/ms4/shared/response/ApiResponseFactory.java`
* `src/main/java/com/upsjb/ms4/security/principal/AuthenticatedUserResolver.java`
* `src/main/java/com/upsjb/ms4/config/SecurityConfig.java`
* `src/main/java/com/upsjb/ms4/config/SwaggerSecurityConfig.java`

---

3. Diagnóstico técnico

### Clases correctas que no requieren cambios

No regenero estas clases porque ya están alineadas con el patrón real del proyecto: `@RestController`, `@Validated`, `@RequestMapping(ApiPaths...)`, `AuthenticatedUserResolver`, `ApiResponseFactory`, `@Operation`, `@SecurityRequirement`, `@ParameterObject`, `@Valid`, DTOs de respuesta y servicios reales.

* `AdminBoletaController`
* `AdminCajaController`
* `AdminReporteController`
* `AdminConfiguracionEmpresaController`
* `AdminConfiguracionTributariaController`
* `AdminSerieBoletaController`
* `AdminOutboxController`
* `AdminCorreoOutboxController`
* `AdminContingenciaController`
* `AdminSnapshotController`
* `AdminAuditoriaController`
* `LookupController`
* `InternalStockSyncController`
* `StripeWebhookController`
* `ApiPaths`
* `SecurityConfig`
* `SwaggerSecurityConfig`
* `ApiResponseFactory`

### Clases con problemas o incompletas

* `AdminVentaController.java`: está vacío. No tiene `@RestController`, no tiene rutas, no usa `VentaConsultaService`, no usa `VentaAdminService`, no resuelve actor autenticado, no responde con `ApiResponseFactory`, no documenta Swagger y no expone las rutas administrativas exigidas por RN.

No fue necesario crear DTOs nuevos ni tocar `VentaAdminService`, `VentaConsultaService`, policies, validators o mappers porque ya existen los métodos requeridos.

---

4. Mapa de endpoints implementados

| Método HTTP | Ruta                                     | Método controller | Método service consumido                                      | DTO request/filter usado           | DTO response usado                  | Motivo funcional                                                              |
| ----------- | ---------------------------------------- | ----------------- | ------------------------------------------------------------- | ---------------------------------- | ----------------------------------- | ----------------------------------------------------------------------------- |
| GET         | `/api/ms4/admin/ventas`                  | `listarVentas`    | `ventaConsultaService.listarVentasAdmin(filter, page, actor)` | `VentaFilterDto`, `PageRequestDto` | `PageResponseDto<VentaResponseDto>` | Consulta administrativa paginada y filtrable de ventas.                       |
| GET         | `/api/ms4/admin/ventas/{idVenta}`        | `obtenerVenta`    | `ventaConsultaService.obtenerDetalleAdmin(idVenta, actor)`    | `idVenta`                          | `VentaDetailResponseDto`            | Obtener detalle administrativo de una venta.                                  |
| POST        | `/api/ms4/admin/ventas/{idVenta}/anular` | `anularVenta`     | `ventaAdminService.anularVenta(idVenta, request, actor)`      | `EstadoChangeRequestDto`           | `VentaDetailResponseDto`            | Anulación administrativa de venta con auditoría y comando de stock si aplica. |

---

5. Métodos service sin endpoint

| Método service                                                       | Motivo por el que no se expone en `AdminVentaController`                                            |
| -------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| `VentaConsultaService.listarVentasEmpleado(...)`                     | Pertenece al flujo de empleado, no al controller admin. Ya corresponde a `EmpleadoVentaController`. |
| `VentaConsultaService.listarVentasCliente(...)`                      | Pertenece al flujo de cliente, no al controller admin. Ya corresponde a `ClienteVentaController`.   |
| `VentaConsultaService.obtenerDetalleEmpleado(...)`                   | Consulta autorizada para empleado, no debe exponerse en rutas admin.                                |
| `VentaConsultaService.obtenerDetalleCliente(...)`                    | Consulta autorizada para cliente, no debe exponerse en rutas admin.                                 |
| `VentaConsultaService.resolverVentaParaProcesoInterno(Long idVenta)` | No se expone porque devuelve entidad JPA `Venta` y es resolver interno para services.               |
| `VentaAdminService.anularVenta(...)`                                 | Sí se expone porque representa acción administrativa explícita requerida por RN.                    |

---

6. Decisiones aplicadas

* Se reemplaza el stub vacío de `AdminVentaController`.
* Se usa `ApiPaths.ADMIN_VENTAS`; no se inventan rutas.
* Se usa `VentaConsultaService` para listar y obtener ventas.
* Se usa `VentaAdminService` solo para anular ventas.
* Se usa `AuthenticatedUserResolver.current()` para obtener el actor real.
* Se usa `ApiResponseFactory` para respuestas estandarizadas.
* No se usan repositories, mappers, validators, Kafka, Stripe, Cloudinary ni transacciones en controller.
* No se agrega `@PreAuthorize` porque la seguridad de ruta ya está centralizada en `SecurityConfig` y la autorización funcional se mantiene en services/policies.
* La anulación responde `200 OK` porque el service ejecuta la acción de forma sincrónica.
* No se agregan rutas de carrito, checkout, factura, notas, SUNAT, OSE ni XML.

---

7. Código completo actualizado

```java
// ruta: src/main/java/com/upsjb/ms4/controller/admin/AdminVentaController.java
package com.upsjb.ms4.controller.admin;

import com.upsjb.ms4.config.SwaggerSecurityConfig;
import com.upsjb.ms4.dto.shared.ApiResponseDto;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.dto.venta.filter.VentaFilterDto;
import com.upsjb.ms4.dto.venta.response.VentaDetailResponseDto;
import com.upsjb.ms4.dto.venta.response.VentaResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.venta.VentaAdminService;
import com.upsjb.ms4.service.contract.venta.VentaConsultaService;
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
@RequestMapping(ApiPaths.ADMIN_VENTAS)
@Tag(name = "Admin - Ventas", description = "Consulta y anulación administrativa de ventas.")
@SecurityRequirement(name = SwaggerSecurityConfig.BEARER_AUTH)
public class AdminVentaController {

    private final VentaConsultaService ventaConsultaService;
    private final VentaAdminService ventaAdminService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final ApiResponseFactory responseFactory;

    public AdminVentaController(VentaConsultaService ventaConsultaService,
                                VentaAdminService ventaAdminService,
                                AuthenticatedUserResolver authenticatedUserResolver,
                                ApiResponseFactory responseFactory) {
        this.ventaConsultaService = ventaConsultaService;
        this.ventaAdminService = ventaAdminService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    @Operation(summary = "Listar ventas administrativamente")
    public ResponseEntity<ApiResponseDto<PageResponseDto<VentaResponseDto>>> listarVentas(
            @ParameterObject @Valid @ModelAttribute VentaFilterDto filter,
            @ParameterObject @Valid @ModelAttribute PageRequestDto page,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        PageResponseDto<VentaResponseDto> data = ventaConsultaService.listarVentasAdmin(filter, page, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Ventas consultadas correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/{idVenta}")
    @Operation(summary = "Obtener detalle administrativo de venta")
    public ResponseEntity<ApiResponseDto<VentaDetailResponseDto>> obtenerVenta(
            @PathVariable @Positive(message = "El idVenta debe ser positivo.") Long idVenta,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        VentaDetailResponseDto data = ventaConsultaService.obtenerDetalleAdmin(idVenta, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Venta consultada correctamente.",
                servletRequest
        ));
    }

    @PostMapping("/{idVenta}/anular")
    @Operation(summary = "Anular venta administrativamente")
    public ResponseEntity<ApiResponseDto<VentaDetailResponseDto>> anularVenta(
            @PathVariable @Positive(message = "El idVenta debe ser positivo.") Long idVenta,
            @Valid @RequestBody EstadoChangeRequestDto request,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        VentaDetailResponseDto data = ventaAdminService.anularVenta(idVenta, request, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Venta anulada correctamente.",
                servletRequest
        ));
    }
}
```

---

8. Notas de integración

* Reemplaza completo el archivo vacío actual `AdminVentaController.java`.
* No cambies `ApiPaths`: ya existe `ADMIN_VENTAS = /api/ms4/admin/ventas`.
* No cambies `SecurityConfig`: las rutas `/api/ms4/admin/**` ya requieren rol `ADMIN`.
* Para anular venta, el request debe enviar `estado=false` y `motivo`, porque `VentaAdminServiceImpl` valida esa regla con `EstadoChangeRequestDto`.
* No se requiere crear service nuevo.
* No se requiere modificar `VentaAdminService`.
* No se requiere modificar `VentaConsultaService`.
* No se requiere modificar mappers, validators, policies ni repositories.

---

9. Checklist final

* [x] Revisé `RN-MS4-VENTAS-FACTURACION-DEFINITIVA.md`.
* [x] Revisé `RN-ESTRUCTURA-CLASES-MS4-DEFINITIVA-ACTUALIZADA.md`.
* [x] Revisé `codigo_unificado_springboot.txt`.
* [x] El controller no usa repositories.
* [x] El controller no usa SDK Stripe, Cloudinary, KafkaTemplate ni JavaMailSender.
* [x] El controller no calcula reglas de negocio.
* [x] El controller no duplica validaciones profundas del service.
* [x] El controller usa el resolver real de actor autenticado.
* [x] El controller usa `ApiResponseFactory`.
* [x] Los endpoints listables son paginados y filtrables.
* [x] Los endpoints HTML devuelven `text/html;charset=UTF-8` cuando aplica.
* [x] Los endpoints PDF devuelven `application/pdf` y no persisten PDF.
* [x] No se crearon endpoints de carrito ni checkout.
* [x] No se crearon endpoints de factura, notas, SUNAT, OSE ni XML.
* [x] Se listaron los endpoints implementados.
* [x] Se listaron los métodos service no expuestos y su motivo.
* [x] El código entregado es completo y compilable.
