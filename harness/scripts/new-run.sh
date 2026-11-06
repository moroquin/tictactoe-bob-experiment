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
