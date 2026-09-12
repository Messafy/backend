# Especificación de Requisitos de Software (SRS): Ghost Notes

## Control del documento

| Campo | Valor |
|---|---|
| Producto | Ghost Notes |
| Documento | Software Requirements Specification (SRS) |
| Versión | 1.1 |
| Estado | Línea base de trabajo |
| Fecha | 2026-09-08 |
| Clasificación | Uso interno |
| Documento de origen | `Ghost_Notes_Requerimientos_v1.0.pdf` |
| Norma de referencia | ISO/IEC/IEEE 29148:2018 |

> Este documento es un artefacto interno de ingeniería. Está adaptado a la estructura y a los principios de calidad de requisitos de ISO/IEC/IEEE 29148:2018, pero no declara conformidad ni certificación formal. Tampoco sustituye asesoramiento legal sobre RGPD, LOPDGDD, menores, contenido generado por usuarios o respuesta ante abuso.

## Historial de cambios

| Versión | Fecha | Cambios |
|---|---|---|
| 1.0 | 2026-06-26 | Documento de requisitos original en PDF. |
| 1.1 | 2026-09-08 | Conversión a SRS versionable en Markdown; actualización tecnológica; separación por iteraciones; incorporación del estado real de implementación, criterios verificables y trazabilidad. |

## Convenciones

Las palabras **debe**, **no debe** y **deberá** expresan requisitos obligatorios. **Puede** expresa una capacidad opcional. Las recomendaciones que aún no constituyen requisitos se identifican explícitamente como tales.

### Prioridad

| Prioridad | Significado |
|---|---|
| Alta | Bloquea la salida de la fase objetivo o protege una propiedad esencial del producto. |
| Media | Necesaria para una experiencia completa, pero admite entrega posterior controlada. |
| Baja | Mejora prevista que no bloquea el objetivo de validación actual. |

### Estado de implementación

| Estado | Significado |
|---|---|
| Implementado | Existe una implementación verificable que satisface sustancialmente el requisito. |
| Parcial | Existe una parte reconocible, pero faltan condiciones obligatorias o verificaciones. |
| Pendiente | No existe todavía un flujo utilizable que satisfaga el requisito. |

### Procedencia de los requisitos

- Los requisitos `RF-001..010`, `RF-020..026`, `RF-030..037`, `RF-040..047`, `RF-050..059` y `RF-060..068` conservan los identificadores y la intención del PDF v1.0. Su redacción se ha normalizado para hacerlos verificables y reflejar el alcance incremental.
- Los requisitos `RNF-001..007`, `RNF-010..014`, `RNF-020..023`, `RNF-030..033`, `RNF-040..043` y `RNF-050..052` proceden del PDF v1.0 y han sido refinados con métricas o condiciones de aceptación.
- Los requisitos `RNF-008`, `RNF-009`, `RNF-015`, `RNF-024`, `RNF-034`, `RNF-044`, `RNF-060..066` y `RNF-070..073` se incorporan en esta versión a partir de la auditoría de implementación, el análisis de riesgos y las decisiones arquitectónicas.
- La columna **Estado actual** es una evaluación de la implementación observada el 2026-09-08; no modifica la obligatoriedad del requisito.

# 1. Introducción

## 1.1 Propósito

Este SRS define qué debe hacer Ghost Notes, bajo qué restricciones debe operar y cómo se verificará su cumplimiento. Sirve como línea base compartida para producto, desarrollo, pruebas, seguridad, operaciones y revisión legal.

El documento distingue deliberadamente entre:

- El **estado actual**, que es una base técnica de cuentas y notas.
- El **MVP de anonimato controlado**, que requiere códigos de recepción, bandeja anónima y controles antiabuso.
- Las **capacidades posteriores**, como conversaciones consentidas y comunidad.

## 1.2 Alcance del producto

Ghost Notes será una aplicación web para comunicaciones asíncronas bajo anonimato social controlado. Una persona registrada podrá recibir notas mediante un código o enlace opaco. El receptor no conocerá la identidad del remitente, aunque el sistema conservará trazabilidad interna mínima y protegida para prevenir abuso y responder a obligaciones legítimas.

La propuesta de valor se apoya en cuatro principios:

1. Anonimato frente a otros usuarios, no impunidad frente al sistema.
2. Control efectivo del receptor mediante denuncia, bloqueo y pausa.
3. Prevención de abuso desde el diseño.
4. Experiencia web limpia, sin anuncios invasivos y sin funciones de seguridad de pago.

## 1.3 Audiencia prevista

- Responsable de producto y diseño.
- Desarrollo backend y frontend.
- QA y seguridad.
- Operaciones y moderación.
- Asesoría legal previa al lanzamiento público.

## 1.4 Fuera del alcance de este documento

Este SRS no prescribe el diseño detallado de clases, endpoints o colecciones salvo cuando sea necesario para hacer verificable un requisito. Las decisiones técnicas se justifican en los ADRs y la arquitectura se representa mediante C4 y Context Map DDD.

# 2. Descripción general

## 2.1 Perspectiva del producto

Ghost Notes es un sistema web compuesto actualmente por:

- Una SPA construida con React 19, Vite y React Router.
- Una API REST construida con Java 21, Spring Boot 4.1.1 y Spring Modulith.
- MongoDB 7 como persistencia.
- Autenticación stateless mediante JWT firmado con HS256.

La solución se desarrolla como monolito modular organizado por bounded contexts y vertical slices. Actualmente existen `account`, `notes` y el módulo compartido `share`. Los contextos `receiving` y `safety` se incorporarán durante la finalización del MVP anónimo.

## 2.2 Objetivos del producto

