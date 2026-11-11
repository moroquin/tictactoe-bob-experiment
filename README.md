# Course enrollment with IBM Bob: with DDD vs. without DDD

Does domain documentation (DDD) make an AI coding assistant build and, above all, **change** a program more accurately and maintainably, even when every session starts without memory?

To answer it, IBM Bob builds the same course enrollment system twice, with the same requirements, the same architecture and the same prompts:

- **without-ddd**: `REQUIREMENTS.md` and `ARCHITECTURE.md`.
- **with-ddd**: the same, plus the DDD documentation in `docs/domain/` (glossary, model, invariants, policies, modules with a file map, and a decision log) and an `AGENTS.md` that says this documentation drives the development.

The only difference between the folders is the DDD documentation. Anything about architecture (packages, the console as an adapter, the `ArchitectureTest` that enforces it) is in `ARCHITECTURE.md`, identical in both.

Each phase is a new Bob session:

| Phase | Prompt | What it asks |
|---|---|---|
| F1 | `prompts/F1-build.md` | Version 1: users, session, permissions, buildings, classrooms, courses, students, professors, sections, enrollments and queries |
| F2 | `prompts/F2-cr1-modify-delete-sections.md` | CR-1: modify and delete sections |
| F3 | `prompts/F3-cr2-load-and-reports.md` | CR-2: load instructions from a file, and four reports |
| F4 | `prompts/F4-cr3-balanced-enrollment.md` | CR-3: the section with the fewest enrollments (**changes** R29) |
| F5 | `prompts/F5-cr4-new-grade-scale.md` | CR-4: a new grade scale (**changes** R28) |
| F6 | `prompts/F6-cr5-prerequisites-and-credits.md` | CR-5: prerequisites and a credit limit; the course query changes format |
| F7 | `prompts/F7-cr6-time-conflicts.md` | CR-6: time conflicts for classrooms, professors and students |

The domain is invented, so the model cannot replace the documentation with what it already knows; the rules cross each other (references, capacity, permissions), and F4 and F5 contradict earlier rules, which is where outdated knowledge and undocumented decisions cause regressions.

## What is in this repository

