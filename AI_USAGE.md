# AI Usage & Engineering Reflection
## AI Tools

- GitHub Copilot in VS Code was the AI coding assistant used for repository review, documentation, domain code, tests, and build verification.
- I supplied feedback generated with Gemini on the implementation plan. Copilot evaluated it against the repository rules and specification; Gemini did not directly modify this repository.
- ChatGPT and Claude were not used.

## AI-Assisted Work and Human Ownership

AI assistance:
- Reviewed the Gradle build and project requirement, changed the Spring Boot plugin to 3.5.0, and aligned web and test dependencies with Spring Boot 3.
- Assisted with Java 21 setup and Gradle verification.
- Compared the interview brief with the project specification and drafted documentation covering scope, API behavior, Lightweight DDD boundaries, and setup.
- Generated the initial pure-Java `CustomerId`, `PersonName`, and `BirthDate` value objects, the `Customer` aggregate, and focused JUnit tests. Updated birth-date validation and tests to use UTC.
- Implemented the domain `CustomerRepository` contract, transactional application service, JPA entity and persistence adapter, validated POST DTO/controller, UTC validation clock, and RFC 7807 error handling for customer creation.
- Added MockMvc integration tests that verify successful H2 persistence, generated IDs and `Location`, and that invalid requests do not persist.
- Implemented `GET /api/v1/customers/{id}` through the domain repository contract, application service, persistence mapping, REST controller, and RFC 7807 handling for missing, malformed, and non-positive IDs. Added integration coverage that follows the POST response's `Location` with GET.
- Implemented `GET /api/v1/customers` with descending ID order, a read-only application transaction, and shared mapping from JPA entities to domain customers. Added integration tests for empty lists, POST-created customers appearing newest-first, and repeated reads leaving storage unchanged.
- Implemented the read-only React customer list with typed Axios requests, a Vite API proxy, cancellation handling, refresh/retry states, accessible table markup, date-only display, and responsive Allica-inspired styling.
- Added the customer creation form and typed POST request, pending-submit guard, success-only clearing, preserved entries on failure, and post-creation list refresh with stale-read cancellation. Added Vitest, Testing Library, and jsdom for focused creation tests.
- Added client-side name and UTC birth-date validation, backend field-error messages, clearer general/network/timeout errors, accessible field descriptions and focus, and tests for invalid input and corrected submissions.
- Added springdoc-openapi Swagger UI and OpenAPI 3 documentation for customer operations, request/response schemas, and RFC 7807 field-validation errors, with integration tests for the generated document and UI route.

Human-owned work:
- Set the Spring Boot 3.x requirement, completed the earlier infrastructure work and commits, and chose Lightweight DDD as an intentional interview focus.
- Experimented with assigning implementation and review to different AI agents.
- Inspected the Gradle dependencies, noticed Lombok was configured but unused in the source, and asked Copilot to remove its dependencies.
- Manually tested the customer list interactions: Verified a failed refresh kept existing rows visible and the retry control worked.

## Current Status

The Java 21 toolchain and Spring Boot 3 setup are in place. The pure domain model, H2 persistence adapter, `POST /api/v1/customers`, `GET /api/v1/customers/{id}`, and `GET /api/v1/customers` are implemented. The customer list and creation form include client validation, backend field errors, accessible feedback, and responsive layout. Swagger UI and the generated OpenAPI document cover all customer operations and their response schemas.

## Validation

