# Prompt para programar Controller en MS4 — `ms-ventas-facturacion`

Actúa como arquitecto senior backend Java Spring Boot 21 especializado en microservicios, arquitectura limpia, diseño de controladores REST productivos, Spring Security OAuth2 Resource Server, JWT, Bean Validation, DTOs, manejo profesional de respuestas HTTP, trazabilidad, OpenAPI/Swagger, separación estricta de capas, SOLID, DRY y código listo para producción.

Trabaja exclusivamente sobre:

```text
MS4: ms-ventas-facturacion
Puerto local: 8084
Paquete raíz: com.upsjb.ms4
Dominio: ventas, pagos, Stripe Sandbox, boletas, caja, reportes, auditoría funcional, correo outbox, snapshots MS2/MS3, Kafka Outbox, comandos de stock hacia MS3 y contingencia.
```

Antes de programar, revisa obligatoriamente y con detalle estos archivos compartidos:

```text
1. RN-MS4-VENTAS-FACTURACION-DEFINITIVA.md
2. RN-ESTRUCTURA-CLASES-MS4-DEFINITIVA-ACTUALIZADA.md
3. codigo_unificado_springboot.txt
```

No programes a ciegas. No inventes rutas, métodos, DTOs, factories, policies, resolvers ni respuestas si el código real o las RN ya definen una forma concreta de hacerlo. Primero entiende el dominio, luego ubica las clases reales existentes y recién después programa.

Si existe contradicción entre `RN-MS4-VENTAS-FACTURACION-DEFINITIVA.md` y `RN-ESTRUCTURA-CLASES-MS4-DEFINITIVA-ACTUALIZADA.md`, prevalece la RN de estructura actualizada.

La RN es obligatoria como guía funcional y técnica, pero no es un límite ciego. Si detectas una mejora profesional que haga el controlador más seguro, coherente, mantenible, trazable o robusto, aplícala siempre que sea compatible con la finalidad real del MS4 y no duplique responsabilidades de otras capas.

---

## Reglas funcionales obligatorias del MS4

```text
MS4 trabaja con venta directa.
No debes crear ni usar carrito ni checkout.
No debes implementar factura, nota de crédito, nota de débito, SUNAT, OSE ni firma XML.
MS4 implementa boleta.
Los PDF de boleta se generan en vivo.
Los PDF no se guardan en BD ni Cloudinary.
Cloudinary solo se usa para assets visuales.
Stripe solo se usa en modo Sandbox.
MS4 consume snapshots de MS2/MS3.
MS4 produce comandos de stock hacia MS3 mediante Outbox.
MS4 no modifica stock oficial directamente.
El controlador debe usar correctamente lo que es Swagger es obligatorio
```

---

# Objetivo de esta actividad

Debes programar de forma completa, definitiva y profesional el controlador que se indicará al final de este prompt.

No te limites a escribir solo la clase controller. Revisa toda la cadena real de clases relacionadas que el controller necesita para funcionar correctamente, sin duplicar responsabilidades ni respuestas que ya existen:

```text
- service contract correspondiente
- service implementation correspondiente
- DTOs request, response, filter y shared
- PageRequestDto y PageResponseDto
- ApiResponseDto y ApiResponseFactory o factory equivalente
- AuthenticatedUserResolver y AuthenticatedUserContext
- policies usadas directa o indirectamente
- validators usados por service
- GlobalExceptionHandler o manejadores de errores existentes
- SecurityConfig y reglas de rutas si aplica
- constantes de rutas o mensajes si existen
- enums usados por filtros, estados, canales o métodos de pago
- configuración OpenAPI/Swagger si ya existe
- utilidades para ResponseEntity, PDF, HTML, headers o archivos si existen
- services auxiliares invocados por el service dueño
```

Si una clase relacionada ya está bien hecha y no requiere cambios, no la regeneres.

