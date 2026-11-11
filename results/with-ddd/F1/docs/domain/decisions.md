# Decision log

Append new decisions at the end. Never rewrite or delete an entry; when a decision changes, add a new entry that says which one it replaces.

| # | Decision | Why |
|---|---|---|
| D1 | The domain is split into aggregates: User, Building (with its Classrooms), Course, Student, Professor and Section (with its Enrollments). | Each aggregate protects its own invariants; the Enrollments of a Section are what its Capacity limits. |
| D2 | Between aggregates, references are by identity (Course code, `ClassroomRef`, Professor ID, Student number), never by object. | A modified Course or Professor shows its new data everywhere (R26) without copies to keep in sync. |
| D3 | A Building has an internal `BuildingId` that never changes, and `ClassroomRef` uses it. The name is only what people type and see. | The name of a Building can change (R18); Sections must keep pointing to the same Classroom. |
| D4 | The rules most likely to change are policy objects: `Permissions`, `SectionChoicePolicy` and `GradeScale` (`policies.md`). | A change to one of those rules should touch one class and its tests. |
| D5 | Cross-aggregate rules (references that block deletion, Capacity against Enrollments, one Enrollment per Course) live in the application services, which ask the repositories. | No aggregate can see the others; the use case can. |
| D6 | Application services check everything first and change the aggregates last (I21). | An Instruction that fails must change nothing (R38), without undo logic. |
| D7 | Repositories are interfaces in the domain and in-memory classes in `enrollment.infrastructure`, built on `java.util` collections. | The data lives in memory (R2); the domain does not depend on how it is stored. |
| D8 | The application services are the only entry point the console uses: one method per use case, returning domain objects for the console to format. | The console stays an adapter (`ARCHITECTURE.md`), and every use case checks Session, Permission and rules in one place. |
| D9 | A broken rule is reported with `RuleViolation` (in `enrollment.domain`) and its reason; the console writes `Error: <reason>`. Missing Session and missing Permission have fixed reasons (R6, R7). | One way to report errors, usable from value objects and services alike. |
| D10 | The docs drive the development: an agent learns the domain from `docs/domain/` and reads only the code files the file map in `modules.md` names. Every design choice is recorded here and in the model. | A new session, without memory, should not need to read the whole codebase to understand the design. |
| D11 | `SectionRepository` has an extra query `enrolledSection(StudentNumber, CourseCode): Optional<Section>` not in the original model. | `EnrollmentService` needs to find whether a student is already enrolled in a course (I19) and which section to call `changeGrades`/`unenroll` on (R32). A linear scan of all sections in the service would repeat in three places; one repository query is cleaner. Added to `model.puml` and `modules.md`. |
| D12 | `Building` exposes `findClassroom(int): Classroom` (package-visible helper). | `SectionService` needs to validate that the classroom exists when creating a section (I16), and `CampusService` needs it to modify/delete classrooms. Adding it to the aggregate avoids exposing the internal list. Added to `model.puml`. |
| D13 | `AccessService` exposes `session()` and `permissions()` accessors. | Other services need them to validate the session and construct the initial admin from `Main` without adding direct dependencies on the Session or Permissions objects. |
| D14 | `ConsoleLoop.handleLine` is package-private to allow direct unit testing without a reader. | Enables simpler tests that call one line at a time; no other class uses it. |
| D15 | The `Dispatcher` handles `Exit()` by returning `null`; `ConsoleLoop` treats `null` as the exit signal. | Keeps the exit logic in one place (the loop) without a separate checked exception path. |