- Permitir registro, autenticación y gestión segura de sesión.
- Proporcionar un código de recepción único y opaco por usuario.
- Permitir enviar una nota anónima autenticada a un código válido.
- Permitir al receptor leer y gestionar notas sin conocer al remitente.
- Proporcionar denuncia, bloqueo, pausa y regeneración del código.
- Aplicar rate limiting, señales de riesgo y challenge anti-bot.
- Mantener separación estricta entre datos públicos, datos del receptor y trazabilidad interna.

## 2.3 Métricas de éxito del MVP

| Métrica | Umbral |
|---|---|
| Flujo registro → código → envío → recepción | Se completa en menos de 3 minutos por un usuario nuevo. |
| Privacidad de API | Ningún DTO de receptor expone identidad o metadatos del remitente. |
| Protección básica | Rate limiting bloquea abuso evidente sin afectar uso normal definido en pruebas. |
| Acceso a safety | Denuncia, bloqueo y pausa disponibles en un máximo de dos interacciones desde una nota. |
| Modularidad | La verificación de Spring Modulith no detecta dependencias ilegales. |
| Calidad percibida | La beta cerrada considera la experiencia clara, privada y segura. |

# 3. Partes interesadas y actores

| Actor | Descripción y necesidades |
|---|---|
| Visitante | Persona sin sesión que necesita registrarse o iniciar sesión. |
| Usuario registrado | Persona con identidad interna. Puede actuar como remitente, receptor o ambos. |
| Remitente | Usuario autenticado que envía una nota. Su identidad no es visible para el receptor. |
| Receptor | Usuario que controla un código de recepción, su bandeja y sus medidas de protección. |
| Administrador/moderador | Operador interno autorizado para revisar denuncias y aplicar restricciones. El panel completo no pertenece al MVP inicial. |
| Bot o atacante | Actor no legítimo que intenta spam, enumeración de códigos, multicuentas o acoso. |
| Menor de edad | Subgrupo sujeto a política de edad mínima y protección especial antes del lanzamiento. |

# 4. Alcance incremental

## 4.1 Incremento 1: MVP anónimo seguro

El Incremento 1 se divide en iteraciones internas para evitar confundir la base técnica actual con el MVP completo.

| Iteración | Alcance | Resultado verificable |
|---|---|---|
| 1A - Base técnica | Cuenta, registro backend, login JWT, autorización básica, dominio y CRUD parcial de notas, SPA inicial. | Base ejecutable para construir el flujo anónimo. Estado actual del proyecto. |
| 1B - Core anónimo | Código opaco de recepción, resolución de destinatario, envío autenticado, bandeja privada, DTOs sin remitente, paginación. | Flujo end-to-end anónimo funcional. |
| 1C - Safety y lanzamiento | Denuncia, bloqueo, pausa, rate limiting, anti-bot, auditoría mínima, cumplimiento y beta. | MVP apto para beta cerrada y posterior revisión pública. |

## 4.2 Incrementos posteriores

| Incremento | Alcance previsto | Condición de entrada |
|---|---|---|
| 2 | Panel mínimo de denuncias, detección de brigading y moderación asistida opcional. | Incremento 1 validado; safety básico operativo. |
| 3 | PWA, personalización y donativos opcionales sin ventajas de seguridad. | Retención y uso recurrente validados. |
| 4 | Respuestas limitadas o conversaciones anónimas consentidas y con límite de rondas. | Necesidad validada; threat model aprobado. |
| 5 | Comunidad anónima como bounded context separado, con feed y moderación comunitaria. | El core privado permanece estable y aislado. |

## 4.3 Exclusiones del MVP

- Feed o comunidad pública.
- Chat persistente o conversaciones sin consentimiento.
- Respuestas públicas.
- Moderación por IA como juez único.
- Monetización integrada.
- Aplicaciones móviles nativas.
- Gamificación, karma o reputación pública.
- Notificaciones push nativas.
- Panel completo de administración.
- Integración con redes sociales externas.

# 5. Contexto operativo, supuestos y restricciones

## 5.1 Entorno operativo

- Navegadores modernos con soporte de JavaScript ES2022 o posterior.
- API ejecutada sobre Java 21.
- MongoDB 7.
- Comunicación HTTPS en producción.
- Despliegue inicial compatible con Railway, Fly.io o proveedor cloud equivalente.

## 5.2 Restricciones arquitectónicas

- El backend debe mantenerse como monolito modular mientras no exista una razón medida para separar servicios.
- Los bounded contexts no deben importar modelos internos de otros contextos.
- `share` será el único módulo abierto y contendrá solo conceptos realmente compartidos.
- Los contextos se comunicarán mediante contratos públicos o eventos de dominio cuando exista comunicación entre ellos.
- Los DTOs públicos deben estar separados de los documentos Mongo y de la trazabilidad interna.
- La SPA seguirá usando React y Vite. El SEO completo se reserva para las páginas públicas cuando exista contenido definitivo.

## 5.3 Supuestos

- En el MVP, solo usuarios autenticados podrán enviar notas.
- El anonimato será social: el receptor no identifica al remitente, pero el sistema puede conservar identidad interna mínima.
- La política de edad mínima, retención y respuesta legal se cerrará antes del lanzamiento público.
- La moderación automática no será requisito para completar el Incremento 1.

## 5.4 Dependencias futuras

- Proveedor de email para verificación y recuperación de contraseña.
- Cloudflare Turnstile o challenge anti-bot equivalente.
- Redis o almacenamiento distribuido si el rate limiting debe escalar horizontalmente.
- Servicio externo de clasificación de contenido solo como apoyo opcional en incrementos posteriores.

# 6. Requisitos de interfaces externas

## 6.1 Interfaz de usuario

