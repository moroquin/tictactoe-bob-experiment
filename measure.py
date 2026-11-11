#!/usr/bin/env python3
"""Measures the two experiment folders and writes results/metrics.md.

Usage:
  python3 measure.py snapshot F1   # when a phase ends in BOTH folders: saves a copy of the code
                                   # and of its history in results/<folder>/F1/
  python3 measure.py               # measures every saved snapshot and writes results/summary.md and metrics.md
  python3 measure.py audit         # checks in ~/.bob/db/bob.db that no session left its folder
  python3 measure.py reset         # restores ../without-ddd and ../with-ddd from src/ (deletes what Bob did)
  python3 measure.py archive "what with-ddd received"
                                   # saves the measured run in the history: moves results/ to runs/run-N/
  python3 measure.py stats         # medians, signs and correlations of every run -> runs/stats.md
  python3 measure.py all           # measures every run again (after the metrics change) and the stats

The measured folders are ../without-ddd and ../with-ddd. They are never modified: each snapshot is built
in a temporary folder. Needs java 21, mvn and jdeps.
"""
import difflib, json, os, re, shutil, subprocess, sys, tempfile
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parent
WORKSPACE = ROOT.parent
FOLDERS = ["without-ddd", "with-ddd"]
RESULTS = Path(os.environ.get("RESULTS", ROOT / "results")).resolve()  # RESULTS=runs/run-1 measures another run
RUNS = ROOT / "runs"
SCENARIOS = ROOT / "scenarios.tsv"
GLOSSARY = WORKSPACE / "with-ddd" / "docs" / "domain" / "glossary.md"
TYPES = ["ambiguity", "gap", "design", "naming", "scope", "deviation", "workaround"]
# Experiment series: goes up when the requirements, the prompts or the scenarios change, so that the stats never
# mix runs that received different things. Series 2: JSONL board log.
SERIES = 2


def _rel(p):
    """Path relative to the repository when it is inside it; absolute otherwise."""
    p = Path(p).resolve()
    return p.relative_to(ROOT) if p.is_relative_to(ROOT) else p


def has_snapshots(folder):
    """True when folder holds at least one phase snapshot (folder/<without-ddd|with-ddd>/F<n>)."""
    return any(p.is_dir() for c in FOLDERS for p in (folder / c).glob("F[1-3]"))


def sh(cmd, cwd=None, timeout=900, input=None):
    p = subprocess.run(cmd, cwd=cwd, capture_output=True, text=True, timeout=timeout, input=input,
                       shell=isinstance(cmd, str))
    return p.returncode, p.stdout + p.stderr


def _now_ms():
    import time
    return int(time.time() * 1000)


# ---------------------------------------------------------------- control plane: no git in the experiment folders
def git_exposure(folder):
    """Why git would work in folder, or None. This repository is the control plane: the experiment folders must
    have no .git of their own and must not be inside any git repository, so that git fails there even if Bob tried it."""
    folder = Path(folder)
    if (folder / ".git").exists():
        return f"{folder} has its own .git"
    p = subprocess.run(["git", "-C", str(folder), "rev-parse", "--show-toplevel"], capture_output=True, text=True)
    if p.returncode == 0:
        return f"{folder} is inside the git repository {p.stdout.strip()}"
    return None


# ---------------------------------------------------------------- snapshot
def snapshot(phase):
    if not re.fullmatch(r"F[1-3]", phase):
        sys.exit("phase: F1, F2 or F3")
    for c in FOLDERS:
        src, dst = WORKSPACE / c, RESULTS / c / phase
        if not src.is_dir():
            sys.exit(f"{src} does not exist")
        if dst.exists():
            sys.exit(f"{dst} already exists; delete it to take the snapshot again")
        exposed = git_exposure(src)
        shutil.copytree(src, dst, ignore=shutil.ignore_patterns("target", ".DS_Store", ".idea", ".vscode", ".git"))
        meta = {"taken_ms": _now_ms()}
        if exposed:
            meta["git_exposure"] = exposed
        (dst / ".snapshot.json").write_text(json.dumps(meta))
        print(f"snapshot {c} {phase} -> {_rel(dst)}")
        if exposed:
            print(f"  WARNING: {exposed}: Bob could use git in this phase. Check it with  python3 measure.py audit")
        # The history leaves the folder (it is already in the snapshot): if it stayed, the next session would
        # read it and without-ddd would get a decision log for free, which is exactly what is being measured.
        for h in (src / "history").glob("session-*.md"):
            h.unlink()
            print(f"  history {h.name} moved out of {c}")


# ---------------------------------------------------------------- audit
def audit():
    """Reads (without modifying it) the Bob Shell database and checks each session of both folders:
    paths outside its folder and forbidden commands (git, curl, wget, env, printenv). It also checks that no
    experiment folder, and no snapshot, could use git."""
    import sqlite3
    from datetime import datetime
    db = Path.home() / ".bob" / "db" / "bob.db"
    if not db.exists():
        sys.exit(f"{db} does not exist: did you use Bob Shell?")
    with tempfile.TemporaryDirectory() as tmp:
        for f in db.parent.glob("bob.db*"):
            shutil.copy(f, tmp)
        con = sqlite3.connect(Path(tmp) / "bob.db")
        invalid = 0
        reset_file = RESULTS / "reset.json"
        since = json.loads(reset_file.read_text()).get("reset_ms", 0) if reset_file.exists() else 0
        if since:
            print(f"Sessions since the last reset ({datetime.fromtimestamp(since / 1000):%Y-%m-%d %H:%M}); ALL=1 shows every session.\n")
        if os.environ.get("ALL"):
            since = 0
        for c in FOLDERS:
            folder = str(WORKSPACE / c)
            tasks = con.execute("select id, substr(title, 1, 40), created_at from tasks where project_id in (?, ?) order by created_at",
                                (folder, "file:" + folder)).fetchall()
            for tid, title, created in tasks:
                if (created if created > 1e12 else created * 1000) < since:
                    continue
                outside, forbidden = set(), set()
                for (data,) in con.execute("select data from messages where task_id = ? and role = 'assistant'", (tid,)):
                    for path in re.findall(r"(/(?:Users|home|private|tmp|var)/[^\s\"'\\,)]+|\.\./[^\s\"'\\,)]+)", data):
                        tools = ("/.sdkman/", "/.m2/", "/homebrew/", "/usr/", "/opt/", "/Library/Java/")
                        if path.startswith(folder) or any(t in path for t in tools) or path.rstrip("/") == str(Path.home()):
                            continue
                        outside.add(path[:80])
                    for cmd in re.findall(r'"command":\s*"([^"]{0,200})', data):
                        if re.search(r"\b(git|curl|wget|env|printenv)\b", cmd):
                            forbidden.add(cmd[:80])
                date = datetime.fromtimestamp(created / 1000 if created > 1e12 else created).strftime("%Y-%m-%d %H:%M")
                ok = not outside and not forbidden
                invalid += not ok
                print(f"{'OK     ' if ok else 'INVALID'} {c:12} {date}  {title.splitlines()[0] if title else ''}")
                for x in sorted(outside)[:5]:
                    print(f"           outside the folder: {x}")
                for x in sorted(forbidden)[:5]:
                    print(f"           forbidden command: {x}")
        print("\nEvery session respected the isolation." if not invalid else f"\n{invalid} invalid sessions: repeat those phases.")
    exposed = [e for e in (git_exposure(WORKSPACE / c) for c in FOLDERS if (WORKSPACE / c).is_dir()) if e]
    exposed += [f"snapshot {p.parent.parent.name} {p.parent.name}: {json.loads(p.read_text())['git_exposure']}"
                for p in sorted(RESULTS.glob("*/F[1-3]/.snapshot.json")) if "git_exposure" in json.loads(p.read_text())]
    print("No experiment folder can use git." if not exposed else "\nGit was available to Bob:\n  " + "\n  ".join(exposed))


# ---------------------------------------------------------------- Bob sessions (bob.db)
EDIT_TOOLS = {"write_file", "apply_diff", "search_and_replace", "insert_content"}


def _bob_db():
    import sqlite3
    db = Path.home() / ".bob" / "db" / "bob.db"
    if not db.exists():
        return None
    tmp = Path(tempfile.mkdtemp(prefix="bobdb-"))
    for f in db.parent.glob("bob.db*"):
        shutil.copy(f, tmp)
    return sqlite3.connect(tmp / "bob.db")


