# Modules

## Bounded contexts

One application with one core and three supporting areas:

- **Enrollment (core):** Sections and their Enrollments, the Section choice policy and the Grade scale. Most change requests land here.
- **Catalog, campus and people (supporting):** Courses, Buildings with their Classrooms, Students and Professors. Simple data with keys and references.
- **Access (supporting):** Users, the Session and the Permissions.

## Packages and the dependency rule

`ARCHITECTURE.md` fixes `enrollment` (`Main`) and `enrollment.console`, and leaves the rest free. The domain organizes the rest like this:

| Package | Holds | May depend on |
|---|---|---|
| `enrollment.domain` | `RuleViolation`, the exception every layer uses to report a broken rule with its reason | Only the JDK |
| `enrollment.domain.*` | The aggregates, value objects, policies and repository interfaces of `model.puml`, one subpackage per area: `access`, `campus`, `catalog`, `people`, `sections` | Only the JDK and other `enrollment.domain` packages |
| `enrollment.application` | One application service per area: `AccessService`, `CampusService`, `CatalogService`, `PeopleService`, `SectionService`, `EnrollmentService`. Each use case checks Session, Permission and rules in the order of `policies.md`, then changes the aggregates. Cross-aggregate rules (references, Capacity against Enrollments, one Enrollment per Course) live here. | `enrollment.domain` |
| `enrollment.infrastructure` | In-memory repositories, one per repository interface, built on `java.util` collections | `enrollment.domain` |
| `enrollment.console` | Parsing Instructions (R33–R37, R40), calling the application services, and formatting the Responses and the query formats of `REQUIREMENTS.md` (see `ARCHITECTURE.md`) | `enrollment.application`, and the domain types the services return |
| `enrollment` | `Main`: it creates the repositories, the services and the console, and starts it | All |

The domain and the application services report a broken rule with `RuleViolation`, which carries the reason; the console turns it into `Error: <reason>`.

## File map

Where each class lives. Read only the files of the classes you change and of the classes they call directly. Keep this table up to date: a class that is not here does not exist for the next session. The files do not exist yet; version 1 creates them here.

| Class | File | Tests |
|---|---|---|
| `User`, `UserType`, `Session`, `Action`, `Permissions`, `UserRepository` | `src/main/java/enrollment/domain/access/` | `src/test/java/enrollment/domain/access/` |
| `Building`, `BuildingId`, `Classroom`, `ClassroomRef`, `BuildingRepository` | `src/main/java/enrollment/domain/campus/` | `src/test/java/enrollment/domain/campus/` |
| `Course`, `CourseCode`, `Semester`, `CourseRepository` | `src/main/java/enrollment/domain/catalog/` | `src/test/java/enrollment/domain/catalog/` |
| `Student`, `StudentNumber`, `Professor`, `ProfessorId`, `StudentRepository`, `ProfessorRepository` | `src/main/java/enrollment/domain/people/` | `src/test/java/enrollment/domain/people/` |
| `Section`, `SectionCode`, `Period`, `TimeOfDay`, `Day`, `Enrollment`, `Grades`, `GradeScale`, `SectionChoicePolicy`, `SectionRepository` | `src/main/java/enrollment/domain/sections/` | `src/test/java/enrollment/domain/sections/` |
| `RuleViolation` | `src/main/java/enrollment/domain/RuleViolation.java` | — |
| Application services | `src/main/java/enrollment/application/` | `src/test/java/enrollment/application/` |
| In-memory repositories | `src/main/java/enrollment/infrastructure/` | — |
| Parser, dispatcher, formatter and console loop | `src/main/java/enrollment/console/` | `src/test/java/enrollment/console/` |
| `Main` | `src/main/java/enrollment/Main.java` | — |

When a folder has several files, list each new class with its file in this table.

## Where changes go

- **A rule that a policy holds** (who may do what, how a Section is chosen, the grade ranges and the Passing grade): change the policy in `policies.md`, then the policy class and its tests. The services that use it do not change.
- **A new rule about one aggregate:** the aggregate and its invariant in `invariants.md`.
- **A new rule that crosses aggregates** (references, limits that count Enrollments): the application service of the use case, with its invariant.
- **A new Instruction:** the parser and the dispatcher in `enrollment.console`, a use case in the application service of its area, and the domain only if a rule of the domain changes.
- **A new query or report format:** the formatter in `enrollment.console`, reading through the application services.
- **A new kind of storage:** a new implementation of the repository interfaces in `enrollment.infrastructure`; nothing else changes.
