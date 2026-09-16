# ADR-08: Cross-Cutting Logging without General-Purpose AOP

**Date:** 2026-09-08. **Status:** Accepted and partially implemented. **Scope:** Backend. **Last reviewed:** 2026-09-16.

## Context

Logging crosses HTTP, application, and persistence boundaries, but its meaning changes at each one. A generic aspect can record every service invocation with little code, yet broad pointcuts tend to produce uniform noise, hide runtime behavior from readers, and risk serializing credentials or domain content into logs. The backend needs enough operational context to diagnose requests without coupling the domain to infrastructure or treating every method call as equally valuable.

The modular architecture also affects the solution. Generic request concerns belong in the shared infrastructure module, while business-significant events are best recorded where their meaning is understood. Domain objects should not gain loggers simply because they are central to execution.

## Considered alternatives

### General-purpose AOP around services

An aspect around every service method would provide immediate breadth and little repeated code. It would also make log behavior dependent on package-sensitive pointcuts, obscure what data is emitted, and encourage logging arguments that may contain passwords, tokens, note bodies, or personal information.

### Explicit logging everywhere

Direct SLF4J calls are visible, searchable, and contextual. Used alone, they duplicate generic request data and do not automatically supply correlation identifiers, response status, or elapsed time across the complete HTTP lifecycle.

### Shared HTTP instrumentation plus contextual explicit logging

A shared filter or interceptor can own transport-level facts, while controllers, application services, persistence adapters, and exception handlers can log only events meaningful at their layer. AOP remains available for narrow opt-in concerns represented by explicit annotations rather than package-wide interception.

## Decision

Business and infrastructure logging uses SLF4J explicitly. Messages should identify the operation and safe identifiers needed to investigate it, while never recording raw passwords, encoded passwords, JWT values, authorization headers, or authentication payloads. Domain packages contain no logging dependency.

Generic HTTP instrumentation belongs under `share/infrastructure`. Its intended responsibility is to record method, normalized route, result status, and duration and to propagate an `X-Correlation-Id` through SLF4J MDC. That component should avoid request and response bodies by default. Correlation data can later connect application logs to metrics or distributed traces without changing domain code.

AOP is reserved for a demonstrated cross-cutting requirement that benefits from an explicit opt-in annotation such as audited or timed behavior. Broad package pointcuts are not part of the logging strategy. If module events are introduced, listeners may record them without creating direct dependencies between slices, but event-based logging is not currently implemented.

Email addresses are operationally useful during account flows but are also personal data. Existing logs record them in several places. Production hardening should establish a masking or retention policy rather than assuming that an email is harmless merely because it is not a credential.

## Current implementation status

Explicit SLF4J logging is present across controllers, application services, MongoDB adapters, exception handlers, `NoteMapper`, `AccountUserDetailsService`, and `JwtAuthenticationFilter`. Domain packages remain free of logging. Passwords and token values are not intentionally emitted.

The shared HTTP filter or interceptor has not been implemented. There is no `X-Correlation-Id` handling, MDC population, generic request duration measurement, structured JSON output, or custom Logback configuration. Development, test, and production profiles currently control logging through package levels in YAML. AOP and Spring Modulith event listeners are also absent, consistent with deferring them until a concrete requirement exists.

## Consequences

Current logs are explicit and can carry business context unavailable to a generic aspect. Reviewers can see what is emitted and can reject sensitive values. Keeping logging out of the domain preserves deterministic model behavior and framework independence.

Until the shared HTTP instrumentation is built, logs cannot reliably correlate all messages from one request or report uniform latency and status data. Explicit logging also requires discipline in each new slice and can produce inconsistent wording or levels. The accepted design handles that through review now and leaves room for structured conventions when operational scale justifies them.

## References

See the [SLF4J manual](https://www.slf4j.org/manual.html#mdc), the [Spring Modulith reference](https://docs.spring.io/spring-modulith/reference/), and [ADR-01](ADR-01-vertical-slice-modular-monolith.md).
