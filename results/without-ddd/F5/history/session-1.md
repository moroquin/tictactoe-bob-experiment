# Session history

## Task
Implement CR-4: change the grade scale so that coursework goes 0–70, final exam 0–30, and the passing grade is 65 or more (replacing R28).

## Timing
- Start: 2026-11-11T18:25:57Z
- End: 2026-11-11T18:27:09Z

## token use
Agent Mode · 70.9k / 270.0k (26%) · 2.73 🅞

## Result
Done. All changes applied and `mvn -q verify` passes.

## Decisions I made on my own

None.

## Pending

None.

## Documentation I relied on
- `REQUIREMENTS.md`: provided R28 (old scale: 0–75/0–25, pass at 61) and the full context of `Enroll`, `ChangeGrades` and `EnrollMe` instructions.
- `AGENTS.md`: build command and file map.
- `ARCHITECTURE.md`: confirmed business rules live in `enrollment.service` and `enrollment.model`, not in the console layer.

## Verification
`mvn -q verify` passed (SLF4J warnings only, no test failures).
