# Executive summary

Bob built the same Tic-Tac-Toe twice with the same prompts: **without-ddd** with the requirements only, **with-ddd** with the requirements plus the DDD documentation (glossary, model, invariants, states and modules). Each phase was a new session without memory: F1 base game, F2 against the PC, F3 three pieces that move.

> One execution per folder: the differences are observations of this execution, not proof. The detail of every number is in `metrics.md`.

## Scoreboard

In bold, the best value of each phase. Not bold: a tie or a descriptive metric.

| Metric | F1 without DDD | F1 with DDD | F2 without DDD | F2 with DDD | F3 without DDD | F3 with DDD |
|---|---|---|---|---|---|---|
| **Accuracy** | | | | | | |
| Correct games according to the log (move by move) | 9/9 | 9/9 | 12/12 | 12/12 | 16/16 | 16/16 |
| Hidden games with the exact console output | 9/9 | 9/9 | 12/12 | 12/12 | 16/16 | 16/16 |
|   · with the correct logic, even if the format fails | 9/9 | 9/9 | 12/12 | 12/12 | 16/16 | 16/16 |
| The console shows the same board as the log | 9/9 | 9/9 | 12/12 | 12/12 | 16/16 | 16/16 |
| F1 regressions | 9/9 | 9/9 | 9/9 | 9/9 | 9/9 | 9/9 |
| Mutation score without the console (%) | 78 | **96** | 80 | **94** | 79 | **93** |
| Bob's tests that pass (%) | 100.0 | 100.0 | 100.0 | 100.0 | 100.0 | 100.0 |
| Bob's suite agrees with the hidden games | yes | yes | yes | yes | yes | yes |
| Escapes: hidden games that fail with the suite green | 0 | 0 | 0 | 0 | 0 | 0 |
| **Agent cost** | | | | | | |
| Time (s) | **197** | 210 | **262** | 476 | **279** | 281 |
| Context at the end (k tokens) | **40.0** | 48.4 | **81.5** | 118.9 | 114.7 | **79.7** |
| Cost (Bobcoins) | **1.69** | 2.52 | **2.48** | 5.91 | **3.97** | 4.47 |
| Bobcoins per correct hidden game | **0.188** | 0.28 | **0.207** | 0.492 | **0.248** | 0.279 |
| Seconds per correct hidden game | **21.9** | 23.3 | **21.8** | 39.7 | **17.4** | 17.6 |
| Bobcoins per game with the correct logic | **0.188** | 0.28 | **0.207** | 0.492 | **0.248** | 0.279 |
| Bobcoins per correct game according to the log | **0.188** | 0.28 | **0.207** | 0.492 | **0.248** | 0.279 |
| **How Bob worked** | | | | | | |
| Docs read before the first edit | 2 | 8 | 1 | 8 | 2 | 8 |
| Code files read before the first edit | **0** | 1 | **6** | 8 | **8** | 12 |
| Code reads in the whole session | 0 | 0 | **5** | 7 | **6** | 10 |
| Model calls | **31** | 36 | **26** | 47 | **35** | 43 |
| **Autonomy** | | | | | | |
| Human interventions (messages and rejected approvals) | 0 | 0 | 0 | 0 | 0 | 0 |
| Questions to the user | 0 | 0 | 0 | 0 | 0 | 0 |
| Denied commands it tried | 1 | 1 | **0** | 1 | 2 | **1** |
| Result Bob declared | done | done | done | done | done | done |
| Own decisions | 5 | 5 | **4** | 5 | **4** | 5 |
|   · because of gaps or ambiguities in the documentation | **1** | 3 | 1 | **0** | 1 | 1 |
| **Design** | | | | | | |
| Classes · production lines | 5 · 182 | 12 · 355 | 6 · 249 | 15 · 439 | 7 · 474 | 15 · 549 |
| Lines per file (max) | **69** | 104 | **69** | 110 | 163 | **151** |
| Methods with more than 30 lines | 1 | **0** | 1 | **0** | 2 | **1** |
| Methods with more than 3 parameters | **0** | 1 | 1 | 1 | 3 | **1** |
| Ce max (classes the most coupled one uses) | **4** | 7 | **5** | 8 | **6** | 8 |
| Propagation cost (%) | 56.0 | **38.2** | 52.8 | **48.0** | 49.0 | **48.0** |
| Yourdon & Constantine index (1 data … 6 content) | **1.12** | 1.5 | **1.18** | 1.56 | **1.14** | 1.56 |
| Worst Yourdon & Constantine connection | stamp | external | stamp | external | stamp | external |
| TCC median (higher = more cohesive) | 0.75 | **0.9** | 0.75 | **0.95** | 0.6 | **0.95** |
| LCOM* median (lower = more cohesive) | **0.34** | 0.67 | **0.34** | 0.67 | **0.56** | 0.65 |
| Public methods that change state | **0** | 1 | **0** | 1 | **0** | 2 |
| Getters that expose modifiable state | 0 | 0 | 0 | 0 | 0 | 0 |
| Classes with console I/O | 1 | 2 | 1 | 2 | 1 | 2 |
| **Change** | | | | | | |
| Existing classes modified | — | — | **1** | 3 | **2** | 6 |
| Lines changed in existing classes | — | — | **74** | 105 | **191** | 232 |
| Existing tests touched | — | — | **0** | 2 | 0 | 0 |
| Bobcoins per 100 lines touched | — | — | **1.84** | 3.54 | **1.37** | 1.93 |
| **Language** | | | | | | |
| Discarded terms in the code | 1 | **0** | 4 | **0** | 4 | **0** |
| Glossary terms present | 7/17 | **17/17** | 9/20 | **20/20** | 11/23 | **22/23** |

