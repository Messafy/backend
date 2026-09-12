# ADR-01: Arquitectura de vertical slices con bounded contexts y Spring Modulith

- **Fecha:** 2026-08-27 (decisión previa al inicio del desarrollo; registrada en ADR el 2026-09-08)
- **Estado:** Aceptada
- **Ámbito:** Backend

## Contexto

Ghost Notes es una aplicación de notas privadas que arranca como proyecto personal. Antes de escribir la primera línea de código había que decidir cómo organizar el código del backend: la organización por capas técnicas horizontales (controller/service/repository globales) es la más común en Spring Boot, pero tiene problemas conocidos de cohesión y acoplamiento a medida que crece el proyecto.

Restricciones y deseos iniciales:

- El dominio tiene al menos dos bounded contexts claros: `account` (cuentas, autenticación) y `notes` (notas y su gestión).
- Se quería seguir principios DDD sin adoptar un framework DDD pesado.
- Se deseaba poder verificar la modularidad de forma automática, no solo por convención.
- El equipo es pequeño (una persona), por lo que la estructura debía ser simple de mantener y explicarse sola.

## Alternativas consideradas

### Alternativa A: Arquitectura en capas horizontales clásica

Paquetes por capa técnica: `controllers/`, `services/`, `repositories/`, `entities/`, compartiendo todos los contextos.

- **A favor:** es la convención por defecto de Spring; casi cualquier desarrollador la entiende al instante.
- **En contra:** los cambios a una feature atraviesan varios paquetes dispersos; el acoplamiento entre features crece de forma silenciosa; es fácil acabar con "god services" compartidos por todos los contextos.

### Alternativa B: Microservicios desde el inicio

Un servicio por bounded context (`account-service`, `notes-service`).

- **A favor:** aislamiento total, despliegues independientes.
- **En contra:** coste operacional enorme para un proyecto en fase temprana (descubrimiento, configuración distribuida, contratos, despliegues, observabilidad distribuida). Se premia una escalabilidad de despliegue que el proyecto no necesita todavía.

### Alternativa C: Modular monolith con vertical slices (elegida)

Organizar el código por **bounded context** (`account/`, `notes/`) y, dentro de cada uno, por **feature slice** (`slices/<feature>/`), donde cada slice contiene su Controller, Service, interfaz de Repository, implementación Mongo y DTOs. Un **shared kernel** (`share/`) contiene identidad, tiempo e infraestructura común, y es el único módulo desde el que otros contextos pueden depender.

Spring Modulith se usa para:

- Declarar la modularidad con `@ApplicationModule` (jMolecules) y verificarla en tests (`ApplicationModularityTests`), de modo que una dependencia ilegal entre contextos rompa el build.
- Mantener el `share/` como módulo `OPEN` y los contextos como módulos cerrados por defecto.

## Decisión

Adoptar un monolito modular con vertical slices organizadas por bounded context, con las reglas siguientes:

1. Cada contexto (`account/`, `notes/`) es un módulo Spring Modulith con `domain/`, `infrastructure/` y `slices/<feature>/`.
2. Una nueva funcionalidad es siempre un nuevo slice dentro del contexto correspondiente; nunca se extiende un slice existente para acomodar features distintas.
3. El único módulo dependible entre contextos es `share/`, declarado como `@ApplicationModule(type = OPEN)`.
4. La modularidad se verifica automáticamente en el build mediante Spring Modulith.

## Consecuencias

### Positivas

- Alta cohesión: todo lo relativo a una feature vive en un solo paquete; un slice se puede leer, testear y borrar de forma aislada.
- Refactors locales: cambiar una feature no contamina a las demás, y el boundary del contexto es explícito.
- El monolito conserva la simplicidad de despliegue, pero deja preparado el camino hacia extracción de servicios si algún contexto lo exige en el futuro.
- La verificación de Modulith convierte la arquitectura en algo ejecutable, no solo en un diagrama.

### Negativas / riesgos

- Curva de aprendizaje: la estructura por slices no es la convención por defecto de Spring y exige disciplina (es tentador colar dependencias entre slices).
- Algo de duplicación deliberada entre slices (DTOs o mappers similares), que se acepta a cambio de desacoplamiento.
- Riesgo de sobre-fragmentación: features demasiado pequeñas generan slices triviales; se mitiga agrupando la funcionalidad en slices de tamaño razonable.

## Referencias

- Spring Modulith — https://docs.spring.io/spring-modulith/reference/
- jMolecules — https://jmolecules.org/
- Documentos de dominio en `backend/docs/` (context map, class diagram, use cases).
