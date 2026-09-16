# ADR-01: Vertical-Slice Modular Monolith with Spring Modulith

**Date:** 2026-08-27. **Status:** Accepted. **Scope:** Backend. **Last reviewed:** 2026-09-16.

## Context

Ghost Notes began as a focused application with two clear business areas: accounts and notes. The backend needed a structure that could preserve those conceptual boundaries without paying the operational cost of distributed services. A traditional Spring layout based on global `controllers`, `services`, `repositories`, and `entities` packages would have made the framework layer immediately recognizable, but it would also have scattered every business change across unrelated directories and encouraged dependencies between otherwise independent capabilities.

The project wanted to use domain-driven design as a way of expressing ownership and invariants, not as a reason to introduce a heavyweight framework. It also needed a structure that a small team could maintain and explain. The central architectural question was therefore how to obtain meaningful module boundaries, local reasoning, and a credible path to future extraction while retaining one process, one deployment unit, and one operational model.

## Considered alternatives

### Horizontal technical layers

A conventional layered design would group all controllers, services, repositories, and persistence entities by technical role. This approach has low initial learning cost and follows common Spring examples. Its weakness is locality: implementing a single use case requires navigating several broad packages, and dependencies tend to flow through shared services until the original business boundaries become difficult to recover.

### Microservices from the beginning

Deploying accounts and notes independently would provide strong runtime isolation and independent release cycles. It would also introduce network contracts, distributed configuration, service discovery, failure handling, observability, and deployment coordination before the product has demonstrated a need for them. That cost would optimize for organizational and scaling pressures that do not currently exist.

### A modular monolith organized by vertical slices

A modular monolith keeps deployment simple while making the source model reflect the business model. Top-level packages represent bounded contexts. Within each context, domain and shared infrastructure remain visible, while application behavior is grouped into focused feature slices. Spring Modulith supplies the module vocabulary, and jMolecules makes DDD roles explicit without controlling runtime behavior.

## Decision

The backend is a modular monolith. `account` and `notes` are bounded contexts discovered by Spring Modulith as direct subpackages of the application package. `share` is a deliberately small shared kernel and is explicitly declared with Spring Modulith's `@ApplicationModule(type = OPEN)`. jMolecules annotations such as `@AggregateRoot`, `@ValueObject`, and `@Repository` describe domain roles independently from that module declaration.

Each context contains a domain package, context-level infrastructure, and a `slices` package. An independent use case receives its own slice when doing so creates a coherent unit that can be understood and tested locally. Existing slices may evolve when new behavior belongs to the same use case; tags, for example, are part of note creation and update rather than artificial standalone features. This replaces the overly rigid rule that every change must create a new package.

A slice may contain its controller, application service, repository port, MongoDB adapter, and transport objects. Persistence components reused by several note slices, including Spring Data repositories, MongoDB documents, and `NoteMapper`, live in context-level infrastructure. Domain code does not depend on those adapters.

Bounded contexts must not import one another's internals. Cross-context needs should use a stable abstraction in the shared kernel or an explicit integration mechanism. The notes context receives the current account through `AuthenticatedUser`, which is defined in `share` and implemented by account infrastructure. This preserves the direction of dependency without duplicating identity semantics.

## Current implementation status

The package structure, shared-kernel declaration, and absence of direct account-to-notes imports implement the decision. The current note slices are `create`, `get`, `update`, `delete`, `markasread`, and `pin`; account slices cover registration, login, and account retrieval.

Spring Modulith dependencies are installed, but module verification is not yet executable architecture. There is currently no test calling `ApplicationModules.verify()`, so an illegal module dependency would not automatically fail the build. Adding that test is the remaining step required to turn the package policy into mechanically enforced boundaries.

## Consequences

Changes are usually local to a capability, and the dependency direction is visible from package placement. The application keeps the operational simplicity of a monolith while retaining boundaries that could support a later extraction if a context develops independent scaling or ownership needs. Domain tests can instantiate behavior without a Spring context because infrastructure is kept outside the model.

The design accepts some deliberate duplication between slices. Similar request objects or repository ports are not automatically centralized because sharing a type creates a coupling that may be more expensive than repeating a small amount of code. The structure also requires discipline: without module verification, package boundaries remain enforceable only through review and developer intent. Excessive fragmentation is another risk, which is why slice boundaries are based on cohesive use cases rather than on a mechanical one-class-per-feature rule.

## References

See the [Spring Modulith reference](https://docs.spring.io/spring-modulith/reference/), [jMolecules](https://jmolecules.org/), the C4 diagrams under [`docs/`](../), and the [DDD context map](../DDD_Context_Map.pdf).