def _ms(path):
    try:
        d = json.loads(path.read_text())
    except (OSError, ValueError):
        return None
    return d.get("taken_ms") or d.get("reset_ms")


def bob_session(con, c, phase):
    """Cost, context, duration and reads of Bob's sessions in folder c during the phase:
    the sessions created between the previous snapshot (or the reset) and this phase's snapshot."""
    if con is None:
        return {}
    end = _ms(RESULTS / c / phase / ".snapshot.json")
    if not end:
        return {}
    n = int(phase[1])
    start = (_ms(RESULTS / c / f"F{n - 1}" / ".snapshot.json") if n > 1 else _ms(RESULTS / "reset.json")) or 0
    folder = str(WORKSPACE / c)
    tasks = con.execute("select id from tasks where project_id in (?, ?) and created_at > ? and created_at < ? order by created_at",
                        (folder, "file:" + folder, start, end)).fetchall()
    if not tasks:
        return {}

    def kind(path):
        rel = path.replace(folder + "/", "")
        if rel.startswith("docs/") or rel in ("AGENTS.md", "REQUIREMENTS.md"):
            return "docs"
        if rel.startswith("src/main/"):
            return "code"
        if rel.startswith("src/test/"):
            return "tests"
        return "other"

    cost, ctx, times, calls, builds = 0.0, 0, [], 0, 0
    reads = defaultdict(int)
    before = defaultdict(int)
    edited, first, doc_edits, code_edits, docs_first = False, None, 0, 0, None
    # Autonomy, measured from what happened and not from what Bob declares in its history.
    auto = {"human_messages": 0, "questions_to_user": 0, "denied_commands": 0, "approvals_rejected": 0, "session_errors": 0}
    for (tid,) in tasks:
        users, last = 0, None
        for (data,) in con.execute("select data from messages where task_id = ? order by created_at", (tid,)):
            try:
                m = json.loads(data)
            except ValueError:
                continue
            meta = m.get("_meta") or {}
            if meta.get("timestamp"):
                times.append(meta["timestamp"])
            role = m.get("role")
            if role == "user":
                users += 1
                # The first message is the prompt; in series 1 the cost line was pasted by hand at the end (protocol, not help).
                if users > 1 and not re.search(r"track (the )?(token|consum)", _text(m.get("content")), re.I):
                    auto["human_messages"] += 1
            elif role == "tool":
                out = _text(m.get("content"))
                if "deny list" in out:
                    auto["denied_commands"] += 1
                elif re.search(r"was cancelled|denied by the user|rejected by the user|user rejected", out, re.I):
                    auto["approvals_rejected"] += 1
            if role != "assistant":
                continue
            last = m
            auto["questions_to_user"] += sum(1 for x in m.get("toolCalls") or [] if re.search(r"ask|question|followup", x.get("name") or "", re.I))
            sp = meta.get("spend") or {}
            cost += sp.get("cost", 0) or 0
            ctx = max(ctx, sp.get("contextTokens", 0) or 0)
            calls += 1
            for t in m.get("toolCalls") or []:
                name, args = t.get("name"), t.get("arguments") or {}
                path = str(args.get("path", ""))
                bob_history = "/history/" in path
                if name == "read_file":
                    k = kind(path)
                    reads[k] += 1
                    if not edited:
                        before[k] += 1
                elif name in EDIT_TOOLS and not bob_history:
                    k = kind(path)
                    doc_edits += k == "docs"
                    code_edits += k in ("code", "tests")
                    if not edited:
                        edited, first = True, k
                    if docs_first is None and k in ("docs", "code"):
                        docs_first = k == "docs"
                elif name == "execute_command" and re.search(r"\bmvn\b", str(args.get("command", ""))):
                    builds += 1
        # A session that ends on a question, with no tool call, stopped to ask instead of finishing.
        if last and not last.get("toolCalls") and _text(last.get("content")).rstrip().endswith("?"):
            auto["questions_to_user"] += 1
        status = con.execute("select status, last_error from tasks where id = ?", (tid,)).fetchone()
        auto["session_errors"] += bool(status and (status[0] == "error" or (status[1] not in (None, "", "null"))))
    auto["interventions"] = auto["human_messages"] + auto["approvals_rejected"]
    return {"bob_sessions": len(tasks), "cost_bob": round(cost, 2), "context_max_k": round(ctx / 1000, 1),
            "bob_duration_s": int((max(times) - min(times)) / 1000) if times else None,
            "model_calls": calls, "mvn_builds": builds,
            "reads_docs": reads["docs"], "reads_code": reads["code"], "reads_tests": reads["tests"],
            "docs_read_before_edit": before["docs"], "code_read_before_edit": before["code"] + before["tests"],
            "first_edit": first, "doc_edits": doc_edits, "code_edits": code_edits,
            "docs_before_code": docs_first, **auto}


def _text(content):
    """The text of a message whose content is a string or a list of parts."""
    if isinstance(content, str):
        return content
    if isinstance(content, list):
        return "\n".join(p.get("text", "") if isinstance(p, dict) else str(p) for p in content)
    return ""


# ---------------------------------------------------------------- Bob's tests against the hidden games
def test_accuracy(r):
    """Bob writes its own tests, so they are judged against the hidden games (the oracle): the log from series 2,
    the console logic before. The suite is right when it is green and every hidden game is correct, or when it
    is red and some game fails. Escapes: hidden games that fail while the suite is green. False alarms: failing
    tests when every hidden game is correct. Recall against planted bugs is the mutation score."""
    tests, failed = r.get("tests"), r.get("tests_failed")
    oracle = r.get("acceptance_log") or r.get("acceptance_logic")
    out = {"tests_passed_pct": round(100 * (tests - failed) / tests, 1) if tests else None}
    m = re.fullmatch(r"(\d+)/(\d+)", oracle or "")
    if not tests or not m:
        return out
    ok, total = int(m.group(1)), int(m.group(2))
    green, hidden_failed = failed == 0, total - ok
    out.update({
        "tests_oracle": "log" if r.get("acceptance_log") else "console logic",
        "suite_verdict": ("right: green and every hidden game correct" if green and not hidden_failed else
                          "false confidence: green but hidden games fail" if green else
                          "right: red and hidden games fail" if hidden_failed else
                          "false alarm: red but every hidden game correct"),
        "suite_right": green == (hidden_failed == 0),
        "test_escapes": hidden_failed if green else None,
        "false_alarms": failed if not hidden_failed else None,
    })
    return out


# ---------------------------------------------------------------- reset
def reset():
    if has_snapshots(RESULTS):
        sys.exit(f"{_rel(RESULTS)} already holds the snapshots of a run: save it first with\n"
                 f"  python3 measure.py archive \"what with-ddd received\"")
    for c in FOLDERS:
        src, dst = ROOT / "src" / c, WORKSPACE / c
        if not src.is_dir():
            sys.exit(f"{src} does not exist")
        if dst.exists():
            shutil.rmtree(dst)
        shutil.copytree(src, dst, ignore=shutil.ignore_patterns(".git"))
        exposed = git_exposure(dst)
        if exposed:
            shutil.rmtree(dst)
            sys.exit(f"{exposed}. The experiment folders must live outside any git repository "
                     f"(this repository is the control plane): move {WORKSPACE} out of that repository.")
        print(f"{c}: restored from src/{c} (no git)")
    RESULTS.mkdir(exist_ok=True)
    (RESULTS / "reset.json").write_text(json.dumps({"reset_ms": _now_ms(), "series": SERIES}))


