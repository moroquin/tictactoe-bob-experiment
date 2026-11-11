#!/usr/bin/env bash
# One-time setup before the first run: checks the tools and Bob's configuration,
# then creates ../without-ddd and ../with-ddd from src/ with `measure.py reset`.
#
# Usage: scripts/init.sh
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

errors=0
warnings=0
fail() { echo "  ✗ $*"; errors=$((errors + 1)); }
warn() { echo "  ! $*"; warnings=$((warnings + 1)); }
ok()   { echo "  ✓ $*"; }

echo "Tools"
for tool in python3 java mvn jdeps javap bob; do
  if command -v "$tool" >/dev/null 2>&1; then ok "$tool"; else fail "$tool is not installed or not in PATH"; fi
done
if command -v java >/dev/null 2>&1; then
  version="$(java -version 2>&1 | sed -n 's/.*version "\([0-9]*\).*/\1/p' | head -1)"
  if [ "${version:-0}" -ge 21 ]; then ok "Java $version"; else fail "Java 21 or later is needed (found ${version:-unknown})"; fi
fi
if python3 -c "import pptx" >/dev/null 2>&1; then ok "python-pptx"; else warn "python-pptx is missing (only for the presentation): pip install python-pptx"; fi

echo "Bob configuration"
settings="$HOME/.bob/settings/settings.json"
problem="$(python3 - "$settings" <<'EOF'
import json, sys
try:
    d = json.load(open(sys.argv[1]))
except (OSError, ValueError):
    sys.exit("it is missing or is not valid JSON")
execs = [e for e in d.get("approval", {}).get("allowedExecutors", []) if e.get("toolId") == "execute_command"]
denied = {c for e in execs for c in e.get("deniedCommands", [])}
approved = [c for e in execs for c in e.get("approvedCommands", [])]
# git, network and environment, and the shell commands that read files by path (they could read outside the folder)
forbidden = ("git", "curl", "wget", "env", "printenv", "cat", "head", "tail", "less", "more", "grep", "find", "ls",
             "sort", "wc", "du", "df", "which", "file", "stat", "tree")
missing = [c for c in forbidden if c not in denied]
risky = [c for c in approved if c.split()[0] in forbidden]
msg = []
if missing:
    msg.append("not denied: " + ", ".join(missing))
if risky:
    msg.append("approved: " + ", ".join(risky))
print("; ".join(msg))
EOF
)" || problem="$problem"
if [ -z "$problem" ]; then
  ok "$settings denies git, the network, env and the shell commands that read files"
else
  fail "$settings: $problem. Merge config/bob-settings.json into it"
fi
if [ -d "$HOME/.bob/rules" ] && [ -n "$(ls -A "$HOME/.bob/rules" 2>/dev/null)" ]; then
  fail "$HOME/.bob/rules is not empty: Bob would load those rules in both folders"
else
  ok "no global Bob rules"
fi
for f in "$HOME/.bob/AGENTS.md" "$(dirname "$ROOT")/AGENTS.md"; do
  if [ -e "$f" ]; then fail "$f exists: Bob would load it in both folders"; fi
done
workspace="$(dirname "$ROOT")"
if top="$(git -C "$workspace" rev-parse --show-toplevel 2>/dev/null)"; then
  fail "$workspace is inside the git repository $top: the experiment folders would have git"
else
  ok "the experiment folders will be outside any git repository"
fi
[ -f src/without-ddd/REQUIREMENTS.md ] && [ -f src/with-ddd/REQUIREMENTS.md ] && ok "src/ has both starting points" \
  || fail "src/without-ddd or src/with-ddd is incomplete"

echo "Same starting point"
# The only difference between the folders is docs/domain/ and the AGENTS.md that points to it.
for f in REQUIREMENTS.md ARCHITECTURE.md pom.xml .gitignore src/test/java/enrollment/ArchitectureTest.java \
         .bob/rules/00-workspace.md .bob/rules/01-autonomy.md .bob/rules/02-history.md; do
  if cmp -s "src/without-ddd/$f" "src/with-ddd/$f"; then ok "$f"; else fail "$f differs between src/without-ddd and src/with-ddd"; fi
done
if python3 oracle.py check >/dev/null 2>&1; then
  ok "the reference model reproduces the examples of the requirements and the change requests"
else
  fail "oracle.py check fails: the reference model and the requirements or prompts disagree"
fi

if [ "$errors" -gt 0 ]; then
  echo; echo "$errors problem(s): fix them and run scripts/init.sh again."
  exit 1
fi

echo "Experiment folders"
if ls -d results/*/F[1-7] >/dev/null 2>&1; then
  echo "  results/ already holds the snapshots of a run. To archive it and start another one:"
  echo "    scripts/new_run.sh \"what with-ddd received in that run\""
  exit 1
fi
python3 measure.py reset
note=""
[ "$warnings" -gt 0 ] && note=" ($warnings warning(s) above)"
echo; echo "Ready$note. Open a terminal in ../without-ddd and another in ../with-ddd, run bob in each and send prompts/F1-build.md."