## Cumulative cost

| | without-ddd | with-ddd | with-ddd ÷ without-ddd |
|---|---|---|---|
| Time (s) | 738 | 967 | 1.31× |
| Cost (Bobcoins) | 8.14 | 12.9 | 1.58× |

Phases included: F1, F2, F3.

## Efficiency: correct games against cost and time

Each phase plays its hidden games and those of the previous phases (regressions). Fewer Bobcoins and seconds per correct game = more efficient.

If a folder fails only because of the console format, the "correct logic" columns still let you compare it.

| Phase | Folder | Correct according to the log | Exact console | Console with the correct logic | Bobcoins | Time (s) | Bobcoins per game (log) | Bobcoins per game (exact console) | Bobcoins per game (logic) | Seconds per game (logic) |
|---|---|---|---|---|---|---|---|---|---|---|
| F1 | without-ddd | 9/9 | 9/9 | 9/9 | 1.69 | 197 | 0.188 | 0.188 | 0.188 | 21.9 |
| F1 | with-ddd | 9/9 | 9/9 | 9/9 | 2.52 | 210 | 0.28 | 0.28 | 0.28 | 23.3 |
| F2 | without-ddd | 12/12 | 12/12 | 12/12 | 2.48 | 262 | 0.207 | 0.207 | 0.207 | 21.8 |
| F2 | with-ddd | 12/12 | 12/12 | 12/12 | 5.91 | 476 | 0.492 | 0.492 | 0.492 | 39.7 |
| F3 | without-ddd | 16/16 | 16/16 | 16/16 | 3.97 | 279 | 0.248 | 0.248 | 0.248 | 17.4 |
| F3 | with-ddd | 16/16 | 16/16 | 16/16 | 4.47 | 281 | 0.279 | 0.279 | 0.279 | 17.6 |
| **Total** | without-ddd | 37/37 | 37/37 | 37/37 | 8.14 | 738 | 0.22 | 0.22 | 0.22 | 19.9 |
| **Total** | with-ddd | 37/37 | 37/37 | 37/37 | 12.9 | 967 | 0.349 | 0.349 | 0.349 | 26.1 |

## Scalability: what happens while the system grows

A design that scales grows in lines without growing its propagation cost, its coupling or the cost of each change, and without losing correct games.

