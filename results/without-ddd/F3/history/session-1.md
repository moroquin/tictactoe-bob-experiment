# Session history

## Task
Implement CR-2: a `--three-pieces` variant of Tic-Tac-Toe where each player has exactly three pieces and, after all six are placed, moves them instead of adding new ones.

## Timing
- Start: 2026-11-07T16:47:41Z
- End: 2026-11-07T16:49:10Z

## Result
Done. `mvn -q verify` passes with 60 tests (0 failures).

## Decisions I made on my own

| ID | Type | Question I would have asked | What I decided | Why | Where |
|---|---|---|---|---|---|
| A1 | design | Should `ThreePiecesGame` extend or wrap `Game`, or be a standalone class? | Standalone class, separate from `Game` | Keeps the two variants fully independent with no shared mutable state; simplest change that satisfies the requirement | `src/main/java/tictactoe/ThreePiecesGame.java` |
| A2 | design | Should the three-pieces game loop live in `Main.java` or a separate class? | Separate private method `runThreePieces()` inside `Main` | Minimal change — no new top-level class required; the existing `Main` already owned the loop for the standard variant | `src/main/java/tictactoe/Main.java` |
| A3 | gap | What should `isPutPhase()` report when both players are in different phases (e.g. X placed 3, O placed 2)? | Based on the *current player's* piece count | The requirement says "once *they* are all on the Board, moves them" — individual player threshold | `src/main/java/tictactoe/ThreePiecesGame.java` |
| A4 | gap | The CR says a line with the wrong number of tokens is "also invalid" — should the put-phase check for 4 tokens (a move command) be rejected with "Invalid"? | Yes — put phase expects exactly 2 tokens; move phase expects exactly 4; anything else prints "Invalid …" | Consistent with the existing standard-game validation | `src/main/java/tictactoe/Main.java` |

## Pending

None.

## Documentation I relied on

- `REQUIREMENTS.md`: Console contract, board rules, result line format, and build requirements.
- `AGENTS.md`: Map of files, build commands.
- CR-2 description (user message): three-pieces rules — put/move phases, invalid-move definitions, no-draw, `--three-pieces` flag.

## Verification
`mvn -q verify` passed, 60 tests (GameTest: 28, PcPlayerTest: 12, ThreePiecesGameTest: 20).
