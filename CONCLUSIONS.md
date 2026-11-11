# Conclusions

> **Notice (section 0):** There is a single execution per folder. All differences below are observations of one run, not proof. Medians would require 3 to 5 executions per folder. The numbers in this document come exclusively from `results/metrics.md` and `results/metrics.json`; where a value is absent, "no data" is written.

---

## 1. Summary

`without-ddd` built a correct, compact game (5–7 classes, 182–474 lines) at lower cost in every phase (cumulative 8.14 Bobcoins vs. 12.9) and with better cohesion by LCOM\* median. `with-ddd` produced a domain aligned with the glossary (17/17 and 22/23 terms present, zero discarded), dramatically higher mutation scores (96 % vs. 78 % in F1, 93 % vs. 79 % in F3), and a lower propagation cost from F1 onward (38.2 % vs. 56.0 % in F1, 48.0 % vs. 49.0 % in F3). Both folders passed 100 % of all hidden games in all phases, both correct according to the JSONL log and on the console. The DDD documentation made Bob more consistent in vocabulary and test coverage, and it made CR-2 land in far fewer lines (none new, six modified) compared to the `without-ddd` approach (one new class, two modified); but it roughly doubled cost in F2 and added more classes everywhere, which inflated the Y&C index and LCOM\*.

---

## 2. Table per phase

### F1

| Metric | without-ddd | with-ddd |
|---|---|---|
| Duration (s) | 197 | 210 |
| Context at the end (k tokens) | 40.0 | 48.4 |
| Cost (Bobcoins) | 1.69 | 2.52 |
| Model calls | 31 | 36 |
| Docs read before first edit | 2 | 8 |
| Code read before first edit | 0 | 1 |
| Code reads in whole session | 0 | 0 |
| Docs edited before the code? | No | No |
| Unit tests | 25 | 57 |
| Bob's tests pass (%) | 100.0 | 100.0 |
| Correct games — JSONL log | 9/9 | 9/9 |
| Console exact output | 9/9 | 9/9 |
| Console correct logic | 9/9 | 9/9 |
| Console matches log | 9/9 | 9/9 |
| Mutation score without console (%) | 78 | 96 |
| Mutants without console | 57/73 | 71/74 |
| Production classes · lines | 5 · 182 | 12 · 355 |
| Packages | 1 | 3 |
| Lines per file (median · max) | 50 · 69 | 19.5 · 104 |
| Methods with > 30 lines | 1 (Main.main 52 lines) | 0 |
| Methods with > 3 parameters | 0 | 1 (ConsoleGame.ConsoleGame 4 params) |
| Ce max | 4 | 7 |
| Propagation cost (%) | 56.0 | 38.2 |
| Y&C index (1–6) | 1.12 | 1.50 |
| Worst Y&C connection | stamp | external |
| TCC median | 0.75 | 0.90 |
| LCOM\* median | 0.34 | 0.67 |
| LCC median | 1.0 | 1.0 |
| Public methods that change state | 0 | 1 (TicTacToe.put) |
| Getters exposing mutable state | 0 | 0 |
| Classes with console I/O | 1 (Main) | 2 (Main, ConsoleGame) |
| Own decisions (total) | 5 | 5 |
| · gap / ambiguity | 1 / 0 | 3 / 0 |
| Discarded terms | 1 (place) | 0 |
| Glossary terms present | 7/17 | 17/17 |
| Bobcoins per correct game | 0.188 | 0.28 |
| Seconds per correct game | 21.9 | 23.3 |

### F2

| Metric | without-ddd | with-ddd |
|---|---|---|
| Duration (s) | 262 | 476 |
| Context at the end (k tokens) | 81.5 | 118.9 |
| Cost (Bobcoins) | 2.48 | 5.91 |
| Model calls | 26 | 47 |
| Docs read before first edit | 1 | 8 |
| Code read before first edit | 6 | 8 |
| Code reads in whole session | 5 | 7 |
| Docs edited before the code? | No | Yes |
| Unit tests | 36 | 66 |
| Bob's tests pass (%) | 100.0 | 100.0 |
| Correct games — JSONL log | 12/12 | 12/12 |
| Console exact output | 12/12 | 12/12 |
| Console correct logic | 12/12 | 12/12 |
| Console matches log | 12/12 | 12/12 |
| F1 regressions | 9/9 | 9/9 |
| Mutation score without console (%) | 80 | 94 |
| Mutants without console | 101/127 | 90/96 |
| Production classes · lines | 6 · 249 | 15 · 439 |
| Packages | 1 | 3 |
| Lines per file (median · max) | 55 · 69 | 23 · 110 |
| Methods with > 30 lines | 1 (Main.main 58 lines) | 0 |
| Methods with > 3 parameters | 1 (PcPlayer.wouldWin 4 params) | 1 (ConsoleGame.ConsoleGame 4 params) |
| Ce max | 5 | 8 |
| Propagation cost (%) | 52.8 | 48.0 |
| Y&C index (1–6) | 1.18 | 1.56 |
| Worst Y&C connection | stamp | external |
| TCC median | 0.75 | 0.95 |
| LCOM\* median | 0.34 | 0.67 |
| LCC median | 1.0 | 1.0 |
| Public methods that change state | 0 | 1 (TicTacToe.put) |
| Getters exposing mutable state | 0 | 0 |
| Classes with console I/O | 1 (Main) | 2 (Main, ConsoleGame) |
| New / modified / deleted classes | 1 / 1 / 0 | 3 / 3 / 0 |
| Open/Closed ratio | 0.5 | 0.5 |
| Lines changed in existing classes | 74 | 105 |
| Existing tests touched | 0 | 2 |
| Own decisions (total) | 4 | 5 |
| · gap / ambiguity | 1 / 0 | 0 / 0 |
| Discarded terms | 4 | 0 |
| Glossary terms present | 9/20 | 20/20 |
| Bobcoins per correct game | 0.207 | 0.492 |
| Seconds per correct game | 21.8 | 39.7 |
| Bobcoins per 100 lines touched | 1.84 | 3.54 |
| docs/domain updated | — | decisions.md, glossary.md, model.puml, modules.md |

