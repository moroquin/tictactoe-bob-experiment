# Executive summary

Bob built the same Tic-Tac-Toe twice with the same prompts: **without-ddd** with the requirements only, **with-ddd** with the requirements plus the DDD documentation (glossary, model, invariants, states and modules). Each phase was a new session without memory: F1 base game, F2 against the PC, F3 three pieces that move.

> One execution per folder: the differences are observations of this execution, not proof. The detail of every number is in `metrics.md`.

## Scoreboard

In bold, the best value of each phase. Not bold: a tie or a descriptive metric.

| Metric | F1 without DDD | F1 with DDD | F2 without DDD | F2 with DDD | F3 without DDD | F3 with DDD |
|---|---|---|---|---|---|---|
| **Accuracy** | | | | | | |
| Hidden games with the exact console output | 0/9 | **9/9** | 0/12 | **12/12** | 0/16 | **16/16** |
|   · with the correct logic, even if the format fails | 9/9 | 9/9 | 11/12 | **12/12** | 15/16 | **16/16** |
| F1 regressions | 0/9 | **9/9** | 0/9 | **9/9** | 0/9 | **9/9** |
| Mutation score without the console (%) | **96** | 90 | **94** | 88 | **93** | 90 |
| **Agent cost** | | | | | | |
| Time (s) | **183** | 235 | **175** | 428 | **110** | 351 |
| Context at the end (k tokens) | **25.4** | 46.7 | **35.4** | 94.8 | **34.4** | 68.1 |
| Cost (Bobcoins) | **1.06** | 2.12 | **1.58** | 5.05 | **1.0** | 4.09 |
| Bobcoins per correct hidden game | — | 0.236 | — | 0.421 | — | 0.256 |
| Seconds per correct hidden game | — | 26.1 | — | 35.7 | — | 21.9 |
| Bobcoins per game with the correct logic | **0.118** | 0.236 | **0.144** | 0.421 | **0.067** | 0.256 |
| **How Bob worked** | | | | | | |
| Docs read before the first edit | 1 | 7 | 1 | 8 | 1 | 8 |
| Code files read before the first edit | **0** | 1 | **4** | 9 | **5** | 12 |
| Code reads in the whole session | 0 | 0 | **4** | 7 | **3** | 11 |
| Model calls | **27** | 31 | **29** | 47 | **18** | 44 |
| **Autonomy** | | | | | | |
| Own decisions | 3 | 3 | **4** | 6 | **4** | 6 |
|   · because of gaps or ambiguities in the documentation | 2 | **1** | 1 | **0** | 2 | 2 |
| **Design** | | | | | | |
| Classes · production lines | 3 · 113 | 12 · 317 | 4 · 168 | 14 · 384 | 5 · 279 | 16 · 583 |
| Lines per file (max) | **44** | 63 | **47** | 68 | 113 | **93** |
| Methods with more than 30 lines | 1 | **0** | 1 | **0** | 2 | **0** |
| Methods with more than 3 parameters | 0 | 0 | 0 | 0 | 1 | **0** |
| Ce max (classes the most coupled one uses) | **2** | 7 | **3** | 7 | **4** | 8 |
| Propagation cost (%) | 66.7 | **38.2** | 56.2 | **48.0** | 48.0 | **46.5** |
| Yourdon & Constantine index (1 data … 6 content) | **1** | 1.5 | **1.2** | 1.44 | **1.14** | 1.58 |
| Worst Yourdon & Constantine connection | data | external | stamp | external | stamp | external |
| TCC median (higher = more cohesive) | 0.75 | **0.95** | 0.75 | **0.95** | 0.62 | **0.9** |
| LCOM* median (lower = more cohesive) | 0.64 | **0.58** | 0.64 | **0.58** | **0.6** | 0.62 |
| Public methods that change state | 2 | **1** | 2 | **1** | 3 | 3 |
| Getters that expose modifiable state | 1 | **0** | 1 | **0** | 2 | **0** |
| Classes with console I/O | 1 | 2 | 1 | 2 | 1 | 3 |
| **Change** | | | | | | |
| Existing classes modified | — | — | **1** | 3 | **1** | 4 |
| Lines changed in existing classes | — | — | **20** | 60 | 90 | **59** |
| Existing tests touched | — | — | **0** | 2 | 0 | 0 |
| Bobcoins per 100 lines touched | — | — | **2.36** | 4.21 | **0.75** | 1.76 |
| **Language** | | | | | | |
| Discarded terms in the code | 3 | **0** | 4 | **1** | 5 | **1** |
| Glossary terms present | 6/17 | **17/17** | 9/19 | **19/19** | 11/21 | **21/21** |

