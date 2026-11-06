# Tic-Tac-Toe with IBM Bob: DDD vs. no DDD

IBM Bob builds the same Tic-Tac-Toe twice, from the same requirements and with the same prompts:

- in `without-ddd`, with the requirements alone;
- in `with-ddd`, with the requirements plus the DDD documentation: ubiquitous language, domain model, invariants, game states and module boundaries.

Each branch ends up with its own game and a `RESULTS.md` holding the time, tokens and cost of every phase, the decisions Bob had to make on its own, and the quality metrics. From `main`, Bob compares both branches and writes the analysis.

This page is the quick start. [EXPERIMENT.md](EXPERIMENT.md) has the full design, every metric and every file.

## How it works

| Branch | What it holds |
|---|---|
| `main` | This guide, `EXPERIMENT.md` and `harness/`: the prompts, the scripts, the acceptance tests and, at the end, the analysis |
| `without-ddd`, `with-ddd` | The starting point at first. After the run: Bob's code, one commit per phase, and `RESULTS.md` |
| tags `start/without-ddd`, `start/with-ddd` | The untouched starting point of each branch. Every run starts here |

The same three prompts go to both branches, each in a new Bob session:

| Phase | Prompt | Result |
|---|---|---|
| F1 | Build the game for two people | `java -jar target/tictactoe.jar` |
| F2 | CR-1: play against the PC, a deterministic opponent that Bob programs (fixed rules, no AI) | `java -jar target/tictactoe.jar --vs-pc` |
| F3 | CR-2: three Pieces each, which move once they are all on the Board | `java -jar target/tictactoe.jar --three-pieces` |

## 1. Install the tools

