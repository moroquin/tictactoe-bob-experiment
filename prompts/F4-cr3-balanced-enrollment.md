Change request CR-3: balanced sections

The way a section is chosen for an enrollment changes. This replaces R29; the rest of REQUIREMENTS.md and of the earlier change requests still holds.

- A student who enrolls in a course goes into the section of that course, among those with room, that has the fewest enrollments. If several have the same number of enrollments, the one with the lowest code. A section has room when its enrollments are fewer than the capacity of its classroom. If the course has no sections, or none has room, the enrollment is rejected.
- It applies to `Enroll`, to `EnrollMe` and to the `Enroll` lines of a loaded file.
- Students already enrolled do not move.

Example: if course 777 has sections 1 and 2, both with room, the first student enrolled in 777 goes into section 1, the second into section 2 and the third into section 1. With the first load example of CR-2, the students of 777 now end up in sections 1 and 2, so `QuerySection(1);` ends with `1/60`.

Follow AGENTS.md and the rules in .bob/rules. You are done when `mvn -q verify` passes.
