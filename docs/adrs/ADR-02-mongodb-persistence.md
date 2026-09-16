# ADR-02: MongoDB Persistence

**Date:** 2026-08-27. **Status:** Accepted and implemented. **Scope:** Backend. **Last reviewed:** 2026-09-16.

## Context

Notes are document-shaped aggregates. They combine free-form text, lifecycle metadata, tags, pin state, timestamps, and ownership data, with a small subtype distinction between private and shared notes. The initial system has no requirement for analytical joins or multi-aggregate transactional workflows. It does require a short local setup, a natural representation of evolving note metadata, and mature integration with the Spring ecosystem.

Choosing persistence also meant deciding where database-specific concerns should live. The domain should not become a collection of Spring Data annotations, and application services should not depend directly on `MongoRepository`. Persistence therefore needed to fit both the data shape and the ports-and-adapters direction established by [ADR-01](ADR-01-vertical-slice-modular-monolith.md).

## Considered alternatives

### A relational database

PostgreSQL or MySQL would provide a mature transactional model, explicit schemas, and powerful relational queries. Notes could be represented with text columns and normalized tag tables. In the current scope, however, that model would add migrations and relational structure without a corresponding need for cross-aggregate joins. A relational database remains a valid future option if reporting, relational integrity, or transaction boundaries become dominant requirements.

### An embedded relational database

H2 or SQLite would minimize local operations but would not represent the production storage model. Differences in query semantics, concurrency, and schema behavior would reduce the value of integration tests and encourage accidental dependence on capabilities that do not exist in production.

### MongoDB

MongoDB maps the aggregate to one document, supports subtype metadata, integrates directly with Spring Data, and can run consistently in local, test, and deployed environments. Its flexible schema accelerates model evolution, provided that flexibility is bounded by application-level validation and explicit mapping.

## Decision

MongoDB 7 is the backend's system of record. Accounts are stored in an `accounts` collection. Notes are stored in one polymorphic `notes` collection, with `PrivateNoteDocument` and `SharedNoteDocument` identified by Spring Data type aliases. BSON `ObjectId` values provide identity, and both account and note documents use `@Version` for optimistic locking.

Domain objects remain persistence-agnostic. Context-level infrastructure owns MongoDB documents, Spring Data repository interfaces, and mapping. Individual slices define repository ports for their use cases and provide MongoDB adapters that implement those ports. Mutation adapters reload existing documents and modify selected fields so subtype information and version state remain intact.

The schema is flexible at the database level but constrained in application code. Bean Validation protects transport contracts, value objects protect domain invariants, and `NoteMapper` controls reconstruction. Dates are currently stored as ISO-8601 strings. Tags and pin state are embedded in the note document. The only explicitly declared application index is the unique account email index; note indexes must be added from observed query requirements rather than assumed to exist.

Local Compose uses `mongo:7` and mounts `/data/db` into the named `mongo-data` volume. Container recreation therefore preserves data unless the volume is explicitly removed. The same Compose file starts the prebuilt `messafy/backend:latest` image after MongoDB passes its health check.

## Environment model

Host-based development reads `MONGO_URI`. Docker Compose maps `MONGO_URI_DOCKER` into the backend container as `MONGO_URI`, because the hostname visible inside the Compose network differs from the host configuration. The production container also receives `JWT_SECRET`. These values are supplied through the local `.env` file and must not be committed or exposed.

Tests do not require Docker. The default-active Maven profile adds Flapdoodle embedded MongoDB 7.0.14, activates the Spring `test` profile, and uses the dedicated `test-privatenotes` database. Most repository adapter tests still mock their Spring Data dependencies; embedded MongoDB currently supports the full-context smoke test rather than a comprehensive data-access integration suite.

## Consequences

The persistence representation follows the aggregate closely, and new optional note metadata can be introduced without immediate schema migrations. One production technology is used across local and deployed environments, while embedded MongoDB keeps the test command self-contained. Repository ports prevent application services from becoming coupled to Spring Data APIs.

The flexible schema places more responsibility on mapping and compatibility behavior. Missing fields in older documents must be normalized safely. Query performance will require explicit indexes as data volume grows. Multi-document atomicity is not part of the current model and would require deliberate transaction or consistency design if future use cases span aggregates. The named Docker volume protects ordinary local recreation, but `docker compose down -v` remains intentionally destructive.

## References

See the [Spring Data MongoDB reference](https://docs.spring.io/spring-data/mongodb/reference/), [`docker-compose.yml`](../../src/main/resources/docker-compose.yml), and [ADR-07](ADR-07-backend-testing-with-junit-and-mockito.md).
