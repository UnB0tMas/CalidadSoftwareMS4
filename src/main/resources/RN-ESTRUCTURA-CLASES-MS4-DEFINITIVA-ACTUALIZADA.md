# RN técnica MS4 — Estructura definitiva de paquetes, clases y métodos
**Microservicio:** `ms-ventas-facturacion`  
**Paquete raíz:** `com.upsjb.ms4`  
**Estado:** documento vinculante para implementación completa y productiva del MS4  
**Objetivo:** definir responsabilidades, convivencia entre clases y métodos oficiales para programar el microservicio sin duplicidad, sin clases basura y con comportamiento funcional completo.

## 1. Correcciones vinculantes de esta versión

- MS4 trabaja con **venta directa**. No se crean clases, endpoints, servicios, entidades ni DTOs de `carrito` ni `checkout`.
- Aunque una versión previa de RN haya mencionado carrito/checkout, esta RN de estructura los elimina para mantener el modelo acordado: selección de productos, venta, pago, boleta y stock.
- MS4 implementa **BOLETA** como comprobante operativo de esta etapa. No implementa factura, nota de crédito, nota de débito, SUNAT, OSE ni firma XML.
- Los PDFs de boleta no se almacenan en BD ni Cloudinary. Se generan en vivo con Thymeleaf/OpenHTMLToPDF.
- Cloudinary se usa solo para assets visuales: logo, marca y recursos de plantilla.
- Stripe se usa únicamente en modo Sandbox, tanto online como presencial en tienda.
- Toda consulta listable debe ser paginada y filtrable.
- Todo FK que el usuario deba seleccionar debe exponerse mediante lookup legible; el usuario no debe ingresar IDs numéricos a ciegas.
- Toda acción debe responder con resultado claro. Los errores de usuario se devuelven como error funcional. Los errores técnicos se registran con detalle en logs y al usuario se le devuelve un mensaje seguro.

## 2. Capas y regla DRY obligatoria

| Capa | Responsabilidad | Prohibido |
| --- | --- | --- |
| Controller | HTTP, `@Valid`, `@Validated`, resolver usuario, delegar a service y construir respuesta | Calcular, validar reglas profundas, usar repository, SDK, KafkaTemplate o JavaMailSender |
| Policy | Autorización funcional por rol, actor, propiedad del recurso y canal | Validar montos, stock, fechas, duplicados o mapear DTOs |
| Validator | Reglas de negocio y consistencia de datos | Autorizar por rol, consultar SDKs o armar respuestas HTTP |
| Service | Orquestación transaccional del caso de uso | Exponer entidades a controllers, duplicar cálculo, publicar Kafka directo o enviar mail directo |
| Mapper | Conversión DTO/entidad/evento/lookup | Consultar repositories o validar negocio |
| Repository | Persistencia JPA | Reglas de negocio, seguridad o respuestas HTTP |
| Specification | Filtros dinámicos reutilizables | Autorización, cálculo o efectos colaterales |
| Integration | SDK/HTTP externo encapsulado | Decidir negocio |
| Outbox/Processor | Procesar pendientes de Kafka/mail con lock y reintento | Crear ventas, pagos o boletas fuera del service dueño |

## 3. Reglas globales de métodos y respuestas

- Los métodos `listar...` siempre deben recibir `FilterDto` + `PageRequestDto` y devolver `PageResponseDto<T>`.
- Los métodos `obtener...` deben devolver response DTO hacia controllers y entidad solo cuando sean resolvers internos.
- Los métodos `resolver...` son internos de service/resolver y pueden devolver entidad JPA activa; nunca deben ser expuestos directamente al frontend.
- Los métodos `procesar...Kafka` deben ser idempotentes por `eventId`.
- Los métodos `registrar...Outbox` deben ejecutarse dentro de la misma transacción del caso de uso dueño.
- Los métodos de generación de PDF devuelven bytes/stream en memoria; nunca rutas, publicId ni URLs persistidas.
- Toda implementación `Impl` debe usar constructor injection, `@Transactional` en mutaciones, `@Transactional(readOnly = true)` en consultas, logging SLF4J y auditoría funcional en acciones críticas.

## 4. Flujo principal obligatorio del MS4

```text
Angular -> API Gateway -> Controller -> Policy -> Validator -> Service -> Mapper/Repository/Integration
VentaService -> VentaCalculoService -> Venta/Pago/Boleta/CorreoOutbox/EventoOutbox
VentaService -> VentaStockCommandService -> EventoDominioOutbox -> OutboxPublisher -> Kafka -> MS3
Kafka Consumer -> KafkaEventoConsumidoService -> Handler -> SnapshotService -> Snapshot local
CorreoOutboxScheduler -> CorreoOutboxProcessor -> BoletaMailService -> BoletaPdfService -> EmailService
BoletaController -> BoletaRenderService/BoletaPdfService -> Thymeleaf/OpenHTMLToPDF en memoria
```

## 5. Controladores oficiales y acciones HTTP

Regla: los controllers no contienen lógica de negocio. Solo reciben, validan superficialmente, resuelven actor, invocan policy/service y devuelven `ApiResponseDto`, HTML o PDF según corresponda.

### `ClienteCatalogoController`

| Método HTTP | Ruta | Método Java sugerido |
| --- | --- | --- |
| GET | /api/ms4/cliente/catalogo/productos | listarProductosVendibles(PageRequestDto, ProductoVentaFilterDto) |
| GET | /api/ms4/cliente/catalogo/productos/{idProductoMs3} | obtenerProductoVendible(Long idProductoMs3) |
| GET | /api/ms4/cliente/catalogo/skus/{idSkuMs3} | obtenerSkuVendible(Long idSkuMs3) |
| POST | /api/ms4/cliente/catalogo/ventas/preview | previsualizarCompraOnline(VentaCalculoPreviewRequestDto) |

**Consume:** `AuthenticatedUserResolver`, policy correspondiente, service correspondiente, DTOs, `ApiResponseFactory`.  
**No debe consumir:** repositories, SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni utilidades de cálculo directamente.

### `ClienteVentaController`

| Método HTTP | Ruta | Método Java sugerido |
| --- | --- | --- |
| POST | /api/ms4/cliente/ventas | crearVentaOnlinePendientePago(VentaOnlineCreateRequestDto) |
| GET | /api/ms4/cliente/ventas | listarMisVentas(PageRequestDto, VentaFilterDto) |
| GET | /api/ms4/cliente/ventas/{idVenta} | obtenerMiVenta(Long idVenta) |

**Consume:** `AuthenticatedUserResolver`, policy correspondiente, service correspondiente, DTOs, `ApiResponseFactory`.  
**No debe consumir:** repositories, SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni utilidades de cálculo directamente.

### `ClientePagoStripeController`

| Método HTTP | Ruta | Método Java sugerido |
| --- | --- | --- |
| POST | /api/ms4/cliente/pagos/stripe/payment-intent | crearPaymentIntentOnline(PagoStripeOnlineRequestDto) |
| GET | /api/ms4/cliente/pagos/stripe/config | obtenerConfiguracionPublicaStripeSandbox() |

**Consume:** `AuthenticatedUserResolver`, policy correspondiente, service correspondiente, DTOs, `ApiResponseFactory`.  
**No debe consumir:** repositories, SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni utilidades de cálculo directamente.

### `ClienteBoletaController`

| Método HTTP | Ruta | Método Java sugerido |
| --- | --- | --- |
| GET | /api/ms4/cliente/boletas | listarMisBoletas(PageRequestDto, BoletaFilterDto) |
| GET | /api/ms4/cliente/boletas/{idBoleta} | obtenerMiBoleta(Long idBoleta) |
| GET | /api/ms4/cliente/boletas/{idBoleta}/preview | renderizarPreviewHtml(Long idBoleta) |
| GET | /api/ms4/cliente/boletas/{idBoleta}/pdf | generarPdfEnVivo(Long idBoleta) |
| POST | /api/ms4/cliente/boletas/{idBoleta}/reenviar-correo | reenviarBoleta(Long idBoleta, BoletaReenvioCorreoRequestDto) |

**Consume:** `AuthenticatedUserResolver`, policy correspondiente, service correspondiente, DTOs, `ApiResponseFactory`.  
**No debe consumir:** repositories, SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni utilidades de cálculo directamente.

### `EmpleadoClienteController`

| Método HTTP | Ruta | Método Java sugerido |
| --- | --- | --- |
| GET | /api/ms4/empleado/clientes | listarClientesSnapshot(PageRequestDto, ClienteSnapshotFilterDto) |
| GET | /api/ms4/empleado/clientes/lookup | lookupClientes(LookupFilterDto) |

**Consume:** `AuthenticatedUserResolver`, policy correspondiente, service correspondiente, DTOs, `ApiResponseFactory`.  
**No debe consumir:** repositories, SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni utilidades de cálculo directamente.

### `EmpleadoCatalogoController`

| Método HTTP | Ruta | Método Java sugerido |
| --- | --- | --- |
| GET | /api/ms4/empleado/catalogo/productos | listarProductosVenta(PageRequestDto, ProductoVentaFilterDto) |
| GET | /api/ms4/empleado/catalogo/skus | listarSkusVenta(PageRequestDto, SkuVentaFilterDto) |
| GET | /api/ms4/empleado/catalogo/stocks | listarStocksVenta(PageRequestDto, StockVentaFilterDto) |
| POST | /api/ms4/empleado/catalogo/ventas/preview | previsualizarVentaFisica(VentaCalculoPreviewRequestDto) |

**Consume:** `AuthenticatedUserResolver`, policy correspondiente, service correspondiente, DTOs, `ApiResponseFactory`.  
**No debe consumir:** repositories, SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni utilidades de cálculo directamente.

### `EmpleadoCajaController`

| Método HTTP | Ruta | Método Java sugerido |
| --- | --- | --- |
| POST | /api/ms4/empleado/caja/aperturar | abrirCaja(CajaAperturaRequestDto) |
| POST | /api/ms4/empleado/caja/cerrar | cerrarCaja(CajaCierreRequestDto) |
| POST | /api/ms4/empleado/caja/ajustes | registrarAjuste(CajaAjusteRequestDto) |
| GET | /api/ms4/empleado/caja/actual | obtenerCajaActual() |
| GET | /api/ms4/empleado/caja/movimientos | listarMovimientosCajaActual(PageRequestDto, CajaMovimientoFilterDto) |

**Consume:** `AuthenticatedUserResolver`, policy correspondiente, service correspondiente, DTOs, `ApiResponseFactory`.  
**No debe consumir:** repositories, SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni utilidades de cálculo directamente.

### `EmpleadoVentaController`

| Método HTTP | Ruta | Método Java sugerido |
| --- | --- | --- |
| POST | /api/ms4/empleado/ventas | crearVentaFisica(VentaFisicaCreateRequestDto) |
| GET | /api/ms4/empleado/ventas | listarMisVentas(PageRequestDto, VentaFilterDto) |
| GET | /api/ms4/empleado/ventas/{idVenta} | obtenerVentaAutorizada(Long idVenta) |

**Consume:** `AuthenticatedUserResolver`, policy correspondiente, service correspondiente, DTOs, `ApiResponseFactory`.  
**No debe consumir:** repositories, SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni utilidades de cálculo directamente.

### `EmpleadoPagoController`

| Método HTTP | Ruta | Método Java sugerido |
| --- | --- | --- |
| POST | /api/ms4/empleado/ventas/{idVenta}/pago-efectivo | registrarPagoEfectivo(Long idVenta, PagoEfectivoRequestDto) |
| POST | /api/ms4/empleado/ventas/{idVenta}/pago-tarjeta-stripe | crearPaymentIntentPresencial(Long idVenta, PagoStripePresencialRequestDto) |

**Consume:** `AuthenticatedUserResolver`, policy correspondiente, service correspondiente, DTOs, `ApiResponseFactory`.  
**No debe consumir:** repositories, SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni utilidades de cálculo directamente.

### `EmpleadoBoletaController`

| Método HTTP | Ruta | Método Java sugerido |
| --- | --- | --- |
| GET | /api/ms4/empleado/boletas | listarBoletasAutorizadas(PageRequestDto, BoletaFilterDto) |
| GET | /api/ms4/empleado/boletas/{idBoleta} | obtenerBoletaAutorizada(Long idBoleta) |
| GET | /api/ms4/empleado/boletas/{idBoleta}/preview | renderizarPreviewHtml(Long idBoleta) |
| GET | /api/ms4/empleado/boletas/{idBoleta}/pdf | generarPdfEnVivo(Long idBoleta) |
| POST | /api/ms4/empleado/boletas/{idBoleta}/reenviar-correo | reenviarBoleta(Long idBoleta, BoletaReenvioCorreoRequestDto) |

