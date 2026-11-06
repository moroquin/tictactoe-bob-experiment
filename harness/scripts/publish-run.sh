#!/usr/bin/env bash
# Usage: publish-run.sh <run-id>
# Publishes a run as it stands after its last finished phase. You can run it after every
# phase. It never touches the run's workspace, so the next session still cannot read the
# history. It:
#   1. writes RESULTS.md (results table and Bob's history for every phase so far),
#   2. builds a commit with the code of the last phase plus RESULTS.md,
#   3. pushes it to the branch of the run, with the phase tags as <run-id>/p0, <run-id>/p1…
#      PUBLISH_TO=branch pushes to the experiment branch itself (without-ddd or with-ddd);
#      the default pushes to results/<run-id>. PUBLISH_PUSH=0 skips the push.
#   4. copies the published files to results/<run-id>/published/ for the comparison on main.
set -euo pipefail
source "$(dirname "$0")/config.sh"

RUN_ID="${1:?run id, e.g. with-ddd-r1}"
OUT="$RESULTS_DIR/$RUN_ID"
fail() { echo "ERROR: $*" >&2; exit 1; }

[[ -f "$OUT/run.txt" ]] || fail "unknown run $RUN_ID"
RUN_DIR="$(sed -n 's/^run_dir=//p' "$OUT/run.txt")"
BRANCH="$(sed -n 's/^branch=//p' "$OUT/run.txt")"
[[ -d "$RUN_DIR/.git" ]] || fail "$RUN_DIR is missing"
LAST=""
for p in 3 2 1; do
  if git -C "$RUN_DIR" rev-parse -q --verify "refs/tags/p$p" >/dev/null; then LAST="p$p"; break; fi
done
[[ -n "$LAST" ]] || fail "$RUN_ID has no finished phase"

# 1. RESULTS.md
python3 "$HARNESS_DIR/scripts/metrics.py" results-doc --results-run "$OUT" --run-id "$RUN_ID" \
  --out "$OUT/RESULTS.md" >/dev/null

# 2. A commit = code of the last phase + RESULTS.md, built with a temporary index.
INDEX="$(mktemp -u)"
GIT_INDEX_FILE="$INDEX" git -C "$RUN_DIR" read-tree "$LAST"
BLOB="$(git -C "$RUN_DIR" hash-object -w "$OUT/RESULTS.md")"
GIT_INDEX_FILE="$INDEX" git -C "$RUN_DIR" update-index --add --cacheinfo "100644,$BLOB,RESULTS.md"
TREE="$(GIT_INDEX_FILE="$INDEX" git -C "$RUN_DIR" write-tree)"
rm -f "$INDEX"
COMMIT="$(git -C "$RUN_DIR" commit-tree "$TREE" -p "$LAST" -m "Results of $RUN_ID after phase ${LAST#p}")"

# 3. Push.
if [[ "${PUBLISH_TO:-results}" == "branch" ]]; then
  TARGET="$BRANCH"
else
  TARGET="results/$RUN_ID"
fi
if [[ "${PUBLISH_PUSH:-1}" == "1" ]]; then
  git -C "$RUN_DIR" push -q --force "$REPO_URL" "$COMMIT:refs/heads/$TARGET"
  for t in $(git -C "$RUN_DIR" tag -l 'p*'); do
    git -C "$RUN_DIR" push -q --force "$REPO_URL" "refs/tags/$t:refs/tags/$RUN_ID/$t"
  done
  echo "[$RUN_ID] published up to phase ${LAST#p} in branch $TARGET (tags $RUN_ID/p0…$RUN_ID/$LAST)"
fi

# 4. Copy for main.
rm -rf "$OUT/published"
mkdir -p "$OUT/published"
git -C "$RUN_DIR" archive "$COMMIT" | tar -x -C "$OUT/published"
