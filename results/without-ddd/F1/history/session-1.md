# Session history

## Task
Implement version 1 of the Tic-Tac-Toe game as described in REQUIREMENTS.md, including unit tests for the game rules, so that `mvn -q verify` passes and `java -jar target/tictactoe.jar` follows the console contract exactly.

## Timing
- Start: 2026-11-10T22:46:29Z
- End: 2026-11-10T22:49:27Z

## token use 
Agent Mode · 40.0k / 270.0k (15%) · 1.69 🅞

## Result
Done — `mvn -q verify` passes (25 tests, 0 failures) and the jar follows the console contract.

## Decisions I made on my own

| ID | Type | Question I would have asked | What I decided | Why | Where |
|---|---|---|---|---|---|
| A1 | design | Should the Board be mutable or immutable? | Immutable: `place()` returns a new `Board` | Immutable objects are thread-safe and easier to reason about; the game loop holds a single `board` reference | `src/main/java/tictactoe/Board.java` |
| A2 | design | Where should win/draw detection live? | Inside `Board.computeState()` as a private static method called during `place()` | Keeps the state consistent with the board contents; the state is always derivable from the squares | `src/main/java/tictactoe/Board.java` |
| A3 | gap | What should the program print for prompts? | A brief "Player X, enter row and column (e.g. 2 3):" prompt before each move | REQUIREMENTS.md says "any other output such as prompts … is allowed before the final board"; a prompt helps human players | `src/main/java/tictactoe/Main.java` |
| A4 | design | How to handle the case when stdin is exhausted before the game ends? | Print "Invalid input" and re-loop (the loop will end on the next `hasNextLine()` check which returns false, re-printing "Invalid input" until the game ends) | Requirements say invalid input is rejected and the same player retries; EOF is analogous | `src/main/java/tictactoe/Main.java` |
| A5 | design | Should the board log enumerate squares in row-major order? | Yes: iterate row 1→3, col 1→3 | The spec says "in any order"; row-major is natural and deterministic | `src/main/java/tictactoe/BoardLog.java` |

## Pending

None.

## Documentation I relied on

- `REQUIREMENTS.md`: game rules, console contract, board log format, build requirements.
- `AGENTS.md`: build commands, entry point class name.
- `pom.xml`: Java 21, JUnit 5.13.4, JaCoCo, PIT configuration.

## Verification
`mvn -q verify` passed — 25 tests, 0 failures, 0 errors.
