# Customer Management

A full-stack customer management application built for the Senior Full Stack Technical Test. It uses Java 21, Spring Boot 3.5, H2, React 19, TypeScript, and Vite. See [`SPEC.md`](SPEC.md) for the detailed functional and API contract, and [`AI_USAGE.md`](AI_USAGE.md) for the AI-use and engineering log.

## Prerequisites

- JDK 21.
- Node.js 24.21.0 was used to verify the frontend.

## Run Locally

Start the backend from the repository root. On Windows PowerShell:

```powershell
.\gradlew.bat bootRun
```

In a second terminal, install frontend dependencies and start Vite:

```powershell
cd frontend
npm ci
npm run dev
```

Open the local URL printed by Vite. Its `/api` development proxy forwards requests to Spring Boot at `http://localhost:8080`.

The API base path is `http://localhost:8080/api/v1/customers`.
Swagger UI is available at `http://localhost:8080/swagger-ui/index.html`
OpenAPI document at `http://localhost:8080/v3/api-docs`.

H2 is in-memory: records remain available across requests while the backend runs and are cleared when it stops.

### Smoke Check

1. Open the UI, create a customer with a past birth date, and confirm it appears first in the list.
2. Refresh the page and confirm the record remains while the backend is still running.
3. Open the Swagger UI and try retrieving the created customer by its ID.

## Verify

Run backend tests from the repository root:

```powershell
.\gradlew.bat test
```

Run frontend checks from the repository root:

```powershell
cd frontend
npm run lint
npm run build
npm test
```

## Design Decisions

- **Java instead of Kotlin:** I chose Java because I have more experience with it and could work more effectively within the assignment's timebox.
- **Lightweight DDD:** I chose Lightweight DDD to keep the core business rules independent of frameworks and persistence. Keeping the domain independent makes its business rules straightforward to test without infrastructure.
- **Validation at boundaries:** Request validation gives API callers useful field-level feedback; domain validation protects invariants for callers that bypass REST.
- **In-memory persistence:** H2 satisfies the assignment and makes local setup straightforward.
- **Newest-first ordering:** The list is ordered by descending generated ID so a newly created customer is immediately visible after refresh.
- **Error handling and tests:** I treated error classification as part of the API contract. Known domain-validation failures return 400 responses, while unexpected application or infrastructure failures return a generic 500. This prevents server faults from being misreported as client mistakes and avoids exposing internal details. Focused tests cover both outcomes and preserve Spring’s standard error responses.
- **Date-only values:** Birth date is a calendar date, not a timestamp, so the app preserves it without timezone conversion.

## Production Considerations

- This is an assessment application, not a deployable banking service. Authentication and authorization, privacy and retention controls for personal data, durable storage, production management-endpoint security, and configured monitoring/alerting are outside scope and would need to be addressed before deployment.
- Duplicate prevention is not implemented. Name and date of birth alone cannot reliably identify a person, so rejecting matching records could block legitimate customers. A production design would require an agreed identity key or a deliberate review and matching process.