- Confirmed the Gradle Java toolchain is configured for Java 21 and identified Temurin Java 21 on the machine.
- `gradlew.bat help` and `gradlew.bat test` passed during the Spring Boot 3 infrastructure work.
- The focused customer-domain JUnit tests and the full backend test suite passed with Java 21 selected through `JAVA_HOME`, including the UTC birth-date tests.
- GET-by-ID integration tests and full backend test suite passed, Verified manually in Postman
- The focused customer-list integration tests and full `gradlew.bat test` suite passed after implementing descending ID order. The tests verify `200` JSON responses, empty arrays, all persisted customer fields, newest-first ordering, and unchanged storage after repeated GET requests.
- Frontend lint and production build passed for the read-only customer screen. Playwright checks with intercepted API responses verified initial loading, populated and empty lists, retained rows during refresh/failure, disabled overlapping refresh, retry recovery, and long-name wrapping at desktop/mobile sizes. Screenshots were inspected and button/secondary-text contrast checked. The live API request through the proxy returned HTTP 500, so successful live backend-to-UI retrieval remains unverified; the UI showed its error state correctly.
- Frontend slice: Create Customers, AI ran the frontend checks and tested customer creation and recovery. I also added a customer manually and confirmed it appeared in the list.
- Frontend slice: Validation And Polish, AI ran lint, build, and 32 tests. Browser checks covered clear field errors, keyboard focus, mobile layout, and failure recovery using simulated API responses. Live customer-list loading also succeeded; no live creation was performed during this slice's automated checks.
- Final live acceptance check: Submitted a customer through the UI and confirmed it appeared first in the list without a page reload and the form cleared. Backend and frontend test suites passed.
- Swagger verification: The full backend test suite passed, including MockMvc checks for all OpenAPI operation paths, success/error responses, the validation-error schema, and Swagger UI HTML. Opened the live UI on port 8081 and confirmed all three customer operations and schemas render.
- Manually sent a valid `POST /api/v1/customers` request from Postman and confirmed the endpoint worked outside the automated test environment.
- `npm --prefix frontend run lint` passed; no frontend files were changed during the domain work.
- VS Code reported no diagnostics for the edited specification and domain files. Markdown checks for balanced fences, local links, and trailing whitespace passed.
- An initial backend baseline attempt ran under Java 25 while Gradle required Java 21 and failed to locate a matching toolchain. After locating the installed Java 21 distribution and setting `JAVA_HOME`, the tests passed.

## AI Errors and Corrections

- An interview-notes draft was removed so interview coaching and distributed-systems discussion would stay out of the implementation specification.
- An early specification draft added an unsupported 150-year maximum age for customers. It was removed to follow the agreed rule that birth dates must be in the past without imposing an arbitrary lifespan limit.
- The active PATH selected Java 25, although the project requires Java 21. An initial `JAVA_HOME` guess pointed at the parent directory rather than the nested JDK installation; correcting it enabled Gradle to find Java 21. This illustrates why environment paths and toolchain versions must be verified rather than inferred.
- A process oversight briefly added the supplied assessment PDF to an unpushed commit so the specification could link to it. After recognizing that the brief may not be redistributable, I reset that commit and removed the link from `SPEC.md`. Later, Copilot incorrectly said the PDF was not included in the proposed commit; it was still in the staged changes, and I caught and removed it manually before committing. 

## Approximate Time

These estimates were not measured with a timer.

| Work | With AI | Estimated without AI |
| --- | ---: | ---: |
| Spring Boot alignment and Java 21 setup | 15-25 min | 50-85 min |
| Brief review, specification, and setup documentation | 20-35 min | 60-100 min |
| Domain model, tests, and verification | 15-25 min | 45-75 min |
| POST API, H2 persistence, and integration tests | 20-30 min | 60-90 min |
| Frontend customer creation, validation, tests, and polish | 45-75 min | 120-180 min |
| Swagger/OpenAPI documentation and tests | 10-20 min | 30-50 min |
| **Estimated total** | **125-210 min** | **365-580 min** |

## Reflection

Java and infrastructure setup is a recurring source of wasted engineering time: developers can spend hours tracing `JAVA_HOME`, `PATH`, multiple installed JDKs, Gradle toolchain requirements, and incompatible framework versions. This project encountered that same category of problem: Java 25 was active while the build required Java 21. AI helped narrow the diagnosis and guide verification, but checking the actual installation path and running the tests were still necessary. More broadly, AI accelerated boilerplate, documentation, and test setup; human judgment remained essential for the architecture, scope, and deciding which suggestions conflicted with the brief or Lightweight DDD goals.

This project has also been an excellent opportunity to explore building an AI-first greenfield application. Early on, I got carried away by that opportunity and moved away from my usual incremental process. As a result, some initial commits became larger and less focused than I would have liked. I’ve recognized that, and I’m applying a more deliberate, step-by-step approach as the implementation continues.