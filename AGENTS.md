# Server repository guidance

- This is an independent Java 25 repository using Gradle Kotlin DSL and the Gradle wrapper.
- Keep the Java toolchain configured for version 25 in `build.gradle.kts`.
- Production code belongs in `src/main/java`; tests belong in `src/test/java`.
- Tests use JUnit 5, including HTTP integration tests for the hello endpoint.
- Start the local API: `./gradlew run` (binds to 127.0.0.1:8080; override the port with `PORT`).
- `GET /api/hello` returns the greeting JSON consumed by the React client.
- Run commands from this repository or its development worktree.
- Build and verify: `./gradlew build`.
- Full test suite: `./gradlew test`.
- Single test class: `./gradlew test --tests 'com.example.MyTest'` (replace with an actual test class).