## Cumulative cost

| | without-ddd | with-ddd | with-ddd ÷ without-ddd |
|---|---|---|---|
| Time (s) | 468 | 1014 | 2.17× |
| Cost (Bobcoins) | 3.64 | 11.26 | 3.09× |

Phases included: F1, F2, F3.

## Efficiency: correct games against cost and time

Each phase plays its hidden games and those of the previous phases (regressions). Fewer Bobcoins and seconds per correct game = more efficient.

If a folder fails only because of the console format, the "correct logic" columns still let you compare it.

| Phase | Folder | Correct according to the log | Exact console | Console with the correct logic | Bobcoins | Time (s) | Bobcoins per game (log) | Bobcoins per game (exact console) | Bobcoins per game (logic) | Seconds per game (logic) |
|---|---|---|---|---|---|---|---|---|---|---|
| F1 | without-ddd | — | 0/9 | 9/9 | 1.06 | 183 | — | — | 0.118 | 20.3 |
| F1 | with-ddd | — | 9/9 | 9/9 | 2.12 | 235 | — | 0.236 | 0.236 | 26.1 |
| F2 | without-ddd | — | 0/12 | 11/12 | 1.58 | 175 | — | — | 0.144 | 15.9 |
| F2 | with-ddd | — | 12/12 | 12/12 | 5.05 | 428 | — | 0.421 | 0.421 | 35.7 |
| F3 | without-ddd | — | 0/16 | 15/16 | 1.0 | 110 | — | — | 0.067 | 7.3 |
| F3 | with-ddd | — | 16/16 | 16/16 | 4.09 | 351 | — | 0.256 | 0.256 | 21.9 |
| **Total** | without-ddd | — | 0/37 | 35/37 | 3.64 | 468 | — | — | 0.104 | 13.4 |
| **Total** | with-ddd | — | 37/37 | 37/37 | 11.26 | 1014 | — | 0.304 | 0.304 | 27.4 |

## Scalability: what happens while the system grows

A design that scales grows in lines without growing its propagation cost, its coupling or the cost of each change, and without losing correct games.

| Folder | Phase | Lines · files | Lines per file (max) | Methods > 30 lines | Propagation cost (%) | Y&C index | TCC median | Correct games (log, or console in series 1) | Bobcoins | Bobcoins per 100 lines touched |
|---|---|---|---|---|---|---|---|---|---|---|
| without-ddd | F1 | 113 · 3 | 44 | 1 | 66.7 | 1 | 0.75 | 0/9 | 1.06 | — |
| without-ddd | F2 | 168 · 4 | 47 | 1 | 56.2 | 1.2 | 0.75 | 0/12 | 1.58 | 2.36 |
| without-ddd | F3 | 279 · 5 | 113 | 2 | 48.0 | 1.14 | 0.62 | 0/16 | 1.0 | 0.75 |
| with-ddd | F1 | 317 · 12 | 63 | 0 | 38.2 | 1.5 | 0.95 | 9/9 | 2.12 | — |
| with-ddd | F2 | 384 · 14 | 68 | 0 | 48.0 | 1.44 | 0.95 | 12/12 | 5.05 | 4.21 |
| with-ddd | F3 | 583 · 16 | 93 | 0 | 46.5 | 1.58 | 0.9 | 16/16 | 4.09 | 1.76 |

