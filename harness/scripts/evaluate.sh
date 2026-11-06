#!/usr/bin/env bash
# Usage: evaluate.sh                      run it from main
#   EVAL_PREPARE_ONLY=1 evaluate.sh       only copy the branches; then use the prompt in the Bob IDE
#
# Copies the current content of without-ddd and with-ddd (code, docs and RESULTS.md) into
# harness/results/compare/, inside main's workspace, so Bob can read both branches without git.
# Then it starts a new Bob session on main with harness/prompts/EVAL-compare.md, which writes
# the analysis to harness/results/CONCLUSIONS.md.
set -euo pipefail
source "$(dirname "$0")/config.sh"
ROOT="$(cd "$HARNESS_DIR/.." && pwd)"
fail() { echo "ERROR: $*" >&2; exit 1; }

for BR in without-ddd with-ddd; do
  DEST="$RESULTS_DIR/compare/$BR"
  TMP="$(mktemp -d)"
  git -c advice.detachedHead=false clone -q --depth 1 --branch "$BR" "$REPO_URL" "$TMP/src" ||
    fail "could not read branch $BR from $REPO_URL"
  rm -rf "$DEST"
  mkdir -p "$DEST"
  git -C "$TMP/src" archive HEAD | tar -x -C "$DEST"
  rm -rf "$TMP"
  if [[ -f "$DEST/RESULTS.md" ]]; then
    echo "Copied $BR to $DEST"
  else
    echo "WARNING: $BR has no RESULTS.md yet. Run: bash harness/scripts/run-branch.sh $BR" >&2
  fi
done

if compgen -G "$RESULTS_DIR/*/F1.json" >/dev/null; then
  python3 "$HARNESS_DIR/scripts/metrics.py" collect --results "$RESULTS_DIR" --quiet
fi

if [[ "${EVAL_PREPARE_ONLY:-0}" == "1" ]]; then
  echo "Ready. In the Bob IDE, open $ROOT and send the content of harness/prompts/EVAL-compare.md."
  exit 0
fi

[[ -n "${BOB_API_KEY:-}" ]] || fail "BOB_API_KEY is not set (see README.md, step 2)"
FLAGS=(--mode "$BOB_MODE" --workspace "$ROOT" --format json --trust --disable-mcp --disable-subagents)
if [[ -n "$BOB_EXTRA_FLAGS" ]]; then
  read -r -a EXTRA <<< "$BOB_EXTRA_FLAGS"
  FLAGS+=("${EXTRA[@]}")
fi
echo "Bob is analyzing the results; this takes a few minutes…"
( cd "$ROOT" && "$BOB_BIN" run "${FLAGS[@]}" "$(cat "$HARNESS_DIR/prompts/EVAL-compare.md")" ) \
  > "$RESULTS_DIR/evaluation.json" 2> "$RESULTS_DIR/evaluation.stderr" || true
python3 - "$RESULTS_DIR/evaluation.json" <<'PY' || true
import json, sys
try:
    data = json.load(open(sys.argv[1], encoding="utf-8"))
    stats = data.get("stats") or {}
    print(data.get("last_message", ""))
    print(f"\n(evaluation: {stats.get('total_tokens')} tokens, {stats.get('session_costs')} Bobcoins)")
except (OSError, ValueError):
    print("Could not read Bob's answer; see harness/results/evaluation.stderr")
PY
if [[ -f "$RESULTS_DIR/CONCLUSIONS.md" ]]; then
  echo "Analysis: $RESULTS_DIR/CONCLUSIONS.md"
else
  echo "WARNING: Bob did not write $RESULTS_DIR/CONCLUSIONS.md" >&2
fi
