#!/usr/bin/env bash
# Usage: run-branch.sh <without-ddd|with-ddd> [phases]     default phases: "1 2 3"
#   run-branch.sh with-ddd          the three prompts, one after the other
#   run-branch.sh with-ddd 1        only F1; run it again with 2 (or nothing) to go on
#   FORCE=1 run-branch.sh with-ddd  start the branch over with a new run
#
# One run per branch. Each phase runs in a new Bob session, inside an isolated copy of
# the branch's starting point (see new-run.sh), and after each phase the result is saved
# into the branch itself: Bob's code, one commit per phase, and RESULTS.md.
set -euo pipefail
source "$(dirname "$0")/config.sh"

BRANCH="${1:?branch: without-ddd or with-ddd}"
PHASES="${2:-1 2 3}"
SCRIPTS="$HARNESS_DIR/scripts"
fail() { echo "ERROR: $*" >&2; exit 1; }

# The current run of this branch: the newest run that this script created.
RUN_ID=""
BEST=-1
for f in "$RESULTS_DIR"/"$BRANCH"-r*/run.txt; do
  [[ -f "$f" ]] || continue
  grep -q '^mode=branch$' "$f" || continue
  ID="$(sed -n 's/^run_id=//p' "$f")"
  NUM="${ID##*-r}"
  if [[ "$NUM" =~ ^[0-9]+$ ]] && (( NUM > BEST )); then
    BEST="$NUM"
    RUN_ID="$ID"
  fi
done

if [[ -z "$RUN_ID" || "${FORCE:-0}" == "1" ]]; then
  # A new run. The branch must still be at its starting point, unless FORCE=1.
  if [[ "${FORCE:-0}" != "1" ]]; then
    HEAD_SHA="$(git ls-remote "$REPO_URL" "refs/heads/$BRANCH" | cut -f1)"
    START_SHA="$(git ls-remote "$REPO_URL" "refs/tags/start/$BRANCH" | cut -f1)"
    [[ -n "$START_SHA" ]] || fail "tag start/$BRANCH is not in $REPO_URL: run setup-branches.sh and push what it prints"
    [[ "$HEAD_SHA" == "$START_SHA" ]] ||
      fail "branch $BRANCH already holds the results of a run. Use FORCE=1 to start it over."
  fi
  N=1
  while [[ -e "$RESULTS_DIR/$BRANCH-r$N" ]]; do N=$((N + 1)); done
  RUN_MODE=branch bash "$SCRIPTS/new-run.sh" "$BRANCH" "$N"
  RUN_ID="$BRANCH-r$N"
fi
RUN_DIR="$(sed -n 's/^run_dir=//p' "$RESULTS_DIR/$RUN_ID/run.txt")"

for PHASE in $PHASES; do
  if git -C "$RUN_DIR" rev-parse -q --verify "refs/tags/p$PHASE" >/dev/null; then
    echo "[$RUN_ID] phase $PHASE is already done"
    continue
  fi
  bash "$SCRIPTS/run-phase.sh" "$RUN_ID" "$PHASE"
  PUBLISH_TO=branch bash "$SCRIPTS/publish-run.sh" "$RUN_ID"
done
echo "[$RUN_ID] branch $BRANCH is up to date. Play it with: bash harness/scripts/play.sh $BRANCH"