**Consume:** `AuthenticatedUserResolver`, policy correspondiente, service correspondiente, DTOs, `ApiResponseFactory`.  
**No debe consumir:** repositories, SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni utilidades de cálculo directamente.

### `EmpleadoReporteController`

| Método HTTP | Ruta | Método Java sugerido |
| --- | --- | --- |
| GET | /api/ms4/empleado/reportes/caja/hoy | obtenerReporteCajaHoy() |
| GET | /api/ms4/empleado/reportes/caja/cierre/{idCaja} | obtenerReporteCierreCaja(Long idCaja) |
| GET | /api/ms4/empleado/reportes/ventas | obtenerReporteVentasEmpleado(ReporteEmpleadoCajaFilterDto) |

**Consume:** `AuthenticatedUserResolver`, policy correspondiente, service correspondiente, DTOs, `ApiResponseFactory`.  
**No debe consumir:** repositories, SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni utilidades de cálculo directamente.

### `AdminVentaController`

| Método HTTP | Ruta | Método Java sugerido |
| --- | --- | --- |
| GET | /api/ms4/admin/ventas | listarVentas(PageRequestDto, VentaFilterDto) |
| GET | /api/ms4/admin/ventas/{idVenta} | obtenerVenta(Long idVenta) |
| POST | /api/ms4/admin/ventas/{idVenta}/anular | anularVenta(Long idVenta, EstadoChangeRequestDto) |

**Consume:** `AuthenticatedUserResolver`, policy correspondiente, service correspondiente, DTOs, `ApiResponseFactory`.  
**No debe consumir:** repositories, SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni utilidades de cálculo directamente.

### `AdminBoletaController`

| Método HTTP | Ruta | Método Java sugerido |
| --- | --- | --- |
| GET | /api/ms4/admin/boletas | listarBoletas(PageRequestDto, BoletaFilterDto) |
| GET | /api/ms4/admin/boletas/{idBoleta} | obtenerBoleta(Long idBoleta) |
| GET | /api/ms4/admin/boletas/{idBoleta}/preview | renderizarPreviewHtml(Long idBoleta) |
| GET | /api/ms4/admin/boletas/{idBoleta}/pdf | generarPdfEnVivo(Long idBoleta) |
| POST | /api/ms4/admin/boletas/{idBoleta}/reenviar-correo | reenviarBoleta(Long idBoleta, BoletaReenvioCorreoRequestDto) |

**Consume:** `AuthenticatedUserResolver`, policy correspondiente, service correspondiente, DTOs, `ApiResponseFactory`.  
**No debe consumir:** repositories, SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni utilidades de cálculo directamente.

### `AdminCajaController`

| Método HTTP | Ruta | Método Java sugerido |
| --- | --- | --- |
| GET | /api/ms4/admin/cajas | listarCajas(PageRequestDto, CajaFilterDto) |
| GET | /api/ms4/admin/cajas/{idCaja} | obtenerCaja(Long idCaja) |
| GET | /api/ms4/admin/cajas/{idCaja}/movimientos | listarMovimientos(Long idCaja, PageRequestDto, CajaMovimientoFilterDto) |

**Consume:** `AuthenticatedUserResolver`, policy correspondiente, service correspondiente, DTOs, `ApiResponseFactory`.  
**No debe consumir:** repositories, SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni utilidades de cálculo directamente.

### `AdminReporteController`

| Método HTTP | Ruta | Método Java sugerido |
| --- | --- | --- |
| POST | /api/ms4/admin/reportes/ventas | generarReporteVentas(ReporteAdminVentasRequestDto) |
| POST | /api/ms4/admin/reportes/financiero | generarReporteFinanciero(ReporteAdminFinancieroRequestDto) |

**Consume:** `AuthenticatedUserResolver`, policy correspondiente, service correspondiente, DTOs, `ApiResponseFactory`.  
**No debe consumir:** repositories, SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni utilidades de cálculo directamente.

### `AdminConfiguracionEmpresaController`

| Método HTTP | Ruta | Método Java sugerido |
| --- | --- | --- |
| POST | /api/ms4/admin/configuracion/empresa/versiones | crearNuevaVersion(ConfiguracionEmpresaRequestDto) |
| GET | /api/ms4/admin/configuracion/empresa/vigente | obtenerVigente() |
| GET | /api/ms4/admin/configuracion/empresa/versiones | listarVersiones(PageRequestDto, ConfiguracionEmpresaFilterDto) |
| POST | /api/ms4/admin/configuracion/empresa/versiones/{id}/activar | activarVersion(Long id) |
| PATCH | /api/ms4/admin/configuracion/empresa/versiones/{id}/estado | cambiarEstado(Long id, EstadoChangeRequestDto) |
| POST | /api/ms4/admin/configuracion/empresa/assets | subirAssetVisual(AssetCloudinaryUploadRequestDto, MultipartFile) |

**Consume:** `AuthenticatedUserResolver`, policy correspondiente, service correspondiente, DTOs, `ApiResponseFactory`.  
**No debe consumir:** repositories, SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni utilidades de cálculo directamente.

### `AdminConfiguracionTributariaController`

| Método HTTP | Ruta | Método Java sugerido |
| --- | --- | --- |
| POST | /api/ms4/admin/configuracion/tributaria/igv/versiones | crearNuevaVersionIgv(ConfiguracionTributariaRequestDto) |
| GET | /api/ms4/admin/configuracion/tributaria/igv/vigente | obtenerIgvVigente() |
| GET | /api/ms4/admin/configuracion/tributaria/igv/versiones | listarVersiones(PageRequestDto, ConfiguracionTributariaFilterDto) |
| POST | /api/ms4/admin/configuracion/tributaria/igv/versiones/{id}/activar | activarVersionIgv(Long id) |
| PATCH | /api/ms4/admin/configuracion/tributaria/igv/versiones/{id}/estado | cambiarEstado(Long id, EstadoChangeRequestDto) |

**Consume:** `AuthenticatedUserResolver`, policy correspondiente, service correspondiente, DTOs, `ApiResponseFactory`.  
**No debe consumir:** repositories, SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni utilidades de cálculo directamente.

### `AdminSerieBoletaController`

| Método HTTP | Ruta | Método Java sugerido |
| --- | --- | --- |
| POST | /api/ms4/admin/series-boleta | crearSerie(SerieBoletaCreateRequestDto) |
| GET | /api/ms4/admin/series-boleta | listarSeries(PageRequestDto, SerieBoletaFilterDto) |
| GET | /api/ms4/admin/series-boleta/{id} | obtenerSerie(Long id) |
| PATCH | /api/ms4/admin/series-boleta/{id}/estado | cambiarEstado(Long id, EstadoChangeRequestDto) |

**Consume:** `AuthenticatedUserResolver`, policy correspondiente, service correspondiente, DTOs, `ApiResponseFactory`.  
**No debe consumir:** repositories, SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni utilidades de cálculo directamente.

### `AdminOutboxController`

| Método HTTP | Ruta | Método Java sugerido |
| --- | --- | --- |
| GET | /api/ms4/admin/outbox | listarOutbox(PageRequestDto, EventoDominioOutboxFilterDto) |
| POST | /api/ms4/admin/outbox/{id}/reintentar | reintentarEvento(Long id) |
| POST | /api/ms4/admin/outbox/{id}/descartar | descartarEvento(Long id, EstadoChangeRequestDto) |

**Consume:** `AuthenticatedUserResolver`, policy correspondiente, service correspondiente, DTOs, `ApiResponseFactory`.  
**No debe consumir:** repositories, SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni utilidades de cálculo directamente.

### `AdminCorreoOutboxController`

| Método HTTP | Ruta | Método Java sugerido |
| --- | --- | --- |
| GET | /api/ms4/admin/correos-outbox | listarCorreos(PageRequestDto, CorreoOutboxFilterDto) |
| POST | /api/ms4/admin/correos-outbox/{id}/reintentar | reintentarCorreo(Long id, CorreoOutboxReintentoRequestDto) |
| POST | /api/ms4/admin/correos-outbox/{id}/descartar | descartarCorreo(Long id, EstadoChangeRequestDto) |

**Consume:** `AuthenticatedUserResolver`, policy correspondiente, service correspondiente, DTOs, `ApiResponseFactory`.  
**No debe consumir:** repositories, SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni utilidades de cálculo directamente.

### `AdminContingenciaController`

| Método HTTP | Ruta | Método Java sugerido |
| --- | --- | --- |
| POST | /api/ms4/admin/contingencia/activar | activarContingencia(ContingenciaActivarRequestDto) |
| POST | /api/ms4/admin/contingencia/finalizar | finalizarContingencia(ContingenciaFinalizarRequestDto) |
| GET | /api/ms4/admin/contingencia/actual | obtenerContingenciaActual() |
| GET | /api/ms4/admin/contingencia/eventos-pendientes | listarEventosPendientes(PageRequestDto, InventarioEventoPendienteFilterDto) |
| POST | /api/ms4/admin/contingencia/reconciliar | reconciliarEventosPendientes() |

**Consume:** `AuthenticatedUserResolver`, policy correspondiente, service correspondiente, DTOs, `ApiResponseFactory`.  
**No debe consumir:** repositories, SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni utilidades de cálculo directamente.

### `AdminSnapshotController`

| Método HTTP | Ruta | Método Java sugerido |
| --- | --- | --- |
| GET | /api/ms4/admin/snapshots/clientes | listarClientesSnapshot(PageRequestDto, ClienteSnapshotFilterDto) |
| GET | /api/ms4/admin/snapshots/empleados | listarEmpleadosSnapshot(PageRequestDto, EmpleadoSnapshotFilterDto) |
| GET | /api/ms4/admin/snapshots/productos | listarProductosSnapshot(PageRequestDto, ProductoVentaFilterDto) |
| GET | /api/ms4/admin/snapshots/stocks | listarStocksSnapshot(PageRequestDto, StockVentaFilterDto) |

**Consume:** `AuthenticatedUserResolver`, policy correspondiente, service correspondiente, DTOs, `ApiResponseFactory`.  
**No debe consumir:** repositories, SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni utilidades de cálculo directamente.

### `AdminAuditoriaController`

| Método HTTP | Ruta | Método Java sugerido |
| --- | --- | --- |
| GET | /api/ms4/admin/auditoria | listarAuditoria(PageRequestDto, AuditoriaFilterDto) |
| GET | /api/ms4/admin/auditoria/{id} | obtenerAuditoria(Long id) |

**Consume:** `AuthenticatedUserResolver`, policy correspondiente, service correspondiente, DTOs, `ApiResponseFactory`.  
**No debe consumir:** repositories, SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni utilidades de cálculo directamente.

### `LookupController`

| Método HTTP | Ruta | Método Java sugerido |
| --- | --- | --- |
| GET | /api/ms4/lookups/clientes | clientes(LookupFilterDto) |
| GET | /api/ms4/lookups/empleados | empleados(LookupFilterDto) |
| GET | /api/ms4/lookups/productos | productos(LookupFilterDto) |
| GET | /api/ms4/lookups/skus | skus(LookupFilterDto) |
| GET | /api/ms4/lookups/almacenes | almacenes(LookupFilterDto) |
| GET | /api/ms4/lookups/series-boleta | seriesBoleta(LookupFilterDto) |
| GET | /api/ms4/lookups/caja-abierta | cajaAbiertaHoy() |

**Consume:** `AuthenticatedUserResolver`, policy correspondiente, service correspondiente, DTOs, `ApiResponseFactory`.  
**No debe consumir:** repositories, SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni utilidades de cálculo directamente.

### `InternalStockSyncController`

| Método HTTP | Ruta | Método Java sugerido |
| --- | --- | --- |
| GET | /api/internal/ms4/stock-events/pending | listarPendientes(PageRequestDto, InventarioEventoPendienteFilterDto) |
| POST | /api/internal/ms4/stock-events/{id}/mark-synced | marcarSincronizado(Long id) |
| POST | /api/internal/ms4/stock-events/{id}/mark-error | marcarError(Long id, String error) |
| POST | /api/internal/ms4/stock-sync/result | registrarResultadoSincronizacion(...) |

