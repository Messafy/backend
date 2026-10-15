Messafy Backend

Messafy keeps personal notes private and makes sharing intentional. This repository contains the API behind account registration, email verification and the note lifecycle, including private and shared notes, tags, pinning and trash. Access rules are enforced on the server, and deleted notes are retained rather than immediately removed from storage.

Built with Java 21, Spring Boot 4 and MongoDB, the backend is a modular monolith organized around accounts, notes and notifications. Onion architecture keeps business rules independent of HTTP, persistence and framework code. Spring Modulith and architecture tests enforce those boundaries, while Spring Security, BCrypt and JWT handle authentication.

For local development, configure MONGO_URI, JWT_SECRET, EMAIL_USERNAME and EMAIL_PASSWORD in a .env file at the backend root, then run ./mvnw -Pdev spring-boot:run. The API runs at http://localhost:8080 and allows the local frontend at http://localhost:5173. Keep credentials out of version control.

Run ./mvnw test to check domain behavior, application services, HTTP contracts, persistence and module boundaries. Tests use embedded MongoDB and a local test mail server without requiring Docker. Requirements, diagrams and architecture decisions live in docs/.
