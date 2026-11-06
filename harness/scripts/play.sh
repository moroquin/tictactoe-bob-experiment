#!/usr/bin/env bash
# Usage: play.sh <branch-or-tag> [game arguments]
#   play.sh without-ddd                  two people, latest result of the branch
#   play.sh with-ddd --vs-pc             against the PC
#   play.sh with-ddd --three-pieces      three Pieces each
#   play.sh with-ddd-r1/p1               the game as it was after phase 1 of that run
#   PLAY_BUILD_ONLY=1 play.sh with-ddd   download and build now, play later
# Downloads the version from the repository into $PLAY_DIR, builds it when it changed,
# and starts the game, so you can play each branch live.
set -euo pipefail
source "$(dirname "$0")/config.sh"

REF="${1:?branch or tag, e.g. with-ddd}"
shift
DIR="$PLAY_DIR/${REF//\//_}"
mkdir -p "$PLAY_DIR"

if [[ -d "$DIR/.git" ]]; then
  git -C "$DIR" fetch -q --depth 1 origin "$REF"
  git -C "$DIR" -c advice.detachedHead=false checkout -q --detach FETCH_HEAD
else
  git -c advice.detachedHead=false clone -q --depth 1 --branch "$REF" "$REPO_URL" "$DIR"
fi

HEAD_SHA="$(git -C "$DIR" rev-parse HEAD)"
STAMP="$DIR/target/.built-from"
if [[ ! -f "$DIR/target/tictactoe.jar" || "$(cat "$STAMP" 2>/dev/null)" != "$HEAD_SHA" ]]; then
  echo "Building $REF…" >&2
  (cd "$DIR" && ${HARNESS_BUILD_CMD:-mvn -q -Dmaven.test.skip=true package})
  echo "$HEAD_SHA" > "$STAMP"
fi

[[ "${PLAY_BUILD_ONLY:-0}" == "1" ]] && { echo "Ready: $REF in $DIR" >&2; exit 0; }
echo "Playing $REF. Type a row and a column, for example: 2 3" >&2
cd "$DIR"
exec java -jar target/tictactoe.jar "$@"
