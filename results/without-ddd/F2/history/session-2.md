# Session history

## Task
Implement CR-1: add a PC player (O) that the program controls, selectable with `--vs-pc`; without arguments, two-human behaviour is preserved.

## Timing
- Start: 2026-11-07T14:22:27Z
- End: 2026-11-07T14:24:33Z

## Result
done — `mvn -q verify` passes with all 40 tests (28 original + 12 new PcPlayer tests).

## Decisions I made on my own

| ID | Type | Question I would have asked | What I decided | Why | Where |
|---|---|---|---|---|---|
| A1 | design | Where should the PC move-selection logic live? | Created a separate `PcPlayer` class with a `chooseMove(Board, char)` method | Keeps the logic isolated and easily unit-testable without touching `Game` or `Board` | `src/main/java/tictactoe/PcPlayer.java` |
| A2 | design | Should the Human player be extracted to its own class? | No — kept the human input loop in `Main`; a `HumanPlayer` class was not needed and the CR did not require it | The CR only said to add a PC player; extracting Human into its own class would be scope creep | `src/main/java/tictactoe/Main.java` |
| A3 | gap | Which piece does the PC play in `--vs-pc` mode? | PC always plays O (Human always plays X and goes first) | The CR states "Human Player plays X and starts. The PC plays O." | `src/main/java/tictactoe/Main.java` |
| A4 | design | How to implement reading-order tie-break for rules 1 and 2? | Lines are enumerated in a fixed order (rows 1-3, cols 1-3, main diagonal, anti-diagonal); the first matching line's first empty square wins | This naturally produces reading-order output for the rows and mirrors the requirement | `src/main/java/tictactoe/PcPlayer.java` |

## Pending

None.

## Documentation I relied on

- `REQUIREMENTS.md`: console contract, result format, build instructions, entry point class name.
- `AGENTS.md`: build command, jar invocation.
- CR-1 description: PC rules (win/block/center/corner/fallback), reading-order tie-break, `--vs-pc` flag, X starts.

## Verification
`mvn -q verify` passed, 40 tests.

## Token usage
34.1k / 270.0k (13%) · 1.37 🅞