# ---------------------------------------------------------------- Bob's history
def history(snap, prev):
    # This phase's history is the session-*.md files that were not, identical, in the previous snapshot.
    hs = sorted((snap / "history").glob("session-*.md"), key=lambda p: int(re.sub(r"\D", "", p.stem) or 0))
    old = {p.read_text(encoding="utf-8") for p in (prev / "history").glob("session-*.md")} if prev and (prev / "history").exists() else set()
    hs = [h for h in hs if h.read_text(encoding="utf-8") not in old]
    if not hs:
        return {"history": "missing"}
    t = "\n".join(h.read_text(encoding="utf-8") for h in hs)
    r = {"history": ", ".join(f"history/{h.name}" for h in hs)}
    start = re.search(r"Start:\s*(\S+)", t)
    end = re.search(r"End:\s*(\S+)", t)
    if start and end:
        from datetime import datetime
        a = datetime.fromisoformat(start.group(1).replace("Z", "+00:00"))
        b = datetime.fromisoformat(end.group(1).replace("Z", "+00:00"))
        r["duration_s"] = int((b - a).total_seconds())
    # Line pasted by Bob's IDE: "33.5k / 270.0k (12%) · 1.03 🅞"
    m = re.search(r"([\d.]+)k\s*/\s*([\d.]+)k\s*\((\d+)%\)\s*·\s*([\d.]+)", t)
    if m:
        r["context_k"] = float(m.group(1))
        r["window_k"] = float(m.group(2))
        r["context_pct"] = int(m.group(3))
        r["cost_bobcoins"] = float(m.group(4))
    dec = re.findall(r"^\|\s*A\d+\s*\|\s*`?(\w+)`?\s*\|\s*([^|]*)\|", t, re.M)
    r["decisions"] = len(dec)
    for typ in TYPES:
        r[f"dec_{typ}"] = sum(1 for d in dec if d[0].lower() == typ)
    r["questions"] = [q.strip() for _, q in dec]
    r["questions_by_type"] = [(typ.lower(), q.strip()) for typ, q in dec]
    r["pending"] = len(re.findall(r"^\|\s*P\d+\s*\|", t, re.M))
    res = re.search(r"## Result\s*\n+(.+)", t)
    r["result"] = res.group(1).strip()[:80] if res else "?"
    status = re.match(r"`?(done|partially done|partially|blocked)", r["result"], re.I)
    r["result_status"] = status.group(1).lower().replace("partially done", "partially") if status else "?"
    docs = re.search(r"## Documentation I relied on\s*\n(.*?)(\n## |\Z)", t, re.S)
    r["docs_used"] = len(re.findall(r"^- ", docs.group(1), re.M)) if docs else 0
    return r


# ---------------------------------------------------------------- build, tests, acceptance, mutants
def build(snap, tmp):
    work = Path(tmp) / "w"
    shutil.copytree(snap, work)
    code, out = sh("mvn -q verify", cwd=work)
    r = {"verify": "ok" if code == 0 else "FAIL"}
    tests = fails = 0
    for x in (work / "target" / "surefire-reports").glob("*.txt"):
        m = re.search(r"Tests run: (\d+), Failures: (\d+), Errors: (\d+)", x.read_text())
        if m:
            tests += int(m.group(1)); fails += int(m.group(2)) + int(m.group(3))
    r["tests"], r["tests_failed"] = tests, fails
    if not (work / "target" / "tictactoe.jar").exists():
        sh("mvn -q -DskipTests package", cwd=work)
    return work, r


RESULT_STATE = {"X wins": "X_WINS", "O wins": "O_WINS", "Draw": "DRAW"}
NO_LOG = "did not write the log"


def _text_board(rows):
    """['XX.', 'O..', '...'] -> {(1, 1): 'X', ...}; None unless they are three rows of three squares."""
    if len(rows) != 3 or not all(re.fullmatch(r"[XO.]{3}", f) for f in rows):
        return None
    return {(i + 1, j + 1): ch for i, f in enumerate(rows) for j, ch in enumerate(f) if ch != "."}


def read_log(path):
    """The lines of the JSONL log as [(board, state)], or (None, reason) when it breaks the contract."""
    if not path.exists():
        return None, NO_LOG
    steps = []
    for i, l in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        if not l.strip():
            continue
        try:
            o = json.loads(l)
            board = {}
            for sq in o["board"]:
                k = (sq["row"], sq["column"])
                if not (sq["row"] in (1, 2, 3) and sq["column"] in (1, 2, 3) and sq["color"] in ("X", "O")) or k in board \
                        or type(sq["row"]) is not int or type(sq["column"]) is not int:
                    return None, f"line {i}: invalid or repeated square"
                board[k] = sq["color"]
            if o["state"] not in ("IN_PROGRESS", "X_WINS", "O_WINS", "DRAW"):
                return None, f"line {i}: unknown state {o['state']!r}"
            steps.append((board, o["state"]))
        except (ValueError, KeyError, TypeError):
            return None, f"line {i}: does not follow the required JSON format"
    return (steps, None) if steps else (None, "empty log")


def check_moves(steps, three_pieces):
    """Checks the game move by move: it starts empty, each line is a valid put or move by the player whose turn
    it is (X starts and they alternate) and only the last line ends the game. Returns the reason it fails, or None."""
    # Repeated consecutive lines are tolerated (writing the same board twice does not change the game).
    steps = [p for i, p in enumerate(steps) if i == 0 or p != steps[i - 1]]
    if steps[0][0]:
        return "the first line is not the empty board"
    turn, prev = "X", {}
    for n, (board, state) in enumerate(steps[1:], 2):
        added = {k: v for k, v in board.items() if k not in prev}
        removed = {k: v for k, v in prev.items() if k not in board}
        if any(prev[k] != board[k] for k in board if k in prev):
            return f"line {n}: a piece changed color"
        if len(added) == 1 and not removed:
            color = next(iter(added.values()))
            if three_pieces and sum(1 for v in board.values() if v == color) > 3:
                return f"line {n}: {color} has more than three pieces"
        elif len(added) == 1 and len(removed) == 1 and set(added.values()) == set(removed.values()):
            color = next(iter(added.values()))
            if not three_pieces:
                return f"line {n}: a piece moved in a variant without moves"
            if sum(1 for v in prev.values() if v == color) < 3:
                return f"line {n}: {color} moved before putting its three pieces"
        else:
            return f"line {n}: neither a put nor a move ({len(added)} new squares, {len(removed)} emptied)"
        if color != turn:
            return f"line {n}: {color} played but it was {turn}'s turn"
        if state != "IN_PROGRESS" and n != len(steps):
            return f"line {n}: the game went on after {state}"
        turn, prev = ("O" if turn == "X" else "X"), board
    return None


def acceptance(jar, phase):
    """Plays each hidden game once and checks it twice: the console (the text contract, what the user sees) and
    the JSONL log in TICTACTOE_BOARD_LOG (the state of the game, move by move)."""
    n = int(phase[1])
    by_phase, failures, logic_ok = defaultdict(lambda: [0, 0]), [], [0]
    js = {"ok": 0, "missing": 0, "consistent": 0, "comparable": 0, "failures": [], "moves_ok": 0}
    for line in SCENARIOS.read_text().splitlines():
        if line.startswith("#") or not line.strip():
            continue
        sid, ph, args, inp, board, res, ninv, _ = line.split("\t")
        if int(ph) > n:
            continue
        cmd = ["java", "-jar", str(jar)] + ([] if args == "-" else args.split())
        with tempfile.TemporaryDirectory() as tmp:
            log = Path(tmp) / "board.jsonl"
            try:
                p = subprocess.run(cmd, capture_output=True, text=True, timeout=20, input="\n".join(inp.split("|")) + "\n",
                                   env=dict(os.environ, TICTACTOE_BOARD_LOG=str(log)))
                code, out = p.returncode, p.stdout + p.stderr
            except subprocess.TimeoutExpired:
                code, out = -1, ""
            steps, reason = read_log(log)
        lines = out.rstrip("\n").split("\n")
        rows = board.split("/")
        ok = lines[-4:] == rows + [res] and sum(l.startswith("Invalid") for l in lines) == int(ninv) and code == 0
        # Correct logic even if the format is not: tolerates a prompt without a line break glued to the first
        # row of the board or to an Invalid line (it happens when the input comes through a pipe).
        logic = (len(lines) >= 4 and lines[-3:] == rows[1:] + [res] and lines[-4].endswith(rows[0])
                 and sum(bool(re.search(r"(^|: )Invalid", l)) for l in lines) == int(ninv) and code == 0)
        by_phase[f"F{ph}"][1] += 1
        by_phase[f"F{ph}"][0] += ok
        logic_ok[0] += logic
        if not ok:
            failures.append(sid)
        # JSONL log: final board and state, and the game move by move.
        if steps is None:
            js["missing"] += reason == NO_LOG
            js["failures"].append(f"{sid}: {reason}")
            continue
        expected = _text_board(rows)
        error = check_moves(steps, "--three-pieces" in args)
        js["moves_ok"] += error is None
        final, state = steps[-1]
        if final != expected:
            error = error or "the final board is not the expected one"
        elif state != RESULT_STATE.get(res):
            error = error or f"final state {state}, expected {RESULT_STATE.get(res)}"
        if error:
            js["failures"].append(f"{sid}: {error}")
        else:
            js["ok"] += 1
        # Consistency: the board the console shows is the same as the log's.
        text = _text_board([l[-3:] for l in lines[-4:-1]]) if len(lines) >= 4 else None
        if text is not None:
            js["comparable"] += 1
            js["consistent"] += text == final
    total = [sum(v[0] for v in by_phase.values()), sum(v[1] for v in by_phase.values())]
    r = {"acceptance": f"{total[0]}/{total[1]}", "acceptance_failures": failures,
         "acceptance_logic": f"{logic_ok[0]}/{total[1]}",
         "acceptance_log": f"{js['ok']}/{total[1]}", "acceptance_log_failures": js["failures"],
         "valid_moves": f"{js['moves_ok']}/{total[1]}", "log_missing": js["missing"],
         "console_matches_log": f"{js['consistent']}/{js['comparable']}" if js["comparable"] else None}
    for k, v in sorted(by_phase.items()):
        r[f"acceptance_{k}"] = f"{v[0]}/{v[1]}"
    return r


