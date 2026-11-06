# DDD vs. no DDD with IBM Bob — a Tic-Tac-Toe experiment

Companion experiment for the talk *How DDD Gives AI Assistants the Context They're Missing* (JCON @ IBM TechXchange 2026).

IBM Bob builds the same Tic-Tac-Toe twice, from the same requirements, in two isolated branches. `without-ddd` only has the requirements. `with-ddd` also carries the DDD artifacts: the ubiquitous language, the domain model, the invariants, the game states and the module boundaries. Bob then receives the same change requests in fresh sessions, and the harness measures what each branch costs and what it produces.

> **This file and the `harness/` folder live only on `main`.** Never copy them into `without-ddd` or `with-ddd`: they describe the change requests and the other branch, and Bob would read them.

**Contents:** [1. The question](#1-the-question) · [2. Design](#2-design) · [3. Repository layout](#3-repository-layout) · [4. Setup](#4-setup-once) · [5. Pilot](#5-pilot-before-the-real-runs) · [6. Running the experiment](#6-running-the-experiment) · [7. Metrics](#7-metrics) · [8. Reading and presenting the results](#8-reading-and-presenting-the-results) · [9. Threats to validity](#9-threats-to-validity) · [10. Appendix: every file](#10-appendix-every-file) · [References](#references)

## 1. The question

Do DDD artifacts make an AI coding assistant cheaper and safer, not only when it builds a program but above all when it has to change it in a new session, without memory?

- **H1, build (F1).** Small differences. Tic-Tac-Toe is in every training set, so Bob can build it without help.
- **H2, change (F2 and F3).** In a fresh session, `with-ddd` needs fewer tokens and fewer reads before its first edit, changes fewer existing classes (Open/Closed), keeps the old acceptance tests green and keeps the vocabulary of the domain.
- **H3, design.** The `with-ddd` code has lower coupling (CBO, fan-out, package dependencies), better cohesion (LCOM*, TCC) and a domain without console input or output.

What is compared is *requirements alone* against *the same requirements plus DDD artifacts*. Both branches get exactly the same rules of the game, console contract, build and prompts.

## 2. Design

### What each branch contains

| | `without-ddd` | `with-ddd` |
|---|---|---|
| `REQUIREMENTS.md`: rules, console contract, build, out of scope | ✓ | ✓ same file |
| `.bob/rules/00-workspace.md`: isolation rules | ✓ | ✓ same file |
| `pom.xml`: Java 21, JUnit 5, JaCoCo, PIT | ✓ | ✓ plus ArchUnit |
| `AGENTS.md` | Build commands only | Index plus golden rules: docs first, glossary names, invariants in test names, Open/Closed |
| `docs/domain/`: glossary, model, invariants, game states, modules, decision log | — | ✓ |
| `ArchitectureTest`: the domain never depends on the console and never does I/O | — | ✓ |

The model in `docs/domain/model.puml` is the team's diagram (`harness/reference/model-original.png`) transcribed to PlantUML, so Bob reads it as text. Version 1 leaves out Human and AI, because version 1 only has people; the full diagram is the target after CR-1 (`harness/reference/model-target.puml`).

### Phases

Each phase is a **new Bob session** with no memory of the previous one. The prompt is identical for both branches, and it is revealed only when its phase starts.

| Phase | Prompt | What it tests | Acceptance scenarios |
|---|---|---|---|
| F1 | Build version 1: two people at the same computer | Building from the same requirements | F1-01 … F1-09 |
| F2 | CR-1, Human vs. AI | Extension at a variation point (Player → Human / AI) | F1 + F2-01 … F2-03 |
| F3 (optional) | CR-2, three Pieces each | Changing a core rule (Put → Move) | F1 + F2 + F3-01 … F3-04 |

The AI Player of CR-1 is a deterministic algorithm: win, block, center, corner, first empty Square. It calls no language model, so the game needs no token, and the acceptance tests are reproducible.

### Isolation: physical first, rules second

In `bob run` every tool is pre-approved, and neither `.bobignore` nor the workspace setting restricts shell commands. So isolation does not rely on instructions alone:

1. **Nothing to see.** Each run is a fresh clone of one branch, renamed to `main`, with one commit, no tags and no remote, in a folder with a neutral name such as `ttt-3fa29c01`. Bob cannot tell which condition it is in. The two branches are orphans and share no history.
2. **Bob configuration**, identical for both branches: `git`, `curl`, `wget`, `env` and `printenv` denied; reading outside the workspace disabled; MCP servers and subagents disabled; no global rules and no `AGENTS.md` in any parent folder.
3. **Audit.** Every run is logged as stream-json, and `metrics.py` checks every tool call. A `git` command, a path outside the clone, or the Bob API key in the log marks the run as invalid, and the run is left out of the medians.

## 3. Repository layout

```
main
├── EXPERIMENT.md                    this file
└── harness/
    ├── branches/                    source of the two experiment branches
    │   ├── common/                  REQUIREMENTS.md, .bob/rules/00-workspace.md, .gitignore
    │   ├── without-ddd/             AGENTS.md, pom.xml
    │   └── with-ddd/                AGENTS.md, pom.xml, docs/domain/*, src/test/java/tictactoe/ArchitectureTest.java
    ├── prompts/                     F1-build.md, F2-cr1-human-vs-ai.md, F3-cr2-three-pieces.md
    ├── acceptance/scenarios.tsv     16 black-box scenarios with their expected results
    ├── bob/settings-approval.json   snippet for ~/.bob/settings/settings.json
    ├── reference/                   model-original.png (the team's diagram), model-target.puml
    ├── scripts/                     config.sh, setup-branches.sh, new-run.sh, run-phase.sh, metrics.py
    └── results/                     one folder per run, plus summary.csv (generated)
without-ddd                          orphan branch, created by setup-branches.sh
with-ddd                             orphan branch, created by setup-branches.sh
```

`metrics.py` only uses the Python standard library.

## 4. Setup (once)

**Prerequisites:** macOS or Linux (on Windows, WSL), git, Java 21, Maven 3.9 or later, Python 3.9 or later, and Bob Shell (`bob`). Optional: Docker for SonarQube, and CK for class-level metrics.

1. **Create the repository.** Create an empty repository (public if you want to share the results) and put this file at the root of `main`. The appendix holds every file of the harness; this command writes them all next to it:
   ```bash
   python3 - <<'PY'
   import pathlib, re
   doc = pathlib.Path("EXPERIMENT.md").read_text(encoding="utf-8")
   for path, body in re.findall(r"\*\*`([^`]+)`\*\*[^\n]*\n\n````[a-z]*\n(.*?)\n````\n", doc, re.S):
       target = pathlib.Path(path)
       target.parent.mkdir(parents=True, exist_ok=True)
       target.write_text(body + "\n", encoding="utf-8")
       print("wrote", path)
   PY
   chmod +x harness/scripts/*.sh harness/scripts/metrics.py
   ```
   Save the team's diagram as `harness/reference/model-original.png`, commit, and push.

2. **Create the experiment branches.**
   ```bash
   bash harness/scripts/setup-branches.sh
   git push -u origin without-ddd with-ddd
   ```

3. **Check that both starting points build.** There is no code yet, so this only validates the `pom.xml` files and downloads the dependencies once:
   ```bash
   for b in without-ddd with-ddd; do
     git worktree add "/tmp/ttt-check-$b" "$b"
     (cd "/tmp/ttt-check-$b" && mvn -q verify) && echo "$b OK"
     git worktree remove --force "/tmp/ttt-check-$b"
   done
   ```

4. **Configure Bob.** Merge `harness/bob/settings-approval.json` into `~/.bob/settings/settings.json`, keeping your other keys. Leave `~/.bob/rules/` empty and do not create `~/.bob/AGENTS.md`: global rules would reach both branches. `new-run.sh` checks both.

5. **Keep the API key out of the repository and off the screen.**
   ```bash
   read -rs BOB_API_KEY && export BOB_API_KEY   # type it; nothing is echoed
   ```
   Never commit it, never `echo` it, and do not show `env` during a demo. `run-phase.sh` refuses to start without it, and the audit flags any log in which the key appears. If your key is a general API key, also run `export BOB_EXTRA_FLAGS="--team-id <your-team-id>"`.

6. **Point the harness at your repository and choose where the runs live.** The runs folder must be outside this repository, with no `AGENTS.md` in any parent folder:
   ```bash
   export REPO_URL=git@github.com:<you>/<repo>.git
   export RUNS_DIR=$HOME/ttt-bob-runs
   ```

7. **Optional tools.**
   - CK, for class-level cohesion and coupling:
     ```bash
     git clone https://github.com/mauricioaniche/ck && (cd ck && mvn -q -DskipTests package)
     export CK_JAR=$(ls "$PWD"/ck/target/ck-*-jar-with-dependencies.jar)
     ```
   - SonarQube, for complexity, duplication, code smells and technical debt:
     ```bash
     docker run -d --name sonarqube -p 9000:9000 sonarqube:community
     # create a token in the UI at http://localhost:9000, then:
     export SONAR_HOST_URL=http://localhost:9000 SONAR_TOKEN=<token>
     ```

## 5. Pilot (before the real runs)

The pilot answers three questions before you spend Bobcoins on the real runs. Use run number 0, and delete the pilot afterwards.

```bash
bash harness/scripts/new-run.sh with-ddd 0
RUN_DIR=$(sed -n 's/^run_dir=//p' harness/results/with-ddd-r0/run.txt)
```

1. **Does Bob load the workspace rules?**
   ```bash
   (cd "$RUN_DIR" && bob run --workspace "$RUN_DIR" --trust --format json \
     "List the rules and the AGENTS.md instructions you were given for this workspace. Do not change any file.") \
     | python3 -c 'import json, sys; print(json.load(sys.stdin)["last_message"])'
   ```
   The answer must mention the workspace rules (no git, stay inside the folder) and, in `with-ddd`, the golden rules of `AGENTS.md`. If it does not, fix that before going on: the whole experiment depends on it.

2. **Is the isolation enforced?** Ask for something forbidden, then run the audit:
   ```bash
   (cd "$RUN_DIR" && bob run --workspace "$RUN_DIR" --trust --format stream-json \
     "Run git log, then list the parent folder.") > /tmp/canary.ndjson
   python3 harness/scripts/metrics.py audit --log /tmp/canary.ndjson --run-dir "$RUN_DIR"
   ```
   Bob should refuse, or be blocked. If a command went through anyway, the audit reports it, and you know that the audit, not the deny list, is what protects the real runs.

3. **What does a phase cost?** Run F1 and F2 once per branch:
   ```bash
   bash harness/scripts/new-run.sh without-ddd 0
   for p in 1 2; do
     bash harness/scripts/run-phase.sh without-ddd-r0 $p
     bash harness/scripts/run-phase.sh with-ddd-r0 $p
   done
   ```
   Look at `bob_session_costs` and `bob_total_tokens` in `harness/results/*-r0/F*.json`. Then choose the number of runs per branch (n) and set the same `MAX_COST` and `MAX_TURNS` for both branches. Total cost ≈ 2 branches × phases × n × cost per phase.

Delete the pilot:
```bash
for f in harness/results/*-r0/run.txt; do rm -rf "$(sed -n 's/^run_dir=//p' "$f")"; done
rm -rf harness/results/*-r0
```

## 6. Running the experiment

```bash
export MAX_COST=<from the pilot> MAX_TURNS=<from the pilot>
N=3   # runs per branch; 5 if the budget allows

for n in $(seq 1 $N); do
  bash harness/scripts/new-run.sh without-ddd $n
  bash harness/scripts/new-run.sh with-ddd $n
done

for n in $(seq 1 $N); do
  for phase in 1 2 3; do            # use "1 2" if you skip CR-2
    bash harness/scripts/run-phase.sh without-ddd-r$n $phase &
    bash harness/scripts/run-phase.sh with-ddd-r$n $phase &
    wait
  done
done

python3 harness/scripts/metrics.py collect --results harness/results
```

`run-phase.sh` does this, in order:

1. Runs `bob run` with the phase prompt in the run folder, with `--mode agent --format stream-json --trust --disable-mcp --disable-subagents` plus your limits, and saves the log in `results/<run>/F<phase>.ndjson`.
2. Commits the result and tags it `p1`, `p2` or `p3`. Version control is done by the harness, never by Bob.
3. Writes `results/<run>/F<phase>.json`: the audit, Bob's stats, the acceptance tests, the Open/Closed diff against the previous tag, and the vocabulary.
4. Writes `results/<run>/F<phase>-design.json`: unit tests, coverage, mutation score, CK, jdeps and SonarQube. `RUN_DESIGN_METRICS=0` skips this step.
5. Removes the build output (`git clean -fdX`), so the next session starts from what a fresh checkout would have, and refreshes `results/summary.csv`.

A phase cannot run twice for the same run; to repeat one, create a new run number.

Notes:
- Running both branches in parallel is fine and makes a good demo, but they share your machine and your rate limits: compare tokens and cost before duration. Run the pilot first, so Maven's local repository is already filled when two builds run at once.
- Run both branches of a round in the same time window, and write down the version of Bob Shell you used: a model update between runs adds noise.
- For a live demo, use two terminal panes with Bob's default output; the measured runs write stream-json to files.

## 7. Metrics

Every key appears in `results/summary.csv`, one row per run and phase. `metrics.py collect` also prints the median per branch for the key metrics, leaving out runs with isolation violations. F1 is a sanity check; the comparison that matters is F2 and F3.

| What | Keys | Better | Source |
|---|---|---|---|
| Tokens and cost | `bob_total_tokens`, `bob_input_tokens`, `bob_output_tokens`, `bob_cache_read_tokens`, `bob_session_costs` | Lower | `stats` of `bob run` |
| Effort | `bob_duration_ms`, `bob_tool_calls`, `tool_uses`, `builds`, `builds_failed` | Lower | Bob log |
| Searching for context | `tool_uses_before_first_edit`, `reads_before_first_edit`, `doc_reads` | Fewer before the first edit | Bob log |
| Docs first | `docs_edited`, `docs_before_code`: did Bob update `docs/domain` before the code? | True in `with-ddd` | Bob log |
| Correctness | `acceptance_passed` of `acceptance_total`; in F2 and F3, `acceptance_F1_passed` shows regressions | All pass | Hidden acceptance tests |
| Open/Closed | `ocp_ratio` = new classes ÷ (new + modified), `main_modified`, `churn_existing_main`, `existing_tests_touched` | Higher ratio, lower churn | `git diff` between tags |
| Separation of concerns | `io_classes`: files that use the System streams, `Scanner` or `Console`; `package_edges`, `package_cycles`, `package_ce_max`, `package_instability` | Fewer I/O files, no cycles | Source scan, `jdeps` |
| Coupling | `ck_cbo_median` and `_max`, `ck_fanout_*`, `ck_rfc_*` | Lower | CK |
| Cohesion | `ck_lcom_star_median` and `_max`; `ck_tcc_median` and `_min` | LCOM* lower, TCC higher | CK |
| Complexity and debt | `ck_wmc_*`, `sonar_complexity`, `sonar_cognitive_complexity`, `sonar_code_smells`, `sonar_sqale_index` (minutes), `sonar_duplicated_lines_density` | Lower | CK, SonarQube |
| Tests | `unit_tests`, `unit_failures`, `line_coverage`, `branch_coverage`, `mutation_score` | Higher mutation score | Surefire, JaCoCo, PIT |
| Vocabulary | `discarded_terms` (Cell, Mark, Turn, Move before CR-2…), `model_terms_present` of `model_terms_total`, `model_terms_missing` | 0 discarded, all present | Source scan against the glossary |
| Isolation | `audit_violations`; any value above 0 invalidates the run | 0 | Bob log |

- The vocabulary metric applies the glossary to both branches. In `without-ddd` it measures how far Bob's default names are from the domain language that the requirements already use.
- In `with-ddd`, `unit_tests` includes the three architecture rules.
- `ocp_ratio` in F1 is always 1, because every class is new. Read it in F2 and F3.

## 8. Reading and presenting the results

- Report the median and the range per branch, with n, and say how many runs the audit removed.
- Lead with F2: a session that has to *change* the code without memory is where the docs should pay off. The single number closest to the thesis is `reads_before_first_edit`: how much Bob had to search before it knew where to make the change.
- Show the vocabulary. If `without-ddd` calls a put `makeMove`, CR-2 brings a real Move and the names collide.
- Publish the repository with both branches, the logs and `summary.csv`. Logs that passed the audit do not contain your key.

## 9. Threats to validity

- **Variance between runs.** `bob run` has no documented flag to pin the model, and runs differ. That is why there are n runs per branch and medians, not a single run.
- **Tic-Tac-Toe is famous.** Bob already knows it, which narrows the gap between the branches in F1. That is deliberate: the change requests (an AI with fixed rules, three Pieces that move) are not the textbook game.
- **Information vs. structure.** Both branches get the same rules. `with-ddd` adds structure (glossary, model, invariants, states, modules) and a workflow (docs first). The experiment measures that package, not each piece separately; a third branch with the same artifacts but without the docs-first rule would separate them.
- **Small n.** With 3 to 5 runs per branch, present the results as indicative, not as proof.
- **The acceptance tests define "correct".** They check the console contract and the rules, not the quality of the code; the design metrics cover the rest.

## 10. Appendix: every file

These are the exact files of the harness. `setup-branches.sh` builds each experiment branch by copying `harness/branches/common/` and then `harness/branches/<branch>/`.

### 10.1 Branch content

#### Common to both branches

**`harness/branches/common/REQUIREMENTS.md`**

````markdown
# Tic-Tac-Toe — requirements (version 1)

A console Tic-Tac-Toe for two people sitting at the same computer.

## Rules of the game

- The board has 3 × 3 squares. A square is identified by its coordinate: a row and a column, each from 1 to 3. Row 1 is the top row and column 1 is the left column.
- There are two players, X and O. Each player puts pieces of their own color.
- X always starts; after that, the players alternate.
- In each round, the current player puts one new piece on an empty square.
- A player wins as soon as three of their pieces are on the same row, the same column, the diagonal (1,1)–(2,2)–(3,3) or the inverse diagonal (1,3)–(2,2)–(3,1). A win is checked before a draw.
- If all 9 squares are occupied and nobody has won, the game is a draw.
- Invalid input is rejected: the board does not change and the same player tries again. Input is invalid when it is not exactly two whole numbers, when a number is outside 1–3, or when the square is already occupied.

## Console contract

Automated acceptance tests drive the game through the console, so follow this contract exactly.

- Start: `java -jar target/tictactoe.jar` (no arguments).
- Input: one line per put, with the row and the column separated by a space, for example `2 3`.
- After invalid input, print one line that starts with `Invalid` and read again for the same player.
- When the game ends, print the final board as three lines, one per row from row 1 to row 3, with one character per square (`X`, `O`, or `.` for an empty square) and no spaces. Then print exactly one result line: `X wins`, `O wins` or `Draw`. The result line is the last line of output, and the program exits with status 0.
- Any other output, such as prompts or the board after every put, is allowed before the final board.

Example of the end of a game:

    XXX
    OO.
    ...
    X wins

## Build

- Java 21 and Maven, with the provided `pom.xml`.
- `mvn -q verify` must pass.
- `mvn -q package` produces `target/tictactoe.jar`. The entry point is the class `tictactoe.Main`.

## Out of scope for version 1

- Playing against the computer.
- Other board sizes or other rule variants.
- Saving games, and graphical or web interfaces.
````

**`harness/branches/common/.bob/rules/00-workspace.md`**

````markdown
# Workspace rules (mandatory)

- Work only inside this workspace folder. Do not read, list or write files outside it.
- Do not run git commands. Version control is handled outside this session.
- Do not use the network beyond what Maven needs to resolve the build: no curl, no wget, no web searches.
- Do not read or print environment variables.
- Do not ask questions. Make reasonable decisions, follow `REQUIREMENTS.md` and `AGENTS.md`, and finish when `mvn -q verify` passes.
````

**`harness/branches/common/.gitignore`**

````text
target/
*.class
.idea/
*.iml
.vscode/
.DS_Store
````

#### Only in `without-ddd`

**`harness/branches/without-ddd/AGENTS.md`**

````markdown
# AGENTS.md — Tic-Tac-Toe

## Map

| File | What it holds |
|---|---|
| `REQUIREMENTS.md` | Rules of the game, console contract, build, out of scope |

## Build and run

- `mvn -q verify` compiles and runs the tests. It must pass before you finish.
- `mvn -q -DskipTests package && java -jar target/tictactoe.jar` plays the game.
````

**`harness/branches/without-ddd/pom.xml`**

````xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>tictactoe</groupId>
  <artifactId>tictactoe</artifactId>
  <version>1.0.0</version>
  <packaging>jar</packaging>

  <properties>
    <maven.compiler.release>21</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.junit</groupId>
        <artifactId>junit-bom</artifactId>
        <version>5.13.4</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <dependencies>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.junit.platform</groupId>
      <artifactId>junit-platform-launcher</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <finalName>tictactoe</finalName>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.13.0</version>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.2.5</version>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-jar-plugin</artifactId>
        <version>3.4.1</version>
        <configuration>
          <archive>
            <manifest>
              <mainClass>tictactoe.Main</mainClass>
            </manifest>
          </archive>
        </configuration>
      </plugin>
      <!-- Quality tooling: coverage (JaCoCo) and mutation testing (PIT). -->
      <plugin>
        <groupId>org.jacoco</groupId>
        <artifactId>jacoco-maven-plugin</artifactId>
        <version>0.8.12</version>
        <executions>
          <execution>
            <goals>
              <goal>prepare-agent</goal>
            </goals>
          </execution>
          <execution>
            <id>report</id>
            <phase>verify</phase>
            <goals>
              <goal>report</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
      <plugin>
        <groupId>org.pitest</groupId>
        <artifactId>pitest-maven</artifactId>
        <version>1.20.0</version>
        <dependencies>
          <dependency>
            <groupId>org.pitest</groupId>
            <artifactId>pitest-junit5-plugin</artifactId>
            <version>1.2.3</version>
          </dependency>
        </dependencies>
        <configuration>
          <targetClasses>
            <param>tictactoe.*</param>
          </targetClasses>
          <targetTests>
            <param>tictactoe.*</param>
          </targetTests>
          <outputFormats>
            <param>XML</param>
            <param>HTML</param>
          </outputFormats>
          <timestampedReports>false</timestampedReports>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
````

#### Only in `with-ddd`

**`harness/branches/with-ddd/AGENTS.md`**

````markdown
# AGENTS.md — Tic-Tac-Toe

Read this first. It says what exists and where; the detail lives in `docs/domain/`.

## Golden rules

1. `docs/domain/` is the source of truth for the domain. If the code and the docs disagree, the docs win and you fix the code in the same task.
2. Name every class, method and variable with the terms in `docs/domain/glossary.md`. A discarded term in the code is a bug.
3. A change to the rules starts in the docs: update the glossary, the model, the invariants, the game states and the decision log first, then the code and the tests, all in the same task.
4. Unit tests cite the invariant they protect in their name, for example `I3_putOnOccupiedSquareIsRejected`.
5. Only `tictactoe.console` reads input or prints output. `tictactoe.domain` never does; `ArchitectureTest` enforces it.
6. Prefer adding a class at a variation point (a new kind of Player, a new Goal) over editing existing domain classes.

## Map

| File | What it holds |
|---|---|
| `REQUIREMENTS.md` | Rules of the game, console contract, build, out of scope |
| `docs/domain/glossary.md` | Ubiquitous language: term, meaning, name in code, discarded terms |
| `docs/domain/model.puml` | Domain model: classes and relationships (PlantUML) |
| `docs/domain/invariants.md` | Numbered rules (I1, I2…) that must always hold |
| `docs/domain/game-states.md` | States of a game and the allowed transitions |
| `docs/domain/modules.md` | Packages, the dependency rule, and where each kind of change goes |
| `docs/domain/decisions.md` | Append-only decision log |
| `src/test/java/tictactoe/ArchitectureTest.java` | Executable architecture rules |

## Build and run

- `mvn -q verify` compiles and runs the unit and architecture tests. It must pass before you finish.
- `mvn -q -DskipTests package && java -jar target/tictactoe.jar` plays the game.
````

**`harness/branches/with-ddd/pom.xml`** — The same as the `without-ddd` pom, plus the ArchUnit test dependency.

````xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>tictactoe</groupId>
  <artifactId>tictactoe</artifactId>
  <version>1.0.0</version>
  <packaging>jar</packaging>

  <properties>
    <maven.compiler.release>21</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.junit</groupId>
        <artifactId>junit-bom</artifactId>
        <version>5.13.4</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <dependencies>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.junit.platform</groupId>
      <artifactId>junit-platform-launcher</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>com.tngtech.archunit</groupId>
      <artifactId>archunit-junit5</artifactId>
      <version>1.4.2</version>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <finalName>tictactoe</finalName>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.13.0</version>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.2.5</version>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-jar-plugin</artifactId>
        <version>3.4.1</version>
        <configuration>
          <archive>
            <manifest>
              <mainClass>tictactoe.Main</mainClass>
            </manifest>
          </archive>
        </configuration>
      </plugin>
      <!-- Quality tooling: coverage (JaCoCo) and mutation testing (PIT). -->
      <plugin>
        <groupId>org.jacoco</groupId>
        <artifactId>jacoco-maven-plugin</artifactId>
        <version>0.8.12</version>
        <executions>
          <execution>
            <goals>
              <goal>prepare-agent</goal>
            </goals>
          </execution>
          <execution>
            <id>report</id>
            <phase>verify</phase>
            <goals>
              <goal>report</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
      <plugin>
        <groupId>org.pitest</groupId>
        <artifactId>pitest-maven</artifactId>
        <version>1.20.0</version>
        <dependencies>
          <dependency>
            <groupId>org.pitest</groupId>
            <artifactId>pitest-junit5-plugin</artifactId>
            <version>1.2.3</version>
          </dependency>
        </dependencies>
        <configuration>
          <targetClasses>
            <param>tictactoe.*</param>
          </targetClasses>
          <targetTests>
            <param>tictactoe.*</param>
          </targetTests>
          <outputFormats>
            <param>XML</param>
            <param>HTML</param>
          </outputFormats>
          <timestampedReports>false</timestampedReports>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
````

**`harness/branches/with-ddd/docs/domain/glossary.md`**

````markdown
# Glossary — the ubiquitous language

Use these names in code, tests and docs. A discarded term in the code is a bug.

| Term | Meaning | Name in code | Discarded |
|---|---|---|---|
| TicTacToe | The game itself: one Board, one Round and two Players. It is the aggregate root: every change to a game goes through it. | `TicTacToe` | Match, Engine |
| Board | The 3 × 3 set of Squares. | `Board` | Grid, Matrix, Field |
| Square | One place on the Board. It holds at most one Piece. | `Square` | Cell, Tile, Box, Spot |
| Coordinate | The row and the column of a Square, each from 1 to 3. Row 1 is the top row; column 1 is the left column. | `Coordinate` | Position, Point, Index |
| Piece | What a Player puts on a Square. It has a Color and a Coordinate. | `Piece` | Mark, Token, Symbol, Stone |
| Color | `X`, `O` or `EMPTY`. EMPTY is a Null Object: a Square without a Piece has Color EMPTY, never `null`. | `Color.X`, `Color.O`, `Color.EMPTY` | Sign, Symbol, `null` for empty |
| Player | One of the two participants. A Player owns a Color. In version 1 both Players are people. | `Player` | User, Participant |
| Round | Decides which Player puts next. X always starts; then the Players alternate. | `Round` | Turn, TurnManager |
| Put | Placing a new Piece of the current Player's Color on an empty target Square. | `put`, `Put` | move, place, mark, makeMove |
| Goal | What a Player tries to reach. In version 1 the only Goal is Win. | `Goal` | Objective |
| Win | Three Pieces of the same Color on a Row, a Column, the Diagonal or the Inverse Diagonal. | `Win` | Victory |
| Row, Column | The three horizontal and the three vertical lines of the Board. | `Row`, `Column` | — |
| Diagonal | The line (1,1)–(2,2)–(3,3). | `Diagonal` | MainDiagonal |
| Inverse Diagonal | The line (1,3)–(2,2)–(3,1). | `InverseDiagonal` | AntiDiagonal, SecondaryDiagonal |
| Draw | All 9 Squares are occupied and there is no Win. | `Draw` | Tie |
| Move | Reserved: moving a Piece that is already on the Board. It is not part of version 1, so never use it for a Put. | — | — |
````

**`harness/branches/with-ddd/docs/domain/model.puml`**

````plantuml
@startuml
' Domain model, version 1. Transcribed from the team's domain diagram.
' Version 1 only has human Players, so Player has no subtypes yet.
' The original diagram draws X, O and Empty as kinds of Color; here Color is an enum with those three values.
title Tic-Tac-Toe — domain model (version 1)
hide empty members

class TicTacToe
class Board
class Round
class Player
class Square
class Coordinate
class Piece
enum Color {
  X
  O
  EMPTY
}
class Goal
class Win
class Put

TicTacToe *-- "1" Board
TicTacToe *-- "1" Round
TicTacToe *-- "2" Player
Round --> "2" Player : alternates
Player --> Board
Player o-- Color
Player o-- Goal
Goal <|-- Win
note bottom of Win : Row, Column,\nDiagonal, Inverse Diagonal
Goal ..> Put
Goal ..> Square
Board *-- "3x3" Square
Square o-- "0..1" Piece
Put ..> Square : target
Put ..> Coordinate
Put ..> Piece
Piece *-- Coordinate
Piece *-- Color
@enduml
````

**`harness/branches/with-ddd/docs/domain/invariants.md`**

````markdown
# Invariants

Numbered rules that must always hold. Cite them in test names (`I3_...`), in commit messages and in prompts.

| # | Invariant |
|---|---|
| I1 | The Board has exactly 9 Squares (3 × 3). The row and the column of a Coordinate go from 1 to 3. |
| I2 | A Square holds at most one Piece. A Square without a Piece has Color EMPTY. |
| I3 | A Put targets an empty Square. A Put on an occupied Square is rejected, and the Board and the Round do not change. |
| I4 | X always starts. The Round alternates X, O, X, O… |
| I5 | A Player only puts Pieces of their own Color. |
| I6 | After every Put, Win is checked first: three Pieces of the same Color on a Row, a Column, the Diagonal or the Inverse Diagonal end the game. |
| I7 | If there is no Win and all 9 Squares are occupied, the game ends in Draw. |
| I8 | Once the game has ended, no Put is accepted. |
| I9 | Input that is not a valid Coordinate (not exactly two whole numbers, or a number outside 1–3) is rejected without changing the Board or the Round. |
````

**`harness/branches/with-ddd/docs/domain/game-states.md`**

````markdown
# Game states

| State | Event | Next state | Invariants |
|---|---|---|---|
| X to put (initial) | Valid Put by X, no Win, Board not full | O to put | I3, I4, I5 |
| X to put | Valid Put by X that completes a line | X wins | I6 |
| X to put | Valid Put by X that fills the Board without a Win | Draw | I7 |
| X to put | Invalid input, or a Put on an occupied Square | X to put | I3, I9 |
| O to put | Valid Put by O, no Win | X to put | I3, I4, I5 |
| O to put | Valid Put by O that completes a line | O wins | I6 |
| O to put | Invalid input, or a Put on an occupied Square | O to put | I3, I9 |
| X wins, O wins, Draw | Any input | No change: the game is over | I8 |

O never fills the Board: with 9 Squares, the ninth Put is always X's.
````

**`harness/branches/with-ddd/docs/domain/modules.md`**

````markdown
# Modules

## Bounded context

Version 1 has a single bounded context, **Game**, which is the core domain. The console is an adapter around it, not part of the model.

## Packages

| Package | Holds | May depend on |
|---|---|---|
| `tictactoe.domain` | TicTacToe, Board, Square, Coordinate, Piece, Color, Player, Round, Goal, Win, Put | Only the JDK. It never reads input or prints output. |
| `tictactoe.console` | Reading Coordinates and printing the Board and the result, following the console contract in `REQUIREMENTS.md` | `tictactoe.domain` |
| `tictactoe` | `Main`: wiring only | Both |

`src/test/java/tictactoe/ArchitectureTest.java` enforces that the domain does not depend on the console and does not use `System.out`, `System.err`, `System.in`, `Scanner` or `Console`.

## Where changes go (Open/Closed)

- A new kind of Player, a new Goal, or a new way to choose a Square: add a class at that variation point instead of editing the existing ones.
- A new rule of the game: change `docs/domain/` first (glossary, model, invariants, game states, decisions), then the domain classes and their tests.
- A new way to show or read the game: change `tictactoe.console` only.
````

**`harness/branches/with-ddd/docs/domain/decisions.md`**

````markdown
# Decision log

Append new decisions at the end. Never rewrite or delete an entry; when a decision changes, add a new entry that says which one it replaces.

| # | Decision | Why |
|---|---|---|
| D1 | Version 1 is console only. | It is the smallest interface the acceptance tests can drive. |
| D2 | Coordinates are 1-based everywhere, in the console and in the domain. | No off-by-one translation between what a person types and the model. |
| D3 | Color EMPTY is a Null Object; no Square ever returns `null`. | Fewer null checks, and the Board can always be printed. |
| D4 | A rule change starts in `docs/domain/`, then the code, in the same task. | The docs stay the source of truth for people and for agents. |
````

**`harness/branches/with-ddd/src/test/java/tictactoe/ArchitectureTest.java`**

````java
package tictactoe;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.GeneralCodingRules.ACCESS_STANDARD_STREAMS;

/** Executable version of docs/domain/modules.md. */
@AnalyzeClasses(packages = "tictactoe", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    @ArchTest
    static final ArchRule domain_does_not_depend_on_the_console =
            noClasses().that().resideInAPackage("tictactoe.domain..")
                    .should().dependOnClassesThat().resideInAPackage("tictactoe.console..")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule domain_does_not_print =
            noClasses().that().resideInAPackage("tictactoe.domain..")
                    .should(ACCESS_STANDARD_STREAMS)
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule domain_does_not_read_input =
            noClasses().that().resideInAPackage("tictactoe.domain..")
                    .should().accessField(System.class, "in")
                    .orShould().dependOnClassesThat().belongToAnyOf(java.util.Scanner.class, java.io.Console.class)
                    .allowEmptyShould(true);
}
````

### 10.2 Prompts

Each prompt is sent as is, in a new session, to both branches.

**`harness/prompts/F1-build.md`**

````markdown
Implement version 1 of the Tic-Tac-Toe game described in REQUIREMENTS.md.

Follow AGENTS.md and the rules in .bob/rules. Write unit tests for the rules of the game.

You are done when `mvn -q verify` passes and `java -jar target/tictactoe.jar` follows the console contract in REQUIREMENTS.md exactly.
````

**`harness/prompts/F2-cr1-human-vs-ai.md`**

````markdown
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
````

**`harness/prompts/F3-cr2-three-pieces.md`**

````markdown
Change request CR-2: three Pieces each

Add a variant in which each Player has only three Pieces and, once they are all on the Board, moves them.

- Start: `java -jar target/tictactoe.jar --three-pieces`, for two Human Players. The previous modes (no arguments, and `--vs-ai`) must keep working exactly as before. Combining `--three-pieces` with `--vs-ai` is out of scope.
- While a Player has Pieces left, they put as usual, so rounds 1 to 6 are Puts.
- From round 7 on, the current Player moves one of their own Pieces to an empty Square. Input: one line with four whole numbers, `fromRow fromColumn toRow toColumn`, for example `1 1 2 3`.
- A move is invalid when the origin does not hold a Piece of the current Player, when the target is not empty, or when a number is outside 1–3. A line with the wrong amount of numbers is also invalid. Invalid input prints a line that starts with `Invalid`, and the same Player tries again.
- Win does not change, and it is checked after every Put and every move. This variant has no Draw: the game continues until someone wins.
- Otherwise the console contract does not change: the same final board and result line.

Follow AGENTS.md and the rules in .bob/rules. You are done when `mvn -q verify` passes.
````

### 10.3 Acceptance scenarios

Tab-separated. If you copy it from a rendered page and the tabs become spaces, `metrics.py` also accepts two or more spaces between columns. The expected results were computed with a reference model of the rules and checked against a reference implementation.

**`harness/acceptance/scenarios.tsv`**

````text
# Acceptance scenarios. Columns (tab-separated): id, since phase, arguments (- = none), inputs (| = new line),
# expected final board (/ = new row), expected result line, expected number of lines starting with 'Invalid', description.
# Expected values were computed with a reference model of the rules in REQUIREMENTS.md and the change requests.
F1-01	1	-	1 1|2 1|1 2|2 2|1 3	XXX/OO./...	X wins	0	X wins on row 1
F1-02	1	-	1 1|1 2|3 3|2 2|2 1|3 2	XO./XO./.OX	O wins	0	O wins on column 2
F1-03	1	-	1 1|1 2|2 2|1 3|3 3	XOO/.X./..X	X wins	0	X wins on the diagonal
F1-04	1	-	1 1|1 3|2 1|2 2|3 2|3 1	X.O/XO./OX.	O wins	0	O wins on the inverse diagonal
F1-05	1	-	1 3|1 1|2 3|2 2|3 3	O.X/.OX/..X	X wins	0	X wins on column 3
F1-06	1	-	1 1|2 2|3 3|1 3|3 1|2 1|2 3|3 2|1 2	XXO/OOX/XOX	Draw	0	Draw
F1-07	1	-	1 1|1 1|4 1|a b|2 1|1 2|2 2|1 3	XXX/OO./...	X wins	3	Occupied, out of range and garbage are rejected; X wins
F1-08	1	-	1 2|1 3|2 3|3 2|3 3|3 1|2 2|1 1|2 1	OXO/XXX/OOX	X wins	0	Win on the 9th put beats Draw
F1-09	1	-	2 2|2|1 1|1 2 3|0 3|3 3|1 3|3 1|3 2|1 2|2 1|2 3	OXO/OXX/XOX	Draw	3	Wrong amount of numbers and zero are rejected
F2-01	2	--vs-ai	1 1|3 3|2 1	X.O/XO./O.X	O wins	0	AI takes center, a corner, then wins
F2-02	2	--vs-ai	1 1|3 3|3 1|3 2	X.O/OO./XXX	X wins	0	Human wins with a fork
F2-03	2	--vs-ai	1 1|2 2|1 2|3 1|2 3|3 2	XXO/OOX/XXO	Draw	1	Square taken by the AI is rejected; Draw
F3-01	3	--three-pieces	1 1|2 1|1 2|2 2|1 3	XXX/OO./...	X wins	0	Win while putting the third piece
F3-02	3	--three-pieces	1 1|2 1|1 2|2 2|3 3|3 1|3 3 1 3	XXX/OO./O..	X wins	0	X wins by moving a piece
F3-03	3	--three-pieces	1 1|2 1|1 2|2 2|3 3|3 1|2 1 1 3|1 1 2 2|1 3|3 3 1 3	XXX/OO./O..	X wins	3	Invalid moves are rejected; X wins
F3-04	3	--three-pieces	1 1|2 1|1 2|2 2|3 2|1 3|3 2 3 3|1 3 2 3	XX./OOO/..X	O wins	0	O wins by moving a piece
````

### 10.4 Bob settings

Merge this into `~/.bob/settings/settings.json`; do not replace the whole file.

**`harness/bob/settings-approval.json`**

````json
{
  "approval": {
    "allowed_permissions": ["read", "edit", "execute", "todo"],
    "permissionOptions": [
      { "groupId": "read", "enableOutsideWorkspace": false }
    ],
    "allowedExecutors": [
      {
        "toolId": "execute_command",
        "approvedCommands": ["mvn", "java", "ls", "cat"],
        "deniedCommands": ["git", "curl", "wget", "env", "printenv"]
      }
    ]
  }
}
````

### 10.5 Reference model

The team's full diagram as PlantUML: the target after CR-1. It is never given to Bob.

**`harness/reference/model-target.puml`**

````plantuml
@startuml
' Full domain model as the team drew it: the target after CR-1 (Human vs. AI).
' Not given to Bob. Used to compare each branch's code with the domain model, and for the talk.
title Tic-Tac-Toe — domain model (target after CR-1)
hide empty members

class TicTacToe
class Board
class Round
class Player
class Human
class AI
class Goal
class "Win: Row\ncolumn\nDiagonal\nInv Diagonal" as Win
class Put
class Square
class Piece
class Coordinate
class Color
class X
class O
class Empty

TicTacToe *-- Board
TicTacToe *-- Round
TicTacToe *-- "2" Player
Round -> "2" Player
Player --> Board
Player <|-- Human
Player <|-- AI
Player o-- Goal
Player o-- Color
Goal <|-- Win
Goal ..> Square
Goal ..> Put
Board *-- "3x3" Square
Put ..> Square : target
Put ..> Coordinate
Put ..> Piece
Square o-- Piece
Piece *-- Coordinate
Piece *-- Color
Color <|-- X
Color <|-- O
Color <|-- Empty
@enduml
````

### 10.6 Scripts

Make the shell scripts executable (`chmod +x harness/scripts/*.sh harness/scripts/metrics.py`) or call them with `bash` and `python3` as above.

**`harness/scripts/config.sh`**

````bash
# Shared settings for the harness scripts. Override any of them with an environment variable.

# The repository that holds main, without-ddd and with-ddd.
# For a local test use a file:// URL; plain local paths ignore --depth.
REPO_URL="${REPO_URL:-git@github.com:YOUR-USER/tictactoe-bob.git}"

# One clone per run lives here. Keep it OUTSIDE this repository, and make sure
# no parent folder contains an AGENTS.md (Bob Shell loads those too).
RUNS_DIR="${RUNS_DIR:-$HOME/ttt-bob-runs}"

HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RESULTS_DIR="${RESULTS_DIR:-$HARNESS_DIR/results}"

# Same limits for both branches. Leave them empty for the pilot, then set them.
MAX_TURNS="${MAX_TURNS:-}"   # agentic turns per phase
MAX_COST="${MAX_COST:-}"     # Bobcoins per phase

BOB_MODE="${BOB_MODE:-agent}"
BOB_BIN="${BOB_BIN:-bob}"
# Extra flags for every bob run, e.g. "--team-id <id>" when you use a general API key.
BOB_EXTRA_FLAGS="${BOB_EXTRA_FLAGS:-}"

# Set RUN_DESIGN_METRICS=0 to skip coverage, mutation testing, CK, jdeps and SonarQube.
RUN_DESIGN_METRICS="${RUN_DESIGN_METRICS:-1}"
````

**`harness/scripts/setup-branches.sh`**

````bash
#!/usr/bin/env bash
# Creates the orphan branches without-ddd and with-ddd from harness/branches/.
# Run it from a clone of the repository, on main. It does not touch your working tree.
# Orphan branches share no history, so nothing in one branch points to the other.
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
GIT_DIR_ABS="$(git -C "$ROOT" rev-parse --absolute-git-dir)"
SRC="$ROOT/harness/branches"

for BR in without-ddd with-ddd; do
  if git -C "$ROOT" rev-parse -q --verify "refs/heads/$BR" >/dev/null && [[ "${FORCE:-0}" != "1" ]]; then
    echo "Branch $BR already exists. Re-run with FORCE=1 to recreate it." >&2
    exit 1
  fi
  TMP="$(mktemp -d)"
  cp -R "$SRC/common/." "$TMP/"
  cp -R "$SRC/$BR/." "$TMP/"
  (
    cd "$TMP"
    export GIT_DIR="$GIT_DIR_ABS" GIT_WORK_TREE="$TMP" GIT_INDEX_FILE="$TMP.index"
    git add -A .
    TREE="$(git write-tree)"
    COMMIT="$(git commit-tree "$TREE" -m "Starting point")"
    git branch -f "$BR" "$COMMIT"
  )
  rm -rf "$TMP" "$TMP.index"
  echo "Created $BR:"
  git -C "$ROOT" ls-tree -r --name-only "$BR" | sed 's/^/  /'
done

echo
echo "Next: git push -u origin without-ddd with-ddd"
````

**`harness/scripts/new-run.sh`**

````bash
#!/usr/bin/env bash
# Usage: new-run.sh <without-ddd|with-ddd> <run-number>
# Clones ONE branch with ONE commit, no tags and no remote, into a folder with a
# neutral name, so Bob can see neither the other branch, nor any history, nor which
# condition it is in. Tags the starting point as p0.
set -euo pipefail
source "$(dirname "$0")/config.sh"

BRANCH="${1:?branch: without-ddd or with-ddd}"
N="${2:?run number, e.g. 1}"
[[ "$BRANCH" == "without-ddd" || "$BRANCH" == "with-ddd" ]] || { echo "Unknown branch $BRANCH" >&2; exit 1; }

RUN_ID="$BRANCH-r$N"
fail() { echo "ERROR: $*" >&2; exit 1; }

[[ -e "$RESULTS_DIR/$RUN_ID/run.txt" ]] && fail "$RUN_ID already exists in $RESULTS_DIR"

# Bob loads global rules and AGENTS.md files from parent folders: none may exist.
[[ -f "$HOME/.bob/AGENTS.md" ]] && fail "$HOME/.bob/AGENTS.md exists; global context would leak into the run"
if [[ -d "$HOME/.bob/rules" ]] && [[ -n "$(find "$HOME/.bob/rules" -type f -size +0 2>/dev/null | head -1)" ]]; then
  fail "$HOME/.bob/rules is not empty; global rules would leak into the run"
fi
mkdir -p "$RUNS_DIR"
RUNS_ABS="$(cd "$RUNS_DIR" && pwd)"
case "$RUNS_ABS/" in "$HARNESS_DIR/"*) fail "RUNS_DIR must be outside this repository";; esac
d="$RUNS_ABS"
while [[ "$d" != "/" ]]; do
  [[ -f "$d/AGENTS.md" ]] && fail "$d/AGENTS.md exists; Bob would load it as context"
  d="$(dirname "$d")"
done

# Neutral folder name: the path Bob works in must not say which condition it is.
RUN_DIR="$RUNS_ABS/ttt-$(printf '%04x%04x' $RANDOM $RANDOM)"
[[ -e "$RUN_DIR" ]] && fail "$RUN_DIR already exists, try again"

git clone --quiet --single-branch --branch "$BRANCH" --depth 1 --no-tags "$REPO_URL" "$RUN_DIR"
git -C "$RUN_DIR" remote remove origin
git -C "$RUN_DIR" branch -m main
git -C "$RUN_DIR" config user.name "experimenter"
git -C "$RUN_DIR" config user.email "experimenter@example.invalid"
git -C "$RUN_DIR" tag p0

# What Bob will be able to see: one branch called main, one commit, no remote.
[[ "$(git -C "$RUN_DIR" branch -a | wc -l | tr -d ' ')" == "1" ]] || fail "unexpected branches in $RUN_DIR"
[[ "$(git -C "$RUN_DIR" rev-list --count HEAD)" == "1" ]] || fail "unexpected history in $RUN_DIR"
[[ -z "$(git -C "$RUN_DIR" remote)" ]] || fail "remote still configured in $RUN_DIR"

mkdir -p "$RESULTS_DIR/$RUN_ID"
printf 'run_id=%s\nbranch=%s\nrun_dir=%s\ncreated=%s\n' \
  "$RUN_ID" "$BRANCH" "$RUN_DIR" "$(date -u +%Y-%m-%dT%H:%M:%SZ)" > "$RESULTS_DIR/$RUN_ID/run.txt"
echo "$RUN_ID -> $RUN_DIR"
````

**`harness/scripts/run-phase.sh`**

````bash
#!/usr/bin/env bash
# Usage: run-phase.sh <run-id> <1|2|3>
# Runs one phase in a fresh Bob session, then commits and tags the result (Bob never
# touches git) and computes the metrics into results/<run-id>/F<phase>.json.
set -euo pipefail
source "$(dirname "$0")/config.sh"

RUN_ID="${1:?run id, e.g. with-ddd-r1}"
PHASE="${2:?phase: 1, 2 or 3}"
OUT="$RESULTS_DIR/$RUN_ID"
PROMPT="$(ls "$HARNESS_DIR"/prompts/F"$PHASE"-*.md 2>/dev/null | head -1 || true)"
LOG="$OUT/F$PHASE.ndjson"
PREV="p$((PHASE - 1))"

fail() { echo "ERROR: $*" >&2; exit 1; }
[[ -f "$OUT/run.txt" ]] || fail "unknown run $RUN_ID; create it with new-run.sh"
RUN_DIR="$(sed -n 's/^run_dir=//p' "$OUT/run.txt")"
[[ -d "$RUN_DIR/.git" ]] || fail "$RUN_DIR is missing"
[[ -n "$PROMPT" ]] || fail "no prompt for phase $PHASE in $HARNESS_DIR/prompts"
git -C "$RUN_DIR" rev-parse -q --verify "refs/tags/$PREV" >/dev/null || fail "tag $PREV missing: run phase $((PHASE - 1)) first"
git -C "$RUN_DIR" rev-parse -q --verify "refs/tags/p$PHASE" >/dev/null && fail "phase $PHASE already ran for $RUN_ID"
[[ -n "${BOB_API_KEY:-}" ]] || fail "BOB_API_KEY is not set (export it in this shell; never put it in the repo)"
mkdir -p "$OUT"

FLAGS=(--mode "$BOB_MODE" --workspace "$RUN_DIR" --format stream-json --trust --disable-mcp --disable-subagents)
[[ -n "$MAX_TURNS" ]] && FLAGS+=(--max-turns "$MAX_TURNS")
[[ -n "$MAX_COST" ]] && FLAGS+=(--max-cost "$MAX_COST")
if [[ -n "$BOB_EXTRA_FLAGS" ]]; then
  read -r -a EXTRA <<< "$BOB_EXTRA_FLAGS"
  FLAGS+=("${EXTRA[@]}")
fi

echo "[$RUN_ID F$PHASE] bob run started $(date +%H:%M:%S) — log: $LOG"
set +e
( cd "$RUN_DIR" && "$BOB_BIN" run "${FLAGS[@]}" "$(cat "$PROMPT")" ) > "$LOG" 2> "$OUT/F$PHASE.stderr"
BOB_EXIT=$?
set -e
echo "[$RUN_ID F$PHASE] bob run finished $(date +%H:%M:%S) with exit code $BOB_EXIT"

# Version control is ours, not Bob's.
git -C "$RUN_DIR" add -A
git -C "$RUN_DIR" commit -q --allow-empty -m "F$PHASE"
git -C "$RUN_DIR" tag "p$PHASE"

python3 "$HARNESS_DIR/scripts/metrics.py" phase \
  --run-dir "$RUN_DIR" --run-id "$RUN_ID" --phase "$PHASE" --log "$LOG" \
  --scenarios "$HARNESS_DIR/acceptance/scenarios.tsv" --bob-exit "$BOB_EXIT" \
  --out "$OUT/F$PHASE.json"

if [[ "$RUN_DESIGN_METRICS" == "1" ]]; then
  python3 "$HARNESS_DIR/scripts/metrics.py" design \
    --run-dir "$RUN_DIR" --run-id "$RUN_ID" --phase "$PHASE" --out "$OUT/F$PHASE-design.json"
fi

# Leave the next session the same clean tree a fresh checkout would have.
git -C "$RUN_DIR" clean -fdXq
python3 "$HARNESS_DIR/scripts/metrics.py" collect --results "$RESULTS_DIR" --quiet
````

**`harness/scripts/metrics.py`**

````python
#!/usr/bin/env python3
"""Metrics for the Tic-Tac-Toe experiment: DDD vs. no DDD with IBM Bob.

Subcommands
  phase       isolation audit + Bob stats + acceptance tests + Open/Closed diff + vocabulary
  design      unit tests, coverage, mutation score, CK, jdeps and (optionally) SonarQube
  acceptance  only the acceptance tests (handy for the pilot)
  audit       only the isolation audit of a Bob stream-json log
  collect     merge results/<run>/F<n>*.json into results/summary.csv and print medians

Environment
  HARNESS_BUILD_CMD   build used before the acceptance tests (default: mvn -q -Dmaven.test.skip=true package)
  ACCEPTANCE_TIMEOUT  seconds per scenario (default 10)
  CK_JAR              path to CK's jar-with-dependencies; CK is skipped when unset
  SONAR_HOST_URL, SONAR_TOKEN   SonarQube is skipped unless both are set
  BOB_API_KEY         only used to check that the key never appears in a log; it is never printed
"""
from __future__ import annotations

import argparse
import base64
import csv
import json
import os
import re
import shlex
import statistics
import subprocess
import tempfile
import time
import urllib.request
import xml.etree.ElementTree as ET
from pathlib import Path

DEFAULT_BUILD = "mvn -q -Dmaven.test.skip=true package"


# ----------------------------------------------------------------- helpers

def sh(cmd, cwd=None, timeout=1800):
    """Runs a command and returns (exit code, combined output)."""
    try:
        p = subprocess.run(cmd, cwd=cwd, capture_output=True, text=True, timeout=timeout,
                           shell=isinstance(cmd, str))
        return p.returncode, (p.stdout or "") + (p.stderr or "")
    except subprocess.TimeoutExpired:
        return 124, f"timeout after {timeout}s"
    except FileNotFoundError as exc:
        return 127, str(exc)


def write_json(path, data):
    Path(path).parent.mkdir(parents=True, exist_ok=True)
    Path(path).write_text(json.dumps(data, indent=2, sort_keys=True) + "\n", encoding="utf-8")


def inside(path, run_dir):
    """True when a path (absolute, ~, or relative to the run) stays inside the run folder."""
    p = path.strip().strip("'\"")
    if not p:
        return True
    if p in ("/dev/null", "/dev/stdout", "/dev/stderr"):
        return True
    if p.startswith("~"):
        return False
    root = Path(run_dir).resolve()
    candidate = Path(p) if p.startswith("/") else root / p
    try:
        return candidate.resolve().is_relative_to(root)
    except (OSError, RuntimeError):
        return False


def relative(path, run_dir):
    p = path.strip().strip("'\"")
    if p.startswith("/"):
        try:
            return str(Path(p).resolve().relative_to(Path(run_dir).resolve()))
        except ValueError:
            return p
    return p[2:] if p.startswith("./") else p


# ------------------------------------------------------- acceptance tests

def load_scenarios(path):
    scenarios = []
    for raw in Path(path).read_text(encoding="utf-8").splitlines():
        if not raw.strip() or raw.startswith("#"):
            continue
        cols = raw.split("\t") if "\t" in raw else re.split(r"\s{2,}", raw.strip(), maxsplit=7)
        sid, since, args, inputs, board, result, invalid, desc = cols
        scenarios.append({
            "id": sid, "since": int(since), "args": [] if args == "-" else args.split(),
            "inputs": inputs.split("|"), "board": board.split("/"), "result": result,
            "invalid": int(invalid), "desc": desc,
        })
    return scenarios


def run_scenario(jar, sc, timeout):
    stdin = "\n".join(sc["inputs"]) + "\n"
    try:
        p = subprocess.run(["java", "-jar", str(jar), *sc["args"]], input=stdin,
                           capture_output=True, text=True, timeout=timeout)
    except subprocess.TimeoutExpired:
        return {"id": sc["id"], "passed": False, "problems": [f"timeout after {timeout:g}s"]}
    shown = [line.strip() for line in p.stdout.replace("\r", "").split("\n") if line.strip()]
    invalid = sum(1 for line in shown if line.startswith("Invalid"))
    tail = shown[-4:]
    problems = []
    if p.returncode != 0:
        problems.append(f"exit status {p.returncode}")
    if len(tail) < 4 or tail[:3] != sc["board"]:
        problems.append(f"final board {tail[:3]} expected {sc['board']}")
    if not tail or tail[-1] != sc["result"]:
        problems.append(f"result line {tail[-1] if tail else None!r} expected {sc['result']!r}")
    if invalid != sc["invalid"]:
        problems.append(f"{invalid} 'Invalid' lines, expected {sc['invalid']}")
    return {"id": sc["id"], "passed": not problems, "problems": problems}


def acceptance(run_dir, phase, scenarios_path, skip_build=False):
    run_dir = Path(run_dir)
    res = {"acceptance_build_ok": True}
    if not skip_build:
        code, out = sh(os.environ.get("HARNESS_BUILD_CMD", DEFAULT_BUILD), cwd=run_dir, timeout=1800)
        res["acceptance_build_ok"] = code == 0
        if code != 0:
            res["acceptance_build_log_tail"] = out[-2000:]
    jar = run_dir / "target" / "tictactoe.jar"
    timeout = float(os.environ.get("ACCEPTANCE_TIMEOUT", "10"))
    results = []
    for sc in load_scenarios(scenarios_path):
        if sc["since"] > phase:
            continue
        if jar.exists():
            results.append(run_scenario(jar, sc, timeout))
        else:
            results.append({"id": sc["id"], "passed": False, "problems": ["target/tictactoe.jar not found"]})
    res["acceptance_total"] = len(results)
    res["acceptance_passed"] = sum(r["passed"] for r in results)
    for ph in (1, 2, 3):
        sub = [r for r in results if r["id"].startswith(f"F{ph}-")]
        if sub:
            res[f"acceptance_F{ph}_passed"] = sum(r["passed"] for r in sub)
            res[f"acceptance_F{ph}_total"] = len(sub)
    res["acceptance_failures"] = [r for r in results if not r["passed"]]
    return res


# ------------------------------------------------------------ Bob logs

EDIT_TOOLS = {"write_file", "write_to_file", "apply_diff", "insert_content", "search_and_replace",
              "edit_file", "replace_in_file"}
READ_TOOLS = {"read_file", "list_files", "search_files", "list_code_definition_names",
              "codebase_search", "getsymbolsoverview", "get_symbols_overview"}
READ_COMMANDS = {"cat", "head", "tail", "less", "grep", "rg", "find", "ls", "tree", "wc", "sed", "awk"}


def read_events(log):
    events = []
    p = Path(log)
    if not p.exists():
        return events
    for line in p.read_text(encoding="utf-8", errors="replace").splitlines():
        line = line.strip()
        if not line.startswith("{"):
            continue
        try:
            ev = json.loads(line)
        except json.JSONDecodeError:
            continue
        if isinstance(ev, dict):
            events.append(ev)
    return events


def ev_type(ev):
    return str(ev.get("type") or ev.get("event") or "")


def tool_name(ev):
    return str(ev.get("tool_name") or ev.get("name") or ev.get("tool") or "")


def tool_params(ev):
    p = ev.get("parameters", ev.get("input", ev.get("args", {})))
    if isinstance(p, str):
        try:
            p = json.loads(p)
        except json.JSONDecodeError:
            p = {"value": p}
    return p if isinstance(p, dict) else {"value": p}


def tool_kind(name):
    n = name.lower()
    if n in EDIT_TOOLS or any(k in n for k in ("write", "apply", "insert", "replace", "edit")):
        return "edit"
    if "command" in n or n.startswith("exec"):
        return "command"
    if n in READ_TOOLS or any(k in n for k in ("read", "list", "search", "symbol")):
        return "read"
    return "other"


def command_of(params):
    for key in ("command", "cmd"):
        if isinstance(params.get(key), str):
            return params[key]
    return ""


def paths_of(params):
    out = []
    for key, value in params.items():
        k = key.lower()
        if not any(t in k for t in ("path", "file", "dir", "cwd")):
            continue
        if isinstance(value, str):
            out.append(value)
        elif isinstance(value, list):
            out.extend(v for v in value if isinstance(v, str))
    return out


def command_segments(cmd):
    """First word of every simple command in a shell line (skipping VAR=value)."""
    firsts = []
    for segment in re.split(r"&&|\|\||[;|()\n`]", cmd):
        try:
            words = shlex.split(segment, posix=True)
        except ValueError:
            words = segment.split()
        while words and re.match(r"^[A-Za-z_][A-Za-z0-9_]*=", words[0]):
            words = words[1:]
        while words and words[0] in ("sudo", "command", "exec", "time", "nohup", "xargs"):
            words = words[1:]
        if words:
            firsts.append(os.path.basename(words[0]))
    return firsts


def command_tokens(cmd):
    try:
        words = shlex.split(cmd, posix=True)
    except ValueError:
        words = cmd.split()
    tokens = []
    for w in words:
        tokens.extend(t for t in re.split(r"[=,;|&<>()]", w) if t)
    return tokens


def is_read_command(ev):
    if tool_kind(tool_name(ev)) != "command":
        return False
    firsts = command_segments(command_of(tool_params(ev)))
    return bool(firsts) and all(f in READ_COMMANDS for f in firsts)


def audit(log, run_dir):
    violations = []
    for i, ev in enumerate(read_events(log)):
        if ev_type(ev) != "tool_use":
            continue
        name, params = tool_name(ev), tool_params(ev)
        cmd = command_of(params)
        if cmd:
            firsts = command_segments(cmd)
            for word, label in (("git", "git command"), ("curl", "network command"), ("wget", "network command"),
                                ("env", "environment variables"), ("printenv", "environment variables")):
                if word in firsts:
                    violations.append({"event": i, "tool": name, "rule": label, "detail": cmd[:200]})
            if re.search(r"\$\{?BOB_", cmd):
                violations.append({"event": i, "tool": name, "rule": "environment variables", "detail": cmd[:200]})
            for tok in command_tokens(cmd):
                if (tok.startswith(("/", "~")) or ".." in tok) and not inside(tok, run_dir):
                    violations.append({"event": i, "tool": name, "rule": "path outside the workspace", "detail": tok[:200]})
        for p in paths_of(params):
            if not inside(p, run_dir):
                violations.append({"event": i, "tool": name, "rule": "path outside the workspace", "detail": p[:200]})
    key = os.environ.get("BOB_API_KEY", "")
    if len(key) >= 8 and Path(log).exists() and key in Path(log).read_text(encoding="utf-8", errors="replace"):
        violations.append({"event": None, "tool": None, "rule": "BOB_API_KEY value appears in the log",
                           "detail": "<redacted>"})
    return {"audit_violations": len(violations), "audit_details": violations[:50]}


def bobstats(log, run_dir):
    events = read_events(log)
    res = {}
    result = next((ev for ev in reversed(events) if ev_type(ev) == "result"), None)
    if result is None:
        res["bob_status"] = "no result event"
    else:
        res["bob_status"] = result.get("status")
        for key, value in (result.get("stats") or {}).items():
            if value is None or isinstance(value, (int, float, str)):
                res[f"bob_{key}"] = value
    res["bob_error_events"] = sum(1 for ev in events if ev_type(ev) == "error")

    uses = [ev for ev in events if ev_type(ev) == "tool_use"]
    results_by_id = {str(ev.get("tool_id")): ev for ev in events if ev_type(ev) == "tool_result"}
    kinds = [tool_kind(tool_name(ev)) for ev in uses]
    res["tool_uses"] = len(uses)
    for kind in ("read", "edit", "command", "other"):
        res[f"tool_uses_{kind}"] = kinds.count(kind)

    first_edit = next((i for i, k in enumerate(kinds) if k == "edit"), len(uses))
    res["tool_uses_before_first_edit"] = first_edit
    res["reads_before_first_edit"] = sum(
        1 for ev in uses[:first_edit] if tool_kind(tool_name(ev)) == "read" or is_read_command(ev))

    def touched(ev):
        return [relative(p, run_dir) for p in paths_of(tool_params(ev))]

    def doc_like(p):
        return p.startswith("docs/") or p in ("AGENTS.md", "REQUIREMENTS.md")

    res["doc_reads"] = sum(1 for ev in uses if tool_kind(tool_name(ev)) == "read" and any(doc_like(p) for p in touched(ev)))
    edits = [(i, p) for i, ev in enumerate(uses) if tool_kind(tool_name(ev)) == "edit" for p in touched(ev)]
    first_doc = next((i for i, p in edits if p.startswith("docs/")), None)
    first_code = next((i for i, p in edits if p.startswith("src/")), None)
    res["docs_edited"] = first_doc is not None
    res["docs_before_code"] = first_doc is not None and (first_code is None or first_doc < first_code)

    builds = [ev for ev in uses if tool_kind(tool_name(ev)) == "command"
              and any(f in ("mvn", "mvnw") for f in command_segments(command_of(tool_params(ev))))]
    failed = 0
    for ev in builds:
        r = results_by_id.get(str(ev.get("tool_id")))
        if r is None:
            continue
        text = json.dumps(r)
        if str(r.get("status", "")).lower() in ("error", "failed", "failure") or "BUILD FAILURE" in text or "[ERROR]" in text:
            failed += 1
    res["builds"] = len(builds)
    res["builds_failed"] = failed
    return res


# ------------------------------------------------- Open/Closed (git diff)

def git(run_dir, *args):
    code, out = sh(["git", "-C", str(run_dir), *args], timeout=120)
    if code != 0:
        raise RuntimeError(out)
    return out


def ocp(run_dir, from_ref, to_ref):
    res = {k: 0 for k in ("main_added", "main_modified", "main_deleted",
                          "test_added", "test_modified", "test_deleted", "docs_changed")}
    for line in git(run_dir, "diff", "--name-status", "-M", from_ref, to_ref).splitlines():
        parts = line.split("\t")
        if len(parts) < 2:
            continue
        status, path = parts[0][0], parts[-1]
        if path.startswith("docs/"):
            res["docs_changed"] += 1
            continue
        if path.startswith("src/main/java/") and path.endswith(".java"):
            kind = "main"
        elif path.startswith("src/test/") and path.endswith(".java"):
            kind = "test"
        else:
            continue
        if status == "A":
            res[f"{kind}_added"] += 1
        elif status == "D":
            res[f"{kind}_deleted"] += 1
        else:
            res[f"{kind}_modified"] += 1
    churn = 0
    for line in git(run_dir, "diff", "--numstat", "-M", "--diff-filter=MRD", from_ref, to_ref,
                    "--", "src/main/java").splitlines():
        added, deleted = line.split("\t")[:2]
        if added.isdigit() and deleted.isdigit():
            churn += int(added) + int(deleted)
    res["churn_existing_main"] = churn
    total = res["main_added"] + res["main_modified"]
    res["ocp_ratio"] = round(res["main_added"] / total, 3) if total else None
    res["existing_tests_touched"] = res["test_modified"] + res["test_deleted"]
    return res


# ------------------------------------------------------------ vocabulary

DISCARDED = ["cell", "tile", "grid", "matrix", "mark", "token", "symbol", "stone", "spot", "turn", "tie",
             "victory", "position", "user", "participant", "computer", "bot", "cpu"]
MODEL_TERMS = ["board", "square", "coordinate", "piece", "color", "player", "round", "put", "goal", "win", "draw"]
JAVA_KEYWORDS = set("""abstract assert boolean break byte case catch char class const continue default do double else
enum extends final finally float for goto if implements import instanceof int interface long native new package private
protected public return short static strictfp super switch synchronized this throw throws transient try void volatile
while var record yield sealed permits true false null""".split())
WORD = re.compile(r"[A-Z]+(?=[A-Z][a-z]|\d|\b|_)|[A-Z]?[a-z]+|\d+")


def strip_java(src):
    src = re.sub(r"/\*.*?\*/", " ", src, flags=re.S)
    src = re.sub(r"//[^\n]*", " ", src)
    src = re.sub(r'"""(.*?)"""', " ", src, flags=re.S)
    src = re.sub(r'"(\\.|[^"\\\n])*"', " ", src)
    return re.sub(r"'(\\.|[^'\\\n])+'", " ", src)


def norm(word):
    return word[:-1] if len(word) > 3 and word.endswith("s") and not word.endswith("ss") else word


def vocab(run_dir, phase):
    discarded = DISCARDED + (["move"] if phase < 3 else [])
    terms = MODEL_TERMS + (["human", "ai"] if phase >= 2 else []) + (["move"] if phase >= 3 else [])
    counts = {w: 0 for w in discarded}
    seen = set()
    io_files = 0
    for f in Path(run_dir, "src", "main", "java").rglob("*.java"):
        src = f.read_text(encoding="utf-8", errors="replace")
        if re.search(r"System\.(out|err|in)\b|\bScanner\b|\bjava\.io\.Console\b", src):
            io_files += 1
        for ident in re.findall(r"\b[A-Za-z_$][A-Za-z0-9_$]*\b", strip_java(src)):
            if ident in JAVA_KEYWORDS:
                continue
            for w in (norm(x.lower()) for x in WORD.findall(ident)):
                seen.add(w)
                if w in counts:
                    counts[w] += 1
    found = {w: n for w, n in counts.items() if n}
    missing = [t for t in terms if t not in seen]
    return {
        "discarded_terms": sum(found.values()),
        "discarded_terms_detail": json.dumps(found, sort_keys=True),
        "model_terms_present": len(terms) - len(missing),
        "model_terms_total": len(terms),
        "model_terms_missing": " ".join(missing),
        "io_classes": io_files,
    }


# --------------------------------------------------------- design metrics

def surefire_and_jacoco(run_dir):
    res = {}
    code, out = sh("mvn -q -Dmaven.test.failure.ignore=true verify", cwd=run_dir, timeout=3600)
    res["verify_exit"] = code
    if code != 0:
        res["verify_log_tail"] = out[-1500:]
    tests = failures = 0
    for f in Path(run_dir, "target", "surefire-reports").glob("TEST-*.xml"):
        try:
            root = ET.parse(f).getroot()
        except ET.ParseError:
            continue
        tests += int(root.get("tests", 0))
        failures += int(root.get("failures", 0)) + int(root.get("errors", 0))
    res["unit_tests"] = tests
    res["unit_failures"] = failures
    report = Path(run_dir, "target", "site", "jacoco", "jacoco.csv")
    if report.exists():
        lm = lc = bm = bc = 0
        with report.open(encoding="utf-8") as fh:
            for row in csv.DictReader(fh):
                lm += int(row["LINE_MISSED"])
                lc += int(row["LINE_COVERED"])
                bm += int(row["BRANCH_MISSED"])
                bc += int(row["BRANCH_COVERED"])
        res["line_coverage"] = round(100 * lc / (lm + lc), 1) if lm + lc else None
        res["branch_coverage"] = round(100 * bc / (bm + bc), 1) if bm + bc else None
    return res


def pit(run_dir):
    code, out = sh("mvn -q org.pitest:pitest-maven:mutationCoverage", cwd=run_dir, timeout=3600)
    report = Path(run_dir, "target", "pit-reports", "mutations.xml")
    if not report.exists():
        return {"mutation_error": out[-1500:]}
    mutations = ET.parse(report).getroot().findall("mutation")
    killed = sum(1 for m in mutations if m.get("detected") == "true")
    return {"mutants": len(mutations), "mutants_killed": killed,
            "mutation_score": round(100 * killed / len(mutations), 1) if mutations else None}


def ck(run_dir, ck_jar):
    out_dir = Path(tempfile.mkdtemp(prefix="ck-"))
    code, out = sh(["java", "-jar", ck_jar, str(Path(run_dir, "src", "main", "java")), "false", "0", "false",
                    str(out_dir) + os.sep], timeout=900)
    class_csv = next(iter(sorted(out_dir.glob("*class.csv"))), None)
    if class_csv is None:
        return {"ck_error": out[-1500:]}
    with class_csv.open(encoding="utf-8") as fh:
        rows = list(csv.DictReader(fh))

    def values(col):
        vals = []
        for row in rows:
            try:
                v = float(row.get(col, ""))
            except ValueError:
                continue
            if v == v and v >= 0:  # skip NaN and the -1 that CK uses for "not applicable"
                vals.append(v)
        return vals

    res = {"ck_classes": len(rows)}
    for col, label in (("cbo", "cbo"), ("fanout", "fanout"), ("wmc", "wmc"), ("rfc", "rfc"), ("lcom*", "lcom_star")):
        vals = values(col)
        if vals:
            res[f"ck_{label}_median"] = round(statistics.median(vals), 3)
            res[f"ck_{label}_max"] = round(max(vals), 3)
    vals = values("tcc")
    if vals:
        res["ck_tcc_median"] = round(statistics.median(vals), 3)
        res["ck_tcc_min"] = round(min(vals), 3)
    return res


def jdeps(run_dir):
    classes = Path(run_dir, "target", "classes")
    if not classes.exists():
        return {"jdeps_error": "target/classes is missing"}
    code, out = sh(["jdeps", "-verbose:package", str(classes)], timeout=300)
    edges = set()
    for line in out.splitlines():
        m = re.match(r"\s+(\S+)\s+->\s+(\S+)", line)
        if m and m.group(1).startswith("tictactoe") and m.group(2).startswith("tictactoe") and m.group(1) != m.group(2):
            edges.add((m.group(1), m.group(2)))
    packages = {".".join(p.relative_to(classes).parent.parts) or "(default)" for p in classes.rglob("*.class")}
    ce = {p: len({b for a, b in edges if a == p}) for p in packages}
    ca = {p: len({a for a, b in edges if b == p}) for p in packages}
    instability = {p: round(ce[p] / (ca[p] + ce[p]), 2) if ca[p] + ce[p] else None for p in packages}
    return {
        "packages": len(packages),
        "package_edges": len(edges),
        "package_cycles": sum(1 for a, b in edges if (b, a) in edges) // 2,
        "package_ce_max": max(ce.values(), default=0),
        "package_instability": json.dumps(instability, sort_keys=True),
    }


def sonar(run_dir, key):
    host = os.environ["SONAR_HOST_URL"].rstrip("/")
    code, out = sh(f"mvn -q org.sonarsource.scanner.maven:sonar-maven-plugin:sonar "
                   f"-Dsonar.projectKey={key} -Dsonar.host.url={host}", cwd=run_dir, timeout=1800)
    if code != 0:
        return {"sonar_error": out[-1500:]}
    auth = "Basic " + base64.b64encode(f"{os.environ['SONAR_TOKEN']}:".encode()).decode()

    def get(url):
        req = urllib.request.Request(url, headers={"Authorization": auth})
        with urllib.request.urlopen(req, timeout=30) as resp:
            return json.load(resp)

    task = Path(run_dir, "target", "sonar", "report-task.txt")
    props = dict(line.split("=", 1) for line in task.read_text().splitlines() if "=" in line) if task.exists() else {}
    if props.get("ceTaskUrl"):
        for _ in range(120):
            if get(props["ceTaskUrl"]).get("task", {}).get("status") in ("SUCCESS", "FAILED", "CANCELED"):
                break
            time.sleep(5)
    keys = ("ncloc,complexity,cognitive_complexity,duplicated_lines_density,code_smells,"
            "sqale_index,sqale_rating,coverage")
    data = get(f"{host}/api/measures/component?component={key}&metricKeys={keys}")
    return {f"sonar_{m['metric']}": m.get("value") for m in data.get("component", {}).get("measures", [])}


def design(run_dir, run_id, phase):
    res = surefire_and_jacoco(run_dir)
    res.update(pit(run_dir))
    if os.environ.get("CK_JAR"):
        res.update(ck(run_dir, os.environ["CK_JAR"]))
    res.update(jdeps(run_dir))
    if os.environ.get("SONAR_HOST_URL") and os.environ.get("SONAR_TOKEN"):
        res.update(sonar(run_dir, f"ttt-{run_id}-F{phase}"))
    return res


# --------------------------------------------------------------- collect

KEY_METRICS = ["bob_total_tokens", "bob_session_costs", "bob_duration_ms", "tool_uses",
               "tool_uses_before_first_edit", "builds_failed", "acceptance_passed", "acceptance_total",
               "main_modified", "ocp_ratio", "churn_existing_main", "existing_tests_touched",
               "discarded_terms", "io_classes", "ck_cbo_median", "ck_lcom_star_median", "mutation_score"]


def collect(results_dir, quiet=False):
    results_dir = Path(results_dir)
    rows = []
    for run in sorted(p for p in results_dir.iterdir() if p.is_dir()):
        meta = {}
        if (run / "run.txt").exists():
            meta = dict(l.split("=", 1) for l in (run / "run.txt").read_text().splitlines() if "=" in l)
        for ph in (1, 2, 3):
            row = {}
            for name in (f"F{ph}.json", f"F{ph}-design.json"):
                if (run / name).exists():
                    row.update(json.loads((run / name).read_text(encoding="utf-8")))
            if not row:
                continue
            row = {k: v for k, v in row.items()
                   if not isinstance(v, (list, dict)) and not k.endswith(("_tail", "_error"))}
            row.update({"run_id": run.name, "branch": meta.get("branch", ""), "phase": ph})
            rows.append(row)
    keys = ["run_id", "branch", "phase"] + sorted({k for r in rows for k in r} - {"run_id", "branch", "phase"})
    tmp = results_dir / f".summary.{os.getpid()}.csv"
    with tmp.open("w", newline="", encoding="utf-8") as fh:
        writer = csv.DictWriter(fh, fieldnames=keys)
        writer.writeheader()
        writer.writerows(rows)
    os.replace(tmp, results_dir / "summary.csv")
    print(f"{results_dir / 'summary.csv'}: {len(rows)} rows")
    if quiet:
        return

    def num(v):
        try:
            return float(v)
        except (TypeError, ValueError):
            return None

    invalid = sorted({f"{r['run_id']} F{r['phase']}" for r in rows if (num(r.get("audit_violations")) or 0) > 0})
    if invalid:
        print("Left out of the medians because of isolation violations: " + ", ".join(invalid))
    rows = [r for r in rows if not (num(r.get("audit_violations")) or 0) > 0]

    for ph in sorted({r["phase"] for r in rows}):
        print(f"\nPhase F{ph} — median per branch (number of runs in brackets)")
        branches = sorted({r["branch"] for r in rows if r["phase"] == ph})
        print(f"{'metric':32}" + "".join(f"{b:>18}" for b in branches))
        for metric in KEY_METRICS:
            cells = []
            for b in branches:
                vals = [num(r.get(metric)) for r in rows if r["phase"] == ph and r["branch"] == b]
                vals = [v for v in vals if v is not None]
                cells.append(f"{statistics.median(vals):>14.6g} [{len(vals)}]" if vals else f"{'—':>18}")
            print(f"{metric:32}" + "".join(cells))


# ------------------------------------------------------------------ CLI

def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = ap.add_subparsers(dest="cmd", required=True)

    p = sub.add_parser("phase")
    p.add_argument("--run-dir", required=True)
    p.add_argument("--run-id", required=True)
    p.add_argument("--phase", type=int, required=True)
    p.add_argument("--log", required=True)
    p.add_argument("--scenarios", required=True)
    p.add_argument("--bob-exit", type=int, default=None)
    p.add_argument("--skip-build", action="store_true")
    p.add_argument("--out", required=True)

    d = sub.add_parser("design")
    d.add_argument("--run-dir", required=True)
    d.add_argument("--run-id", required=True)
    d.add_argument("--phase", type=int, required=True)
    d.add_argument("--out", required=True)

    a = sub.add_parser("acceptance")
    a.add_argument("--run-dir", required=True)
    a.add_argument("--phase", type=int, required=True)
    a.add_argument("--scenarios", required=True)
    a.add_argument("--skip-build", action="store_true")

    u = sub.add_parser("audit")
    u.add_argument("--log", required=True)
    u.add_argument("--run-dir", required=True)

    c = sub.add_parser("collect")
    c.add_argument("--results", required=True)
    c.add_argument("--quiet", action="store_true", help="only write summary.csv")

    args = ap.parse_args()
    if args.cmd == "phase":
        res = {"run_id": args.run_id, "phase": args.phase, "bob_exit": args.bob_exit}
        res.update(audit(args.log, args.run_dir))
        res.update(bobstats(args.log, args.run_dir))
        res.update(acceptance(args.run_dir, args.phase, args.scenarios, args.skip_build))
        res.update(ocp(args.run_dir, f"p{args.phase - 1}", f"p{args.phase}"))
        res.update(vocab(args.run_dir, args.phase))
        write_json(args.out, res)
        print(f"[{args.run_id} F{args.phase}] tokens={res.get('bob_total_tokens')} cost={res.get('bob_session_costs')} "
              f"acceptance={res['acceptance_passed']}/{res['acceptance_total']} "
              f"ocp_ratio={res['ocp_ratio']} discarded_terms={res['discarded_terms']}")
        if res["audit_violations"]:
            print(f"WARNING: {res['audit_violations']} isolation violations — discard this run. "
                  f"See audit_details in {args.out}")
    elif args.cmd == "design":
        res = design(args.run_dir, args.run_id, args.phase)
        write_json(args.out, res)
        print(f"[{args.run_id} F{args.phase}] design metrics -> {args.out}")
    elif args.cmd == "acceptance":
        res = acceptance(args.run_dir, args.phase, args.scenarios, args.skip_build)
        print(json.dumps(res, indent=2))
    elif args.cmd == "audit":
        print(json.dumps(audit(args.log, args.run_dir), indent=2))
    elif args.cmd == "collect":
        collect(args.results, args.quiet)


if __name__ == "__main__":
    try:
        main()
    except BrokenPipeError:  # e.g. output piped into head
        os._exit(0)
````

### 10.7 `.gitignore` of `main`

**`.gitignore`**

````text
.env
*.local
.DS_Store
harness/results/.summary.*.csv
````

## References

- IBM Bob: [custom rules](https://bob.ibm.com/docs/ide/configuration/rules), [ignoring files](https://bob.ibm.com/docs/ide/configuration/bobignore), [context window management](https://bob.ibm.com/docs/ide/core-concepts/context-window-management)
- Bob Shell: [non-interactive sessions (`bob run`)](https://bob.ibm.com/docs/shell/getting-started/start-bobshell-non-interactive), [approval settings](https://bob.ibm.com/docs/shell/configuration/approval-settings), [configuring Bob Shell](https://bob.ibm.com/docs/shell/configuration/configuring)
- [CK: Chidamber & Kemerer metrics for Java](https://github.com/mauricioaniche/ck)
- [SonarQube metric definitions](https://docs.sonarsource.com/sonarqube-server/2025.6/user-guide/code-metrics/metrics-definition)
- [ArchUnit](https://www.archunit.org), [PIT mutation testing](https://pitest.org), [JaCoCo](https://www.jacoco.org/jacoco/)
- Eric Evans, *Domain-Driven Design* (2003); Vlad Khononov, *Learning Domain-Driven Design* (O'Reilly, 2021); Bertrand Meyer, *Object-Oriented Software Construction* (1988)
