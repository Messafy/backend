# ADR-03: Autenticación stateless con JWT (librería JJWT)

- **Fecha:** 2026-09-03 (registro en ADR: 2026-09-08)
- **Estado:** Aceptada
- **Ámbito:** Backend + Frontend

## Contexto

La aplicación exige autenticación: cada nota pertenece a una cuenta y solo su propietario puede acceder a ella. Había que decidir el mecanismo de sesión entre el frontend SPA (React) y el backend (Spring Boot), así que dónde vive el estado de la sesión.

Requisitos:

- El frontend es una SPA que consume una API REST (`/v1/auth/login`, `/v1/notes`, etc.).
- Las peticiones llegan con `Authorization: Bearer <token>` (implementado en `LoginPage.jsx` y `NoteList.jsx`).
- Se desea una API preparada para consumo desde clientes distintos (móvil a futuro) sin acoplar la sesión a cookies de un dominio concreto.
- Simplicidad operativa: sin almacenamiento de sesiones en servidor en la fase actual.

## Alternativas consideradas

### Alternativa A: Sesiones de servidor con cookies (Spring Session / HttpSession)

- **A favor:** revocación trivial, protección CSRF integrada, el token nunca toca el JS del cliente.
- **En contra:** estado en servidor (afecta escalado horizontal), acopla la API al navegador, y complica futuros clientes no-navegador. Para una API-first SPA añade fricción de configuración CSRF/CORS.

### Alternativa B: OAuth2 / OpenID Connect con proveedor externo (Auth0, Keycloak)

- **A favor:** estándar completo, flujos probados, gestión de usuarios delegada.
- **En contra:** dependencia externa o servicio adicional (Keycloak) para un producto en fase temprana; sobrecarga conceptual para el alcance actual.

### Alternativa C: JWT stateless con JJWT (elegida)

El backend emite un JWT firmado en el login; el cliente lo guarda y lo envía en cada petición; el backend valida firma y expiración en un filtro/validador sin tocar base de datos.

## Decisión

1. **Emisión:** `POST /v1/auth/login` valida credenciales (email + password hasheada) y devuelve un token JWT firmado con JJWT.
2. **Validación:** cada petición autenticada incluye `Authorization: Bearer <token>`; el backend verifica firma y expiración y resuelve el contexto de autenticación.
3. **Sesión en el frontend:** el token y una instantánea de la cuenta se persisten en el cliente (`src/context/authSession.js` en `localStorage`), expuestos mediante React Context (`AuthContext`).
4. **Guardas de ruta:** el router de la SPA (`App.jsx`) protege `/notes` con `requireAuth` y `/login` con `requireGuest`, redirigiendo según exista token en la sesión.
5. La librería elegida para firmar/validar es **JJWT** (`io.jsonwebtoken`), por su API explícita y su mantenimiento activo.

## Consecuencias

### Positivas

- Backend stateless: ninguna dependencia de afinidad de sesión; escalar es trivial.
- La API queda usable por cualquier cliente (web, móvil, scripts) con un solo header estándar.
- Implementación pequeña y controlada: nada de infraestructura de identidad adicional en esta fase.
- El par guardas-de-ruta + AuthContext en el frontend da una UX de sesión clara con poco código.

### Negativas / riesgos

- **Revocación:** un JWT emitido es válido hasta expirar; no hay logout real en el backend en la fase actual. Mitigación futura: expiraciones cortas + refresh tokens, o una lista de revocación (blacklist) en MongoDB si el requisito aparece.
- **Almacenamiento del token en el cliente:** `localStorage` mantiene la sesión entre reinicios del navegador, pero también prolonga la exposición ante XSS. Mitigación: disciplina estricta de saneamiento en React (que ya escapa por defecto), CSP, expiraciones cortas y reevaluar cookies `HttpOnly` antes de producción.
- **Rotación de claves de firma** debe planificarse antes de producción (clave en variable de entorno, no en código).
- Los claims del token deben mantenerse mínimos (id de cuenta, rol); no meter datos volátiles del perfil.

## Referencias

- JJWT — https://github.com/jwtk/jjwt
- Slice de autenticación: `backend/src/main/java/com/luispiquinrey/backend/account/`
- Sesión y guardas en frontend: `frontend/src/context/authSession.js`, `frontend/src/app/App.jsx`
