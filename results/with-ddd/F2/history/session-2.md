# Session history

## Task
Add a PC Player (CR-1: Human vs. PC): a deterministic, program-controlled Player that can be activated via `--vs-pc`, while keeping the two-Human-Player mode working without arguments.

## Token usage
94.3k / 270.0k (35%) · 4.67 🅞

## Timing
- Start: 2026-11-07T14:30:17Z
- End: 2026-11-07T14:36:30Z

## Result
Done. All 73 tests pass (`mvn -q verify`), docs and code are in agreement.

## Decisions I made on my own

| ID | Type | Question I would have asked | What I decided | Why | Where |
|---|---|---|---|---|---|
| A1 | design | Should `chooseCoordinate` receive `Board` or `TicTacToe`? | `TicTacToe` | `Board` is not directly accessible from the console; `TicTacToe` is the only public gateway to the game state. `PcPlayer` only needs `colorAt(Coordinate)` which is on `TicTacToe`. | `src/main/java/tictactoe/domain/Player.java`, `docs/domain/decisions.md` D11 |
| A2 | design | Should `chooseCoordinate` signal "defer to console" via `null` return or a separate interface method? | `null` return | Simplest; avoids adding an extra method or boolean field. Documented clearly in javadoc and decisions D11. No caller needs to know the type of Player. | `src/main/java/tictactoe/domain/HumanPlayer.java` |
| A3 | design | Should `Player` be an abstract class or an interface? | Abstract class | `color()` and its validation (`EMPTY` guard) are shared by all players. Abstract class avoids duplicating this in every subclass. | `src/main/java/tictactoe/domain/Player.java` |
| A4 | naming | What to name the two concrete subclasses? | `HumanPlayer` and `PcPlayer` | CR-1 wording ("Human Players", "PC"), consistent with glossary terms. | `docs/domain/glossary.md`, source files |
| A5 | design | How does PcPlayer implement reading order for Win/Block candidates? | Helper method `readingOrderIndex(Coordinate)` → `(row-1)*3 + (col-1)` and track best candidate across all 8 lines | Ensures first-in-reading-order even when the threat square is found in a later line. | `src/main/java/tictactoe/domain/PcPlayer.java` |
| A6 | scope | Should REQUIREMENTS.md be updated to reflect CR-1? | Yes | REQUIREMENTS.md listed "Playing against the PC" as out of scope; it is now in scope. Keeping it stale would violate rule 10 (docs and code in agreement). | `REQUIREMENTS.md` |

## Pending

None.

## Documentation I relied on

- `AGENTS.md`: golden rules (change docs first, extend at variation point, no instanceof, tests cite invariants).
- `docs/domain/modules.md`: file map (where new files go), "A new kind of Player" section, console access to `TicTacToe` only through `state()`, `currentPlayer()`, `colorAt()`.
- `docs/domain/decisions.md`: existing decisions D1–D10, confirmation that Player variation point was designed from the start.
- `docs/domain/invariants.md`: I10 (only TicTacToe changes a game), I9 (invalid input not reaching domain).
- `REQUIREMENTS.md`: CR-1 PC strategy rules (Win, Block, Center, Corner, First Empty), console contract unchanged, `--vs-pc` flag.

## Verification
`mvn -q verify` passed with 73 tests (0 failures, 0 errors): BoardTest (9), CoordinateTest (9), LineTest (7), RoundTest (3), SquareTest (4), TicTacToeTest (18), PcPlayerTest (19), ArchitectureTest (3).