### F3

| Metric | without-ddd | with-ddd |
|---|---|---|
| Duration (s) | 279 | 281 |
| Context at the end (k tokens) | 114.7 | 79.7 |
| Cost (Bobcoins) | 3.97 | 4.47 |
| Model calls | 35 | 43 |
| Docs read before first edit | 2 | 8 |
| Code read before first edit | 8 | 12 |
| Code reads in whole session | 6 | 10 |
| Docs edited before the code? | No | Yes |
| Unit tests | 57 | 84 |
| Bob's tests pass (%) | 100.0 | 100.0 |
| Correct games — JSONL log | 16/16 | 16/16 |
| Console exact output | 16/16 | 16/16 |
| Console correct logic | 16/16 | 16/16 |
| Console matches log | 16/16 | 16/16 |
| F1 regressions | 9/9 | 9/9 |
| F2 regressions | 3/3 | 3/3 |
| Mutation score without console (%) | 79 | 93 |
| Mutants without console | 175/221 | 126/136 |
| Production classes · lines | 7 · 474 | 15 · 549 |
| Packages | 1 | 3 |
| Lines per file (median · max) | 69 · 163 | 29 · 151 |
| Methods with > 30 lines | 2 (Main.runThreePiecesGame 79, Main.runStandardGame 44 lines) | 1 (TicTacToe.put 35 lines) |
| Methods with > 3 parameters | 3 (PcPlayer.wouldWin, ThreePiecesBoard constructor, ThreePiecesBoard.move) | 1 (ConsoleGame.ConsoleGame 4 params) |
| Ce max | 6 | 8 |
| Propagation cost (%) | 49.0 | 48.0 |
| Y&C index (1–6) | 1.14 | 1.56 |
| Worst Y&C connection | stamp | external |
| TCC median | 0.60 | 0.95 |
| LCOM\* median | 0.56 | 0.65 |
| LCC median | 1.0 | 1.0 |
| Public methods that change state | 0 | 2 (TicTacToe.put, TicTacToe.move) |
| Getters exposing mutable state | 0 | 0 |
| Classes with console I/O | 1 (Main) | 2 (Main, ConsoleGame) |
| New / modified / deleted classes | 1 / 2 / 0 | 0 / 6 / 0 |
| Open/Closed ratio | 0.33 | 0.0 |
| Lines changed in existing classes | 191 | 232 |
| Existing tests touched | 0 | 0 |
| Own decisions (total) | 4 | 5 |
| · gap / ambiguity | 1 / 0 | 1 / 0 |
| Discarded terms | 4 | 0 |
| Glossary terms present | 11/23 | 22/23 |
| Bobcoins per correct game | 0.248 | 0.279 |
| Seconds per correct game | 17.4 | 17.6 |
| Bobcoins per 100 lines touched | 1.37 | 1.93 |
| docs/domain updated | — | decisions.md, game-states.md, glossary.md, invariants.md, model.puml, modules.md |

---

## 3. Coupling, cohesion and encapsulation

### Efferent coupling, instability and propagation cost

`with-ddd` has more classes (12–15 vs. 5–7), so individual Ce values are naturally higher: the most coupled class, `domain.TicTacToe`, reaches Ce = 7 in all three phases, while `Main` reaches Ce = 8 by F2. In `without-ddd` the maximum Ce stays at 4 (F1) → 5 (F2) → 6 (F3). However, the propagation cost — the fraction of the system a single change can reach — favours `with-ddd` from F1: 38.2 % vs. 56.0 % in F1, 48.0 % vs. 52.8 % in F2, and 48.0 % vs. 49.0 % in F3. In a smaller system, each connection has a larger proportional impact; once `with-ddd` grows to 15 classes the many stable leaf classes (Color, Coordinate, Piece, State, Line) absorb change without rippling outwards.

