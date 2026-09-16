# ADR-03: Stateless JWT Authentication with JJWT

**Date:** 2026-09-03. **Status:** Accepted and implemented with documented limitations. **Scope:** Backend and frontend. **Last reviewed:** 2026-09-16.

## Context

The application is a browser SPA backed by a separate HTTP API. Every note belongs to an account, and the backend must establish both route-level authentication and resource-level authorization. The session mechanism needed to work for the React client without tying the API exclusively to browser cookies, and it needed to remain simple enough for a small deployment.

The system does not currently need delegated identity, social login, server-side session administration, or a dedicated identity provider. It does need password hashing, a short authentication flow, and credentials that can be sent by future non-browser clients through a standard HTTP header.

## Considered alternatives

### Server-side sessions and cookies

Spring Session or `HttpSession` would make revocation direct and could keep credentials in `HttpOnly` cookies. It would also introduce server-side session state and CSRF-oriented browser configuration. This remains a strong alternative if revocation and browser-focused security become more important than client independence.

### OAuth 2.0 and OpenID Connect

An external provider or a self-hosted identity platform would supply mature standards, federation, and lifecycle management. It would also add infrastructure and conceptual scope beyond the current product. The decision can be revisited if third-party identity, enterprise SSO, or delegated authorization becomes a requirement.

### Signed JWT bearer tokens

A signed token allows the API to avoid server-side session storage and gives browser, mobile, and script clients the same authentication contract. JJWT offers explicit signing and parsing APIs without introducing a full identity platform.

## Decision

Registration accepts an email and raw password. The password is encoded with BCrypt and only the encoded value is persisted. Login normalizes the email, loads the account, verifies the raw password against the stored hash, records the login time, and returns a signed HS256 JWT.

The token contains the account identifier as `sub`, plus email, role, issue time, and a hardcoded 24-hour expiration. Authenticated requests send `Authorization: Bearer <token>`. `JwtAuthenticationFilter` parses the token, extracts the email, reloads the account through `UserDetailsService`, validates signature, expiration, and email consistency, and installs Spring Security authentication. The backend is stateless with respect to sessions, but token validation intentionally performs an account lookup so the current account record and role are used. The filter installs authentication directly without checking `UserDetails.isEnabled()`, so current account status is loaded but not enforced.

Production security disables CSRF, form login, and HTTP Basic. `/v1/auth/**` is public. Note operations and `GET /v1/accounts/me` admit `USER` and `ADMIN`; other account routes require `ADMIN`. Resource ownership is enforced inside note use cases rather than inferred solely from route authorization.

The React client stores `{ token, account }` under the `session` localStorage key through `authSession.js` and exposes it through `AuthContext`. Login is followed by `GET /v1/accounts/me` to build the account snapshot. Route guards redirect based on token presence only; they do not parse expiration or replace backend authorization.

## Current limitations

There are no refresh tokens, revocation records, or backend logout endpoint. A token remains usable until expiration unless account lookup fails or token validation rejects it; changing account status alone does not currently invalidate it. Local storage preserves the session across browser restarts but increases exposure if script execution is compromised, so content security policy and careful XSS prevention remain important.

The base configuration contains a development fallback signing secret. Production and Compose environments must override it with `JWT_SECRET`; the fallback must not be treated as production-safe. Key rotation is not implemented.

Resource authorization is not yet perfectly uniform. Owners control update, delete, and pin operations, and recipients can read shared notes. The mark-as-read slice does not currently receive the authenticated principal and must be brought under the same ownership or recipient checks. Client-side route guards are user-experience controls, not a confidentiality boundary.

## Consequences

The API has one authentication mechanism across client types and requires no session affinity when scaled horizontally. Authentication code remains small and owned by the account context. Reloading account data on each authenticated request means role changes can take effect without waiting for token expiry, but it also means JWT authentication is not database-free and adds a MongoDB lookup to requests.

The design accepts the operational simplicity of bearer tokens together with their revocation and client-storage trade-offs. If the product moves toward a public high-risk deployment, the decision should be reevaluated against short-lived access tokens, refresh-token rotation, `HttpOnly` cookies, key rotation, and external identity providers.

## References

See [JJWT](https://github.com/jwtk/jjwt), the account infrastructure under `src/main/java/com/luispiquinrey/backend/account/infrastructure`, and the frontend session implementation under `frontend/src/context/`.
