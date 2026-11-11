# AGENTS.md — Course Enrollment

## Map

| File | What it holds |
|---|---|
| `REQUIREMENTS.md` | The rules of the system, the console contract, build, out of scope |
| `ARCHITECTURE.md` | Packages and the rules every version follows |
| `src/test/java/enrollment/ArchitectureTest.java` | Executable architecture rules |

## Build and run

- `mvn -q verify` compiles and runs the tests. It must pass before you finish.
- `mvn -q -DskipTests package && java -jar target/enrollment.jar` starts the console.