- **without-ddd**, from F1 to F3: lines went from 113 to 279 (+147 %), the propagation cost from 66.7 % to 48.0 %, the Y&C index from 1 to 1.14, correct games from 0 % to 0 %.
- **with-ddd**, from F1 to F3: lines went from 317 to 583 (+84 %), the propagation cost from 38.2 % to 46.5 %, the Y&C index from 1.5 to 1.58, correct games from 100 % to 100 %.

With several archived runs, `python3 measure.py stats` writes to `runs/stats.md` the medians, whether each difference repeats in every run, and the correlations between design, accuracy and cost.

## Findings

### F1
**without-ddd**
- Hidden games that fail: F1-01 (X wins on row 1); F1-02 (O wins on column 2); F1-03 (X wins on the diagonal); F1-04 (O wins on the inverse diagonal); F1-05 (X wins on column 3); F1-06 (Draw); F1-07 (Occupied, out of range and garbage are rejected; X wins); F1-08 (Win on the 9th put beats Draw); F1-09 (Wrong amount of numbers and zero are rejected).
- With the correct game logic: 9/9. The rest fail because of the format: the output breaks the console contract (for example, a prompt without a line break glued to the first row of the board).
- Gaps Bob had to decide on its own: REQUIREMENTS.md says "any other output … is allowed before the final board" — what to print as prompts? / What should happen if stdin ends before the game is over (e.g. in a script that supplies too few moves)?
- Exposed state: Game.board() returns Board.
- Public methods that change state: Board.place, Game.tryMove.
- Discarded terms in the code: Status (Status, status), move (tryMove), place (place).
- This phase's snapshot was rebuilt from bob.db (it was not taken in time) and validated against the reads of the next phase.

**with-ddd**
- Every hidden game correct (9/9).
- Gaps Bob had to decide on its own: The test for I6 "win checked before draw" had a move sequence where O actually won before X; the test was wrong
- Public methods that change state: domain.TicTacToe.put.
- This phase's snapshot was rebuilt from bob.db (it was not taken in time) and validated against the reads of the next phase.

### F2
**without-ddd**
- Hidden games that fail: F1-01 (X wins on row 1); F1-02 (O wins on column 2); F1-03 (X wins on the diagonal); F1-04 (O wins on the inverse diagonal); F1-05 (X wins on column 3); F1-06 (Draw); F1-07 (Occupied, out of range and garbage are rejected; X wins); F1-08 (Win on the 9th put beats Draw); F1-09 (Wrong amount of numbers and zero are rejected); F2-01 (PC takes the center, a corner, then wins); F2-02 (Human wins with a fork); F2-03 (Square taken by the PC is rejected; Draw).
- With the correct game logic: 11/12. The rest fail because of the format: the output breaks the console contract (for example, a prompt without a line break glued to the first row of the board).
- Gaps Bob had to decide on its own: Which piece does the PC play in `--vs-pc` mode?
- Exposed state: Game.board() returns Board.
- Public methods that change state: Board.place, Game.tryMove.
- Discarded terms in the code: Cell (cell), Status (Status, status), move (chooseMove, findWinningMove, move, tryMove), place (place).
- Existing classes modified: Main.
- Bob modified REQUIREMENTS.md, which had to stay the same in both folders.

**with-ddd**
- Every hidden game correct (12/12).
- Public methods that change state: domain.TicTacToe.put.
- Discarded terms in the code: Index (readingOrderIndex).
- Existing classes modified: Main, ConsoleGame, Player.
- Bob modified REQUIREMENTS.md, which had to stay the same in both folders.
- Domain docs updated: decisions.md, glossary.md, model.puml, modules.md.