- La aplicación debe ofrecer rutas diferenciadas para acceso, cuenta, código de recepción, envío y bandeja.
- Las acciones críticas deben mostrar estados de carga, éxito y error comprensibles.
- Los errores de seguridad no deben revelar la existencia de cuentas, códigos o políticas internas.
- La interfaz debe ser utilizable con teclado, lectores de pantalla y dispositivos móviles.

## 6.2 API REST

- La API debe intercambiar JSON y usar versionado bajo `/v1` mientras dure esta línea base.
- Las rutas protegidas deben exigir autenticación y autorización por recurso, no solo por rol.
- El backend debe derivar la identidad del solicitante desde el principal autenticado; no debe confiar en un `ownerId` aportado por el cliente.
- Los errores deben seguir un contrato estable y no incluir stack traces, secretos ni datos internos.

## 6.3 Comunicaciones

- Todo tráfico de producción debe usar HTTPS/TLS.
- Los bearer tokens no deben aparecer en URLs, logs o analytics.
- Los clientes deben tratar de forma diferenciada, pero segura, respuestas 401, 403, 404, 409, 422 y 429.

## 6.4 Persistencia

- MongoDB almacenará cuentas, notas y, en las iteraciones correspondientes, códigos, reportes, bloqueos y señales de seguridad.
- Los índices de unicidad y consulta requeridos deben declararse explícitamente y probarse.

# 7. Requisitos funcionales

## 7.1 Cuenta, autenticación y sesión

| ID | Requisito verificable | Prioridad / fase | Estado actual |
|---|---|---|---|
| RF-001 | El sistema debe permitir registrar una cuenta con email único y contraseña mediante API y UI. Un segundo registro con el mismo email debe rechazarse sin duplicar la cuenta. | Alta / 1A | Parcial: API y unicidad implementadas; falta UI. |
| RF-002 | El sistema debe normalizar y validar el email, exigir contraseña de al menos 8 caracteres y exigir al menos un número o carácter especial. | Alta / 1A | Parcial: email validado; política de contraseña incompleta. |
| RF-003 | El sistema debe persistir solo un hash adaptativo de la contraseña mediante BCrypt con coste mínimo 12 o Argon2id. | Alta / 1A | Parcial: BCrypt implementado con coste por defecto no fijado en 12. |
| RF-004 | El sistema debe autenticar credenciales válidas y emitir una sesión expirable. Antes del lanzamiento, el access token debe durar como máximo 15 minutos y renovarse mediante un mecanismo revocable. | Alta / 1A-1C | Parcial: JWT HS256 de 24 horas, sin refresh ni revocación. |
| RF-005 | El sistema debe rechazar toda operación protegida sin autenticación y debe autorizar cada recurso contra la identidad actual. | Alta / 1A | Parcial: autenticación y roles existen; falta autorización por propiedad de nota. |
| RF-006 | Antes del lanzamiento público, el sistema debe exigir email verificado para enviar notas y evitar enumeración durante el flujo de verificación. | Alta / 1C | Pendiente; existe el campo `verified`, pero no el flujo ni su enforcement. |
| RF-007 | El sistema debe permitir restablecer la contraseña mediante token de un solo uso con expiración máxima de una hora. | Alta / 1C | Pendiente. |
| RF-008 | El sistema debe aplicar bloqueo progresivo configurable ante intentos fallidos de login. | Alta / 1C | Pendiente. |
| RF-009 | El usuario debe poder cerrar sesión explícitamente y el sistema debe impedir reutilizar la credencial revocada. | Media / 1C | Parcial: limpieza local disponible, sin UI ni invalidación backend. |
| RF-010 | El sistema puede detectar sesiones geográficamente incompatibles en una ventana corta sin convertir geolocalización en requisito del MVP. | Baja / 2 | Pendiente. |

## 7.2 Código de recepción

| ID | Requisito verificable | Prioridad / fase | Estado actual |
|---|---|---|---|
| RF-020 | Al crear una cuenta, el sistema debe generar un código de recepción único y opaco con al menos 128 bits de entropía efectiva. | Alta / 1B | Pendiente. |
| RF-021 | El receptor debe poder consultar, copiar y compartir su código o enlace desde su cuenta. | Alta / 1B | Pendiente. |
| RF-022 | El receptor debe poder regenerar el código; el anterior debe quedar invalidado inmediatamente sin eliminar notas existentes. | Alta / 1B | Pendiente. |
| RF-023 | El receptor debe poder pausar la recepción durante 1 hora, 24 horas, 7 días o indefinidamente. | Alta / 1C | Pendiente. |
| RF-024 | El sistema no debe entregar notas a códigos inexistentes, inactivos o pausados y debe devolver una respuesta que no revele cuál de esas condiciones ocurrió. | Alta / 1B | Pendiente. |
| RF-025 | El enlace público no debe incluir `userId`, email ni información derivable sobre la cuenta. | Alta / 1B | Pendiente. |
| RF-026 | El sistema debe registrar fecha y motivo de cada regeneración: manual, abuso o expiración futura. | Media / 1B | Pendiente. |

## 7.3 Envío de notas anónimas