- git, Java 21, Maven 3.9 or later, and Python 3.9 or later.
- Bob Shell, which needs Node.js 24 or later. On macOS or Linux:
  ```bash
  curl -fsSL https://bob.ibm.com/download/bobshell.sh | bash
  ```
  The [Bob Shell installation guide](https://bob.ibm.com/docs/shell/getting-started/install-and-setup) has the other options.
- Windows: use WSL.

## 2. Create your Bob API key and make it available

The scripts run Bob without anyone at the keyboard, so Bob signs in with an API key instead of the browser.

1. **Create the key.** Sign in at [bob.ibm.com](https://bob.ibm.com), open your subscription instance, and create an API key in the API key management section. Choose the **Inference** scope; a **General** key also needs your team ID. Copy the key right away: Bob does not show it again.

2. **Make it available as `BOB_API_KEY`.** Pick one way:
   - **Only for the terminal you are in.** Nothing is shown or saved:
     ```bash
     read -rs BOB_API_KEY && export BOB_API_KEY      # paste the key and press Enter
     ```
   - **macOS, kept in the Keychain**, so every new terminal has it:
     ```bash
     security add-generic-password -a "$USER" -s bob-api-key -w      # paste the key when asked
     echo 'export BOB_API_KEY="$(security find-generic-password -a "$USER" -s bob-api-key -w 2>/dev/null)"' >> ~/.zshrc
     source ~/.zshrc
     ```
   - **Linux, in a file only you can read**:
     ```bash
     mkdir -p ~/.config/bob && (umask 077 && read -rs k && printf '%s' "$k" > ~/.config/bob/api-key)
     echo 'export BOB_API_KEY="$(cat ~/.config/bob/api-key)"' >> ~/.bashrc
     source ~/.bashrc
     ```

3. **Check it without printing it:**
   ```bash
   [ -n "$BOB_API_KEY" ] && echo "BOB_API_KEY is set (${#BOB_API_KEY} characters)"
   ```

4. **Only if your key is General**, add your team ID:
   ```bash
   export BOB_EXTRA_FLAGS="--team-id <your-team-id>"
   ```

Never put the key in the repository or on screen during a demo, and never `echo` it. `.env` is in `.gitignore`, and the scripts check every Bob log for the key.

## 3. Configure Bob once

- Merge `harness/bob/settings-approval.json` into `~/.bob/settings/settings.json`, keeping your other keys. It denies `git`, `curl`, `wget`, `env` and `printenv`, and keeps Bob from reading outside its folder.
- Leave `~/.bob/rules/` empty, and do not create `~/.bob/AGENTS.md`: global rules would reach both branches.

## 4. Create the branches

From the root of this repository, on `main`:

```bash
bash harness/scripts/setup-branches.sh
git push -u origin without-ddd with-ddd refs/tags/start/without-ddd refs/tags/start/with-ddd
```

If they already exist, recreate them with `FORCE=1 bash harness/scripts/setup-branches.sh`, then run the push it prints, which uses `--force`.

## 5. Tell the scripts where things are

```bash
export REPO_URL=git@github.com:<you>/<repo>.git   # this repository
export RUNS_DIR=$HOME/ttt-bob-runs                 # where Bob works: outside this repository
```

No folder above `RUNS_DIR` may contain an `AGENTS.md`. Optional: `MAX_COST` and `MAX_TURNS` limit each phase (use the same values for both branches), and `LIVE=0` hides the live view.

## 6. Check that Bob loads the rules (once)

```bash
bash harness/scripts/new-run.sh with-ddd 0
RUN_DIR=$(sed -n 's/^run_dir=//p' harness/results/with-ddd-r0/run.txt)
(cd "$RUN_DIR" && bob run --workspace "$RUN_DIR" --trust --format json \
  "List the rules and the AGENTS.md instructions you were given for this workspace. Do not change any file.") \
  | python3 -c 'import json, sys; print(json.load(sys.stdin)["last_message"])'
rm -rf "$RUN_DIR" harness/results/with-ddd-r0
```

The answer must mention the three workspace rules (stay in the folder and do not use git, work without asking, write the session history) and the golden rules of `AGENTS.md`. If it does not, stop here: the experiment depends on it. Section 5 of `EXPERIMENT.md` has the full pilot.

## 7. Run the prompts in each branch

```bash
bash harness/scripts/run-branch.sh without-ddd &
bash harness/scripts/run-branch.sh with-ddd &
wait
```

Each command sends the three prompts, one after the other, each to a new Bob session, and shows every action of Bob in the terminal. After each phase it saves the result into its branch: the code and an updated `RESULTS.md`.

- **One phase at a time:** `bash harness/scripts/run-branch.sh with-ddd 1`, and later the same command with `2`, or with nothing to finish.
- **Start a branch over:** `FORCE=1 bash harness/scripts/run-branch.sh with-ddd`.

Bob works in an isolated copy of the branch's starting point: one commit, no remote, and a folder whose name does not say which branch it is. So it can see neither the other branch nor any history. You never need to open that folder: the result goes to the branch.

## 8. Play each version live

```bash
bash harness/scripts/play.sh without-ddd                  # two people
bash harness/scripts/play.sh with-ddd --vs-pc             # against the PC
bash harness/scripts/play.sh with-ddd --three-pieces      # three Pieces each
bash harness/scripts/play.sh with-ddd-r1/p1               # the version after phase 1
```

`play.sh` downloads the version into `~/ttt-play/`, builds it when it changed, and starts the game. To play, type a row and a column, for example `2 3`. Before a talk, prepare everything with `PLAY_BUILD_ONLY=1 bash harness/scripts/play.sh with-ddd` (and the same for `without-ddd`).

## 9. Compare the branches on main

```bash
bash harness/scripts/evaluate.sh
```

It copies both branches into `harness/results/compare/`, inside `main`, so Bob can read them without git. Then it starts a Bob session on `main` that writes the analysis to `harness/results/CONCLUSIONS.md`: the numbers side by side, the decisions each branch had to make on its own and which DDD document answers each one, the pending items, the design, and what it all says about each hypothesis.

To use the Bob IDE instead, run `EVAL_PREPARE_ONLY=1 bash harness/scripts/evaluate.sh`, open `main` in the IDE, and send the content of `harness/prompts/EVAL-compare.md`.

Read `CONCLUSIONS.md` critically: every number in it must appear in a `RESULTS.md`. Then commit `harness/results/` to `main`.

With one run per branch, the differences are observations of that run, not proof. To repeat the experiment several times per branch, see section 6 of `EXPERIMENT.md`.

## If something fails

| Message | What to do |
|---|---|
| `BOB_API_KEY is not set` | Step 2, in the same terminal |
| `tag start/with-ddd is not in …` | Step 4: push the branches and the `start/` tags |
| `branch with-ddd already holds the results of a run` | Continue the current run, or start over with `FORCE=1` |
| `… AGENTS.md exists …` or `~/.bob/rules is not empty` | Move `RUNS_DIR` somewhere else, or empty the global rules while you run the experiment |
| `NOTE: Bob did not write history/session.md` | Bob is not loading `.bob/rules`: repeat step 6 |
| `WARNING: … isolation violations` | Bob reached outside its folder: discard that run and start the branch over with `FORCE=1` |
