Markdown
## Customer Management Application

This specification translates the provided interview brief into a small, testable implementation contract. The brief governs the functional scope and timebox. Lightweight Domain-Driven Design (DDD) is an intentional engineering objective, even where the brief does not mandate it, because it keeps domain rules explicit and technical concerns separated. Apply it without overbuilding the brief's small application.

## Goals and Scope

Deliver a working full-stack application that lets a user create customer records and retrieve and display them. The solution should be clear to maintain, validate input at the API boundary and in the domain, handle expected failures, include focused tests, and document setup and design decisions. Use the customer domain to demonstrate Lightweight DDD: explicit invariants, a meaningful aggregate boundary, and clear separation between business rules and technical adapters.

In scope:
- Create a customer with first name, last name, and date of birth.
- Retrieve one customer or list all customers through HTTP endpoints.
- Persist records in an embedded, in-memory database for the lifetime of the running application.
- Provide a simple web interface to create and display customers.

Out of scope: editing or deleting customers, authentication, search, pagination, deployment, and durable storage across application restarts.

## Selected Stack

The brief prefers Kotlin, Spring Boot 3.x, React 19 with TypeScript, and H2, while explicitly allowing Java and acceptable embedded/in-memory alternatives. This repository is already scaffolded for Java 21, Spring Boot 3.5, Gradle, React 19, TypeScript, Vite, and H2; retain that compatible stack rather than restarting in a different language. Use the existing Spring Data JPA setup for persistence and springdoc-openapi for API documentation.

## Domain and Validation Rules

- `Customer` is the aggregate root. Domain types have no Spring, JPA, or Jackson dependencies; persistence and REST mapping stay outside the domain.
- Use immutable Java records for `CustomerId`, `PersonName`, and `BirthDate`. Construct and change domain state through validated constructors or explicit domain methods, not public setters.
- Trim first and last names before storage. Each is required, must not be blank, and is limited to 50 characters.
- Date of birth is required, must be a valid ISO-8601 calendar date (`YYYY-MM-DD`), and must be earlier than the current UTC date. Do not add an arbitrary minimum-age or maximum-lifespan restriction.
- Enforce request validation at the REST boundary and domain invariants in domain types so invalid data cannot enter through another application caller.
- Assign each persisted customer a unique, generated, positive identifier.

## HTTP API

Base path: `/api/v1/customers`.

Document the customer API using OpenAPI 3. The Swagger UI is available at `/swagger-ui/index.html`, and the generated OpenAPI document is available at `/v3/api-docs`. Document the create, retrieve, and list operations, including request and response schemas, status codes, and RFC 7807 error responses with the field-validation `errors` extension. The `/v3/api-docs` path refers to the OpenAPI document format and does not change the customer API's `/v1` version.

### Create a customer

`POST /api/v1/customers` accepts:

```json
{
  "firstName": "Deckard",
  "lastName": "Cain",
  "dateOfBirth": "1985-05-20"
}
```

On success, return `201 Created`, a `Location` header for `/api/v1/customers/{id}`, and a JSON customer representation:

```json
{
  "id": 1,
  "firstName": "Deckard",
  "lastName": "Cain",
  "dateOfBirth": "1985-05-20"
}
```

Invalid JSON, missing or invalid fields, and future dates return `400 Bad Request` as RFC 7807 `ProblemDetail` responses. For field-validation failures, include an `errors` extension containing an array of `{ "field": "...", "message": "..." }` entries. Malformed JSON may return a general `ProblemDetail` without field errors. Do not persist invalid requests.

### Retrieve customers

- `GET /api/v1/customers` returns `200 OK` and a JSON array of customer representations ordered by `id` descending, so the newest customer appears first. An empty database returns an empty array.
- `GET /api/v1/customers/{id}` returns `200 OK` and the customer representation, or `404 Not Found` as an RFC 7807 `ProblemDetail` response when no customer exists for that identifier.

The H2 database is in-memory: data remains available across requests while the application is running and is lost when it stops. Do not expose persistence entities from the API.

## Web Application

The React and TypeScript client provides a customer creation form and a list of customers. It submits dates as `YYYY-MM-DD` and displays names and dates from the API. It handles loading, submission, error, empty-list, and populated-list states; labels form fields accessibly and reports validation or network failures without losing already loaded customers. Keep the interface focused on this workflow.

Use the Vite development proxy for `/api` requests to the local Spring Boot server so development does not depend on permissive cross-origin settings.

- **Refresh behavior:** Refreshing the customer list preserves existing records while loading. If refresh fails, keep them visible and offer retry.
- **Date display:** Treat birth dates as date-only values, without timezone conversion.
- **Accessibility:** Use semantic table headings, keyboard-accessible controls, visible focus indicators, and accessible loading/error feedback.

Use a restrained, Allica-inspired visual style suitable for customer-management work, without implying the application is an official Allica product.

## Architecture

Keep the dependency direction `REST / infrastructure -> application -> domain`. The domain must not depend on Spring, JPA, Hibernate, Jackson, or HTTP types.

- `domain/`: `Customer` aggregate root, immutable `CustomerId`, `PersonName`, and `BirthDate` value objects, a `CustomerRepository` interface that accepts and returns domain `Customer` instances only, and domain exceptions where needed. Enforce invariants here so every caller gets the same guarantees.
- `application/`: `CustomerApplicationService` use cases for creating and retrieving customers. Orchestrate domain behavior and put transaction boundaries here; do not move business rules into controllers or persistence adapters.
- `infrastructure/persistence/`: JPA entities, Spring Data interfaces, and a repository adapter that maps between persistence models and domain objects. Persistence annotations stay out of the domain.
- `application/rest/`: controllers, validated request/response records, and RFC 7807 exception handling. Translate transport data at the boundary and never expose JPA entities.
- `frontend/`: TypeScript API types matching the response contract and UI workflows that depend on the HTTP API, not backend implementation details.

This is Lightweight DDD, not a requirement to add ceremony: introduce only the domain types and boundaries needed to protect the customer rules and make the use cases testable.

## Operational Scope

Spring Boot Actuator is included as a foundation for health checks and metrics. External metrics collection, dashboards, alerting, distributed tracing, and production management-endpoint security configuration are outside this assignment's scope.

## Verification and Acceptance Criteria

The work is complete when:
- A user can create a valid customer in the UI and see it in the customer list.
- A created customer can be retrieved through the API, and the list remains available across requests.
- Swagger UI and the generated OpenAPI document load successfully and describe all customer operations, request and response schemas, and documented error responses.
- Invalid names and invalid, missing, or future dates are rejected with a useful `400` response; an unknown customer ID returns `404`.
- Domain tests run without a Spring context and cover value-object and aggregate invariants; Spring/MockMvc tests cover creation, retrieval, persistence mapping, validation, and error responses.
- The domain remains framework-independent, repositories speak domain types, application services orchestrate use cases and transactions, and persistence/REST concerns stay in their adapters.
- Frontend lint and production build pass, and the UI explicitly handles loading, error, empty, and populated states.
- The root README documents prerequisites, startup, tests, and the key design decisions; `AI_USAGE.md` records actual AI use and review evidence.

Run backend tests with `gradlew.bat test` from the repository root. Run frontend checks with `npm run lint` and `npm run build` from `frontend/`.

## Submission

Submit a Git bundle as required by the brief. Include the completed work in commits before creating the bundle so the implementation and its history are present. The brief's example command is `git bundle create your-name-tech-test.bundle --all`; verify the resulting bundle before submission.