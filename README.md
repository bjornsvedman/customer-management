# Customer Management

A full-stack customer management application built for the Senior Full Stack Technical Test. The project uses Java 21, Spring Boot, H2, React 19, TypeScript, and Vite. See [`SPEC.md`](SPEC.md) for the functional contract, API, validation rules, and acceptance criteria; see [`AI_USAGE.md`](AI_USAGE.md) for the engineering and AI-use log.

## Prerequisites

- JDK 21
- Node.js and npm versions supported by the Vite version in `frontend/package.json`

## Run Locally

Start the backend from the repository root:

```powershell
./gradlew.bat bootRun
```

In a second terminal, install frontend dependencies and start Vite:

```powershell
cd frontend
npm ci
npm run dev
```

Open the local URL printed by Vite. The frontend will use a `/api` development proxy to reach Spring Boot; configure it as part of the frontend implementation. The API base path is `/api/v1/customers`. H2 is in-memory, so records are cleared when the backend stops.

## Verify

Run backend tests from the repository root:

```powershell
./gradlew.bat test
```

Run frontend checks from `frontend/`:

```powershell
npm run lint
npm run build
```

## Design Decisions

- Use the repository's existing Java 21, Spring Boot 3.5, Gradle, React 19, TypeScript, Vite, and H2 stack, all compatible with the brief's preferences or accepted alternatives.
- Keep domain rules independent from Spring and persistence; map domain objects to JPA entities and API records at their respective boundaries.
- Use H2 in-memory persistence to satisfy the brief's lightweight persistence requirement; durable storage is intentionally out of scope.
- Return customers ordered by descending ID so that after creation and a list refresh, the newest customer appears first and is easy to confirm. (Spec Change)
- Validate customer names and birth dates at the API and domain boundaries, return RFC 7807 problem responses for expected API errors, and cover behavior with focused backend and frontend checks.

## Production Considerations

- Duplicate prevention is not implemented. Name and date of birth alone cannot reliably identify a person, so rejecting matching records could block legitimate customers. A production system would need an agreed identity key or a deliberate review and matching process.

## Submission

The interview brief requests a Git bundle containing the repository history. Commit the completed work before creating the bundle, then follow the command in [`SPEC.md`](SPEC.md) and verify the bundle.