**Consume:** `AuthenticatedUserResolver`, policy correspondiente, service correspondiente, DTOs, `ApiResponseFactory`.  
**No debe consumir:** repositories, SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni utilidades de cálculo directamente.

### `StripeWebhookController`

| Método HTTP | Ruta | Método Java sugerido |
| --- | --- | --- |
| POST | /api/ms4/webhooks/stripe | procesarWebhook(String rawPayload, String stripeSignature) |

**Consume:** `AuthenticatedUserResolver`, policy correspondiente, service correspondiente, DTOs, `ApiResponseFactory`.  
**No debe consumir:** repositories, SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni utilidades de cálculo directamente.

## 6. Entidades de dominio

Regla: las entidades representan estado persistente, no respuestas HTTP. Todas las entidades funcionales deben respetar `estado`, auditoría temporal y relaciones del script SQL.

| Clase | Responsabilidad |
| --- | --- |
| BaseEntity | Superclase JPA con id, estado, createdAt y updatedAt. |
| AssetCloudinary | Asset visual en Cloudinary. Nunca representa PDF de boleta. |
| ConfiguracionEmpresaVersion | Datos versionados del emisor congelables en boleta. |
| ConfiguracionTributariaVersion | IGV versionado usado para ventas históricas. |
| BoletaPlantillaVersion | Plantilla Thymeleaf versionada para boleta y correo. |
| SerieBoleta | Serie y correlativo de boletas. |
| ClienteSnapshotMs2 | Snapshot local de cliente oficial de MS2. |
| EmpleadoSnapshotMs2 | Snapshot local de empleado oficial de MS2. |
| ProductoSnapshotMs3 | Snapshot local de producto publicado por MS3. |
| SkuSnapshotMs3 | Snapshot local de SKU vendible. |
| PrecioSnapshotMs3 | Snapshot local de precio versionado de SKU. |
| PromocionSnapshotMs3 | Snapshot local de promoción maestra de MS3. |
| PromocionSkuDescuentoSnapshotMs3 | Snapshot local de descuento por SKU. |
| StockSnapshotMs3 | Snapshot local de stock por SKU y almacén. |
| Venta | Cabecera de venta física u online. |
| VentaDetalle | Detalle vendido con precio, promoción, IGV y stock congelados. |
| Pago | Pago efectivo o Stripe Sandbox asociado a venta. |
| StripeEvento | Webhook Stripe registrado de forma idempotente. |
| Boleta | Boleta lógica; fuente de verdad del comprobante. |
| BoletaDetalle | Detalle congelado de la boleta. |
| Caja | Caja diaria para ventas físicas. |
| CajaMovimiento | Movimiento auditado de caja. |
| KafkaEventoConsumido | Evento Kafka consumido desde MS2/MS3 para idempotencia. |
| EventoDominioOutbox | Evento Outbox a publicar en Kafka. |
| InventarioEventoPendienteMs4 | Comando de inventario pendiente para contingencia/reconciliación. |
| CorreoOutbox | Correo pendiente o procesado para boletas/alertas. |
| ModoContingencia | Estado de contingencia controlada por ADMIN. |
| AuditoriaFuncional | Registro funcional de operaciones críticas. |

## 7. Enums oficiales

Regla: ningún service/controller debe usar strings mágicos para estados, canales, métodos de pago o tipos de evento.

| Enum | Valores/uso esperado |
| --- | --- |
| CanalVenta | FISICA, ONLINE. |
| EstadoVenta | BORRADOR, PENDIENTE_PAGO, PAGADA, CONFIRMADA, ANULADA, RECHAZADA, ERROR_STOCK, PENDIENTE_SYNC_STOCK. |
| MetodoPago | EFECTIVO, TARJETA_PRESENCIAL_STRIPE_SANDBOX, TARJETA_ONLINE_STRIPE_SANDBOX. |
| EstadoPago | PENDIENTE, APROBADO, RECHAZADO, ANULADO, REEMBOLSADO, ERROR. |
| EstadoCaja | ABIERTA, CERRADA, ANULADA. |
| TipoMovimientoCaja | APERTURA, VENTA_EFECTIVO, VENTA_TARJETA, CIERRE, AJUSTE, ANULACION. |
| EstadoBoleta | EMITIDA, ANULADA, ERROR_ENVIO_CORREO. |
| TipoCorreo | BOLETA_COMPRA_FISICA, BOLETA_COMPRA_ONLINE, ALERTA_MS3_CAIDO, ALERTA_MS3_RECUPERADO, ALERTA_CONTINGENCIA_ACTIVADA, ALERTA_CONTINGENCIA_FINALIZADA, ALERTA_KAFKA_ERROR, ALERTA_STRIPE_ERROR, ALERTA_CAJA_DIFERENCIA. |
| EstadoCorreo | PENDIENTE, ENVIANDO, ENVIADO, ERROR, DESCARTADO. |
| EstadoOutbox | PENDIENTE, PUBLICANDO, PUBLICADO, ERROR, DESCARTADO. |
| EstadoKafkaProcesamiento | RECIBIDO, PROCESADO, IGNORADO, ERROR. |
| TipoComandoStock | RESERVAR_STOCK, CONFIRMAR_VENTA, LIBERAR_RESERVA, ANULAR_VENTA, RECONCILIAR_STOCK. |
| EstadoSincronizacionInventario | PENDIENTE, ENVIADO, SINCRONIZADO, ERROR, REQUIERE_REVISION. |
| EstadoContingencia | PENDIENTE_CONFIRMACION, ACTIVO, FINALIZADO, CANCELADO. |
| TipoDescuento | PORCENTAJE, MONTO_FIJO. |
| NombreImpuesto | IGV. |
| ResourceTypeCloudinary | IMAGE, RAW, VIDEO. |
| ResultadoAuditoria | EXITOSO, ERROR_USUARIO, ERROR_TECNICO. |

## 8. DTOs oficiales

Regla: los DTOs no contienen lógica de negocio ni anotaciones JPA. Los request DTOs usan Bean Validation. Los response DTOs deben ser legibles para UX. Los filter DTOs alimentan specifications. Los lookup DTOs evitan que el usuario escriba FKs manuales.

### `dto.shared`

| DTO | Responsabilidad |
| --- | --- |
| ApiResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| ErrorResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| PageRequestDto | Entrada de usuario validada con Bean Validation; no debe incluir lógica. |
| PageResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| IdResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| EstadoChangeRequestDto | Entrada de usuario validada con Bean Validation; no debe incluir lógica. |
| BaseFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| DateRangeFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| EstadoFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |

### `dto.lookup`

| DTO | Responsabilidad |
| --- | --- |
| LookupItemResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| LookupFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| ClienteLookupResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| EmpleadoLookupResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| ProductoLookupResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| SkuLookupResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| AlmacenLookupResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| SerieBoletaLookupResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| CajaAbiertaLookupResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |

### `dto.config.request`

| DTO | Responsabilidad |
| --- | --- |
| ConfiguracionEmpresaRequestDto | Entrada de usuario validada con Bean Validation; no debe incluir lógica. |
| ConfiguracionTributariaRequestDto | Entrada de usuario validada con Bean Validation; no debe incluir lógica. |
| SerieBoletaCreateRequestDto | Entrada de usuario validada con Bean Validation; no debe incluir lógica. |
| AssetCloudinaryUploadRequestDto | Entrada de usuario validada con Bean Validation; no debe incluir lógica. |

### `dto.config.response`

| DTO | Responsabilidad |
| --- | --- |
| ConfiguracionEmpresaResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| ConfiguracionTributariaResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| SerieBoletaResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| BoletaPlantillaResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| AssetCloudinaryResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |

### `dto.config.filter`

| DTO | Responsabilidad |
| --- | --- |
| ConfiguracionEmpresaFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| ConfiguracionTributariaFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| SerieBoletaFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| AssetCloudinaryFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |

### `dto.snapshot.response`

| DTO | Responsabilidad |
| --- | --- |
| ClienteSnapshotResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| EmpleadoSnapshotResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| ProductoVentaResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| SkuVentaResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| PrecioVentaResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| PromocionVentaResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| StockVentaResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |

### `dto.snapshot.filter`

| DTO | Responsabilidad |
| --- | --- |
| ClienteSnapshotFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| EmpleadoSnapshotFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| ProductoVentaFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| SkuVentaFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| PrecioVentaFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| PromocionVentaFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| StockVentaFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |

### `dto.venta.request`

| DTO | Responsabilidad |
| --- | --- |
| VentaFisicaCreateRequestDto | Entrada de usuario validada con Bean Validation; no debe incluir lógica. |
| VentaOnlineCreateRequestDto | Entrada de usuario validada con Bean Validation; no debe incluir lógica. |
| VentaDetalleRequestDto | Entrada de usuario validada con Bean Validation; no debe incluir lógica. |
| VentaCalculoPreviewRequestDto | Entrada de usuario validada con Bean Validation; no debe incluir lógica. |

### `dto.venta.response`

| DTO | Responsabilidad |
| --- | --- |
| VentaResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| VentaDetailResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| VentaDetalleResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| VentaCalculoPreviewResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| VentaResumenResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |

### `dto.venta.filter`

| DTO | Responsabilidad |
| --- | --- |
| VentaFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |

### `dto.pago.request`

| DTO | Responsabilidad |
| --- | --- |
| PagoEfectivoRequestDto | Entrada de usuario validada con Bean Validation; no debe incluir lógica. |
| PagoStripePresencialRequestDto | Entrada de usuario validada con Bean Validation; no debe incluir lógica. |
| PagoStripeOnlineRequestDto | Entrada de usuario validada con Bean Validation; no debe incluir lógica. |
| StripePaymentIntentRequestDto | Entrada de usuario validada con Bean Validation; no debe incluir lógica. |

### `dto.pago.response`

| DTO | Responsabilidad |
| --- | --- |
| PagoResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| StripePaymentIntentResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| StripeWebhookProcessResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |

### `dto.pago.filter`

| DTO | Responsabilidad |
| --- | --- |
| PagoFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| StripeEventoFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |

### `dto.boleta.request`

| DTO | Responsabilidad |
| --- | --- |
| BoletaReenvioCorreoRequestDto | Entrada de usuario validada con Bean Validation; no debe incluir lógica. |

### `dto.boleta.response`

| DTO | Responsabilidad |
| --- | --- |
| BoletaResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| BoletaDetailResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| BoletaDetalleResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| BoletaPreviewResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |

### `dto.boleta.filter`

| DTO | Responsabilidad |
| --- | --- |
| BoletaFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |

### `dto.caja.request`

| DTO | Responsabilidad |
| --- | --- |
| CajaAperturaRequestDto | Entrada de usuario validada con Bean Validation; no debe incluir lógica. |
| CajaCierreRequestDto | Entrada de usuario validada con Bean Validation; no debe incluir lógica. |
| CajaAjusteRequestDto | Entrada de usuario validada con Bean Validation; no debe incluir lógica. |

### `dto.caja.response`

| DTO | Responsabilidad |
| --- | --- |
| CajaResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| CajaDetailResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| CajaMovimientoResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| CajaCierreResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| CajaResumenDiaResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |

### `dto.caja.filter`

| DTO | Responsabilidad |
| --- | --- |
| CajaFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| CajaMovimientoFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |

### `dto.reporte.request`

| DTO | Responsabilidad |
| --- | --- |
| ReporteEmpleadoCajaRequestDto | Entrada de usuario validada con Bean Validation; no debe incluir lógica. |
| ReporteAdminVentasRequestDto | Entrada de usuario validada con Bean Validation; no debe incluir lógica. |
| ReporteAdminFinancieroRequestDto | Entrada de usuario validada con Bean Validation; no debe incluir lógica. |

### `dto.reporte.response`

| DTO | Responsabilidad |
| --- | --- |
| ReporteEmpleadoCajaResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| ReporteEmpleadoCierreCajaResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| ReporteAdminVentasResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| ReporteAdminFinancieroResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| ReporteProductoVendidoDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| ReporteVentaPorEmpleadoDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| ReporteVentaPorMetodoPagoDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| ReporteVentaPorCanalDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| ReporteVentaPorCategoriaDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| ReporteGananciaEstimadaDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |

### `dto.reporte.filter`

| DTO | Responsabilidad |
| --- | --- |
| ReporteVentasAdminFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| ReporteFinancieroAdminFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| ReporteEmpleadoCajaFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |

### `dto.kafka.common`

| DTO | Responsabilidad |
| --- | --- |
| DomainEventEnvelopeDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |

