# Active Account Lookup for Shared Notes

## Context

Creating a shared note currently accepts any syntactically valid MongoDB identifier as `sharedWith`. The notes context persists that identifier without establishing that the recipient exists or can receive a shared note. This permits references to missing or deleted accounts and leaves a cross-context invariant unenforced.

Recipient eligibility is a synchronous creation precondition. The note must not be persisted unless the target account exists and has `ACTIVE` status. The notes context owns the decision to reject creation, while the account context owns account existence and status data.

## Decision

The account module will publish a minimal synchronous API named `AccountLookup`. Its contract will expose `boolean existsActiveAccount(String accountId)`. The method returns `true` only when an account with that identifier exists and `Account.isActive()` is true. Missing and inactive accounts both return `false`; consumers do not receive the account aggregate or learn why the recipient is unavailable.

The interface will live in `com.luispiquinrey.backend.account.api`, declared as the Spring Modulith named interface `api` in that package's `package-info.java`. `GetAccountService` will implement the interface by reusing `GetAccountRepository`, avoiding a second account lookup service with identical persistence dependencies.

The notes module will declare that it may depend on `share` and `account :: api`. `CreateNoteService` will receive `AccountLookup` by constructor injection. It will call the contract only for `SHARED` creation. Private-note creation will remain independent from account lookup because the authenticated owner has already been established by Spring Security.

## Creation flow

For a private note, `CreateNoteService` will continue validating request data, creating the aggregate through `NoteFactory`, and persisting it without recipient lookup.

For a shared note, the service will validate the request and ask `AccountLookup` whether `sharedWith` identifies an active account. A `false` result will raise `NoteValidationException` with one generic recipient-unavailable message. The existing note exception handler will translate that failure to HTTP `400`. The service will not distinguish missing from deleted accounts in its external behavior.

When the lookup returns `true`, the service will create and persist the shared-note aggregate exactly once. The lookup and note write occur synchronously in one process but not in one cross-aggregate transaction. An account could theoretically be deleted between validation and persistence; that race is accepted for the current modular monolith because account deletion and referential cleanup do not yet define a stronger consistency model.

## Module boundaries

`notes` must not import `GetAccountService`, `GetAccountRepository`, `Account`, or account persistence classes. Its only compile-time account dependency will be the named `account :: api` interface. The contract remains owned by the provider module because it describes account availability, while the notes service owns the use-case-specific rejection.

The shared kernel will not contain this interface. Account availability is not a neutral concept shared equally by both contexts, and moving it into `share` would weaken ownership merely to hide an intentional module dependency.

Spring Modulith verification will be added with `ApplicationModules.of(BackendApplication.class).verify()`. This will make the named-interface rule executable and reject accidental access from notes to account internals.

## Error semantics

An invalid identifier format remains a transport validation error. A well-formed identifier that does not resolve to an active account becomes a note validation error. Both missing and deleted recipients produce the same response so the API does not expose account lifecycle details through note creation.

Repository or infrastructure failures are not converted into “recipient unavailable.” They propagate through the existing error behavior because an unavailable database is different from a negative business lookup and must not silently become a client error.

## Testing

Account lookup tests will prove that an active account returns `true`, a deleted account returns `false`, and a missing account returns `false`. Create-note service tests will prove that shared-note creation invokes the lookup, persists only for an active recipient, and rejects unavailable recipients without calling the note repository. A private-note test will prove that recipient lookup is not invoked.

The Spring Modulith verification test will prove that the declared `notes -> account :: api` dependency is valid and that no dependency on account internals has been introduced. Existing note creation and account retrieval tests will be updated only where constructor signatures or implemented interfaces require it.

## Out of scope

This change does not add recipient search, email-based sharing, notifications, shared-note acceptance, restore behavior, account-deletion cleanup, or database-level referential constraints. It does not change the current requirement that `sharedWith` is present in the transport request even for private notes. Those concerns require separate product and consistency decisions.
