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
