# server

Independent Java 25 project using Gradle Kotlin DSL and JUnit 5.

## Requirements

- JDK 25, available through `JAVA_HOME` or `PATH`.
- Internet access on the first build to download Gradle and dependencies.

## Commands

Run these commands from this repository or one of its worktrees:

```sh
./gradlew build
./gradlew test
./gradlew run
./gradlew test --tests 'com.example.MyTest'
```

Replace `com.example.MyTest` with an actual test class. On Windows, use
`gradlew.bat` instead of `./gradlew`.

Production code belongs in `src/main/java`; tests belong in `src/test/java`.

## Hello API

`./gradlew run` starts a local HTTP server at `http://127.0.0.1:8080`.
Override the port with `PORT=8081 ./gradlew run` if needed (also update the
client's development proxy target).

`GET /api/hello` returns JSON with `eyebrow`, `title`, `message`, and `source`.
The React client fetches this endpoint through its Vite development proxy.
The prototype binds to loopback only, has no authentication, and is not a
production deployment setup. Stop it with Ctrl+C.