def mutants(work, io_classes):
    code, out = sh("mvn -q org.pitest:pitest-maven:mutationCoverage", cwd=work, timeout=1800)
    xml = next((work / "target" / "pit-reports").rglob("mutations.xml"), None) if (work / "target" / "pit-reports").exists() else None
    if not xml:
        return {"mutation_score": "error"}
    import xml.etree.ElementTree as ET
    tot = defaultdict(lambda: [0, 0])
    for m in ET.parse(xml).getroot():
        cls = m.find("mutatedClass").text.split("$")[0].replace("tictactoe.", "")
        tot[cls][1] += 1
        tot[cls][0] += m.get("status") == "KILLED"
    k, g = sum(v[0] for v in tot.values()), sum(v[1] for v in tot.values())
    nk = sum(v[0] for c, v in tot.items() if c not in io_classes)
    ng = sum(v[1] for c, v in tot.items() if c not in io_classes)
    return {"mutants": f"{k}/{g}", "mutation_score": round(100 * k / g) if g else None,
            "mutation_score_no_io": round(100 * nk / ng) if ng else None, "mutants_no_io": f"{nk}/{ng}",
            "mutants_by_class": {c: f"{v[0]}/{v[1]}" for c, v in sorted(tot.items())}}


# ---------------------------------------------------------------- design: coupling and cohesion
# All the coupling (Ce, Ca, propagation cost, Yourdon & Constantine), cohesion (LCOM*, LCOM4, TCC, LCC),
# encapsulation and file and method size calculations live in design_metrics.py.
from design_metrics import design, size_metrics, YC_TYPES, MAX_LINES, MAX_PARAMS  # noqa: E402


# ---------------------------------------------------------------- size, vocabulary, Open/Closed
def sources(snap, sub):
    base = snap / "src" / sub / "java"
    return {str(p.relative_to(base)): p.read_text(encoding="utf-8") for p in base.rglob("*.java")} if base.exists() else {}


def without_comments(code):
    code = re.sub(r"/\*.*?\*/", " ", code, flags=re.S)
    code = re.sub(r"//[^\n]*", " ", code)
    return re.sub(r'"(?:\\.|[^"\\])*"', '""', code)


def words(ident):
    return [p.lower() for p in re.findall(r"[A-Z]+(?=[A-Z][a-z])|[A-Z]?[a-z]+|[A-Z]+|\d+", ident)]


def glossary(phase):
    discarded, model = set(), set()
    g = RESULTS / "with-ddd" / phase / "docs" / "domain" / "glossary.md"
    if not g.exists():
        g = ROOT / "src" / "with-ddd" / "docs" / "domain" / "glossary.md"
    for line in (g if g.exists() else GLOSSARY).read_text(encoding="utf-8").splitlines():
        cols = [c.strip() for c in line.strip().strip("|").split("|")]
        if len(cols) != 4 or cols[0] in ("Term", "---"):
            continue
        # Model terms: the Term column ("Row, Column" are two). Reserved terms (no name in code) are skipped,
        # and so are terms that do not appear under their own name in code (e.g. Reading order -> Coordinate.all()).
        for t in cols[0].split(","):
            t = t.strip()
            ws = t.lower().split()
            if not t or cols[2].strip() in ("—", "-") or not all(w in cols[2].lower() for w in ws):
                continue
            model.add("".join(w[:1].upper() + w[1:] for w in t.split()))
        for t in cols[3].split(","):
            t = t.strip().strip("`")
            if re.fullmatch(r"[A-Za-z]+", t):
                discarded.add(t)
    return sorted(discarded), sorted(model)


def vocabulary(main, phase):
    discarded, model = glossary(phase)
    if phase == "F3":  # with CR-2, Move is a valid domain term
        discarded = [t for t in discarded if t.lower() != "move"]
    elif "move" not in {t.lower() for t in discarded}:
        discarded.append("Move")  # reserved until CR-2
    discarded = list({t.lower(): t for t in discarded}.values())
    idents = set()
    for code in main.values():
        idents |= set(re.findall(r"\b[A-Za-z_]\w*\b", without_comments(code)))
    # Java API names that contain a discarded term but are not domain names.
    JDK_API = {"allMatch", "anyMatch", "noneMatch", "matches", "Matcher", "replaceAll", "indexOf", "lastIndexOf",
               "charAt", "Collectors", "toList", "Status"}
    idents -= JDK_API - {"Status"}
    found = defaultdict(set)
    for ident in idents:
        p = words(ident)
        for t in discarded:
            tp = words(t)
            if any(p[i:i + len(tp)] == tp for i in range(len(p))):
                found[t].add(ident)
    def contains(t, i):
        tp, ip = words(t), words(i)
        return any(ip[k:k + len(tp)] == tp for k in range(len(ip)))
    present = [t for t in model if any(contains(t, i) for i in idents)]
    return {"discarded_terms": len(found),
            "discarded_in_code": {k: sorted(v)[:6] for k, v in sorted(found.items())},
            "model_terms": f"{len(present)}/{len(model)}",
            "model_terms_missing": sorted(set(model) - set(present))}


def open_closed(prev, cur):
    pm, cm = sources(prev, "main"), sources(cur, "main")
    pt, ct = sources(prev, "test"), sources(cur, "test")
    new = sorted(set(cm) - set(pm))
    deleted = sorted(set(pm) - set(cm))
    modified = sorted(f for f in set(cm) & set(pm) if cm[f] != pm[f])
    churn = 0
    for f in modified:
        for l in difflib.unified_diff(pm[f].splitlines(), cm[f].splitlines(), lineterm="", n=0):
            if (l.startswith("+") or l.startswith("-")) and not l.startswith(("+++", "---")):
                churn += 1
    tests_touched = sorted(f for f in set(ct) & set(pt) if ct[f] != pt[f]) + sorted(set(pt) - set(ct))
    ratio = round(len(new) / (len(new) + len(modified)), 2) if new or modified else None
    new_loc = loc({f: cm[f] for f in new})
    return {"new_classes": len(new), "modified_classes": len(modified), "deleted_classes": len(deleted),
            "ocp_ratio": ratio, "lines_changed_in_existing": churn, "lines_in_new_classes": new_loc,
            "lines_touched": churn + new_loc,
            "change_in_existing_pct": round(100 * churn / (churn + new_loc)) if churn + new_loc else None,
            "existing_tests_touched": len(tests_touched),
            "new_detail": new, "modified_detail": modified, "touched_tests_detail": tests_touched}


def docs_changed(prev, cur):
    r = {}
    # REQUIREMENTS.md is the same in both folders and must not change: if Bob edits it, it is reported.
    initial = ROOT / "src" / cur.parent.name / "REQUIREMENTS.md"
    if initial.exists() and (cur / "REQUIREMENTS.md").exists():
        r["requirements_modified"] = initial.read_bytes() != (cur / "REQUIREMENTS.md").read_bytes()
    d1, d2 = prev / "docs" / "domain", cur / "docs" / "domain"
    if d2.exists():
        r["docs_domain_updated"] = sorted(p.name for p in d2.glob("*") if not (d1 / p.name).exists() or (d1 / p.name).read_bytes() != p.read_bytes())
    return r


def loc(files):
    return sum(1 for code in files.values() for l in code.splitlines() if l.strip() and not l.strip().startswith(("//", "*", "/*", "import ", "package ")))


