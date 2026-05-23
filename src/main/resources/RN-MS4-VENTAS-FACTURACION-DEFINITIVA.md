# RN-MS4 DEFINITIVA: `ms-ventas-facturacion`

**Microservicio:** `ms-ventas-facturacion`  
**Puerto local:** `8084`  
**Paquete raíz:** `com.upsjb.ms4`  
**Base de datos:** SQL Server  
**Frontend:** Angular 18 mediante API Gateway  
**Versión:** RN definitiva para implementación  
**Fecha:** 2026-05-21  
**Estado:** Documento oficial de trabajo

---

# 1. Finalidad del MS4

El microservicio **MS4 - ms-ventas-facturacion** debe encargarse exclusivamente del proceso comercial final del sistema.

MS4 debe vender, cobrar, emitir boletas, enviar boletas por correo, manejar caja, generar reportes, auditar operaciones y sincronizar el impacto comercial con inventario mediante Kafka.

MS4 debe operar dentro de esta arquitectura:

```text
Angular
   ↓
API Gateway :8080
   ↓
Microservicios
   ├── ms-seguridad-usuarios :8081
   ├── ms-personas-clientes-empleados :8082
   ├── ms-catalogo-inventario :8083
   └── ms-ventas-facturacion :8084
```

Reglas obligatorias:

```text
Angular consume únicamente el API Gateway.
MS4 valida JWT emitidos por MS1.
MS4 no autentica usuarios.
MS4 no administra clientes ni empleados oficiales.
MS4 no administra productos, precios, promociones ni stock oficial.
MS4 consume snapshots locales de MS2 y MS3.
MS4 produce comandos de stock hacia MS3.
MS4 genera boletas en vivo con Thymeleaf.
MS4 no almacena PDFs de boletas en Cloudinary.
MS4 envía boletas por correo generándolas en memoria.
MS4 usa Stripe únicamente en modo Sandbox.
```

---

# 2. Propiedad de dominio por microservicio

## 2.1. MS1: seguridad y usuarios

MS1 es dueño de:

```text
- usuarios
- roles
- login
- logout
- sesiones
- refresh tokens
- access tokens JWT
- recuperación de contraseña
- auditoría de seguridad
```

MS4 solo valida el JWT emitido por MS1.

## 2.2. MS2: personas, clientes y empleados

MS2 es dueño de:

```text
- personas
- empresas
- clientes
- empleados
- teléfonos
- direcciones
- ubigeo
- datos funcionales de clientes
- datos funcionales de empleados
```

MS4 solo consume snapshots Kafka de clientes y empleados.

## 2.3. MS3: catálogo e inventario

MS3 es dueño de:

```text
- catálogo
- productos
- SKU
- atributos
- imágenes de productos
- precios versionados
- promociones versionadas
- almacenes
- stock físico
- stock reservado
- stock disponible
- movimientos
- kardex
- compras de inventario
```

MS4 solo consume snapshots de catálogo, precio, promoción y stock.  
MS4 envía comandos a MS3 para reservar, confirmar, liberar o anular stock.

## 2.4. MS4: ventas y facturación funcional

MS4 es dueño de:

```text
- venta
- venta_detalle
- carrito
- carrito_detalle
- checkout
- pago
- stripe_payment
- boleta
- boleta_detalle
- serie_comprobante
- configuracion_empresa
- configuracion_tributaria
- caja
- caja_movimiento
- reporte_venta
- reporte_caja
- auditoria_funcional
- evento_dominio_outbox
- correo_outbox
- inventario_evento_pendiente_ms4
- modo_contingencia
- cliente_snapshot_ms2
- empleado_snapshot_ms2
- producto_snapshot_ms3
- sku_snapshot_ms3
- precio_snapshot_ms3
- promocion_snapshot_ms3
- promocion_sku_descuento_snapshot_ms3
- stock_snapshot_ms3
- asset_cloudinary
```

---

# 3. Seguridad obligatoria

MS4 debe ser un **OAuth2 Resource Server**.

Toda ruta protegida debe exigir:

```http
Authorization: Bearer <access_token>
```

MS4 debe validar:

```text
- firma del JWT
- expiración
- issuer
- audience si aplica
- tipo de token access
- id_usuario_ms1
- username
- email
- rol
- authorities
- sesión si se usa claim sid
```

MS4 no debe:

```text
- emitir JWT
- renovar JWT
- validar password
- guardar password
- crear sesiones
- cerrar sesiones
- modificar roles
```

Las rutas internas deben protegerse con:

```http
X-Internal-Service-Key: <internal-key>
```

---

# 4. Roles oficiales en MS4

## 4.1. ADMIN

ADMIN puede:

```text
- consultar todas las ventas
- consultar detalle de cualquier venta
- consultar boletas
- generar vista previa de cualquier boleta
- reenviar boleta por correo
- consultar auditoría funcional
- consultar reportes globales
- consultar reportes financieros
- consultar cajas
- consultar cierres de caja
- gestionar configuración empresarial
- gestionar configuración tributaria
- gestionar IGV vigente
- gestionar series de boleta
- consultar eventos Outbox
- reintentar eventos Kafka
- activar modo contingencia
- finalizar modo contingencia
- reconciliar eventos pendientes con MS3
```

ADMIN no debe:

```text
- crear productos desde MS4
- crear clientes oficiales desde MS4
- crear empleados oficiales desde MS4
- modificar stock directamente desde MS4
- modificar kardex desde MS4
```

## 4.2. EMPLEADO

EMPLEADO puede:

```text
- abrir caja
- consultar caja abierta
- cerrar caja
- realizar ventas físicas
- asociar un cliente existente a una venta física
- buscar clientes desde snapshot local
- buscar productos/SKU desde snapshot local
- registrar pago en efectivo
- registrar pago con tarjeta mediante Stripe Sandbox en tienda
- emitir boleta de venta física
- visualizar boleta de una venta que realizó
- reenviar boleta por correo al cliente
- consultar sus ventas
- consultar su reporte de caja del día
```

