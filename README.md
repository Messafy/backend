# Ghost Notes Backend

Ghost Notes is the backend service behind the note-taking experience currently branded as Messafy. It is not structured as a conventional Spring application split into global controller, service, and repository layers. Instead, it is a modular monolith organized around business capabilities. Account management and note management are separate bounded contexts, each use case is implemented as a vertical slice, and the domain model remains independent from HTTP, MongoDB, and Spring Security concerns.

The project is deliberately compact, but it treats architectural boundaries as first-class design decisions. It uses domain-driven design where the domain has meaningful behavior, ports and adapters where infrastructure needs to remain replaceable, and Spring Modulith as the vocabulary for module boundaries. The result is a codebase intended to be read from a use case inward rather than from a framework layer downward.

## What the system does

The service manages accounts and private or shared notes. Accounts can register, authenticate, and retrieve their current profile. Authenticated users can create notes, read notes they own or that have been shared with them, update their own notes, apply tags, pin important notes, mark notes as read, and move notes to a logical trash.

A note is not modeled as a passive persistence record. It is an aggregate with validated value objects, explicit ownership, a lifecycle, timestamps, tags, and pin state. Private notes have one owner. Shared notes have an owner and one recipient. Update, delete, and pin operations are owner-oriented, while read operations and mark-as-read admit the recipient of a shared note. Deletion is a domain transition to `DELETED`, not physical removal from MongoDB, which makes the trash view a query over retained domain state rather than a separate storage mechanism.

## Architectural shape

The top-level packages under `com.luispiquinrey.backend` define the main architectural boundaries. The `account` context owns identity, credentials, account state, roles, login, registration, JWT issuance, and the Spring Security integration. The `notes` context owns the note aggregate and all note-oriented use cases. The `share` package is a deliberately small shared kernel containing concepts such as authenticated identity and time, together with global infrastructure that genuinely belongs to neither business context.

The two bounded contexts do not depend directly on each other. A notes controller receives the authenticated account through the `AuthenticatedUser` abstraction from the shared kernel. Account infrastructure supplies the concrete `AccountUserDetails` implementation. This is a small but important boundary: the notes model can enforce ownership without importing account internals, and authentication can evolve without turning the notes context into a client of the account context.

Within a bounded context, code is grouped by business capability. A slice such as `notes/slices/pin` contains the HTTP controller, application service, repository port, MongoDB adapter, and transport objects required to pin a note. Shared persistence mechanics, MongoDB documents, Spring Data repositories, and domain-to-document mapping live in the context-level `infrastructure` package. The domain package contains aggregates, value objects, lifecycle rules, and domain exceptions, with no dependency on web or persistence APIs.

```text
src/main/java/com/luispiquinrey/backend
├── account
│   ├── domain
│   ├── infrastructure
│   └── slices
│       ├── register
│       ├── login
│       └── get
├── notes
│   ├── domain
│   ├── infrastructure
│   └── slices
│       ├── create
│       ├── get
│       ├── update
│       ├── delete
│       ├── markasread
│       └── pin
└── share
    ├── identity
    ├── time
    └── infrastructure
```

Spring Modulith discovers `account`, `notes`, and `share` from these direct application subpackages. The shared kernel is explicitly declared as an open application module. jMolecules annotations make DDD roles such as aggregate roots and value objects visible in code. The dependencies for automatic module verification are present, although an `ApplicationModules.verify()` test has not yet been added; today the boundary is an architectural convention backed by package design rather than a build-breaking rule.

The accepted decisions behind this structure are documented in the [architecture decision records](docs/adrs/README.md). The repository also contains C4 diagrams, a DDD context map, a class diagram, a sequence diagram, and the system requirements specification under [`docs/`](docs/).

## A request through the system

Consider `PATCH /v1/notes/{id}/pin`. Spring Security first authenticates the bearer token and builds the current principal. The controller translates the HTTP request into a small application call containing the note identifier, desired pin state, and authenticated account identifier. The application service loads the aggregate through its repository port, rejects missing notes, verifies ownership, prevents pinning a deleted note, and asks the aggregate to change its pin state. The MongoDB adapter reloads the existing polymorphic document, updates the relevant state, and saves it while preserving the document subtype and optimistic-lock version.

