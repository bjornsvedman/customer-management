# AI Usage & Engineering Reflection
## AI Tools

- GitHub Copilot in VS Code was the AI coding assistant used for repository review, documentation, domain code, tests, and build verification.
- The project owner supplied feedback generated with Gemini on the implementation plan. Copilot evaluated it against the repository rules and specification; Gemini did not directly modify this repository.
- ChatGPT and Claude were not used.

## AI-Assisted Work and Human Ownership

AI assistance:
- Reviewed the Gradle build and project requirement, changed the Spring Boot plugin to 3.5.0, and aligned web and test dependencies with Spring Boot 3.
- Assisted with Java 21 setup and Gradle verification.
- Compared the interview brief with the project specification and drafted documentation covering scope, API behavior, Lightweight DDD boundaries, and setup.
- Generated the initial pure-Java `CustomerId`, `PersonName`, and `BirthDate` value objects, the `Customer` aggregate, and focused JUnit tests. Updated birth-date validation and tests to use UTC.

Human-owned work:
- Set the Spring Boot 3.x requirement, completed the earlier infrastructure work and commits, and chose Lightweight DDD as an intentional interview focus.

## Current Status

The Java 21 toolchain and Spring Boot 3 setup are in place. The pure domain model and its unit tests are implemented and passing. Application use cases, the domain repository interface, persistence mapping, REST API, and customer UI are not implemented yet. The next planned slice is the repository contract, application service, and persistence adapter, with tests for those boundaries.

## Validation

- Confirmed the Gradle Java toolchain is configured for Java 21 and identified Temurin Java 21 on the machine.
- `gradlew.bat help` and `gradlew.bat test` passed during the Spring Boot 3 infrastructure work.
- The focused customer-domain JUnit tests and the full backend test suite passed with Java 21 selected through `JAVA_HOME`, including the UTC birth-date tests.
- `npm --prefix frontend run lint` passed; no frontend files were changed during the domain work.
- VS Code reported no diagnostics for the edited specification and domain files. Markdown checks for balanced fences, local links, and trailing whitespace passed.
- An initial backend baseline attempt ran under Java 25 while Gradle required Java 21 and failed to locate a matching toolchain. After locating the installed Java 21 distribution and setting `JAVA_HOME`, the tests passed.

## AI Errors and Corrections

- An interview-notes draft was removed so interview coaching and distributed-systems discussion would stay out of the implementation specification.
- An early specification draft added an unsupported 150-year maximum age for customers. It was removed to follow the agreed rule that birth dates must be in the past without imposing an arbitrary lifespan limit.
- The active PATH selected Java 25, although the project requires Java 21. An initial `JAVA_HOME` guess pointed at the parent directory rather than the nested JDK installation; correcting it enabled Gradle to find Java 21. This illustrates why environment paths and toolchain versions must be verified rather than inferred.
- A process oversight briefly added the supplied assessment PDF to an unpushed commit so the specification could link to it. After recognizing that the brief may not be redistributable, I reset that commit, kept the PDF local, and removed the link from `SPEC.md`. Check permission before publishing supplied assessment material, and distinguish local reference files from submission artifacts.

## Approximate Time

These retrospective estimates were not measured with a timer. The without-AI column is an estimate, not a claim about a measured counterfactual.

| Work | With AI | Estimated without AI |
| --- | ---: | ---: |
| Spring Boot alignment and Java 21 setup | 15-25 min | 50-85 min |
| Brief review, specification, and setup documentation | 20-35 min | 60-100 min |
| Domain model, tests, and verification | 15-25 min | 45-75 min |
| **Estimated total** | **50-85 min** | **155-260 min** |

## Reflection

Java and infrastructure setup is a recurring source of wasted engineering time: developers can spend hours tracing `JAVA_HOME`, `PATH`, multiple installed JDKs, Gradle toolchain requirements, and incompatible framework versions. This project encountered that same category of problem: Java 25 was active while the build required Java 21. AI helped narrow the diagnosis and guide verification, but checking the actual installation path and running the tests were still necessary. More broadly, AI accelerated boilerplate, documentation, and test setup; human judgment remained essential for the architecture, scope, and deciding which suggestions conflicted with the brief or Lightweight DDD goals.

This project has also been an excellent opportunity to explore building an AI-first greenfield application. Early on, I got carried away by that opportunity and moved away from my usual incremental process. As a result, some initial commits became larger and less focused than I would have liked. I’ve recognized that, and I’m applying a more deliberate, step-by-step approach as the implementation continues.