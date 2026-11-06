# Modules

## Bounded context

Version 1 has a single bounded context, **Game**, which is the core domain. The console is an adapter around it, not part of the model.

## Packages

| Package | Holds | May depend on |
|---|---|---|
| `tictactoe.domain` | TicTacToe, Board, Square, Coordinate, Piece, Color, Player, Round, Goal, Win, Put | Only the JDK. It never reads input or prints output. |
| `tictactoe.console` | Reading Coordinates and printing the Board and the result, following the console contract in `REQUIREMENTS.md` | `tictactoe.domain` |
| `tictactoe` | `Main`: wiring only | Both |

`src/test/java/tictactoe/ArchitectureTest.java` enforces that the domain does not depend on the console and does not use `System.out`, `System.err`, `System.in`, `Scanner` or `Console`.

## Where changes go (Open/Closed)

- A new kind of Player, a new Goal, or a new way to choose a Square: add a class at that variation point instead of editing the existing ones.
- A new rule of the game: change `docs/domain/` first (glossary, model, invariants, game states, decisions), then the domain classes and their tests.
- A new way to show or read the game: change `tictactoe.console` only.