Coupling to stable, never-changing classes (`domain.Color`, instability 0.0 in every phase; `domain.Coordinate`, instability 0.0 in every phase) is cheap: a change to neither of those is expected, so the coupling does not create real maintenance risk. In `without-ddd`, `Color` (instability 0.0) and `State` (instability 0.0) serve the same role.

### Yourdon and Constantine connections

`without-ddd` stayed at **stamp** as its worst connection type throughout all three phases. In `without-ddd` F1: 7 data + 1 stamp pair; F2: 9 data + 2 stamp; F3: 12 data + 2 stamp. No control, external, common or content connections were ever detected.

`with-ddd` reached **external** (level 4, sharing a side-effecting resource — the console) in every phase because `Main` and `console.ConsoleGame` both use `System.out`/`System.in`. The connection `Main ↔ console.ConsoleGame (console): external` was reported in F1, F2, and F3. In practice this is a deliberate architectural boundary: `Main` wires the players and hands the scanner and output stream to `ConsoleGame`, so both objects are aware of the same console. This is a real external coupling, not a false positive.

**Control connections** inside classes: in `without-ddd` `Board.place → Board.computeState` passes the current player's Color to a helper that returns the next State — a light control dependency, where the Color decides which win/draw branch is taken. In F3, additional control connections appeared inside `Main.main → Main.runStandardGame` (the `vsPC`/`threePieces` booleans decide which path runs), and `ThreePiecesBoard.move → ThreePiecesBoard.computeState` (the `movePhase` boolean argument). The detector flags these accurately: the boolean does determine which branch the callee takes. In `with-ddd` the same two internal pairs — `ConsoleGame.buildBoardLogJson → ConsoleGame.toLogState` and `ConsoleGame.printFinalBoard → ConsoleGame.colorChar` — were detected in all three phases. Both pass an enum (`State` or `Color`) that drives a switch; these are genuine control connections, though lightweight because the switch is a pure output translation with no side effects.

### File and method size

`without-ddd` grew its largest file from 54 lines (Main, F1) to 163 lines (Main, F3) and produced two methods with more than 30 lines in F3 (`Main.runThreePiecesGame` at 79 lines and `Main.runStandardGame` at 44 lines). Both methods are pure game loops composed of input validation, board mutation, and logging — repetitive rather than complex. `with-ddd` kept its maximum file at 104 lines (F1) → 110 (F2) → 151 (F3), and had no methods over 30 lines in F1 or F2; `TicTacToe.put` reached 35 lines in F3 because it handles the piece-limit decrement and state-transition logic for both the standard and three-pieces variants in one method.

`without-ddd` F3 also had three methods with more than 3 parameters (`PcPlayer.wouldWin`, `ThreePiecesBoard.ThreePiecesBoard`, `ThreePiecesBoard.move` each with 4), while `with-ddd` kept one (`ConsoleGame.ConsoleGame` with 4).

### Most coupled and least cohesive classes

**without-ddd:** The most coupled class is `Main` (Ce = 4/5/6 across phases), which is a wiring class by design — it reaches every domain class and is entirely instability-1. Its LCOM\* is undefined (no instance fields). In F3, `Main` carries two 40–79-line private static methods that duplicate input-parsing logic. `Board` (LCOM\* = 0.67, TCC = 0.5) is the least cohesive measured class, because it mixes state-reading (`at`, `state`, `currentPlayer`) with state-writing (`place`), and `state` is computed from three conceptually separate concerns (piece placement, win detection, draw detection).

**with-ddd:** The most coupled class is `domain.TicTacToe` (Ce = 7 in all phases, Ca = 2 F1, 6 F2/F3). It depends on Board, Round, Player, Piece, Coordinate, Color, and State — which is exactly what the aggregate root of the domain model is supposed to do. `console.ConsoleGame` has the highest LCOM\* among non-trivial classes (0.82 F1, 0.81 F2, 0.78 F3): it mixes input parsing, output printing, board-log writing, and the retry loop. This is a deliberate single-adapter choice (one class owns the console boundary), not a violation of the domain model.

### Coupling between stable and changing classes

Coupling to `domain.Color` and `domain.Coordinate` (instability 0.0) is widespread in `with-ddd` but cheap: 9 classes depend on `Color` (Ca = 9 in F3) and 10 depend on `Coordinate` (Ca = 10). These classes have never changed across phases. In `without-ddd`, `Color` and `State` are the stable leaves (Ca = 3–5).

### Encapsulation

Both `without-ddd`'s `Board` and `with-ddd`'s `TicTacToe` keep all instance fields private. `without-ddd` has 0 public methods that change state in every phase: `Board.place` is package-private and returns a new immutable Board; `Main` is the only code that drives the game. `with-ddd` has 1 public mutator in F1–F2 (`TicTacToe.put`) and 2 in F3 (`TicTacToe.put`, `TicTacToe.move`). These are the intended public entry points of the aggregate root, not leaks — there is no other route to change the board. Neither folder exposes any getter that returns a mutable object (both score 0 for exposed mutable state).

