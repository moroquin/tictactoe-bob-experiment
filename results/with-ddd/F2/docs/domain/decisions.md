# Decision log

Append new decisions at the end. Never rewrite or delete an entry; when a decision changes, add a new entry that says which one it replaces.

| # | Decision | Why |
|---|---|---|
| D1 | Version 1 is console only. | It is the smallest interface the acceptance tests can drive. |
| D2 | Coordinates are 1-based everywhere, in the console and in the domain. | No off-by-one translation between what a person types and the model. |
| D3 | Color EMPTY is a Null Object; no Square ever returns `null`. | Fewer null checks, and the Board can always be printed. |
| D4 | A rule change starts in `docs/domain/`, then the code, in the same task. | The docs stay the source of truth for people and for agents. |
| D5 | The team diagram's Goal is not modeled in version 1. Win is checked by `Board.hasWin(Color)` and recorded in the State. | Version 1 has a single Goal; an abstract class with one subclass adds code and no behaviour. Add Goal back when a second Goal appears. |
| D6 | Put is an operation, `TicTacToe.put(Coordinate)`, not a class. | A Put carries nothing but its Coordinate; a class around it adds ceremony. |
| D7 | A Piece has a Color but no Coordinate; its position is the Square that holds it. | One source of truth for where a Piece is. |
| D8 | Only `TicTacToe` changes a game (I10); the console reads it through `state()`, `currentPlayer()` and `colorAt()`. | The aggregate root protects the invariants, and the console cannot bypass them. |
| D9 | The Players are created outside `TicTacToe` and passed to its constructor. | Who plays is decided at the edge (`Main`), not inside the game. |
| D10 | The docs drive the development: an agent learns the domain from `docs/domain/` and reads only the code files the file map in `modules.md` names. Every design choice is recorded here and in the model. | A new session, without memory, should not need to read the whole codebase to understand the design. |
| D11 | The board log is written by `tictactoe.console`, and its `state` uses the names of `REQUIREMENTS.md`: `X_TO_PUT` and `O_TO_PUT` become `IN_PROGRESS` (table in `game-states.md`). | The log is an output contract like the console; the domain keeps its own States and does no output. |
| D12 | `Board`, `Square` and `Round` are package-private classes in `tictactoe.domain`. Only `TicTacToe` (the aggregate root, public) and the domain unit tests access them. | Enforces I10: nothing outside the domain can change a game without going through `TicTacToe`. |
| D13 | `ConsoleGame` writes each board-log line by opening the file in append mode and closing it immediately. | Simple and correct; the file grows one line at a time and partial writes are not a risk at this scale. |
| D14 | `parseCoordinate` in `ConsoleGame` splits on whitespace and checks for exactly two tokens. An empty token array after split, a non-integer token, or a number outside 1–3 all print a line starting with `Invalid` (I9). | Matches the REQUIREMENTS.md rejection rule exactly. |
| D15 | The initial empty board is written to the board log before the game loop starts. | REQUIREMENTS.md says "one line at the start of the game with the empty board". |
| D16 | CR-1: `Player` becomes an abstract class with an abstract `chooseCoordinate(TicTacToe)` method. `HumanPlayer` and `PcPlayer` extend it. | Lets `ConsoleGame` call `currentPlayer().chooseCoordinate(game)` without asking which kind of Player it has (modules.md rule). |
| D17 | CR-1: `CoordinateSupplier` is a domain interface with a single method `get(TicTacToe): Coordinate`. `HumanPlayer` holds one; `ConsoleGame` supplies the implementation that reads from stdin. | Keeps the domain free of I/O while letting the Human Player integrate with the console (modules.md: domain declares the interface, console implements it). |
| D18 | CR-1: `PcPlayer` lives in `tictactoe.domain`. It reads the game only through `TicTacToe.colorAt` and `TicTacToe.state`; it does no I/O. The five rules are evaluated in order, ties broken by reading order. | Pure domain logic; no I/O rules are violated; ArchitectureTest still passes. |
| D19 | CR-1: `--vs-pc` is handled entirely in `Main`. With the flag, `Main` creates a `PcPlayer` for O; without it, it creates a `HumanPlayer` for O (exactly as before). `ConsoleGame` does not know about the flag. | Keeps the new command-line option in `Main` only (modules.md: "A new way to start the game: change Main only"). |
