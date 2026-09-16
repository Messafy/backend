# ADR-07: Backend Testing with JUnit and Mockito

**Date:** 2026-08-27. **Status:** Accepted as a unit-heavy hybrid strategy. **Scope:** Backend. **Last reviewed:** 2026-09-16.

## Context

The vertical-slice architecture described by [ADR-01](ADR-01-vertical-slice-modular-monolith.md) gives application services explicit constructor dependencies and keeps domain behavior independent from Spring. This makes fast, isolated tests practical. At the same time, a Spring application can compile while failing at runtime because of missing beans, invalid mappings, or configuration errors. The testing strategy must balance local diagnostic precision with evidence that the assembled application can start.

The development loop should not depend on a separately running MongoDB container. Tests should remain deterministic and quick enough to run routinely, while integration infrastructure should be introduced where it validates behavior that mocks cannot represent.

## Considered alternatives

### Full-context and container-backed tests by default

Running every use case through `@SpringBootTest`, Testcontainers, and real HTTP or MongoDB boundaries would provide strong assembly confidence. It would also make failures slower and less local, and it would spend most test time rebuilding framework state for domain behavior that can be verified directly.

### Pure unit tests only

JUnit and Mockito tests without Spring are fast and make dependencies explicit. Used alone, they cannot detect application wiring failures, profile problems, serialization differences, or incorrect framework annotations.

### A unit-heavy hybrid

Most domain and slice behavior can be tested directly, while a thin integration layer can verify that the application context and selected infrastructure boundaries work together. Embedded MongoDB can make that layer self-contained without requiring Docker.

## Decision

The primary testing style is plain JUnit 5 with Mockito where a dependency boundary makes mocking appropriate. Test packages mirror production packages, and each slice tests its application service, controller behavior, and adapters at the narrowest useful level. Controller tests generally use standalone MockMvc rather than a full web application context. Repository adapter tests generally mock Spring Data repositories and verify translation and orchestration rather than MongoDB itself.

The suite also includes `BackendApplicationTests`, a real `@SpringBootTest` smoke test. The default-active Maven `integration-tests` profile supplies Flapdoodle embedded MongoDB 7.0.14 and activates the Spring `test` profile with the `test-privatenotes` database. Running the normal suite requires no Docker daemon.

Test methods generally use a behavioral `@Tag` so focused groups can be executed through Maven. Most slice tests use `@Timeout(1)`, while standalone controller tests may use a slightly larger timeout. These are conventions, not universal invariants: value-object tests and the full-context smoke test contain justified exceptions.

The standard verification command is `./mvnw test`. A class can be selected with `./mvnw test -Dtest=CreateNoteServiceTest`, and tagged behavior can be selected with `./mvnw test -Dgroups=createNoteService`. Surefire configures Mockito's Java agent explicitly for the current JDK.

## Current implementation status

The suite is predominantly isolated and fast, with one full-context smoke test. It does not currently include `@DataMongoTest` slices, Testcontainers, end-to-end JWT filter coverage, exception-handler coverage, or an executable Spring Modulith boundary test. The pin slice does not yet have dedicated tests, and not every adapter has a one-to-one test class.

Embedded MongoDB is available but should not be confused with comprehensive repository integration coverage. Its current purpose is to support application startup under the test profile. New integration tests should be added where query derivation, document mapping, security configuration, or framework wiring is the behavior under test.

## Consequences

Most failures point directly to one business rule or orchestration step, and developers can run the complete command without external infrastructure. Constructor injection and repository ports remain easy to test because the architecture was designed around explicit dependencies. The context smoke test catches a class of assembly problems that pure unit testing cannot.

Mocks can still drift from Spring Data query semantics or framework behavior. A passing adapter unit test does not prove that a derived Mongo query matches persisted documents. The strategy therefore expects integration coverage to grow selectively around high-risk boundaries rather than expanding every test into a full-stack scenario. Module verification and focused persistence/security integration are the highest-value next additions.

## References

See [JUnit 5](https://junit.org/junit5/), [Mockito](https://site.mockito.org/), [ADR-01](ADR-01-vertical-slice-modular-monolith.md), and [ADR-02](ADR-02-mongodb-persistence.md).
