# Shared settings for the harness scripts. Override any of them with an environment variable.

# The repository that holds main, without-ddd and with-ddd.
# For a local test use a file:// URL; plain local paths ignore --depth.
REPO_URL="${REPO_URL:-git@github.com:YOUR-USER/tictactoe-bob.git}"

# One clone per run lives here. Keep it OUTSIDE this repository, and make sure
# no parent folder contains an AGENTS.md (Bob Shell loads those too).
RUNS_DIR="${RUNS_DIR:-$HOME/ttt-bob-runs}"

HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RESULTS_DIR="${RESULTS_DIR:-$HARNESS_DIR/results}"

# Same limits for both branches. Leave them empty for the pilot, then set them.
MAX_TURNS="${MAX_TURNS:-}"   # agentic turns per phase
MAX_COST="${MAX_COST:-}"     # Bobcoins per phase

BOB_MODE="${BOB_MODE:-agent}"
BOB_BIN="${BOB_BIN:-bob}"
# Extra flags for every bob run, e.g. "--team-id <id>" when you use a general API key.
BOB_EXTRA_FLAGS="${BOB_EXTRA_FLAGS:-}"

# Set RUN_DESIGN_METRICS=0 to skip coverage, mutation testing, CK, jdeps and SonarQube.
RUN_DESIGN_METRICS="${RUN_DESIGN_METRICS:-1}"