| ID | Requisito verificable | Prioridad / fase | Estado actual |
|---|---|---|---|
| RF-030 | Solo un usuario autenticado y permitido debe poder enviar una nota a un código válido. | Alta / 1B | Parcial: creación autenticada por rol; aún usa IDs aportados por el cliente. |
| RF-031 | El contenido debe tener entre 2 y 1000 caracteres tras normalización; el máximo debe ser configurable. | Alta / 1B | Parcial: máximo 1000 y no vacío; acepta un carácter y no es configurable. |
| RF-032 | Antes de persistir, el sistema debe rechazar contenido vacío, excesivo, con controles inválidos o marcado como spam/prohibido por reglas deterministas. | Alta / 1B-1C | Parcial: validaciones básicas y URLs; faltan filtros y heurísticas. |
| RF-033 | La nota debe persistir `recipientUserId` y, para seguridad, `senderUserId` interno, sin exponer el remitente al receptor. | Alta / 1B | Parcial: modelo `ownerId/sharedWith` no cumple aún la semántica anónima. |
| RF-034 | El envío debe superar políticas de bloqueo, restricción, rate limiting y riesgo antes de persistirse. | Alta / 1C | Pendiente. |
| RF-035 | La respuesta de envío no debe permitir deducir si un código existe, está pausado, bloqueó al remitente o activó un filtro. | Alta / 1B-1C | Pendiente. |
| RF-036 | Tras persistir una nota anónima, el sistema debe publicar `PrivateNoteSent` con `noteId`, timestamp y contexto mínimo no público. | Media / 1B | Pendiente. |
| RF-037 | El contrato debe admitir tipos futuros de nota sin romper clientes existentes; texto será el único tipo obligatorio en el MVP. | Baja / 2 | Parcial: existe `PRIVATE/SHARED`, que representa visibilidad y no tipo de contenido. |

## 7.4 Bandeja y ciclo de vida

| ID | Requisito verificable | Prioridad / fase | Estado actual |
|---|---|---|---|
| RF-040 | El receptor debe listar exclusivamente sus notas recibidas, ordenadas por `createdAt` descendente. | Alta / 1B | Parcial: consultas por IDs del cliente, sin orden ni ownership seguro. |
| RF-041 | Ninguna respuesta de bandeja o detalle debe contener `senderUserId`, email, IP, user-agent o derivados del remitente. | Alta / 1B | Pendiente para el modelo anónimo; el DTO actual expone IDs de relación. |
| RF-042 | El receptor debe marcar notas como leídas individualmente y en bloque, registrando `readAt`. | Alta / 1B | Parcial: operación individual implementada sin autorización de propietario. |
| RF-043 | El receptor debe ocultar una nota mediante eliminación lógica, conservándola para una posible denuncia. | Alta / 1B | Parcial de dominio: existe `HIDDEN`, pero no caso de uso HTTP/UI. |
| RF-044 | El receptor debe solicitar borrado definitivo; una nota con denuncia abierta no debe borrarse físicamente. | Media / 1C | Parcial: `DELETE` cambia a `DELETED`, pero no respeta denuncia abierta ni ownership. |
| RF-045 | La bandeja debe usar paginación por cursor y devolver un cursor siguiente opaco cuando haya más resultados. | Alta / 1B | Pendiente. |
| RF-046 | El receptor debe filtrar su bandeja por nuevas, leídas, ocultas y denunciadas. | Media / 1B | Parcial backend genérico; UI y aislamiento por receptor pendientes. |
| RF-047 | El sistema debe devolver el número de notas no leídas sin cargar toda la bandeja. | Media / 1B | Pendiente. |

## 7.5 Denuncia, bloqueo y control del receptor

| ID | Requisito verificable | Prioridad / fase | Estado actual |
|---|---|---|---|
| RF-050 | El receptor debe poder denunciar una nota que le pertenece mientras exista en su bandeja. | Alta / 1C | Pendiente; solo existe transición de dominio sin caso de uso. |
| RF-051 | Una denuncia debe exigir uno de estos motivos: acoso, insultos, amenaza, spam, contenido sexual, autolesión, suplantación u otro. | Alta / 1C | Pendiente. |
| RF-052 | La denuncia puede incluir comentario de hasta 500 caracteres. | Media / 1C | Pendiente. |
| RF-053 | Denunciar debe ocultar la nota de la bandeja activa y permitir verla mientras el proceso siga abierto. | Alta / 1C | Pendiente. |
| RF-054 | Amenaza, extorsión, autolesión o contenido ilegal severo deben producir severidad CRITICAL y preservar evidencia según la política aprobada. | Alta / 1C | Pendiente. |
| RF-055 | El receptor debe bloquear al remitente interno desde una nota sin conocer su identidad. | Alta / 1C | Pendiente. |
| RF-056 | Un remitente bloqueado no debe entregar nuevas notas a ese receptor; la respuesta no debe confirmar el bloqueo. | Alta / 1C | Pendiente. |
| RF-057 | El receptor debe listar y retirar bloqueos mediante identificadores opacos que no revelen la identidad bloqueada. | Media / 1C | Pendiente. |
| RF-058 | El receptor debe pausar y reanudar la recepción con una sola acción desde ajustes o bandeja. | Alta / 1C | Pendiente. |
| RF-059 | Denuncia, bloqueo y pausa deben estar disponibles en un máximo de dos interacciones desde una nota. | Alta / 1C | Pendiente. |

## 7.6 Antiabuso y automatización hostil

| ID | Requisito verificable | Prioridad / fase | Estado actual |
|---|---|---|---|
| RF-060 | El sistema debe aplicar límites configurables por remitente por minuto, hora y día. | Alta / 1C | Pendiente. |
| RF-061 | El sistema debe limitar el volumen recibido por un único receptor dentro de una ventana temporal. | Alta / 1C | Pendiente. |
| RF-062 | El sistema debe limitar por IP usando una representación hasheada y una retención corta documentada. | Alta / 1C | Pendiente. |
| RF-063 | Las cuentas con menos de 24 horas o riesgo elevado deben recibir límites más estrictos. | Alta / 1C | Pendiente. |
| RF-064 | El sistema debe activar un challenge anti-bot cuando el riesgo supere un umbral configurable. | Alta / 1C | Pendiente. |
| RF-065 | El sistema debe detectar y penalizar patrones de enumeración de códigos sin revelar qué valores existen. | Alta / 1C | Pendiente. |
| RF-066 | El sistema debe registrar `SafetySignal` con datos mínimos, IP hasheada cuando proceda y TTL definido. | Alta / 1C | Pendiente. |
| RF-067 | Un operador autorizado debe poder restringir temporalmente el envío, suspender o cerrar una cuenta. | Media / 2 | Pendiente. |
| RF-068 | El sistema debe detectar contenido idéntico o altamente similar enviado repetidamente en una ventana corta. | Media / 1C | Pendiente. |

