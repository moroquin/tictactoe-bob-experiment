#!/usr/bin/env bash
# Usage: run-phase.sh <run-id> <1|2|3>
# Runs one phase in a fresh Bob session, takes Bob's session history out of the
# workspace (so the next session cannot read it), commits and tags the result
# (Bob never touches git), and computes the metrics into results/<run-id>/.
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
mkdir -p "$OUT/history"

FLAGS=(--mode "$BOB_MODE" --workspace "$RUN_DIR" --format stream-json --trust --disable-mcp --disable-subagents)
[[ -n "$MAX_TURNS" ]] && FLAGS+=(--max-turns "$MAX_TURNS")
[[ -n "$MAX_COST" ]] && FLAGS+=(--max-cost "$MAX_COST")
if [[ -n "$BOB_EXTRA_FLAGS" ]]; then
  read -r -a EXTRA <<< "$BOB_EXTRA_FLAGS"
  FLAGS+=("${EXTRA[@]}")
fi

STARTED="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
echo "[$RUN_ID F$PHASE] bob run started $STARTED — log: $LOG"
set +e
if [[ "$LIVE" == "1" ]]; then
  ( cd "$RUN_DIR" && "$BOB_BIN" run "${FLAGS[@]}" "$(cat "$PROMPT")" ) 2> "$OUT/F$PHASE.stderr" \
    | tee "$LOG" | python3 "$HARNESS_DIR/scripts/metrics.py" live --prefix "[$RUN_ID F$PHASE]"
  BOB_EXIT=${PIPESTATUS[0]}
else
  ( cd "$RUN_DIR" && "$BOB_BIN" run "${FLAGS[@]}" "$(cat "$PROMPT")" ) > "$LOG" 2> "$OUT/F$PHASE.stderr"
  BOB_EXIT=$?
fi
set -e
ENDED="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
echo "[$RUN_ID F$PHASE] bob run finished $ENDED with exit code $BOB_EXIT"

# Take the session history out of the workspace before committing: if it stayed,
# the next session would read it as extra documentation. publish-run.sh publishes
# it in RESULTS.md without putting it back into the workspace.
HIST_RAW="$OUT/history/F$PHASE.bob.md"
HIST_WHERE=""
rm -f "$HIST_RAW"
for candidate in history/session.md HISTORY.md history.md; do
  if [[ -f "$RUN_DIR/$candidate" ]]; then
    mv "$RUN_DIR/$candidate" "$HIST_RAW"
    HIST_WHERE="$candidate"
    break
  fi
done
if [[ -d "$RUN_DIR/history" ]]; then
  if [[ -n "$(ls -A "$RUN_DIR/history")" ]]; then
    mkdir -p "$OUT/history/F$PHASE-other-files"
    cp -R "$RUN_DIR/history/." "$OUT/history/F$PHASE-other-files/"
  fi
  rm -rf "$RUN_DIR/history"
fi

# Version control is ours, not Bob's.
git -C "$RUN_DIR" add -A
git -C "$RUN_DIR" commit -q --allow-empty -m "F$PHASE"
git -C "$RUN_DIR" tag "p$PHASE"

python3 "$HARNESS_DIR/scripts/metrics.py" phase \
  --run-dir "$RUN_DIR" --run-id "$RUN_ID" --phase "$PHASE" --log "$LOG" \
  --scenarios "$HARNESS_DIR/acceptance/scenarios.tsv" --bob-exit "$BOB_EXIT" \
  --history "$HIST_RAW" --history-where "$HIST_WHERE" --started "$STARTED" --ended "$ENDED" \
  --out "$OUT/F$PHASE.json"

if [[ "$RUN_DESIGN_METRICS" == "1" ]]; then
  python3 "$HARNESS_DIR/scripts/metrics.py" design \
    --run-dir "$RUN_DIR" --run-id "$RUN_ID" --phase "$PHASE" --out "$OUT/F$PHASE-design.json"
fi

# The history of this phase: the numbers above plus Bob's own account.
python3 "$HARNESS_DIR/scripts/metrics.py" history-report --results-run "$OUT" --run-id "$RUN_ID" --phase "$PHASE" >/dev/null

# Leave the next session the same clean tree a fresh checkout would have.
git -C "$RUN_DIR" clean -fdXq
python3 "$HARNESS_DIR/scripts/metrics.py" collect --results "$RESULTS_DIR" --quiet
