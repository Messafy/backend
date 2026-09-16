# Architecture Decision Records

This directory records the architectural decisions that shape Ghost Notes, the repository codename for the note-taking product currently presented as Messafy in the user interface and container image names. The records cover both backend decisions and project-wide frontend decisions because those choices define one system and frequently constrain each other.

An ADR explains the context that made a decision necessary, the credible alternatives considered at that time, the selected direction, and the consequences the project accepts. Accepted ADRs are historical records: if the decision itself changes, a new ADR supersedes the old one rather than silently rewriting history. Editorial translation, corrected references, and explicit implementation-status updates may be applied to an existing ADR when they do not alter the accepted decision. Each document separates the decision from current implementation so that incomplete enforcement is visible rather than mistaken for a change in architecture.

Files follow the form `ADR-XX-short-english-title.md`. New records should include a date, status, scope, context, considered alternatives, decision, current implementation status where relevant, consequences, and references. Status describes whether the decision is accepted or superseded; implementation language explains how fully the repository currently realizes it.

## Decision index

| ADR | Decision | Status |
| --- | --- | --- |
| [ADR-01](ADR-01-vertical-slice-modular-monolith.md) | Vertical-Slice Modular Monolith with Spring Modulith | Accepted; boundary verification remains pending |
| [ADR-02](ADR-02-mongodb-persistence.md) | MongoDB Persistence | Accepted and implemented |
| [ADR-03](ADR-03-stateless-jwt-authentication.md) | Stateless JWT Authentication with JJWT | Accepted and implemented with limitations |
| [ADR-04](ADR-04-react-vite-spa-and-deferred-seo.md) | React and Vite SPA with Deferred SEO | Accepted; SEO work is partial |
| [ADR-05](ADR-05-atomic-design-and-scss-tokens.md) | Atomic Design and SCSS Design Tokens | Accepted with known convention gaps |
| [ADR-06](ADR-06-semantic-html-and-accessibility.md) | Semantic HTML and Accessibility | Accepted and partially implemented |
| [ADR-07](ADR-07-backend-testing-with-junit-and-mockito.md) | Backend Testing with JUnit and Mockito | Accepted as a unit-heavy hybrid strategy |
| [ADR-08](ADR-08-cross-cutting-logging.md) | Cross-Cutting Logging without General-Purpose AOP | Accepted and partially implemented |

The ADRs should be read together with the project [README](../../README.md), the [software requirements specification](../Ghost_Notes_SRS.md), and the C4 and DDD diagrams in the parent documentation directory. Diagrams describe structure at a point in time; ADRs explain why that structure exists and what trade-offs it carries.
