# Modules

## Bounded context

Version 1 has a single bounded context, **Game**, which is the core domain. The console is an adapter around it, not part of the model.

## Packages

| Package | Holds | May depend on |
|---|---|---|
| `tictactoe.domain` | TicTacToe, Board, Square, Coordinate, Line, Piece, Color, Player, Round, State | Only the JDK. It never reads input or prints output. |
| `tictactoe.console` | Reading Coordinates, printing the Board and the result, following the console contract in `REQUIREMENTS.md` | `tictactoe.domain`, and only through `TicTacToe` and the value types it returns (`Coordinate`, `Color`, `State`, `Player`) |
| `tictactoe` | `Main`: wiring only. It creates the Players and the `TicTacToe`, and hands them to the console | Both |

`src/test/java/tictactoe/ArchitectureTest.java` enforces that the domain does not depend on the console and does not use `System.out`, `System.err`, `System.in`, `Scanner` or `Console`.

## File map

Where each class lives. Read only the files of the classes you change and of the classes they call directly. Keep this table up to date: a class that is not here does not exist for the next session.

| Class | File | Tests |
|---|---|---|
| `TicTacToe` | `src/main/java/tictactoe/domain/TicTacToe.java` | `src/test/java/tictactoe/domain/TicTacToeTest.java` |
| `ThreePiecesTicTacToe` | `src/main/java/tictactoe/domain/ThreePiecesTicTacToe.java` | `src/test/java/tictactoe/domain/ThreePiecesTicTacToeTest.java` |
| `Board` | `src/main/java/tictactoe/domain/Board.java` | `src/test/java/tictactoe/domain/BoardTest.java` |
| `Square` | `src/main/java/tictactoe/domain/Square.java` | `src/test/java/tictactoe/domain/SquareTest.java` |
| `Coordinate` | `src/main/java/tictactoe/domain/Coordinate.java` | `src/test/java/tictactoe/domain/CoordinateTest.java` |
| `Line` | `src/main/java/tictactoe/domain/Line.java` | `src/test/java/tictactoe/domain/LineTest.java` |
| `Piece` | `src/main/java/tictactoe/domain/Piece.java` | — |
| `Color` | `src/main/java/tictactoe/domain/Color.java` | — |
| `Player` | `src/main/java/tictactoe/domain/Player.java` | — |
| `HumanPlayer` | `src/main/java/tictactoe/domain/HumanPlayer.java` | — |
| `PcPlayer` | `src/main/java/tictactoe/domain/PcPlayer.java` | `src/test/java/tictactoe/domain/PcPlayerTest.java` |
| `Round` | `src/main/java/tictactoe/domain/Round.java` | `src/test/java/tictactoe/domain/RoundTest.java` |
| `State` | `src/main/java/tictactoe/domain/State.java` | — |
| Console adapter (standard) | `src/main/java/tictactoe/console/ConsoleGame.java` | — |
| Console adapter (three-pieces) | `src/main/java/tictactoe/console/ThreePiecesConsoleGame.java` | — |
| `Main` | `src/main/java/tictactoe/Main.java` | — |
| Architecture rules | — | `src/test/java/tictactoe/ArchitectureTest.java` |

## Responsibilities

- **Changing a game** is `TicTacToe`'s job alone (I10). `Board.put`, `Square.put` and `Round.next` are package-private.
- **Reading a game** goes through `TicTacToe`: `state()`, `currentPlayer()` and `colorAt(Coordinate)`. No public method returns a mutable Board, Square or Round.
- **Choosing the Coordinate of the next Put** belongs to the current Player. A `HumanPlayer`'s Coordinate is supplied by the console reading input. A `PcPlayer` chooses its own Coordinate via `chooseCoordinate(Board)`.
- **Win and Draw** are decided by the Board (`hasWin`, `isFull`) and applied by `TicTacToe` to the State.
- **When the domain needs something from outside** (for example, a Coordinate typed by a person), the domain declares an interface for it in `tictactoe.domain`, and `tictactoe.console` implements it. The domain never calls the console directly.

## Where changes go (Open/Closed)

- A new kind of Player: add a class at the Player variation point instead of editing the existing ones. No class in any package — domain, console or `Main` — asks which kind of Player it has: no `instanceof`, no type flags, no `isHuman()`-style methods. The console calls `player.chooseCoordinate(board)` — if the Coordinate is null the console reads input; if it is non-null the console uses it directly. Each Player kind decides.
- A new rule of the game: change `docs/domain/` first (glossary, model, invariants, game states, decisions, this file map), then the domain classes and their tests.
- A new way to show or read the game: change `tictactoe.console` only.
- A new way to start the game (command-line options): change `Main` only.
