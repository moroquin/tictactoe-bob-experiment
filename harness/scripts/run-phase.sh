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
