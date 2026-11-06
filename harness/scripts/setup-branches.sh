#!/usr/bin/env bash
# Creates the orphan branches without-ddd and with-ddd from harness/branches/, and a tag
# start/<branch> on each starting commit. Runs always start from the tag, so a branch can
# hold the results of a run while the starting point stays untouched.
# Run it from a clone of the repository, on main. It does not touch your working tree.
# Orphan branches share no history, so nothing in one branch points to the other.
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
GIT_DIR_ABS="$(git -C "$ROOT" rev-parse --absolute-git-dir)"
SRC="$ROOT/harness/branches"
[[ -d "$SRC/common" ]] || { echo "ERROR: $SRC/common not found; the branch sources must be in harness/branches/" >&2; exit 1; }

for BR in without-ddd with-ddd; do
  if [[ "${FORCE:-0}" != "1" ]] && { git -C "$ROOT" rev-parse -q --verify "refs/heads/$BR" >/dev/null ||
                                     git -C "$ROOT" rev-parse -q --verify "refs/tags/start/$BR" >/dev/null; }; then
    echo "Branch $BR or tag start/$BR already exists. Re-run with FORCE=1 to recreate them" >&2
    echo "(the branch loses any results saved in it)." >&2
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
    git tag -f "start/$BR" "$COMMIT" >/dev/null
  )
  rm -rf "$TMP" "$TMP.index"
  echo "Created branch $BR and tag start/$BR:"
  git -C "$ROOT" ls-tree -r --name-only "$BR" | sed 's/^/  /'
done

echo
PUSH="git push -u origin without-ddd with-ddd refs/tags/start/without-ddd refs/tags/start/with-ddd"
if [[ "${FORCE:-0}" == "1" ]]; then
  echo "Next: ${PUSH/git push/git push --force}"
else
  echo "Next: $PUSH"
fi