### `dto.kafka.ms2`

| DTO | Responsabilidad |
| --- | --- |
| ClienteSnapshotPayloadDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| EmpleadoSnapshotPayloadDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |

### `dto.kafka.ms3`

| DTO | Responsabilidad |
| --- | --- |
| ProductoSnapshotPayloadDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| SkuSnapshotPayloadDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| PrecioSnapshotPayloadDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| PromocionSnapshotPayloadDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| PromocionSkuDescuentoPayloadDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| StockSnapshotPayloadDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |

### `dto.kafka.ms4`

| DTO | Responsabilidad |
| --- | --- |
| Ms4StockCommandEventDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| Ms4StockCommandPayloadDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| Ms4VentaPayloadDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| Ms4SkuPayloadDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| Ms4AlmacenPayloadDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |

### `dto.mail.request`

| DTO | Responsabilidad |
| --- | --- |
| CorreoOutboxReintentoRequestDto | Entrada de usuario validada con Bean Validation; no debe incluir lógica. |

### `dto.mail.response`

| DTO | Responsabilidad |
| --- | --- |
| CorreoOutboxResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| EmailAttachmentDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |

### `dto.mail.filter`

| DTO | Responsabilidad |
| --- | --- |
| CorreoOutboxFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |

### `dto.contingencia.request`

| DTO | Responsabilidad |
| --- | --- |
| ContingenciaActivarRequestDto | Entrada de usuario validada con Bean Validation; no debe incluir lógica. |
| ContingenciaFinalizarRequestDto | Entrada de usuario validada con Bean Validation; no debe incluir lógica. |

### `dto.contingencia.response`

| DTO | Responsabilidad |
| --- | --- |
| ModoContingenciaResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| InventarioEventoPendienteResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |

### `dto.contingencia.filter`

| DTO | Responsabilidad |
| --- | --- |
| ModoContingenciaFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |
| InventarioEventoPendienteFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |

### `dto.auditoria.response`

| DTO | Responsabilidad |
| --- | --- |
| AuditoriaResponseDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |

### `dto.auditoria.filter`

| DTO | Responsabilidad |
| --- | --- |
| AuditoriaFilterDto | Salida legible para frontend o payload controlado; no expone entidad JPA. |

## 9. Repositories oficiales

Regla: cada repository extiende `JpaRepository<Entidad, Long>`. Los repositorios listables deben extender `JpaSpecificationExecutor<Entidad>`. Solo declaran consultas por intención clara; no contienen negocio.

| Repository | Entidad | Métodos obligatorios |
| --- | --- | --- |
| AssetCloudinaryRepository | AssetCloudinary | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. |
| ConfiguracionEmpresaVersionRepository | ConfiguracionEmpresaVersion | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. |
| ConfiguracionTributariaVersionRepository | ConfiguracionTributariaVersion | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. |
| BoletaPlantillaVersionRepository | BoletaPlantillaVersion | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. |
| SerieBoletaRepository | SerieBoleta | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. |
| KafkaEventoConsumidoRepository | KafkaEventoConsumido | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. Debe tener búsqueda idempotente por identificador externo/eventId. |
| ClienteSnapshotMs2Repository | ClienteSnapshotMs2 | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. |
| EmpleadoSnapshotMs2Repository | EmpleadoSnapshotMs2 | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. |
| ProductoSnapshotMs3Repository | ProductoSnapshotMs3 | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. |
| SkuSnapshotMs3Repository | SkuSnapshotMs3 | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. |
| PrecioSnapshotMs3Repository | PrecioSnapshotMs3 | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. |
| PromocionSnapshotMs3Repository | PromocionSnapshotMs3 | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. |
| PromocionSkuDescuentoSnapshotMs3Repository | PromocionSkuDescuentoSnapshotMs3 | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. |
| StockSnapshotMs3Repository | StockSnapshotMs3 | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. |
| CajaRepository | Caja | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. |
| CajaMovimientoRepository | CajaMovimiento | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. |
| VentaRepository | Venta | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. |
| VentaDetalleRepository | VentaDetalle | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. |
| PagoRepository | Pago | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. |
| StripeEventoRepository | StripeEvento | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. Debe tener búsqueda idempotente por identificador externo/eventId. |
| BoletaRepository | Boleta | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. |
| BoletaDetalleRepository | BoletaDetalle | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. |
| EventoDominioOutboxRepository | EventoDominioOutbox | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. Debe tener métodos para reclamar pendientes por estado y lock. |
| CorreoOutboxRepository | CorreoOutbox | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. Debe tener métodos para reclamar pendientes por estado y lock. |
| ModoContingenciaRepository | ModoContingencia | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. |
| InventarioEventoPendienteMs4Repository | InventarioEventoPendienteMs4 | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. Debe tener métodos para reclamar pendientes por estado y lock. |
| AuditoriaFuncionalRepository | AuditoriaFuncional | Debe soportar `JpaSpecificationExecutor` si se lista con filtros. |

## 10. Specifications oficiales

Regla: cada specification construye predicados reutilizables. No autoriza ni calcula. Debe soportar estado, fechas y texto cuando aplique.

| Specification | Responsabilidad/método |
| --- | --- |
| VentaSpecification | Método principal: `Specification<Entidad> build(FilterDto filter)`; predicados auxiliares privados o estáticos sin efectos colaterales. |
| BoletaSpecification | Método principal: `Specification<Entidad> build(FilterDto filter)`; predicados auxiliares privados o estáticos sin efectos colaterales. |
| CajaSpecification | Método principal: `Specification<Entidad> build(FilterDto filter)`; predicados auxiliares privados o estáticos sin efectos colaterales. |
| CajaMovimientoSpecification | Método principal: `Specification<Entidad> build(FilterDto filter)`; predicados auxiliares privados o estáticos sin efectos colaterales. |
| PagoSpecification | Método principal: `Specification<Entidad> build(FilterDto filter)`; predicados auxiliares privados o estáticos sin efectos colaterales. |
| AuditoriaSpecification | Método principal: `Specification<Entidad> build(FilterDto filter)`; predicados auxiliares privados o estáticos sin efectos colaterales. |
| ClienteSnapshotSpecification | Método principal: `Specification<Entidad> build(FilterDto filter)`; predicados auxiliares privados o estáticos sin efectos colaterales. |
| EmpleadoSnapshotSpecification | Método principal: `Specification<Entidad> build(FilterDto filter)`; predicados auxiliares privados o estáticos sin efectos colaterales. |
| ProductoSnapshotSpecification | Método principal: `Specification<Entidad> build(FilterDto filter)`; predicados auxiliares privados o estáticos sin efectos colaterales. |
| SkuSnapshotSpecification | Método principal: `Specification<Entidad> build(FilterDto filter)`; predicados auxiliares privados o estáticos sin efectos colaterales. |
| StockSnapshotSpecification | Método principal: `Specification<Entidad> build(FilterDto filter)`; predicados auxiliares privados o estáticos sin efectos colaterales. |
| EventoDominioOutboxSpecification | Método principal: `Specification<Entidad> build(FilterDto filter)`; predicados auxiliares privados o estáticos sin efectos colaterales. |
| CorreoOutboxSpecification | Método principal: `Specification<Entidad> build(FilterDto filter)`; predicados auxiliares privados o estáticos sin efectos colaterales. |
| ModoContingenciaSpecification | Método principal: `Specification<Entidad> build(FilterDto filter)`; predicados auxiliares privados o estáticos sin efectos colaterales. |
| InventarioEventoPendienteSpecification | Método principal: `Specification<Entidad> build(FilterDto filter)`; predicados auxiliares privados o estáticos sin efectos colaterales. |

## 11. Mappers oficiales

Regla: mapper convierte datos. No consulta BD, no llama services, no calcula negocio. Puede recibir datos ya resueltos por service.

| Mapper | Métodos obligatorios |
| --- | --- |
| ConfiguracionEmpresaMapper | `toResponse(entity)`, `toDetailResponse(entity)`, `toEntity(request)` cuando aplique, `updateEntity(entity, request)` sin resolver FK, `toLookup(entity)` cuando aplique. |
| ConfiguracionTributariaMapper | `toResponse(entity)`, `toDetailResponse(entity)`, `toEntity(request)` cuando aplique, `updateEntity(entity, request)` sin resolver FK, `toLookup(entity)` cuando aplique. |
| SerieBoletaMapper | `toResponse(entity)`, `toDetailResponse(entity)`, `toEntity(request)` cuando aplique, `updateEntity(entity, request)` sin resolver FK, `toLookup(entity)` cuando aplique. |
| AssetCloudinaryMapper | `toResponse(entity)`, `toDetailResponse(entity)`, `toEntity(request)` cuando aplique, `updateEntity(entity, request)` sin resolver FK, `toLookup(entity)` cuando aplique. |
| ClienteSnapshotMapper | `toEntityFromPayload(payload, envelope, rawJson)`, `updateFromPayload(entity, payload, envelope, rawJson)`, `toResponse(entity)`, `toLookup(entity)`. |
| EmpleadoSnapshotMapper | `toEntityFromPayload(payload, envelope, rawJson)`, `updateFromPayload(entity, payload, envelope, rawJson)`, `toResponse(entity)`, `toLookup(entity)`. |
| ProductoSnapshotMapper | `toEntityFromPayload(payload, envelope, rawJson)`, `updateFromPayload(entity, payload, envelope, rawJson)`, `toResponse(entity)`, `toLookup(entity)`. |
| PrecioSnapshotMapper | `toEntityFromPayload(payload, envelope, rawJson)`, `updateFromPayload(entity, payload, envelope, rawJson)`, `toResponse(entity)`, `toLookup(entity)`. |
| PromocionSnapshotMapper | `toEntityFromPayload(payload, envelope, rawJson)`, `updateFromPayload(entity, payload, envelope, rawJson)`, `toResponse(entity)`, `toLookup(entity)`. |
| StockSnapshotMapper | `toEntityFromPayload(payload, envelope, rawJson)`, `updateFromPayload(entity, payload, envelope, rawJson)`, `toResponse(entity)`, `toLookup(entity)`. |
| LookupMapper | `toResponse(entity)`, `toDetailResponse(entity)`, `toEntity(request)` cuando aplique, `updateEntity(entity, request)` sin resolver FK, `toLookup(entity)` cuando aplique. |
| VentaMapper | `toResponse(entity)`, `toDetailResponse(entity)`, `toEntity(request)` cuando aplique, `updateEntity(entity, request)` sin resolver FK, `toLookup(entity)` cuando aplique. |
| VentaDetalleMapper | `toResponse(entity)`, `toDetailResponse(entity)`, `toEntity(request)` cuando aplique, `updateEntity(entity, request)` sin resolver FK, `toLookup(entity)` cuando aplique. |
| PagoMapper | `toResponse(entity)`, `toDetailResponse(entity)`, `toEntity(request)` cuando aplique, `updateEntity(entity, request)` sin resolver FK, `toLookup(entity)` cuando aplique. |
| BoletaMapper | `toResponse(entity)`, `toDetailResponse(entity)`, `toEntity(request)` cuando aplique, `updateEntity(entity, request)` sin resolver FK, `toLookup(entity)` cuando aplique. |
| BoletaDetalleMapper | `toResponse(entity)`, `toDetailResponse(entity)`, `toEntity(request)` cuando aplique, `updateEntity(entity, request)` sin resolver FK, `toLookup(entity)` cuando aplique. |
| CajaMapper | `toResponse(entity)`, `toDetailResponse(entity)`, `toEntity(request)` cuando aplique, `updateEntity(entity, request)` sin resolver FK, `toLookup(entity)` cuando aplique. |
| CajaMovimientoMapper | `toResponse(entity)`, `toDetailResponse(entity)`, `toEntity(request)` cuando aplique, `updateEntity(entity, request)` sin resolver FK, `toLookup(entity)` cuando aplique. |
| StockCommandEventMapper | `toStockCommandEvent(venta, detalle, tipoComando, idempotencyKey)`, `toPayload(...)`, `toOutboxPayload(event)`. |
| CorreoOutboxMapper | `toResponse(entity)`, `toEmailContext(entity)`, `toRetryResponse(entity)`. |
| AuditoriaMapper | `toResponse(entity)`, `toDetailResponse(entity)`, `toEntity(request)` cuando aplique, `updateEntity(entity, request)` sin resolver FK, `toLookup(entity)` cuando aplique. |

## 12. Service contracts oficiales con métodos obligatorios

