# Session history

## Task
Implement CR-5: course prerequisites and a 24-credit enrollment limit.

## Timing
- Start: 2026-11-11T18:38:42Z
- End: 2026-11-11T18:42:27Z

## token use
Agent Mode · 107.1k / 270.0k (40%) · 7.31 🅞

## Result
Done: `mvn -q verify` passes with 182 tests, 0 failures.

## Decisions I made on my own

| ID | Type | Question I would have asked | What I decided | Why | Where |
|---|---|---|---|---|---|
| A1 | design | Where to store prerequisites — in a separate entity/table or inside the Course aggregate? | Inside `Course` as an ordered `List<CourseCode>`, and also via repository methods on `CourseRepository`. | The aggregate owns its invariants; the list is small and ordered by insertion. The repository methods handle cross-aggregate queries (cycle detection, `isPrerequisiteOfAny`). | `Course.java`, `CourseRepository.java`, `InMemoryCourseRepository.java` |
| A2 | design | How to check the credit limit for the enrolled student — add a method to `SectionRepository` or scan in the service? | Added `SectionRepository.enrolledCourseCodes(StudentNumber)` returning the list of course codes the student is enrolled in. | Keeps the scan in one place (consistent with D11 which added `enrolledSection`). Used by `CatalogService.currentCredits`. | `SectionRepository.java`, `InMemorySectionRepository.java` |
| A3 | design | Where to put the `currentCredits` helper that sums enrolled credits? | In `CatalogService` (accessible to both `EnrollmentService` and `CatalogService.modifyCourse`). | Both use cases need it; `CatalogService` owns the `CourseRepository` and can look up credits by code. | `CatalogService.java` |
| A4 | design | The CR-5 requirement says `Prerequisite` is allowed for admin and staff; which `Action` applies? | `MANAGE_SCHOOL_DATA`, which is already granted to admin and staff and covers creating/modifying courses. | Prerequisites are course metadata (catalog data), so the same action that governs course management applies. | `CatalogService.addPrerequisite` |
| A5 | naming | What response text does `Prerequisite(...)` return on success? | `OK: prerequisite added` | The text after OK is free (R41); chose a descriptive message matching the pattern of other OK messages. | `Dispatcher.handlePrerequisite` |
| A6 | design | Does `modifyCourse` need to validate the credit range before the credit-limit check, or after? | Before: construct `Semester` and check `credits > 0` first, then check the limit. | Fail fast on invalid inputs before checking cross-aggregate rules (I21, I6 ordering principle). | `CatalogService.modifyCourse` |

## Pending

None.

## Documentation I relied on

- `AGENTS.md`: golden rules, how to work on a task, the file map.
- `docs/domain/invariants.md`: numbered invariants I18–I28 as baseline; added I29–I33.
- `docs/domain/policies.md`: enrollment check order; extended with CR-5 order.
- `docs/domain/modules.md`: file map for all classes changed or created.
- `docs/domain/decisions.md`: D1–D23 as baseline; added D24–D29.
- `docs/domain/model.puml`: domain model used to place new methods correctly.
- `REQUIREMENTS.md`: R20–R21 for course delete/modify rules that were extended by CR-5.

## Verification
`mvn -q verify` passed: 182 tests, 0 failures, 0 errors, 0 skipped.
