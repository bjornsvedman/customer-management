# Customer Management

A full-stack customer management application built for the Senior Full Stack Technical Test. It uses Java 21, Spring Boot 3.5, H2, React 19, TypeScript, and Vite. See [`SPEC.md`](SPEC.md) for the detailed functional and API contract, and [`AI_USAGE.md`](AI_USAGE.md) for the AI-use and engineering log.

## Prerequisites

- JDK 21.
- Node.js 24.21.0 was used to verify the frontend.

## Run The Prebuilt Demo

If you received the optional prebuilt bundle, extract it and follow its included README. With Docker running, open a terminal in the extracted directory:

```powershell
docker load --input customer-management-images.tar
docker compose -f compose.prebuilt.yaml up
```

Open http://localhost:8081. No Java, Node.js, build-time CA certificate, or dependency downloads are needed. The bundle targets Linux AMD64; ARM64 emulation has not been verified. Stop any source-built demo using the same ports first. See [the standalone demo instructions](docs/demo-readme.md) for requirements, port overrides, and shutdown.

### Create A Bundle

First build and verify the source images using the Docker instructions below, including the CA override if your network requires it. Then run from the repository root (PowerShell):

```powershell
New-Item -ItemType Directory -Force dist/customer-management-demo
docker image tag customer-management-backend:latest customer-management-backend:demo
docker image tag customer-management-frontend:latest customer-management-frontend:demo
docker image save --output dist/customer-management-demo/customer-management-images.tar customer-management-backend:demo customer-management-frontend:demo
Copy-Item compose.prebuilt.yaml dist/customer-management-demo/
Copy-Item docs/demo-readme.md dist/customer-management-demo/README.md
```

Distribute the complete `dist/customer-management-demo` directory alongside the source, subject to submission size limits. The generated bundle is excluded from Git. Do not include local CA certificate files. The included Compose file uses the exported `demo` image tags and never builds or pulls images. The source build uses different image tags and is unaffected.

## Run With Docker

As an alternative to installing Java and Node.js, install Docker Desktop and start its Linux container engine. From the repository root:

```powershell
docker compose version
docker compose up --build
```

Open the UI at http://localhost:8081. Swagger UI is available at http://localhost:8080/swagger-ui/index.html, and the API at http://localhost:8080/api/v1/customers.

The first build needs internet access to download images and dependencies. Image builds run the backend tests and frontend lint, tests, and build. Compose waits for the backend health check before starting the frontend. Both ports are bound to localhost for this local demo; this is not a production deployment configuration.

Keep the command running while using the demo. To stop, press `Ctrl+C`, then remove the containers with:

```powershell
docker compose down
```

H2 data is in memory and is lost whenever the backend stops, even if the container is not removed. No database installation or volume is needed.

Stop any locally running backend before starting Compose. If ports 8080 or 8081 are occupied, change the host-side port (the middle number in `127.0.0.1:8081:80`, for example) in [compose.yaml](compose.yaml) and use that port in the browser URL. The internal container ports and API proxy do not need to change.

### Corporate Network Certificates

If a build fails with `UNABLE_TO_GET_ISSUER_CERT_LOCALLY` or a Java certificate trust error on an HTTPS-inspecting corporate network, obtain your organisation's approved public root CA certificate in PEM format. Save it as `.build-ca.pem` in the repository root. This local file is ignored by Git and excluded from the ordinary Docker build context. Do not supply a private key or disable TLS verification.

Use the optional CA configuration when building on that network:

```powershell
$env:BUILD_CA_FILE = '.build-ca.pem'
docker compose -f compose.yaml -f compose.ca.yaml up --build
```

The certificate is mounted as a BuildKit build secret and used only to establish trust during dependency downloads. It is not included in either runtime image. Keep it inside the repository directory to avoid Docker build file-access restrictions. Other networks can use the standard command above without this configuration. `docker compose down` still stops and removes the same services.

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