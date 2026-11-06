Change request CR-2: three Pieces each

Add a variant in which each Player has only three Pieces and, once they are all on the Board, moves them.

- Start: `java -jar target/tictactoe.jar --three-pieces`, for two Human Players. The previous modes (no arguments, and `--vs-ai`) must keep working exactly as before. Combining `--three-pieces` with `--vs-ai` is out of scope.
- While a Player has Pieces left, they put as usual, so rounds 1 to 6 are Puts.
- From round 7 on, the current Player moves one of their own Pieces to an empty Square. Input: one line with four whole numbers, `fromRow fromColumn toRow toColumn`, for example `1 1 2 3`.
- A move is invalid when the origin does not hold a Piece of the current Player, when the target is not empty, or when a number is outside 1–3. A line with the wrong amount of numbers is also invalid. Invalid input prints a line that starts with `Invalid`, and the same Player tries again.
- Win does not change, and it is checked after every Put and every move. This variant has no Draw: the game continues until someone wins.
- Otherwise the console contract does not change: the same final board and result line.

Follow AGENTS.md and the rules in .bob/rules. You are done when `mvn -q verify` passes.