**Domain free of I/O:** In `with-ddd`, the `domain` package is cleanly free of console input and output. The ArchitectureTest verifies this (checked in every phase with 3 architecture assertions passing). `console.ConsoleGame` is the sole I/O adapter. In `without-ddd`, `Main` owns all I/O; there is no enforced architectural boundary.

### Localisation of changes

`with-ddd` has more small classes, so it accumulates more inter-class dependencies by construction. However, when a change arrived (CR-2), `without-ddd` spread the impact across new files and methods within the large `Main`, while `with-ddd` spread it across existing, focused domain classes with no new files. The share of the F3 change that fell on existing classes was 100 % in `with-ddd` vs. 66 % in `without-ddd` (metrics.md F3).

---

## 4. Agent cost and whether the docs drove the work

### Cost per phase and cumulative

| Phase | Folder | Time (s) | Max context (k tokens) | Model calls | Cost (Bobcoins) |
|---|---|---|---|---|---|
| F1 | without-ddd | 197 | 40.0 | 31 | 1.69 |
| F1 | with-ddd | 210 | 48.4 | 36 | 2.52 |
| F2 | without-ddd | 262 | 81.5 | 26 | 2.48 |
| F2 | with-ddd | 476 | 118.9 | 47 | 5.91 |
| F3 | without-ddd | 279 | 114.7 | 35 | 3.97 |
| F3 | with-ddd | 281 | 79.7 | 43 | 4.47 |
| **Total** | **without-ddd** | **738** | — | **92** | **8.14** |
| **Total** | **with-ddd** | **967** | — | **126** | **12.9** |

The context column is the **maximum the window held at the end of the session**, not the total consumed across all calls; Bobcoins reflect actual spend better. `with-ddd` spent 1.58× more Bobcoins overall (12.9 vs. 8.14). The overhead was concentrated in F2 (5.91 vs. 2.48, a 2.4× ratio), where the documentation drove a more thorough structural refactor (3 new classes, 3 modified, docs updated). F3 costs converged sharply (4.47 vs. 3.97, a 1.13× ratio), suggesting that the stable domain vocabulary from F1–F2 reduced exploration in F3.

### Docs read vs. code read before the first edit

| Phase | Folder | Docs before first edit | Code before first edit | First edit in | Docs before code? |
|---|---|---|---|---|---|
| F1 | without-ddd | 2 | 0 | code | No |
| F1 | with-ddd | 8 | 1 | code | No |
| F2 | without-ddd | 1 | 6 | code | No |
| F2 | with-ddd | 8 | 8 | **docs** | **Yes** |
| F3 | without-ddd | 2 | 8 | code | No |
| F3 | with-ddd | 8 | 12 | **docs** | **Yes** |

In F1, both folders started in code, so `with-ddd`'s `AGENTS.md` rule ("docs first") was not followed on the first edit — consistent with the F1 history file confirming no docs were edited (Bob updated `decisions.md` but not before starting code). From F2 onward, `with-ddd` genuinely edited docs before code (first edit recorded as docs in F2 and F3). In `without-ddd` the docs read were 2 (REQUIREMENTS.md + AGENTS.md) in every phase, serving as a quick reference rather than a planning artefact.

**Did docs replace or add to code reading?** In `with-ddd`, the pre-edit code reads grew alongside the docs reads (F2: 8 docs + 8 code; F3: 8 docs + 12 code). The documentation did not replace code reading; it added to it. Total code reads in the whole session: `with-ddd` 0/7/10 vs. `without-ddd` 0/5/6 across F1/F2/F3. The docs gave Bob a roadmap but did not eliminate the need to read existing implementations.

### Design decisions in history

`with-ddd` recorded 12–19 domain decisions in `docs/domain/decisions.md` (D1–D11 pre-seeded in F1; D12–D15 added in F1; D16–D19 in F2; D20–D25 in F3). The F2 history explicitly cites modules.md ("no class asks which kind of Player it has") as the reason for the `Player.chooseCoordinate` polymorphic extension. The F3 history cites six domain documents as relied on. `without-ddd` did not maintain any living decision log outside `history/session-n.md`.

`modules.md` was updated in F2 and F3 (both explicitly listed in the docs/domain updated row of metrics.md). The file map was kept current, fulfilling the `AGENTS.md` requirement.

---

## 5. Decisions Bob made alone

### without-ddd — decisions by phase

| Phase | ID | Type | Question Bob would have asked |
|---|---|---|---|
| F1 | A1 | design | Should the Board be mutable or immutable? |
| F1 | A2 | design | Where should win/draw detection live? |
| F1 | A3 | gap | What should the program print for prompts? |
| F1 | A4 | design | How to handle the case when stdin is exhausted before the game ends? |
| F1 | A5 | design | Should the board log enumerate squares in row-major order? |
| F2 | A1 | design | Should the PC strategy be a new class, a method on Board, or inline in Main? |
| F2 | A2 | design | How to check "would placing color X at square S win?" without exposing internals or adding a public API to Board? |
| F2 | A3 | design | Should the board log be written for PC moves the same as for human moves? |
| F2 | A4 | gap | The requirements say "the same input format for the Human Player" with `--vs-pc`; should the human still see a prompt? |
| F3 | A1 | design | Should `ThreePiecesBoard` reuse `Board` or be a separate class? |
| F3 | A2 | design | Should `BoardLog` accept a generic interface or overloads for each board type? |
| F3 | A3 | design | Should the three-pieces game loop be in `Main` or a separate class? |
| F3 | A4 | gap | What happens when `null` comes from `scanner.nextLine()` in the move phase (EOF mid-game)? |

