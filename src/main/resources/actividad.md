Actúa como arquitecto senior backend Java Spring Boot 21 especializado en microservicios, arquitectura limpia, diseño de servicios transaccionales, JPA/Hibernate, SQL Server, Spring Security OAuth2 Resource Server, JWT, API Gateway, Kafka Outbox, integración entre microservicios, auditoría funcional, Bean Validation, DTOs, mappers, validators, policies, specifications, manejo profesional de errores, trazabilidad, SOLID, DRY y código listo para producción.

Trabaja exclusivamente sobre:

```text
MS4: ms-ventas-facturacion
Puerto local: 8084
Paquete raíz: com.upsjb.ms4
Dominio: ventas, pagos, Stripe Sandbox, boletas, caja, reportes, auditoría funcional, correo outbox, snapshots MS2/MS3, Kafka Outbox, comandos de stock hacia MS3 y contingencia.
```

Antes de programar, revisa obligatoriamente y con detalle estos 3 archivos compartidos:

```text
1. RN-MS4-VENTAS-FACTURACION-DEFINITIVA.md
2. RN-ESTRUCTURA-CLASES-MS4-DEFINITIVA-ACTUALIZADA.md
3. codigo_unificado_springboot.txt
```

No hagas suposiciones. No inventes clases, métodos, DTOs, entidades, rutas ni reglas si el código real o las RN ya definen una forma concreta de hacerlo. Primero entiende el dominio, luego ubica las clases reales existentes y recién después programa.

La RN es obligatoria como guía funcional y técnica, pero no es un límite ciego. Si detectas que una mejora profesional hace el sistema más seguro, coherente, mantenible, trazable o robusto, aplícala siempre que sea compatible con la finalidad real del MS4.

Si existe contradicción entre `RN-MS4-VENTAS-FACTURACION-DEFINITIVA.md` y `RN-ESTRUCTURA-CLASES-MS4-DEFINITIVA-ACTUALIZADA.md`, prevalece la RN de estructura actualizada.

Reglas obligatorias:

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
```

## Objetivo de esta actividad

Debes programar de forma completa, definitiva y profesional el contrato service y su implementación que se indicarán al final de este prompt.

No te limites a escribir solo esas 2 clases. Revisa toda la cadena real de clases relacionadas que el service necesita para funcionar correctamente:

Si la clase contiene codigo y esta programada entonces debes asegurarte que sea ejecutable, sin errores y aplicar mejoras correspondientes
```text
- DTOs request, response y filter
- mappers
- validators
- policies
- repositories
- specifications
- shared utilities
- resolvers internos
- exceptions
- constants
- enums
- factories de respuesta si aplican
- auditoría funcional
- integración Kafka/Outbox si aplica
- correo outbox si aplica
- servicios auxiliares usados por el caso de uso
```

Si una clase relacionada ya está bien hecha y no requiere cambios, no la regeneres.

Si una clase relacionada tiene errores, incoherencias, duplicidad, nombres incorrectos, responsabilidades mal ubicadas, validaciones incompletas, filtros insuficientes, falta de paginación, mal uso de entidades, exposición indebida de entidades JPA, falta de logging, falta de transaccionalidad o rompe reglas de MS4, corrígela y entrega su código completo actualizado.

## Reglas de arquitectura

```text
Controller:
- No se programa en esta actividad. asi que ignora lo que es controller.

Service:
- Orquesta el caso de uso.
- Usa policies para autorización funcional.
- Usa validators para reglas de negocio.
- Usa repositories para persistencia.
- Usa mappers para convertir entidad/DTO.
- Usa integrations para SDK o servicios externos.
- Usa outbox para Kafka o correo cuando corresponda.
- No expone entidades JPA hacia controllers.
- Trabaja con DTOs de salida.
- Tiene transacciones correctas.

Policy:
- Solo autorización funcional por rol, actor, propiedad del recurso o canal.
- No valida fechas, montos, stock, duplicados ni consistencia de datos.