Si una clase relacionada tiene errores que impiden que el controller compile o funcione correctamente, corrígela y entrega su código completo actualizado. Esto aplica especialmente a DTOs mal anotados, factories incompletas, métodos service inexistentes, nombres incoherentes, imports incorrectos, falta de `@Valid`, falta de `@Validated`, rutas duplicadas, respuestas inconsistentes o firma incompatible con el service real.

---

# Responsabilidad exacta del Controller

El controller solo puede encargarse de:

```text
- Exponer rutas HTTP.
- Recibir parámetros, path variables, headers, request body, query params y multipart si aplica.
- Aplicar Bean Validation superficial con @Valid, @Validated, @RequestBody, @ModelAttribute o el patrón real del proyecto.
- Resolver el actor autenticado usando exclusivamente las clases existentes del proyecto.
- Delegar al service correspondiente.
- Construir la respuesta HTTP usando ApiResponseFactory o el mecanismo real ya existente.
- Devolver HTML o PDF con ResponseEntity cuando corresponda.
- Definir códigos HTTP coherentes: 200, 201, 202, 204, 400, 401, 403, 404, 409, 422, 500 según el manejador global.
- Documentar el endpoint con OpenAPI/Swagger si el proyecto ya usa esa convención.
```

El controller no debe:

```text
- Usar repositories.
- Usar mappers de entidad directamente salvo que el patrón existente lo justifique para responses técnicas simples.
- Usar validators de negocio directamente si el service ya valida.
- Calcular totales, IGV, descuentos, stock, promociones o estados.
- Consultar SDK Stripe, Cloudinary, KafkaTemplate, JavaMailSender ni clientes HTTP externos.
- Registrar Outbox, correo, auditoría, pagos, boletas o stock directamente.
- Manejar transacciones.
- Capturar excepciones funcionales que ya resuelve el GlobalExceptionHandler.
- Devolver entidades JPA.
- Crear respuestas manuales si existe ApiResponseFactory o helper equivalente.
- Duplicar mensajes, constantes o formatos de respuesta ya definidos.
- Exponer métodos internos del service solo porque son públicos.
```

---

# Criterios para decidir qué métodos service exponer como endpoint

Debes revisar todos los métodos públicos del service contract y su implementation real.

Expón un método como endpoint solo si cumple al menos uno de estos criterios:

```text
- Representa una acción iniciada por Angular a través del API Gateway.
- Representa una acción administrativa explícita requerida por RN.
- Representa una ruta interna documentada para integración controlada entre microservicios.
- Representa webhook externo documentado, como Stripe.
- Representa consulta, descarga, preview, generación en vivo o reintento funcional que el usuario o sistema externo necesita invocar.
```

No expongas como endpoint los métodos que sean:

```text
- resolvers internos, especialmente métodos resolver...
- métodos de sincronización Kafka consumidos por listeners
- métodos procesar...Kafka invocados por consumers
- métodos registrar...Outbox usados dentro de una transacción de negocio
- métodos enviar...DesdeOutbox usados por scheduler/processors
- métodos de cálculo internos usados por otro service
- métodos auxiliares de factories, mappers, validators o policies
- métodos que devuelven entidades JPA
- métodos que generan efectos colaterales peligrosos sin caso de uso HTTP claro
- métodos duplicados por otro controller oficial
- métodos de compatibilidad interna no usados por frontend ni integración documentada
```

Si decides no exponer un método, debes explicarlo en la respuesta final con una razón técnica clara.

---

# Reglas para endpoints listables

Todo endpoint listable debe cumplir:

```text
- Recibir PageRequestDto.
- Recibir FilterDto correspondiente.
- Devolver ApiResponseDto<PageResponseDto<T>> o el wrapper real usado por el proyecto.
- Permitir filtros reales útiles para UX.
- Incluir filtros por estado cuando aplique.
- Incluir filtros por fecha desde/hasta cuando aplique.
- Incluir búsqueda por texto/código/nombre/documento/correo cuando aplique.
- No devolver listas sin paginar.
- No forzar IDs técnicos si existe lookup o dato reconocible.
```