### with-ddd — decisions by phase

| Phase | ID | Type | Question Bob would have asked |
|---|---|---|---|
| F1 | A1 | design | `Board`, `Square`, `Round` — package-private class or package-private constructor? |
| F1 | A2 | gap | ArchitectureTest `domain_does_not_print` rule: test classes in `tictactoe.domain` excluded by `DoNotIncludeTests`? |
| F1 | A3 | design | Board uses `HashMap<Coordinate, Square>`. Should it be an array instead? |
| F1 | A4 | gap | Board log: occupied squares listed "in any order" — what order to use? |
| F1 | A5 | gap | What should the "Invalid" message say exactly? REQUIREMENTS.md only says it starts with `Invalid`. |
| F2 | A1 | design | How should `Player` be extended without `instanceof`? |
| F2 | A2 | design | How to break the circular dependency between `HumanPlayer` and `ConsoleGame`? |
| F2 | A3 | design | Where should the "occupied square retry" loop live after refactoring? |
| F2 | A4 | design | How to test `PcPlayer`'s rule 5 (Any) in isolation? |
| F2 | A5 | naming | What to name the domain interface for supplying Coordinates? |
| F3 | A1 | design | Should `TicTacToe` be subclassed or modified to support the three-pieces variant? |
| F3 | A2 | design | Should `MoveSupplier` be a domain interface (like `CoordinateSupplier`)? |
| F3 | A3 | design | Where should the transition from put phase to move phase be triggered? |
| F3 | A4 | gap | What error message should appear for an invalid move? |
| F3 | A5 | design | Should `piecesLeft(Color)` return `Integer.MAX_VALUE` or `-1` for the standard variant? |

### Cross-mapping: without-ddd questions vs. with-ddd documents

| without-ddd question | Answered by a with-ddd document? | Document and section |
|---|---|---|
| Should the Board be mutable or immutable? | Partially — `model.puml` marks `Board.put` as `~` (package-private mutator), implying mutability via the aggregate root | `docs/domain/model.puml` (class signatures) |
| Where should win/draw detection live? | Yes — invariants I6 and I7 assign this to `TicTacToe` and `Board` | `docs/domain/invariants.md` (I6, I7) |
| What should the program print for prompts? | No — REQUIREMENTS.md only says any output before the final board is allowed | None |
| How to handle EOF / stdin exhaustion? | No | None |
| Should the board log enumerate squares in row-major order? | Yes — glossary defines "Reading order" and `Coordinate.all()` | `docs/domain/glossary.md` (Reading order) |
| Should the PC strategy be a new class, method on Board, or inline in Main? | Yes — modules.md rule: "A new kind of Player: add a class at the Player variation point" | `docs/domain/modules.md` (Where changes go) |
| How to check "would X win at S?" without exposing Board internals? | Yes — D8 says the console reads via `colorAt`; domain encapsulation rule forces a domain method | `docs/domain/decisions.md` (D8), `invariants.md` (I10) |
| Should the board log be written for PC moves the same as for human moves? | No — REQUIREMENTS.md covers this, but no domain doc does | None |
| Should the human still see a prompt with `--vs-pc`? | No | None |
| Should `ThreePiecesBoard` reuse `Board` or be a separate class? | Yes — D4 (rule change starts in docs), and modules.md extension points guide toward extending `TicTacToe` | `docs/domain/decisions.md` (D4), `modules.md` |
| Should `BoardLog` accept a generic interface or overloads per board type? | Indirectly — the domain is sealed behind `TicTacToe`, so no BoardLog overload is needed | `docs/domain/decisions.md` (D8) |
| Should the three-pieces game loop be in `Main` or a separate class? | Yes — modules.md says "A new way to start the game: change Main only" | `docs/domain/modules.md` (Where changes go) |
| What happens with `null` from `scanner.nextLine()`? | No | None |

### Questions that arose in with-ddd because of the documentation itself

