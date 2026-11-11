# Invariants

Rules that must always hold. Each one cites the rule of `REQUIREMENTS.md` it comes from and says who enforces it. Tests cite the invariant they protect in their name, for example `I19_aStudentEnrollsInACourseOnlyOnce`.

| # | Invariant | From | Enforced by |
|---|---|---|---|
| I1 | Each key is unique among the elements of its entity; a Classroom number is unique within its Building. | R9 | The repositories (`add` rejects a duplicate key) and `Building.addClassroom` |
| I2 | An element that another one uses cannot be deleted: a Building with Classrooms, a Classroom, Course or Professor used by a Section, a Student with Enrollments or a linked User. | R10, R19, R21, R22, R23 | The application service that deletes, which asks the repositories who uses the element |
| I3 | The initial admin (ID 1, `admin`/`admin`) always exists and is always an admin. | R5 | `AccessService` |
| I4 | There is at most one Session. Without a Session only `Login` and `Exit` run; with one, `Login` is rejected. | R6 | `Session` and `AccessService` |
| I5 | A User runs only what their User type allows. | R7 | `Permissions`, asked by every application service before anything else |
| I6 | The checks run in the order of R8: valid Instruction, Session, Permission, rules of the entities. | R8 | `enrollment.console` checks the syntax; then every application service checks Session, Permission and rules, in that order |
| I7 | User IDs are assigned in ascending order and never reused. | R11 | `UserRepository.nextId()` |
| I8 | User names are unique. | R12 | `AccessService` |
| I9 | A User of type student is linked to an existing Student, and a Student has at most one User. Other types have no Student. | R13, R15 | `AccessService` |
| I10 | The User of the open Session cannot be deleted. | R14 | `AccessService` |
| I11 | A Classroom's Capacity is greater than 0 and never lower than the Enrollments of any Section that uses it. | 3.2, R17 | `Classroom` (greater than 0) and `CampusService` (against the Sections) |
| I12 | Building names are unique, also after renaming. | R18 | `CampusService` |
| I13 | A Course's Semester is from 1 to 10 and its Credits greater than 0. | 3.3 | `Semester` and `Course` |
| I14 | A Period follows R24 and its start is before its end. | R24 | `Period.parse` and `TimeOfDay` |
| I15 | A Day is one of the seven days of the week. | R25 | `Day.parse` |
| I16 | A Section refers to a Course, a Classroom and a Professor that exist, by their identity, never by a copy. | R26 | `SectionService` when it creates the Section |
| I17 | The Grades of every Enrollment are inside the Grade scale. | R28 | `GradeScale.grades(coursework, finalExam)`, the only way to build `Grades` |
| I18 | A new Enrollment goes into the Section the Section choice policy picks, and a Section never has more Enrollments than the Capacity of its Classroom. | R29 | `SectionChoicePolicy` and `Section.enroll` |
| I19 | A Student has at most one Enrollment per Course, in only one of its Sections. | R30 | `EnrollmentService` |
| I20 | A Student who enrolls themselves starts with Coursework 0 and Final exam 0. | R31 | `EnrollmentService.enrollMe` |
| I21 | An Instruction that fails changes nothing. | R38 | Every application service: it checks every rule first and changes the aggregates only at the end |