Esta sección es vinculante. No se debe implementar un service sin estos métodos, salvo que se agregue una mejora compatible sin duplicar responsabilidades. Las interfaces deben vivir en `service.contract.*` y las implementaciones en `service.impl.*`.

### `AssetCloudinaryService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
AssetCloudinaryResponseDto subirAssetVisual(AssetCloudinaryUploadRequestDto request, MultipartFile file, AuthenticatedUserContext actor);
AssetCloudinaryResponseDto reemplazarAssetVisual(Long idAssetActual, AssetCloudinaryUploadRequestDto request, MultipartFile file, AuthenticatedUserContext actor);
AssetCloudinaryResponseDto obtenerPorId(Long idAsset);
PageResponseDto<AssetCloudinaryResponseDto> listar(AssetCloudinaryFilterDto filter, PageRequestDto page);
AssetCloudinaryResponseDto cambiarEstado(Long idAsset, EstadoChangeRequestDto request, AuthenticatedUserContext actor);
void validarAssetVisualPermitido(MultipartFile file, String tipoAsset);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `ConfiguracionEmpresaService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
ConfiguracionEmpresaResponseDto crearNuevaVersion(ConfiguracionEmpresaRequestDto request, AuthenticatedUserContext actor);
ConfiguracionEmpresaResponseDto obtenerVersionVigente();
ConfiguracionEmpresaResponseDto obtenerPorId(Long idVersion);
PageResponseDto<ConfiguracionEmpresaResponseDto> listarVersiones(ConfiguracionEmpresaFilterDto filter, PageRequestDto page);
ConfiguracionEmpresaResponseDto activarVersion(Long idVersion, AuthenticatedUserContext actor);
ConfiguracionEmpresaResponseDto cambiarEstado(Long idVersion, EstadoChangeRequestDto request, AuthenticatedUserContext actor);
ConfiguracionEmpresaVersion resolverVersionVigenteParaEmisionBoleta();
ConfiguracionEmpresaVersion resolverVersionPorIdParaRender(Long idVersion);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `ConfiguracionTributariaService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
ConfiguracionTributariaResponseDto crearNuevaVersionIgv(ConfiguracionTributariaRequestDto request, AuthenticatedUserContext actor);
ConfiguracionTributariaResponseDto obtenerIgvVigente();
ConfiguracionTributariaResponseDto obtenerPorId(Long idVersion);
PageResponseDto<ConfiguracionTributariaResponseDto> listarVersiones(ConfiguracionTributariaFilterDto filter, PageRequestDto page);
ConfiguracionTributariaResponseDto activarVersionIgv(Long idVersion, AuthenticatedUserContext actor);
ConfiguracionTributariaResponseDto cambiarEstado(Long idVersion, EstadoChangeRequestDto request, AuthenticatedUserContext actor);
ConfiguracionTributariaVersion resolverIgvVigenteParaVenta();
ConfiguracionTributariaVersion resolverVersionPorIdParaRender(Long idVersion);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `BoletaPlantillaService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
BoletaPlantillaResponseDto crearNuevaVersion(BoletaPlantillaRequestDto request, AuthenticatedUserContext actor);
BoletaPlantillaResponseDto obtenerVersionVigente();
BoletaPlantillaResponseDto obtenerPorId(Long idVersion);
PageResponseDto<BoletaPlantillaResponseDto> listarVersiones(BoletaPlantillaFilterDto filter, PageRequestDto page);
BoletaPlantillaResponseDto activarVersion(Long idVersion, AuthenticatedUserContext actor);
BoletaPlantillaVersion resolverPlantillaVigenteParaEmision();
BoletaPlantillaVersion resolverPlantillaPorIdParaRender(Long idVersion);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `SerieBoletaService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
SerieBoletaResponseDto crearSerie(SerieBoletaCreateRequestDto request, AuthenticatedUserContext actor);
SerieBoletaResponseDto obtenerPorId(Long idSerie);
PageResponseDto<SerieBoletaResponseDto> listar(SerieBoletaFilterDto filter, PageRequestDto page);
SerieBoletaResponseDto cambiarEstado(Long idSerie, EstadoChangeRequestDto request, AuthenticatedUserContext actor);
SerieBoleta resolverSerieActivaParaEmision();
NumeroBoletaReservado reservarSiguienteNumero(Long idSerie, AuthenticatedUserContext actor);
void confirmarNumeroUsado(Long idSerie, Long numeroReservado);
void liberarNumeroReservadoSiFalla(Long idSerie, Long numeroReservado);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `LookupService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
PageResponseDto<ClienteLookupResponseDto> buscarClientes(LookupFilterDto filter, PageRequestDto page, AuthenticatedUserContext actor);
PageResponseDto<EmpleadoLookupResponseDto> buscarEmpleados(LookupFilterDto filter, PageRequestDto page, AuthenticatedUserContext actor);
PageResponseDto<ProductoLookupResponseDto> buscarProductosVendibles(LookupFilterDto filter, PageRequestDto page, AuthenticatedUserContext actor);
PageResponseDto<SkuLookupResponseDto> buscarSkusVendibles(LookupFilterDto filter, PageRequestDto page, AuthenticatedUserContext actor);
PageResponseDto<AlmacenLookupResponseDto> buscarAlmacenesPorSku(Long idSkuMs3, LookupFilterDto filter, PageRequestDto page, AuthenticatedUserContext actor);
PageResponseDto<SerieBoletaLookupResponseDto> buscarSeriesBoletaActivas(LookupFilterDto filter, PageRequestDto page, AuthenticatedUserContext actor);
CajaAbiertaLookupResponseDto obtenerCajaAbiertaHoy(AuthenticatedUserContext actor);
List<LookupItemResponseDto> listarMetodosPagoPermitidos(CanalVenta canalVenta, AuthenticatedUserContext actor);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `Ms4ReferenceResolverService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
ClienteSnapshotMs2 resolverClienteSnapshotActivo(Long idClienteSnapshot);
ClienteSnapshotMs2 resolverClientePorUsuarioMs1(Long idUsuarioMs1);
EmpleadoSnapshotMs2 resolverEmpleadoActivoPorUsuarioMs1(Long idUsuarioMs1);
EmpleadoSnapshotMs2 resolverEmpleadoSnapshotActivo(Long idEmpleadoSnapshot);
ProductoSnapshotMs3 resolverProductoVendible(Long idProductoMs3);
SkuSnapshotMs3 resolverSkuVendible(Long idSkuMs3);
PrecioSnapshotMs3 resolverPrecioVigentePorSku(Long idSkuMs3);
PromocionSkuDescuentoSnapshotMs3 resolverMejorPromocionAplicable(Long idSkuMs3, Integer cantidad, LocalDateTime fechaOperacion);
StockSnapshotMs3 resolverStockDisponible(Long idSkuMs3, Long idAlmacenMs3);
Caja resolverCajaAbiertaDelDia();
Venta resolverVentaActiva(Long idVenta);
Boleta resolverBoletaActiva(Long idBoleta);
ConfiguracionTributariaVersion resolverIgvVigente();
ConfiguracionEmpresaVersion resolverEmpresaVigente();
BoletaPlantillaVersion resolverPlantillaVigente();
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `ClienteSnapshotService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
void procesarSnapshotKafka(DomainEventEnvelopeDto<ClienteSnapshotPayloadDto> envelope, String payloadJson);
ClienteSnapshotResponseDto obtenerPorId(Long idSnapshot);
ClienteSnapshotResponseDto obtenerPorUsuarioMs1(Long idUsuarioMs1);
PageResponseDto<ClienteSnapshotResponseDto> listar(ClienteSnapshotFilterDto filter, PageRequestDto page);
PageResponseDto<ClienteLookupResponseDto> lookup(LookupFilterDto filter, PageRequestDto page);
ClienteSnapshotMs2 resolverActivoPorId(Long idSnapshot);
ClienteSnapshotMs2 resolverActivoPorUsuarioMs1(Long idUsuarioMs1);
boolean existeClienteActivoPorUsuarioMs1(Long idUsuarioMs1);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `EmpleadoSnapshotService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
void procesarSnapshotKafka(DomainEventEnvelopeDto<EmpleadoSnapshotPayloadDto> envelope, String payloadJson);
EmpleadoSnapshotResponseDto obtenerPorId(Long idSnapshot);
EmpleadoSnapshotResponseDto obtenerPorUsuarioMs1(Long idUsuarioMs1);
PageResponseDto<EmpleadoSnapshotResponseDto> listar(EmpleadoSnapshotFilterDto filter, PageRequestDto page);
PageResponseDto<EmpleadoLookupResponseDto> lookup(LookupFilterDto filter, PageRequestDto page);
EmpleadoSnapshotMs2 resolverActivoPorUsuarioMs1(Long idUsuarioMs1);
EmpleadoSnapshotMs2 resolverActivoPorId(Long idSnapshot);
void validarEmpleadoPuedeVender(AuthenticatedUserContext actor);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `CatalogoVentaSnapshotService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
void procesarProductoSnapshot(DomainEventEnvelopeDto<ProductoSnapshotPayloadDto> envelope, String payloadJson);
void procesarSkuSnapshot(DomainEventEnvelopeDto<SkuSnapshotPayloadDto> envelope, String payloadJson);
void procesarPrecioSnapshot(DomainEventEnvelopeDto<PrecioSnapshotPayloadDto> envelope, String payloadJson);
void procesarPromocionSnapshot(DomainEventEnvelopeDto<PromocionSnapshotPayloadDto> envelope, String payloadJson);
void procesarPromocionSkuDescuentoSnapshot(DomainEventEnvelopeDto<PromocionSkuDescuentoPayloadDto> envelope, String payloadJson);
ProductoVentaResponseDto obtenerProductoVendible(Long idProductoMs3);
SkuVentaResponseDto obtenerSkuVendible(Long idSkuMs3);
PageResponseDto<ProductoVentaResponseDto> listarProductosVendibles(ProductoVentaFilterDto filter, PageRequestDto page);
PageResponseDto<SkuVentaResponseDto> listarSkusVendibles(SkuVentaFilterDto filter, PageRequestDto page);
PrecioSnapshotMs3 resolverPrecioVigente(Long idSkuMs3);
Optional<PromocionSkuDescuentoSnapshotMs3> resolverPromocionAplicable(Long idSkuMs3, Integer cantidad, LocalDateTime fechaOperacion);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `StockSnapshotService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
void procesarStockSnapshot(DomainEventEnvelopeDto<StockSnapshotPayloadDto> envelope, String payloadJson);
StockVentaResponseDto obtenerStock(Long idSkuMs3, Long idAlmacenMs3);
PageResponseDto<StockVentaResponseDto> listar(StockVentaFilterDto filter, PageRequestDto page);
StockSnapshotMs3 resolverStockDisponible(Long idSkuMs3, Long idAlmacenMs3);
void validarDisponibilidad(Long idSkuMs3, Long idAlmacenMs3, Integer cantidadSolicitada);
void aplicarActualizacionLocalPorSnapshot(StockSnapshotPayloadDto payload, String payloadJson);
boolean tieneStockDisponible(Long idSkuMs3, Long idAlmacenMs3, Integer cantidadSolicitada);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `VentaFisicaService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
VentaCalculoPreviewResponseDto previsualizarVentaFisica(VentaCalculoPreviewRequestDto request, AuthenticatedUserContext actor);
VentaDetailResponseDto crearVentaFisicaConPagoEfectivo(VentaFisicaCreateRequestDto request, PagoEfectivoRequestDto pagoRequest, AuthenticatedUserContext actor);
VentaDetailResponseDto crearVentaFisicaPendientePago(VentaFisicaCreateRequestDto request, AuthenticatedUserContext actor);
VentaDetailResponseDto confirmarVentaFisicaConPagoEfectivo(Long idVenta, PagoEfectivoRequestDto request, AuthenticatedUserContext actor);
VentaDetailResponseDto confirmarVentaFisicaPagadaStripe(Long idVenta, String stripePaymentIntentId, AuthenticatedUserContext actor);
VentaDetailResponseDto anularVentaFisica(Long idVenta, EstadoChangeRequestDto request, AuthenticatedUserContext actor);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `VentaOnlineService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
VentaCalculoPreviewResponseDto previsualizarVentaOnline(VentaCalculoPreviewRequestDto request, AuthenticatedUserContext actor);
VentaDetailResponseDto crearVentaOnlinePendientePago(VentaOnlineCreateRequestDto request, AuthenticatedUserContext actor);
VentaDetailResponseDto confirmarVentaOnlinePagadaStripe(String stripePaymentIntentId);
VentaDetailResponseDto rechazarVentaOnlinePorStripe(String stripePaymentIntentId, String motivo);
VentaDetailResponseDto anularVentaOnline(Long idVenta, EstadoChangeRequestDto request, AuthenticatedUserContext actor);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `VentaConsultaService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
PageResponseDto<VentaResponseDto> listarVentasAdmin(VentaFilterDto filter, PageRequestDto page, AuthenticatedUserContext actor);
PageResponseDto<VentaResponseDto> listarVentasEmpleado(VentaFilterDto filter, PageRequestDto page, AuthenticatedUserContext actor);
PageResponseDto<VentaResponseDto> listarVentasCliente(VentaFilterDto filter, PageRequestDto page, AuthenticatedUserContext actor);
VentaDetailResponseDto obtenerDetalleAdmin(Long idVenta, AuthenticatedUserContext actor);
VentaDetailResponseDto obtenerDetalleEmpleado(Long idVenta, AuthenticatedUserContext actor);
VentaDetailResponseDto obtenerDetalleCliente(Long idVenta, AuthenticatedUserContext actor);
Venta resolverVentaParaProcesoInterno(Long idVenta);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `VentaCalculoService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
VentaCalculoPreviewResponseDto calcularPreview(VentaCalculoPreviewRequestDto request, CanalVenta canalVenta, AuthenticatedUserContext actor);
VentaCalculoResultado calcularVenta(List<VentaDetalleRequestDto> detalles, CanalVenta canalVenta, LocalDateTime fechaOperacion);
VentaLineaCalculada calcularLinea(VentaDetalleRequestDto detalle, ConfiguracionTributariaVersion igv, LocalDateTime fechaOperacion);
BigDecimal calcularDescuentoLinea(PrecioSnapshotMs3 precio, Optional<PromocionSkuDescuentoSnapshotMs3> promocion, Integer cantidad);
BigDecimal calcularIgvLinea(BigDecimal baseGravada, BigDecimal porcentajeIgv);
void validarResultadoNoNegativo(VentaCalculoResultado resultado);
VentaDetalle construirDetalleCongelado(Venta venta, VentaLineaCalculada linea);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `VentaStockCommandService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
void registrarComandosConfirmacionStock(Venta venta, AuthenticatedUserContext actor);
void registrarComandosAnulacionStock(Venta venta, AuthenticatedUserContext actor);
void registrarComandoLiberacionReserva(Venta venta, AuthenticatedUserContext actor);
Ms4StockCommandEventDto construirComandoStock(Venta venta, VentaDetalle detalle, TipoComandoStock tipoComando);
String generarIdempotencyKey(Venta venta, VentaDetalle detalle, TipoComandoStock tipoComando);
void registrarEventoPendienteSiContingencia(Venta venta, VentaDetalle detalle, TipoComandoStock tipoComando);
void registrarOutboxStockCommand(Ms4StockCommandEventDto event, AuthenticatedUserContext actor);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `PagoService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
PagoResponseDto registrarPagoEfectivoAprobado(Long idVenta, PagoEfectivoRequestDto request, AuthenticatedUserContext actor);
PagoResponseDto registrarPagoStripePendiente(Long idVenta, MetodoPago metodoPago, String paymentIntentId, BigDecimal monto, String payloadJson);
PagoResponseDto confirmarPagoStripe(String paymentIntentId, String chargeId, String stripeStatus, String payloadJson);
PagoResponseDto rechazarPagoStripe(String paymentIntentId, String stripeStatus, String motivo, String payloadJson);
PagoResponseDto obtenerPorId(Long idPago);
PagoResponseDto obtenerPorVenta(Long idVenta);
PageResponseDto<PagoResponseDto> listar(PagoFilterDto filter, PageRequestDto page);
Pago resolverPagoPorPaymentIntent(String paymentIntentId);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `StripePaymentService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
StripePaymentIntentResponseDto crearPaymentIntentOnline(PagoStripeOnlineRequestDto request, AuthenticatedUserContext actor);
StripePaymentIntentResponseDto crearPaymentIntentPresencial(Long idVenta, PagoStripePresencialRequestDto request, AuthenticatedUserContext actor);
StripePaymentIntentResponseDto obtenerEstadoPaymentIntent(String paymentIntentId, AuthenticatedUserContext actor);
LookupItemResponseDto obtenerPublishableKeySandbox();
void validarModoSandboxActivo();
void cancelarPaymentIntent(String paymentIntentId, String motivo);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `StripeWebhookService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
StripeWebhookProcessResponseDto procesarWebhook(String rawPayload, String stripeSignatureHeader);
StripeEvento registrarEventoRecibido(String stripeEventId, String eventType, String rawPayload);
StripeWebhookProcessResponseDto procesarPaymentIntentSucceeded(String paymentIntentId, String rawPayload);
StripeWebhookProcessResponseDto procesarPaymentIntentPaymentFailed(String paymentIntentId, String rawPayload);
StripeWebhookProcessResponseDto procesarEventoIgnorado(String stripeEventId, String eventType);
boolean eventoYaProcesado(String stripeEventId);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `BoletaService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
BoletaDetailResponseDto emitirBoletaPorVentaConfirmada(Long idVenta, AuthenticatedUserContext actor);
BoletaDetailResponseDto obtenerDetalleAdmin(Long idBoleta, AuthenticatedUserContext actor);
BoletaDetailResponseDto obtenerDetalleEmpleado(Long idBoleta, AuthenticatedUserContext actor);
BoletaDetailResponseDto obtenerDetalleCliente(Long idBoleta, AuthenticatedUserContext actor);
PageResponseDto<BoletaResponseDto> listarAdmin(BoletaFilterDto filter, PageRequestDto page, AuthenticatedUserContext actor);
PageResponseDto<BoletaResponseDto> listarEmpleado(BoletaFilterDto filter, PageRequestDto page, AuthenticatedUserContext actor);
PageResponseDto<BoletaResponseDto> listarCliente(BoletaFilterDto filter, PageRequestDto page, AuthenticatedUserContext actor);
void programarCorreoBoleta(Long idBoleta, TipoCorreo tipoCorreo, AuthenticatedUserContext actor);
void marcarBoletaEnviadaPorCorreo(Long idBoleta);
Boleta resolverBoletaParaRender(Long idBoleta);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `BoletaRenderService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
String renderizarHtml(Long idBoleta, AuthenticatedUserContext actor);
String renderizarHtmlInterno(Long idBoleta);
String renderizarHtmlDesdeModelo(Map<String, Object> model, String templatePath);
BoletaPreviewResponseDto previsualizarComoDto(Long idBoleta, AuthenticatedUserContext actor);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `BoletaPdfService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
byte[] generarPdf(Long idBoleta, AuthenticatedUserContext actor);
byte[] generarPdfInterno(Long idBoleta);
byte[] convertirHtmlAPdf(String html, String baseUri);
String construirNombreArchivoPdf(Long idBoleta);
ResponseEntity<byte[]> construirRespuestaPdfInline(Long idBoleta, AuthenticatedUserContext actor);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `BoletaTemplateModelFactory`

**Responsabilidad:** 

**Métodos oficiales:**

```java
Map<String, Object> construirModeloBoleta(Long idBoleta);
Map<String, Object> construirModeloBoleta(Boleta boleta, List<BoletaDetalle> detalles);
Map<String, Object> construirModeloCorreoBoleta(Long idBoleta);
String resolverRutaTemplateBoleta(Boleta boleta);
String resolverRutaTemplateCorreo(Boleta boleta);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `BoletaMailService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
void programarEnvioBoletaCompraFisica(Long idBoleta, AuthenticatedUserContext actor);
void programarEnvioBoletaCompraOnline(Long idBoleta, AuthenticatedUserContext actor);
void programarReenvioBoleta(Long idBoleta, BoletaReenvioCorreoRequestDto request, AuthenticatedUserContext actor);
void enviarBoletaDesdeOutbox(CorreoOutbox correoOutbox);
EmailMessage construirEmailBoleta(CorreoOutbox correoOutbox);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `CajaService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
CajaResponseDto abrirCaja(CajaAperturaRequestDto request, AuthenticatedUserContext actor);
CajaCierreResponseDto cerrarCaja(CajaCierreRequestDto request, AuthenticatedUserContext actor);
CajaResponseDto obtenerCajaActual(AuthenticatedUserContext actor);
CajaDetailResponseDto obtenerDetalle(Long idCaja, AuthenticatedUserContext actor);
PageResponseDto<CajaResponseDto> listar(CajaFilterDto filter, PageRequestDto page, AuthenticatedUserContext actor);
CajaResumenDiaResponseDto obtenerResumenDia(LocalDate fechaOperacion, AuthenticatedUserContext actor);
CajaResponseDto registrarAjuste(CajaAjusteRequestDto request, AuthenticatedUserContext actor);
Caja resolverCajaAbiertaParaVentaFisica();
void recalcularTotalesCaja(Long idCaja);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `CajaMovimientoService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
CajaMovimientoResponseDto registrarMovimientoApertura(Caja caja, AuthenticatedUserContext actor);
CajaMovimientoResponseDto registrarMovimientoVentaEfectivo(Caja caja, Venta venta, Pago pago, AuthenticatedUserContext actor);
CajaMovimientoResponseDto registrarMovimientoVentaTarjeta(Caja caja, Venta venta, Pago pago, AuthenticatedUserContext actor);
CajaMovimientoResponseDto registrarMovimientoCierre(Caja caja, AuthenticatedUserContext actor);
CajaMovimientoResponseDto registrarMovimientoAjuste(Caja caja, CajaAjusteRequestDto request, AuthenticatedUserContext actor);
PageResponseDto<CajaMovimientoResponseDto> listarMovimientos(Long idCaja, CajaMovimientoFilterDto filter, PageRequestDto page);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `ReporteEmpleadoService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
ReporteEmpleadoCajaResponseDto obtenerReporteCajaHoy(AuthenticatedUserContext actor);
ReporteEmpleadoCajaResponseDto obtenerReporteCajaPorFecha(LocalDate fecha, AuthenticatedUserContext actor);
ReporteEmpleadoCierreCajaResponseDto obtenerReporteCierreCaja(Long idCaja, AuthenticatedUserContext actor);
PageResponseDto<VentaResumenResponseDto> listarVentasEmpleado(ReporteEmpleadoCajaFilterDto filter, PageRequestDto page, AuthenticatedUserContext actor);
ReporteVentaPorMetodoPagoDto obtenerResumenPorMetodoPago(LocalDate fecha, AuthenticatedUserContext actor);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `ReporteAdminService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
ReporteAdminVentasResponseDto generarReporteVentas(ReporteAdminVentasRequestDto request, AuthenticatedUserContext actor);
ReporteAdminFinancieroResponseDto generarReporteFinanciero(ReporteAdminFinancieroRequestDto request, AuthenticatedUserContext actor);
List<ReporteProductoVendidoDto> obtenerProductosMasVendidos(ReporteVentasAdminFilterDto filter);
List<ReporteVentaPorEmpleadoDto> obtenerVentasPorEmpleado(ReporteVentasAdminFilterDto filter);
List<ReporteVentaPorMetodoPagoDto> obtenerVentasPorMetodoPago(ReporteVentasAdminFilterDto filter);
List<ReporteVentaPorCanalDto> obtenerVentasPorCanal(ReporteVentasAdminFilterDto filter);
List<ReporteVentaPorCategoriaDto> obtenerVentasPorCategoria(ReporteVentasAdminFilterDto filter);
ReporteGananciaEstimadaDto calcularGananciaEstimada(ReporteFinancieroAdminFilterDto filter);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `KafkaEventoConsumidoService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
boolean existeEvento(UUID eventId);
KafkaEventoConsumido registrarRecibido(DomainEventEnvelopeDto<?> envelope, String topic, String payloadJson);
void marcarProcesado(UUID eventId);
void marcarIgnorado(UUID eventId, String motivo);
void marcarError(UUID eventId, Exception exception);
KafkaEventoConsumido resolverPorEventId(UUID eventId);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `EventoDominioOutboxService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
EventoDominioOutbox crearEvento(String aggregateType, String aggregateId, String topic, String eventType, Object payload, AuthenticatedUserContext actor);
EventoDominioOutbox crearEventoStockCommand(Ms4StockCommandEventDto event, AuthenticatedUserContext actor);
PageResponseDto<EventoDominioOutboxResponseDto> listar(EventoDominioOutboxFilterDto filter, PageRequestDto page);
void reintentar(Long idOutbox, AuthenticatedUserContext actor);
void descartar(Long idOutbox, EstadoChangeRequestDto request, AuthenticatedUserContext actor);
List<EventoDominioOutbox> reclamarBatchPendiente(String workerId, int batchSize);
void marcarPublicando(Long idOutbox, String workerId);
void marcarPublicado(Long idOutbox);
void marcarError(Long idOutbox, String errorDetalle);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `InventarioEventoPendienteService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
InventarioEventoPendienteResponseDto registrarPendiente(Venta venta, VentaDetalle detalle, TipoComandoStock tipoComando, String payloadJson);
PageResponseDto<InventarioEventoPendienteResponseDto> listar(InventarioEventoPendienteFilterDto filter, PageRequestDto page);
PageResponseDto<InventarioEventoPendienteResponseDto> listarPendientesParaMs3(InventarioEventoPendienteFilterDto filter, PageRequestDto page);
void marcarSincronizado(Long idPendiente, String detalleResultado);
void marcarError(Long idPendiente, String errorDetalle);
void reintentar(Long idPendiente, AuthenticatedUserContext actor);
void reconciliarPendientes(AuthenticatedUserContext actor);
long contarPendientesActivos();
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `CorreoOutboxService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
CorreoOutboxResponseDto programarCorreo(TipoCorreo tipoCorreo, String entidadOrigen, Long idEntidadOrigen, Long idBoleta, String destinatarioEmail, String destinatarioNombre, String asunto, AuthenticatedUserContext actor);
void programarAlertaAdministradores(TipoCorreo tipoCorreo, String asunto, String detalle, AuthenticatedUserContext actor);
PageResponseDto<CorreoOutboxResponseDto> listar(CorreoOutboxFilterDto filter, PageRequestDto page);
void reintentar(Long idCorreoOutbox, CorreoOutboxReintentoRequestDto request, AuthenticatedUserContext actor);
void descartar(Long idCorreoOutbox, EstadoChangeRequestDto request, AuthenticatedUserContext actor);
List<CorreoOutbox> reclamarBatchPendiente(String workerId, int batchSize);
void marcarEnviando(Long idCorreoOutbox, String workerId);
void marcarEnviado(Long idCorreoOutbox);
void marcarError(Long idCorreoOutbox, String errorDetalle);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `EmailService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
void enviar(EmailMessage message);
void enviarConAdjuntos(EmailMessage message, List<EmailAttachment> attachments);
void validarDestinatario(String email);
EmailMessage construirMensaje(String to, String nombre, String subject, String htmlBody);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `ContingenciaService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
ModoContingenciaResponseDto activarContingencia(ContingenciaActivarRequestDto request, AuthenticatedUserContext actor);
ModoContingenciaResponseDto finalizarContingencia(ContingenciaFinalizarRequestDto request, AuthenticatedUserContext actor);
ModoContingenciaResponseDto obtenerContingenciaActual();
PageResponseDto<ModoContingenciaResponseDto> listar(ModoContingenciaFilterDto filter, PageRequestDto page);
void validarVentaPermitidaSegunEstadoMs3();
boolean estaContingenciaActivaParaMs3();
void registrarDeteccionMs3Caido(String detalleTecnico);
void registrarRecuperacionMs3(String detalleTecnico);
void notificarAdministradoresMs3Caido(String detalle);
void reconciliarEventosPendientes(AuthenticatedUserContext actor);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

### `AuditoriaFuncionalService`

**Responsabilidad:** Contrato de servicio de aplicación. Define la funcionalidad necesaria para que MS4 opere completo según RN.

**Métodos oficiales:**

```java
void registrarExito(String entidad, Long entidadId, String accion, AuthenticatedUserContext actor, Object detalle);
void registrarErrorUsuario(String entidad, Long entidadId, String accion, AuthenticatedUserContext actor, String codigoError, String mensaje);
void registrarErrorTecnico(String entidad, Long entidadId, String accion, AuthenticatedUserContext actor, Exception exception);
AuditoriaResponseDto obtenerPorId(Long idAuditoria);
PageResponseDto<AuditoriaResponseDto> listar(AuditoriaFilterDto filter, PageRequestDto page);
```

**Reglas de implementación:** debe validar permisos con policy antes de ejecutar acciones sensibles, usar validators para reglas de negocio, usar mapper para conversiones, repository para persistencia, auditoría funcional para acciones críticas y respuesta DTO estable. No debe duplicar cálculo ni acceder a SDKs si existe integration dedicada.

## 13. Implementaciones `service.impl.*`

Cada clase `*ServiceImpl` implementa exactamente su contrato. Debe usar `@Service`, constructor injection, transacciones explícitas y logging seguro.

| Implementación | Responsabilidad |
| --- | --- |
| AssetCloudinaryServiceImpl | Implementa `AssetCloudinaryService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| ConfiguracionEmpresaServiceImpl | Implementa `ConfiguracionEmpresaService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| ConfiguracionTributariaServiceImpl | Implementa `ConfiguracionTributariaService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| BoletaPlantillaServiceImpl | Implementa `BoletaPlantillaService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| SerieBoletaServiceImpl | Implementa `SerieBoletaService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| LookupServiceImpl | Implementa `LookupService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| Ms4ReferenceResolverServiceImpl | Implementa `Ms4ReferenceResolverService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| ClienteSnapshotServiceImpl | Implementa `ClienteSnapshotService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| EmpleadoSnapshotServiceImpl | Implementa `EmpleadoSnapshotService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| CatalogoVentaSnapshotServiceImpl | Implementa `CatalogoVentaSnapshotService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| StockSnapshotServiceImpl | Implementa `StockSnapshotService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| VentaFisicaServiceImpl | Implementa `VentaFisicaService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| VentaOnlineServiceImpl | Implementa `VentaOnlineService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| VentaConsultaServiceImpl | Implementa `VentaConsultaService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| VentaCalculoServiceImpl | Implementa `VentaCalculoService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| VentaStockCommandServiceImpl | Implementa `VentaStockCommandService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| PagoServiceImpl | Implementa `PagoService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| StripePaymentServiceImpl | Implementa `StripePaymentService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| StripeWebhookServiceImpl | Implementa `StripeWebhookService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| BoletaServiceImpl | Implementa `BoletaService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| BoletaRenderServiceImpl | Implementa `BoletaRenderService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| BoletaPdfServiceImpl | Implementa `BoletaPdfService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| BoletaTemplateModelFactoryImpl | Implementa `BoletaTemplateModelFactory`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| BoletaMailServiceImpl | Implementa `BoletaMailService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| CajaServiceImpl | Implementa `CajaService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| CajaMovimientoServiceImpl | Implementa `CajaMovimientoService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| ReporteEmpleadoServiceImpl | Implementa `ReporteEmpleadoService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| ReporteAdminServiceImpl | Implementa `ReporteAdminService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| KafkaEventoConsumidoServiceImpl | Implementa `KafkaEventoConsumidoService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| EventoDominioOutboxServiceImpl | Implementa `EventoDominioOutboxService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| InventarioEventoPendienteServiceImpl | Implementa `InventarioEventoPendienteService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| CorreoOutboxServiceImpl | Implementa `CorreoOutboxService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| EmailServiceImpl | Implementa `EmailService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| ContingenciaServiceImpl | Implementa `ContingenciaService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |
| AuditoriaFuncionalServiceImpl | Implementa `AuditoriaFuncionalService`. No agrega métodos públicos fuera del contrato salvo auxiliares privados. Orquesta con policy, validator, repository, mapper e integration correspondiente. |