EMPLEADO no puede:

```text
- cambiar IGV
- cambiar datos de empresa emisora
- gestionar series de boleta
- reintentar eventos Kafka
- activar contingencia
- consultar auditoría global
- consultar reportes financieros globales
- consultar ventas de otros empleados salvo que una regla futura lo autorice
```

## 4.3. CLIENTE

CLIENTE puede:

```text
- consultar productos disponibles para compra online
- gestionar su carrito
- iniciar checkout online
- pagar con tarjeta mediante Stripe Sandbox
- consultar sus compras
- visualizar sus boletas
- descargar su boleta generada en vivo
- recibir su boleta por correo
```

CLIENTE no puede:

```text
- comprar como otro cliente
- registrar venta física
- pagar online con efectivo
- consultar ventas de otros clientes
- consultar caja
- consultar reportes administrativos
- activar contingencia
```

---

# 5. Integración con MS2

MS4 debe consumir eventos Kafka de MS2.  
MS4 no debe producir eventos hacia MS2.

Topics consumidos:

```text
ms2.cliente.snapshot.v1
ms2.empleado.snapshot.v1
```

MS4 debe guardar snapshots locales para operar aunque MS2 no esté disponible.

## 5.1. Reglas sobre clientes

MS4 no crea ni edita clientes oficiales.

MS4 usa `cliente_snapshot_ms2` para:

```text
- asociar cliente a venta física
- identificar cliente online autenticado
- obtener nombre o razón social
- obtener documento o RUC
- obtener correo para envío de boleta
- obtener teléfono de contacto
- obtener dirección si el flujo de entrega lo requiere
```

Si MS2 está caído, MS4 puede:

```text
- listar clientes ya sincronizados
- usar cliente snapshot en venta
- emitir boleta con datos congelados
```

Si MS2 está caído, MS4 no puede:

```text
- crear cliente
- editar cliente
- crear dirección oficial
- editar dirección oficial
- crear teléfono oficial
- editar teléfono oficial
```

## 5.2. Tabla `cliente_snapshot_ms2`

```text
id
id_cliente_ms2
id_usuario_ms1
tipo_cliente
estado_cliente
tipo_documento
numero_documento
nombres
ape_paterno
ape_materno
ruc
razon_social
nombre_comercial
correo
telefono_principal
direccion_principal
distrito
provincia
departamento
ubigeo
payload_json
version_evento
event_id
fecha_evento
fecha_sincronizacion
activo
created_at
updated_at
```

## 5.3. Reglas sobre empleados

MS4 no crea ni edita empleados oficiales.

MS4 usa `empleado_snapshot_ms2` para:

```text
- validar vendedor activo
- registrar empleado que abre caja
- registrar empleado que realiza venta física
- registrar empleado que cierra caja
- generar reportes por empleado
```

## 5.4. Tabla `empleado_snapshot_ms2`

```text
id
id_empleado_ms2
id_usuario_ms1
codigo_empleado
nombres
ape_paterno
ape_materno
correo
area
estado_empleado
payload_json
version_evento
event_id
fecha_evento
fecha_sincronizacion
activo
created_at
updated_at
```

---

# 6. Integración con MS3

MS4 debe consumir eventos Kafka de MS3 y producir comandos de stock hacia MS3.

Topics consumidos desde MS3:

```text
ms3.producto.snapshot.v1
ms3.precio.snapshot.v1
ms3.promocion.snapshot.v1
ms3.stock.snapshot.v1
ms3.movimiento-inventario.v1
```

Topics producidos por MS4 hacia MS3:

```text
ms4.stock.command.v1
ms4.stock.reconciliation.v1
```

MS4 debe trabajar con snapshots locales. No debe depender de llamadas HTTP a MS3 para cada consulta de venta.

## 6.1. Regla definitiva sobre promociones

Las promociones pertenecen a MS3.

```text
MS3 = maestro de promociones.
MS4 = consumidor y aplicador de promociones en ventas.
```

MS4 debe:

```text
- consumir promociones vigentes publicadas por MS3
- aplicar la promoción al momento de vender
- congelar el descuento aplicado en venta_detalle
- mantener trazabilidad con id_promocion_ms3 y codigo_promocion
```

MS4 no debe:

```text
- crear promociones
- editar promociones
- eliminar promociones
- cambiar vigencia de promociones
```

## 6.2. Tablas snapshot de MS3

MS4 debe implementar:

```text
producto_snapshot_ms3
sku_snapshot_ms3
precio_snapshot_ms3
promocion_snapshot_ms3
promocion_sku_descuento_snapshot_ms3
stock_snapshot_ms3
```

## 6.3. Tabla `producto_snapshot_ms3`

```text
id
id_producto_ms3
codigo_producto
slug
nombre_producto
descripcion
categoria
marca
estado_registro
estado_publicacion
estado_venta
imagen_principal_url
activo
payload_json
version_evento
event_id
fecha_evento
fecha_sincronizacion
created_at
updated_at
```

## 6.4. Tabla `sku_snapshot_ms3`

```text
id
id_sku_ms3
id_producto_ms3
codigo_sku
barcode
nombre_comercial
atributos_json
activo
vendible
payload_json
version_evento
event_id
fecha_evento
fecha_sincronizacion
created_at
updated_at
```

## 6.5. Tabla `precio_snapshot_ms3`

```text
id
id_precio_ms3
id_sku_ms3
moneda
precio_venta
fecha_inicio
fecha_fin
vigente
motivo
payload_json
version_evento
event_id
fecha_evento
fecha_sincronizacion
created_at
updated_at
```

## 6.6. Tabla `promocion_snapshot_ms3`

```text
id
id_promocion_ms3
codigo_promocion
nombre_promocion
descripcion
tipo_promocion
fecha_inicio
fecha_fin
vigente
activa
prioridad
payload_json
version_evento
event_id
fecha_evento
fecha_sincronizacion
created_at
updated_at
```

