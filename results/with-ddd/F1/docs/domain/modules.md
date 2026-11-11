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

Where each class lives. Read only the files of the classes you change and of the classes they call directly. Keep this table up to date: a class that is not here does not exist for the next session.

| Class | File | Tests |
|---|---|---|
| `User` | `src/main/java/enrollment/domain/access/User.java` | `src/test/java/enrollment/domain/access/` |
| `UserType` | `src/main/java/enrollment/domain/access/UserType.java` | — |
| `Session` | `src/main/java/enrollment/domain/access/Session.java` | — |
| `Action` | `src/main/java/enrollment/domain/access/Action.java` | — |
| `Permissions` | `src/main/java/enrollment/domain/access/Permissions.java` | `src/test/java/enrollment/domain/access/PermissionsTest.java` |
| `UserRepository` | `src/main/java/enrollment/domain/access/UserRepository.java` | — |
| `Building` | `src/main/java/enrollment/domain/campus/Building.java` | `src/test/java/enrollment/domain/campus/BuildingTest.java` |
| `BuildingId` | `src/main/java/enrollment/domain/campus/BuildingId.java` | — |
| `Classroom` | `src/main/java/enrollment/domain/campus/Classroom.java` | — |
| `ClassroomRef` | `src/main/java/enrollment/domain/campus/ClassroomRef.java` | — |
| `BuildingRepository` | `src/main/java/enrollment/domain/campus/BuildingRepository.java` | — |
| `Course` | `src/main/java/enrollment/domain/catalog/Course.java` | — |
| `CourseCode` | `src/main/java/enrollment/domain/catalog/CourseCode.java` | — |
| `Semester` | `src/main/java/enrollment/domain/catalog/Semester.java` | `src/test/java/enrollment/domain/catalog/SemesterTest.java` |
| `CourseRepository` | `src/main/java/enrollment/domain/catalog/CourseRepository.java` | — |
| `Student` | `src/main/java/enrollment/domain/people/Student.java` | — |
| `StudentNumber` | `src/main/java/enrollment/domain/people/StudentNumber.java` | — |
| `Professor` | `src/main/java/enrollment/domain/people/Professor.java` | — |
| `ProfessorId` | `src/main/java/enrollment/domain/people/ProfessorId.java` | — |
| `StudentRepository` | `src/main/java/enrollment/domain/people/StudentRepository.java` | — |
| `ProfessorRepository` | `src/main/java/enrollment/domain/people/ProfessorRepository.java` | — |
| `Section` | `src/main/java/enrollment/domain/sections/Section.java` | `src/test/java/enrollment/domain/sections/SectionTest.java` |
| `SectionCode` | `src/main/java/enrollment/domain/sections/SectionCode.java` | — |
| `Period` | `src/main/java/enrollment/domain/sections/Period.java` | `src/test/java/enrollment/domain/sections/PeriodTest.java` |
| `TimeOfDay` | `src/main/java/enrollment/domain/sections/TimeOfDay.java` | `src/test/java/enrollment/domain/sections/TimeOfDayTest.java` |
| `Day` | `src/main/java/enrollment/domain/sections/Day.java` | `src/test/java/enrollment/domain/sections/DayTest.java` |
| `Enrollment` | `src/main/java/enrollment/domain/sections/Enrollment.java` | — |
| `Grades` | `src/main/java/enrollment/domain/sections/Grades.java` | — |
| `GradeScale` | `src/main/java/enrollment/domain/sections/GradeScale.java` | `src/test/java/enrollment/domain/sections/GradeScaleTest.java` |
| `SectionChoicePolicy` | `src/main/java/enrollment/domain/sections/SectionChoicePolicy.java` | `src/test/java/enrollment/domain/sections/SectionChoicePolicyTest.java` |
| `SectionRepository` | `src/main/java/enrollment/domain/sections/SectionRepository.java` | — |
| `RuleViolation` | `src/main/java/enrollment/domain/RuleViolation.java` | — |
| `AccessService` | `src/main/java/enrollment/application/AccessService.java` | `src/test/java/enrollment/application/AccessServiceTest.java` |
| `CampusService` | `src/main/java/enrollment/application/CampusService.java` | `src/test/java/enrollment/application/CampusServiceTest.java` |
| `CatalogService` | `src/main/java/enrollment/application/CatalogService.java` | — |
| `PeopleService` | `src/main/java/enrollment/application/PeopleService.java` | — |
| `SectionService` | `src/main/java/enrollment/application/SectionService.java` | — |
| `EnrollmentService` | `src/main/java/enrollment/application/EnrollmentService.java` | `src/test/java/enrollment/application/EnrollmentServiceTest.java` |
| `InMemoryUserRepository` | `src/main/java/enrollment/infrastructure/InMemoryUserRepository.java` | — |
| `InMemoryBuildingRepository` | `src/main/java/enrollment/infrastructure/InMemoryBuildingRepository.java` | — |
| `InMemoryCourseRepository` | `src/main/java/enrollment/infrastructure/InMemoryCourseRepository.java` | — |
| `InMemoryStudentRepository` | `src/main/java/enrollment/infrastructure/InMemoryStudentRepository.java` | — |
| `InMemoryProfessorRepository` | `src/main/java/enrollment/infrastructure/InMemoryProfessorRepository.java` | — |
| `InMemorySectionRepository` | `src/main/java/enrollment/infrastructure/InMemorySectionRepository.java` | — |
| `InstructionParser` | `src/main/java/enrollment/console/InstructionParser.java` | `src/test/java/enrollment/console/InstructionParserTest.java` |
| `Dispatcher` | `src/main/java/enrollment/console/Dispatcher.java` | — |
| `ResponseFormatter` | `src/main/java/enrollment/console/ResponseFormatter.java` | — |
| `ConsoleLoop` | `src/main/java/enrollment/console/ConsoleLoop.java` | `src/test/java/enrollment/console/ConsoleIntegrationTest.java` |
| `Main` | `src/main/java/enrollment/Main.java` | — |

## Where changes go

- **A rule that a policy holds** (who may do what, how a Section is chosen, the grade ranges and the Passing grade): change the policy in `policies.md`, then the policy class and its tests. The services that use it do not change.
- **A new rule about one aggregate:** the aggregate and its invariant in `invariants.md`.
- **A new rule that crosses aggregates** (references, limits that count Enrollments): the application service of the use case, with its invariant.
- **A new Instruction:** the parser and the dispatcher in `enrollment.console`, a use case in the application service of its area, and the domain only if a rule of the domain changes.
- **A new query or report format:** the formatter in `enrollment.console`, reading through the application services.
- **A new kind of storage:** a new implementation of the repository interfaces in `enrollment.infrastructure`; nothing else changes.
