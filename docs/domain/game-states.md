# Game states

| State | Event | Next state | Invariants |
|---|---|---|---|
| X to put (initial) | Valid Put by X, no Win, Board not full | O to put | I3, I4, I5 |
| X to put | Valid Put by X that completes a line | X wins | I6 |
| X to put | Valid Put by X that fills the Board without a Win | Draw | I7 |
| X to put | Invalid input, or a Put on an occupied Square | X to put | I3, I9 |
| O to put | Valid Put by O, no Win | X to put | I3, I4, I5 |
| O to put | Valid Put by O that completes a line | O wins | I6 |
| O to put | Invalid input, or a Put on an occupied Square | O to put | I3, I9 |
| X wins, O wins, Draw | Any input | No change: the game is over | I8 |

O never fills the Board: with 9 Squares, the ninth Put is always X's.