Usa el patrón real del código para bindear filtros y paginación. Si el proyecto usa `@ModelAttribute`, respétalo. Si usa query params explícitos o un binder compartido, respétalo. No inventes un patrón paralelo.

---

# Reglas para endpoints de obtención

Todo endpoint de obtención debe cumplir:

```text
- Devolver response DTO, nunca entidad JPA.
- Validar existencia mediante el service.
- Validar acceso mediante policy dentro del service o patrón existente.
- Responder con mensaje funcional claro.
- No resolver FKs ni reglas de negocio dentro del controller.
```

---

# Reglas para endpoints de mutación

Todo endpoint de mutación debe cumplir:

```text
- Recibir @Valid @RequestBody cuando haya payload JSON.
- Recibir MultipartFile solo cuando el caso de uso lo requiere.
- Resolver actor autenticado antes de invocar el service si el service lo requiere.
- Delegar toda autorización funcional, validación profunda, persistencia, auditoría y outbox al service.
- Devolver DTO claro para frontend.
- Usar estado HTTP coherente: 201 para creación, 200 para operación ejecutada, 202 para solicitudes aceptadas/asíncronas si el proyecto lo maneja así.
- No usar @Transactional en controller.
```

---

# Reglas para HTML y PDF de boleta

Si el controller está relacionado con boletas:

```text
- El preview HTML debe devolver text/html;charset=UTF-8.
- El PDF debe devolver application/pdf.
- El Content-Disposition debe ser inline con filename coherente.
- El controller no debe renderizar Thymeleaf directamente.
- El controller no debe convertir HTML a PDF directamente.
- El controller no debe guardar PDF.
- El controller no debe subir PDF a Cloudinary.
- El controller debe delegar a BoletaRenderService o BoletaPdfService según el contrato real.
```

Si `BoletaPdfService` ya tiene un método como `construirRespuestaPdfInline`, úsalo y no dupliques headers en el controller. Si no existe pero la RN lo exige y es necesario, corrige el service correspondiente, justificándolo.

---

# Reglas para Stripe y webhooks

Si el controller está relacionado con Stripe:

```text
- No usar llaves live.
- No manejar datos sensibles de tarjeta.
- No recibir PAN, CVV ni número de tarjeta.
- El backend solo crea PaymentIntent o procesa webhook.
- El webhook debe recibir raw payload y header Stripe-Signature.
- La confirmación final de venta debe depender del webhook válido.
- No validar firma Stripe en controller si existe StripeWebhookService.
- No consultar Stripe SDK directamente desde controller.
```

---

# Reglas para rutas internas

Si el controller es interno:

```text
- Usar prefijo /api/internal/ms4 cuando corresponda.
- Exigir X-Internal-Service-Key mediante filtro, interceptor, security config o validador existente.
- No exponer rutas internas para Angular.
- No mezclar rutas internas con rutas públicas o protegidas normales.
- No reemplazar Kafka con HTTP interno.
```

---

# Seguridad MS4

Mantén coherencia con:

```text
Angular -> API Gateway -> MS4
MS4 valida JWT emitido por MS1
MS4 no autentica usuarios
MS4 no emite tokens
MS4 no administra usuarios ni roles
Rutas internas se protegen con X-Internal-Service-Key
```

Si el controller depende del actor autenticado, debe usar o recibir un contexto de actor seguro mediante las clases existentes del proyecto. No inventes una forma paralela de autenticación.

No agregues `@PreAuthorize`, `@Secured` o reglas nuevas si el proyecto usa policies en service. Si el proyecto ya usa anotaciones de seguridad en controllers, mantén la convención existente sin duplicar autorización funcional.

---

# Respuestas, errores y trazabilidad

