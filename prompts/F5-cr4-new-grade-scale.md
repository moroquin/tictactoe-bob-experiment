Change request CR-4: new grade scale

The School changes its grade scale. This replaces R28; the rest of REQUIREMENTS.md and of the earlier change requests still holds.

- The coursework goes from 0 to 70 and the final exam from 0 to 30, in integers. The grade is coursework plus final exam, and the student passes with 65 or more.
- Every instruction that receives grades (`Enroll`, `ChangeGrades` and the `Enroll` lines of a loaded file) checks the new ranges, and every report that says `passed` or `failed` uses the new passing grade.
- A student who enrolls themselves still starts with coursework 0 and final exam 0.

Examples: coursework 60 and final exam 10 is 70, passed; 30 and 25 is 55, failed; 50 and 14 is 64, failed. Coursework 72 is now rejected, and final exam 28 is now valid.

Follow AGENTS.md and the rules in .bob/rules. You are done when `mvn -q verify` passes.
