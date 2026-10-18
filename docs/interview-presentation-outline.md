# Interview Presentation: Customer Management Solution

Personal interview preparation notes. This document is separate from the assignment specification and implementation documentation.

## Shape for a 20-Minute Slot

Aim for a concise 10-minute walkthrough, then leave the remaining time for interviewer-led discussion. The goal is to explain the choices in the submitted solution and show how you would evolve it, not to present unimplemented architecture as if it already exists.

### 0-1 min: Frame the solution

> I’ll walk through the customer flow end to end, explain the main trade-offs, and distinguish what I implemented from what I would add for production.

The assignment asks for a customer create-and-retrieve flow with a simple UI and in-memory persistence. The solution uses Java 21, Spring Boot 3.5, React 19, TypeScript, and H2. Java was an accepted option, matched the existing project setup, and was the language I could use most effectively within the timebox.

### 1-5 min: Trace one request

Follow a customer creation request through the system:

1. The React form in [App.tsx](../frontend/src/App.tsx) collects first name, last name, and a date-only birth date.
2. The REST request record in 
[CreateCustomerRequest.java](../src/main/java/com/bjorn/customer_management/application/rest/dto/CreateCustomerRequest.java) 
validates fields at the API boundary.
3. Domain value objects such as 
[PersonName.java](../src/main/java/com/bjorn/customer_management/domain/model/PersonName.java), 
[BirthDate.java](../src/main/java/com/bjorn/customer_management/domain/model/BirthDate.java), and 
[CustomerId.java](../src/main/java/com/bjorn/customer_management/domain/model/CustomerId.java) 
normalize values and enforce domain invariants. The 
[Customer.java](../src/main/java/com/bjorn/customer_management/domain/model/Customer.java) 
aggregate has no Spring or JPA dependency.
4. The [CustomerApplicationService.java](../src/main/java/com/bjorn/customer_management/application/CustomerApplicationService.java) coordinates the create use case and transaction.
5. The [CustomerPersistenceAdapter.java](../src/main/java/com/bjorn/customer_management/infrastructure/persistence/CustomerPersistenceAdapter.java) maps the domain customer to a [CustomerJpaEntity.java](../src/main/java/com/bjorn/customer_management/infrastructure/persistence/CustomerJpaEntity.java) and saves it to H2.
6. The [CustomerController.java](../src/main/java/com/bjorn/customer_management/application/rest/CustomerController.java) returns `201 Created`, the response body, and a `Location` header.

> Validation appears in three layers for different reasons, not because each layer is authoritative. The frontend gives immediate feedback and avoids an unnecessary round trip; the request DTO protects the HTTP boundary and returns useful client errors; the domain enforces the business rules for every caller, including callers that bypass the UI and REST API. The domain is the source of truth. This does mean some duplication and a risk of drift, so I keep the rules simple and test each layer. If they became more complex, I would consider generating client validation from the API contract rather than maintaining more copies by hand.

> I chose Lightweight DDD to keep the core rules independent of frameworks and persistence. Keeping the domain independent makes its business rules straightforward to test without infrastructure. That boundary adds mapping code, so I kept it deliberately small.

Mention that birth dates are represented as calendar dates, not timestamps, and that the list is ordered newest first so the user can confirm a successful creation easily.

### 5-7 min: Errors and recovery

> I treated error classification as part of the API contract. Known domain validation failures return `400`; unexpected failures return a generic `500` without exposing internal details. That keeps a server fault from being mistaken for bad input.

The UI preserves entered data and existing rows on failure. After a successful create, if refreshing the list fails, retry performs a GET rather than another POST. A timed-out POST can still have an uncertain outcome; making automatic retries safe would require an idempotency mechanism.

### 7-9 min: Verification

Describe the different test layers accurately:

- Pure domain tests cover value-object and aggregate rules without a Spring context.
- MockMvc integration tests cover HTTP behavior, validation, persistence, and error responses using H2.
- Frontend tests cover user interactions with mocked API calls.
- I also verified the live create-and-display flow and API documentation separately.

Be clear about the boundary: mocked frontend tests are not a full automated end-to-end suite.

### 9-10 min: Summarize the assignment trade-offs

H2 is deliberately in-memory and loses data when the process stops. Newest-first ordering is an assignment-level product choice. The solution is a tested assessment vertical slice, not a production-ready banking application.

> H2 has a short memory: stopping the app clears its records. That is convenient for this assignment, but not a production retention strategy.

