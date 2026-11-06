Change request CR-1: Human vs. AI

Add a new kind of Player, the AI: a Player controlled by the program. The existing Players become Human Players.

- Start: `java -jar target/tictactoe.jar --vs-ai`. Without arguments, the game must keep working exactly as before, with two Human Players.
- With `--vs-ai`, the Human Player plays X and starts. The AI plays O and puts automatically, without reading input.
- The AI is deterministic: no randomness, no external services and no language models. In its round it chooses the target Square with these rules, in this order:
  1. Win: if it can complete three in a line with one put, it puts there.
  2. Block: otherwise, if X could complete three in a line with its next put, it puts on that Square.
  3. Center: otherwise, if (2,2) is empty, it puts there.
  4. Corner: otherwise, it puts on the first empty corner in this order: (1,1), (1,3), (3,1), (3,3).
  5. Otherwise, it puts on the first empty Square in reading order: row 1 to 3 and, within a row, column 1 to 3.
  When rule 1 or rule 2 matches more than one Square, it uses the first one in reading order.
- The console contract does not change: the same input format for the Human Player, the same `Invalid` line, and the same final board and result line.

Follow AGENTS.md and the rules in .bob/rules. You are done when `mvn -q verify` passes.