| with-ddd question | Caused by which document |
|---|---|
| `Board`, `Square`, `Round` — package-private class or package-private constructor? | `model.puml` uses `~` for mutators but leaves class visibility ambiguous |
| ArchitectureTest: test classes in `tictactoe.domain` excluded by `DoNotIncludeTests`? | `ArchitectureTest` enforced by the module rules in `modules.md` |
| Board uses `HashMap<Coordinate, Square>` — should it be an array instead? | `glossary.md` defines `Coordinate` as a value object with `equals`/`hashCode`, suggesting map keying |
| How should `Player` be extended without `instanceof`? | `modules.md` explicitly prohibits `instanceof`; forced Bob to choose a polymorphic design |
| How to break the circular dependency between `HumanPlayer` and `ConsoleGame`? | The domain-interface pattern in `modules.md` (domain declares, console implements) created the wiring challenge |
| How to name the domain interface for supplying Coordinates? | `modules.md`'s naming convention ("use glossary terms") required a new glossary entry |
| Should `MoveSupplier` be a domain interface (like `CoordinateSupplier`)? | `modules.md`'s extension-point pattern raised the question; Bob decided against it |

---

## 6. Accuracy

Both folders passed **100 % of all hidden games** in all three phases — correct according to the JSONL log (move by move), with the exact console output, with the correct logic even when format fails, and with the console showing the same board as the log. No game was ever missing a log, and there were no regressions: F1 scenarios pass in F2 and F3 for both folders.

**Mutation score** is the clearest separator: `with-ddd` scored 96 %/94 %/93 % (without console) vs. `without-ddd`'s 78 %/80 %/79 %. With 74 mutants killed out of 74 generated in F1 and 90/96 in F2, `with-ddd`'s tests are nearly exhaustive for the non-console code. `without-ddd`'s 22 % of surviving mutants in F1 (57/73 killed) correspond to unchecked boundary cases in `Board` and `BoardLog`.

**Decisions that could have produced wrong behavior while scenarios pass:**

- `without-ddd` F1, A4 (design): EOF handling in `Main` re-loops and prints "Invalid input" — correct per requirements; but a scenario that exhausts stdin mid-game would loop indefinitely, which no hidden scenario tests.
- `without-ddd` F1, A5 (design): board log in row-major order — REQUIREMENTS.md allows "any order", so this is safe.
- `without-ddd` F2, A2 (design): `PcPlayer.wouldWin` duplicates `Board.hasWon` logic into a local simulation. A bug in the copy (e.g., missing the inverse diagonal) would produce wrong PC play; the mutation score of 80 % means some mutants in this area could survive.
- `with-ddd` F1, A5 (gap): `with-ddd` chose two distinct "Invalid" messages (one for bad format, one for occupied square). REQUIREMENTS.md requires only that the line start with "Invalid". This decision is safe.
- `with-ddd` F3, A1 (design): `TicTacToe.put` with `piecesLeft <= 0` and `Integer.MAX_VALUE` for unlimited. If a caller ever checked `piecesLeft(Color.X) == Integer.MAX_VALUE` to distinguish modes, that would be a leaky abstraction — but no test or console code does so; the check inside `put` is `<= 0`, which `Integer.MAX_VALUE` never satisfies. Safe.

