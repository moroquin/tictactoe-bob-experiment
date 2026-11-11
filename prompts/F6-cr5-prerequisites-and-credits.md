Change request CR-5: prerequisites and a credit limit

Courses can now require other courses, and a student cannot take too many credits at once. The rest of REQUIREMENTS.md and of the earlier change requests still holds; prerequisites and the credit limit are no longer out of scope.

## Prerequisites

- New instruction `Prerequisite(courseCode, prerequisiteCode);`, for admin and staff: the course requires the prerequisite. Both courses must exist and be different, the pair must not exist already, and a course cannot end up requiring itself, directly or through other courses. It responds `OK: …` or `Error: <reason>`. It is also accepted in a loaded file.
- `QueryCourse` now ends with its prerequisites, in the order they were added, or `none`:
  `777 | compilers1 | 5 | 4 | prerequisites: 771, 795`
  `771 | programming2 | 4 | 4 | prerequisites: none`
- To enroll in a course, the student must have passed every one of its prerequisites: they must have an enrollment in that prerequisite with a passing grade. It is checked only when enrolling: changing grades or unenrolling later does not undo enrollments already made.
- A course that is a prerequisite of another course cannot be deleted. Deleting a course that has prerequisites (and no sections) removes its prerequisites too.

## Credit limit

- The credits of a student are the sum of the credits of the courses they are enrolled in. An enrollment that would take the student above 24 credits is rejected.
- `ModifyCourse` cannot raise the credits of a course if a student enrolled in it would end up above 24 credits.

## Order of the checks when enrolling

`Enroll`, `EnrollMe` and the `Enroll` lines of a loaded file check, in this order: the student and the course exist; the student is not enrolled in the course yet; the grades are in range (`Enroll` only); the prerequisites; the credit limit; a section with room (CR-3).

Follow AGENTS.md and the rules in .bob/rules. You are done when `mvn -q verify` passes.