def efficiency(r):
    """Correct hidden games against cost and time, and cost of the change per line touched (scalability)."""
    out = {}
    try:
        ok, total = map(int, r["acceptance"].split("/"))
    except (KeyError, ValueError, AttributeError):
        return out
    out["games_correct"], out["games_total"] = ok, total
    out["acceptance_pct"] = round(100 * ok / total, 1) if total else None
    cost, sec = r.get("cost_bobcoins"), r.get("duration_s")
    if ok and cost:
        out["cost_per_game"] = round(cost / ok, 3)
    if ok and sec:
        out["seconds_per_game"] = round(sec / ok, 1)
    if cost:
        out["games_per_bobcoin"] = round(ok / cost, 2)
    try:  # a folder can fail only because of the format: the correct logic is compared too
        logic = int(r["acceptance_logic"].split("/")[0])
    except (KeyError, ValueError, AttributeError):
        logic = 0
    if logic and cost:
        out["cost_per_game_logic"] = round(cost / logic, 3)
    if logic and sec:
        out["seconds_per_game_logic"] = round(sec / logic, 1)
    try:  # series 2: correct games according to the JSONL log, move by move
        js = int(r["acceptance_log"].split("/")[0])
        if js and cost:
            out["cost_per_game_log"] = round(cost / js, 3)
        if js and sec:
            out["seconds_per_game_log"] = round(sec / js, 1)
    except (KeyError, ValueError, AttributeError):
        pass
    if cost and r.get("lines_touched"):
        out["cost_per_100_lines_touched"] = round(100 * cost / r["lines_touched"], 2)
    return out


# ---------------------------------------------------------------- report
ROWS = [
    ("Result (according to Bob)", "result"), ("Duration (s, Timing in the history)", "duration_s"),
    ("Context at the end (k tokens)", "context_k"), ("Context used (% of the window)", "context_pct"),
    ("Cost (Bobcoins)", "cost_bobcoins"), ("Source of the cost", "cost_source"), ("Bob sessions in the phase", "bob_sessions"),
    ("Model calls", "model_calls"), ("Builds with mvn", "mvn_builds"),
    ("Reads: docs · code · tests", "reads"),
    ("Before the first edit: docs read · code read", "before_edit"),
    ("First edit in", "first_edit"), ("Docs edited before the code?", "docs_before_code"),
    ("mvn -q verify", "verify"), ("Unit tests", "tests"),
    ("Experiment series", "series"),
    ("Correct games according to the JSONL log (board, state and moves)", "acceptance_log"),
    ("Log: games with every move valid", "valid_moves"),
    ("Log: games without a log", "log_missing"), ("Log: failures", "acceptance_log_failures"),
    ("Console: the board shown matches the log", "console_matches_log"),
    ("Console: games with the exact output (hidden scenarios)", "acceptance"), ("Console: correct logic even if the format fails", "acceptance_logic"),
    ("Acceptance F1 (regressions)", "acceptance_F1"),
    ("Acceptance F2", "acceptance_F2"), ("Acceptance F3", "acceptance_F3"), ("Failed scenarios", "acceptance_failures"),
    ("Bob's tests that pass (%)", "tests_passed_pct"),
    ("Bob's tests against the hidden games: verdict of the suite", "suite_verdict"),
    ("Bob's tests: escapes (hidden games that fail with the suite green)", "test_escapes"),
    ("Bob's tests: false alarms (failing tests with every hidden game correct)", "false_alarms"),
    ("Total mutation score (%)", "mutation_score"), ("Mutants killed/generated", "mutants"),
    ("Mutation score without the console (%)", "mutation_score_no_io"), ("Mutants without the console", "mutants_no_io"),
    ("Production classes", "classes"), ("Packages", "packages"), ("Production lines", "loc_main"),
    ("Test lines", "loc_test"),
    ("Production files", "files"), ("Lines per file (median · max)", "file_lines"),
    ("Files with more than 200 lines", "files_>200_lines"),
    ("Methods and constructors", "methods_total"), ("Lines per method (median · max)", "method_lines"),
    ("Methods of 30 lines or fewer (%)", "methods_up_to_30_lines_pct"),
    ("Methods with more than 30 lines", "long_methods"),
    ("Methods with more than 3 parameters", "many_params_methods"),
    ("Efferent coupling Ce (median · max)", "ce"), ("Afferent coupling Ca (max)", "Ca_max"),
    ("Propagation cost (% of the system a change reaches)", "propagation_cost"),
    ("Yourdon & Constantine: mean index per class pair (1 data … 6 content)", "yc_index"),
    ("Yourdon & Constantine: class pairs by worst connection", "yc_pairs_txt"),
    ("Yourdon & Constantine: worst connection", "yc_worst"),
    ("Yourdon & Constantine: control, external, common or content connections", "yc_detail"),
    ("Yourdon & Constantine: control inside a class", "yc_internal_control"),
    ("Cohesion LCOM* median, without getters (lower = better)", "LCOM*_median"),
    ("Cohesion TCC median (higher = better)", "TCC_median"), ("Cohesion LCC median (higher = better)", "LCC_median"),
    ("Low-cohesion classes (TCC < 0.5)", "classes_TCC<0.5"),
    ("Classes with splittable fields (LCOM4 > 1)", "classes_LCOM4>1"), ("Classes with console I/O", "classes_with_IO"),
    ("Encapsulation: private instance fields", "private_fields"), ("Encapsulation: public methods (%)", "public_methods_pct"),
    ("Encapsulation: public methods that change state", "public_mutators"),
    ("Encapsulation: exposed mutable state (a getter returns an array or a modifiable object)", "state_leaks"),
    ("Cycles between packages", "package_cycles"), ("New / modified / deleted classes", "ocp"),
    ("Open/Closed (new ÷ (new + modified))", "ocp_ratio"), ("Lines changed in existing classes", "lines_changed_in_existing"),
    ("Lines touched (changed + in new classes)", "lines_touched"),
    ("Share of the change that fell on existing classes (%)", "change_in_existing_pct"),
    ("Efficiency: Bobcoins per correct hidden game", "cost_per_game"),
    ("Efficiency: seconds per correct hidden game", "seconds_per_game"),
    ("Efficiency: Bobcoins per game with the correct logic", "cost_per_game_logic"),
    ("Efficiency: seconds per game with the correct logic", "seconds_per_game_logic"),
    ("Efficiency: Bobcoins per correct game according to the log", "cost_per_game_log"),
    ("Efficiency: seconds per correct game according to the log", "seconds_per_game_log"),
    ("Scalability: Bobcoins per 100 lines touched", "cost_per_100_lines_touched"),
    ("Existing tests touched", "existing_tests_touched"), ("docs/domain updated", "docs_domain_updated"),
    ("Did Bob modify REQUIREMENTS.md?", "requirements_modified"), ("Snapshot rebuilt from bob.db", "snapshot_rebuilt"),
    ("Own decisions (total)", "decisions"), *[(f"  · {t}", f"dec_{t}") for t in TYPES],
    ("Pending items", "pending"), ("Result Bob declared", "result_status"),
    ("Autonomy: human messages after the prompt", "human_messages"),
    ("Autonomy: questions to the user", "questions_to_user"),
    ("Autonomy: approvals rejected by a human", "approvals_rejected"),
    ("Autonomy: denied commands it tried", "denied_commands"),
    ("Autonomy: sessions that ended in an error", "session_errors"),
    ("Documents it relied on", "docs_used"),
    ("Discarded terms in the code", "discarded_terms"), ("Glossary terms present", "model_terms"),
]


def fmt(v):
    if v is None or v == "":
        return "—"
    if isinstance(v, list):
        return ", ".join(map(str, v)) if v else "none"
    return str(v)


def current_series():
    """The series of the run being measured: the one reset recorded; runs before the JSONL log are series 1."""
    try:
        return json.loads((RESULTS / "reset.json").read_text()).get("series", 1)
    except (OSError, ValueError):
        return 1


