# Glossary — the ubiquitous language

Use these names in code, tests and docs. A discarded term in the code is a bug.

| Term | Meaning | Name in code | Discarded |
|---|---|---|---|
| TicTacToe | The game itself: one Board, one Round and two Players. It is the aggregate root: every change to a game goes through it. | `TicTacToe` | Match, Engine |
| Board | The 3 × 3 set of Squares. | `Board` | Grid, Matrix, Field |
| Square | One place on the Board. It holds at most one Piece. | `Square` | Cell, Tile, Box, Spot |
| Coordinate | The row and the column of a Square, each from 1 to 3. Row 1 is the top row; column 1 is the left column. | `Coordinate` | Position, Point, Index |
| Piece | What a Player puts on a Square. It has a Color and a Coordinate. | `Piece` | Mark, Token, Symbol, Stone |
| Color | `X`, `O` or `EMPTY`. EMPTY is a Null Object: a Square without a Piece has Color EMPTY, never `null`. | `Color.X`, `Color.O`, `Color.EMPTY` | Sign, Symbol, `null` for empty |
| Player | One of the two participants. A Player owns a Color. In version 1 both Players are people. | `Player` | User, Participant |
| Round | Decides which Player puts next. X always starts; then the Players alternate. | `Round` | Turn, TurnManager |
| Put | Placing a new Piece of the current Player's Color on an empty target Square. | `put`, `Put` | move, place, mark, makeMove |
| Goal | What a Player tries to reach. In version 1 the only Goal is Win. | `Goal` | Objective |
| Win | Three Pieces of the same Color on a Row, a Column, the Diagonal or the Inverse Diagonal. | `Win` | Victory |
| Row, Column | The three horizontal and the three vertical lines of the Board. | `Row`, `Column` | — |
| Diagonal | The line (1,1)–(2,2)–(3,3). | `Diagonal` | MainDiagonal |
| Inverse Diagonal | The line (1,3)–(2,2)–(3,1). | `InverseDiagonal` | AntiDiagonal, SecondaryDiagonal |
| Draw | All 9 Squares are occupied and there is no Win. | `Draw` | Tie |
| Move | Reserved: moving a Piece that is already on the Board. It is not part of version 1, so never use it for a Put. | — | — |