# 8. Requisitos no funcionales

## 8.1 Seguridad

| ID | Requisito verificable | Fase | Estado actual |
|---|---|---|---|
| RNF-001 | El backend debe validar toda entrada externa independientemente de la validación del cliente. | 1A | Parcial: value objects validan campos; falta validación sistemática de DTOs y propiedad. |
| RNF-002 | La API y UI no deben exponer trazabilidad interna del remitente. | 1B | Pendiente para el flujo anónimo. |
| RNF-003 | Producción debe aceptar tráfico exclusivamente mediante HTTPS/TLS. | 1C | Pendiente de infraestructura. |
| RNF-004 | Las credenciales de sesión deben expirar, ser revocables y no aparecer en URLs o logs. | 1C | Parcial: JWT expira; no es revocable y dura 24 horas. |
| RNF-005 | Claves JWT, credenciales y salts deben proceder de secretos externos sin valores productivos por defecto. | 1A | Parcial: Mongo externalizado; existe fallback de clave JWT. |
| RNF-006 | Producción debe emitir HSTS, CSP, X-Content-Type-Options y una política anti-framing equivalente a `frame-ancestors`. | 1C | Parcial por defaults de Spring; falta política explícita completa. |
| RNF-007 | El hash de contraseña debe usar BCrypt ≥12 o Argon2id y debe existir prueba de configuración. | 1A | Parcial. |
| RNF-008 | Toda operación sobre una nota debe verificar que el principal es propietario, receptor autorizado o moderador autorizado. | 1A-1B | Pendiente; bloqueante de seguridad actual. |
| RNF-009 | Los endpoints de login, registro, código y envío deben resistir enumeración mediante mensajes y tiempos razonablemente uniformes. | 1C | Pendiente. |

## 8.2 Privacidad y cumplimiento

| ID | Requisito verificable | Fase | Estado actual |
|---|---|---|---|
| RNF-010 | Solo procesos de seguridad o moderadores autorizados pueden acceder a trazabilidad interna. | 1C | Pendiente. |
| RNF-011 | IP y user-agent deben pseudonimizarse con hash y salt rotable cuando sea necesario conservarlos. | 1C | Pendiente. |
| RNF-012 | Antes del lanzamiento, el usuario debe poder solicitar una exportación legible de sus datos. | 1C | Pendiente. |
| RNF-013 | Antes del lanzamiento, el usuario debe poder eliminar su cuenta y el sistema debe borrar o anonimizar datos personales según política. | 1C | Pendiente. |
| RNF-014 | Debe existir una política aprobada de retención; el objetivo inicial para señales y logs técnicos será un máximo de 90 días salvo obligación legítima. | 1C | Pendiente. |
| RNF-015 | Las finalidades y bases legales de cada dato deben documentarse antes de una beta pública. | 1C | Pendiente de revisión legal. |

## 8.3 Rendimiento y capacidad

| ID | Requisito verificable | Fase | Estado actual |
|---|---|---|---|
| RNF-020 | En carga inicial de hasta 100 usuarios concurrentes, enviar una nota debe responder en menos de 500 ms p99, excluyendo challenge externo. | 1C | Pendiente de pruebas. |
| RNF-021 | La primera página de bandeja debe responder en menos de 300 ms p99 bajo la misma carga. | 1C | Pendiente. |
| RNF-022 | El tiempo de consulta no debe crecer linealmente con el total de notas; se usará paginación por cursor e índices. | 1B | Pendiente. |
| RNF-023 | Deben existir índices explícitos para email, código, receptor, remitente interno cuando proceda, estado y fecha de creación. | 1B | Parcial: solo email único está declarado explícitamente. |
| RNF-024 | Los límites y presupuestos deben comprobarse mediante una prueba reproducible y registrar p50, p95 y p99. | 1C | Pendiente. |

## 8.4 Mantenibilidad y arquitectura

| ID | Requisito verificable | Fase | Estado actual |
|---|---|---|---|
| RNF-030 | Los límites de Spring Modulith deben verificarse automáticamente en CI. | 1A | Parcial: librería y estructura existen; falta test `ApplicationModules.verify()`. |
| RNF-031 | Cada capacidad nueva debe implementarse en un slice y contexto explícitos sin crear dependencias circulares. | Todas | Implementado como convención; pendiente de enforcement automático. |
| RNF-032 | Los contextos no deben compartir agregados; solo contratos públicos, eventos o el shared kernel mínimo. | Todas | Parcial: `account` y `notes` están separados; falta test arquitectónico. |
| RNF-033 | Configuración de base de datos, URLs, secretos y límites debe externalizarse por entorno. | 1A | Parcial: frontend y expiración JWT tienen valores hardcodeados. |
| RNF-034 | El frontend debe centralizar el cliente HTTP y el tratamiento de 401, 403, 404, 409, 422 y 429. | 1B | Pendiente; `fetch` está disperso. |

## 8.5 Observabilidad y auditoría

