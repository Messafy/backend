# ADR-08: Logging como cross-cutting: interceptor HTTP + SLF4J explícito, AOP reservado

- **Fecha:** 2026-09-08
- **Estado:** Aceptada (implementación pendiente)
- **Ámbito:** Backend

## Contexto

El backend ya tiene flujos reales (login, notas) y aún no tiene estrategia de logging. El logging es el ejemplo canónico de cross-cutting concern, y la respuesta "de libro" en Spring es AOP (`@Aspect` con `@Around` sobre los servicios). Había que decidir si adoptar esa aproximación o algo más pragmático.

Consideraciones:

- Un pointcut amplio (ej. `execution(* com.luispiquinrey.backend..*Service.*(..))`) loguearía todo con el mismo formato: ruido, no señal.
- El AOP oculta el comportamiento: al leer un service no se ve que hay advice alrededor, y los pointcuts por paquete son frágiles ante refactors.
- El proyecto corre sobre Spring Modulith (ADR-01) y quiere un dominio limpio de preocupaciones técnicas.
- El 80% del valor del logging de aplicación (qué petición llegó, con qué resultado, cuánto tardó, con qué correlation-id) se resuelve a nivel HTTP, sin tocar los servicios.

## Alternativas consideradas

### Alternativa A: AOP generalizado para logging

Un aspect que envuelva todos los servicios y loguee entrada/salida/tiempo automáticamente.

- **A favor:** cero código repetido; cobertura instantánea de todos los servicios.
- **En contra:** loguea por defecto en lugar de por decisión (ruido y datos potencialmente sensibles en logs); oculta el flujo; testing de aspects molesto; pointcuts frágiles. El coste de mantenimiento supera al ahorro a esta escala.

### Alternativa B: SLF4J explícito en todos los servicios

- **A favor:** explícito y grepeable; se elige qué y en qué nivel se loguea.
- **En contra:** repetición manual; sin correlation-id transversal a menos que se propague a mano.

### Alternativa C: Híbrido — interceptor HTTP + SLF4J explícito + AOP quirúrgico (elegida)

## Decisión

Tres piezas, cada una en su sitio:

1. **Interceptor/filter HTTP en `share/infrastructure/`** (por implementar): loguea por petición método, ruta, status, duración y un **correlation-id** (generado por petición, propagable vía header `X-Correlation-Id`). Cubre la visión transversal sin tocar ningún slice. Para correlation-id se usará MDC de SLF4J.
2. **SLF4J explícito en los servicios de aplicación** de cada slice: se loguean operaciones de negocio relevantes y errores con contexto, nunca datos sensibles (passwords, tokens). Explícito, grepeable y revisable en code review.
3. **AOP solo quirúrgico**: los aspects quedan reservados para necesidades transversales puntuales y *opt-in* mediante anotación propia (ej. `@Audited`, `@Timed`) sobre métodos concretos, no pointcuts por paquete. Se introducirán cuando exista un requisito real (auditoría, métricas de negocio).

Reglas permanentes:

- El `domain/` de ningún contexto contiene logging; el dominio habla mediante sus abstracciones y, si procede, eventos de dominio.
- Si se loguea a través de eventos, se usan los mecanismos de Spring Modulith (`ApplicationModuleListener`), nunca dependencias directas entre slices.
- Nada de loguear secretos: tokens JWT, passwords o payloads de autenticación no aparecen nunca en logs.

## Consecuencias

### Positivas

- Cobertura transversal (HTTP) + señal de negocio (servicios) sin acoplar nada al dominio ni entre slices.
- El correlation-id en MDC permite seguir una petición de extremo a extremo en los logs, base para debugging y para trazabilidad futura (Micrometer/Actuator cuando toque).
- El AOP queda en la caja de herramientas para el día que resuelva un problema real, con uso controlado y visible.

### Negativas / riesgos

- El SLF4J explícito exige disciplina en cada slice nuevo (recordatorio: revisar logging en code review de cada feature).
- El interceptor HTTP no ve lo que ocurre *dentro* de una operación larga multi-paso; ahí es donde el logging explícito de servicios cubre el hueco.
- Implementación pendiente: este ADR se marca Aceptada como decisión; la pieza 1 requiere un commit de implementación (filter + MDC + configuración).

## Referencias

- ADR-01 (arquitectura por slices y shared kernel, donde vivirá el interceptor)
- SLF4J MDC — https://www.slf4j.org/manual.html#mdc
- Spring Modulith — https://docs.spring.io/spring-modulith/reference/
