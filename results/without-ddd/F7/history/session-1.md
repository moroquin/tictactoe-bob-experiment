# Session history

## Task
Implement CR-6 (time conflicts): sections that overlap on the same day can no longer share a classroom, a professor, or a student.

## Timing
- Start: 2026-11-11T18:46:59Z
- End: 2026-11-11T18:49:26Z

## token use
Agent Mode · 105.8k / 270.0k (39%) · 4.36 🅞

## Result
Done. All 177 tests pass and `mvn -q verify` succeeds.

## Decisions I made on my own

| ID | Type | Question I would have asked | What I decided | Why | Where |
|---|---|---|---|---|---|
| A1 | design | How to implement period overlap detection? | Added a static `overlapsTime(period1, day1, period2, day2)` helper that compares minutes-since-midnight for the two periods; returns false for different days and uses strict `start1 < end2 && start2 < end1` (so touching = not overlapping). | CR-6 specifies "each one starts before the other one ends" for overlap, and "periods that only touch do not overlap". | `src/main/java/enrollment/service/EnrollmentService.java` |
| A2 | design | Where to put the classroom/professor conflict check in `createSection`? | Added the check at the end of validation, just before `store.addSection(...)`, after all references are resolved. | This follows R8 order (entity rules last) and is consistent with the existing pattern. | `src/main/java/enrollment/service/EnrollmentService.java` |
| A3 | design | Where to put the student conflict check in `modifySection`? | Added it at the end of all other checks (after the capacity check), iterating over enrolled students and comparing the new period/day against their other sections. | CR-6 says "ModifySection responds Error if, after the change, a student enrolled in that section would have two overlapping sections." This is logically the last check. | `src/main/java/enrollment/service/EnrollmentService.java` |
| A4 | deviation | Two existing CR-5 credit-limit tests used 8 sections all with `7:10am-8:00am, monday` same classroom and professor, which would now fail CR-6's classroom/professor conflict check. | Updated those tests to use distinct days (monday through sunday for 7 sections, then monday at `8:00am-8:50am` for the 8th) so each section creation is valid under CR-6. | CR-6 removes the "out of scope" exemption for overlap checks; the test scenarios simply needed different schedules to remain valid. | `src/test/java/enrollment/EnrollmentServiceTest.java` |

## Pending

None.

## Documentation I relied on

- `REQUIREMENTS.md`: Base rules (R24–R29, R8 ordering) and the CR-6 change request.
- `ARCHITECTURE.md`: All domain rules must live in `EnrollmentService`, not in the console.
- `AGENTS.md`: Build instructions and required `mvn -q verify`.

## Verification
`mvn verify` passed; 177 tests: 140 in EnrollmentServiceTest (including 13 new CR-6 tests), 20 in LoadAndReportConsoleTest, 14 in ParserTest, 3 in ArchitectureTest.
