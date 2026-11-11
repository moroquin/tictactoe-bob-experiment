#!/usr/bin/env bash
# Starts a new run: measures the current run if needed, archives it in runs/run-N/,
# and restores ../without-ddd and ../with-ddd from src/ with `measure.py reset`.
# It never touches src/.
#
# Usage: scripts/new_run.sh ["what with-ddd received in the run being archived"] [-y]
#   The description is only needed if results/description.txt does not exist yet.
#   -y skips the confirmation before the experiment folders are deleted.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

description=""
yes=0
for arg in "$@"; do
  case "$arg" in
    -y|--yes) yes=1 ;;
    -h|--help) sed -n '2,9p' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
    *) description="$arg" ;;
  esac
done

has_run=0
if ls -d results/*/F[1-7] >/dev/null 2>&1; then
  has_run=1
  phases="$(ls -d results/*/F[1-7] | xargs -n1 basename | sort -u | tr '\n' ' ')"
  echo "results/ holds a run with phases: $phases"
  if [ -z "$description" ] && [ ! -f results/description.txt ]; then
    echo "Say what with-ddd received in this run, for example:"
    echo "  scripts/new_run.sh \"Docs drive the development: AGENTS.md workflow, file map and decision log\""
    exit 1
  fi
else
  echo "results/ holds no snapshots: nothing to archive."
fi

# Ask first, before archiving anything: reset deletes ../without-ddd and ../with-ddd: any work there that is not in a snapshot is lost.
if [ "$yes" -ne 1 ]; then
  read -r -p "Archive the current run (if any), delete ../without-ddd and ../with-ddd and restore them from src/? [y/N] " answer
  case "$answer" in y|Y|yes|YES) ;; *) echo "Stopped: nothing was archived or deleted."; exit 1 ;; esac
fi

if [ "$has_run" -eq 1 ]; then
  if [ ! -f results/metrics.json ]; then
    echo "The run is not measured yet: measuring it (one or two minutes per snapshot)…"
    python3 measure.py
  fi
  if [ -n "$description" ]; then
    python3 measure.py archive "$description"
  else
    python3 measure.py archive
  fi
fi
python3 measure.py reset
echo; echo "New run ready. Open a terminal in ../without-ddd and another in ../with-ddd, run bob in each and send prompts/F1-build.md."