| File | What it is for |
|---|---|
| `src/without-ddd/`, `src/with-ddd/` | The starting point of each folder: what Bob finds before F1 |
| `prompts/` | The prompts of each phase, the same for both folders |
| `scenarios/` | **Not published** (see [Hidden scenarios and the reference model](#hidden-scenarios-and-the-reference-model)). The hidden scenarios (Bob never sees them): one file per scenario, a shared `base.txt` and the files `Load` reads |
| `oracle.py` | **Not published.** The reference model of the requirements and the change requests: it computes what each scenario must output in each phase |
| `scenarios/EXPECTED.md` | **Not published.** The expected output of every scenario and the phases where it holds (`python3 oracle.py scenarios`) |
| `CONCLUSIONS.md` | Bob's analysis of the run (Agent Mode), reviewed by hand; see its revision note |
| `measure.py` | Resets the folders, takes a snapshot of each phase, audits the isolation, measures everything and archives each run in the history |
| `design_metrics.py` | The design calculations: coupling (Ce, Ca, propagation cost, Yourdon & Constantine types), cohesion (LCOM*, LCOM4, TCC, LCC), encapsulation and file and method size |
| `stats.py` | Statistics of every run: medians, paired differences, sign test and correlations (`python3 measure.py stats`) |
| `COMPARE.md` | The prompt Bob uses to compare both folders and write `CONCLUSIONS.md` |
| `presentation.py` | Generates `results/presentation.pptx` with the rules, the metrics, the model and the results of each phase |
| `scripts/init.sh`, `scripts/new_run.sh` | Setup checks and the first reset; archive the current run and start a new one |
| `Execute.md` | One run, command by command |
| `config/bob-settings.json` | Recommended permissions for Bob during the experiment |
| `.bob/rules/` | Bob's rules in this repository, for the comparing session |
| `results/` | The current run: snapshots of each phase, `summary.md`, `metrics.md`, `metrics.json` and `presentation.pptx` |
| `runs/run-N/` | The history: earlier runs archived, with their `description.txt` and, if Bob compared them, their `CONCLUSIONS.md` |
| `runs/stats.md`, `stats.json`, `history.csv` | The statistics of every run and one row per run, phase and folder for a spreadsheet |

The Tic-Tac-Toe version of this experiment, with its runs and findings, is in the `tictactoe` branch.

## A run, step by step

```bash
scripts/init.sh                      # first time only: checks everything and creates the folders
# F1 to F7, one after the other:
#   bob in ../without-ddd and in ../with-ddd at the same time, a new session each, the phase prompt as is
python3 measure.py snapshot F<n>     # when both sessions have finished
python3 measure.py audit             # every session must be OK
# after F7:
python3 measure.py                   # measure the seven phases
#   optional: bob in this repository, "run COMPARE.md"; python3 presentation.py
scripts/new_run.sh "what with-ddd received in this run"   # archive in runs/run-N and reset for the next run
```

The sections below explain each step.

## Requirements

- Java 21, Maven 3.9 or later and Python 3.9 or later. `measure.py` also uses `jdeps` and `javap`, which come with the JDK.
- [Bob Shell](https://bob.ibm.com/docs/shell/getting-started/install-and-setup) with a session started.
- For the presentation: `pip install python-pptx`.

## Setup (once)

This repository is the **control plane**: it holds the starting points, the prompts, the hidden scenarios, the reference model and the measurements, and it creates the experiment folders next to it. Those folders have no git: they are outside any repository and have no `.git` of their own, so git fails there even if Bob tries it. `measure.py reset` refuses to create them inside a git repository, `snapshot` warns if Bob created a `.git` during a phase, and `audit` checks both.

`scripts/init.sh` checks everything below (tools, Java 21, Bob's permissions, no global context, no git around the folders) and then runs `measure.py reset`.

1. **Bob's permissions.** Merge `config/bob-settings.json` into `~/.bob/settings/settings.json`, keeping your other keys and a backup: it denies `git`, `curl`, `wget`, `env` and `printenv`, approves only `mvn`, `java` and the `date` command the session history needs, and does not allow reading outside the working folder. It also denies the shell commands that read or list files (`cat`, `ls`, `grep`, `head`, `find`…): they take any path, so they could read the hidden scenarios; Bob reads with its own file tools, which stay inside the folder. No `git …` or reading command may stay approved.
2. **No global context.** Leave `~/.bob/rules/` empty and do not create `~/.bob/AGENTS.md` or an `AGENTS.md` in the folder that holds this repository: Bob would load them in both folders.
3. **Create the experiment folders** next to this repository:
   ```bash
   python3 measure.py reset
   ```
   It is the same as copying `src/without-ddd` and `src/with-ddd` to `../without-ddd` and `../with-ddd`, and it also records the start time and the series, to measure Bob's sessions later. The result:
   ```
   workspace/
   ├── <this repository>/
   ├── without-ddd/
   └── with-ddd/
   ```

## Each phase (F1 to F7)

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
   If a session is cut from outside (for example, the Bob plan runs out of Bobcoins: `BudgetExceededError`) after it changed files, run that phase again in that folder only. Do it before taking the snapshot of the next phase:
   ```bash
   python3 measure.py redo F<n> with-ddd          # restores ../with-ddd to its F<n-1> snapshot; the discarded snapshot goes to results/discarded/
   # a new bob session in ../with-ddd with the same prompt
   python3 measure.py snapshot F<n> with-ddd
   ```
   `measure.py` then counts only the sessions after the redo. Note it in the run description: that folder ran the phase alone, not side by side. Before each phase, check that the plan has Bobcoins left for both folders (a long phase can take 5 to 10 Bobcoins per folder).
5. **Measure** when you want to see results (after F7, or after any phase for partial results). It takes one or two minutes per snapshot, mostly because of the mutation testing:
   ```bash
   python3 measure.py
   ```

There is no need to copy tokens by hand: `measure.py` reads the cost, the context, the duration and every file Bob read or edited from the Bob Shell database (`~/.bob/db/bob.db`), without modifying it.

## Compare and present

- **Bob's analysis:** open a terminal in this repository, run `bob` and type `run COMPARE.md`. Bob runs `measure.py`, reads the snapshots and writes `CONCLUSIONS.md`. Every number must appear in `results/metrics.md`.
- **Executive summary:** `results/summary.md`, with the scoreboard per phase (the best value in bold), the cumulative cost, efficiency, scalability and the findings.
- **Presentation:** `python3 presentation.py` generates `results/presentation.pptx` with every run: those in `runs/` and the current one. For each phase it shows each run and whether the result repeats, and it ends with a conclusion computed from what repeats across runs. To view it in the macOS preview, which does not show decks with notes: `NO_NOTES=1 OUTPUT=/tmp/preview.pptx python3 presentation.py`.

## Hidden scenarios and the reference model

> **Why `scenarios/` and `oracle.py` are not in this repository.** The experiment depends on a problem the model has never seen. If the hidden scenarios and the reference model were public, future models could be trained on them, and later runs (the 3 to 5 repetitions planned, or runs with new Bob versions) would no longer measure what they are meant to measure. They are kept private so the experiment can be repeated. They are available on request for review or replication. `measure.py` imports `oracle.py`, so it does not run without them; the published results in `results/` are the output of a full run. The results in `results/` include some expected lines in the failure details.

A scenario is a sequence of instructions, run through standard input; its standard output is compared line by line with what `oracle.py` expects. The text after `OK:` and `Error:` is free (R41), so only the prefix is compared there; the texts the requirements fix (`log in first`, `permission denied`, `invalid instruction`, the enrollment response, the load summary) and the query and report lines are compared exactly. A scenario is correct when every line matches and the program exits with code 0; **expected lines matched** also counts the lines that appear in order, so a nearly correct output still scores.

Each scenario runs in the phase it appears in (`# since:`) and in **every later phase, with the rules of that phase**. The reference model computes the expected output of each phase, so a change request that changes a rule also changes what earlier scenarios expect: the scenario stops being a plain regression check and starts checking the new rule. `scenarios/EXPECTED.md` shows, for every scenario, until which phase its first output holds and in which phases it changes (for example, `F1-04-enrollment` changes in F4, the balanced sections, and in F5, the new grade scale).

To add or change a scenario, edit `scenarios/`, then run `python3 oracle.py scenarios`: it checks the model against every example in the requirements and the change requests, and writes `scenarios/EXPECTED.md` again.

## Series

The **series** (the `SERIES` constant in `measure.py`) goes up every time anything in `src/` (requirements, architecture, docs, rules), the prompts, the scenarios or the reference model change. Change them only between runs, never in the middle of one, and if the series already has archived runs, raise `SERIES` before the next `reset`. `reset` records it and the stats only combine runs of the same series.

## Several runs

One run alone is an anecdote; what repeats across runs is a signal.

`scripts/new_run.sh ["description"]` does steps 1 and 2 in one go: it asks for confirmation first, measures the run if needed, archives it and resets the folders. It never touches `src/`.

Compare it with Bob (`run COMPARE.md`) before archiving if you want its `CONCLUSIONS.md`: `archive` moves it into the run.

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

If you forget to take a snapshot between phases, the code of that phase is overwritten, and the history of the previous phase stays in the folder: check with `bob.db` that the next session did not read it, or repeat the run.

## Rules for valid data

- **Same starting point.** Both folders share `REQUIREMENTS.md`, `ARCHITECTURE.md`, `ArchitectureTest.java`, `pom.xml` and the rules in `.bob/rules/`: work without asking, do not leave the folder (temporary files go in `target/`), read files only with Bob's file tools, do not use git or the network, keep no memory for later sessions, and write the history. The only difference is `docs/domain/` and the `AGENTS.md` that points to it.
- **Same prompts, no help.** Send each prompt as is. Do not answer Bob's questions or correct its work during a phase.
- **New sessions, without memory.** One new session per phase and per folder, never `bob --resume`. The history leaves the folder with `snapshot`, so the next session only sees the code and, in with-ddd, the documentation.
- **Isolation.** Each folder works alone; `audit` checks it session by session.
- **External measurement.** The hidden scenarios, the mutants, coupling, cohesion and cost are measured by `measure.py`, not by Bob. The decisions and pending items are Bob's own account: they are used to see which questions the documentation left open.

## What is measured

| Group | Metrics |
|---|---|
| Accuracy | Hidden scenarios correct and expected lines matched, per phase; the scenarios of earlier phases that are still correct (regressions and changed rules). Unit tests and mutation score (total and without the console). **Bob's tests against the hidden scenarios**: share of tests that pass, whether the suite's verdict agrees with the scenarios, escapes (scenarios that fail with the suite green) and false alarms (failing tests with every scenario correct) |
| Agent cost | Cost, max context, duration, model calls and builds, from `bob.db` |
| How Bob worked | Docs and code files read before the first edit and in the whole session; whether it edited the docs before the code |
| Size | Classes, packages and lines; lines per file (median, max, files with more than 200); lines per method (methods with more than 30) and methods with more than 3 parameters |
| Coupling | Efferent (Ce) and afferent (Ca); propagation cost (what part of the system a change reaches); Yourdon & Constantine connection type between classes: data, stamp, control, external, common or content, with an index from 1 to 6 |
| Cohesion | LCOM* and LCOM4; TCC and LCC (pairs of methods that share fields, directly or indirectly) |
| Design | Encapsulation; classes with console input and output |
| Efficiency and scalability | Bobcoins and seconds per correct scenario; Bobcoins per 100 lines touched from F2; how propagation cost, coupling, cohesion and code read before editing change from F1 to F7 |
| Change (F2 to F7) | New, modified and deleted classes; Open/Closed ratio; lines changed in existing classes; existing tests touched; docs updated. F4 and F5 only change a rule: they show whether the rule lives in one place |
| Autonomy | Measured in `bob.db`: human messages after the prompt, questions to the user, approvals rejected, denied commands it tried and sessions that ended in an error. Declared in the history: decisions Bob made alone, by type, with the question it would have asked; pending items; the result (done, partially, blocked) |
| Language | Discarded glossary terms that appear in the code and glossary terms present |

## Hypotheses

- **H1, build (F1):** with-ddd costs more (it reads and follows the docs) and makes fewer decisions on its own; accuracy is similar.
- **H2, change (F2 to F7):** as the code grows, with-ddd reads less code before editing, and its cost per change grows less.
- **H3, changed rules (F4, F5):** with-ddd changes the rule in one place (its policy) and touches fewer classes; without-ddd misses more places where the old rule lived, which shows up as failing scenarios of earlier phases.
- **H4, crossing rules (F6, F7):** with-ddd breaks fewer earlier scenarios when a new rule crosses the existing ones.

## Limits

- **One execution per folder** is a demonstration, not proof. Repeat the experiment 3 to 5 times (`reset` and the seven phases) to talk about medians.
- **with-ddd receives documentation and a workflow** (docs first and as the source of truth). The experiment measures the whole package, not each part separately.
- **The DDD docs prescribe a design** (aggregates, policies, layers inside the free part of `ARCHITECTURE.md`). The design metrics measure that design as well as Bob's work.
- **The reference model is code too.** It is checked against every example in the requirements and the change requests (`python3 oracle.py check`), but a scenario that fails in both folders in the same way deserves a look at the model before blaming Bob.
- **Bob writes the history**: its decisions are its own account, not an audit.