Toda acción funcional debe producir mensajes claros.

Para respuestas exitosas:

```text
- Usa ApiResponseFactory, ResponseFactory, ApiResponseDto o el helper real existente.
- No construyas manualmente el JSON si ya existe una factory.
- No dupliques mensajes si existen constantes.
- Mantén estructura estable para frontend.
```

Para errores funcionales:

```text
- No captures ValidationException, NotFoundException, ConflictException, ForbiddenException, UnauthorizedException o BusinessException si ya las maneja GlobalExceptionHandler.
- No devuelvas stacktrace al usuario.
- No conviertas errores funcionales en 500.
```

Para errores técnicos:

```text
- No ocultes errores con try/catch vacío.
- Si el controller necesita log técnico por manejo especial de streaming, usa SLF4J.
- Incluye contexto útil: endpoint, actor, id o código de entidad, requestId, correlationId o eventId cuando aplique.
- No uses System.out.println.
```

Logger profesional si es necesario:

```java
private static final Logger log = LoggerFactory.getLogger(NombreController.class);
```

---

# DRY y coherencia con clases existentes

Evita:

```text
- Crear un nuevo ApiResponseFactory si ya existe uno.
- Crear nuevos DTOs si los existentes cubren el caso.
- Crear rutas duplicadas con otro controller.
- Agregar endpoints de carrito o checkout.
- Agregar endpoints de factura, notas, SUNAT, OSE o firma XML.
- Duplicar validaciones profundas del service en controller.
- Duplicar armado de PageResponseDto.
- Repetir headers PDF si un service/helper ya los arma.
- Usar strings mágicos si existen constantes o enums.
- Meter lógica de autorización funcional en controller.
- Meter reglas de negocio en controller.
- Crear clases basura.
- Crear endpoints que no serán usados.
```

Antes de crear una clase nueva, verifica si ya existe algo equivalente en `codigo_unificado_springboot.txt`.

---

# Compatibilidad con UX y FK

No diseñes endpoints donde el usuario tenga que ingresar IDs numéricos a ciegas si existe alternativa UX.

Si una operación necesita FK, revisa si existe lookup o dato reconocible:

```text
- código de venta
- código de boleta
- serie
- código SKU
- código producto
- código almacén
- documento de cliente
- RUC
- correo
- username
- código empleado
```

El controller no debe resolver manualmente esos FKs. Debe recibir el DTO correcto y delegar al service/resolver interno existente.

No rompas compatibilidad si el DTO existente usa ID y está extendido por muchas clases. En ese caso puedes aceptar ambos criterios solo si el DTO/service real lo permite y validando ambigüedad dentro del service/validator, no dentro del controller.

---

# OpenAPI / Swagger

Si el proyecto ya usa Swagger/OpenAPI:

```text
- Agrega @Tag al controller.
- Agrega @Operation a cada endpoint.
- Agrega @ApiResponses si la convención existente lo usa.
- No sobre-documentes con información falsa.
- No declares códigos que el GlobalExceptionHandler no maneja.
```

Si el proyecto no usa OpenAPI todavía, no lo introduzcas solo para este controller salvo que la RN o el código unificado ya lo exijan.

---

# Formato obligatorio de respuesta

Tu respuesta debe seguir esta estructura exacta:

```text
1. Resumen de entendimiento
2. Archivos revisados
3. Diagnóstico técnico
   - Clases correctas que no requieren cambios
   - Clases con problemas o incompletas
4. Mapa de endpoints implementados
   - Método HTTP
   - Ruta
   - Método controller
   - Método service consumido
   - DTO request/filter usado
   - DTO response usado
   - Motivo funcional
5. Métodos service sin endpoint
   - Método service
   - Motivo por el que no se expone
6. Decisiones aplicadas
7. Código completo actualizado
8. Notas de integración
9. Checklist final
```

