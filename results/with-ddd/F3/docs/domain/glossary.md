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
| Player | One of the two participants. A Player owns a Color. A Player chooses the Coordinate of the next Put; how it chooses depends on the kind of Player. | `Player` | User, Participant |
| Human Player | A Player that is a person. The console reads the Coordinate from standard input on their behalf. | `HumanPlayer` | — |
| PC Player | A Player controlled by the program. It chooses its Coordinate automatically using the fixed PC strategy, without any input. | `PcPlayer` | Computer, AI, Bot |
| PC Strategy | The deterministic, fixed sequence of rules the PC Player uses to choose a Coordinate: Win, Block, Center, Corner, First Empty (in that order, reading order for ties). | (logic inside `PcPlayer`) | — |
| Round | Decides which Player puts next. X always starts; then the Players alternate. | `Round` | Turn, TurnManager |
| Put | Placing a new Piece of the current Player's Color on an empty Square. It is an operation, not a class: `TicTacToe.put(Coordinate)`. Board and Square use the same verb for their part of it. | `put` | move, place, mark, makeMove, play |
| Line | Three Coordinates in a row: a Row, a Column, the Diagonal or the Inverse Diagonal. There are 8 Lines. | `Line` | Triple, WinningLine, Combo |
| Row, Column | The three horizontal and the three vertical Lines. | `Line.row(n)`, `Line.column(n)` | — |
| Diagonal | The Line (1,1)–(2,2)–(3,3). | `Line.diagonal()` | MainDiagonal |
| Inverse Diagonal | The Line (1,3)–(2,2)–(3,1). | `Line.inverseDiagonal()` | AntiDiagonal, SecondaryDiagonal |
| Win | Three Pieces of the same Color on one Line. | `Board.hasWin(Color)`, `State.X_WINS`, `State.O_WINS` | Victory, Goal |
| Draw | All 9 Squares are occupied and there is no Win. | `State.DRAW` | Tie |
| State | Where a game is: X to put, O to put, X to move, O to move, X wins, O wins or Draw. See `game-states.md`. | `State.X_TO_PUT`, `State.O_TO_PUT`, `State.X_TO_MOVE`, `State.O_TO_MOVE`, `State.X_WINS`, `State.O_WINS`, `State.DRAW` | Status, Outcome, Phase |
| Move | Moving a Piece that is already on the Board from one Square to another empty Square. Used only in the three-pieces variant (CR-2). It is an operation, not a class: `TicTacToe.move(from, to)`. Board and Square use the same verb for their part of it. | `move` | — |
| Three-pieces variant | A game mode in which each Player has exactly three Pieces. Rounds 1–6 are Puts (as usual). From round 7 on, the current Player moves one of their own Pieces to an empty Square. There is no Draw: the game continues until someone wins. Started with `--three-pieces`. | `ThreePiecesTicTacToe` | — |
| Pieces remaining | The number of Pieces a Player has not yet put on the Board. In the three-pieces variant each Player starts with 3 and decrements on each Put. When both Players have 0, the game is in the moving phase. | `piecesRemaining` | — |