## Discussion Topics for the Remaining Time

Use these as options when the interviewer wants to go deeper; do not try to cover every topic regardless of interest.

### Likely Questions and Answer Cues

1. **Why use DDD for a small CRUD application? Is it overengineered?** I wanted the domain rules and boundaries to be explicit and independently testable. The domain here is small, so I kept the model and application layers lightweight and avoided adding broader DDD ceremony. I would simplify further if those boundaries stopped providing value.
2. **Why validate in the UI, DTO, and domain?** The UI gives immediate feedback, the DTO protects the HTTP contract, and the domain protects its rules for every caller. The domain is authoritative. Repeating checks risks drift, so I kept these rules simple and tested each layer; for more complex contracts I would consider generating client validation from the API schema.
3. **What if a create request succeeds but the client times out?** The client may not know whether the write completed. The UI avoids repeating the POST when creation succeeded but the list refresh failed; a timed-out POST is still ambiguous. Safe retries would need an idempotency key or equivalent server-side handling.
4. **Would you deploy this to a bank as-is?** No. Authentication and authorization, personal-data protection and retention, durable storage, and production operations are not implemented. I would work with Security, Risk, and Compliance and use the bank's existing platform standards before deployment.
5. **How would you investigate a generic `500` without exposing details?** The response is sanitized, but this assignment has limited diagnostics. In production I would use the bank's structured logging and tracing, correlate failures with a trusted trace ID, and monitor aggregate error rates. Logs must not include customer data, request bodies, credentials, or tokens.
6. **Why not add event sourcing, queues, or actors now?** The current use case does not require their complexity. If reliable downstream notifications become a requirement, I would consider events with a transactional outbox. Event sourcing or actors would need a specific requirement that justifies their operational and data-management trade-offs.

### Security and data protection

State what exists: request validation, domain validation, explicit REST DTOs, and generic unexpected-error responses. State what does not: authentication, authorization, production secrets management, privacy/retention controls, and configured security monitoring. For a real banking service, begin with threat modelling and data classification alongside Security, Risk, and Compliance, then apply the organisation's identity, network, secret-management, scanning, and audit standards. Avoid claiming that this assignment implements zero trust or is production secure.

### Moving from H2 to a production database

Do not describe this as only changing a connection string. Select a supported database based on the bank's platform and transactional needs; replace `create-drop` with versioned Flyway or Liquibase migrations; externalize credentials; and plan deployment sequencing, backup/restore, and integration testing. The current repository adapter gives a boundary for persistence, but the migration still needs compatibility and operational work.

For this customer record workflow, a relational database is a reasonable default because it provides transactions, constraints, and relational queries. NoSQL may be a better fit for access patterns requiring specific horizontal scale or flexible documents, but brings different consistency, integrity, and query trade-offs. Choose from workload and platform requirements, not fashion.

### List growth and pagination

The current list endpoint returns every customer, which is appropriate for the assignment but would increase response size, memory use, and database work as the dataset grows. Before production, I would define the client use case and API contract for pagination, enforce a maximum page size, and preserve deterministic newest-first ordering. Offset pagination may be sufficient for modest datasets; cursor pagination can be a better fit for large or frequently changing lists. The UI would load additional pages rather than fetch the entire customer table at once.

### Events, queues, and service boundaries

Do not jump from this small application to microservices. If another system needs reliable notification of customer changes, consider domain events and a transactional outbox so the database write and event publication cannot silently diverge. Choose synchronous versus asynchronous communication based on latency, consistency, ownership, and failure-recovery requirements.

An actor model can be useful when the domain benefits from serialized, per-entity state processing, but it is not a generic solution to high concurrency. It introduces its own lifecycle, distribution, and failure-handling concerns.

### Event sourcing and historical data

Event sourcing can support reconstructing past state and recording business changes, but an append-only event log is not automatically tamper-proof or a complete audit solution. It adds event-schema evolution, replay, projections, and operational complexity. Correcting bad historical data generally means recording compensating events rather than rewriting history. Personal-data retention or deletion can be especially difficult; encryption and key destruction may help in some designs but need legal, privacy, and security review. Use event sourcing only when the required audit/history capabilities justify these costs; an audit log can be a separate, simpler requirement.

## Closing

> The solution keeps the implemented flow small, testable, and explicit about its boundaries. I would evolve it based on the bank's security, data, reliability, and team-platform requirements rather than add distributed architecture before those needs are clear.