## 6.7. Tabla `promocion_sku_descuento_snapshot_ms3`

```text
id
id_promocion_ms3
id_sku_ms3
tipo_descuento
valor_descuento
monto_descuento_estimado
precio_final_estimado
limite_unidades
activo
created_at
updated_at
```

## 6.8. Tabla `stock_snapshot_ms3`

```text
id
id_stock_ms3
id_sku_ms3
id_almacen_ms3
codigo_almacen
nombre_almacen
stock_fisico
stock_reservado
stock_disponible
fecha_ultimo_movimiento
payload_json
version_evento
event_id
fecha_evento
fecha_sincronizacion
created_at
updated_at
```

---

# 7. Comandos de stock MS4 → MS3

MS4 debe publicar comandos de stock mediante Outbox Pattern.  
MS4 no debe publicar directamente con `KafkaTemplate` desde el service de venta.

Tipos de comando:

```text
RESERVAR_STOCK
CONFIRMAR_VENTA
LIBERAR_RESERVA
ANULAR_VENTA
RECONCILIAR_STOCK
```

Payload base obligatorio:

```json
{
  "eventId": "uuid",
  "eventType": "CONFIRMAR_VENTA",
  "sourceService": "ms-ventas-facturacion",
  "occurredAt": "2026-05-21T10:30:00-05:00",
  "idempotencyKey": "MS4-VENTA-000001-SKU-15-CONFIRMAR",
  "referenciaTipo": "VENTA_MS4",
  "referenciaIdExterno": "VEN-2026-000001",
  "venta": {
    "idVentaMs4": 1,
    "codigoVenta": "VEN-2026-000001",
    "canalVenta": "ONLINE"
  },
  "sku": {
    "idSkuMs3": 15,
    "codigoSku": "SKU-CAMISA-BLANCA-M"
  },
  "almacen": {
    "idAlmacenMs3": 1,
    "codigoAlmacen": "ALM-PRINCIPAL"
  },
  "cantidad": 2,
  "requestId": "req-123",
  "correlationId": "checkout-123"
}
```

Regla de idempotencia:

```text
Un evento duplicado jamás debe descontar, reservar, liberar o anular stock dos veces.
```

Formato de `idempotencyKey`:

```text
MS4-VENTA-{idVenta}-SKU-{idSku}-RESERVAR
MS4-VENTA-{idVenta}-SKU-{idSku}-CONFIRMAR
MS4-VENTA-{idVenta}-SKU-{idSku}-LIBERAR
MS4-VENTA-{idVenta}-SKU-{idSku}-ANULAR
```

---

# 8. Integración interna HTTP MS3 ↔ MS4

Kafka es el flujo principal.

HTTP interno se usa solo para reconciliación, soporte técnico o recuperación controlada.

MS4 debe exponer:

```text
GET  /api/internal/ms4/stock-events/pending
POST /api/internal/ms4/stock-events/{id}/mark-synced
POST /api/internal/ms4/stock-events/{id}/mark-error
POST /api/internal/ms4/stock-sync/result
```

Reglas:

```text
Las rutas internas requieren X-Internal-Service-Key.
Las rutas internas no son consumidas por Angular.
Las rutas internas no reemplazan Kafka.
```

---

# 9. Outbox Pattern obligatorio

MS4 debe implementar Outbox Pattern.

Tabla:

```text
evento_dominio_outbox
```

Campos:

```text
id
event_id
aggregate_type
aggregate_id
topic
event_type
payload_json
status
attempts
max_attempts
last_error
locked_by
locked_at
published_at
created_at
updated_at
request_id
correlation_id
```

Estados:

```text
PENDIENTE
PUBLICANDO
PUBLICADO
ERROR
DESCARTADO
```

Flujo obligatorio:

```text
1. Service registra venta, pago y boleta dentro de una transacción.
2. Service registra evento Outbox dentro de la misma transacción.
3. Scheduler Outbox publica a Kafka.
4. Evento se marca como PUBLICADO si Kafka confirma.
5. Evento se marca como ERROR si falla.
6. ADMIN puede reintentar eventos ERROR desde endpoint administrativo.
```

---

# 10. Contingencia cuando MS3 está caído

MS4 debe detectar indisponibilidad de MS3.

Si MS3 está caído, MS4 no debe seguir vendiendo automáticamente.

Flujo obligatorio:

```text
1. MS4 detecta que MS3 no responde o que no puede confirmar sincronización de stock.
2. MS4 marca estado de MS3 como NO_DISPONIBLE.
3. MS4 envía correo inmediato a usuarios ADMIN.
4. MS4 bloquea nuevas ventas que impacten stock.
5. ADMIN activa modo contingencia si autoriza vender con snapshots locales.
6. MS4 permite vender solo si modo contingencia está ACTIVO.
7. MS4 guarda todos los eventos de inventario pendientes.
8. Cuando MS3 vuelve, MS4 reenvía eventos pendientes.
9. MS3 procesa reservas, confirmaciones, liberaciones o anulaciones.
10. MS4 marca eventos como SINCRONIZADO o ERROR.
```

Regla crítica:

```text
Si MS3 cae y ADMIN no activa contingencia, MS4 no permite ventas.
```

## 10.1. Tabla `modo_contingencia`

```text
id
servicio_afectado
estado
fecha_inicio
fecha_fin
activado_por_id_usuario_ms1
activado_por_rol
motivo
ventas_permitidas
guardar_eventos_pendientes
total_eventos_pendientes
observacion
created_at
updated_at
```

Estados:

```text
PENDIENTE_CONFIRMACION
ACTIVO
FINALIZADO
CANCELADO
```

## 10.2. Tabla `inventario_evento_pendiente_ms4`

```text
id
id_venta
codigo_venta
id_detalle_venta
tipo_evento
topic_destino
payload_json
estado
idempotency_key
cantidad_reintentos
ultimo_error
fecha_creacion
fecha_ultimo_reintento
fecha_sincronizacion
request_id
correlation_id
created_at
updated_at
```