## 14. Validators oficiales

Regla: los validators no autorizan por rol ni consultan SDKs. Deben lanzar `ValidationException` o `ConflictException` con código funcional claro.

| Validator | Métodos obligatorios |
| --- | --- |
| VentaValidator | `validarCrearVentaFisica(...)`, `validarCrearVentaOnline(...)`, `validarVentaConfirmable(...)`, `validarVentaAnulable(...)`, `validarClienteObligatorio(...)`, `validarCanalMetodoPago(...)` |
| VentaDetalleValidator | `validarDetallesNoVacios(...)`, `validarCantidadPositiva(...)`, `validarSkuNoDuplicado(...)`, `validarAlmacenObligatorio(...)`, `validarDetalleVendible(...)` |
| VentaCalculoValidator | `validarPreviewRequest(...)`, `validarTotalesNoNegativos(...)`, `validarPrecioVigente(...)`, `validarPromocionVigente(...)`, `validarIgvVigente(...)` |
| StockDisponibilidadValidator | `validarStockDisponible(...)`, `validarContingenciaSiMs3Caido(...)`, `validarAlmacenActivo(...)`, `validarStockSnapshotRecienteSegunPolitica(...)` |
| PagoValidator | `validarPagoEfectivo(...)`, `validarPagoStripePresencial(...)`, `validarPagoStripeOnline(...)`, `validarMontoCoincideVenta(...)`, `validarNoDoblePago(...)` |
| StripeWebhookValidator | `validarFirma(...)`, `validarEventoSoportado(...)`, `validarIdempotencia(...)`, `validarPaymentIntentExiste(...)` |
| CajaValidator | `validarApertura(...)`, `validarCajaNoDuplicadaPorDia(...)`, `validarCajaAbiertaParaVenta(...)`, `validarAjuste(...)` |
| CajaCierreValidator | `validarCierre(...)`, `validarMontoReal(...)`, `validarCajaPerteneceADia(...)`, `validarCajaNoCerrada(...)` |
| BoletaValidator | `validarVentaConfirmada(...)`, `validarBoletaNoDuplicadaPorVenta(...)`, `validarSerieDisponible(...)`, `validarCorreoCliente(...)` |
| ConfiguracionEmpresaValidator | `validarNuevaVersion(...)`, `validarRuc(...)`, `validarVigencias(...)`, `validarAssetLogo(...)` |
| ConfiguracionTributariaValidator | `validarNuevaVersionIgv(...)`, `validarPorcentaje(...)`, `validarMotivo(...)`, `validarSolapamientoVigencias(...)` |
| SerieBoletaValidator | `validarCrearSerie(...)`, `validarRangoNumerico(...)`, `validarNoReutilizarSerie(...)`, `validarPuedeInactivar(...)` |
| SnapshotValidator | `validarEnvelope(...)`, `validarVersionEvento(...)`, `validarPayloadJson(...)`, `validarEventoNoObsoleto(...)` |
| LookupValidator | `validarFiltroLookup(...)`, `validarLimiteResultados(...)`, `validarPermisoLookup(...)` |
| ContingenciaValidator | `validarActivacion(...)`, `validarFinalizacion(...)`, `validarGuardarEventosPendientes(...)`, `validarReconciliacion(...)` |
| CorreoOutboxValidator | `validarProgramacion(...)`, `validarDestinatario(...)`, `validarReintento(...)`, `validarNoDescartarCorreoEnviando(...)` |
| OutboxValidator | `validarEvento(...)`, `validarReintento(...)`, `validarDescartar(...)`, `validarMaxAttempts(...)` |

