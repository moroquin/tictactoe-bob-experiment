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

`Login` and `Exit` need no Session; `Logout` needs one and is allowed to every User type.

## Section choice — `SectionChoicePolicy`

Picks the Section of a new Enrollment among the Sections of the Course (I18, R29): the Section with the lowest Section code among those that have Room. A Section has Room when its Enrollments are fewer than the Capacity of its Classroom. If no Section has Room, there is no choice and the enrollment is rejected.

`SectionChoicePolicy.choose(sections, capacityOf)` receives the Sections of the Course and a way to know the Capacity of each one's Classroom, and returns the chosen Section or nothing. It does not change anything.

## Grade scale — `GradeScale`

| | From | To |
|---|---|---|
| Coursework | 0 | 75 |
| Final exam | 0 | 25 |
| Passing grade | 61 | |

`GradeScale.grades(coursework, finalExam)` validates the ranges and builds the `Grades`; `Grades.grade()` is the sum; `GradeScale.passed(grades)` compares it with the Passing grade (I17, R28). Nothing else knows these numbers.

## Order of the checks

1. `enrollment.console` parses the line. If it is not a valid Instruction (R40): `Error: invalid instruction`.
2. The application service checks the Session (I4): `Error: log in first`.
3. It checks the Permission (I5): `Error: permission denied`.
4. It checks every rule of the entities, in the order the use case lists them. The first that fails gives `Error: <reason>`.
5. Only then it changes the aggregates (I21).