def measure():
    data = {}
    con = _bob_db()
    series = current_series()
    for c in FOLDERS:
        snaps = sorted(p.name for p in (RESULTS / c).glob("F[1-3]") if p.is_dir())
        for phase in snaps:
            snap = RESULTS / c / phase
            print(f"measuring {c} {phase}…", flush=True)
            with tempfile.TemporaryDirectory() as tmp:
                work, r = build(snap, tmp)
                r.update(acceptance(work / "target" / "tictactoe.jar", phase))
                if series < 2:  # before series 2 the JSONL log was not in the requirements
                    for k in ("acceptance_log", "acceptance_log_failures", "valid_moves", "log_missing", "console_matches_log"):
                        r.pop(k, None)
                r.update(design(work / "target" / "classes"))
                r.update(mutants(work, r["classes_with_IO"]))
            main, test = sources(snap, "main"), sources(snap, "test")
            r["loc_main"], r["loc_test"] = loc(main), loc(test)
            r.update(size_metrics(main))
            r["file_lines"] = f"{r['file_lines_median']} · {r['file_lines_max']}"
            r["method_lines"] = f"{r['method_lines_median']} · {r['method_lines_max']}"
            r["yc_pairs_txt"] = " · ".join(f"{r['yc_pairs'][t]} {t}" for t in YC_TYPES if r["yc_pairs"].get(t))
            r["ce"] = f"{r['Ce_median']} · {r['Ce_max']}"
            prev = RESULTS / c / f"F{int(phase[1]) - 1}"
            r.update(history(snap, prev if prev.exists() else None))
            r.update(vocabulary(main, phase))
            try:
                r["snapshot_rebuilt"] = bool(json.loads((snap / ".snapshot.json").read_text()).get("rebuilt"))
            except (OSError, ValueError):
                pass
            bob = bob_session(con, c, phase)
            r.update(bob)
            if bob:  # bob.db is more complete than the line pasted by hand and than the Timing Bob writes
                r["cost_bobcoins"], r["context_k"] = bob["cost_bob"], bob["context_max_k"]
                r["duration_s"] = bob["bob_duration_s"] or r.get("duration_s")
                r["cost_source"] = "bob.db"
                r["reads"] = f"{bob['reads_docs']} · {bob['reads_code']} · {bob['reads_tests']}"
                r["before_edit"] = f"{bob['docs_read_before_edit']} · {bob['code_read_before_edit']}"
            else:
                r["cost_source"] = "history"
            if prev.exists():
                r.update(open_closed(prev, snap))
                r["ocp"] = f"{r['new_classes']} / {r['modified_classes']} / {r['deleted_classes']}"
                r.update(docs_changed(prev, snap))
            r.update(efficiency(r))
            r.update(test_accuracy(r))
            r["series"] = series
            data.setdefault(c, {})[phase] = r
    if not data:
        sys.exit("no snapshots: run  python3 measure.py snapshot F1  first")
    (RESULTS / "metrics.json").write_text(json.dumps(data, indent=2, ensure_ascii=False, default=str))

    md = ["# Measured metrics", "",
          "Generated by `python3 measure.py`. Each phase is the snapshot saved with `measure.py snapshot F<n>`, built in a temporary folder.",
          "Cost, context, duration and reads come from the Bob Shell database (`~/.bob/db/bob.db`) when it is available; otherwise, from the line pasted at the end of the history. Decisions and pending items come from the history Bob writes.", ""]
    phases = sorted({f for c in data for f in data[c]})
    for phase in phases:
        md += [f"## {phase}", "", "| Metric | without-ddd | with-ddd |", "|---|---|---|"]
        for label, key in ROWS:
            vals = [fmt(data.get(c, {}).get(phase, {}).get(key)) for c in FOLDERS]
            if all(v == "—" for v in vals):
                continue
            md.append(f"| {label} | {vals[0]} | {vals[1]} |")
        md += ["", "### Coupling and cohesion by class", ""]
        for c in FOLDERS:
            r = data.get(c, {}).get(phase)
            if not r:
                continue
            md += [f"**{c}** — dependencies between packages: {fmt(r['package_dependencies'] or 'none')}", "",
                   "| Class | File lines | Ce (uses) | Ca (used by) | Instability Ce/(Ca+Ce) | Classes a change reaches | Methods | Fields | LCOM* | LCOM4 | TCC | LCC |",
                   "|---|---|---|---|---|---|---|---|---|---|---|---|"]
            lbf = {k.replace("/", "."): v for k, v in (r.get("lines_by_file") or {}).items()}
            md += [f"| {t['class']} | {fmt(lbf.get('tictactoe.' + t['class']))} | {t['Ce']} | {t['Ca']} | {t['I']} | {fmt(t.get('change_reach'))} | {t['methods']} | {t['fields']} "
                   f"| {fmt(t['LCOM*'])} | {fmt(t['LCOM4'])} | {fmt(t.get('TCC'))} | {fmt(t.get('LCC'))} |" for t in r["by_class"]]
            md.append("")
            if r.get("yc_by_pair"):
                md += ["Connections between classes according to Yourdon & Constantine (the worst type of each pair): " + "; ".join(r["yc_by_pair"]), ""]
        md += ["### Vocabulary, Open/Closed and questions", ""]
        for c in FOLDERS:
            r = data.get(c, {}).get(phase)
            if not r:
                continue
            md.append(f"**{c}**")
            md.append(f"- Discarded terms found: {fmt(r['discarded_in_code'] or 'none')}")
            md.append(f"- Glossary terms missing: {fmt(r['model_terms_missing'])}")
            if "new_detail" in r:
                md.append(f"- New classes: {fmt(r['new_detail'])}; modified: {fmt(r['modified_detail'])}; existing tests touched: {fmt(r['touched_tests_detail'])}")
            md.append("- Questions Bob would have asked:")
            md += [f"  - {q}" for q in r.get("questions", [])] or ["  - none"]
            md.append("")
    (RESULTS / "metrics.md").write_text("\n".join(md) + "\n", encoding="utf-8")
    print(f"wrote {_rel(RESULTS / 'metrics.md')}")
    summary(data)
    print(f"wrote {_rel(RESULTS / 'summary.md')}")


# ---------------------------------------------------------------- executive summary
def _num(v):
    """Turns '11/12' into 0.9167, '1 / 2' into 0.5, numbers into float; None when it cannot."""
    if isinstance(v, (int, float)):
        return float(v)
    if isinstance(v, list):
        return float(len(v))
    if isinstance(v, str):
        m = re.fullmatch(r"\s*(\d+)\s*/\s*(\d+)\s*", v)
        if m:
            return int(m.group(1)) / int(m.group(2)) if int(m.group(2)) else None
        try:
            return float(v)
        except ValueError:
            return None
    return None


# (label, key or function, better: "max" | "min" | None)
SCOREBOARD = [
    ("Accuracy", None, None),
    ("Correct games according to the log (move by move)", "acceptance_log", "max"),
    ("Hidden games with the exact console output", "acceptance", "max"),
    ("  · with the correct logic, even if the format fails", "acceptance_logic", "max"),
    ("The console shows the same board as the log", "console_matches_log", "max"),
    ("F1 regressions", lambda r: r.get("acceptance_F1"), "max"),
    ("Mutation score without the console (%)", "mutation_score_no_io", "max"),
    ("Bob's tests that pass (%)", "tests_passed_pct", "max"),
    ("Bob's suite agrees with the hidden games", lambda r: None if "suite_right" not in r else ("yes" if r["suite_right"] else "no"), None),
    ("Escapes: hidden games that fail with the suite green", "test_escapes", "min"),
    ("Agent cost", None, None),
    ("Time (s)", "duration_s", "min"),
    ("Context at the end (k tokens)", "context_k", "min"),
    ("Cost (Bobcoins)", "cost_bobcoins", "min"),
    ("Bobcoins per correct hidden game", "cost_per_game", "min"),
    ("Seconds per correct hidden game", "seconds_per_game", "min"),
    ("Bobcoins per game with the correct logic", "cost_per_game_logic", "min"),
    ("Bobcoins per correct game according to the log", "cost_per_game_log", "min"),
    ("How Bob worked", None, None),
    ("Docs read before the first edit", "docs_read_before_edit", None),
    ("Code files read before the first edit", "code_read_before_edit", "min"),
    ("Code reads in the whole session", "reads_code", "min"),
    ("Model calls", "model_calls", "min"),
    ("Autonomy", None, None),
    ("Human interventions (messages and rejected approvals)", "interventions", "min"),
    ("Questions to the user", "questions_to_user", "min"),
    ("Denied commands it tried", "denied_commands", "min"),
    ("Result Bob declared", "result_status", None),
    ("Own decisions", "decisions", "min"),
    ("  · because of gaps or ambiguities in the documentation", lambda r: r.get("dec_gap", 0) + r.get("dec_ambiguity", 0) if "decisions" in r else None, "min"),
    ("Design", None, None),
    ("Classes · production lines", lambda r: f"{r.get('classes')} · {r.get('loc_main')}", None),
    ("Lines per file (max)", "file_lines_max", "min"),
    ("Methods with more than 30 lines", "methods_>30_lines", "min"),
    ("Methods with more than 3 parameters", "methods_>3_params", "min"),
    ("Ce max (classes the most coupled one uses)", "Ce_max", "min"),
    ("Propagation cost (%)", "propagation_cost", "min"),
    ("Yourdon & Constantine index (1 data … 6 content)", "yc_index", "min"),
    ("Worst Yourdon & Constantine connection", "yc_worst", None),
    ("TCC median (higher = more cohesive)", "TCC_median", "max"),
    ("LCOM* median (lower = more cohesive)", "LCOM*_median", "min"),
    ("Public methods that change state", lambda r: len(r.get("public_mutators", [])), "min"),
    ("Getters that expose modifiable state", lambda r: len(r.get("state_leaks", [])), "min"),
    ("Classes with console I/O", lambda r: len(r.get("classes_with_IO", [])), None),
    ("Change", None, None),
    ("Existing classes modified", "modified_classes", "min"),
    ("Lines changed in existing classes", "lines_changed_in_existing", "min"),
    ("Existing tests touched", "existing_tests_touched", "min"),
    ("Bobcoins per 100 lines touched", "cost_per_100_lines_touched", "min"),
    ("Language", None, None),
    ("Discarded terms in the code", "discarded_terms", "min"),
    ("Glossary terms present", "model_terms", "max"),
]


