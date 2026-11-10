# Session history

## Task
Implement version 1 of the Tic-Tac-Toe game described in REQUIREMENTS.md, following AGENTS.md and the domain docs, including unit tests, so that `mvn -q verify` passes and `java -jar target/tictactoe.jar` follows the console contract exactly.

## Timing
- Start: 2026-11-07T14:25:50Z
- End: 2026-11-07T14:28:53Z

## Result
Done. `mvn -q verify` passes with 54 tests and the jar follows the console contract exactly.

## Decisions I made on my own

| ID | Type | Question I would have asked | What I decided | Why | Where |
|---|---|---|---|---|---|
| A1 | design | What text should the `Invalid` line say for bad format vs. occupied square? | Two distinct `Invalid`-prefixed messages: one for bad input format, one for occupied square | REQUIREMENTS.md says the line must *start with* `Invalid`; using two messages makes the output human-readable and still satisfies the contract | `src/main/java/tictactoe/console/ConsoleGame.java` |
| A2 | design | Should `Square` and `Board` constructors be package-private or public? | Package-private for `Square` and `Board` constructors, public for the rest of the domain | Only `Board` (and via `Board`, `Square`) are created internally; `Coordinate`, `Line`, `Piece`, `Player`, `Round`, `TicTacToe` are created from outside the package | `src/main/java/tictactoe/domain/` |
| A3 | gap | The test for I6 "win checked before draw" had a move sequence where O actually won before X; the test was wrong | Redesigned the sequence to: X(1,1), O(2,1), X(2,3), O(2,2), X(3,1), O(3,2), X(1,2), O(3,3), X(1,3) so the board is full on the 9th put and X wins row 1 | Traced through every put manually to ensure no earlier win and that X completes row 1 on the 9th put | `src/test/java/tictactoe/domain/TicTacToeTest.java` |

## Pending

None.

## Documentation I relied on

- `AGENTS.md`: golden rules (docs first, only TicTacToe changes a game, tests cite invariants, no I/O in domain), how-to-work-on-a-task steps, file map.
- `docs/domain/modules.md`: package structure, file map, dependency rules, responsibilities.
- `docs/domain/model.puml`: class hierarchy, method signatures, package-private markers.
- `docs/domain/glossary.md`: canonical class and method names (used verbatim in code).
- `docs/domain/invariants.md`: I1–I10, which classes enforce each, used as test names.
- `docs/domain/game-states.md`: state transition table driving `TicTacToe.put` logic.
- `docs/domain/decisions.md`: D1–D10, especially D3 (Color.EMPTY Null Object), D6 (Put is an operation not a class), D8 (console reads through TicTacToe).
- `REQUIREMENTS.md`: console contract (output format, "Invalid" prefix, final board format, exit 0).

## Verification
`mvn -q verify` passed, 54 tests (18 domain, 3 architecture, rest across other test classes). Manually verified `java -jar target/tictactoe.jar` produces `X wins`, `O wins`, and `Draw` outputs matching the console contract, with invalid input and occupied-square rejection working correctly.

## Token usage
· 46.0k / 270.0k (17%) · 1.84 🅞