This flow illustrates the intended dependency direction. HTTP knows about the application service. The service knows about the domain and a repository interface. The MongoDB adapter knows how to satisfy that interface. The domain knows none of them. Validation that describes the transport contract stays close to request objects, while validation that protects business invariants stays in value objects and aggregate behavior.

## Domain model

`Note` is an abstract aggregate root specialized by `PrivateNote` and `SharedNote`. Its identity is a BSON-compatible `NoteId`; title, content, user identifiers, tags, and dates are represented by dedicated domain types rather than unvalidated strings. Aggregate reconstruction validates persisted temporal state, and collection-valued state is copied before being exposed so callers cannot mutate tags behind the aggregate's back.

The note lifecycle contains `NEW`, `READ`, `HIDDEN`, `REPORTED`, and `DELETED`. A new note can be read, hidden, reported, or deleted. A read note can be hidden, reported, or deleted. Hidden and reported notes have narrower transitions, and deletion is terminal. Repeating the current state is accepted, but timestamped operations such as mark-as-read still replace their timestamps and are therefore not idempotent. The HTTP API currently exposes reading and deletion; hiding and reporting already exist as domain behavior but do not yet have application slices.

Pinning is intentionally independent from lifecycle status. It is a presentation and prioritization concern attached to an active note, not another lifecycle state. Tags are immutable value objects at the API boundary and are replaced as a collection during updates. A null tag collection on update means “preserve the existing tags,” while an empty collection means “remove all tags.”

## Security model

Authentication is stateless with respect to server-side sessions. Registration hashes raw passwords with BCrypt. Login verifies the stored hash, records the successful login time, and issues an HS256 JWT through JJWT. The token carries the account identifier as its subject together with email, role, issue time, and a 24-hour expiration.

Authenticated requests send the token as `Authorization: Bearer <token>`. `JwtAuthenticationFilter` verifies the token and reloads the account through `UserDetailsService` before installing the Spring Security authentication. The application therefore stores no session, but it intentionally consults the current account record on authenticated requests. Authorities come from the reloaded account rather than trusting only the role embedded in the token. The filter currently installs authentication directly and does not enforce `UserDetails.isEnabled()`, so account status is not yet an effective token-revocation mechanism.

The production security chain disables CSRF, form login, and HTTP Basic because the API is bearer-token based. Authentication endpoints are public. Note endpoints and the current-account endpoint admit `USER` and `ADMIN`; administrative account lookup requires `ADMIN`. CORS currently allows the local Vite origin at `http://localhost:5173`.

Route authorization is only the outer boundary. Resource authorization is enforced inside note application services and read filters using the authenticated account identifier. Update, delete, and pin operations require ownership. Reading a shared note and marking it as read admit its recipient. Missing and inaccessible note details both result in a not-found response, avoiding unnecessary disclosure about resource existence.

There are no refresh tokens, token revocation list, or backend logout endpoint. The base configuration contains a development fallback for the signing secret, but deployed environments must provide `JWT_SECRET`. Production secrets must never be committed, printed, or copied into an image as source-controlled resources.

## Persistence model

MongoDB 7 is the system of record. Accounts are stored in an `accounts` collection with a unique email index and optimistic locking through `@Version`. Notes use one polymorphic `notes` collection. Spring Data type aliases distinguish private and shared note documents while retaining common fields for lifecycle, timestamps, tags, pin state, and versioning.

The application generates BSON `ObjectId` values before persistence so identity exists as soon as an aggregate is created. Dates are currently serialized as ISO-8601 strings inside documents. Persistence adapters reconstruct full domain objects through `NoteMapper`, which means old documents with absent optional fields can be normalized at the mapping boundary. Mutation adapters update existing documents rather than replacing subtype-specific structure blindly.

