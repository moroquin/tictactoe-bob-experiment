# Tic-Tac-Toe with IBM Bob: with DDD vs. without DDD

Does domain documentation (DDD) make an AI coding assistant build and, above all, **change** a program more accurately and maintainably, even when every session starts without memory?

To answer it, IBM Bob builds the same Tic-Tac-Toe twice, with the same requirements and the same prompts:

- **without-ddd**: `REQUIREMENTS.md` only.
- **with-ddd**: `REQUIREMENTS.md` plus the DDD documentation in `docs/domain/` (glossary, model, invariants, states, modules with a file map, and a decision log) and an `AGENTS.md` that says this documentation drives the development.

Each phase is a new Bob session: F1 builds the game, F2 adds an opponent controlled by the program (CR-1) and F3 changes a core rule: three pieces that move (CR-2). After each phase, `measure.py` measures both folders; at the end Bob compares the results.

## What is in this repository

| File | What it is for |
|---|---|
| `src/without-ddd/`, `src/with-ddd/` | The starting point of each folder: what Bob finds before F1 |
| `prompts/F1-build.md`, `F2-cr1-human-vs-pc.md`, `F3-cr2-three-pieces.md` | The prompts of each phase, the same for both folders |
| `scenarios.tsv` | The hidden acceptance games of each phase (Bob never sees them) |
| `measure.py` | Resets the folders, takes a snapshot of each phase, audits the isolation, measures everything and archives each run in the history |
| `design_metrics.py` | The design calculations in a single module: coupling (Ce, Ca, propagation cost, Yourdon & Constantine types), cohesion (LCOM*, LCOM4, TCC, LCC), encapsulation and file and method size |
| `stats.py` | Statistics of every run: medians, paired differences, sign test and correlations (`python3 measure.py stats`) |
| `COMPARE.md` | The prompt Bob uses to compare both folders and write `CONCLUSIONS.md` |
| `presentation.py` | Generates `results/presentation.pptx` with the rules, the metrics, the model and the results of each phase |
| `scripts/init.sh`, `scripts/new_run.sh` | Setup checks and the first reset; archive the current run and start a new one |
| `config/bob-settings.json` | Recommended permissions for Bob during the experiment |
| `.bob/rules/` | Bob's rules in this repository, for the comparing session |
| `reference/model-original.png` | The team's original domain diagram, which the model comes from |
| `results/` | The current run: snapshots of each phase, `summary.md`, `metrics.md`, `metrics.json` and `presentation.pptx` |
| `runs/run-N/` | The history: earlier runs archived, with their `description.txt` and, if Bob compared them, their `CONCLUSIONS.md` |
| `runs/stats.md`, `stats.json`, `history.csv` | The statistics of every run and one row per run, phase and folder for a spreadsheet |
| `FAILURES.md`, `failures.json` | The observed failures, their cause and their evidence; `failures.json` is the summary the presentation uses |
| `FINDINGS.md` | What Tic-Tac-Toe can and cannot tell about DDD, and the lessons for a harder problem |
| `conclusion.txt` | The verdict in one sentence, shown on the conclusion slide |

## A run, step by step

```bash
scripts/init.sh                      # first time only: checks everything and creates the folders
# F1, F2 and F3, one after the other:
#   bob in ../without-ddd and in ../with-ddd at the same time, a new session each, the phase prompt as is
python3 measure.py snapshot F<n>     # when both sessions have finished
python3 measure.py audit             # every session must be OK
# after F3:
python3 measure.py                   # measure the three phases
#   optional: bob in this repository, "run COMPARE.md"; python3 presentation.py
scripts/new_run.sh "what with-ddd received in this run"   # archive in runs/run-N and reset for the next run
```

The sections below explain each step.

## Requirements