| ID | Requisito verificable | Fase | Estado actual |
|---|---|---|---|
| RNF-040 | El sistema debe registrar errores y decisiones operativas relevantes sin passwords, tokens ni datos personales innecesarios. | 1A-1C | Parcial: SLF4J existe, pero algunos logs incluyen emails e IDs. |
| RNF-041 | Debe existir un health endpoint autenticado o aislado compatible con monitorización. | 1C | Pendiente; no hay Actuator. |
| RNF-042 | Eventos críticos de seguridad deben registrarse en auditoría separada con acceso y retención controlados. | 1C | Pendiente. |
| RNF-043 | Los logs deben distinguir errores de dominio, autenticación, persistencia e infraestructura. | 1A | Parcial. |
| RNF-044 | Cada petición debe disponer de correlation-id propagado y visible en logs. | 1C | Pendiente conforme a ADR-08. |

## 8.6 Usabilidad y accesibilidad

| ID | Requisito verificable | Fase | Estado actual |
|---|---|---|---|
| RNF-050 | Denuncia, bloqueo y pausa deben ser comprensibles sin documentación y accesibles en dos interacciones. | 1C | Pendiente. |
| RNF-051 | Todo fallo visible debe explicar la acción posible sin revelar detalles técnicos o de seguridad. | 1A-1C | Pendiente: errores actuales solo en consola. |
| RNF-052 | Registro, obtención de código y primer envío deben completarse en menos de 3 minutos en prueba de usabilidad. | 1C | Pendiente. |
| RNF-060 | Las interfaces públicas y autenticadas deben aspirar a WCAG 2.2 nivel AA. | Todas | Parcial: buena semántica base, sin auditoría WCAG. |
| RNF-061 | Todas las acciones deben ser operables por teclado con foco visible y orden lógico. | Todas | Parcial: controles principales son nativos; quedan elementos y focus states incompletos. |
| RNF-062 | La UI debe reordenarse sin pérdida funcional a 320 CSS px y zoom del 200 %. | 1B | Pendiente: el layout de notas fija tres columnas. |
| RNF-063 | Mensajes de error, carga y éxito deben anunciarse mediante mecanismos accesibles como `aria-live`. | 1A | Pendiente. |
| RNF-064 | Las animaciones no esenciales deben respetar `prefers-reduced-motion`. | 1A | Parcial. |
| RNF-065 | El contraste de texto y controles debe comprobarse contra WCAG AA. | 1B | Pendiente de medición. |
| RNF-066 | Los objetivos táctiles principales deben tener tamaño y separación suficientes para uso móvil. | 1B | Parcial. |

## 8.7 Compatibilidad web, SEO y compartición

| ID | Requisito verificable | Fase | Estado actual |
|---|---|---|---|
| RNF-070 | Las rutas privadas y de autenticación no deben indexarse. | 1C | Pendiente: existe un `robots` global `index, follow`. |
| RNF-071 | La landing pública debe definir title, description, canonical, Open Graph y una imagen de compartición válidos para el dominio real. | 1C | Parcial: metadatos base con canonical provisional, sin imagen. |
| RNF-072 | Deben existir `robots.txt` y sitemap para las rutas públicas que procedan antes del lanzamiento. | 1C | Pendiente. |
| RNF-073 | La estrategia de prerender o landing estática debe decidirse cuando el contenido público sea definitivo; no bloquea la aplicación privada. | 1C | Decisión diferida conforme a ADR-04. |

# 9. Modelo conceptual de información

## 9.1 Cuenta

La cuenta identifica internamente a una persona y contiene, como mínimo: identificador, email normalizado único, hash de contraseña, rol, estado, verificación, fecha de alta y último login.

Estado actual: `ACTIVE` y `DELETED`. Estado objetivo antes de moderación: `ACTIVE`, `RESTRICTED`, `SUSPENDED` y `DELETED`, con transiciones explícitas.

## 9.2 Código de recepción

Entidad pendiente del contexto `receiving`: propietario, slug opaco único, estado activo, pausa, fechas y motivo de regeneración. El slug no será reversible ni derivado del identificador de cuenta.

## 9.3 Nota anónima

Entidad objetivo del core: identificador, receptor interno, remitente interno protegido, título opcional si se conserva el modelo actual, contenido, estado y timestamps de ciclo de vida.

El modelo actual `PrivateNote/SharedNote` con `ownerId/sharedWith` es una base técnica, no el contrato definitivo del buzón anónimo. La Iteración 1B debe resolver el lenguaje ubicuo y la migración sin exponer IDs internos.

## 9.4 Reporte

Entidad pendiente: nota, receptor que reporta, remitente interno, motivo, comentario opcional, severidad, estado y timestamps de revisión. Solo operadores autorizados acceden a la identidad interna.

## 9.5 Bloqueo de receptor

Entidad pendiente: receptor, remitente interno bloqueado, nota origen opcional y fecha. La UI debe utilizar un identificador opaco o una acción contextual, nunca la identidad real.

## 9.6 Señal de seguridad

Entidad pendiente: cuenta opcional, hash de IP opcional, acción, score de riesgo, metadatos mínimos y fecha con TTL.

# 10. Privacidad, anonimato y safety

## 10.1 Reglas no negociables

- Ningún endpoint del receptor expondrá identidad o metadata del remitente.
- La denuncia y el bloqueo no revelarán al receptor a quién está denunciando o bloqueando.
- Los DTOs públicos no reutilizarán documentos de persistencia.
- Los logs no incluirán tokens, contraseñas, contenido completo de notas ni IP en claro.
- La respuesta ante código inexistente, pausa o bloqueo no permitirá enumeración.
- La trazabilidad interna tendrá finalidad, acceso y retención documentados.

## 10.2 Niveles de acceso a datos

