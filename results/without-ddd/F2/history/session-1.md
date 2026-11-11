# Session history

## Task
Implement CR-1: add a PC player (plays O deterministically) selectable via `--vs-pc`; without the flag the game keeps working with two human players.

## Timing
- Start: 2026-11-10T22:51:53Z
- End: 2026-11-10T22:55:59Z

## Token use 
Agent Mode · 81.5k / 270.0k (30%) · 2.48 🅞

## Result
Done — `mvn -q verify` passes with 11 new tests (10 in `PcPlayerTest`, all passing alongside the 25 existing `BoardTest` cases).

## Decisions I made on my own

| ID | Type | Question I would have asked | What I decided | Why | Where |
|---|---|---|---|---|---|
| A1 | design | Should the PC strategy be a new class, a method on `Board`, or inline in `Main`? | New package-private class `PcPlayer` with a `play(Board)` entry point and a `chooseMove(Board)` method visible to tests. | Keeps `Main` focused on I/O and allows the strategy to be unit-tested in isolation without coupling it to the board model. | `src/main/java/tictactoe/PcPlayer.java` |
| A2 | design | How to check "would placing color X at square S win?" without exposing internals or adding a public API to `Board`? | `PcPlayer.wouldWin` reads the board into a local `Color[][]` via the existing `board.at(r,c)` accessor, then runs the same win-detection logic as `Board.hasWon`. | Requires no changes to `Board` and avoids making a special-purpose `Board` method just for simulation. | `src/main/java/tictactoe/PcPlayer.java` |
| A3 | design | Should the board log be written for PC moves the same as for human moves? | Yes — the requirements say "one line after every valid put … this applies to every way of starting the game." | Explicit requirement. | `src/main/java/tictactoe/Main.java` |
| A4 | gap | The requirements say "the same input format for the Human Player" with `--vs-pc`; should the human still see a prompt? | Yes — the prompt `"Player X, enter row and column (e.g. 2 3):"` is printed before every human turn because any output before the final board is explicitly allowed by the console contract. | Consistent with the existing two-human flow; removing the prompt would be a silent change to user experience. | `src/main/java/tictactoe/Main.java` |

## Pending

None.

## Documentation I relied on

- `REQUIREMENTS.md`: console contract (prompt format, `Invalid` line, final board + result, board log contract), game rules (X starts, alternation, win/draw detection), build instructions.
- `AGENTS.md`: map of source files, build and run commands.

## Verification
`mvn -q verify` passed, 36 tests (25 `BoardTest` + 11 `PcPlayerTest`).
