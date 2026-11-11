# Findings: what Tic-Tac-Toe can and cannot tell about DDD

Written on 2026-11-10, after the first complete and valid run of series 2 (F1, F2 and F3, both folders run side by side, every session `OK` in the audit). The numbers come from `results/metrics.md` of that run, archived as the first run of series 2. Bob's own analysis of the same run is in its `CONCLUSIONS.md`.

> One run per folder: these are observations, not proof.

## In one sentence

Tic-Tac-Toe is a game the model already knows and a codebase small enough to read whole, so with and without DDD both get every hidden game right: the experiment cannot show whether domain documentation makes Bob more accurate; what it does show is that the documentation costs more, produces more and better tests, and keeps the vocabulary consistent.

## Why accuracy does not separate the folders

- **Ceiling effect.** Both folders pass 100 % of the hidden games in every phase (9/9, 12/12, 16/16), according to the JSONL log and to the console, with no regressions of F1 and no escapes (hidden games failing with Bob's suite green). A metric both folders max out cannot show an advantage.
- **The model knows the game.** The rules of Tic-Tac-Toe are in its training data, so its prior knowledge replaces the documentation. The change requests (a PC opponent, three pieces that move) only add rules; none contradicts an earlier one.
- **The code is too small for the docs to save anything.** The hypothesis is that a session without memory understands the system from the docs instead of reading all the code. Here the final code is 474 lines (without-ddd) and 549 lines (with-ddd), and Bob reads almost all of it anyway: with-ddd read 12 code files before its first edit in F3, out of 15 classes. The documentation was read in addition to the code, not instead of it.

## What the run does show

| | without-ddd | with-ddd |
|---|---|---|
| Hidden games correct, F1 · F2 · F3 | 9/9 · 12/12 · 16/16 | 9/9 · 12/12 · 16/16 |
| Mutation score without the console, F1 · F2 · F3 | 78 · 80 · 79 % | **96 · 94 · 93 %** |
| Unit tests, F1 · F2 · F3 | 25 · 36 · 57 | 57 · 66 · 84 |
| Cumulative cost | **8.14 Bobcoins** | 12.9 Bobcoins (1.58×) |
| Cumulative time | **738 s** | 967 s (1.31×) |
| Code files read before the first edit, F1 · F2 · F3 | **0 · 6 · 8** | 1 · 8 · 12 |
| Discarded glossary terms in the code, F3 | 4 | **0** |
| TCC median, F3 (higher = more cohesive) | 0.6 | **0.95** |
| Human interventions and questions | 0 | 0 |

1. **Documentation costs more and does not pay it back in a small system.** with-ddd spent 1.58× the Bobcoins, most of it in F2 (5.91 against 2.48). It reads the docs and, in F2 and F3, updates them before the code, but it does not read less code.
2. **Better tests: the clearest advantage.** with-ddd's tests kill 93–96 % of the mutants against 78–80 %. The likely cause is `invariants.md`: it gives Bob a list of rules to test. It is an effect of the documentation, although a prompt that says "test every rule" might achieve the same.
3. **Consistent vocabulary.** with-ddd uses no discarded term in any phase. Part of this metric is circular (it is measured against the glossary only with-ddd received); the real finding is the F3 name clash in without-ddd, where `move` means both choosing the PC's square and moving a piece. In with-ddd the glossary reserved `move` from F1, and no clash happened.
4. **More cohesive, more classes.** with-ddd ends with 15 classes and a TCC median of 0.95; without-ddd with 7 classes and 0.6. with-ddd keeps console input and output out of the domain, enforced by an architecture test. More small classes also raise its Yourdon & Constantine index and LCOM*, partly as a counting artifact.
5. **Part of the design was given.** The with-ddd docs prescribe a design (an aggregate root, polymorphic players, no `instanceof`), not only domain knowledge. The design differences measure that prescription as much as Bob's work.
6. **Full autonomy in both.** No session asked a question, needed a human or ended in an error.

## Lessons for the next problem

To answer whether DDD helps an agent, the problem needs what Tic-Tac-Toe lacks:

1. **A domain the model does not know**, with invented rules, so the documentation is the only source of truth.
2. **Rules that interact and have exceptions**, and hidden scenarios that test those interactions, so an ambiguity the docs resolve becomes a wrong result without them.
3. **Change requests that contradict earlier rules**, not only add new ones: that is where outdated knowledge and unrecorded decisions cause regressions.
4. **More phases** (6 to 8), so the cost per change has time to grow or to stay flat.
5. **Enough code that reading all of it is expensive** (a few thousand lines at the end), so documentation can show up as less code read and a lower cost in the late phases.

The experiment setup can stay: the control plane, the audit, the JSONL log as oracle, the hidden scenarios and the metrics carry over to a new problem as a new series.