| Nivel | Datos | Acceso |
|---|---|---|
| Público | Contenido permitido por un flujo público y existencia no confirmable de enlace. | Visitantes según caso de uso. |
| Receptor | Bandeja, contenido, fechas y estados, sin remitente. | Propietario autenticado. |
| Sistema/safety | Identidad interna, hashes técnicos y señales de riesgo. | Procesos y operadores autorizados. |
| Legal | Evidencia preservada de incidentes graves. | Proceso documentado y acceso excepcional. |

## 10.3 Defensa por capas

1. Validación determinista de formato y longitud.
2. Normalización segura del texto.
3. Rate limiting por remitente, receptor e IP pseudonimizada.
4. Heurísticas de repetición y enumeración.
5. Challenge anti-bot condicionado por riesgo.
6. Denuncia y bloqueo inmediato por el receptor.
7. Revisión humana de casos graves.
8. Clasificación automática opcional y nunca juez único.

## 10.4 Incidentes críticos

Antes del lanzamiento debe existir un procedimiento revisado legalmente para amenaza creíble, extorsión, autolesión o contenido ilegal severo. El software deberá permitir preservar evidencia, restringir la cuenta y registrar cada acceso. La respuesta institucional no se automatizará sin política y asesoramiento legal aplicables.

# 11. Validación y verificación

## 11.1 Estrategia de pruebas

| Nivel | Objetivo |
|---|---|
| Unitarias | Reglas de value objects, transiciones, ownership, códigos, bloqueos y riesgo sin infraestructura. |
| Integración | Mapeos y repositorios MongoDB mediante Testcontainers o entorno reproducible. |
| API | Contratos, autenticación, autorización por recurso y escenarios negativos de privacidad. |
| Frontend | Formularios, rutas, estados de carga/error, teclado y comunicación con API simulada. |
| End-to-end | Registro → código → envío → bandeja → denuncia/bloqueo. |
| Seguridad | OWASP Top 10 relevante, JWT, fuerza bruta, NoSQL injection, XSS, CSRF según mecanismo de sesión, enumeración y bypass de rate limit. |
| Arquitectura | `ApplicationModules.verify()` y reglas de dependencia entre contextos. |
| Rendimiento | Presupuestos p99 y comportamiento bajo límites. |
| Privacidad | Aserciones negativas: campos internos ausentes en respuestas y logs. |
| Usabilidad | Flujo completo menor de 3 minutos y safety en dos interacciones. |

## 11.2 Criterio de requisito verificable

Cada requisito se considerará satisfecho solo si existe:

1. Implementación funcional.
2. Prueba automatizada o evidencia de validación adecuada.
3. Trazabilidad al caso de uso o riesgo que lo originó.
4. Ausencia de una brecha conocida que invalide su objetivo principal.

## 11.3 Escenarios críticos de aceptación

### Autorización por recurso

**Dado** un usuario autenticado A y una nota de B, **cuando** A intenta consultar, leer, ocultar o eliminar la nota por ID, **entonces** el sistema rechaza la operación sin revelar si la nota existe.

### Privacidad del remitente

**Dado** un receptor con una nota anónima, **cuando** consulta bandeja o detalle, **entonces** la respuesta no contiene `senderUserId`, email, IP, user-agent ni derivados.

### Bloqueo silencioso

**Dado** que el receptor bloqueó al remitente desde una nota, **cuando** ese remitente intenta enviar otra, **entonces** la nota no se entrega y la respuesta no confirma el bloqueo.

### Regeneración de código

**Dado** un código activo, **cuando** el receptor lo regenera, **entonces** el anterior queda inválido inmediatamente y las notas existentes permanecen accesibles.

### Rate limiting

**Dado** un remitente que supera el límite configurado, **cuando** intenta enviar, **entonces** el sistema impide el abuso, registra una señal mínima y responde sin detalles de política interna.

# 12. Trazabilidad

## 12.1 Capacidades y requisitos

| Capacidad | Requisitos | Iteración |
|---|---|---|
| Cuenta y sesión | RF-001..010, RNF-004..009 | 1A-1C |
| Código de recepción | RF-020..026 | 1B-1C |
| Entrega anónima | RF-030..037, RNF-002 | 1B-1C |
| Bandeja | RF-040..047, RNF-008 | 1B-1C |
| Safety | RF-050..059, RNF-010..015 | 1C-2 |
| Antiabuso | RF-060..068 | 1C-2 |
| Rendimiento | RNF-020..024 | 1B-1C |
| Arquitectura | RNF-030..034 | Todas |
| Observabilidad | RNF-040..044 | 1A-1C |
| UX y accesibilidad | RNF-050..066 | Todas |
| SEO/compartición | RNF-070..073 | 1C |

## 12.2 Artefactos relacionados

- `docs/adrs/`: decisiones arquitectónicas.
- `docs/Ghost_Notes_C4_Level_1_System_Context.txt`: contexto de sistema.
- `docs/Ghost_Notes_C4_Level_2_Container_Diagram.txt`: contenedores.
- `docs/Ghost_Notes_C4_Level_3_Backend_Components.txt`: componentes backend.
- `docs/Ghost_Notes_DDD_Context_Map_PlantUML.txt`: relaciones DDD.
- `docs/Ghost_Notes_JWT_Sequence_Diagram_PlantUML.txt`: login y autenticación JWT.
- `docs/Ghost_Notes_Use_Cases_PlantUML.txt`: casos de uso implementados en la base actual.

# 13. Estado actual y brechas bloqueantes

La Iteración 1A está en progreso. La aplicación dispone de estructura modular, cuentas, login JWT, persistencia Mongo y operaciones básicas de notas. Todavía no debe presentarse como MVP anónimo seguro.

## 13.1 Bloqueantes inmediatos