MongoDB's document model fits the aggregate shape and allows tags and note metadata to evolve without a relational join model. That flexibility is constrained by Java document classes, Bean Validation at transport boundaries, domain value objects, and optimistic locking. The code currently declares an explicit index only for account email; indexes for note ownership, recipient, status, or tags should be introduced from measured query needs rather than claimed implicitly.

## HTTP contract

All endpoints are rooted at `/v1`. Authentication routes are public; all other routes require a valid bearer token in every non-test profile.

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/v1/auth/register` | Register an account with an email and raw password |
| `POST` | `/v1/auth/login` | Authenticate credentials and issue a JWT |
| `GET` | `/v1/accounts/me` | Retrieve the authenticated account snapshot |
| `GET` | `/v1/accounts/{id}` | Administrative account lookup |
| `POST` | `/v1/notes` | Create a private or shared note |
| `GET` | `/v1/notes/{id}` | Retrieve an accessible note |
| `GET` | `/v1/notes?ownerId=...` | List visible active notes for the current account |
| `GET` | `/v1/notes?sharedWith=...` | List active notes shared with the current account |
| `GET` | `/v1/notes?status=...` | List accessible notes by lifecycle status |
| `GET` | `/v1/notes/trash` | List deleted notes owned by the current account |
| `PATCH` | `/v1/notes/{id}` | Replace editable note content and optionally replace tags |
| `PATCH` | `/v1/notes/{id}/pin` | Pin or unpin an owned note |
| `PATCH` | `/v1/notes/{id}/read` | Mark a note as read |
| `DELETE` | `/v1/notes` | Soft-delete an owned note using a JSON body containing its ID |

Creating a note derives ownership from the authenticated principal. Clients cannot create a note on behalf of another account by supplying an owner identifier. A representative private-note request is:

```http
POST /v1/notes
Authorization: Bearer <token>
Content-Type: application/json

{
  "type": "PRIVATE",
  "title": "Architecture notes",
  "content": "A modular monolith keeps deployment simple without giving up boundaries.",
  "sharedWith": "507f1f77bcf86cd799439011",
  "tags": ["architecture", "spring"]
}
```

`sharedWith` is optional at the transport layer: a shared note requires it and rejects a missing or inactive recipient through the account lookup contract, while a private note ignores it. The owner query parameters are similarly validated for compatibility with the HTTP contract but replaced by the authenticated account identifier before execution. These are current contract characteristics, not security mechanisms.

Validation failures produce a structured `400` response with field-level messages. Domain validation, missing resources, lifecycle conflicts, and access denial are translated centrally through controller advice into `400`, `404`, `409`, and `403` responses. Credential failures deliberately collapse account absence and password mismatch into the same `401 invalid credentials` result.

## Configuration profiles

The base configuration imports `optional:classpath:.env[.properties]`, serves on port `8080`, names the MongoDB database `privatenotes`, and enables automatic index creation outside tests. Profile-specific files refine that configuration.

The `dev` profile expects `MONGO_URI` and enables verbose application, MongoDB, and web logging. The `prod` profile also expects `MONGO_URI`, disables Spring's Docker Compose integration, and uses quieter logging defaults. The `test` profile targets `test-privatenotes`, disables automatic index creation, and replaces production security with test-oriented configuration.

For host-based development, place the required values in the local `src/main/resources/.env` file. This file contains secrets, is ignored by Git, and must never be committed or quoted in logs or documentation. At minimum, local and deployed environments use MongoDB connection information and a JWT signing secret. Docker Compose additionally expects `MONGO_INITDB_ROOT_PASSWORD` and `MONGO_URI_DOCKER` because the URI used from inside the Compose network differs from the host URI.

## Running locally

The project requires Java 21. The Maven wrapper is committed, so a system Maven installation is unnecessary. From the `backend` directory, run the application with the development profile:

```bash
./mvnw -Pdev spring-boot:run
```

The API starts at `http://localhost:8080`. The Vite frontend is expected at `http://localhost:5173`, which is the origin currently allowed by CORS.