## 15. Policies oficiales

Regla: las policies autorizan. No validan montos ni stock. Deben lanzar `ForbiddenException` cuando el actor no puede ejecutar la acción.

| Policy | Métodos obligatorios |
| --- | --- |
| VentaPolicy | `authorizeCrearVentaFisica(...)`, `authorizeCrearVentaOnline(...)`, `authorizeVerVenta(...)`, `authorizeAnularVenta(...)`, `authorizePreviewCalculo(...)` |
| BoletaPolicy | `authorizeVerBoleta(...)`, `authorizeGenerarPdf(...)`, `authorizeReenviarCorreo(...)`, `authorizeVerBoletaAdmin(...)` |
| CajaPolicy | `authorizeAbrirCaja(...)`, `authorizeCerrarCaja(...)`, `authorizeVerCaja(...)`, `authorizeAjustarCaja(...)` |
| ReportePolicy | `authorizeReporteEmpleado(...)`, `authorizeReporteAdmin(...)` |
| ConfiguracionPolicy | `authorizeGestionEmpresa(...)`, `authorizeGestionTributaria(...)`, `authorizeGestionSerie(...)` |
| OutboxPolicy | `authorizeListarOutbox(...)`, `authorizeReintentarOutbox(...)`, `authorizeDescartarOutbox(...)` |
| CorreoOutboxPolicy | `authorizeListarCorreos(...)`, `authorizeReintentarCorreo(...)`, `authorizeDescartarCorreo(...)` |
| ContingenciaPolicy | `authorizeActivarContingencia(...)`, `authorizeFinalizarContingencia(...)`, `authorizeReconciliar(...)` |
| SnapshotPolicy | `authorizeConsultarSnapshots(...)` |
| LookupPolicy | `authorizeLookupClientes(...)`, `authorizeLookupCatalogo(...)`, `authorizeLookupCaja(...)` |
| AuditoriaPolicy | `authorizeConsultarAuditoria(...)` |

