Change request CR-1: modify and delete sections

Sections can now be modified and deleted. The rest of REQUIREMENTS.md still holds.

- New instruction `ModifySection(code, period, day, courseCode, classroomNumber, building, professorId);`, for admin and staff. It changes the period, the day, the course, the classroom and the professor of the section; the code does not change. The new values follow the same rules as when a section is created (R24 to R26). It responds `OK: …` or `Error: <reason>`.
- If the section has enrollments, its course cannot change, and the capacity of its new classroom cannot be lower than the number of students enrolled in it. The enrollments stay in the section, in the same order.
- New instruction `DeleteSection(code);`, for admin and staff. A section can be deleted only if it has no enrollments. Once it is deleted, its course, its classroom and its professor are no longer used by it, so they may become deletable (R19, R21, R23).
- Modifying and deleting sections is no longer out of scope.

Follow AGENTS.md and the rules in .bob/rules. You are done when `mvn -q verify` passes.
