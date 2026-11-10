# Observed failures and their cause

Each failure was checked against the code of the snapshots, Bob's histories and the Bob Shell database (`~/.bob/db/bob.db`). There are three executions with the same material for `without-ddd` and different documentation for `with-ddd`:

| Execution | What with-ddd received | Where the data is |
|---|---|---|
| Round 0 (pilot) | Original DDD documentation: model without responsibilities | Not in the repository |
| Run 1 | Improved domain: responsibilities, `Line`, `State`, invariant I10 | `runs/run-1/` |
| Run 2 | The same, plus "the docs drive the development": workflow in `AGENTS.md`, file map and decisions in the log | `results/` |

`without-ddd` received exactly the same in all three, so its differences are Bob's own variation.

## 1. The PC blocks the wrong square (F2-02): missing domain context

**What happens.** In game F2-02 (X on (1,1), (3,3) and (3,1)), X threatens two squares: (2,1) and (3,2). CR-1 asks that, if the block rule finds more than one square, the PC chooses "the first one in reading order": (2,1). The `without-ddd` PC blocks (3,2), and the game ends differently.

**Cause.** `without-ddd` walks the 8 lines in order (rows, columns, diagonals) and returns the first threat it finds. It applies reading order to the lines and not to the squares. Row 3 comes before column 1, so (3,2) wins.

| | without-ddd | with-ddd |
|---|---|---|
| Round 0 | Fails. Bob wrote the doubt down as a gap: *"CR-1 says 'reading order' … but does not define whether reading order applies per-line or per-square"* (decision A3) | Correct |
| Run 1 | Correct: it walks the squares row by row | Correct: it walks `Coordinate.all()` |
| Run 2 | Fails. Bob decided it alone: *"Lines are enumerated in a fixed order … the first matching line's first empty square wins. This naturally produces reading-order output"* (decision A4, type `design`) | Correct: it walks `Line.all()`, but breaks ties with the `readingOrderIndex` of the `Coordinate` |

**Why the domain avoids it.** The CR-1 prompt is the same for both folders. The difference is that the `with-ddd` glossary defines *Reading order* as an order of **Coordinates**: "Row 1 to 3 and, within a row, column 1 to 3. The order of `Coordinate.all()`". With that term, "the first one in reading order" can only refer to a square. Without it, Bob had to interpret it, and in two of three executions it got it wrong. It is the question Bob would have asked in round 0, answered by one line of the glossary.

**Lesson.** An ambiguous term in a requirement turns into a decision by the agent. If the glossary fixes the meaning, the decision disappears.

## 2. Board glued to the prompt: variation of the agent, not of the domain

**What happens.** In run 2, `without-ddd` fails every hidden game of F1 and F2 (0/9 and 0/12), although its logic is correct (9/9 and 11/12).

**Cause.** It prints the prompt with `print` instead of `println` (`Player X, enter your move: `). When the input comes through a pipe, as in the automated tests, the first row of the board is glued to the last prompt: `Player X, enter your move: XXX`. `REQUIREMENTS.md` requires the board in exactly three lines.

**Why it is not attributed to the domain.** The console contract is in `REQUIREMENTS.md`, which is the same in both folders, and the DDD documentation does not say how to print prompts. It happened in one of three executions of `without-ddd` and in none of `with-ddd`, which is not enough to attribute it.

**Lesson.** Bob wrote its own tests, ran `mvn -q verify` and declared that the jar met the contract. The agent's tests did not detect it; the hidden games did. That is why accuracy is measured outside the agent. Since series 2, the JSONL board log separates the game from its presentation, so a failure like this one shows up as a console failure with the game still correct.

## 3. with-ddd modified REQUIREMENTS.md: a side effect of its own documentation

**What happens.** In F2, `with-ddd` edited `REQUIREMENTS.md` to include the mode against the PC, in round 0 and in run 2. `REQUIREMENTS.md` is the client's contract and had to stay the same in both folders.

**Cause.** Run 2 says it in its decision A6 (type `scope`): *"REQUIREMENTS.md listed 'Playing against the PC' as out of scope; it is now in scope. Keeping it stale would violate rule 10 (docs and code in agreement)."* Rule 10 of `AGENTS.md` asks that docs and code agree, but it does not say which documents belong to the team and which to the client.

**Lesson.** A documentation rule that is too general also drives the agent, in this case towards a change that was not its to make. The fix for the next execution is to add to `AGENTS.md`: "`REQUIREMENTS.md` is the client's contract: never edit it; rule changes go to `docs/domain/`."