Estados:

```text
PENDIENTE
ENVIADO
SINCRONIZADO
ERROR
REQUIERE_REVISION
```

---

# 11. Cloudinary en MS4

Cloudinary debe usarse solo para assets visuales configurables.

Cloudinary debe almacenar:

```text
- logo de empresa
- imágenes de marca del comprobante
- assets visuales de plantilla
```

Cloudinary no debe almacenar:

```text
- PDFs de boletas
- PDFs de comprobantes
- PDFs generados por vista previa
- PDFs generados para correo
- HTML renderizado de boletas
```

Regla obligatoria:

```text
Los comprobantes PDF se generan en vivo y no se persisten en Cloudinary.
```

Tabla permitida:

```text
asset_cloudinary
```

Campos:

```text
id
entidad_origen
id_entidad_origen
tipo_asset
nombre_archivo
extension
content_type
resource_type
folder
public_id
secure_url
url
version
bytes
hash_sha256
estado
subido_por_id_usuario_ms1
request_id
correlation_id
created_at
updated_at
```

Estados:

```text
ACTIVO
REEMPLAZADO
ANULADO
ERROR_SUBIDA
```

Reglas de seguridad:

```text
API Secret de Cloudinary nunca se expone en Angular.
MS4 sube assets mediante backend.
Angular solo recibe URLs controladas de assets públicos.
```

---

# 12. Comprobantes: alcance definitivo

MS4 debe implementar **BOLETA** como comprobante operativo inicial y obligatorio.

En esta RN:

```text
Se implementa BOLETA.
No se implementa FACTURA.
No se implementa NOTA_CREDITO.
No se implementa NOTA_DEBITO.
No se integra SUNAT.
No se firma XML.
No se envía XML a SUNAT.
No se integra OSE.
```

El diseño debe dejar preparado el dominio para futuras extensiones, pero la programación de esta etapa debe enfocarse en boleta.

Regla:

```text
Toda venta confirmada genera una boleta lógica en BD.
La boleta lógica es la fuente de verdad.
La vista HTML y el PDF son representaciones generadas en vivo.
```

---

# 13. Boleta: fuente de verdad

La fuente de verdad de una boleta es la información congelada en SQL Server.

La boleta debe guardar:

```text
- datos del emisor
- datos del cliente
- serie
- número correlativo
- fecha de emisión
- moneda
- detalle de productos
- cantidades
- precios unitarios
- descuentos
- promoción aplicada
- IGV usado
- subtotal
- total
- payload_json
- hash_payload
- version_plantilla
```

La boleta no debe guardar:

```text
- PDF binario
- HTML renderizado permanente
- URL Cloudinary de PDF
- public_id Cloudinary de PDF
```

---

# 14. Tabla `boleta`

```text
id
id_venta
serie
numero
codigo_boleta
fecha_emision
moneda

ruc_emisor
razon_social_emisor
nombre_comercial_emisor
direccion_fiscal_emisor
telefono_emisor
correo_emisor
logo_url_emisor

tipo_documento_cliente
numero_documento_cliente
nombre_cliente
correo_cliente
telefono_cliente
direccion_cliente

subtotal
descuento_total
op_gravada
op_exonerada
op_inafecta
igv_porcentaje
igv_total
total

estado_boleta
payload_json
hash_payload
version_plantilla

enviado_por_correo
fecha_ultimo_envio_correo
cantidad_envios_correo

created_at
updated_at
```

Estados:

```text
EMITIDA
ANULADA
ERROR_ENVIO_CORREO
```

---

# 15. Tabla `boleta_detalle`

```text
id
id_boleta
id_venta_detalle

id_producto_ms3
id_sku_ms3
codigo_producto
codigo_sku
nombre_producto
descripcion_sku

cantidad
precio_unitario_base
precio_unitario_final
subtotal

tipo_descuento
valor_descuento
monto_descuento
id_promocion_ms3
codigo_promocion

igv_porcentaje
igv_monto
total_linea

payload_producto_snapshot_json
payload_precio_snapshot_json
payload_promocion_snapshot_json

created_at
updated_at
```

---

# 16. Generación de boleta en vivo con Thymeleaf

MS4 debe renderizar la boleta en backend usando Thymeleaf.

Regla oficial:

```text
Backend renderiza la boleta HTML con Thymeleaf.
Backend puede generar PDF en memoria cuando sea necesario.
Backend no almacena PDF de boleta.
Frontend consume la vista HTML o el stream PDF.
Frontend gestiona visualización, impresión y descarga desde la respuesta recibida.
```

Servicios obligatorios:

```text
BoletaService
BoletaRenderService
BoletaPdfService
BoletaTemplateModelFactory
```

Templates obligatorios:

```text
src/main/resources/templates/boleta/boleta.html
src/main/resources/templates/mail/boleta-compra.html
```

## 16.1. Vista previa HTML

Endpoint:

```text
GET /api/ms4/cliente/boletas/{idBoleta}/preview
GET /api/ms4/empleado/boletas/{idBoleta}/preview
GET /api/ms4/admin/boletas/{idBoleta}/preview
```

Respuesta:

```http
Content-Type: text/html;charset=UTF-8
```

Flujo:

```text
1. Usuario solicita vista previa.
2. MS4 valida autorización.
3. MS4 obtiene boleta desde BD.
4. MS4 arma modelo de plantilla.
5. MS4 renderiza Thymeleaf.
6. MS4 devuelve HTML.
7. Angular muestra la boleta.
```

## 16.2. PDF generado en vivo

Endpoint:

```text
GET /api/ms4/cliente/boletas/{idBoleta}/pdf
GET /api/ms4/empleado/boletas/{idBoleta}/pdf
GET /api/ms4/admin/boletas/{idBoleta}/pdf
```

Respuesta:

```http
Content-Type: application/pdf
Content-Disposition: inline; filename="B001-00000001.pdf"
```

Flujo:

```text
1. Usuario solicita PDF.
2. MS4 valida autorización.
3. MS4 obtiene boleta desde BD.
4. MS4 renderiza HTML con Thymeleaf.
5. MS4 convierte HTML a PDF en memoria.
6. MS4 devuelve el PDF como stream HTTP.
7. MS4 no guarda el PDF.
8. MS4 no sube el PDF a Cloudinary.
```

Librerías obligatorias:

```text
Thymeleaf
OpenHTMLToPDF
```

OpenPDF puede usarse solo como dependencia complementaria si la implementación lo requiere.

---

# 17. Envío de boleta por correo

MS4 debe enviar la boleta por correo en toda venta confirmada.

Aplica para:

```text
- compra física en tienda
- compra online
```

Regla:

```text
Toda venta confirmada debe programar el envío de boleta al correo del cliente.
```

La boleta enviada por correo no se almacena como PDF.

Flujo obligatorio:

```text
1. Venta se confirma.
2. MS4 genera boleta lógica en BD.
3. MS4 registra solicitud de correo en correo_outbox.
4. Transacción de venta finaliza.
5. Scheduler de correo toma correo_outbox pendiente.
6. MS4 obtiene boleta desde BD.
7. MS4 renderiza HTML con Thymeleaf.
8. MS4 genera PDF en memoria.
9. MS4 renderiza plantilla de correo.
10. MS4 envía correo con PDF adjunto.
11. MS4 marca correo_outbox como ENVIADO.
12. MS4 actualiza enviado_por_correo en boleta.
```

Regla transaccional:

```text
La venta no debe fallar por error SMTP.
Si el correo falla, se registra ERROR en correo_outbox y se reintenta.
```

## 17.1. Servicios de correo obligatorios

```text
EmailService
EmailTemplateRenderer
BoletaMailService
CorreoOutboxService
CorreoOutboxPublisher
```

Regla DRY:

```text
Los services de venta no deben usar JavaMailSender directamente.
El envío real debe pasar por EmailService.
Las plantillas HTML deben vivir en mail/template o templates/mail.
```

## 17.2. Tabla `correo_outbox`

```text
id
event_id
tipo_correo
entidad_origen
id_entidad_origen
destinatario_email
destinatario_nombre
asunto
estado
attempts
max_attempts
last_error
fecha_programada
fecha_envio
request_id
correlation_id
created_at
updated_at
```

Tipos de correo:

```text
BOLETA_COMPRA_FISICA
BOLETA_COMPRA_ONLINE
ALERTA_MS3_CAIDO
ALERTA_MS3_RECUPERADO
ALERTA_CONTINGENCIA_ACTIVADA
ALERTA_CONTINGENCIA_FINALIZADA
ALERTA_KAFKA_ERROR
ALERTA_STRIPE_ERROR
ALERTA_CAJA_DIFERENCIA
```

Estados:

```text
PENDIENTE
ENVIANDO
ENVIADO
ERROR
DESCARTADO
```

## 17.3. Plantilla de correo de boleta

Template:

```text
templates/mail/boleta-compra.html
```

Debe mostrar:

```text
- saludo al cliente
- código de boleta
- fecha de compra
- canal de venta
- total pagado
- método de pago
- mensaje de agradecimiento
```

Debe adjuntar:

```text
B001-00000001.pdf
```

El adjunto se genera en memoria y no se almacena.

---

# 18. Configuración empresarial

Tabla:

```text
configuracion_empresa
```

Campos:

```text
id
ruc
razon_social
nombre_comercial
direccion_fiscal
telefono
correo
web
logo_url
logo_public_id
color_primario
color_secundario
mensaje_pie_boleta
terminos_condiciones
politica_cambios
activo
created_at
updated_at
```

Reglas:

```text
Solo ADMIN puede modificar configuración empresarial.
El logo se almacena en Cloudinary como asset.
La venta congela los datos del emisor en la boleta.
Si la empresa cambia logo o razón social después, las boletas antiguas conservan los datos congelados.
```

---

# 19. Configuración tributaria

Tabla:

```text
configuracion_tributaria
```

Campos:

```text
id
igv_porcentaje
fecha_inicio_vigencia
fecha_fin_vigencia
vigente
modificado_por_id_usuario_ms1
motivo
created_at
updated_at
```

Reglas:

```text
Solo ADMIN puede modificar IGV.
Solo debe existir un IGV vigente.
Toda venta congela el IGV usado.
Las ventas antiguas no se recalculan si cambia el IGV.
```

---

# 20. Series de boleta

Tabla:

```text
serie_comprobante
```

Campos:

```text
id
tipo_comprobante
serie
numero_actual
numero_inicio
numero_fin
activo
created_at
updated_at
```

Reglas:

```text
Solo ADMIN configura series.
En esta RN solo se usa tipo_comprobante = BOLETA.
La numeración debe ser correlativa.
No se puede reutilizar número.
No se puede eliminar físicamente una serie usada.
```

Formato:

```text
B001-00000001
```

---

# 21. Venta física en tienda

La venta física ocurre cuando un EMPLEADO o ADMIN registra una venta presencial.

Reglas:

```text
Requiere JWT válido.
Requiere rol EMPLEADO o ADMIN.
Requiere caja abierta.
Requiere cliente asociado.
No existe venta sin cliente.
Puede pagarse en EFECTIVO.
Puede pagarse con TARJETA_PRESENCIAL_STRIPE_SANDBOX.
Debe registrar empleado vendedor.
Debe congelar precio, promoción e IGV.
Debe generar boleta lógica.
Debe programar envío de boleta por correo.
Debe generar comando de stock hacia MS3 mediante Outbox.
```

## 21.1. Flujo con efectivo

```text
1. EMPLEADO inicia sesión.
2. MS4 valida JWT.
3. MS4 valida empleado snapshot activo.
4. MS4 valida caja abierta.
5. EMPLEADO busca cliente.
6. EMPLEADO agrega productos.
7. MS4 calcula totales.
8. EMPLEADO selecciona EFECTIVO.
9. MS4 registra venta.
10. MS4 registra pago aprobado en efectivo.
11. MS4 genera boleta lógica.
12. MS4 registra correo_outbox para enviar boleta.
13. MS4 registra evento Outbox para stock.
14. MS4 responde venta confirmada.
15. Scheduler de correo envía boleta.
16. Scheduler Kafka publica comando de stock.
```