Validator:
- Solo reglas de negocio y consistencia.
- No autoriza por rol.
- No arma respuestas HTTP.
- No mapea DTOs.

Mapper:
- Solo convierte.
- No consulta repositories.
- No valida negocio.
- No resuelve FKs.

Repository:
- Solo persistencia.
- Si se lista con filtros debe extender JpaSpecificationExecutor.

Specification:
- Solo filtros dinámicos reutilizables.
- No contiene autorización ni efectos colaterales.

Outbox/Processor:
- Procesa pendientes con lock, reintento e idempotencia.
- No crea ventas, pagos ni boletas fuera del service dueño.
```

## Reglas obligatorias para services

Todo método listable debe cumplir:

```text
- Recibir PageRequestDto.
- Recibir FilterDto correspondiente.
- Devolver PageResponseDto<T>.
- Permitir filtros reales útiles para UX.
- Incluir filtros por estado cuando aplique.
- Incluir filtros por fecha desde/hasta cuando aplique.
- Incluir búsqueda por texto/código/nombre/documento/correo cuando aplique.
- No devolver listas sin paginar.
```

Todo método de obtención debe cumplir:

```text
- Devolver response DTO.
- No devolver entidad JPA al controller.
- Validar existencia.
- Validar estado activo si corresponde.
- Aplicar policy si el acceso depende del actor.
```

Todo método de mutación debe cumplir:

```text
- Usar @Transactional.
- Validar reglas de negocio mediante validator.
- Aplicar autorización mediante policy.
- Persistir de forma consistente.
- Registrar auditoría funcional si la acción es crítica.
- Registrar outbox si el flujo lo requiere.
- Devolver un DTO claro para frontend.
- Permitir mensaje funcional de éxito.
- Lanzar errores funcionales claros cuando el problema sea del usuario.
- Registrar errores técnicos con logger estructurado.
```

Todo método de consulta debe cumplir:

```text
- Usar @Transactional(readOnly = true).
- No modificar estado.
- No tener efectos colaterales.
```

## Experiencia de usuario y FK

No diseñes flujos donde el usuario tenga que ingresar IDs numéricos a ciegas.

Si una operación necesita relacionarse con un FK, revisa si existe lookup o dato reconocible. Cuando sea razonable, el request debe permitir datos entendibles para el usuario, por ejemplo:

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

Internamente puedes resolver el FK, pero el usuario no debe depender de conocer IDs técnicos sin contexto. Si el código actual solo permite IDs y eso degrada la UX, propón y programa una mejora coherente usando resolvers, lookups o validadores.

No rompas compatibilidad si el DTO existente ya usa ID y está extendido por muchas clases; en ese caso puedes aceptar ambos criterios, siempre validando ambigüedad y consistencia.

## Mensajes, errores y trazabilidad

Toda acción funcional debe producir mensajes claros.

Para errores de usuario:

```text
- Usa excepciones funcionales existentes como ValidationException, NotFoundException, ConflictException, ForbiddenException, UnauthorizedException o BusinessException según corresponda.
- El mensaje debe ser entendible para frontend.
- No devuelvas stacktrace al usuario.
```

Para errores técnicos:

```text
- Registra el detalle con SLF4J Logger.
- Incluye contexto útil: caso de uso, id o código de entidad, actor, requestId, correlationId, eventId o idempotencyKey cuando aplique.
- Devuelve al usuario un mensaje seguro y simple.
- No ocultes el error en logs.
```

No uses `System.out.println`. Usa logger profesional:

```java
private static final Logger log = LoggerFactory.getLogger(NombreClase.class);
```

## DRY y calidad

Evita:

```text
- duplicar validaciones ya existentes
- duplicar cálculos
- repetir armado de PageResponseDto si ya existe un helper
- repetir filtros si ya existe SpecificationBuilder
- meter reglas de negocio en mappers
- meter autorización en validators
- meter persistencia en mappers
- crear clases basura
- crear métodos que no serán usados
- crear endpoints o controllers innecesarios
```

Usa las utilidades shared existentes cuando estén disponibles. Antes de crear una utilidad nueva, revisa si ya existe algo equivalente en el código unificado.

## Seguridad MS4

Mantén coherencia con:

```text
Angular -> API Gateway -> MS4
MS4 valida JWT emitido por MS1
MS4 no autentica usuarios
MS4 no emite tokens
MS4 no administra usuarios ni roles
Rutas internas se protegen con X-Internal-Service-Key
```

Si el service depende del actor autenticado, debe recibir o resolver un contexto de actor seguro mediante las clases existentes del proyecto. No inventes una forma paralela de autenticación.

## Kafka, Outbox e idempotencia

Si el caso de uso toca Kafka, snapshots, inventario, correo o sincronización, respeta:

```text
- No publicar directo con KafkaTemplate desde services de negocio.
- Registrar EventoDominioOutbox dentro de la misma transacción del caso dueño.
- Procesar eventos Kafka de forma idempotente por eventId.
- Usar idempotencyKey para comandos de stock.
- No descontar, reservar, liberar ni anular stock dos veces.
- No depender de llamadas HTTP a MS3 para cada venta.
- MS4 trabaja con snapshots locales.
```

## Boleta y PDF

Si el service está relacionado con boletas:

```text
- La boleta lógica en BD es la fuente de verdad.
- El HTML se renderiza en vivo con Thymeleaf.
- El PDF se genera en memoria con OpenHTMLToPDF.
- No guardar PDF en BD.
- No subir PDF a Cloudinary.
- No guardar HTML renderizado permanente.
- El correo debe adjuntar PDF generado en memoria.
```

## Stripe

Si el service está relacionado con Stripe:

```text
- Solo modo Sandbox/Test.
- No usar llaves live.
- No guardar número de tarjeta, CVV ni PAN.
- Backend crea PaymentIntent.
- Webhook válido confirma el resultado final.
- Registrar StripeEvento de forma idempotente.
```

## Auditoría funcional

En acciones críticas, registra auditoría funcional si el proyecto ya tiene infraestructura para eso.

Acciones críticas:

```text
- crear venta
- confirmar venta
- anular venta
- registrar pago
- abrir/cerrar caja
- modificar configuración empresarial
- modificar IGV
- gestionar series
- reintentar outbox
- descartar outbox
- activar/finalizar contingencia
- procesar webhook Stripe
```

## Formato obligatorio de respuesta

Tu respuesta debe seguir esta estructura:

```text
1. Resumen de entendimiento
2. Archivos revisados
3. Diagnóstico técnico
   - Clases correctas que no requieren cambios
   - Clases con problemas o incompletas
4. Decisiones aplicadas
5. Código completo actualizado
6. Notas de integración
7. Checklist final
```

En “Código completo actualizado” entrega cada clase modificada o creada en bloques separados.

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
- Sin clases incompletas.
- Sin placeholders.
- Sin inventar paquetes que no existen salvo que sea estrictamente necesario y coherente.
```

Si una clase relacionada está bien y no requiere cambios, menciónala en diagnóstico pero no repitas su código.

Si necesitas crear una clase nueva, justifica por qué es necesaria y asegúrate de que no exista ya una equivalente.

## Actividad concreta

Al final de este prompt se indicará el contrato service y la implementación service que debes programar.

Debes revisar primero el contrato y la implementación actual en `codigo_unificado_springboot.txt`, luego revisar todas sus dependencias reales y finalmente entregar el código definitivo.

No programes a ciegas.

No te limites a copiar la RN.

No ignores clases relacionadas.

El objetivo es dejar este service cerrado, profesional, seguro, mantenible y coherente con todo MS4.

## Service contract e implementation a programar

