# Course Enrollment — requirements (version 1)

A console application that enrolls the students of a School of Engineering in the sections of its courses.

The rules are numbered (R1, R2…) so they can be cited.

## Contents

1. [General rules](#1-general-rules)
2. [Users and permissions](#2-users-and-permissions)
3. [Entities](#3-entities)
4. [Console instructions](#4-console-instructions)
5. [Example](#5-example)
6. [Build and tests](#6-build-and-tests)
7. [Out of scope](#7-out-of-scope)

## 1. General rules

- **R1. Console.** The program reads instructions from standard input, one per line, and writes the responses to standard output. It has no graphical interface and no web server. The console uses UTF-8.
- **R2. In memory.** The data exists while the program runs; nothing is saved to disk.
- **R3. Deterministic output.** The same sequence of instructions always produces the same output.
- **R4. Libraries.** Use the Java standard library, collections included. The program has no other runtime dependency; the tests use JUnit 5.

## 2. Users and permissions

There are three types of user: admin, staff and student.

| Action | Admin | Staff | Student |
|---|:---:|:---:|:---:|
| Create, modify, delete and query users | ✓ | | |
| Create, modify, delete and query buildings, classrooms, courses, students and professors | ✓ | ✓ | |
| Create and query sections | ✓ | ✓ | |
| Enroll a student in a course, change their grades and unenroll them | ✓ | ✓ | |
| Enroll themselves in a course | | | ✓ |

- **R5. Initial admin.** When the program starts there is one admin with ID 1, name `admin` and password `admin`. It cannot be deleted and its type cannot change.
- **R6. Session.** There is one session at a time. Without a session only `Login` and `Exit` are accepted; any other instruction responds `Error: log in first`. While a session is open, `Login` responds `Error` until it is closed.
- **R7. Permissions.** An instruction the user is not allowed to run responds `Error: permission denied` and changes nothing.
- **R8. Order of the checks.** An instruction is checked in this order, and the first check that fails gives the response: (1) it is a valid instruction (R40); (2) a session is open, or the instruction does not need one (R6); (3) the user has permission (R7); (4) the rules of the entities in section 3.

## 3. Entities

| Entity | Key | Order when listed |
|---|---|---|
| User | ID | ascending ID |
| Building | name | creation order |
| Classroom | building and number | ascending number, within its building |
| Course | code | creation order |
| Student | student number | ascending student number |
| Professor | professor ID | ascending professor ID |
| Section | code | ascending code |
| Enrollment | student, within a section | enrollment order, within its section |

- **R9. Keys.** Each key is unique among the elements of its entity; a classroom number is unique within its building. Creating an element with a key that already exists responds `Error` and changes nothing. Keys never change, except the name of a building (R18).
- **R10. References.** An element that another element uses cannot be deleted: the instruction responds `Error` and changes nothing. The rules of each entity say which references there are.

### 3.1 Users

Data: ID (number), name, password, type (`admin`, `staff` or `student`) and, only for the student type, the student number of their student.

- **R11.** The system assigns the ID: one more than the highest ID assigned so far. The IDs of deleted users are not reused.
- **R12.** The name is unique. A user logs in with their name and password.
- **R13.** A user of type student is linked by the student number to a student that exists. Each student has at most one user.
- **R14.** The user that has the session open cannot be deleted.
- **R15.** Modifying a user changes their name, password, type and student number, with the same rules as creating it.

### 3.2 Buildings and classrooms

Data of a building: name. Data of a classroom: number and capacity, the number of students that fit in it (an integer greater than 0).

- **R16.** A classroom is identified by its building and its number. Two buildings may have a classroom with the same number.
- **R17.** Modifying a classroom changes its capacity, which cannot fall below the number of students enrolled in any of its sections.
- **R18.** Modifying a building changes its name, which must not belong to another building.
- **R19.** A building can be deleted only if it has no classrooms. A classroom can be deleted only if no section uses it.

### 3.3 Courses

Data: code, name, semester (from 1 to 10) and credits (an integer greater than 0).

- **R20.** Modifying a course changes its name, semester and credits.
- **R21.** A course can be deleted only if no section uses it.

### 3.4 Students

Data: student number, name and address. Modifying a student changes their name and address.

- **R22.** A student can be deleted only if they have no enrollments and no linked user.

### 3.5 Professors

Data: professor ID, name and address. Modifying a professor changes their name and address.

- **R23.** A professor can be deleted only if no section uses them.

### 3.6 Sections

A section is one offering of a course: a period on one day of the week, in a classroom, taught by a professor. Data: code, period and day, and references to its course, its classroom and its professor.

- **R24. Period.** It has the form `h:mm` followed by `am` or `pm`, a hyphen and another time of the same form; for example, `7:10am-8:00am`. Hours go from 1 to 12 and minutes from 00 to 59; `12:00pm` is noon and `12:00am` is midnight. The start is before the end. It is accepted in any case and stored in lower case.
- **R25. Day.** `monday`, `tuesday`, `wednesday`, `thursday`, `friday`, `saturday` or `sunday`, in any case. It is stored in lower case.
- **R26. References.** The course, the classroom (building and number) and the professor must exist. The section refers to them; it does not copy them: if they are modified, the section shows the new data.
- **R27.** A course can have several sections.

### 3.7 Enrollments

Enrolling a student in a course puts them in one section of that course. An enrollment has its student, its coursework score and its final exam score.

- **R28. Grades.** The coursework goes from 0 to 75 and the final exam from 0 to 25, in integers. The grade is coursework plus final exam, and the student passes with 61 or more.
- **R29. Section of an enrollment.** A student who enrolls in a course goes into the section of that course with the lowest code that has room, that is, whose enrollments are fewer than the capacity of its classroom. If the course has no sections, or none has room, the enrollment is rejected.
- **R30.** A student enrolls in a course only once, in only one of its sections.
- **R31.** A student who enrolls themselves starts with coursework 0 and final exam 0.
- **R32.** Changing the grades changes the coursework and the final exam. Unenrolling removes the enrollment from its section.

## 4. Console instructions

### 4.1 Syntax

Each line is one instruction of the form `Name(value, value, …);`.

- **R33.** The name of the instruction is not case sensitive (`login` and `Login` are the same instruction), and there may be spaces before the parenthesis. The final `;` is required.
- **R34.** A number is an unsigned integer of up to 10 digits, written without quotes.
- **R35.** A text goes between double quotes, straight (`"`) or typographic (`“ ”`), which may be mixed. A text without spaces, commas, parentheses, semicolons or quotes may go without quotes (`G`, `staff`). Spaces around each value are ignored.
- **R36.** Texts that identify something, such as the name of a user or of a building, are case sensitive. The user type and the day are not.
- **R37.** Empty lines are ignored.

### 4.2 Responses

- **R38.** An instruction that creates, modifies or deletes something, or that logs in or out, responds one line: `OK: …` if it was done, or `Error: <reason>` if it was not, and in that case nothing changes.
- **R39.** Queries respond with the formats of section 4.3, or with one line `Error: <reason>`.
- **R40.** A line that is not a valid instruction responds `Error: invalid instruction`. A line is not a valid instruction when its name is unknown, it has the wrong number of values, a value that must be a number is not one (R34), or a parenthesis, a quote or the final `;` is missing. A number out of range, such as semester 11, is a valid instruction that breaks a rule of section 3.
- **R41.** The text after `OK:` and `Error:` is free, except where this document defines it.
- **R42.** If the program shows a prompt before reading each instruction (for example, `> `), it writes it to standard error, so that standard output has only responses.
- **R43.** `Exit();` or the end of the input ends the program with exit code 0.

### 4.3 Instructions

Values in brackets are optional. A, S and St are admin, staff and student.

| Instruction | Who | Responds |
|---|---|---|
| `Login(name, password);` | no session | `OK: …` |
| `Logout();` | A, S, St | `OK: …` |
| `Exit();` | anyone | ends the program |
| `User(name, password, type[, studentNumber]);` | A | `OK: …` (the student number is required for the student type and not allowed for the others) |
| `ModifyUser(id, name, password, type[, studentNumber]);` | A | `OK: …` |
| `DeleteUser(id);` | A | `OK: …` |
| `QueryUser(id);` | A | format below |
| `Building(name);` | A, S | `OK: …` |
| `ModifyBuilding(name, newName);` | A, S | `OK: …` |
| `DeleteBuilding(name);` | A, S | `OK: …` |
| `QueryBuilding(name);` | A, S | format below |
| `Classroom(building, number, capacity);` | A, S | `OK: …` |
| `ModifyClassroom(building, number, capacity);` | A, S | `OK: …` |
| `DeleteClassroom(building, number);` | A, S | `OK: …` |
| `Course(code, name, semester, credits);` | A, S | `OK: …` |
| `ModifyCourse(code, name, semester, credits);` | A, S | `OK: …` |
| `DeleteCourse(code);` | A, S | `OK: …` |
| `QueryCourse(code);` | A, S | format below |
| `Student(studentNumber, name, address);` | A, S | `OK: …` |
| `ModifyStudent(studentNumber, name, address);` | A, S | `OK: …` |
| `DeleteStudent(studentNumber);` | A, S | `OK: …` |
| `QueryStudent(studentNumber);` | A, S | format below |
| `Professor(professorId, name, address);` | A, S | `OK: …` |
| `ModifyProfessor(professorId, name, address);` | A, S | `OK: …` |
| `DeleteProfessor(professorId);` | A, S | `OK: …` |
| `QueryProfessor(professorId);` | A, S | format below |
| `Section(code, period, day, courseCode, classroomNumber, building, professorId);` | A, S | `OK: …` |
| `QuerySection(code);` | A, S | format below |
| `Enroll(studentNumber, courseCode, coursework, finalExam);` | A, S | `OK: <studentNumber> enrolled in course <courseCode> in section <code>` |
| `EnrollMe(courseCode);` | St | the same as `Enroll` |
| `ChangeGrades(studentNumber, courseCode, coursework, finalExam);` | A, S | `OK: …` |
| `Unenroll(studentNumber, courseCode);` | A, S | `OK: …` |

Codes, numbers and IDs are numbers (R34); names, passwords, addresses, periods, days and user types are texts (R35).

Query formats: the data in the order of the instruction that creates the element, separated by ` | `. A user query never shows the password. A building query lists its classrooms in ascending number, each with its capacity in parentheses, or says `no classrooms`. A section query ends with the number of students enrolled and the capacity of its classroom.

```text
QueryUser(1);              →  1 | admin | admin
QueryUser(4);              →  4 | FMarroquín | student | 200145785
QueryBuilding(G);          →  G | 12 (75), 105 (60), 109 (40), 110 (35)
QueryBuilding(D);          →  D | no classrooms
QueryCourse(777);          →  777 | compilers1 | 5 | 4
QueryStudent(200145785);   →  200145785 | Fernando Marroquín | Quetzaltenango
QueryProfessor(12345);     →  12345 | Prof1 | City
QuerySection(1);           →  1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/60
```

These responses come from the data of the example in section 5, right after its first block.

## 5. Example

A session that logs in as the admin and creates some data. The `OK` lines are left out because their text is free (R41).

```text
Login(admin, admin);
Student(200145785, “Fernando Marroquín”, “Quetzaltenango”);
Student(200745781, “Alfonso Antonio”, “Cantel”);
Student(200478542, “Mario Morales”, “La Esperanza”);
User(Secretary, “murphy1”, staff);
User(Control, “control123”, staff);
User(FMarroquín, “fffmmm”, student, 200145785);
Professor(12345, “Prof1”, “City”);
Professor(47854, “Prof2”, “City”);
Building(“G”);
Building(“D”);
Classroom(“G”, 12, 75);
Classroom (“G”, 105, 60);
Classroom(“G”, 109, 40);
Classroom(“G”, 110, 35);
Course (777,"compilers1", 5,4);
Course (771,"programming2", 4, 4);
Section(1,"7:10am-8:00am","monday",777,105,G,12345);
Section(2,"8:00am-8:50am","monday",777,105,G,12345);
Section(5,"7:10am-8:00am","tuesday",771,109,G,47854);
Enroll (200145785, 777, 30, 25);
Enroll (200745781, 777, 60, 10);
Enroll (200478542, 771, 70, 20);
```

After it, both students of course 777 are in section 1, and the student of course 771 is in section 5. The session goes on like this (each instruction followed by its response; `OK: …` is a free text):

```text
Enroll (200478542, 777, 0, 0);
OK: 200478542 enrolled in course 777 in section 1
DeleteCourse(777);
Error: course 777 has sections
Logout();
OK: …
QueryCourse(777);
Error: log in first
Login(FMarroquín, fffmmm);
OK: …
QueryCourse(777);
Error: permission denied
EnrollMe(771);
OK: 200145785 enrolled in course 771 in section 5
```

The reasons after `Error:` are examples; only `log in first`, `permission denied` and `invalid instruction` are fixed (R6, R7, R40).

## 6. Build and tests

- **R44.** The project uses Java 21 and Maven. `mvn -q package` builds `target/enrollment.jar`, and `java -jar target/enrollment.jar` starts the console. The main class is `enrollment.Main`.
- **R45.** There are unit tests with JUnit 5 for the rules of this document, and `mvn verify` ends without errors.

## 7. Out of scope

- A graphical or web interface.
- Saving the data to disk.
- Loading instructions from a file, and reports.
- Modifying or deleting sections.
- Checking that two sections do not overlap in the same classroom, with the same professor or for the same student.
- Course prerequisites, a credit limit per student, and password encryption.
