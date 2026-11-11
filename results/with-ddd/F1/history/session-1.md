# Session history

## Task
Implement version 1 of the course enrollment system described in REQUIREMENTS.md, following AGENTS.md and the rules in `.bob/rules`, with unit tests for all rules, passing `mvn -q verify` and matching the console contract exactly.

## Timing
- Start: 2026-11-11T06:02:29Z
- End: 2026-11-11T06:09:57Z

## token use
Agent Mode · 108.1k / 270.0k (40%) · 8.86 🅞

## Result
Done. `mvn -q verify` passes (116 tests), and `java -jar target/enrollment.jar` matches the console contract in REQUIREMENTS.md exactly.

## Decisions I made on my own

| ID | Type | Question I would have asked | What I decided | Why | Where |
|---|---|---|---|---|---|
| A1 | gap | `SectionRepository` in `model.puml` had no way to ask "is this student enrolled in this course?", but `EnrollmentService` needs it for I19 (duplicate check), `changeGrades` and `unenroll`. | Added `enrolledSection(StudentNumber, CourseCode): Optional<Section>` to `SectionRepository` and its in-memory implementation. Recorded as D11. | Three use cases need the same lookup; a repository query avoids repeated linear scans in the service layer. | `src/main/java/enrollment/domain/sections/SectionRepository.java`, `src/main/java/enrollment/infrastructure/InMemorySectionRepository.java` |
| A2 | gap | `Building` in the model had `addClassroom` and `removeClassroom` but not a way to look up one classroom by number (needed by `SectionService` to validate I16 and by `CampusService` to modify/delete). | Added `findClassroom(int): Classroom` to `Building`. Recorded as D12. | The only alternative was to expose the internal list; a named method keeps encapsulation. | `src/main/java/enrollment/domain/campus/Building.java` |
| A3 | gap | `AccessService` is constructed with `Session` and `Permissions`, but other services and `EnrollmentService.enrollMe` need to call `requireSession()` and `requirePermission()` on it, and `EnrollmentService.enrollMe` needs to read the logged-in user's student number. | Exposed `session()` and `permissions()` as public accessors on `AccessService`. Recorded as D13. | Avoids duplicating session/permission checks across services; all services delegate to `AccessService`. | `src/main/java/enrollment/application/AccessService.java` |
| A4 | design | The `ConsoleLoop` dispatches every instruction including `Exit()`, but `Exit()` must exit without printing and the loop must stop. | `Dispatcher` returns `null` for `Exit()` as a sentinel; `ConsoleLoop` exits when it receives `null`. Blank lines (parser returns `Optional.empty()`) produce no output and continue. Recorded as D15. | Clean separation: `ConsoleLoop` owns the "should I exit?" decision; `Dispatcher` signals it without a checked exception. | `src/main/java/enrollment/console/ConsoleLoop.java`, `src/main/java/enrollment/console/Dispatcher.java` |
| A5 | design | The model showed `SectionChoicePolicy.choose(List<Section>, capacityOf)` with `capacityOf` typed as a function, not a concrete type. | Used `java.util.function.Function<ClassroomRef, Integer>` as the second parameter, with `CampusService::capacityOf` as the actual argument. | Keeps `SectionChoicePolicy` free of any dependency on the campus package (domain purity). | `src/main/java/enrollment/domain/sections/SectionChoicePolicy.java` |
| A6 | ambiguity | R40 says a number with more than 10 digits is not a valid instruction, but the instruction for `Enroll` takes numbers as arguments. Should classroom numbers that fit in `int` but not a 10-digit `long` be rejected? | All numbers are parsed as unsigned long up to 10 digits; values that fit in `int` are cast with `(int)`. An instruction with a value `> Integer.MAX_VALUE` for a field that needs an `int` would produce an incorrect cast but the documentation gives no upper bounds for classroom numbers — the rule says only that a number is "an unsigned integer of up to 10 digits" (R34). | Consistent with R34; no upper bound is given for classroom or section numbers. | `src/main/java/enrollment/console/Dispatcher.java` |

## Pending

None.

## Documentation I relied on

- `REQUIREMENTS.md`: rules R1–R45, the exact console contract (query formats, error strings), the section 5 example.
- `docs/domain/invariants.md`: which class enforces each invariant — drove where each validation lives.
- `docs/domain/policies.md`: `Permissions` table, `SectionChoicePolicy` algorithm, `GradeScale` ranges — used literally as specification for those three classes.
- `docs/domain/model.puml`: class names, method signatures, relationships — used as the primary API specification.
- `docs/domain/modules.md`: package layout, dependency rule, file locations.
- `docs/domain/glossary.md`: canonical names for every class, method and variable.
- `ARCHITECTURE.md`: the adapter rule (console never contains domain logic), architecture test.
- `docs/domain/decisions.md`: D1–D10 informed the aggregate design and split of responsibilities.

## Verification
`mvn -q verify` passed, 116 tests (115 feature tests + 1 ArchitectureTest).