1. Los endpoints de notas no autorizan por propietario/receptor.
2. Las consultas aceptan `ownerId` y `sharedWith` del cliente.
3. El DTO de notas expone IDs de relación incompatibles con el futuro anonimato.
4. El JWT dura 24 horas, no es revocable y se persiste en `localStorage`.
5. BCrypt no fija el coste mínimo requerido.
6. Existe un valor por defecto para el secreto JWT.
7. No existen tests de autenticación, autorización por recurso o privacidad.
8. El frontend no tiene registro, errores visibles ni sesión expirable.
9. Lista y editor usan fuentes de datos distintas; varias acciones son solo visuales.
10. El layout principal aún no soporta móvil correctamente.

## 13.2 Brechas del MVP anónimo

- No existe contexto `receiving`.
- No existe código opaco ni ruta de envío.
- No existe bandeja anónima segura.
- No existe contexto `safety`.
- No existen denuncia, bloqueo, pausa, rate limiting o challenge.
- No existen eventos de dominio ni auditoría crítica.
- No existe documentación legal aprobada.

# 14. Riesgos

| Riesgo | Probabilidad | Impacto | Mitigación requerida |
|---|---|---|---|
| Acceso horizontal a notas ajenas | Alta | Crítico | Autorización por recurso derivada del principal y tests negativos. |
| Exposición de identidad del remitente | Media | Crítico | DTOs específicos de receptor, pruebas de contrato y revisión de logs. |
| Hostigamiento sistemático | Alta | Alto | Bloqueo, pausa y límites por receptor/remitente. |
| Enumeración de cuentas o códigos | Alta | Alto | Slugs de alta entropía, respuestas uniformes y rate limiting. |
| Robo de token por XSS | Media | Alto | CSP, token corto, renovación revocable y revisión del almacenamiento cliente. |
| Brigading | Media | Alto | Señales de riesgo, límites por receptor y antigüedad de cuenta. |
| Moderación insuficiente | Media | Alto | Reportes, severidad, procedimiento humano y beta cerrada. |
| Sobrearquitectura prematura | Media | Medio | Mantener monolito modular y añadir contextos solo por capacidad real. |
| Desalineación entre UI personal y producto anónimo | Alta | Alto | Priorizar Iteración 1B y ajustar lenguaje, rutas y DTOs al SRS. |
| Incumplimiento legal | Media | Crítico | Revisión profesional antes de beta pública y política de retención aprobada. |

# 15. Decisiones abiertas

Estas decisiones no impiden continuar la Iteración 1A, pero deben cerrarse antes de la fase indicada:

| Decisión | Fecha límite |
|---|---|
| Mecanismo definitivo de access/refresh token y almacenamiento cliente. | Antes de 1C. |
| Proveedor y flujo de verificación/recuperación por email. | Antes de 1C. |
| Estrategia de rate limiting local/distribuida. | Antes de implementar RF-060. |
| Política de edad mínima y consentimiento. | Antes de beta pública. |
| Retención de notas, logs, señales y evidencia crítica. | Antes de beta pública. |
| Lenguaje definitivo del modelo: nota personal/compartida frente a mensaje anónimo recibido. | Antes de 1B. |
| Prerender o landing estática para páginas públicas. | Antes del lanzamiento público. |

# 16. Criterios de salida del Incremento 1

El Incremento 1 solo podrá declararse completo cuando:

- El flujo end-to-end anónimo funciona en producción-like.
- Todos los requisitos de prioridad alta asignados a 1A, 1B y 1C están implementados o cuentan con excepción formal aprobada.
- Las pruebas de API confirman que no se expone al remitente.
- La autorización horizontal está cubierta por tests negativos.
- Rate limiting, bloqueo, denuncia y pausa funcionan.
- La verificación de Spring Modulith pasa en CI.
- Los presupuestos de rendimiento se han medido.
- La interfaz supera una revisión básica de WCAG 2.2 AA y diseño adaptable.
- Las políticas legales y el procedimiento crítico han sido revisados.
- La beta cerrada termina sin incidentes graves de privacidad.

# 17. Glosario

| Término | Definición |
|---|---|
| Anonimato social | El receptor y otros usuarios no pueden identificar al remitente, aunque el sistema conserve trazabilidad interna protegida. |
| Bounded context | Límite dentro del cual un modelo y lenguaje de dominio son consistentes. |
| Brigading | Acoso coordinado desde múltiples cuentas contra un receptor. |
| Challenge | Prueba anti-bot activada según riesgo. |
| Core domain | Área que proporciona el valor diferencial principal; en Ghost Notes, la entrega privada anónima segura. |
| CSPRNG | Generador pseudoaleatorio criptográficamente seguro. |
| DTO | Objeto de transferencia que separa el contrato público del modelo interno. |
| Enumeración | Prueba sistemática de identificadores para deducir recursos existentes. |
| Monolito modular | Una unidad de despliegue con límites internos explícitos y verificables. |
| Pseudonimización | Transformación que reduce la asociación directa con una persona, sin equivaler necesariamente a anonimización irreversible. |
| Rate limiting | Restricción de frecuencia de acciones en una ventana temporal. |
| SRS | Software Requirements Specification. |
| UGC | Contenido generado por usuarios. |

# 18. Referencias

- ISO, **ISO/IEC/IEEE 29148:2018 - Systems and software engineering - Life cycle processes - Requirements engineering**: https://www.iso.org/standard/72089.html
- IEEE Standards Association, **IEEE/ISO/IEC 29148-2018**: https://standards.ieee.org/ieee/29148/6937/
- Documento de origen: `backend/docs/Ghost_Notes_Requerimientos_v1.0.pdf`.
- Architecture Decision Records: `backend/docs/adrs/`.
- OWASP Application Security Verification Standard: https://owasp.org/www-project-application-security-verification-standard/
- WCAG 2.2: https://www.w3.org/TR/WCAG22/