## 21.2. Flujo con tarjeta presencial Stripe Sandbox

```text
1. EMPLEADO inicia venta física.
2. EMPLEADO asocia cliente.
3. EMPLEADO agrega productos.
4. MS4 calcula totales.
5. EMPLEADO selecciona TARJETA_PRESENCIAL_STRIPE_SANDBOX.
6. MS4 crea PaymentIntent en Stripe Sandbox.
7. Angular muestra Stripe Elements o componente oficial equivalente.
8. Cliente o empleado ingresa tarjeta de prueba de Stripe Sandbox.
9. Stripe Sandbox aprueba o rechaza.
10. Stripe envía webhook a MS4.
11. MS4 valida firma del webhook.
12. Si el pago fue aprobado, MS4 confirma venta.
13. MS4 genera boleta lógica.
14. MS4 registra correo_outbox para enviar boleta.
15. MS4 registra evento Outbox para stock.
16. Scheduler de correo envía boleta.
17. Scheduler Kafka publica comando de stock.
```

Reglas de seguridad:

```text
MS4 no guarda número de tarjeta.
MS4 no guarda CVV.
MS4 no guarda datos sensibles de tarjeta.
Angular usa Stripe.js o Stripe Elements.
MS4 solo guarda IDs, estado y metadata del pago.
```

---

# 22. Venta online

La venta online ocurre cuando CLIENTE autenticado compra desde Angular.

Reglas:

```text
Requiere JWT válido.
Requiere rol CLIENTE.
El cliente se obtiene desde id_usuario_ms1 del JWT.
Solo se permite TARJETA_ONLINE_STRIPE_SANDBOX.
No existe pago online en efectivo.
Debe existir cliente snapshot local.
Debe validar stock snapshot.
Debe confirmar venta solo cuando Stripe confirme pago exitoso.
Debe generar boleta lógica.
Debe programar envío de boleta por correo.
Debe generar comando de stock hacia MS3 mediante Outbox.
```

Flujo:

```text
1. CLIENTE inicia sesión.
2. Angular muestra catálogo usando endpoints MS4 basados en snapshots.
3. CLIENTE agrega productos al carrito.
4. CLIENTE inicia checkout.
5. MS4 valida cliente snapshot.
6. MS4 valida stock snapshot.
7. MS4 calcula precio, promoción, descuento, IGV y total.
8. MS4 crea PaymentIntent en Stripe Sandbox.
9. Angular confirma pago con Stripe.js.
10. Stripe envía webhook a MS4.
11. MS4 valida webhook.
12. Si payment_intent.succeeded, MS4 confirma venta.
13. MS4 genera boleta lógica.
14. MS4 registra correo_outbox para enviar boleta.
15. MS4 registra evento Outbox para stock.
16. Scheduler de correo envía boleta.
17. Scheduler Kafka publica comando de stock.
```

---

# 23. Stripe Sandbox

MS4 debe usar Stripe únicamente en modo Sandbox.

Variables:

```properties
stripe.enabled=true
stripe.mode=sandbox
stripe.secret-key=${STRIPE_SECRET_KEY_TEST}
stripe.publishable-key=${STRIPE_PUBLISHABLE_KEY_TEST}
stripe.webhook-secret=${STRIPE_WEBHOOK_SECRET_TEST}
```

Reglas:

```text
No usar llaves live.
No usar cuenta productiva.
No mover dinero real.
No almacenar PAN, CVV ni datos sensibles.
Backend crea PaymentIntent.
Frontend confirma pago con Stripe.js o Stripe Elements.
Webhook confirma el resultado final.
La venta se confirma solamente desde webhook válido.
```

Tabla:

```text
pago
```

Campos:

```text
id
id_venta
codigo_pago
metodo_pago
estado_pago
moneda
monto
stripe_payment_intent_id
stripe_charge_id
stripe_status
fecha_pago
fecha_confirmacion
payload_pasarela_json
created_at
updated_at
```

Estados:

```text
PENDIENTE
APROBADO
RECHAZADO
ANULADO
REEMBOLSADO
ERROR
```

Métodos:

```text
EFECTIVO
TARJETA_PRESENCIAL_STRIPE_SANDBOX
TARJETA_ONLINE_STRIPE_SANDBOX
```

---

# 24. Carrito y checkout

## 24.1. Tabla `carrito`

```text
id
id_usuario_ms1
id_cliente_snapshot
estado
moneda
subtotal_estimado
descuento_estimado
igv_estimado
total_estimado
fecha_creacion
fecha_actualizacion
fecha_expiracion
created_at
updated_at
```

Estados:

```text
ACTIVO
CHECKOUT
COMPRADO
ABANDONADO
EXPIRADO
```

## 24.2. Tabla `carrito_detalle`

```text
id
id_carrito
id_sku_ms3
codigo_sku
nombre_producto
cantidad
precio_unitario_estimado
descuento_estimado
total_estimado
stock_disponible_snapshot
created_at
updated_at
```

Regla:

```text
El carrito no es una venta.
Los importes definitivos se congelan en venta_detalle y boleta_detalle.
```

---

# 25. Venta

## 25.1. Tabla `venta`

```text
id
codigo_venta
canal_venta
estado_venta
id_cliente_snapshot
id_cliente_ms2
id_usuario_cliente_ms1
id_empleado_snapshot
id_empleado_ms2
id_usuario_empleado_ms1
id_caja
moneda
subtotal
descuento_total
op_gravada
op_exonerada
op_inafecta
igv_porcentaje
igv_total
total
metodo_pago_principal
fecha_venta
request_id
correlation_id
created_at
updated_at
```

Canales:

```text
FISICA
ONLINE
```

