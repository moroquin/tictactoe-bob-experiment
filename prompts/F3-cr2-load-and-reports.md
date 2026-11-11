Change request CR-2: loading a file and reports

The admin can now load instructions from a file, and there are four console reports. The rest of REQUIREMENTS.md still holds.

## Loading a file

- New instruction `Load(path);`, for the admin only. It reads a UTF-8 text file with one instruction per line, with the syntax of section 4.1, and runs them with the admin's permissions. A relative path is relative to the folder where the program was started.
- The file only accepts the instructions that create: `User`, `Building`, `Classroom`, `Course`, `Student`, `Professor`, `Section` and `Enroll`. Any other instruction is an error of that line.
- The lines run in order and with the same rules as in the console. What a line refers to must exist before it: created by an earlier line or before the load.
- A line with an error does not stop the load: the program writes `Error in line <n>: <reason>` and goes on with the next one. Lines are counted from 1, empty lines included. Correct lines write nothing, and empty lines count neither as correct nor as errors.
- At the end the program writes `OK: load finished, <c> correct and <e> with errors`. If the file cannot be read, it responds only `Error: <reason>`.

Example. With a new session of the admin, this file:

```text
Student(200145785, “Fernando Marroquín”, “Quetzaltenango”);
Student(200745781, “Alfonso Antonio”, “Cantel”);
Student(200478542, “Mario Morales”, “La Esperanza”);
Student(201012362, “Ulises López”, “Salcajá”);

User(Secretary, “murphy1”, staff);
User(Control, “control123”, staff);
User(FMarroquín, “fffmmm”, student, 200145785);
User(Ulises, “uliuli”, student, 201012362);

Professor(12345, “Prof1”, “City”);
Professor(47854, “Prof2”, “City”);
Professor(12445, “Prof3”, “City”);
Professor(47354, “Prof4”, “City”);
Professor(12365, “Prof5”, “City”);

Building(“G”);
Building(“E”);
Building(“D”);

Classroom(“G”, 12, 75);
Classroom (“E”, 10, 25);
CLASSROOM (“D”, 5, 125);
Classroom(“G”, 105, 60);
Classroom(“G”, 109, 40);
Classroom(“G”, 110, 35);

Course (777,"compilers1", 5,4);
Course (771,"programming2", 4, 4);
Course (779,"Computer architecture and assemblers 2", 7, 4);
Course (732,"Statistics 1”, 4, 4);
Course (795,"Formal languages", 4, 4);

Section(1,"7:10am-8:00am","monday",777,105,G,12345);
Section(2,"8:00am-8:50am","monday",777,105,G,12345);
Section(3,"7:10am-8:00am","friday",777,105,G,47854);
Section(4,"8:00am-8:50am","friday",777,105,G,47854);
Section(5,"7:10am-8:00am","tuesday",771,109,G,12345);
Section(6,"8:00am-8:50am","tuesday",771,109,G,12345);
Section(7,"7:10am-8:00am","thursday",771,109,G,47854);
Section(8,"8:00am-8:50am","thursday",771,109,G,47854);
Section(9,"9:10am-10:00am","tuesday",779,110,G,12445);
Section(10,"10:00am-10:50am","tuesday",779,110,G,12445);
Section(11,"9:10am-10:00am","thursday",779,110,G,12365);
Section(12,"10:00am-10:50am","thursday",779,110,G,12365);

Enroll (200145785, 777, 30, 25);
Enroll (200745781, 777, 60, 10);
Enroll (200478542, 771, 70, 20);
```

responds:

```text
OK: load finished, 42 correct and 0 with errors
```

After this load, both students of 777 are in section 1, and the student of 771 is in section 5.

Example with errors. With no data loaded yet, this file:

```text
Building("G");
Classroom("Z", 1, 30);
Professor(12345, "Prof1", "City";
Course(777, "Compilers 1", 11, 4);

Student(200145785, "Fernando Marroquín", "Quetzaltenango");
Student(200145785, "Other", "Xela");
```

responds (the text of each reason is free):

```text
Error in line 2: building Z does not exist
Error in line 3: invalid instruction
Error in line 4: the semester must be between 1 and 10
Error in line 7: student 200145785 already exists
OK: load finished, 2 correct and 4 with errors
```

## Reports

Each report writes one line per result, in the order given. If there are no results, it writes the line given for that case. If the student, the course or the classroom asked for does not exist, the report responds `Error: <reason>`.

1. `StudentCourses(studentNumber);`, for admin, staff and students: the courses the student is enrolled in. A student may only ask for their own student number; with another one the response is `Error: permission denied`. One line per enrollment, in ascending section code:
   `courseCode | course name | section <code> | coursework <c> | final <f> | passed` (or `failed`).
   No results: `No courses`.
2. `CourseStudents(courseCode);`, for admin and staff: the students enrolled in the course. One line per enrollment, in ascending section code and, within a section, in enrollment order:
   `studentNumber | name | section <code> | coursework <c> | final <f> | passed` (or `failed`).
   No results: `No students`.
3. `ClassroomSections(building, number);`, for admin and staff: the sections that use the classroom. One line per section, in ascending code:
   `section <code> | day | period | courseCode | course name`.
   No results: `No sections`.
4. `PassedBySemester(semester);`, for admin and staff: one line per course of the semester, in creation order, and a last line with the totals:
   `courseCode | course name | passed <p> | failed <f>`
   `Total | passed <P> | failed <F>`.
   Each enrollment counts once: a student enrolled in two courses of the semester counts in both. If the semester has no courses: `No courses in the semester`.

With the data of the first load example (each block starts with the instruction; the other lines are its response):

```text
StudentCourses(200145785);
777 | compilers1 | section 1 | coursework 30 | final 25 | failed

CourseStudents(777);
200145785 | Fernando Marroquín | section 1 | coursework 30 | final 25 | failed
200745781 | Alfonso Antonio | section 1 | coursework 60 | final 10 | passed

ClassroomSections(G, 109);
section 5 | tuesday | 7:10am-8:00am | 771 | programming2
section 6 | tuesday | 8:00am-8:50am | 771 | programming2
section 7 | thursday | 7:10am-8:00am | 771 | programming2
section 8 | thursday | 8:00am-8:50am | 771 | programming2

PassedBySemester(4);
771 | programming2 | passed 1 | failed 0
732 | Statistics 1 | passed 0 | failed 0
795 | Formal languages | passed 0 | failed 0
Total | passed 1 | failed 0
```

Loading a file and reports are no longer out of scope.

Follow AGENTS.md and the rules in .bob/rules. You are done when `mvn -q verify` passes.
