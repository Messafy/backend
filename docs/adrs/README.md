# Architecture Decision Records (ADRs)

Registro de decisiones de arquitectura del proyecto Ghost Notes.

Cada ADR documenta una decisión significativa e irreversible o costosa de revertir: el contexto que la motivó, las alternativas consideradas, la decisión tomada y sus consecuencias. Los ADRs son inmutables: si una decisión cambia, se escribe un nuevo ADR que la reemplace (y se actualiza su estado a `Reemplazada por ADR-XX`), nunca se reescribe el original.

## Formato

- `ADR-XX-titulo-corto.md`
- Campos mínimos: fecha, estado, ámbito, contexto, alternativas consideradas, decisión, consecuencias.

## Índice

| ADR | Título | Estado |
|-----|--------|--------|
| [ADR-01](ADR-01-arquitectura-vertical-slices-con-modulith.md) | Arquitectura de vertical slices con bounded contexts y Spring Modulith | Aceptada |
| [ADR-02](ADR-02-mongodb-como-persistencia.md) | MongoDB como base de datos de persistencia | Aceptada |
| [ADR-03](ADR-03-autenticacion-jwt.md) | Autenticación stateless con JWT (JJWT) | Aceptada |
| [ADR-04](ADR-04-frontend-spa-react-vite-seo-diferido.md) | Frontend SPA (React 19 + Vite), sin SSR, SEO diferido | Aceptada |
| [ADR-05](ADR-05-atomic-design-tokens-scss.md) | Atomic Design con design tokens en SCSS | Aceptada |
| [ADR-06](ADR-06-html-semantico-accesibilidad.md) | HTML semántico y accesibilidad como estándar del frontend | Aceptada |
| [ADR-07](ADR-07-testing-junit-mockito-sin-spring.md) | Testing backend: JUnit 5 + Mockito sin contexto de Spring | Aceptada |
| [ADR-08](ADR-08-logging-cross-cutting.md) | Logging cross-cutting: interceptor HTTP + SLF4J, AOP reservado | Aceptada (pendiente de implementar) |