Estados:

```text
BORRADOR
PENDIENTE_PAGO
PAGADA
CONFIRMADA
ANULADA
RECHAZADA
ERROR_STOCK
PENDIENTE_SYNC_STOCK
```

## 25.2. Tabla `venta_detalle`

```text
id
id_venta
id_producto_ms3
id_sku_ms3
codigo_producto
codigo_sku
nombre_producto
descripcion_sku
cantidad
precio_unitario_base
precio_unitario_final
subtotal
tipo_descuento
valor_descuento
monto_descuento
id_promocion_ms3
codigo_promocion
igv_porcentaje
igv_monto
total_linea
payload_producto_snapshot_json
payload_precio_snapshot_json
payload_promocion_snapshot_json
created_at
updated_at
```

---

# 26. Caja diaria

Tabla:

```text
caja
```

Campos:

```text
id
codigo_caja
fecha_operacion
estado_caja
monto_inicial
monto_esperado_efectivo
monto_real_efectivo
monto_tarjeta
monto_total_vendido
diferencia
id_empleado_apertura
id_usuario_apertura_ms1
fecha_apertura
id_empleado_cierre
id_usuario_cierre_ms1
fecha_cierre
observacion_apertura
observacion_cierre
created_at
updated_at
```

Estados:

```text
ABIERTA
CERRADA
ANULADA
```

Reglas:

```text
Solo puede existir una caja abierta por día.
Cualquier EMPLEADO activo puede abrir caja.
Si ya existe caja abierta, ningún empleado puede abrir otra.
Toda venta física se asocia a caja abierta.
Cada venta física registra empleado vendedor.
El cierre calcula diferencia entre efectivo esperado y efectivo real.
Pagos con tarjeta Stripe Sandbox no aumentan efectivo real.
```

---

# 27. Reportes

## 27.1. Reporte empleado

EMPLEADO puede consultar su reporte del día.

Debe incluir:

```text
fecha
caja del día
monto inicial
ventas realizadas por el empleado
total efectivo vendido
total tarjeta vendido por Stripe Sandbox presencial
cantidad de transacciones
productos vendidos
ventas anuladas
diferencia de caja si cerró caja
```

Endpoints:

```text
GET /api/ms4/empleado/reportes/caja/hoy
GET /api/ms4/empleado/reportes/ventas?fecha=2026-05-21
```

## 27.2. Reporte ADMIN

ADMIN puede filtrar por:

```text
día
rango de fechas
mes
rango de meses
año
rango de años
canal de venta
empleado
cliente
categoría
producto
SKU
método de pago
```

Debe incluir:

```text
ventas_totales
ventas_netas
ventas_brutas
descuento_total
igv_total
cantidad_ventas
ticket_promedio
productos_vendidos
productos_mas_vendidos
ventas_por_empleado
ventas_online
ventas_fisicas
ventas_por_metodo_pago
ventas_por_categoria
ventas_por_producto
```

Regla sobre ganancia:

```text
MS4 calcula ganancia estimada solo si tiene costo snapshot suficiente.
La ganancia real requiere conciliación con MS3 porque MS3 posee compras, costos, movimientos y kardex.
```

---

# 28. Auditoría funcional

MS4 debe auditar:

```text
VENTA_CREADA
VENTA_CONFIRMADA
VENTA_ANULADA
PAGO_REGISTRADO
PAGO_APROBADO
PAGO_RECHAZADO
BOLETA_EMITIDA
BOLETA_PREVIEW_GENERADA
BOLETA_PDF_GENERADO_EN_VIVO
BOLETA_CORREO_PROGRAMADO
BOLETA_CORREO_ENVIADO
BOLETA_CORREO_ERROR
CAJA_ABIERTA
CAJA_CERRADA
CONTINGENCIA_SOLICITADA
CONTINGENCIA_ACTIVADA
CONTINGENCIA_FINALIZADA
KAFKA_EVENTO_PUBLICADO
KAFKA_EVENTO_ERROR
STRIPE_PAYMENT_INTENT_CREADO
STRIPE_WEBHOOK_RECIBIDO
STRIPE_WEBHOOK_INVALIDO
```

Tabla:

```text
auditoria_funcional
```

Campos:

```text
id
entidad
entidad_id
accion
resultado
actor_id_usuario_ms1
actor_rol
actor_username
ip
user_agent
request_id
correlation_id
detalle_json
created_at
```

---

# 29. Endpoints oficiales

## 29.1. Cliente

```text
GET    /api/ms4/cliente/carrito
POST   /api/ms4/cliente/carrito/items
PUT    /api/ms4/cliente/carrito/items/{idItem}
DELETE /api/ms4/cliente/carrito/items/{idItem}

POST   /api/ms4/cliente/checkout/iniciar
POST   /api/ms4/cliente/pagos/stripe/payment-intent

GET    /api/ms4/cliente/ventas
GET    /api/ms4/cliente/ventas/{idVenta}

GET    /api/ms4/cliente/boletas/{idBoleta}/preview
GET    /api/ms4/cliente/boletas/{idBoleta}/pdf
POST   /api/ms4/cliente/boletas/{idBoleta}/reenviar-correo
```

## 29.2. Empleado

```text
GET  /api/ms4/empleado/clientes
GET  /api/ms4/empleado/productos

POST /api/ms4/empleado/caja/aperturar
POST /api/ms4/empleado/caja/cerrar
GET  /api/ms4/empleado/caja/actual

POST /api/ms4/empleado/ventas
POST /api/ms4/empleado/ventas/{idVenta}/pago-efectivo
POST /api/ms4/empleado/ventas/{idVenta}/pago-tarjeta-stripe

GET  /api/ms4/empleado/ventas
GET  /api/ms4/empleado/ventas/{idVenta}

GET  /api/ms4/empleado/boletas/{idBoleta}/preview
GET  /api/ms4/empleado/boletas/{idBoleta}/pdf
POST /api/ms4/empleado/boletas/{idBoleta}/reenviar-correo

GET  /api/ms4/empleado/reportes/caja/hoy
```

