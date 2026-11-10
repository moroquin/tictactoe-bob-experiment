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
| I10 | Only `TicTacToe` changes a game. Nothing outside `tictactoe.domain` can put a Piece on a Board or a Square, or advance the Round, except through `TicTacToe.put`. | `TicTacToe` (package-private mutators) |