**Pending items:** Neither folder had any pending items in any phase. Zero escapes (hidden games that fail with Bob's suite green) were recorded in all six measurements.

---

## 7. Efficiency and scalability

### Correct games vs. cost and time

| Phase | Folder | Bobcoins per correct game (log) | Seconds per correct game | Bobcoins per 100 lines touched |
|---|---|---|---|---|
| F1 | without-ddd | 0.188 | 21.9 | — |
| F1 | with-ddd | 0.280 | 23.3 | — |
| F2 | without-ddd | 0.207 | 21.8 | 1.84 |
| F2 | with-ddd | 0.492 | 39.7 | 3.54 |
| F3 | without-ddd | 0.248 | 17.4 | 1.37 |
| F3 | with-ddd | 0.279 | 17.6 | 1.93 |
| Total | without-ddd | 0.220 | 19.9 | — |
| Total | with-ddd | 0.349 | 26.1 | — |

`without-ddd` is cheaper per correct game in every phase: 0.188 vs. 0.280 (F1), 0.207 vs. 0.492 (F2), 0.248 vs. 0.279 (F3). The gap narrows sharply from F2 to F3 (from 2.4× to 1.13×). Time per game also converges in F3 (17.4 vs. 17.6 seconds).

### What happens from F1 to F3

| Folder | Phase | Lines | Propagation cost (%) | Y&C index | TCC median | Bobcoins per 100 lines touched |
|---|---|---|---|---|---|---|
| without-ddd | F1 | 182 | 56.0 | 1.12 | 0.75 | — |
| without-ddd | F2 | 249 | 52.8 | 1.18 | 0.75 | 1.84 |
| without-ddd | F3 | 474 | 49.0 | 1.14 | 0.60 | 1.37 |
| with-ddd | F1 | 355 | 38.2 | 1.50 | 0.90 | — |
| with-ddd | F2 | 439 | 48.0 | 1.56 | 0.95 | 3.54 |
| with-ddd | F3 | 549 | 48.0 | 1.56 | 0.95 | 1.93 |

`without-ddd` grew +160 % in lines (182 → 474) but improved its propagation cost (56.0 % → 49.0 %) and kept its Y&C index nearly flat (1.12 → 1.14). TCC fell from 0.75 to 0.60 in F3, reflecting `ThreePiecesBoard`'s moderate cohesion (TCC = 0.6). Bobcoins per 100 lines touched fell from 1.84 (F2) to 1.37 (F3) — a decreasing marginal cost.

`with-ddd` grew only +55 % in lines (355 → 549) but its propagation cost rose from 38.2 % to 48.0 % between F1 and F2 (adding 3 new classes with more inter-class links) and then plateaued at 48.0 % in F3. Y&C index was flat at 1.56 across F2–F3. TCC held at 0.95 in both F2 and F3. Bobcoins per 100 lines touched fell sharply from 3.54 (F2) to 1.93 (F3).

**Which folder grows without making each change more expensive?** `without-ddd` has a lower absolute Bobcoins-per-100-lines in both F2 and F3 (1.84/1.37 vs. 3.54/1.93). Both folders show a decreasing trend from F2 to F3. `with-ddd` starts higher (documentation reading is extra overhead) but converges. If the trend continued, the DDD overhead would become proportionally smaller with each change request.

`runs/stats.md` contains only one run, so medians equal the single observation; no inter-run repetition can be confirmed.

---

## 8. Maintainability under change (F2 and F3)

### F2 — CR-1: add a PC player

| | without-ddd | with-ddd |
|---|---|---|
| New classes | 1 (PcPlayer) | 3 (CoordinateSupplier, HumanPlayer, PcPlayer) |
| Modified classes | 1 (Main) | 3 (Main, ConsoleGame, Player) |
| Open/Closed ratio | 0.5 | 0.5 |
| Lines changed in existing classes | 74 | 105 |
| Existing tests touched | 0 | 2 |
| docs/domain updated | — | decisions.md, glossary.md, model.puml, modules.md |

Both reached Open/Closed = 0.5. `without-ddd` changed only `Main` (added a `PcPlayer` call and a `--vs-pc` branch — 74 lines changed), leaving `Board`, `BoardLog`, `Color`, and `State` untouched. `with-ddd` refactored `Player` into an abstract class with `chooseCoordinate`, split the existing `Player` into `HumanPlayer` and `PcPlayer`, and touched `RoundTest` and `TicTacToeTest`. The broader refactor was driven by the `modules.md` rule prohibiting `instanceof`, which meant an abstract-method extension rather than a simple flag. This is a structurally superior approach for future new player types, but at the cost of 105 lines changed (vs. 74) and two test files touched.

**Where the PC ended up:** In `without-ddd`, the PC is a standalone utility class `PcPlayer` with a static `play(Board)` method called directly from `Main`. In `with-ddd`, the PC is `domain.PcPlayer`, a subclass of the abstract `domain.Player`, which computes its `Coordinate` through `chooseCoordinate(TicTacToe)`. The `with-ddd` placement enforces the domain boundary (the PC never does I/O), while `without-ddd`'s `PcPlayer` duplicates Board win-detection logic in a private simulation — a small but measurable design leak.

### F3 — CR-2: three-pieces variant

| | without-ddd | with-ddd |
|---|---|---|
| New classes | 1 (ThreePiecesBoard) | 0 |
| Modified classes | 2 (BoardLog, Main) | 6 (Main, ConsoleGame, Board, Square, State, TicTacToe) |
| Open/Closed ratio | 0.33 | 0.0 |
| Lines changed in existing classes | 191 | 232 |
| Existing tests touched | 0 | 0 |
| docs/domain updated | — | decisions.md, game-states.md, glossary.md, invariants.md, model.puml, modules.md |

`without-ddd` chose a separate `ThreePiecesBoard` class (Open/Closed = 0.33, one new + two modified). `with-ddd` added a second `TicTacToe` constructor and a `move()` method, extending the existing aggregate root (Open/Closed = 0.0 — no new classes, six modified). Despite the lower Open/Closed ratio, the `with-ddd` change was more localised semantically: `TicTacToe` is the only aggregate root, and CR-2 adds a new operation (`move`) and a new state variant (`X_TO_MOVE`, `O_TO_MOVE`), both of which the existing architecture was designed to absorb.

In `with-ddd`, the docs were updated before the code in both F2 and F3: the history files confirm `First edit in: docs` for both. In `without-ddd`, the first edit was always code.

---

## 9. Vocabulary

### Discarded terms in the code

| Phase | without-ddd discarded terms (class name) | with-ddd discarded terms |
|---|---|---|
| F1 | `place` in `Board.place` | none |
| F2 | `Grid` (grid local var in PcPlayer.wouldWin), `move` (PcPlayer.chooseMove, PcPlayer.findWinningMove, PcPlayer.move alias), `place` (Board.place), `play` (PcPlayer.play) | none |
| F3 | `Grid` (grid local var), `Phase` (Board.isPutPhase, Main.movePhase local), `place` (Board.place), `play` (PcPlayer.play) | none |

`without-ddd` accumulated discarded terms over each phase: 1 in F1, 4 in F2, 4 in F3. The same terms (`place`, `play`) persisted because earlier code was not updated when the glossary would have prohibited them, and no glossary existed to check against. `move` was introduced for the PC's internal method name (`chooseMove`, `findWinningMove`) — but the glossary (had it existed) would have reserved `move` for the physical piece-move operation. In F3 this clash became real when `ThreePiecesBoard.move` is the piece-move operation and `PcPlayer.chooseMove` is a coordinate-selection method; both use `move`, with different meanings.

`with-ddd` had zero discarded terms in all three phases. In F3, the only missing glossary term was `MoveSupplier` (22/23 glossary terms present) — a term that was added to the glossary but the corresponding domain interface was deliberately not implemented (decision A2 in F3 history).

### Missing glossary terms in without-ddd

F1 missing: Column, Coordinate, Diagonal, InverseDiagonal, Piece, Put, Round, Square, TicTacToe, Win (10 of 17).
F2 missing: Column, Coordinate, CoordinateSupplier, Diagonal, HumanPlayer, InverseDiagonal, Piece, Put, Round, Square, TicTacToe (11 of 20).
F3 missing: Column, Coordinate, CoordinateSupplier, Diagonal, HumanPlayer, InverseDiagonal, MoveSupplier, Piece, PiecesLeft, Round, Square, TicTacToe (12 of 23).

The core domain terms `Coordinate`, `Square`, `Piece`, `Round`, and `TicTacToe` were never used in `without-ddd`. Instead it used `Board` (which covers multiple concepts), `int row`/`int col` for coordinates, `Color[][]` for squares, and `currentPlayer: Color` for turns.

### CR-2 name clash with Move

In `without-ddd`, `move` was already used for PC player methods (`chooseMove`, `findWinningMove`) when CR-2 introduced the physical piece-move operation `ThreePiecesBoard.move`. The same word now means two different things. In `with-ddd`, `move` was reserved in the F1 glossary ("Reserved: moving a Piece that is already on the Board … never use it for a Put") and activated in F3 for `TicTacToe.move(Coordinate from, Coordinate to)`. No clash occurred.

---

## 10. Verdict

`without-ddd` is better for **building fast**: it spent 1.58× fewer Bobcoins over three phases (8.14 vs. 12.9), always started in code, passed all scenarios without any extra workflow overhead, and kept simpler class counts (5–7 vs. 12–15). `with-ddd` is better for **changing safely and maintaining vocabulary**: it achieved 96 % / 93 % mutation scores (vs. 78 % / 79 %), zero discarded terms (vs. up to 4), lower propagation cost from F1 onward (38.2 % vs. 56.0 % in F1), and the F3 change required no new classes and touched focused, invariant-documented code. The DDD documentation contributed fewer vocabulary defects, stronger test coverage of domain rules, a stable glossary that prevented the `move` name clash, and architectural rules that guided CR-1 and CR-2 toward the extension points. It cost approximately 58 % more Bobcoins total, with the overhead highest in F2 (2.4×) and nearly eliminated by F3 (1.13×); it also doubled the class count (which inflated Y&C index and LCOM\*) and added 0.33 more context per session.

---

## 11. Limits

- **Single execution per folder.** All differences are observations of one run. Three to five runs per folder would be needed to compute medians and distinguish signal from noise. The `runs/stats.md` file has statistics for only one prior run (series 2, F1 only), so cross-run stability cannot be confirmed for F2 or F3 in either folder.
- **Tic-Tac-Toe is a well-known problem.** Both agents likely benefited from having seen Tic-Tac-Toe in their training data. Vocabulary alignment and correct game logic may be easier to achieve here than for a domain-specific problem where no pre-trained knowledge exists. The advantage of DDD documentation may be larger in genuinely novel domains.
- **Bob writes its own history.** The decisions, reasoning, and document citations in `history/session-n.md` are self-reported. Bob could record decisions after the fact or describe a rationale that was not actually used. The cross-checks in section 2 and 3 (reading the code to verify claimed decisions) confirm the two decisions verified per folder per phase, but do not rule out unverified discrepancies.
- **Two effects cannot be separated.** The `with-ddd` folder receives both domain documentation (glossary, model, invariants, states, modules) and a workflow prescription (`AGENTS.md`: "docs first", update decisions, update file map). The better mutation score, the zero discarded terms, and the docs-before-code behavior in F2 and F3 could be due to the documentation, the workflow, or both. A third condition — workflow without documentation, or documentation without the docs-first rule — would be needed to separate them.
- **Model non-determinism.** Different runs of the same session may produce different code, decisions, and costs even with the same prompt. The 1.58× cost ratio, the mutation score gap, and the vocabulary gap are consistent with the hypothesis that documentation helps, but each could partly reflect random variation in the model's output.
- **Class-count inflation and metric artifacts.** `with-ddd`'s higher Y&C index (1.50–1.56 vs. 1.12–1.14) and higher LCOM\* median (0.65–0.67 vs. 0.34–0.56) are partly a mechanical consequence of having more small, single-purpose classes: more class pairs are counted, and each small class interacts with fewer fields. These metrics do not by themselves indicate worse design in a folder with a richer type model.
