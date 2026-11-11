# Policies

The rules most likely to change live in one place each, as policy objects in the domain. To change one of these rules, change its policy and its tests, not the services that use it.

## Permissions — `Permissions`

What each User type may do. Every application service asks `Permissions.allows(userType, action)` before it checks any rule of the entities (I5, I6).

| Action | Admin | Staff | Student |
|---|:---:|:---:|:---:|
| `MANAGE_USERS`: create, modify, delete and query Users | ✓ | | |
| `MANAGE_SCHOOL_DATA`: create, modify, delete and query Buildings, Classrooms, Courses, Students and Professors | ✓ | ✓ | |
| `MANAGE_SECTIONS`: create, modify, delete and query Sections | ✓ | ✓ | |
| `MANAGE_ENROLLMENTS`: enroll a Student, change Grades and unenroll | ✓ | ✓ | |
| `ENROLL_SELF`: enroll themselves | | | ✓ |
| `QUERY_ENROLLMENTS`: StudentCourses report; a Student may only query their own student number | ✓ | ✓ | ✓ |
| `LOAD_FILE`: run instructions from a file | ✓ | | |

`Login` and `Exit` need no Session; `Logout` needs one and is allowed to every User type.

## Section choice — `SectionChoicePolicy`

Picks the Section of a new Enrollment among the Sections of the Course (I18, I35, CR-3, CR-6): the Section with the **fewest enrollments** among those that have Room **and** do not overlap any Section the Student is already enrolled in; if several tie on enrollment count, the one with the **lowest Section code**. A Section has Room when its Enrollments are fewer than the Capacity of its Classroom. If no such Section exists (or the Course has no Sections), there is no choice and the enrollment is rejected.

`SectionChoicePolicy.choose(sections, capacityOf, enrolledSections)` receives the Sections of the Course, a way to know the Capacity of each one's Classroom, and the list of Sections the Student is already enrolled in. It returns the chosen Section or nothing. It does not change anything.

## Grade scale — `GradeScale`

| | From | To |
|---|---|---|
| Coursework | 0 | 70 |
| Final exam | 0 | 30 |
| Passing grade | 65 | |

`GradeScale.grades(coursework, finalExam)` validates the ranges and builds the `Grades`; `Grades.grade()` is the sum; `GradeScale.passed(grades)` compares it with the Passing grade (I17, R28). Nothing else knows these numbers. (CR-4 replaces R28.)

## Order of the checks

1. `enrollment.console` parses the line. If it is not a valid Instruction (R40): `Error: invalid instruction`.
2. The application service checks the Session (I4): `Error: log in first`.
3. It checks the Permission (I5): `Error: permission denied`.
4. It checks every rule of the entities, in the order the use case lists them. The first that fails gives `Error: <reason>`.
5. Only then it changes the aggregates (I21).

### Order of the checks when enrolling (CR-5, CR-6, I33)

`Enroll`, `EnrollMe` and `Enroll` lines in a loaded file check in this order:
1. The Student and the Course exist.
2. The Student is not already enrolled in the Course.
3. The Grades are in range (`Enroll` only).
4. The Student has passed every Prerequisite of the Course (I31).
5. Enrolling would not take the Student above 24 Credits (I32).
6. A Section with Room that does not overlap the Student's existing Sections exists (I18, I35).