### F3
**without-ddd**
- Hidden games that fail: F1-01 (X wins on row 1); F1-02 (O wins on column 2); F1-03 (X wins on the diagonal); F1-04 (O wins on the inverse diagonal); F1-05 (X wins on column 3); F1-06 (Draw); F1-07 (Occupied, out of range and garbage are rejected; X wins); F1-08 (Win on the 9th put beats Draw); F1-09 (Wrong amount of numbers and zero are rejected); F2-01 (PC takes the center, a corner, then wins); F2-02 (Human wins with a fork); F2-03 (Square taken by the PC is rejected; Draw); F3-01 (Win while putting the third piece); F3-02 (X wins by moving a piece); F3-03 (Invalid moves are rejected; X wins); F3-04 (O wins by moving a piece).
- With the correct game logic: 15/16. The rest fail because of the format: the output breaks the console contract (for example, a prompt without a line break glued to the first row of the board).
- Gaps Bob had to decide on its own: What should `isPutPhase()` report when both players are in different phases (e.g. X placed 3, O placed 2)? / The CR says a line with the wrong number of tokens is "also invalid" — should the put-phase check for 4 tokens (a move command) be rejected with "Invalid"?
- Exposed state: Game.board() returns Board, ThreePiecesGame.board() returns Board.
- Public methods that change state: Board.place, Game.tryMove, ThreePiecesGame.tryPut.
- Discarded terms in the code: Cell (cell), Index (playerIndex), Phase (isPutPhase), Status (Status, status, updateStatusOrSwitch), place (place).
- Existing classes modified: Main.
- Bob modified REQUIREMENTS.md, which had to stay the same in both folders.

**with-ddd**
- Every hidden game correct (16/16).
- Gaps Bob had to decide on its own: The sequence "after both players have 0 pieces remaining, who moves first?" / The CR says move input is "fromRow fromColumn toRow toColumn" — should the console also validate that from ≠ to?
- Public methods that change state: domain.ThreePiecesTicTacToe.move, domain.ThreePiecesTicTacToe.put, domain.TicTacToe.put.
- Discarded terms in the code: Index (readingOrderIndex).
- Existing classes modified: Main, Board, Square, State.
- Bob modified REQUIREMENTS.md, which had to stay the same in both folders.
- Domain docs updated: decisions.md, game-states.md, glossary.md, invariants.md, model.puml, modules.md.

## How to read the metrics

- **JSONL log (series 2):** the program writes the board and the state after each move to the file in `TICTACTOE_BOARD_LOG`. A game is correct if it starts empty, each line is a valid put or move by the player whose turn it is, and the final board and state are the expected ones. The console is still measured separately: it is what the user sees.
- **Ce (efferent coupling):** how many project classes a class uses. Lower is better.
- **Ca (afferent coupling):** how many classes use it. It is only fine to be high for stable classes, such as an enum.
- **Propagation cost:** for each change in a class, what part of the system it can reach, directly or transitively (MacCormack et al., 2006). Lower is better; if it grows with each phase, the design scales badly.
- **Yourdon & Constantine:** the type of connection between classes, from best to worst: 1 data (simple values), 2 stamp (passes a whole object), 3 control (passes a boolean or enum flag that decides what the other one does), 4 external (they share the console or another device), 5 common (they share mutable global data), 6 content (one touches the other's fields). The index is the mean per class pair.
- **TCC and LCC:** share of pairs of visible methods that share a field, directly (TCC) or through a chain (LCC). 1 is very cohesive; TCC < 0.5, not very cohesive.
- **LCOM\*:** lack of cohesion. 0 means every method uses every field; 1 or more, that the class mixes responsibilities. Getters are excluded.
- **Size:** lines per file and per method without blank lines or comments; a method with more than 30 lines or more than 3 parameters usually does too much.
- **Efficiency:** Bobcoins and seconds per correct hidden game. **Bobcoins per 100 lines touched** (F2 and F3) is the marginal cost of the change.
- **Mutation score:** PIT introduces small bugs in the code and counts how many the tests detect. It is shown without the console, which neither folder tests.
- **Encapsulation:** the public methods that change state are the doors to modify the game; a getter that exposes modifiable state lets callers skip the rules.
- **Open/Closed:** a well-absorbed change adds classes and touches few lines of the existing ones.
- **Context:** what filled Bob's window at the end, not the total consumed; the cost in Bobcoins reflects the spend better.