En `Mapa de endpoints implementados`, debes indicar explícitamente cada endpoint añadido o corregido.

En `Métodos service sin endpoint`, debes listar los métodos del service contract revisado que decidiste no exponer, por ejemplo:

```text
- resolverVentaParaProcesoInterno(Long idVenta): no se expone porque devuelve entidad JPA y es resolver interno.
- confirmarVentaOnlinePagadaStripe(String stripePaymentIntentId): no se expone como endpoint público porque lo invoca StripeWebhookService desde el webhook válido.
- registrarEventoOutbox(...): no se expone porque debe ejecutarse dentro de la transacción del caso de uso dueño.
```

Si todos los métodos públicos del service se exponen, indícalo expresamente y justifica por qué.

---

# Formato obligatorio del código

En `Código completo actualizado`, entrega cada clase modificada o creada en bloques separados.

Cada bloque debe iniciar con la ruta exacta:

```java
// ruta: src/main/java/com/upsjb/ms4/...
```

Reglas para el código:

```text
- Código completo, no fragmentos.
- Código compilable.
- Imports completos.
- Sin pseudocódigo.
- Sin comentarios innecesarios.
- Sin TODOs.
- Sin placeholders.
- Sin clases incompletas.
- Sin inventar paquetes que no existen salvo que sea estrictamente necesario y coherente.
- Constructor injection obligatorio.
- @RestController en controllers REST.
- @RequestMapping con prefijo correcto.
- @Validated cuando corresponda.
- @Valid en request bodies.
- ResponseEntity solo cuando sea necesario para status, headers, HTML, PDF o multipart.
- No devolver entidades JPA.
```

Si una clase relacionada está bien y no requiere cambios, menciónala en diagnóstico pero no repitas su código.

Si necesitas crear una clase nueva, justifica por qué es necesaria y asegúrate de que no exista ya una equivalente.

---

# Checklist final obligatorio

Al final responde con checklist verificable:

```text
- [ ] Revisé RN-MS4-VENTAS-FACTURACION-DEFINITIVA.md.
- [ ] Revisé RN-ESTRUCTURA-CLASES-MS4-DEFINITIVA-ACTUALIZADA.md.
- [ ] Revisé codigo_unificado_springboot.txt.
- [ ] El controller no usa repositories.
- [ ] El controller no usa SDK Stripe, Cloudinary, KafkaTemplate ni JavaMailSender.
- [ ] El controller no calcula reglas de negocio.
- [ ] El controller no duplica validaciones profundas del service.
- [ ] El controller usa el resolver real de actor autenticado.
- [ ] El controller usa ApiResponseFactory o helper real existente.
- [ ] Los endpoints listables son paginados y filtrables.
- [ ] Los endpoints HTML devuelven text/html;charset=UTF-8 cuando aplica.
- [ ] Los endpoints PDF devuelven application/pdf y no persisten PDF.
- [ ] No se crearon endpoints de carrito ni checkout.
- [ ] No se crearon endpoints de factura, notas, SUNAT, OSE ni XML.
- [ ] Se listaron los endpoints implementados.
- [ ] Se listaron los métodos service no expuestos y su motivo.
- [ ] El código entregado es completo y compilable.
```

---

# Actividad concreta

Al final de este prompt se indicará el controller que debes programar y los services relacionados.

Debes revisar primero el controller actual, si existe, en `codigo_unificado_springboot.txt`. Luego revisa el service contract, la implementation actual y todas sus dependencias reales. Finalmente entrega el código definitivo.

No programes a ciegas.
No te limites a copiar la RN.
No ignores clases relacionadas.
No expongas métodos internos como endpoints.
No dupliques respuestas, validaciones ni responsabilidades ya existentes.

El objetivo es dejar este controller cerrado, profesional, seguro, mantenible y coherente con todo MS4.

## Controller y services relacionados a programar

```text
Controller objetivo:

Services relacionados:

Notas específicas:
```
