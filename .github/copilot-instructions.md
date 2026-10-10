# Copilot Instructions
# Architectural Rules & Standards: Customer Management (Lightweight DDD)

## Project Structure
- The backend is a Java 21 Spring Boot application built with Gradle. Java sources are under `src/main/java`, tests under `src/test/java`, and runtime configuration under `src/main/resources`.
- The frontend is a React and TypeScript application built with Vite in `frontend/`.

## Working In This Repository
- Inspect nearby code and tests before changing behavior. Follow existing naming, formatting, and architectural patterns; keep changes focused and avoid unrelated refactoring.
- Preserve existing public APIs and configuration unless the task requires a change.
- For backend changes, add or update focused tests for behavior that changes. Use the existing JUnit and Spring test setup.
- For frontend changes, keep types accurate and follow the existing React and ESLint conventions. Do not add dependencies unless needed.
- Do not edit generated or build output under `build/` or `bin/`.

## Verification
- Backend tests: run `gradlew.bat test` from the repository root.
- Frontend checks: run `npm run lint` and `npm run build` from `frontend/` when frontend files change.
- Report any checks that could not be run and distinguish them from checks that passed.

## 1. Domain-Driven Design (DDD) Principles
- **Core Domain Isolation**: All classes in `domain/` MUST be pure Java 21 (zero dependencies on Spring, JPA, Hibernate, or Jackson).
- **Aggregate Root**: `Customer` is the Aggregate Root. Instantiation and state transitions MUST happen via explicit factory methods or domain methods—NO public setters or mutators.
- **Value Objects**: Domain fields (`CustomerId`, `PersonName`, `BirthDate`) MUST be immutable Java 21 `record` types that validate domain invariants upon creation (e.g., birth date must be in the past, name cannot be blank).
- **Repository Interface**: `CustomerRepository` in the domain package MUST work exclusively with domain entities (`Customer`), not JPA entities or DTOs.

## 2. Package & Layering Boundaries
Maintain strict dependency flow: `Infrastructure / REST` → `Application` → `Domain`. 
- `domain/`: Entities, Value Objects, Domain Repository Interfaces, Domain Exceptions.
- `application/`: Use cases and orchestration (`CustomerApplicationService`). `@Transactional` belongs here.
- `infrastructure/persistence/`: JPA Entities (`CustomerJpaEntity`), Spring Data Interfaces, and Repository Implementations mapping between JPA and Domain aggregates.
- `application/rest/`: Controllers, DTO Records (`CreateCustomerRequest`, `CustomerResponse`), and RFC 7807 Exception Handlers.

## 3. Java 21 & Spring Boot 3 Standards
- Use Java 21 `record` types for all Value Objects and REST DTOs.
- DTOs in `application/rest/dto/` use `jakarta.validation` annotations (`@NotBlank`, `@NotNull`, `@Past`).
- Controllers MUST return `ResponseEntity<T>` with standard HTTP status codes (`201 Created` with a `Location` header for creations).
- Global exception handling MUST map domain/validation exceptions to Spring 3 RFC 7807 `ProblemDetail` responses.

## 4. Frontend Architecture (React 19 + TypeScript)
- TypeScript interfaces in `src/types.ts` MUST strictly match the REST DTO records exposed by the backend.
- UI components MUST explicitly handle Loading, Error, Empty, and Populated states.
- Do not use `any` in TypeScript.

## 5. Testing Standards
- **Domain Unit Tests**: Test Aggregate roots and Value Objects using pure JUnit 5 (fast, zero Spring context overhead).
- **Integration Tests**: Use `@SpringBootTest` with `MockMvc` to verify REST endpoints, input validation bounds, and RFC 7807 error responses.