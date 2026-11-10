# Invariants

Numbered rules that must always hold. Cite them in test names (`I3_...`), in commit messages and in prompts.

| # | Invariant | Who enforces it |
|---|---|---|
| I1 | The Board has exactly 9 Squares (3 × 3). The row and the column of a Coordinate go from 1 to 3; a Coordinate outside that range cannot be created. | `Board`, `Coordinate` |
| I2 | A Square holds at most one Piece. A Square without a Piece has Color EMPTY. | `Square` |
| I3 | A Put targets an empty Square. A Put on an occupied Square is rejected, and the Board, the Round and the State do not change. | `TicTacToe` |
| I4 | X always starts. The Round alternates X, O, X, O… | `Round` |
| I5 | A Player only puts Pieces of their own Color. | `TicTacToe` |
| I6 | After every Put, Win is checked first: three Pieces of the same Color on one Line end the game. | `TicTacToe`, `Board` |
| I7 | If there is no Win and all 9 Squares are occupied, the game ends in Draw. | `TicTacToe`, `Board` |
| I8 | Once the State is X wins, O wins or Draw, no Put is accepted. | `TicTacToe` |
| I9 | Input that is not exactly two whole numbers, each from 1 to 3, is rejected without calling the domain. | `tictactoe.console` |
| I10 | Only `TicTacToe` (or `ThreePiecesTicTacToe`) changes a game. Nothing outside `tictactoe.domain` can put a Piece on a Board or a Square, or advance the Round, except through `TicTacToe.put` or `ThreePiecesTicTacToe.put`/`move`. | `TicTacToe`, `ThreePiecesTicTacToe` (package-private mutators) |
| I11 | In the three-pieces variant, each Player starts with exactly three Pieces remaining. A Put decrements the current Player's Pieces remaining. | `ThreePiecesTicTacToe` |
| I12 | In the three-pieces variant, Puts are accepted only while any Player has Pieces remaining (rounds 1–6). | `ThreePiecesTicTacToe` |
| I13 | In the three-pieces variant, a Move is accepted only when the game is in state X_TO_MOVE or O_TO_MOVE. A Move from a Square not holding a Piece of the current Player's Color is rejected. A Move to an occupied Square is rejected. | `ThreePiecesTicTacToe` |
| I14 | In the three-pieces variant, Win is checked after every Put and every Move. | `ThreePiecesTicTacToe`, `Board` |
| I15 | The three-pieces variant has no Draw: the game continues until someone wins. | `ThreePiecesTicTacToe` |
