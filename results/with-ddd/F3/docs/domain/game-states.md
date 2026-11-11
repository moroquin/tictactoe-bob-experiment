# Game states

The State of a game is `State` in the code. `TicTacToe.state()` returns it, and `State.isOver()` is true for X wins, O wins and Draw. `State.isMoving()` is true for `X_TO_MOVE` and `O_TO_MOVE`.

## Standard variant (no piece limit)

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

## Three-pieces variant (`--three-pieces`)

In this variant each Player has exactly 3 Pieces. Rounds 1–6 are Puts. From round 7 on, Players move.

| State | Event | Next state | Invariants |
|---|---|---|---|
| `X_TO_PUT` (initial) | Valid Put by X, no Win, X still has pieces left | `O_TO_PUT` | I3, I4, I5, I12 |
| `X_TO_PUT` | Valid Put by X, no Win, X's last piece (O still has pieces) | `O_TO_PUT` | I3, I4, I5, I12 |
| `X_TO_PUT` | Valid Put by X that completes a Line | `X_WINS` | I6 |
| `X_TO_PUT` | Put on an occupied Square (`put` returns false) | `X_TO_PUT` | I3 |
| `O_TO_PUT` | Valid Put by O, no Win, O still has pieces left | `X_TO_PUT` | I3, I4, I5, I12 |
| `O_TO_PUT` | Valid Put by O, no Win, O's last piece (X in move phase already) | `X_TO_MOVE` | I3, I4, I5, I12 |
| `O_TO_PUT` | Valid Put by O that completes a Line | `O_WINS` | I6 |
| `O_TO_PUT` | Put on an occupied Square (`put` returns false) | `O_TO_PUT` | I3 |
| `X_TO_MOVE` | Valid Move by X, no Win | `O_TO_MOVE` | I4, I11 |
| `X_TO_MOVE` | Valid Move by X that completes a Line | `X_WINS` | I6, I11 |
| `X_TO_MOVE` | Invalid Move (`move` returns false) | `X_TO_MOVE` | I11 |
| `O_TO_MOVE` | Valid Move by O, no Win | `X_TO_MOVE` | I4, I11 |
| `O_TO_MOVE` | Valid Move by O that completes a Line | `O_WINS` | I6, I11 |
| `O_TO_MOVE` | Invalid Move (`move` returns false) | `O_TO_MOVE` | I11 |
| `X_WINS`, `O_WINS` | Any Put or Move | No change: the game is over | I8 |

There is no Draw in the three-pieces variant (I12).

Invalid input (I9) never reaches the domain: the console rejects it and the State does not change.

## The State in the board log

The board log of `REQUIREMENTS.md` is an output contract, like the console, so it uses the contract's names and not the domain's. `tictactoe.console` writes it and translates the State; the domain does not change and does not know the log exists.

| `State` in the domain | `"state"` in the board log |
|---|---|
| `X_TO_PUT`, `O_TO_PUT` | `"IN_PROGRESS"` |
| `X_TO_MOVE`, `O_TO_MOVE` | `"IN_PROGRESS"` |
| `X_WINS` | `"X_WINS"` |
| `O_WINS` | `"O_WINS"` |
| `DRAW` | `"DRAW"` |

Any State in which the game goes on, including those a change request adds, is `"IN_PROGRESS"` in the log. Never write the name of a domain State such as `X_TO_PUT` in the log.
