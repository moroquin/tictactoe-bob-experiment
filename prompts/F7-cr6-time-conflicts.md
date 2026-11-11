Change request CR-6: time conflicts

Sections that happen at the same time can no longer share a classroom, a professor or a student. The rest of REQUIREMENTS.md and of the earlier change requests still holds; overlaps are no longer out of scope.

- Two sections overlap when they are on the same day and their periods overlap: each one starts before the other one ends. Periods that only touch do not overlap: `7:10am-8:00am` and `8:00am-8:50am` can follow each other in the same classroom.
- A classroom cannot have two overlapping sections, and a professor cannot teach two overlapping sections. `Section`, `ModifySection` and the `Section` lines of a loaded file respond `Error` when they would break this.
- A student cannot be enrolled in two overlapping sections. When choosing the section of an enrollment (CR-3), only the sections that have room and do not overlap any section the student is already enrolled in are considered; among them, the one with the fewest enrollments and then the lowest code. If there is none, the enrollment is rejected. This is the last check when enrolling (CR-5).
- `ModifySection` responds `Error` if, after the change, a student enrolled in that section would have two overlapping sections.

Example: a student enrolled in section 1 (`7:10am-8:00am`, monday) who enrolls in a course whose only sections with room are on monday at `7:30am-8:20am` and on tuesday at `7:10am-8:00am` goes into the tuesday one, even if it has more enrollments.

Follow AGENTS.md and the rules in .bob/rules. You are done when `mvn -q verify` passes.
