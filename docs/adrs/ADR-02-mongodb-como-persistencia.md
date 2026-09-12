# ADR-02: MongoDB como base de datos de persistencia

- **Fecha:** 2026-08-27 (decisión previa al inicio del desarrollo; registrada en ADR el 2026-09-08)
- **Estado:** Aceptada
- **Ámbito:** Backend

## Contexto

Las notas del usuario son documentos con contenido de texto libre, etiquetas y metadatos. El modelo es naturalmente jerárquico/documental: una nota tiene un título, un cuerpo extenso sin esquema fijo, tags, y propiedades de auditoría. No había requisitos de transacciones multi-entidad complejas ni de consultas analíticas relacionales en el alcance inicial.

Requisitos que influían en la decisión:

- Guardar contenido de notas de longitud y estructura variables.
- Consultas simples por propietario (`ownerId`) y por etiquetas.
- Puesta en marcha rápida en local (Docker) y en cloud.
- Coherencia con Spring Data (repositorios y mapeo documental maduros).

## Alternativas consideradas

### Alternativa A: PostgreSQL / MySQL

- **A favor:** garantías ACID completas, ecosistema maduro, herramientas de modelado relacional. Las notas como tablas con JOINs hacia tags es viable.
- **En contra:** el modelo de notas exige modelar tags en tabla intermedia, y el contenido libre en columnas `text`; no aporta valor real dado que no hay consultas relacionales complejas en el alcance. Mayor fricción de arranque (migraciones, esquema).

### Alternativa B: Base de datos embebida (H2/SQLite)

- **A favor:** cero operativa en desarrollo.
- **En contra:** no representaría el entorno de producción; limitaciones de concurrencia y de capacidades documentales.

### Alternativa C: MongoDB (elegida)

Base de datos documental: cada nota es un documento con esquema flexible, índices sobre `ownerId` y tags, y operativa sencilla vía Docker.

## Decisión

Adoptar MongoDB 7 como única base de datos del backend:

- Entorno local levantado con `docker compose` (`src/main/resources/docker-compose.yml`), base de datos `privatenotes`, puerto 27017.
- Conexión configurada vía variable `MONGO_URI` en `src/main/resources/.env`, importada por Spring (`spring.config.import: optional:classpath:.env[.properties]`).
- El perfil de tests (`application-test.yml`) usa una base de datos dedicada `test-privatenotes` para aislar las pruebas de los datos de desarrollo.
- Spring Data MongoDB como capa de acceso; cada slice define su interfaz de repositorio y su implementación Mongo en `infrastructure/`.

## Consecuencias

### Positivas

- El modelo documental encaja con la naturaleza de una nota sin mapeos relacionales artificiales.
- Iteración rápida: cambios de esquema no requieren migraciones versionadas en fase temprana.
- Operativa mínima: un contenedor Docker y una URI de conexión.
- La separación interfaz/implementación del repositorio por slice deja la puerta abierta a cambiar de tecnología por contexto sin tocar los servicios.

### Negativas / riesgos

- Sin transacciones multi-documento por defecto (Mongo las soporta en réplicas, pero no es su punto fuerte): las operaciones que algún día necesiten atomicidad multi-entidad requerirán diseño explícito.
- Consistencia eventual en despliegues con réplicas; suficiente para notas personales, pero debe reevaluarse si aparecen requisitos de fuerte consistencia.
- El esquema flexible es un arma de doble filo: sin disciplina, los documentos divergen en forma. Se mitiga con validación en la capa de dominio/servicio y con esquemas de colección si hace falta.

## Referencias

- Spring Data MongoDB — https://docs.spring.io/spring-data/mongodb/reference/
- Docker Compose del proyecto: `backend/src/main/resources/docker-compose.yml`
