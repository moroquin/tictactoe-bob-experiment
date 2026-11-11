# Session history

## Task
Implement CR-4: change the grade scale so that coursework goes from 0 to 70, final exam from 0 to 30, and the passing grade is 65 (replacing R28).

## Timing
- Start: 2026-11-11T18:28:10Z
- End: 2026-11-11T18:29:24Z

## token use
Agent Mode · 57.3k / 270.0k (21%) · 2.22 🅞

## Result
Done. `mvn -q verify` passes with all tests green.

## Decisions I made on my own

| ID | Type | Question I would have asked | What I decided | Why | Where |
|---|---|---|---|---|---|
| A1 | scope | The integration tests (ConsoleIntegrationTest) use `Enroll(200145785, 777, 30, 25)` and `Enroll(200745781, 777, 60, 10)`; do they need updating? | No change needed — all existing grade values in the tests are within the new ranges (≤70 coursework, ≤30 final), and the `passed`/`failed` verdicts are still correct under the new passing grade of 65 (55 fails, 70 passes). | The new scale is strictly a superset of valid values for those particular numbers; no assertion changes. | `src/test/java/enrollment/console/ConsoleIntegrationTest.java` |

## Pending

None.

## Documentation I relied on

- `AGENTS.md`: how to work on a task, golden rules, file map.
- `docs/domain/policies.md`: the Grade scale section that needed updating.
- `docs/domain/modules.md`: confirmed the only files to change are `GradeScale.java` and its test; no other class knows the grade numbers.
- `docs/domain/decisions.md`: where to append the new decision D23.
- `src/main/java/enrollment/domain/sections/GradeScale.java`: the three constants to change.
- `src/test/java/enrollment/domain/sections/GradeScaleTest.java`: the test file to rewrite with new values.
- `src/test/java/enrollment/console/ConsoleIntegrationTest.java`: verified that existing grade values in integration tests are still valid and that `passed`/`failed` assertions still hold.

## Verification
`mvn -q verify` passed (SLF4J no-provider warnings only, no test failures).
