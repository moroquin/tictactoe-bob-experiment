# Tic-Tac-Toe — requirements (version 1, updated by CR-1)

A console Tic-Tac-Toe for two people sitting at the same computer, or one person against the PC.

## Rules of the game

- The board has 3 × 3 squares. A square is identified by its coordinate: a row and a column, each from 1 to 3. Row 1 is the top row and column 1 is the left column.
- There are two players, X and O. Each player puts pieces of their own color.
- X always starts; after that, the players alternate.
- In each round, the current player puts one new piece on an empty square.
- A player wins as soon as three of their pieces are on the same row, the same column, the diagonal (1,1)–(2,2)–(3,3) or the inverse diagonal (1,3)–(2,2)–(3,1). A win is checked before a draw.
- If all 9 squares are occupied and nobody has won, the game is a draw.
- Invalid input is rejected: the board does not change and the same player tries again. Input is invalid when it is not exactly two whole numbers, when a number is outside 1–3, or when the square is already occupied.

## Console contract

Automated acceptance tests drive the game through the console, so follow this contract exactly.

- Start: `java -jar target/tictactoe.jar` (no arguments, two Human Players) or `java -jar target/tictactoe.jar --vs-pc` (Human plays X, PC plays O).
- Input: one line per put, with the row and the column separated by a space, for example `2 3`.
- After invalid input, print one line that starts with `Invalid` and read again for the same player.
- When the game ends, print the final board as three lines, one per row from row 1 to row 3, with one character per square (`X`, `O`, or `.` for an empty square) and no spaces. Then print exactly one result line: `X wins`, `O wins` or `Draw`. The result line is the last line of output, and the program exits with status 0.
- Any other output, such as prompts or the board after every put, is allowed before the final board.

Example of the end of a game:

    XXX
    OO.
    ...
    X wins

## Build

- Java 21 and Maven, with the provided `pom.xml`.
- `mvn -q verify` must pass.
- `mvn -q package` produces `target/tictactoe.jar`. The entry point is the class `tictactoe.Main`.

## Changes in CR-1

- `--vs-pc` flag: Human plays X and starts; PC plays O automatically.
- PC strategy (deterministic, no randomness or AI):
  1. Win: complete three in a line if possible.
  2. Block: prevent X from completing three in a line.
  3. Center: take (2,2) if empty.
  4. Corner: take the first empty corner in reading order: (1,1),(1,3),(3,1),(3,3).
  5. First empty: take the first empty Square in reading order.
  When rules 1 or 2 match more than one Square, the first in reading order is used.

## Out of scope

- Other board sizes or other rule variants.
- Saving games, and graphical or web interfaces.