def summary(data):
    phases = sorted({f for c in data for f in data[c]})
    val = lambda r, k: (k(r) if callable(k) else r.get(k)) if r else None
    md = ["# Executive summary", "",
          "Bob built the same Tic-Tac-Toe twice with the same prompts: **without-ddd** with the requirements only, "
          "**with-ddd** with the requirements plus the DDD documentation (glossary, model, invariants, states and modules). "
          "Each phase was a new session without memory: F1 base game, F2 against the PC, F3 three pieces that move.", "",
          "> One execution per folder: the differences are observations of this execution, not proof. "
          "The detail of every number is in `metrics.md`.", ""]

    # Scoreboard
    head = ["Metric"] + [f"{f} {'without' if c == 'without-ddd' else 'with'} DDD" for f in phases for c in FOLDERS]
    md += ["## Scoreboard", "", "In bold, the best value of each phase. Not bold: a tie or a descriptive metric.", "",
           "| " + " | ".join(head) + " |", "|" + "---|" * len(head)]
    for label, key, better in SCOREBOARD:
        if key is None:
            md.append(f"| **{label}** |" + " |" * (len(head) - 1))
            continue
        cells = []
        for f in phases:
            rs = [data.get(c, {}).get(f) for c in FOLDERS]
            vs = [val(r, key) for r in rs]
            ns = [_num(v) for v in vs]
            wins = None
            if better and all(n is not None for n in ns) and ns[0] != ns[1]:
                wins = ns.index(max(ns) if better == "max" else min(ns))
            for i, v in enumerate(vs):
                t = fmt(v) if not isinstance(v, list) else str(len(v))
                cells.append(f"**{t}**" if wins == i else t)
        if all(c == "—" for c in cells):
            continue
        md.append(f"| {label} | " + " | ".join(cells) + " |")
    md.append("")

    # Cumulative cost
    md += ["## Cumulative cost", "", "| | without-ddd | with-ddd | with-ddd ÷ without-ddd |", "|---|---|---|---|"]
    for label, k in (("Time (s)", "duration_s"), ("Cost (Bobcoins)", "cost_bobcoins")):
        tot = [sum(data.get(c, {}).get(f, {}).get(k) or 0 for f in phases) for c in FOLDERS]
        ratio = f"{tot[1] / tot[0]:.2f}×" if tot[0] else "—"
        md.append(f"| {label} | {round(tot[0], 2)} | {round(tot[1], 2)} | {ratio} |")
    md += ["", f"Phases included: {', '.join(phases)}.", ""]

    # Efficiency: correct games against cost and time
    md += ["## Efficiency: correct games against cost and time", "",
           "Each phase plays its hidden games and those of the previous phases (regressions). Fewer Bobcoins and seconds per correct game = more efficient.", "",
           "If a folder fails only because of the console format, the \"correct logic\" columns still let you compare it.", "",
           "| Phase | Folder | Correct according to the log | Exact console | Console with the correct logic | Bobcoins | Time (s) | Bobcoins per game (log) | Bobcoins per game (exact console) | Bobcoins per game (logic) | Seconds per game (logic) |",
           "|---|---|---|---|---|---|---|---|---|---|---|"]
    for f in phases:
        for c in FOLDERS:
            r = data.get(c, {}).get(f)
            if r:
                md.append(f"| {f} | {c} | {fmt(r.get('acceptance_log'))} | {fmt(r.get('acceptance'))} | {fmt(r.get('acceptance_logic'))} "
                          f"| {fmt(r.get('cost_bobcoins'))} | {fmt(r.get('duration_s'))} | {fmt(r.get('cost_per_game_log'))} "
                          f"| {fmt(r.get('cost_per_game'))} | {fmt(r.get('cost_per_game_logic'))} | {fmt(r.get('seconds_per_game_logic'))} |")
    for c in FOLDERS:
        rs = [data.get(c, {}).get(f) or {} for f in phases]
        ok = sum(r.get("games_correct") or 0 for r in rs)
        tot = sum(r.get("games_total") or 0 for r in rs)
        rl = [r.get("acceptance_log") for r in rs if r.get("acceptance_log")]
        log_txt = f"{sum(int(x.split('/')[0]) for x in rl)}/{sum(int(x.split('/')[1]) for x in rl)}" if rl else "—"
        log_ok = sum(int(x.split("/")[0]) for x in rl)
        lg = sum(int(str(r.get("acceptance_logic", "0/0")).split("/")[0] or 0) for r in rs)
        cost = sum(r.get("cost_bobcoins") or 0 for r in rs)
        sec = sum(r.get("duration_s") or 0 for r in rs)
        per = lambda a, b, nd: round(a / b, nd) if a and b else "—"
        if tot:
            md.append(f"| **Total** | {c} | {log_txt} | {ok}/{tot} | {lg}/{tot} | {round(cost, 2)} | {sec} | {per(cost, log_ok, 3)} "
                      f"| {per(cost, ok, 3)} | {per(cost, lg, 3)} | {per(sec, lg, 1)} |")
    md.append("")

    # Scalability: how design, accuracy and cost change while the system grows
    md += ["## Scalability: what happens while the system grows", "",
           "A design that scales grows in lines without growing its propagation cost, its coupling or the cost of each change, and without losing correct games.", "",
           "| Folder | Phase | Lines · files | Lines per file (max) | Methods > 30 lines | Propagation cost (%) | Y&C index | TCC median | Correct games (log, or console in series 1) | Bobcoins | Bobcoins per 100 lines touched |",
           "|---|---|---|---|---|---|---|---|---|---|---|"]
    for c in FOLDERS:
        for f in phases:
            r = data.get(c, {}).get(f)
            if r:
                md.append(f"| {c} | {f} | {fmt(r.get('loc_main'))} · {fmt(r.get('files'))} | {fmt(r.get('file_lines_max'))} "
                          f"| {fmt(r.get('methods_>30_lines'))} | {fmt(r.get('propagation_cost'))} | {fmt(r.get('yc_index'))} "
                          f"| {fmt(r.get('TCC_median'))} | {fmt(r.get('acceptance_log') or r.get('acceptance'))} | {fmt(r.get('cost_bobcoins'))} "
                          f"| {fmt(r.get('cost_per_100_lines_touched'))} |")
    md.append("")
    for c in FOLDERS:
        fs = [f for f in phases if data.get(c, {}).get(f)]
        if len(fs) < 2:
            continue
        a, b = data[c][fs[0]], data[c][fs[-1]]
        parts = []
        if a.get("loc_main") and b.get("loc_main"):
            parts.append(f"lines went from {a['loc_main']} to {b['loc_main']} ({round(100 * (b['loc_main'] / a['loc_main'] - 1)):+d} %)")
        if a.get("propagation_cost") is not None and b.get("propagation_cost") is not None:
            parts.append(f"the propagation cost from {a['propagation_cost']} % to {b['propagation_cost']} %")
        if a.get("yc_index") is not None and b.get("yc_index") is not None:
            parts.append(f"the Y&C index from {a['yc_index']} to {b['yc_index']}")
        pa, pb = _num(a.get("acceptance_log") or a.get("acceptance")), _num(b.get("acceptance_log") or b.get("acceptance"))
        if pa is not None and pb is not None:
            parts.append(f"correct games from {round(100 * pa)} % to {round(100 * pb)} %")
        if parts:
            md.append(f"- **{c}**, from {fs[0]} to {fs[-1]}: " + ", ".join(parts) + ".")
    md += ["", "With several archived runs, `python3 measure.py stats` writes to `runs/stats.md` the medians, "
           "whether each difference repeats in every run, and the correlations between design, accuracy and cost.", ""]

    # Findings
    desc = {}
    for line in SCENARIOS.read_text().splitlines():
        if line and not line.startswith("#"):
            cols = line.split("\t")
            desc[cols[0]] = cols[-1]
    md += ["## Findings", ""]
    for f in phases:
        md.append(f"### {f}")
        for c in FOLDERS:
            r = data.get(c, {}).get(f)
            if not r:
                continue
            points = []
            if r.get("acceptance_log_failures"):
                points.append("JSONL log: " + "; ".join(r["acceptance_log_failures"][:6]) + ".")
            if r.get("console_matches_log") and r["console_matches_log"].split("/")[0] != r["console_matches_log"].split("/")[1]:
                points.append(f"The console does not show the same board as the log in every game: {r['console_matches_log']}.")
            if r.get("acceptance_failures"):
                points.append("Hidden games that fail: " + "; ".join(f"{s} ({desc.get(s, '')})" for s in r["acceptance_failures"]) + ".")
                if r.get("acceptance_logic") and r["acceptance_logic"].split("/")[0] != r["acceptance"].split("/")[0]:
                    points.append(f"With the correct game logic: {r['acceptance_logic']}. The rest fail because of the format: the output breaks "
                                  "the console contract (for example, a prompt without a line break glued to the first row of the board).")
            else:
                points.append(f"Every hidden game correct ({r.get('acceptance')}).")
            doubts = [q for t, q in r.get("questions_by_type", []) if t in ("gap", "ambiguity")]
            if doubts:
                points.append("Gaps Bob had to decide on its own: " + " / ".join(doubts))
            if r.get("state_leaks"):
                points.append("Exposed state: " + ", ".join(r["state_leaks"]) + ".")
            if r.get("public_mutators"):
                points.append("Public methods that change state: " + ", ".join(r["public_mutators"]) + ".")
            if r.get("discarded_in_code"):
                points.append("Discarded terms in the code: " + ", ".join(f"{t} ({', '.join(ids)})" for t, ids in r["discarded_in_code"].items()) + ".")
            if r.get("modified_detail"):
                points.append("Existing classes modified: " + ", ".join(Path(x).stem for x in r["modified_detail"]) + ".")
            if r.get("requirements_modified"):
                points.append("Bob modified REQUIREMENTS.md, which had to stay the same in both folders.")
            if r.get("snapshot_rebuilt"):
                points.append("This phase's snapshot was rebuilt from bob.db (it was not taken in time) and validated against the reads of the next phase.")
            if r.get("docs_domain_updated"):
                points.append("Domain docs updated: " + ", ".join(r["docs_domain_updated"]) + ".")
            md.append(f"**{c}**")
            md += [f"- {p}" for p in points]
            md.append("")
    md += ["## How to read the metrics", "",
           "- **JSONL log (series 2):** the program writes the board and the state after each move to the file in `TICTACTOE_BOARD_LOG`. "
           "A game is correct if it starts empty, each line is a valid put or move by the player whose turn it is, and the final board and state are the expected ones. "
           "The console is still measured separately: it is what the user sees.",
           "- **Ce (efferent coupling):** how many project classes a class uses. Lower is better.",
           "- **Ca (afferent coupling):** how many classes use it. It is only fine to be high for stable classes, such as an enum.",
           "- **Propagation cost:** for each change in a class, what part of the system it can reach, directly or transitively (MacCormack et al., 2006). Lower is better; if it grows with each phase, the design scales badly.",
           "- **Yourdon & Constantine:** the type of connection between classes, from best to worst: 1 data (simple values), 2 stamp (passes a whole object), "
           "3 control (passes a boolean or enum flag that decides what the other one does), 4 external (they share the console or another device), "
           "5 common (they share mutable global data), 6 content (one touches the other's fields). The index is the mean per class pair.",
           "- **TCC and LCC:** share of pairs of visible methods that share a field, directly (TCC) or through a chain (LCC). 1 is very cohesive; TCC < 0.5, not very cohesive.",
           "- **LCOM\\*:** lack of cohesion. 0 means every method uses every field; 1 or more, that the class mixes responsibilities. Getters are excluded.",
           f"- **Size:** lines per file and per method without blank lines or comments; a method with more than {MAX_LINES} lines or more than {MAX_PARAMS} parameters usually does too much.",
           "- **Efficiency:** Bobcoins and seconds per correct hidden game. **Bobcoins per 100 lines touched** (F2 and F3) is the marginal cost of the change.",
           "- **Mutation score:** PIT introduces small bugs in the code and counts how many the tests detect. It is shown without the console, which neither folder tests.",
           "- **Encapsulation:** the public methods that change state are the doors to modify the game; a getter that exposes modifiable state lets callers skip the rules.",
           "- **Open/Closed:** a well-absorbed change adds classes and touches few lines of the existing ones.",
           "- **Context:** what filled Bob's window at the end, not the total consumed; the cost in Bobcoins reflects the spend better.", ""]
    (RESULTS / "summary.md").write_text("\n".join(md), encoding="utf-8")