The test suite requires no Docker daemon. The default-active Maven profile named `integration-tests` adds Flapdoodle embedded MongoDB 7.0.14 and activates the Spring `test` profile:

```bash
./mvnw test
```

A single test class or a JUnit tag can be selected without changing project configuration:

```bash
./mvnw test -Dtest=CreateNoteServiceTest
./mvnw test -Dgroups=createNoteService
```

Most tests are plain JUnit 5 and Mockito tests that instantiate services and adapters without a Spring application context. Controller tests commonly use standalone MockMvc. `BackendApplicationTests` provides a thin full-context smoke test backed by embedded MongoDB. This unit-heavy hybrid keeps the feedback loop fast while still detecting fundamental wiring failures. Dedicated module-boundary, security-filter, and real repository integration tests remain areas for further hardening.

## Containers and deployment image

Jib builds the runtime image directly from Maven configuration without requiring a Dockerfile. The image uses `eclipse-temurin:21-jre` and is published locally as `messafy/backend`:

```bash
./mvnw jib:dockerBuild -DskipTests
```

The Compose file runs MongoDB 7 and the prebuilt backend image. MongoDB stores `/data/db` in the named `mongo-data` volume, so ordinary container recreation preserves notes and accounts. Removing the Compose volume intentionally destroys that persisted database.

```bash
docker compose \
  --env-file src/main/resources/.env \
  -f src/main/resources/docker-compose.yml \
  up -d --force-recreate backend
```

Compose does not build the Java service. After changing backend code, rebuild `messafy/backend:latest` with `jib:dockerBuild` before recreating the backend container. Running only the Compose command after a source change starts the previous image and can make newly implemented routes appear to be missing.

## Extending the backend

New behavior should begin with the business capability, not with a framework layer. If the capability is independent, create a focused slice inside the owning bounded context. Keep request validation in transport objects, orchestration and authorization in the application service, invariants in the domain, and database mechanics in an adapter behind a repository port. If behavior genuinely belongs to an existing use case, evolve that slice rather than creating fragmentation for its own sake.

Cross-context dependencies should pass through a deliberately shared abstraction or an explicit integration mechanism. A notes use case should not import account repositories or account domain objects merely because both run in one process. Likewise, convenience is not sufficient reason to move business concepts into the shared kernel; shared code should remain small, stable, and semantically neutral between contexts.

The same principle applies to observability. Business-relevant events are logged explicitly where their meaning is known. Generic request correlation, timing, and structured logging belong in shared HTTP infrastructure. Domain objects contain no logger and do not acquire technical dependencies for diagnostic convenience.

## Current engineering boundaries

The repository documents accepted decisions and known deviations rather than presenting an idealized architecture. Spring Modulith boundary verification is not yet wired into the test suite. The HTTP logging interceptor and correlation IDs described by ADR-08 remain planned. Resource authorization for mark-as-read still needs to be aligned with the other note mutations. The administrative account lookup has an awkward GET-body contract, and the note creation request currently requires a recipient identifier even for private notes.

These constraints are visible because the project treats architecture as an evolving set of explicit trade-offs. The ADRs distinguish what has been decided from what has been implemented, and the tests favor precise local behavior over the appearance of exhaustive integration coverage. That makes future work concrete: close a documented gap, add executable evidence, and update the decision record when the architectural trade-off itself changes.

## Further documentation

The [ADR index](docs/adrs/README.md) explains the reasoning behind the modular monolith, MongoDB, stateless authentication, frontend architecture, design system, accessibility baseline, testing strategy, and logging approach. [`Ghost_Notes_SRS.md`](docs/Ghost_Notes_SRS.md) records the broader requirements. The PDF diagrams under [`docs/`](docs/) provide C4 system, container, and component views together with the DDD context map, class model, and authentication sequence.

This README describes the current implementation rather than a hypothetical target state. When code and documentation disagree, that mismatch is considered an engineering defect: either the implementation must be brought back to the accepted decision, or the decision must be revised explicitly.
