# Invariants

Numbered rules that must always hold. Cite them in test names (`I3_...`), in commit messages and in prompts.

| # | Invariant |
|---|---|
| I1 | The Board has exactly 9 Squares (3 × 3). The row and the column of a Coordinate go from 1 to 3. |
| I2 | A Square holds at most one Piece. A Square without a Piece has Color EMPTY. |
| I3 | A Put targets an empty Square. A Put on an occupied Square is rejected, and the Board and the Round do not change. |
| I4 | X always starts. The Round alternates X, O, X, O… |
| I5 | A Player only puts Pieces of their own Color. |
| I6 | After every Put, Win is checked first: three Pieces of the same Color on a Row, a Column, the Diagonal or the Inverse Diagonal end the game. |
| I7 | If there is no Win and all 9 Squares are occupied, the game ends in Draw. |
| I8 | Once the game has ended, no Put is accepted. |
| I9 | Input that is not a valid Coordinate (not exactly two whole numbers, or a number outside 1–3) is rejected without changing the Board or the Round. |
