# Game states

The State of a game is `State` in the code. `TicTacToe.state()` returns it, and `State.isOver()` is true for X wins, O wins and Draw.

| State | Event | Next state | Invariants |
|---|---|---|---|
| `X_TO_PUT` (initial) | Valid Put by X, no Win, Board not full | `O_TO_PUT` | I3, I4, I5 |
| `X_TO_PUT` | Valid Put by X that completes a Line | `X_WINS` | I6 |
| `X_TO_PUT` | Valid Put by X that fills the Board without a Win | `DRAW` | I7 |
| `X_TO_PUT` | Put on an occupied Square (`put` returns false) | `X_TO_PUT` | I3 |
| `O_TO_PUT` | Valid Put by O, no Win | `X_TO_PUT` | I3, I4, I5 |
| `O_TO_PUT` | Valid Put by O that completes a Line | `O_WINS` | I6 |
| `O_TO_PUT` | Put on an occupied Square (`put` returns false) | `O_TO_PUT` | I3 |
| `X_WINS`, `O_WINS`, `DRAW` | Any Put | No change: the game is over | I8 |

O never fills the Board: with 9 Squares, the ninth Put is always X's.

Invalid input (I9) never reaches the domain: the console rejects it and the State does not change.

## Three-pieces variant (CR-2)

The three-pieces variant adds `X_TO_MOVE` and `O_TO_MOVE` states. There is no Draw (I15).

| State | Event | Next state | Invariants |
|---|---|---|---|
| `X_TO_PUT` (initial) | Valid Put by X, no Win, O still has Pieces remaining | `O_TO_PUT` | I11, I12 |
| `X_TO_PUT` | Valid Put by X, no Win, O has 0 Pieces remaining (both done) | `O_TO_MOVE` | I11, I12 |
| `X_TO_PUT` | Valid Put by X that completes a Line | `X_WINS` | I14 |
| `O_TO_PUT` | Valid Put by O, no Win, X still has Pieces remaining | `X_TO_PUT` | I11, I12 |
| `O_TO_PUT` | Valid Put by O, no Win, X has 0 Pieces remaining (both done) | `X_TO_MOVE` | I11, I12 |
| `O_TO_PUT` | Valid Put by O that completes a Line | `O_WINS` | I14 |
| `X_TO_MOVE` | Valid Move by X, no Win | `O_TO_MOVE` | I13 |
| `X_TO_MOVE` | Valid Move by X that completes a Line | `X_WINS` | I14 |
| `O_TO_MOVE` | Valid Move by O, no Win | `X_TO_MOVE` | I13 |
| `O_TO_MOVE` | Valid Move by O that completes a Line | `O_WINS` | I14 |
| Any PUT/MOVE state | Invalid (rejected) | unchanged | I3, I13 |
| `X_WINS`, `O_WINS` | Any Put or Move | No change: game is over | I8 |

Note: X puts on rounds 1, 3, 5; O puts on rounds 2, 4, 6. After O's third Put (round 6), both Players have 0 Pieces remaining, and it is X's turn again → X_TO_MOVE. There is one special case: if X wins on round 5 (their third Put), the game ends immediately.