## 16. Kafka consumers, handlers y outbox

### Consumers

| Consumer | Responsabilidad |
| --- | --- |
| ClienteSnapshotConsumer | Escucha `ms2.cliente.snapshot.v1` y delega a `ClienteSnapshotEventHandler`. |
| EmpleadoSnapshotConsumer | Escucha `ms2.empleado.snapshot.v1` y delega a `EmpleadoSnapshotEventHandler`. |
| ProductoSnapshotConsumer | Escucha `ms3.producto.snapshot.v1` y delega a `ProductoSnapshotEventHandler`. |
| PrecioSnapshotConsumer | Escucha `ms3.precio.snapshot.v1` y delega a `PrecioSnapshotEventHandler`. |
| PromocionSnapshotConsumer | Escucha `ms3.promocion.snapshot.v1` y delega a `PromocionSnapshotEventHandler`. |
| StockSnapshotConsumer | Escucha `ms3.stock.snapshot.v1` y delega a `StockSnapshotEventHandler`. |
| MovimientoInventarioConsumer | Escucha `ms3.movimiento-inventario.v1` solo para trazabilidad/diagnóstico o actualización derivada si aplica. |

### Handlers

| Handler | Responsabilidad |
| --- | --- |
| ClienteSnapshotEventHandler | Valida idempotencia y aplica snapshot de cliente. |
| EmpleadoSnapshotEventHandler | Valida idempotencia y aplica snapshot de empleado. |
| ProductoSnapshotEventHandler | Valida idempotencia y aplica producto/SKU embebidos si el evento lo trae. |
| PrecioSnapshotEventHandler | Valida idempotencia y actualiza precio vigente/histórico local. |
| PromocionSnapshotEventHandler | Valida idempotencia y actualiza promoción/descuentos SKU. |
| StockSnapshotEventHandler | Valida idempotencia y actualiza stock snapshot. |
| MovimientoInventarioEventHandler | Registra evento consumido y no altera stock si no hay contrato explícito de snapshot. |

### Producer/Outbox

| Clase | Responsabilidad |
| --- | --- |
| KafkaMessagePublisher | Encapsula KafkaTemplate y publica mensajes serializados. Nadie fuera de outbox debe usar KafkaTemplate. |
| OutboxPublisherScheduler | Scheduler que dispara publicación de eventos pendientes. |
| OutboxPublisher | Reclama batch, publica, marca PUBLICADO/ERROR. |
| OutboxLockService | Controla lock por worker para evitar doble publicación. |
| OutboxPayloadSerializer | Serializa/deserializa payloads de Outbox sin lógica de negocio. |

**Métodos mínimos de consumers:** `consume(String payload, Acknowledgment ack)`, `deserializeEnvelope(...)`, `delegateToHandler(...)`, `acknowledgeOnlyAfterSuccess(...)`.  
**Métodos mínimos de handlers:** `handle(envelope, rawJson)`, `validateEnvelope(...)`, `registerConsumed(...)`, `applySnapshot(...)`, `markProcessed(...)`, `markError(...)`.

## 17. Correo outbox y modelos de correo

| Clase | Responsabilidad |
| --- | --- |
| CorreoOutboxScheduler | Scheduler que dispara procesamiento de correo_outbox. |
| CorreoOutboxProcessor | Reclama correos, construye mensaje y adjuntos, envía y marca estado. |
| CorreoOutboxLockService | Controla lock por worker para evitar doble envío. |
| JavaMailEmailSender | Adaptador JavaMailSender. No decide negocio ni consulta boletas. |
| EmailTemplateRenderer | Renderiza templates HTML de correo. |
| EmailMessage | Modelo interno de correo. |
| EmailAttachment | Modelo interno de adjunto generado en memoria. |

**Regla:** `VentaService` y `BoletaService` solo programan correo. El envío real sucede en `CorreoOutboxProcessor`. Si SMTP falla, la venta no se revierte.

## 18. Integraciones externas e internas

| Clase | Responsabilidad |
| --- | --- |
| StripeClient | Configura SDK Stripe Sandbox y valida que no se usen llaves live. |
| StripePaymentIntentClient | Crea, consulta y cancela PaymentIntent en Stripe Sandbox. |
| StripeWebhookVerifier | Valida firma de webhook Stripe. |
| StripeExceptionTranslator | Traduce errores Stripe a excepciones funcionales seguras. |
| CloudinaryAssetClient | Sube/reemplaza assets visuales; jamás sube PDFs de boleta. |
| CloudinaryExceptionTranslator | Traduce errores Cloudinary. |
| Ms1AdminContactClient | Consulta o resuelve destinatarios ADMIN para alertas cuando aplique. |
| AdminRecipientProvider | Proveedor de correos ADMIN: MS1 si disponible y fallback config. |
| Ms3InternalStockSyncClient | Cliente HTTP interno de soporte/reconciliación con MS3; no reemplaza Kafka. |

**Regla:** ninguna integración decide negocio. Stripe nunca usa llaves live. Cloudinary nunca recibe PDF de boleta. MS3 HTTP interno no reemplaza Kafka.

## 19. Shared y excepciones

| Clase | Responsabilidad |
| --- | --- |
| ApiPaths | Constantes de rutas oficiales. |
| Ms4Constants | Constantes de dominio MS4. |
| KafkaTopics | Constantes de topics. |
| ErrorCodes | Códigos funcionales de error. |
| HeaderNames | Nombres de headers: Authorization, X-Internal-Service-Key, request/correlation. |
| BusinessException | Excepción base de negocio. |
| ValidationException | Error de validación funcional. |
| NotFoundException | Recurso inexistente o inactivo. |
| ConflictException | Conflicto de estado o duplicado. |
| ForbiddenException | Acceso funcional denegado. |
| UnauthorizedException | Autenticación inválida. |
| ExternalServiceException | Falla de dependencia externa. |
| KafkaPublishException | Falla publicación Kafka. |
| StripePaymentException | Falla Stripe. |
| MailSendException | Falla SMTP. |
| GlobalExceptionHandler | Handler global de respuestas de error y logging seguro. |
| ApiResponseFactory | Crea respuestas exitosas estandarizadas. |
| ErrorResponseFactory | Crea errores estandarizados. |
| PaginationService | Convierte PageRequestDto y Page de JPA a PageResponseDto. |
| AuditContextHolder | Contexto temporal de auditoría/request. |
| AuditEventFactory | Construye detalle JSON de auditoría. |
| AuditRegistrar | Facilita auditoría desde services. |
| RequestMetadataExtractor | Extrae requestId, correlationId, IP y user-agent. |
| EntityLookupService | Utilidad común para buscar entidades con mensaje uniforme. |
| ActiveRecordResolver | Resuelve entidades activas por estado. |

**Regla de errores:** `GlobalExceptionHandler` debe registrar errores técnicos con stacktrace completo y devolver mensaje seguro. Errores funcionales deben conservar código y mensaje entendible.

## 20. Utilidades

| Clase | Responsabilidad |
| --- | --- |
| DateTimeUtil | Operaciones de fechas usando Clock. |
| MoneyUtil | Redondeo monetario, escala y validaciones BigDecimal. |
| IgvCalculator | Cálculo único de IGV. |
| DiscountCalculator | Cálculo único de descuentos por monto/porcentaje. |
| HashUtil | Hash de payloads congelados. |
| JsonUtil | Serialización/deserialización JSON. |
| CodigoGenerator | Códigos VEN, PAG, CAJ, B001-00000001, etc. |
| IdempotencyKeyGenerator | Genera claves idempotentes de stock y procesos. |

**Regla:** utilidades son puras y sin estado; no acceden a repositories, services ni SDKs.

## 21. Métodos de flujo crítico por caso de uso

### 21.1. Venta física con efectivo

```text
EmpleadoVentaController.crearVentaFisica
-> VentaPolicy.authorizeCrearVentaFisica
-> VentaFisicaService.crearVentaFisicaConPagoEfectivo
-> EmpleadoSnapshotService.validarEmpleadoPuedeVender
-> CajaService.resolverCajaAbiertaParaVentaFisica
-> VentaValidator.validarCrearVentaFisica
-> VentaCalculoService.calcularVenta
-> PagoService.registrarPagoEfectivoAprobado
-> BoletaService.emitirBoletaPorVentaConfirmada
-> BoletaMailService.programarEnvioBoletaCompraFisica
-> VentaStockCommandService.registrarComandosConfirmacionStock
-> CajaMovimientoService.registrarMovimientoVentaEfectivo
-> AuditoriaFuncionalService.registrarExito
```

### 21.2. Venta física con Stripe presencial Sandbox

```text
EmpleadoPagoController.crearPaymentIntentPresencial
-> StripePaymentService.crearPaymentIntentPresencial
-> PagoService.registrarPagoStripePendiente
-> StripeWebhookController.procesarWebhook
-> StripeWebhookService.procesarPaymentIntentSucceeded
-> PagoService.confirmarPagoStripe
-> VentaFisicaService.confirmarVentaFisicaPagadaStripe
-> BoletaService.emitirBoletaPorVentaConfirmada
-> BoletaMailService.programarEnvioBoletaCompraFisica
-> VentaStockCommandService.registrarComandosConfirmacionStock
-> CajaMovimientoService.registrarMovimientoVentaTarjeta
```

### 21.3. Venta online con Stripe Sandbox

```text
ClienteVentaController.crearVentaOnlinePendientePago
-> VentaOnlineService.crearVentaOnlinePendientePago
-> Ms4ReferenceResolverService.resolverClientePorUsuarioMs1
-> VentaCalculoService.calcularVenta
-> StripePaymentService.crearPaymentIntentOnline
-> PagoService.registrarPagoStripePendiente
-> StripeWebhookService.procesarPaymentIntentSucceeded
-> VentaOnlineService.confirmarVentaOnlinePagadaStripe
-> BoletaService.emitirBoletaPorVentaConfirmada
-> BoletaMailService.programarEnvioBoletaCompraOnline
-> VentaStockCommandService.registrarComandosConfirmacionStock
```

### 21.4. Render/PDF de boleta

```text
Cliente/Empleado/AdminBoletaController.preview/pdf
-> BoletaPolicy.authorizeVerBoleta / authorizeGenerarPdf
-> BoletaRenderService.renderizarHtml or BoletaPdfService.generarPdf
-> BoletaTemplateModelFactory.construirModeloBoleta
-> BoletaService.resolverBoletaParaRender
-> Thymeleaf/OpenHTMLToPDF en memoria
```

### 21.5. Contingencia MS3

```text
ContingenciaService.registrarDeteccionMs3Caido
-> CorreoOutboxService.programarAlertaAdministradores
-> ContingenciaService.validarVentaPermitidaSegunEstadoMs3
-> AdminContingenciaController.activarContingencia
-> ContingenciaService.activarContingencia
-> VentaStockCommandService.registrarEventoPendienteSiContingencia
-> AdminContingenciaController.reconciliarEventosPendientes
-> InventarioEventoPendienteService.reconciliarPendientes
-> EventoDominioOutboxService.crearEventoStockCommand
```

## 22. Reglas finales para implementación con calidad

- No crear clases no listadas salvo mejora justificada y sin duplicar responsabilidades.
- No crear `Carrito*`, `Checkout*`, `Factura*`, `NotaCredito*`, `NotaDebito*`, `SunatClient`, `OseClient`, `BoletaPdfStorageService` ni `PdfCloudinaryStorageService`.
- Todo endpoint listable debe soportar paginación y filtros.
- Todo selector de FK debe tener lookup.
- Todo service mutador debe auditar éxito y errores relevantes.
- Todo proceso asíncrono debe ser idempotente y reintentable.
- Toda venta confirmada debe generar boleta, programar correo y registrar comandos de stock.
- MS4 nunca modifica stock oficial; solo consume snapshot y produce comandos para MS3.
- MS4 nunca crea ni edita clientes/empleados oficiales; solo consume snapshots de MS2.
- La documentación se considera completa para implementar el MS4 productivo en esta etapa; mejoras futuras deben ser extensiones compatibles, no reemplazos de responsabilidades.
