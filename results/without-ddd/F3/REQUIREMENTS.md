# Tic-Tac-Toe — requirements (version 1)

A console Tic-Tac-Toe for two people sitting at the same computer.

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

- Start: `java -jar target/tictactoe.jar` (no arguments).
- Input: one line per put, with the row and the column separated by a space, for example `2 3`.
- After invalid input, print one line that starts with `Invalid` and read again for the same player.
- When the game ends, print the final board as three lines, one per row from row 1 to row 3, with one character per square (`X`, `O`, or `.` for an empty square) and no spaces. Then print exactly one result line: `X wins`, `O wins` or `Draw`. The result line is the last line of output, and the program exits with status 0.
- Any other output, such as prompts or the board after every put, is allowed before the final board.

Example of the end of a game:

    XXX
    OO.
    ...
    X wins

## Board log

Automated tests also check every step of the game through a log file, so follow this contract exactly too.

- When the environment variable `TICTACTOE_BOARD_LOG` holds a file path, the program writes the board to that file in JSON Lines format: one JSON object per line, in UTF-8. When the variable is not set, the program writes no log.
- Write one line at the start of the game with the empty board, and then one line after every valid put, with the board as it is after that put. Invalid input writes nothing. This applies to every way of starting the game.
- Each line has exactly this shape:

      {"board":[{"row":1,"column":1,"color":"X"},{"row":2,"column":2,"color":"O"}],"state":"IN_PROGRESS"}

  - `board` lists only the occupied squares, in any order. `row` and `column` are whole numbers from 1 to 3, the same coordinates the player types. `color` is `"X"` or `"O"`.
  - `state` is `"IN_PROGRESS"` while the game goes on, and `"X_WINS"`, `"O_WINS"` or `"DRAW"` on the line written after the put that ends the game.
- The log does not replace the console contract: the console output stays exactly as described above.

## Build

- Java 21 and Maven, with the provided `pom.xml`.
- `mvn -q verify` must pass.
- `mvn -q package` produces `target/tictactoe.jar`. The entry point is the class `tictactoe.Main`.

## Out of scope for version 1

- Playing against the PC (a player controlled by the program).
- Other board sizes or other rule variants.
- Saving games, and graphical or web interfaces.
