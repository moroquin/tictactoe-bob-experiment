# Session history

## Task
Implement CR-2: a three-pieces variant of Tic-Tac-Toe where each Player has only three Pieces and, once all are on the Board, moves them instead of putting new ones.

## Timing
- Start: 2026-11-07T16:49:51Z
- End: 2026-11-07T16:54:54Z

## Result
done — `mvn -q verify` passes with 93 tests, 0 failures.

## Decisions I made on my own

| ID | Type | Question I would have asked | What I decided | Why | Where |
|---|---|---|---|---|---|
| A1 | design | Should `ThreePiecesTicTacToe` extend `TicTacToe` or be a separate class? | Separate standalone class (not a subclass) | `TicTacToe` is `final`; composition over inheritance; both share the same package-private mutators | `src/main/java/tictactoe/domain/ThreePiecesTicTacToe.java` |
| A2 | design | Should the console handle both modes in one class or two? | Two classes: existing `ConsoleGame` (unchanged) + new `ThreePiecesConsoleGame` | Keeps existing class stable; avoids if-chains for put vs. move input parsing; `Main` wires the right one (D17) | `src/main/java/tictactoe/console/ThreePiecesConsoleGame.java` |
| A3 | design | How should `State` signal whether we are in the move phase? | Added `X_TO_MOVE`, `O_TO_MOVE` values and a new `isMoving()` helper | Symmetric with the existing put-phase states; `isOver()` is unchanged; `isMoving()` lets the console choose the right parser | `src/main/java/tictactoe/domain/State.java` |
| A4 | design | How should `Board` support moving a Piece? | Package-private `Board.move(from, to)` that calls `Square.remove()` then `Square.put()` | Keeps mutators package-private (I10); the board handles the two-step atomically | `src/main/java/tictactoe/domain/Board.java`, `Square.java` |
| A5 | ambiguity | The sequence "after both players have 0 pieces remaining, who moves first?" | X moves first (X_TO_MOVE), because X goes first in alternation and O makes the last Put (round 6) | Consistent with "X always starts" (I4); after O's 3rd Put the round advances back to X | `src/main/java/tictactoe/domain/ThreePiecesTicTacToe.java` |
| A6 | gap | The CR says move input is "fromRow fromColumn toRow toColumn" — should the console also validate that from ≠ to? | No extra validation added | The domain rejects it naturally: `colorAt(from)` would be correct color but `board.move(from, from)` would fail in `Square.remove()` then `Square.put()` on the same (now empty) square; the move returns false correctly | `src/main/java/tictactoe/console/ThreePiecesConsoleGame.java` |

## Pending

None.

## Documentation I relied on

- `AGENTS.md`: golden rules for doc-first development, variation points, file map.
- `REQUIREMENTS.md` (CR-2): start flag, put/move sequence, invalid input rules, no draw.
- `docs/domain/glossary.md`: existing terms; added Move, Three-pieces variant, Pieces remaining, updated State.
- `docs/domain/model.puml`: existing class diagram; added ThreePiecesTicTacToe, Board.move, Square.remove, State.X/O_TO_MOVE.
- `docs/domain/invariants.md`: existing invariants; added I11–I15 for the three-pieces variant.
- `docs/domain/game-states.md`: existing states; added CR-2 state table.
- `docs/domain/modules.md`: file map; added ThreePiecesTicTacToe and ThreePiecesConsoleGame entries.
- `docs/domain/decisions.md`: added D14–D17 for CR-2 design choices.

## Verification
`mvn verify` passed; 93 tests, 0 failures, 0 errors.
