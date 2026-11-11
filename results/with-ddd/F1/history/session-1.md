# Session history

## Task
Implement version 1 of the Tic-Tac-Toe game: all domain classes, console adapter, Main entry point, and unit tests, so that `mvn -q verify` passes and `java -jar target/tictactoe.jar` follows the console contract in REQUIREMENTS.md exactly.

## Timing
- Start: 2026-11-10T22:46:05Z
- End: 2026-11-10T22:49:11Z

## token use
Agent Mode · 48.4k / 270.0k (18%) · 2.52 🅞

## Result
Done. `mvn -q verify` passes with 57 tests (0 failures); the jar follows the console contract and writes the board log correctly.

## Decisions I made on my own

| ID | Type | Question I would have asked | What I decided | Why | Where |
|---|---|---|---|---|---|
| A1 | design | `Board`, `Square`, `Round` — package-private class or package-private constructor? | Made the classes themselves package-private (`final class`, not `public final class`) | modules.md says "only TicTacToe changes a game" and the model marks mutators `~` (package-private); hiding the class is stronger than hiding only the constructors | `src/main/java/tictactoe/domain/Board.java`, `Square.java`, `Round.java` |
| A2 | gap | The ArchitectureTest's `domain_does_not_print` rule uses `ACCESS_STANDARD_STREAMS` which checks for use of `System.out/err/in`. The test classes `BoardTest`, `SquareTest` etc. live in `tictactoe.domain` package but the ArchUnit import option is `DoNotIncludeTests` so they are excluded from the check. | No action needed — the `DoNotIncludeTests` option already handles it | The ArchitectureTest itself uses that option | `src/test/java/tictactoe/ArchitectureTest.java` |
| A3 | design | Board uses `HashMap<Coordinate, Square>`. Should it be an array instead? | Used `HashMap` keyed by `Coordinate` (which has `equals`/`hashCode`) | Matches the domain model language (lookup by Coordinate), avoids 0-based index translation, straightforward | `src/main/java/tictactoe/domain/Board.java` |
| A4 | gap | Board log: the "board" array lists only occupied squares "in any order". What order to use? | Reading order (`Coordinate.all()` order, row 1–3, column 1–3) | It is the most predictable order; REQUIREMENTS.md says "in any order" so any order is valid, and reading order is the canonical order defined in the glossary | `src/main/java/tictactoe/console/ConsoleGame.java` |
| A5 | gap | What should the "Invalid" message say exactly? REQUIREMENTS.md only says print a line that starts with `Invalid`. | Used two distinct messages: one for bad format/out-of-range, one for occupied square | Both start with `Invalid` as required; the extra detail is allowed by REQUIREMENTS.md ("print one line that starts with `Invalid`") | `src/main/java/tictactoe/console/ConsoleGame.java` |

## Pending

None.

## Documentation I relied on

- `AGENTS.md`: golden rules, work order, file map — the primary guide for this session.
- `docs/domain/modules.md`: package structure, file map, responsibility rules, dependency rule.
- `docs/domain/glossary.md`: all class, method, and variable names used in code.
- `docs/domain/model.puml`: class signatures (public API), relationships and package-private markers.
- `docs/domain/invariants.md`: I1–I10, cited in every unit test name.
- `docs/domain/game-states.md`: state-transition table, board-log state name mapping.
- `docs/domain/decisions.md`: D1–D11 (pre-existing), D12–D15 added this session.
- `REQUIREMENTS.md`: console contract, board log contract, build requirements.

## Verification
`mvn test` passed: 57 tests run, 0 failures, 0 errors, 0 skipped (BoardTest: 10, LineTest: 6, CoordinateTest: 10, RoundTest: 4, SquareTest: 5, TicTacToeTest: 19, ArchitectureTest: 3). Then `mvn -q verify` passed with no output beyond SLF4J warnings (no providers, benign).
