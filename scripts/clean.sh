#!/usr/bin/env bash
# Restarts the experiment from zero: deletes every run (results/ and runs/), the conclusions and the
# experiment folders ../without-ddd and ../with-ddd, then runs scripts/init.sh, which checks the setup and
# creates both folders again from src/.
#
# It never touches src/, prompts/, scenarios/, the scripts, Bob's settings or Bob's database (~/.bob/db):
# the audit and the measurement only count the Bob sessions after the new reset.
#
# Usage: scripts/clean.sh [-y]
#   -y skips the confirmation.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WORKSPACE="$(dirname "$ROOT")"
cd "$ROOT"

yes=0
for arg in "$@"; do
  case "$arg" in
    -y|--yes) yes=1 ;;
    -h|--help) sed -n '2,10p' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
    *) echo "unknown argument: $arg (use -y or -h)"; exit 1 ;;
  esac
done

# What will be deleted
targets=()
[ -d results ] && [ -n "$(ls -A results 2>/dev/null)" ] && targets+=("results/ (snapshots, metrics, presentation, discarded phases)")
[ -d runs ] && [ -n "$(ls -A runs 2>/dev/null)" ] && targets+=("runs/ (archived runs and stats; not in git)")
[ -f CONCLUSIONS.md ] && targets+=("CONCLUSIONS.md")
for c in without-ddd with-ddd; do
  [ -e "$WORKSPACE/$c" ] && targets+=("$WORKSPACE/$c (will be created again from src/$c)")
done

if [ "${#targets[@]}" -eq 0 ]; then
  echo "Nothing to delete: the experiment is already clean."
else
  echo "This deletes:"
  printf '  - %s\n' "${targets[@]}"
  if git ls-files --error-unmatch results >/dev/null 2>&1 || [ -n "$(git ls-files results CONCLUSIONS.md)" ]; then
    echo "  (what is committed in results/ or CONCLUSIONS.md can be recovered from git history)"
  fi
  if [ -n "$(git status --porcelain -- results CONCLUSIONS.md 2>/dev/null)" ]; then
    echo "  ! results/ or CONCLUSIONS.md have changes that are not committed: they will be lost."
  fi
  if [ -d runs ] && [ -n "$(ls -A runs 2>/dev/null)" ]; then
    echo "  ! runs/ is ignored by git: its runs will be lost."
  fi
  if [ "$yes" -ne 1 ]; then
    read -r -p "Delete all of it and start a clean experiment? [y/N] " answer
    case "$answer" in y|Y|yes|YES) ;; *) echo "Stopped: nothing was deleted."; exit 1 ;; esac
  fi
fi

rm -rf results runs CONCLUSIONS.md __pycache__
for c in without-ddd with-ddd; do
  rm -rf "${WORKSPACE:?}/$c"
done
mkdir -p results runs
echo "Deleted. Checking the setup and creating the experiment folders…"
echo
exec "$ROOT/scripts/init.sh"
