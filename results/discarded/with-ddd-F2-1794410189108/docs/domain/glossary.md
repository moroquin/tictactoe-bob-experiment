# Glossary — the ubiquitous language

Use these names in code, tests and docs. A discarded term in the code is a bug. When you need a name that is not here, add the term to this glossary first.

## Enrollment (core)

| Term | Meaning | Name in code | Discarded |
|---|---|---|---|
| Section | One offering of a Course: a Period on a Day, in a Classroom, taught by a Professor. It holds its Enrollments. | `Section` | Schedule, Timetable, Slot |
| Section code | The number that identifies a Section. | `SectionCode` | SectionId |
| Period | The start and end time of a Section, such as `7:10am-8:00am`. A value object; the start is before the end. | `Period` | TimeRange, Interval |
| Time of day | One of the two ends of a Period, from `12:00am` (midnight) to `11:59pm`. | `TimeOfDay` | Clock |
| Day | A day of the week, from `monday` to `sunday`. | `Day` | Weekday |
| Enrollment | A Student in a Section, with their Grades. A Student has at most one Enrollment per Course. | `Enrollment` | Assignment, Registration, Inscription |
| Enroll | Put a Student into a Section of a Course chosen by the Section choice policy. | `enroll` | assign, register, signUp |
| Unenroll | Remove an Enrollment from its Section. | `unenroll` | unassign |
| Section choice policy | The rule that picks the Section of a new Enrollment among the Sections of the Course. See `policies.md`. | `SectionChoicePolicy` | AssignmentStrategy, Allocator |
| Room | A Section has Room when its Enrollments are fewer than the Capacity of its Classroom. | `hasRoom` | quota, vacancy |
| Grades | The Coursework and the Final exam of an Enrollment. A value object. | `Grades` | Scores, Marks |
| Coursework | The part of the Grades earned during the term. | `coursework` | zone, zona |
| Final exam | The part of the Grades earned in the final exam. | `finalExam` | — |
| Grade | Coursework plus Final exam. | `grade()` | note |
| Grade scale | The ranges of Coursework and Final exam and the Passing grade. See `policies.md`. | `GradeScale` | — |
| Passing grade | The lowest Grade that passes. | `passingGrade` | — |
| Passed, Failed | Whether the Grade reaches the Passing grade. | `passed()` | approved, reproved |

## Catalog, campus and people (supporting)

| Term | Meaning | Name in code | Discarded |
|---|---|---|---|
| Course | A subject the School teaches. It has a Course code, a name, a Semester and Credits, and may have several Sections. | `Course` | Subject, Asignatura |
| Course code | The number that identifies a Course. | `CourseCode` | CourseId |
| Semester | The semester of the program a Course belongs to, from 1 to 10. | `Semester` | — |
| Credits | What a Course is worth, an integer greater than 0. | `credits` | units |
| Building | A building of the School. It holds its Classrooms. | `Building` | Edifice |
| Classroom | A room in a Building, identified by its Building and its number, with a Capacity. | `Classroom` | Salon, Hall |
| Capacity | The number of Students that fit in a Classroom, an integer greater than 0. | `capacity` | seats |
| Student | A person who studies at the School, identified by a Student number. | `Student` | Pupil, Alumno |
| Student number | The number that identifies a Student. | `StudentNumber` | Carnet, StudentId |
| Professor | A person who teaches Sections, identified by a Professor ID. | `Professor` | Teacher, Instructor, Catedratico |
| Professor ID | The number that identifies a Professor. | `ProfessorId` | TeacherId |

## Access (supporting)

| Term | Meaning | Name in code | Discarded |
|---|---|---|---|
| User | Someone who can log in: an ID, a name, a password and a User type. A User of type student is linked to one Student. | `User` | Account |
| User type | `admin`, `staff` or `student`. | `UserType` | Role |
| Admin | The User type that can do everything; there is always the initial admin with ID 1. | `UserType.ADMIN` | SuperUser |
| Staff | The User type that manages the data of the School but not the Users. | `UserType.STAFF` | Collaborator |
| Session | The User currently logged in. There is at most one. | `Session` | CurrentUser |
| Permission | What a User type may do. See `policies.md`. | `Permission` | Privilege |

## Console (adapter)

| Term | Meaning | Name in code | Discarded |
|---|---|---|---|
| Instruction | One line of input, such as `Enroll(200145785, 777, 30, 25);`. | `Instruction` | Command |
| Response | What the program writes for an Instruction: `OK: …`, `Error: …` or the lines of a query. | `Response` | Answer |
