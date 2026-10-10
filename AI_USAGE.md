# AI Usage & Engineering Reflection

## Scope
This entry records the AI-assisted infrastructure work from the setup session. It does not claim AI authorship of application features or domain code from other work.

## AI Tools
- GitHub Copilot in VS Code was the AI coding assistant used for this work.
- ChatGPT, Claude, and other external AI tools were not used in this session.

## Delegated Work and Human Ownership
AI assistance:
- Inspected the Gradle build and project requirement, then changed the Spring Boot plugin from 4.1.1 to 3.5.0 and replaced Boot 4-specific web and test starters with Boot 3-compatible dependencies.
- Installed the Temurin Java 21 JDK in the user profile and configured user-level `JAVA_HOME`.
- Ran Gradle configuration checks and the backend test suite.

Human-owned work:
- Set the requirement that the project use Spring Boot 3.x and directed the migration.
- Owned the earlier infrastructure work and its commits; this usage-log commit is separate.
- Reviewed the proposed changes and decided whether they met the project requirement.

## Validation
- Confirmed the installed runtime was Temurin Java 21.0.12.
- `gradlew.bat help` completed successfully after the Boot 3 migration.
- `gradlew.bat test` completed successfully with Java 21.
- Reviewed the Gradle diff to confirm the changes were limited to the Boot version and compatible dependency coordinates.

## AI Errors and Corrections
- An initial `winget` attempt did not find an applicable user-scope installer. The JDK was instead downloaded from the official Adoptium API and extracted under the user profile.
- The first test invocation used the Unix-style `./gradlew.bat` path, which PowerShell did not recognize. Switching to PowerShell's local-path syntax fixed it; the tests then ran successfully once Java 21 was available.
- An interim status update incorrectly said the JDK download was still running after it had completed. The terminal output confirmed Java 21 was installed, and work continued with that runtime.

## Approximate Time
These are retrospective estimates, not timed measurements.

| Work | With AI | Estimated without AI |
| --- | ---: | ---: |
| Inspect requirement and align Spring Boot dependencies | 5-10 min | 20-35 min |
| Install Java 21 and verify the backend build | 10-15 min | 30-50 min |
| **Total** | **15-25 min** | **50-85 min** |

## Reflection
AI shortened the dependency migration and Java setup by identifying the Boot 4/Boot 3 mismatch, applying the build changes, and running verification. Human direction remained essential: the Spring Boot 3.x requirement came from the project owner, the changes were reviewed against that requirement, and the initial package-manager and PowerShell issues needed correction. The successful test run provided evidence that the migrated build worked with the Java 21 toolchain.
