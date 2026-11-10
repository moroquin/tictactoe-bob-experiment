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
| D11 | `Player` is made an abstract class (not an interface) with two concrete subclasses: `HumanPlayer` and `PcPlayer`. `chooseCoordinate(TicTacToe)` returns the Coordinate the Player wants to put on, or `null` when the Player defers to the console (Human). The console checks for null: null → read input; non-null → use directly. No `instanceof` anywhere. `TicTacToe` is passed (not `Board`) because it is the only public interface the console has to the game. | Abstract class lets `color` be a shared concrete field, avoiding duplication. Null-means-defer is the simplest way to let each Player kind decide without the console knowing which kind it has. |
| D12 | `PcPlayer.chooseCoordinate(TicTacToe)` implements the five-rule PC strategy directly inside the class, querying the game through `colorAt(Coordinate)` (the only read method needed). It does not expose any helper methods. | The strategy is a single, stable algorithm; decomposing it into public helpers would leak implementation detail. |
| D13 | `ConsoleGame` is updated to call `currentPlayer().chooseCoordinate(...)` before reading input. If the result is non-null it is used as the Put Coordinate; if null it reads from the Scanner. The console contract (output format, `Invalid` line) is unchanged. | This is the only change needed in the console: `Main` wires up the right Player type, and `ConsoleGame` stays type-blind. |
