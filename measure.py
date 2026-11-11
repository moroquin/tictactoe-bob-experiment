#!/usr/bin/env python3
"""Measures the two experiment folders and writes results/metrics.md.

Usage:
  python3 measure.py snapshot F1   # when a phase (F1 … F7) ends in BOTH folders: saves a copy of the
                                   # code and of its history in results/<folder>/F1/
  python3 measure.py               # measures every saved snapshot and writes results/summary.md and metrics.md
  python3 measure.py snapshot F2 with-ddd   # the snapshot of one folder only (after a redo)
  python3 measure.py redo F2 with-ddd       # discards that folder's F2 and restores it to its F1 snapshot,
                                   # to run the phase again (for example, after a session was cut)
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
PACKAGE, JAR = "enrollment", "enrollment.jar"
PHASES = 7
PHASE_GLOB = "F[1-7]"
GLOSSARY = WORKSPACE / "with-ddd" / "docs" / "domain" / "glossary.md"
TYPES = ["ambiguity", "gap", "design", "naming", "scope", "deviation", "workaround"]
# Experiment series: goes up when anything in src/, the prompts or the scenarios change, so that the stats never
# mix runs that received different things. Series 1: the course enrollment system.
SERIES = 1
import oracle  # noqa: E402  the reference model and the hidden scenarios


def _rel(p):
    """Path relative to the repository when it is inside it; absolute otherwise."""
    p = Path(p).resolve()
    return p.relative_to(ROOT) if p.is_relative_to(ROOT) else p


def has_snapshots(folder):
    """True when folder holds at least one phase snapshot (folder/<without-ddd|with-ddd>/F<n>)."""
    return any(p.is_dir() for c in FOLDERS for p in (folder / c).glob(PHASE_GLOB))


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
def snapshot(phase, only=None):
    if not re.fullmatch(r"F[1-7]", phase):
        sys.exit("phase: F1 to F7")
    if only and only not in FOLDERS:
        sys.exit(f"folder: {' or '.join(FOLDERS)}")
    for c in [only] if only else FOLDERS:
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


def redo(phase, c):
    """Runs a phase again in one folder: moves its snapshot of the phase (if any) to results/discarded/, restores the
    experiment folder to its snapshot of the previous phase (or to src/ for F1) and records the time, so that
    measure.py only counts the Bob sessions that start after it."""
    if not re.fullmatch(r"F[1-7]", phase) or c not in FOLDERS:
        sys.exit("usage: python3 measure.py redo F<n> without-ddd|with-ddd")
    n = int(phase[1:])
    later = [p.name for p in (RESULTS / c).glob(PHASE_GLOB) if int(p.name[1:]) > n]
    if later:
        sys.exit(f"{c} already has snapshots of later phases ({', '.join(sorted(later))}): redo only works on the last phase")
    source = RESULTS / c / f"F{n - 1}" if n > 1 else ROOT / "src" / c
    if not source.is_dir():
        sys.exit(f"{source} does not exist")
    snap = RESULTS / c / phase
    if snap.exists():
        dest = RESULTS / "discarded" / f"{c}-{phase}-{_now_ms()}"
        dest.parent.mkdir(exist_ok=True)
        shutil.move(str(snap), dest)
        print(f"{_rel(snap)} moved to {_rel(dest)}")
    dst = WORKSPACE / c
    if dst.exists():
        shutil.rmtree(dst)
    shutil.copytree(source, dst, ignore=shutil.ignore_patterns(".snapshot.json", "session-*.md", ".git"))
    (dst / "history").mkdir(exist_ok=True)
    restarts = RESULTS / "restarts.json"
    data = json.loads(restarts.read_text()) if restarts.exists() else {}
    data.setdefault(c, {})[phase] = _now_ms()
    restarts.write_text(json.dumps(data, indent=2))
    print(f"{c}: restored to {_rel(source)}. Run {phase} again in ../{c} (a new bob session), then:\n"
          f"  python3 measure.py snapshot {phase} {c}")


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
        # Sessions a redo threw away: from the snapshot of the phase before up to the restart.
        try:
            restarts = json.loads((RESULTS / "restarts.json").read_text())
        except (OSError, ValueError):
            restarts = {}
        discarded = {c: [((_ms(RESULTS / c / f"F{int(ph[1:]) - 1}" / ".snapshot.json") if int(ph[1:]) > 1 else since) or 0, ms, ph)
                         for ph, ms in phases.items()] for c, phases in restarts.items()}
        skipped = 0
        for c in FOLDERS:
            folder = str(WORKSPACE / c)
            tasks = con.execute("select id, substr(title, 1, 40), created_at from tasks where project_id in (?, ?) order by created_at",
                                (folder, "file:" + folder)).fetchall()
            for tid, title, created in tasks:
                created_ms = created if created > 1e12 else created * 1000
                if created_ms < since:
                    continue
                redo = next((ph for a, b, ph in discarded.get(c, []) if a <= created_ms < b), None)
                if redo:
                    skipped += 1
                    if not os.environ.get("ALL"):
                        continue
                outside, forbidden = set(), set()
                for (data,) in con.execute("select data from messages where task_id = ? and role = 'assistant'", (tid,)):
                    for path in re.findall(r"(/(?:Users|home|private|tmp|var)/[^\s\"'\\,)]+|\.\./[^\s\"'\\,)]+)", data):
                        # /bob-tool-outputs/: Bob Shell saves there the full output of a tool it truncated (a file
                        # of the folder, a build) and tells the agent to read it: the content comes from the folder.
                        tools = ("/.sdkman/", "/.m2/", "/homebrew/", "/usr/", "/opt/", "/Library/Java/", "/bob-tool-outputs/")
                        if path.startswith(folder) or any(t in path for t in tools) or path.rstrip("/") == str(Path.home()):
                            continue
                        outside.add(path[:80])
                    for cmd in re.findall(r'"command":\s*"([^"]{0,200})', data):
                        if re.search(r"\b(git|curl|wget|env|printenv)\b", cmd):
                            forbidden.add(cmd[:80])
                date = datetime.fromtimestamp(created / 1000 if created > 1e12 else created).strftime("%Y-%m-%d %H:%M")
                ok = not outside and not forbidden
                invalid += not ok and not redo
                label = "REDONE " if redo else "OK     " if ok else "INVALID"
                print(f"{label} {c:12} {date}  {title.splitlines()[0] if title else ''}" + (f"  (discarded: {redo} was run again)" if redo else ""))
                for x in sorted(outside)[:5]:
                    print(f"           outside the folder: {x}")
                for x in sorted(forbidden)[:5]:
                    print(f"           forbidden command: {x}")
        if skipped and not os.environ.get("ALL"):
            print(f"({skipped} session(s) of a phase that was run again with redo are not shown and do not count; ALL=1 shows them)")
        print("\nEvery session respected the isolation." if not invalid else f"\n{invalid} invalid sessions: repeat those phases.")
    exposed = [e for e in (git_exposure(WORKSPACE / c) for c in FOLDERS if (WORKSPACE / c).is_dir()) if e]
    exposed += [f"snapshot {p.parent.parent.name} {p.parent.name}: {json.loads(p.read_text())['git_exposure']}"
                for p in sorted(RESULTS.glob(f"*/{PHASE_GLOB}/.snapshot.json")) if "git_exposure" in json.loads(p.read_text())]
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
    n = int(phase[1:])
    start = (_ms(RESULTS / c / f"F{n - 1}" / ".snapshot.json") if n > 1 else _ms(RESULTS / "reset.json")) or 0
    try:  # a phase run again (redo): only the sessions after the restart count
        start = max(start, json.loads((RESULTS / "restarts.json").read_text()).get(c, {}).get(phase, 0))
    except (OSError, ValueError):
        pass
    folder = str(WORKSPACE / c)
    tasks = con.execute("select id from tasks where project_id in (?, ?) and created_at > ? and created_at < ? order by created_at",
                        (folder, "file:" + folder, start, end)).fetchall()
    if not tasks:
        return {}

    def kind(path):
        rel = path.replace(folder + "/", "")
        if rel.startswith("docs/") or rel in ("AGENTS.md", "REQUIREMENTS.md", "ARCHITECTURE.md"):
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
    auto = {"human_messages": 0, "questions_to_user": 0, "denied_commands": 0, "approvals_rejected": 0, "session_errors": 0,
            "budget_interruptions": 0}
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
        failed = bool(status and (status[0] == "error" or (status[1] not in (None, "", "null"))))
        # Running out of the plan's Bobcoins cuts a session from outside: it is not Bob's error, it is counted apart.
        budget = failed and "BudgetExceeded" in str(status[1])
        auto["budget_interruptions"] += budget
        auto["session_errors"] += failed and not budget
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


# ---------------------------------------------------------------- Bob's tests against the hidden scenarios
def test_accuracy(r):
    """Bob writes its own tests, so they are judged against the hidden scenarios (the oracle). The suite is right
    when it is green and every hidden scenario is correct, or when it is red and some scenario fails. Escapes:
    hidden scenarios that fail while the suite is green. False alarms: failing tests when every hidden scenario is
    correct. Recall against planted bugs is the mutation score."""
    tests, failed = r.get("tests"), r.get("tests_failed")
    oracle = r.get("acceptance")
    out = {"tests_passed_pct": round(100 * (tests - failed) / tests, 1) if tests else None}
    m = re.fullmatch(r"(\d+)/(\d+)", oracle or "")
    if not tests or not m:
        return out
    ok, total = int(m.group(1)), int(m.group(2))
    green, hidden_failed = failed == 0, total - ok
    out.update({
        "suite_verdict": ("right: green and every hidden scenario correct" if green and not hidden_failed else
                          "false confidence: green but hidden scenarios fail" if green else
                          "right: red and hidden scenarios fail" if hidden_failed else
                          "false alarm: red but every hidden scenario correct"),
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
    if not (work / "target" / JAR).exists():
        sh("mvn -q -DskipTests package", cwd=work)
    return work, r


def _matches(want, got):
    """An expected line of the oracle against an actual line: OK, Error and 'Error in line <n>' leave the reason free."""
    if want in (oracle.OK, oracle.ERR):
        return got.startswith(want + ":")
    if re.fullmatch(r"Error in line \d+", want):
        return got.startswith(want + ":")
    return got.rstrip() == want


def _lines_matched(want, got):
    """How many expected lines appear in order in the actual output (longest common subsequence)."""
    prev = [0] * (len(got) + 1)
    for w in want:
        cur = [0]
        for j, g in enumerate(got, 1):
            cur.append(prev[j - 1] + 1 if _matches(w, g) else max(prev[j], cur[j - 1]))
        prev = cur
    return prev[-1]


def acceptance(jar, phase, cmd=None):
    """Runs every hidden scenario that exists in this phase (scenarios/, `since` up to this phase) and compares its
    standard output with what the reference model (oracle.py) expects in this phase. A scenario is correct when
    every line matches and the program ends with exit code 0. It also counts the expected lines that appear in
    order, a finer measure of how close the output is."""
    n = int(phase[1:])
    by_phase, failures = defaultdict(lambda: [0, 0]), []
    lines_ok = lines_total = 0
    regressions = [0, 0]
    for sc in oracle.scenarios():
        if sc["since"] > n:
            continue
        with tempfile.TemporaryDirectory() as tmp:
            oracle.write_files(sc, tmp)
            want = oracle.expected(sc["lines"], n, tmp)
            run = cmd or ["java", "-jar", str(jar)]
            try:
                p = subprocess.run(run, cwd=tmp, capture_output=True, text=True, timeout=30,
                                   input="\n".join(sc["lines"]) + "\n", encoding="utf-8", errors="replace")
                code, out = p.returncode, p.stdout
            except subprocess.TimeoutExpired:
                code, out = -1, ""
        got = [l for l in out.split("\n")]
        while got and got[-1].strip() == "":
            got.pop()
        ok = code == 0 and len(got) == len(want) and all(_matches(w, g) for w, g in zip(want, got))
        matched = _lines_matched(want, got)
        lines_ok, lines_total = lines_ok + matched, lines_total + len(want)
        key = f"F{sc['since']}"
        by_phase[key][0] += ok
        by_phase[key][1] += 1
        if sc["since"] < n:
            regressions[0] += ok
            regressions[1] += 1
        if not ok:
            first = next((i for i, (w, g) in enumerate(zip(want, got)) if not _matches(w, g)), min(len(want), len(got)))
            detail = (f"exit code {code}" if code != 0 and len(got) == len(want) else
                      f"line {first + 1}: expected {want[first]!r}, got {got[first]!r}" if first < min(len(want), len(got)) else
                      f"{len(got)} lines instead of {len(want)}")
            failures.append(f"{sc['name']}: {detail}")
    total = [sum(v[0] for v in by_phase.values()), sum(v[1] for v in by_phase.values())]
    r = {"acceptance": f"{total[0]}/{total[1]}", "acceptance_failures": failures,
         "acceptance_lines": f"{lines_ok}/{lines_total}",
         "acceptance_lines_pct": round(100 * lines_ok / lines_total, 1) if lines_total else None,
         "regressions": f"{regressions[0]}/{regressions[1]}" if regressions[1] else None}
    for k, v in sorted(by_phase.items(), key=lambda kv: int(kv[0][1:])):
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
        cls = m.find("mutatedClass").text.split("$")[0].replace(PACKAGE + ".", "")
        tot[cls][1] += 1
        tot[cls][0] += m.get("status") == "KILLED"
    k, g = sum(v[0] for v in tot.values()), sum(v[1] for v in tot.values())
    # Without the console: the classes that touch the standard streams and the console package (ARCHITECTURE.md).
    io = lambda c: c in io_classes or c.startswith("console.")
    nk = sum(v[0] for c, v in tot.items() if not io(c))
    ng = sum(v[1] for c, v in tot.items() if not io(c))
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
    """Correct hidden scenarios against cost and time, and cost of the change per line touched (scalability)."""
    out = {}
    try:
        ok, total = map(int, r["acceptance"].split("/"))
    except (KeyError, ValueError, AttributeError):
        return out
    out["scenarios_correct"], out["scenarios_total"] = ok, total
    out["acceptance_pct"] = round(100 * ok / total, 1) if total else None
    cost, sec = r.get("cost_bobcoins"), r.get("duration_s")
    if ok and cost:
        out["cost_per_scenario"] = round(cost / ok, 3)
    if ok and sec:
        out["seconds_per_scenario"] = round(sec / ok, 1)
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
    ("Hidden scenarios correct", "acceptance"), ("Expected lines matched", "acceptance_lines"),
    ("Scenarios of earlier phases still correct (regressions and changed rules)", "regressions"),
    *[(f"Scenarios of F{i}", f"acceptance_F{i}") for i in range(1, PHASES + 1)],
    ("Failed scenarios", "acceptance_failures"),
    ("Bob's tests that pass (%)", "tests_passed_pct"),
    ("Bob's tests against the hidden scenarios: verdict of the suite", "suite_verdict"),
    ("Bob's tests: escapes (hidden scenarios that fail with the suite green)", "test_escapes"),
    ("Bob's tests: false alarms (failing tests with every hidden scenario correct)", "false_alarms"),
    ("Total mutation score (%)", "mutation_score"), ("Mutants killed/generated", "mutants"),
    ("Mutation score without the console (%)", "mutation_score_no_io"), ("Mutants without the console", "mutants_no_io"),
    ("Production classes", "classes"), ("Packages", "packages"), ("Production lines", "loc_main"),
    ("Test lines", "loc_test"),
    ("Production files", "files"), ("Lines per file (median · max)", "file_lines"),
    ("Files with more than 200 lines", "files_>200_lines"),
    ("Methods and constructors", "methods_total"), ("Lines per method (median · max)", "method_lines"),
    ("Methods of 30 lines or fewer (%)", "methods_up_to_30_lines_pct"),
    ("Methods with more than 30 lines", "long_methods"),
    ("Methods and constructors with more than 3 parameters (4 or more)", "methods_>3_params"),
    ("  · methods, without constructors", "methods_>3_params_without_constructors"),
    ("  · constructors", "constructors_>3_params"),
    ("Which ones", "many_params_methods"),
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
    ("Efficiency: Bobcoins per correct hidden scenario", "cost_per_scenario"),
    ("Efficiency: seconds per correct hidden scenario", "seconds_per_scenario"),
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
    ("Sessions cut because the plan ran out of Bobcoins", "budget_interruptions"),
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
        snaps = sorted((p.name for p in (RESULTS / c).glob(PHASE_GLOB) if p.is_dir()), key=lambda f: int(f[1:]))
        for phase in snaps:
            snap = RESULTS / c / phase
            print(f"measuring {c} {phase}…", flush=True)
            with tempfile.TemporaryDirectory() as tmp:
                work, r = build(snap, tmp)
                r.update(acceptance(work / "target" / JAR, phase))
                r.update(design(work / "target" / "classes"))
                r.update(mutants(work, r["classes_with_IO"]))
            main, test = sources(snap, "main"), sources(snap, "test")
            r["loc_main"], r["loc_test"] = loc(main), loc(test)
            r.update(size_metrics(main))
            r["file_lines"] = f"{r['file_lines_median']} · {r['file_lines_max']}"
            r["method_lines"] = f"{r['method_lines_median']} · {r['method_lines_max']}"
            r["yc_pairs_txt"] = " · ".join(f"{r['yc_pairs'][t]} {t}" for t in YC_TYPES if r["yc_pairs"].get(t))
            r["ce"] = f"{r['Ce_median']} · {r['Ce_max']}"
            prev = RESULTS / c / f"F{int(phase[1:]) - 1}"
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
            else:  # F1: the docs against the starting point Bob received
                r.update(docs_changed(ROOT / "src" / c, snap))
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
    phases = sorted({f for c in data for f in data[c]}, key=lambda f: int(f[1:]))
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
            md += [f"| {t['class']} | {fmt(lbf.get(PACKAGE + '.' + t['class']))} | {t['Ce']} | {t['Ca']} | {t['I']} | {fmt(t.get('change_reach'))} | {t['methods']} | {t['fields']} "
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
    ("Hidden scenarios correct", "acceptance", "max"),
    ("Expected lines matched (%)", "acceptance_lines_pct", "max"),
    ("Scenarios of earlier phases still correct", "regressions", "max"),
    ("Mutation score without the console (%)", "mutation_score_no_io", "max"),
    ("Bob's tests that pass (%)", "tests_passed_pct", "max"),
    ("Bob's suite agrees with the hidden scenarios", lambda r: None if "suite_right" not in r else ("yes" if r["suite_right"] else "no"), None),
    ("Escapes: hidden scenarios that fail with the suite green", "test_escapes", "min"),
    ("Agent cost", None, None),
    ("Time (s)", "duration_s", "min"),
    ("Context at the end (k tokens)", "context_k", "min"),
    ("Cost (Bobcoins)", "cost_bobcoins", "min"),
    ("Bobcoins per correct hidden scenario", "cost_per_scenario", "min"),
    ("Seconds per correct hidden scenario", "seconds_per_scenario", "min"),
    ("How Bob worked", None, None),
    ("Docs read before the first edit", "docs_read_before_edit", None),
    ("Code and test reads before the first edit", "code_read_before_edit", "min"),
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
    ("Methods with more than 3 parameters (constructors included)", "methods_>3_params", "min"),
    ("  · without constructors", "methods_>3_params_without_constructors", "min"),
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
    ("New / modified classes", lambda r: f"{r['new_classes']} / {r['modified_classes']}" if "new_classes" in r else None, None),
    ("Existing classes modified", "modified_classes", "min"),
    ("Lines changed in existing classes", "lines_changed_in_existing", "min"),
    ("Existing tests touched", "existing_tests_touched", "min"),
    ("Bobcoins per 100 lines touched", "cost_per_100_lines_touched", "min"),
    ("Language", None, None),
    ("Discarded terms in the code", "discarded_terms", "min"),
    ("Glossary terms present", "model_terms", "max"),
]

PHASE_NAMES = {1: "build version 1", 2: "CR-1 modify and delete sections", 3: "CR-2 load a file and reports",
               4: "CR-3 balanced sections", 5: "CR-4 new grade scale", 6: "CR-5 prerequisites and credit limit",
               7: "CR-6 time conflicts"}


def summary(data):
    phases = sorted({f for c in data for f in data[c]}, key=lambda f: int(f[1:]))
    val = lambda r, k: (k(r) if callable(k) else r.get(k)) if r else None
    md = ["# Executive summary", "",
          "Bob built the same course enrollment system twice with the same prompts: **without-ddd** with the requirements "
          "and the architecture only, **with-ddd** with the same plus the DDD documentation (glossary, model, invariants, "
          "policies, modules and decision log). Each phase was a new session without memory: "
          + "; ".join(f"F{n} {PHASE_NAMES[n]}" for n in range(1, PHASES + 1)) + ".", "",
          "> One execution per folder: the differences are observations of this execution, not proof. "
          "The detail of every number is in `metrics.md`.", ""]

    # Scoreboard, one table per group of phases so that it stays readable
    for chunk in [phases[i:i + 4] for i in range(0, len(phases), 4)]:
        head = ["Metric"] + [f"{f} {'without' if c == 'without-ddd' else 'with'}" for f in chunk for c in FOLDERS]
        md += [f"## Scoreboard {chunk[0]}" + (f"–{chunk[-1]}" if len(chunk) > 1 else ""), "",
               "In bold, the best value of each phase. Not bold: a tie or a descriptive metric.", "",
               "| " + " | ".join(head) + " |", "|" + "---|" * len(head)]
        for label, key, better in SCOREBOARD:
            if key is None:
                md.append(f"| **{label}** |" + " |" * (len(head) - 1))
                continue
            cells = []
            for f in chunk:
                vs = [val(data.get(c, {}).get(f), key) for c in FOLDERS]
                ns = [_num(v) for v in vs]
                wins = None
                if better and all(n is not None for n in ns) and ns[0] != ns[1]:
                    wins = ns.index(max(ns) if better == "max" else min(ns))
                for i, v in enumerate(vs):
                    s = fmt(v) if not isinstance(v, list) else str(len(v))
                    cells.append(f"**{s}**" if wins == i else s)
            if all(c == "—" for c in cells):
                continue
            md.append(f"| {label} | " + " | ".join(cells) + " |")
        md.append("")

    # Cumulative cost and accuracy
    md += ["## Cumulative cost", "", "| | without-ddd | with-ddd | with-ddd ÷ without-ddd |", "|---|---|---|---|"]
    for label, k in (("Time (s)", "duration_s"), ("Cost (Bobcoins)", "cost_bobcoins")):
        tot = [sum(data.get(c, {}).get(f, {}).get(k) or 0 for f in phases) for c in FOLDERS]
        ratio = f"{tot[1] / tot[0]:.2f}×" if tot[0] else "—"
        md.append(f"| {label} | {round(tot[0], 2)} | {round(tot[1], 2)} | {ratio} |")
    md += ["", f"Phases included: {', '.join(phases)}.", ""]

    # Efficiency
    md += ["## Efficiency: correct scenarios against cost and time", "",
           "Each phase runs its own hidden scenarios and those of the earlier phases, with the rules of this phase "
           "(see `scenarios/EXPECTED.md`). Fewer Bobcoins and seconds per correct scenario = more efficient.", "",
           "| Phase | Folder | Scenarios correct | Expected lines matched | Bobcoins | Time (s) | Bobcoins per scenario | Seconds per scenario |",
           "|---|---|---|---|---|---|---|---|"]
    for f in phases:
        for c in FOLDERS:
            r = data.get(c, {}).get(f)
            if r:
                md.append(f"| {f} | {c} | {fmt(r.get('acceptance'))} | {fmt(r.get('acceptance_lines'))} | {fmt(r.get('cost_bobcoins'))} "
                          f"| {fmt(r.get('duration_s'))} | {fmt(r.get('cost_per_scenario'))} | {fmt(r.get('seconds_per_scenario'))} |")
    for c in FOLDERS:
        rs = [data.get(c, {}).get(f) or {} for f in phases]
        ok = sum(r.get("scenarios_correct") or 0 for r in rs)
        tot = sum(r.get("scenarios_total") or 0 for r in rs)
        cost = sum(r.get("cost_bobcoins") or 0 for r in rs)
        sec = sum(r.get("duration_s") or 0 for r in rs)
        per = lambda a, b, nd: round(a / b, nd) if a and b else "—"
        if tot:
            md.append(f"| **Total** | {c} | {ok}/{tot} | | {round(cost, 2)} | {sec} | {per(cost, ok, 3)} | {per(sec, ok, 1)} |")
    md.append("")

    # Scalability
    md += ["## Scalability: what happens while the system grows", "",
           "A design that scales grows in lines without growing its propagation cost, its coupling or the cost of each change, and without losing correct scenarios.", "",
           "| Folder | Phase | Lines · files | Lines per file (max) | Methods > 30 lines | Propagation cost (%) | Y&C index | TCC median | Scenarios correct | Bobcoins | Bobcoins per 100 lines touched |",
           "|---|---|---|---|---|---|---|---|---|---|---|"]
    for c in FOLDERS:
        for f in phases:
            r = data.get(c, {}).get(f)
            if r:
                md.append(f"| {c} | {f} | {fmt(r.get('loc_main'))} · {fmt(r.get('files'))} | {fmt(r.get('file_lines_max'))} "
                          f"| {fmt(r.get('methods_>30_lines'))} | {fmt(r.get('propagation_cost'))} | {fmt(r.get('yc_index'))} "
                          f"| {fmt(r.get('TCC_median'))} | {fmt(r.get('acceptance'))} | {fmt(r.get('cost_bobcoins'))} "
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
        pa, pb = _num(a.get("acceptance")), _num(b.get("acceptance"))
        if pa is not None and pb is not None:
            parts.append(f"correct scenarios from {round(100 * pa)} % to {round(100 * pb)} %")
        if parts:
            md.append(f"- **{c}**, from {fs[0]} to {fs[-1]}: " + ", ".join(parts) + ".")
    md += ["", "With several archived runs, `python3 measure.py stats` writes to `runs/stats.md` the medians, "
           "whether each difference repeats in every run, and the correlations between design, accuracy and cost.", ""]

    # Findings
    desc = {sc["name"]: sc["description"] for sc in oracle.scenarios()}
    md += ["## Findings", ""]
    for f in phases:
        md.append(f"### {f} · {PHASE_NAMES[int(f[1:])]}")
        for c in FOLDERS:
            r = data.get(c, {}).get(f)
            if not r:
                continue
            points = []
            if r.get("acceptance_failures"):
                points.append("Hidden scenarios that fail: " + "; ".join(
                    f"{x} ({desc.get(x.split(':')[0], '')})" for x in r["acceptance_failures"]) + ".")
            else:
                points.append(f"Every hidden scenario correct ({r.get('acceptance')}).")
            doubts = [q for t_, q in r.get("questions_by_type", []) if t_ in ("gap", "ambiguity")]
            if doubts:
                points.append("Gaps Bob had to decide on its own: " + " / ".join(doubts))
            if r.get("state_leaks"):
                points.append("Exposed state: " + ", ".join(r["state_leaks"]) + ".")
            if r.get("discarded_in_code"):
                points.append("Discarded terms in the code: " + ", ".join(f"{t_} ({', '.join(ids)})" for t_, ids in r["discarded_in_code"].items()) + ".")
            if r.get("modified_detail"):
                points.append("Existing classes modified: " + ", ".join(Path(x).stem for x in r["modified_detail"]) + ".")
            if r.get("requirements_modified"):
                points.append("Bob modified REQUIREMENTS.md, which had to stay the same in both folders.")
            if r.get("snapshot_rebuilt"):
                points.append("This phase's snapshot was rebuilt from bob.db (it was not taken in time).")
            if r.get("docs_domain_updated"):
                points.append("Domain docs updated: " + ", ".join(r["docs_domain_updated"]) + ".")
            md.append(f"**{c}**")
            md += [f"- {p_}" for p_ in points]
            md.append("")
    md += ["## How to read the metrics", "",
           "- **Hidden scenarios:** sequences of instructions Bob never sees (`scenarios/`). The reference model `oracle.py` computes "
           "what each one must output in each phase; a change request can change that, and `scenarios/EXPECTED.md` shows where. "
           "A scenario is correct when every line matches; the text after `OK:` and `Error:` is free unless the requirements fix it. "
           "**Expected lines matched** counts the expected lines that appear in order, so a nearly correct output still scores.",
           "- **Scenarios of earlier phases:** they run in every later phase with the rules of that phase: some check that nothing broke, "
           "others that an old behaviour changed as the change request said.",
           "- **Ce (efferent coupling):** how many project classes a class uses. Lower is better.",
           "- **Ca (afferent coupling):** how many classes use it. It is only fine to be high for stable classes, such as a value object.",
           "- **Propagation cost:** for each change in a class, what part of the system it can reach, directly or transitively (MacCormack et al., 2006). Lower is better; if it grows with each phase, the design scales badly.",
           "- **Yourdon & Constantine:** the type of connection between classes, from best to worst: 1 data (simple values), 2 stamp (passes a whole object), "
           "3 control (passes a boolean or enum flag that decides what the other one does), 4 external (they share the console or another device), "
           "5 common (they share mutable global data), 6 content (one touches the other's fields). The index is the mean per class pair.",
           "- **TCC and LCC:** share of pairs of visible methods that share a field, directly (TCC) or through a chain (LCC). 1 is very cohesive; TCC < 0.5, not very cohesive.",
           "- **LCOM\\*:** lack of cohesion. 0 means every method uses every field; 1 or more, that the class mixes responsibilities. Getters are excluded.",
           f"- **Size:** lines per file and per method without blank lines or comments; a method with more than {MAX_LINES} lines or more than {MAX_PARAMS} parameters usually does too much.",
           "- **Efficiency:** Bobcoins and seconds per correct hidden scenario. **Bobcoins per 100 lines touched** (F2 onwards) is the marginal cost of the change.",
           "- **Mutation score:** PIT introduces small bugs in the code and counts how many the tests detect. It is shown without the console classes.",
           "- **Encapsulation:** the public methods that change state are the doors to modify the data; a getter that exposes modifiable state lets callers skip the rules.",
           "- **Open/Closed:** a well-absorbed change adds classes and touches few lines of the existing ones. A change request that only changes a rule "
           "(F4, F5) should touch as few classes as possible.",
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
    if len(sys.argv) in (3, 4) and sys.argv[1] == "snapshot":
        snapshot(sys.argv[2], sys.argv[3] if len(sys.argv) == 4 else None)
    elif len(sys.argv) == 4 and sys.argv[1] == "redo":
        redo(sys.argv[2], sys.argv[3])
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
