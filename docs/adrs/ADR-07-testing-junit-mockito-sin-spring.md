# ADR-07: Testing en backend con JUnit 5 + Mockito, sin contexto de Spring

- **Fecha:** 2026-08-27 (decisión previa al inicio del desarrollo; registrada en ADR el 2026-09-08)
- **Estado:** Aceptada
- **Ámbito:** Backend

## Contexto

El backend sigue DDD con slices aislados (ADR-01), lo que produce servicios y lógica de dominio altamente cohesivos y con dependencias explícitas inyectables. Eso abre una pregunta de estrategia de tests: ¿tests de integración con Spring levantando contexto (y MongoDB), tests unitarios puros, o una mezcla?

Restricciones y observaciones:

- La velocidad del bucle de feedback es prioritaria en un proyecto personal con iteración constante.
- MongoDB corre en Docker; depender de él para cada test añade fragilidad (contenedor caído, latencia) o coste (Testcontainers en cada ejecución).
- La arquitectura por slices ya aísla las dependencias: los servicios reciben sus colaboradores por constructor.

## Alternativas consideradas

### Alternativa A: Tests de integración con `@SpringBootTest` (+ Testcontainers/Mongo de test)

- **A favor:** ejercitan el wiring real de Spring, los mapeos Mongo y la configuración completa; dan mucha confianza de extremo a extremo.
- **En contra:** arranque lento de contexto (segundos por suite), dependencia de infraestructura en cada run, y fallos difíciles de localizar (¿es el código o el wiring?).

### Alternativa B: Tests unitarios puros con mocks (elegida como base)

### Alternativa C: Híbrido (unitarios + una capa fina de `@WebMvcTest`/`@DataMongoTest` por slice)

Reconocida como el estado final deseable a futuro, pero diferida: primero consolidar la base unitaria, añadir integración cuando existan flujos críticos que la justifiquen.

## Decisión

1. **Tests unitarios con JUnit 5 y Mockito puros**: no se arranca contexto de Spring en ninguna prueba actual.
2. Los tests **replican la estructura de paquetes** del código principal (`src/test/java/.../notes/slices/create/`), de modo que cada slice tiene su clase de test paralela.
3. **Convenciones obligatorias por método de test:**
   - `@Tag("<camelCaseSubject>")`: el tag describe el comportamiento verificado y permite filtrar ejecuciones (`-Dgroups=...`).
   - `@Timeout(1)`: todo test debe terminar en menos de un segundo; si lo necesita, es señal de que está tocando I/O real.
4. La verificación del proyecto es `./mvnw test`; se puede ejecutar una sola clase con `./mvnw test -Dtest=CreateNoteServiceTest`.
5. El perfil `application-test.yml` (base `test-privatenotes`) queda definido para la futura capa de integración, que no existe todavía.

## Consecuencias

### Positivas

- Suite rápida y estable: sin contexto Spring ni MongoDB, el feedback es casi instantáneo y no hay dependencias externas que fallen.
- El diseño empuja a la inyección por constructor y a interfaces limpias; si algo es difícil de mockear, el diseño está mal, no el test.
- Los `@Tag` documentan el comportamiento y permiten seleccionar subconjuntos de la suite.
- La estructura paralela de paquetes hace trivial localizar el test de cualquier slice.

### Negativas / riesgos

- **Cobertura de wiring:** con solo unitarios, un error de configuración de Spring (bean faltante, mapping Mongo mal definido) solo se descubre al ejecutar la app. Mitigación futura: la capa de integración por slice (opción C) o, como mínimo, el test de modularidad de Spring Modulith que ya valida boundaries en el build.
- Duplicación de setup entre tests de un mismo slice; mitigado con builders/fixtures sencillos si crece.
- La regla `@Timeout(1)` prohibe de facto tests de integración pesados dentro de esta convención; es intencional: cuando lleguen, se documentarán en un ADR propio.

## Referencias

- ADR-01 (arquitectura por slices, que habilita los unitarios puros)
- JUnit 5 — https://junit.org/junit5/
- Mockito — https://site.mockito.org/
