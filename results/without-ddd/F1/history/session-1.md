# Session history

## Task
Implement version 1 of the course enrollment console system as described in REQUIREMENTS.md, with unit tests, such that `mvn -q verify` passes and `java -jar target/enrollment.jar` follows the console contract exactly.

## Timing
- Start: 2026-11-11T06:12:56Z
- End: 2026-11-11T06:17:10Z

## token use
 Agent Mode · 57.6k / 270.0k (21%) · 2.36 🅞

## Result
Done. `mvn -q verify` passes (all tests green, ArchUnit rules satisfied) and the console output for the section-5 example matches the requirements exactly.

## Decisions I made on my own

| ID | Type | Question I would have asked | What I decided | Why | Where |
|---|---|---|---|---|---|
| A1 | design | How should the store, service, and console be structured without DDD? | Three packages: `enrollment.store` (Store), `enrollment.service` (EnrollmentService + Result), `enrollment.console` (Parser + Console). The store holds raw collections; the service holds all business rules; the console is a pure adapter. | Matches ARCHITECTURE.md's "console is an adapter" and "rules are not decided there". | `src/main/java/enrollment/` |
| A2 | design | What collection type to use for buildings (insertion order required by spec for listing)? | `LinkedHashMap` keyed by name for buildings, courses, students, professors, sections; `ArrayList` for classrooms within a building; `LinkedHashMap` for users by ID. | The spec says buildings and courses list in "creation order", students/professors/sections in "ascending key" order — ascending-key ordering is handled at query time by sorting. Insertion order in the map is irrelevant for those; I used LinkedHashMap uniformly for consistency. | `enrollment.store.Store` |
| A3 | ambiguity | R24: what constitutes a valid period — must hours be exactly 1–12? | Regex `(1[0-2]|[1-9])` enforces hours 1–12; minutes `[0-5][0-9]` enforce 00–59. Leading zeros on hours are not accepted (e.g. `07:10am` would fail), matching common 12-hour clock convention. | The spec says "Hours go from 1 to 12" without explicit mention of leading zeros; rejecting leading zeros is the simplest interpretation. | `enrollment.service.EnrollmentService.PERIOD_RE` |
| A4 | design | R40: numbers out of range (e.g. semester 11) should be "valid instruction that breaks a rule" — where to validate range? | Range validation (semester 1–10, credits > 0, capacity > 0, grades 0–75/0–25) is done in the service, not the parser. The parser only checks structural syntax. | R40 explicitly says "a number out of range … is a valid instruction that breaks a rule of section 3", so the parser must not reject it. | `enrollment.console.Parser`, `enrollment.service.EnrollmentService` |
| A5 | gap | R41: free text after `OK:` for mutating operations — what text? | Used descriptive messages like "logged in as admin", "student 200145785 created", etc. The exact wording does not matter (R41). | R41 says free. | All `Result.ok(...)` calls in `EnrollmentService` |
| A6 | design | How to handle `QueryBuilding` classrooms order vs. insertion? | Sort by classroom number at query time using `Comparator.comparingLong(Classroom::getNumber)`. | R spec: "ascending number, within its building". | `enrollment.service.EnrollmentService.queryBuilding` |
| A7 | gap | R35: typographic open-quote `"` — what is the matching close quote? | Close with `"` (U+201D). Also accept straight `"` as close (mixed, per R35). | R35 says "straight or typographic … which may be mixed". | `enrollment.console.Parser.parseArgs` |

## Pending

None.

## Documentation I relied on

- `REQUIREMENTS.md`: All rules (R1–R45), entity definitions, console syntax, query formats, section-5 example, build requirements.
- `ARCHITECTURE.md`: Package structure, console-as-adapter rule, dependency constraints.
- `src/test/java/enrollment/ArchitectureTest.java`: Executable architecture constraints (console/standard-stream rules).
- `pom.xml`: Java 21, Maven, JUnit 5, ArchUnit, JaCoCo, PIT versions and configuration.

## Verification

`mvn -q verify` passed; 37 tests (EnrollmentServiceTest + ParserTest + ArchitectureTest) all green. Console output for the section-5 example matches REQUIREMENTS.md exactly (verified by piping `target/manual-test.txt` into `java -jar target/enrollment.jar`).