| Folder | Phase | Lines · files | Lines per file (max) | Methods > 30 lines | Propagation cost (%) | Y&C index | TCC median | Correct games (log, or console in series 1) | Bobcoins | Bobcoins per 100 lines touched |
|---|---|---|---|---|---|---|---|---|---|---|
| without-ddd | F1 | 182 · 5 | 69 | 1 | 56.0 | 1.12 | 0.75 | 9/9 | 1.69 | — |
| without-ddd | F2 | 249 · 6 | 69 | 1 | 52.8 | 1.18 | 0.75 | 12/12 | 2.48 | 1.84 |
| without-ddd | F3 | 474 · 7 | 163 | 2 | 49.0 | 1.14 | 0.6 | 16/16 | 3.97 | 1.37 |
| with-ddd | F1 | 355 · 12 | 104 | 0 | 38.2 | 1.5 | 0.9 | 9/9 | 2.52 | — |
| with-ddd | F2 | 439 · 15 | 110 | 0 | 48.0 | 1.56 | 0.95 | 12/12 | 5.91 | 3.54 |
| with-ddd | F3 | 549 · 15 | 151 | 1 | 48.0 | 1.56 | 0.95 | 16/16 | 4.47 | 1.93 |

- **without-ddd**, from F1 to F3: lines went from 182 to 474 (+160 %), the propagation cost from 56.0 % to 49.0 %, the Y&C index from 1.12 to 1.14, correct games from 100 % to 100 %.
- **with-ddd**, from F1 to F3: lines went from 355 to 549 (+55 %), the propagation cost from 38.2 % to 48.0 %, the Y&C index from 1.5 to 1.56, correct games from 100 % to 100 %.

With several archived runs, `python3 measure.py stats` writes to `runs/stats.md` the medians, whether each difference repeats in every run, and the correlations between design, accuracy and cost.

## Findings

### F1
**without-ddd**
- Every hidden game correct (9/9).
- Gaps Bob had to decide on its own: What should the program print for prompts?
- Discarded terms in the code: place (place).

**with-ddd**
- Every hidden game correct (9/9).
- Gaps Bob had to decide on its own: The ArchitectureTest's `domain_does_not_print` rule uses `ACCESS_STANDARD_STREAMS` which checks for use of `System.out/err/in`. The test classes `BoardTest`, `SquareTest` etc. live in `tictactoe.domain` package but the ArchUnit import option is `DoNotIncludeTests` so they are excluded from the check. / Board log: the "board" array lists only occupied squares "in any order". What order to use? / What should the "Invalid" message say exactly? REQUIREMENTS.md only says print a line that starts with `Invalid`.
- Public methods that change state: domain.TicTacToe.put.

### F2
**without-ddd**
- Every hidden game correct (12/12).
- Gaps Bob had to decide on its own: The requirements say "the same input format for the Human Player" with `--vs-pc`; should the human still see a prompt?
- Discarded terms in the code: Grid (grid), move (chooseMove, findWinningMove, move), place (place), play (play).
- Existing classes modified: Main.

**with-ddd**
- Every hidden game correct (12/12).
- Public methods that change state: domain.TicTacToe.put.
- Existing classes modified: Main, ConsoleGame, Player.
- Domain docs updated: decisions.md, glossary.md, model.puml, modules.md.

### F3
**without-ddd**
- Every hidden game correct (16/16).
- Gaps Bob had to decide on its own: What happens when `null` comes from `scanner.nextLine()` in the move phase (EOF mid-game)?
- Discarded terms in the code: Grid (grid), Phase (isPutPhase, movePhase), place (place), play (play).
- Existing classes modified: BoardLog, Main.

**with-ddd**
- Every hidden game correct (16/16).
- Gaps Bob had to decide on its own: What error message should appear for an invalid move?
- Public methods that change state: domain.TicTacToe.move, domain.TicTacToe.put.
- Existing classes modified: Main, ConsoleGame, Board, Square, State, TicTacToe.
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