- Java 21, Maven 3.9 or later and Python 3.9 or later. `measure.py` also uses `jdeps` and `javap`, which come with the JDK.
- [Bob Shell](https://bob.ibm.com/docs/shell/getting-started/install-and-setup) with a session started.
- For the presentation: `pip install python-pptx`.

## Setup (once)

This repository is the **control plane**: it holds the starting points, the prompts, the hidden games and the measurements, and it creates the experiment folders next to it. Those folders have no git: they are outside any repository and have no `.git` of their own, so git fails there even if Bob tries it. `measure.py reset` refuses to create them inside a git repository, `snapshot` warns if Bob created a `.git` during a phase, and `audit` checks both.

`scripts/init.sh` checks everything below (tools, Java 21, Bob's permissions, no global context, no git around the folders) and then runs `measure.py reset`.

1. **Bob's permissions.** Merge `config/bob-settings.json` into `~/.bob/settings/settings.json`, keeping your other keys and a backup: it denies `git`, `curl`, `wget`, `env` and `printenv`, approves only `mvn`, `java` and the `date` command the session history needs, and does not allow reading outside the working folder. It also denies the shell commands that read or list files (`cat`, `ls`, `grep`, `head`, `find`…): they take any path, so they could read the hidden games; Bob reads with its own file tools, which stay inside the folder. No `git …` or reading command may stay approved.
2. **No global context.** Leave `~/.bob/rules/` empty and do not create `~/.bob/AGENTS.md` or an `AGENTS.md` in the folder that holds this repository: Bob would load them in both folders.
3. **Create the experiment folders** next to this repository:
   ```bash
   python3 measure.py reset
   ```
   It is the same as copying `src/without-ddd` and `src/with-ddd` to `../without-ddd` and `../with-ddd`, and it also records the start time and the series, to measure Bob's sessions later. The result:
   ```
   workspace/
   ├── tictactoe-bob-experiment/   ← this repository
   ├── without-ddd/
   └── with-ddd/
   ```

## Each phase (F1, F2 and F3)

1. **One terminal per folder.** Open a terminal in `../without-ddd` and another in `../with-ddd`, run `bob` in each one and send the content of `prompts/F<n>-….md` unchanged. Each phase is a new session: do not continue the previous one and never use `bob --resume` (Bob Shell prints that command at the end of each session; it is only a way to continue it).
   Run both folders **at the same time**, side by side, in every run: both sessions then share the same conditions (model load, network, machine). Running them one after the other changes how long each one takes, so do not mix both ways inside a series; if you want to try it, note it in the run description.
2. **Wait for both to finish.** Bob works without asking, leaves the code building and writes its history in `history/`. If a session stops waiting for you to approve a command, do not approve it: the permissions in `config/bob-settings.json` are missing something. Note the command, fix the configuration and repeat the run.
3. **Take the snapshot** from this repository:
   ```bash
   python3 measure.py snapshot F<n>
   ```
   It copies each folder to `results/<folder>/F<n>/` and **moves the history out of the folders**, so that the next session does not read it as documentation.
4. **Audit** that no session left its folder or used forbidden commands:
   ```bash
   python3 measure.py audit
   ```
   It only shows the sessions since the last `reset` (`ALL=1` shows every session). If a session comes out `INVALID`, the run is not valid: discard it without archiving it and start again from F1 in both folders:
   ```bash
   rm -rf results/without-ddd results/with-ddd results/metrics.* results/summary.md results/presentation.pptx
   python3 measure.py reset
   ```
5. **Measure** (it takes one or two minutes per snapshot, mostly because of the mutation testing):
   ```bash
   python3 measure.py
   ```

There is no need to copy tokens by hand: `measure.py` reads the cost, the context, the duration and every file Bob read or edited from the Bob Shell database (`~/.bob/db/bob.db`), without modifying it.

## Compare and present

- **Bob's analysis:** open a terminal in this repository, run `bob` and type `run COMPARE.md`. Bob runs `measure.py`, reads the snapshots and writes `CONCLUSIONS.md`. Every number must appear in `results/metrics.md`.
- **Executive summary:** `results/summary.md`, with the scoreboard per phase (the best value in bold), the cumulative cost, efficiency, scalability and the findings.
- **Presentation:** `python3 presentation.py` generates `results/presentation.pptx` with every run: those in `runs/` and the current one. For each phase it shows each run and whether the result repeats, and it ends with the failures and a conclusion computed from what repeats across runs. To view it in the macOS preview, which does not show decks with notes: `NO_NOTES=1 OUTPUT=/tmp/preview.pptx python3 presentation.py`.

## Board log and series

Since **series 2**, `REQUIREMENTS.md` asks for a log: when the variable `TICTACTOE_BOARD_LOG` exists, the program writes to that file one JSON line with the empty board at the start and another one after every valid put or move:

```json
{"board":[{"row":1,"column":1,"color":"X"}],"state":"IN_PROGRESS"}
```

This way accuracy does not depend on the console format: `measure.py` plays each hidden game with the log on and counts it as correct if it starts empty, if each line is a valid put or move by the player whose turn it is, and if the final board and state are the expected ones. The console is still measured separately, because it is what the user sees. The F3 prompt reminds that moves are logged too.

The **series** (the `SERIES` constant in `measure.py`) goes up every time anything in `src/` (requirements, docs, rules), the prompts or the scenarios change. Change them only between runs, never in the middle of one, and if the series already has archived runs, raise `SERIES` before the next `reset`. `reset` records it and the stats only combine runs of the same series. Series 1 is the runs without a log.

## Several runs

One run alone is an anecdote; what repeats across runs is a signal.

`scripts/new_run.sh ["description"]` does steps 1 and 2 in one go: it asks for confirmation first, measures the run if needed, archives it and resets the folders. It never touches `src/`.

Compare it with Bob (`run COMPARE.md`) before archiving if you want its `CONCLUSIONS.md`: `archive` moves `results/` as it is.

1. **Measure and archive the current run:**
   ```bash
   python3 measure.py
   python3 measure.py archive "what with-ddd received in this run"
   ```
   `archive` moves `results/` to `runs/run-N/` (the next free number), saves the description, leaves `results/` empty and recomputes the stats. `reset` refuses to start while `results/` holds snapshots that are not archived, so that two runs never mix.
2. **Start another one:** `python3 measure.py reset` and the phases.
3. **Statistics:** `python3 measure.py stats` writes `runs/stats.md` with, per phase and folder, the median, range, mean and deviation of each metric; the paired difference per run (with DDD minus without DDD); in how many runs each folder wins, with the p of the sign test; efficiency and scalability per phase; and the Spearman correlations between design, accuracy and cost, overall and inside each folder. `runs/history.csv` has one row per run, phase and folder.
4. **Measure everything again** when the metrics change: `python3 measure.py all` measures every archived run and the current one again, and recomputes the stats. For a single one: `RESULTS=runs/run-N python3 measure.py`.

With fewer than 6 runs, the sign test cannot give p < 0.05 even if one folder always wins: with 3 to 5 runs, what counts is whether the difference repeats.

If you forget to take a snapshot between phases, the code of that phase is overwritten. It can be rebuilt by replaying the edits Bob saved in `bob.db` and validating them against what the next phase read; that is how F1 of run 2 was rebuilt (see the note in `metrics.md`). The history of the previous phase also stays in the folder: check with `bob.db` that the next session did not read it.

## Rules for valid data

- **Same starting point.** Both folders share `REQUIREMENTS.md`, `pom.xml` (with-ddd adds ArchUnit) and the rules in `.bob/rules/`: work without asking, do not leave the folder (temporary files go in `target/`), read files only with Bob's file tools, do not use git or the network, keep no memory for later sessions, and write the history.
- **Same prompts, no help.** Send each prompt as is. Do not answer Bob's questions or correct its work during a phase.
- **New sessions, without memory.** One new session per phase and per folder, never `bob --resume`. The history leaves the folder with `snapshot`, so the next session only sees the code and, in with-ddd, the documentation.
- **Isolation.** Each folder works alone; `audit` checks it session by session.
- **External measurement.** The hidden games, the mutants, coupling, cohesion and cost are measured by `measure.py`, not by Bob. The decisions and pending items are Bob's own account: they are used to see which questions the documentation left open.

## What is measured

| Group | Metrics |
|---|---|
| Accuracy | Hidden games from `scenarios.tsv` with the regressions of earlier phases, checked in two ways: the board's **JSONL log** (the state of the game, move by move) and the **console** (what the user sees: the exact output, the logic even if the format fails, and whether it shows the same board as the log). Unit tests and mutation score (total and without the console). **Bob's tests against the hidden games**: share of tests that pass, whether the suite's verdict agrees with the hidden games, escapes (hidden games that fail with the suite green) and false alarms (failing tests with every hidden game correct) |
| Agent cost | Cost, max context, duration, model calls and builds, from `bob.db` |
| How Bob worked | Docs and code files read before the first edit and in the whole session; whether it edited the docs before the code |
| Size | Classes, packages and lines; lines per file (median, max, files with more than 200); lines per method (methods with more than 30) and methods with more than 3 parameters |
| Coupling | Efferent (Ce) and afferent (Ca); propagation cost (what part of the system a change reaches); Yourdon & Constantine connection type between classes: data, stamp, control, external, common or content, with an index from 1 to 6 |
| Cohesion | LCOM* and LCOM4; TCC and LCC (pairs of methods that share fields, directly or indirectly) |
| Design | Encapsulation; classes with console input and output |
| Efficiency and scalability | Bobcoins and seconds per correct hidden game (also with the correct logic); Bobcoins per 100 lines touched in F2 and F3; how propagation cost, coupling and cohesion change from F1 to F3 |
| Change (F2 and F3) | New, modified and deleted classes; Open/Closed ratio; lines changed in existing classes; existing tests touched; docs updated |
| Autonomy | Measured in `bob.db`: human messages after the prompt, questions to the user, approvals rejected, denied commands it tried and sessions that ended in an error. Declared in the history: decisions Bob made alone, by type, with the question it would have asked; pending items; the result (done, partially, blocked) |
| Language | Discarded glossary terms that appear in the code and glossary terms present |

## Hypotheses

- **H1, build (F1):** small differences. Bob already knows Tic-Tac-Toe.
- **H2, change (F2 and F3):** with-ddd reads less code before editing, changes fewer existing classes, does not break earlier games and keeps the vocabulary.
- **H3, design:** with-ddd ends up better encapsulated, with a domain free of console I/O and the glossary's vocabulary.

## Limits

- **One execution per folder** is a demonstration, not proof. Repeat the experiment 3 to 5 times (`reset` and the three phases) to talk about medians.
- **Tic-Tac-Toe is very well known**, which reduces the advantage of the documentation in F1. The change phases (an opponent with fixed rules, pieces that move) are not the textbook game.
- **with-ddd receives documentation and a workflow** (docs first and as the source of truth). The experiment measures the whole package, not each part separately.
- **Bob writes the history**: its decisions are its own account, not an audit.