# ---------------------------------------------------------------- run history
def archive(description=None):
    """Moves the current run (results/) to the history, runs/run-N/, and recomputes the stats."""
    if not has_snapshots(RESULTS):
        sys.exit(f"{RESULTS} has no snapshots: there is no run to archive")
    if not (RESULTS / "metrics.json").exists():
        sys.exit("the run is not measured: run  python3 measure.py  first")
    desc = RESULTS / "description.txt"
    if description:
        desc.write_text(description.strip() + "\n", encoding="utf-8")
    elif not desc.exists():
        sys.exit('the description is missing:  python3 measure.py archive "what with-ddd received in this run"')
    RUNS.mkdir(exist_ok=True)
    n = 1 + max((int(m.group(1)) for p in RUNS.glob("run-*") if (m := re.fullmatch(r"run-(\d+)", p.name))), default=0)
    target = RUNS / f"run-{n}"
    shutil.move(str(RESULTS), target)
    RESULTS.mkdir()
    conclusions = ROOT / "CONCLUSIONS.md"  # COMPARE.md writes it at the root; it belongs to this run
    if conclusions.exists():
        shutil.move(str(conclusions), target / "CONCLUSIONS.md")
    print(f"run archived in {_rel(target)}{' with CONCLUSIONS.md' if conclusions.exists() is False and (target / 'CONCLUSIONS.md').exists() else ''}; results/ is empty for the next one")
    import stats
    stats.main()


def measure_all():
    """Measures every run in the history and the current one again, and recomputes the stats."""
    global RESULTS
    paths = sorted(p for p in RUNS.glob("*") if p.is_dir()) + [ROOT / "results"]
    for path in paths:
        if has_snapshots(path):
            print(f"\n=== {_rel(path)}")
            RESULTS = path.resolve()
            measure()
    import stats
    stats.main()


if __name__ == "__main__":
    if len(sys.argv) == 3 and sys.argv[1] == "snapshot":
        snapshot(sys.argv[2])
    elif sys.argv[1:] == ["audit"]:
        audit()
    elif sys.argv[1:] == ["reset"]:
        reset()
    elif sys.argv[1:2] == ["archive"] and len(sys.argv) <= 3:
        archive(sys.argv[2] if len(sys.argv) == 3 else None)
    elif sys.argv[1:] == ["stats"]:
        import stats
        stats.main()
    elif sys.argv[1:] == ["all"]:
        measure_all()
    elif len(sys.argv) == 1:
        measure()
    else:
        sys.exit(__doc__)