## 29.3. Admin

```text
GET   /api/ms4/admin/ventas
GET   /api/ms4/admin/ventas/{idVenta}

GET   /api/ms4/admin/boletas
GET   /api/ms4/admin/boletas/{idBoleta}
GET   /api/ms4/admin/boletas/{idBoleta}/preview
GET   /api/ms4/admin/boletas/{idBoleta}/pdf
POST  /api/ms4/admin/boletas/{idBoleta}/reenviar-correo

GET   /api/ms4/admin/reportes/ventas
GET   /api/ms4/admin/reportes/financiero

GET   /api/ms4/admin/cajas
GET   /api/ms4/admin/cajas/{idCaja}

PUT   /api/ms4/admin/configuracion/empresa
PUT   /api/ms4/admin/configuracion/tributaria/igv

POST  /api/ms4/admin/series-boleta

GET   /api/ms4/admin/outbox
POST  /api/ms4/admin/outbox/{id}/reintentar

GET   /api/ms4/admin/correos-outbox
POST  /api/ms4/admin/correos-outbox/{id}/reintentar

POST  /api/ms4/admin/contingencia/activar
POST  /api/ms4/admin/contingencia/finalizar
```

## 29.4. Webhook Stripe

```text
POST /api/ms4/webhooks/stripe
```

Reglas:

```text
No usa JWT de usuario.
Debe validar firma Stripe.
Debe rechazar payload inválido.
Debe ser idempotente por event_id de Stripe.
```

## 29.5. Internos

```text
GET  /api/internal/ms4/stock-events/pending
POST /api/internal/ms4/stock-events/{id}/mark-synced
POST /api/internal/ms4/stock-events/{id}/mark-error
POST /api/internal/ms4/stock-sync/result
```

---

# 30. Paquetes oficiales

```text
com.upsjb.ms4
├── Ms4VentasFacturacionApplication.java
├── config
│   ├── AppPropertiesConfig.java
│   ├── ClockConfig.java
│   ├── CorsConfig.java
│   ├── JacksonConfig.java
│   ├── JpaAuditingConfig.java
│   ├── MailConfig.java
│   ├── OpenApiConfig.java
│   ├── SwaggerSecurityConfig.java
│   ├── KafkaTopicProperties.java
│   ├── OutboxProperties.java
│   ├── StripeProperties.java
│   ├── InternalSecurityProperties.java
│   └── CloudinaryProperties.java
├── security
│   ├── config
│   ├── jwt
│   ├── principal
│   ├── roles
│   ├── filter
│   └── handler
├── controller
│   ├── cliente
│   ├── empleado
│   ├── admin
│   ├── internal
│   └── webhook
├── domain
│   ├── entity
│   └── enums
├── dto
│   ├── venta
│   ├── pago
│   ├── boleta
│   ├── caja
│   ├── reporte
│   ├── snapshot
│   ├── kafka
│   ├── mail
│   └── shared
├── repository
├── service
│   ├── contract
│   └── impl
├── mapper
├── validator
├── policy
├── kafka
│   ├── consumer
│   ├── producer
│   ├── event
│   └── outbox
├── mail
│   ├── model
│   ├── template
│   └── sender
├── integration
│   ├── stripe
│   ├── cloudinary
│   ├── ms1
│   └── ms3
├── template
│   ├── boleta
│   └── mail
├── specification
├── shared
└── util
```

---

# 31. Stack oficial

```text
Java 21
Spring Boot
Spring Security OAuth2 Resource Server
Spring Data JPA
SQL Server
Spring Kafka
Outbox Pattern
Stripe Java SDK
Java Mail Sender
Thymeleaf
OpenHTMLToPDF
Cloudinary Java SDK solo para assets visuales
Swagger / OpenAPI
Bean Validation
```

---

# 32. Reglas finales obligatorias

```text
MS4 no duplica MS2.
MS4 no duplica MS3.
MS4 trabaja con snapshots locales.
MS4 congela información comercial usada en cada venta.
MS4 produce comandos de stock hacia MS3.
MS4 nunca modifica stock directamente.
MS4 usa Stripe únicamente Sandbox.
MS4 también usa Stripe Sandbox para tarjeta presencial en tienda.
MS4 genera BOLETA como comprobante obligatorio de esta etapa.
MS4 no guarda PDFs de boletas en Cloudinary.
MS4 no guarda PDFs de boletas en base de datos.
MS4 genera HTML/PDF de boleta en vivo con Thymeleaf.
MS4 envía boleta por correo generando PDF en memoria.
MS4 usa correo_outbox para no perder envíos y no romper ventas por error SMTP.
MS4 usa Cloudinary solo para assets visuales como logo de empresa.
MS4 requiere aprobación ADMIN para contingencia cuando MS3 esté caído.
```

---

# 33. Referencias técnicas usadas

## 33.1. Documentos internos

```text
RN-MS1.txt
RN-MS2.md
RN-MS3-CATALOGO-INVENTARIO.md
RN-MS4-VENTAS-FACTURACION.md previo
codigo_unificado_springboot.txt de MS2
codigo_unificado_springboot.txt de MS3
```

## 33.2. Referencias externas oficiales

```text
Stripe Webhooks:
https://docs.stripe.com/webhooks

Stripe PaymentIntent status con webhooks:
https://docs.stripe.com/payments/payment-intents/verifying-status

Stripe API event types:
https://docs.stripe.com/api/events/types

SUNAT guías y manuales de comprobantes electrónicos:
https://cpe.sunat.gob.pe/guias-y-manuales

SUNAT comprobantes desde sistemas del contribuyente:
https://cpe.sunat.gob.pe/noticias/comprobantes-desde-los-sistemas-del-contribuyente

Thymeleaf envío de correo con Spring:
https://www.thymeleaf.org/doc/articles/springmail.html

Thymeleaf documentación:
https://www.thymeleaf.org/documentation.html
```



