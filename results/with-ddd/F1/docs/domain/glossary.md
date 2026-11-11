# Glossary — the ubiquitous language

Use these names in code, tests and docs. A discarded term in the code is a bug. When you need a name that is not here, add the term to this glossary first.

| Term | Meaning | Name in code | Discarded |
|---|---|---|---|
| TicTacToe | The game itself: one Board, one Round and two Players. It is the aggregate root: it is the only object that changes a game, and everything outside the domain talks to it. | `TicTacToe` | Match, Engine |
| Board | The 3 × 3 set of Squares. It answers what is on each Square and whether there is a Win or the Board is full. | `Board` | Grid, Matrix, Field |
| Square | One place on the Board, at one Coordinate. It holds at most one Piece. | `Square` | Cell, Tile, Box, Spot |
| Coordinate | The row and the column of a Square, each from 1 to 3. Row 1 is the top row; column 1 is the left column. A value object: two Coordinates with the same row and column are equal. | `Coordinate` | Position, Point, Index, Location |
| Reading order | Row 1 to 3 and, within a row, column 1 to 3. The order of `Coordinate.all()` and of the printed Board. | `Coordinate.all()` | — |
| Piece | What a Player puts on a Square. It has a Color. Its position is the Coordinate of the Square that holds it, never a field of the Piece. | `Piece` | Mark, Token, Symbol, Stone |
| Color | `X`, `O` or `EMPTY`. EMPTY is a Null Object: a Square without a Piece has Color EMPTY, never `null`. Pieces and Players are always X or O. | `Color.X`, `Color.O`, `Color.EMPTY` | Sign, Symbol, `null` for empty |
| Player | One of the two participants. A Player owns a Color. In version 1 both Players are people, and the console reads their Coordinates. | `Player` | User, Participant |
| Round | Decides which Player puts next. X always starts; then the Players alternate. | `Round` | Turn, TurnManager |
| Put | Placing a new Piece of the current Player's Color on an empty Square. It is an operation, not a class: `TicTacToe.put(Coordinate)`. Board and Square use the same verb for their part of it. | `put` | move, place, mark, makeMove, play |
| Line | Three Coordinates in a row: a Row, a Column, the Diagonal or the Inverse Diagonal. There are 8 Lines. | `Line` | Triple, WinningLine, Combo |
| Row, Column | The three horizontal and the three vertical Lines. | `Line.row(n)`, `Line.column(n)` | — |
| Diagonal | The Line (1,1)–(2,2)–(3,3). | `Line.diagonal()` | MainDiagonal |
| Inverse Diagonal | The Line (1,3)–(2,2)–(3,1). | `Line.inverseDiagonal()` | AntiDiagonal, SecondaryDiagonal |
| Win | Three Pieces of the same Color on one Line. | `Board.hasWin(Color)`, `State.X_WINS`, `State.O_WINS` | Victory, Goal |
| Draw | All 9 Squares are occupied and there is no Win. | `State.DRAW` | Tie |
| State | Where a game is: X to put, O to put, X wins, O wins or Draw. See `game-states.md`. The board log writes it with the names of `REQUIREMENTS.md` (`IN_PROGRESS` while the game goes on): the table is in `game-states.md`. | `State.X_TO_PUT`, `State.O_TO_PUT`, `State.X_WINS`, `State.O_WINS`, `State.DRAW` | Status, Outcome, Phase |
| Board log | The JSON Lines file that `REQUIREMENTS.md` asks for when `TICTACTOE_BOARD_LOG` is set: the Board and the State after each change. It is output, so `tictactoe.console` writes it, with the State translated as `game-states.md` says. | — | — |
| Move | Reserved: moving a Piece that is already on the Board. It is not part of version 1, so never use it for a Put. | — | — |
