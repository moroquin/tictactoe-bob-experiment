#!/usr/bin/env python3
"""Metrics for the Tic-Tac-Toe experiment: DDD vs. no DDD with IBM Bob.

Subcommands
  phase       isolation audit + Bob stats + acceptance tests + Open/Closed diff + vocabulary
  design      unit tests, coverage, mutation score, CK, jdeps and (optionally) SonarQube
  acceptance  only the acceptance tests (handy for the pilot)
  audit       only the isolation audit of a Bob stream-json log
  history-report  the history of one phase: harness numbers plus Bob's own account
  results-doc     the RESULTS.md that publish-run.sh puts in the branch: results and history of every phase
  live        a one-line-per-action view of a Bob stream-json log, read from stdin
  collect     merge results/<run>/F<n>*.json into results/summary.csv and print medians

Environment
  HARNESS_BUILD_CMD   build used before the acceptance tests (default: mvn -q -Dmaven.test.skip=true package)
  ACCEPTANCE_TIMEOUT  seconds per scenario (default 10)
  CK_JAR              path to CK's jar-with-dependencies; CK is skipped when unset
  SONAR_HOST_URL, SONAR_TOKEN   SonarQube is skipped unless both are set
  BOB_API_KEY         only used to check that the key never appears in a log; it is never printed
"""
from __future__ import annotations

import argparse
import base64
import csv
import json
import os
import re
import shlex
import statistics
import subprocess
import sys
import tempfile
import time
from datetime import datetime
import urllib.request
import xml.etree.ElementTree as ET
from pathlib import Path

DEFAULT_BUILD = "mvn -q -Dmaven.test.skip=true package"


# ----------------------------------------------------------------- helpers

def sh(cmd, cwd=None, timeout=1800):
    """Runs a command and returns (exit code, combined output)."""
    try:
        p = subprocess.run(cmd, cwd=cwd, capture_output=True, text=True, timeout=timeout,
                           shell=isinstance(cmd, str))
        return p.returncode, (p.stdout or "") + (p.stderr or "")
    except subprocess.TimeoutExpired:
        return 124, f"timeout after {timeout}s"
    except FileNotFoundError as exc:
        return 127, str(exc)


def write_json(path, data):
    Path(path).parent.mkdir(parents=True, exist_ok=True)
    Path(path).write_text(json.dumps(data, indent=2, sort_keys=True) + "\n", encoding="utf-8")


def inside(path, run_dir):
    """True when a path (absolute, ~, or relative to the run) stays inside the run folder."""
    p = path.strip().strip("'\"")
    if not p:
        return True
    if p in ("/dev/null", "/dev/stdout", "/dev/stderr"):
        return True
    if p.startswith("~"):
        return False
    root = Path(run_dir).resolve()
    candidate = Path(p) if p.startswith("/") else root / p
    try:
        return candidate.resolve().is_relative_to(root)
    except (OSError, RuntimeError):
        return False


def relative(path, run_dir):
    p = path.strip().strip("'\"")
    if p.startswith("/"):
        try:
            return str(Path(p).resolve().relative_to(Path(run_dir).resolve()))
        except ValueError:
            return p
    return p[2:] if p.startswith("./") else p


# ------------------------------------------------------- acceptance tests

def load_scenarios(path):
    scenarios = []
    for raw in Path(path).read_text(encoding="utf-8").splitlines():
        if not raw.strip() or raw.startswith("#"):
            continue
        cols = raw.split("\t") if "\t" in raw else re.split(r"\s{2,}", raw.strip(), maxsplit=7)
        sid, since, args, inputs, board, result, invalid, desc = cols
        scenarios.append({
            "id": sid, "since": int(since), "args": [] if args == "-" else args.split(),
            "inputs": inputs.split("|"), "board": board.split("/"), "result": result,
            "invalid": int(invalid), "desc": desc,
        })
    return scenarios


def run_scenario(jar, sc, timeout):
    stdin = "\n".join(sc["inputs"]) + "\n"
    try:
        p = subprocess.run(["java", "-jar", str(jar), *sc["args"]], input=stdin,
                           capture_output=True, text=True, timeout=timeout)
    except subprocess.TimeoutExpired:
        return {"id": sc["id"], "passed": False, "problems": [f"timeout after {timeout:g}s"]}
    shown = [line.strip() for line in p.stdout.replace("\r", "").split("\n") if line.strip()]
    invalid = sum(1 for line in shown if line.startswith("Invalid"))
    tail = shown[-4:]
    problems = []
    if p.returncode != 0:
        problems.append(f"exit status {p.returncode}")
    if len(tail) < 4 or tail[:3] != sc["board"]:
        problems.append(f"final board {tail[:3]} expected {sc['board']}")
    if not tail or tail[-1] != sc["result"]:
        problems.append(f"result line {tail[-1] if tail else None!r} expected {sc['result']!r}")
    if invalid != sc["invalid"]:
        problems.append(f"{invalid} 'Invalid' lines, expected {sc['invalid']}")
    return {"id": sc["id"], "passed": not problems, "problems": problems}


def acceptance(run_dir, phase, scenarios_path, skip_build=False):
    run_dir = Path(run_dir)
    res = {"acceptance_build_ok": True}
    if not skip_build:
        code, out = sh(os.environ.get("HARNESS_BUILD_CMD", DEFAULT_BUILD), cwd=run_dir, timeout=1800)
        res["acceptance_build_ok"] = code == 0
        if code != 0:
            res["acceptance_build_log_tail"] = out[-2000:]
    jar = run_dir / "target" / "tictactoe.jar"
    timeout = float(os.environ.get("ACCEPTANCE_TIMEOUT", "10"))
    results = []
    for sc in load_scenarios(scenarios_path):
        if sc["since"] > phase:
            continue
        if jar.exists():
            results.append(run_scenario(jar, sc, timeout))
        else:
            results.append({"id": sc["id"], "passed": False, "problems": ["target/tictactoe.jar not found"]})
    res["acceptance_total"] = len(results)
    res["acceptance_passed"] = sum(r["passed"] for r in results)
    for ph in (1, 2, 3):
        sub = [r for r in results if r["id"].startswith(f"F{ph}-")]
        if sub:
            res[f"acceptance_F{ph}_passed"] = sum(r["passed"] for r in sub)
            res[f"acceptance_F{ph}_total"] = len(sub)
    res["acceptance_failures"] = [r for r in results if not r["passed"]]
    return res


# ------------------------------------------------------------ Bob logs

EDIT_TOOLS = {"write_file", "write_to_file", "apply_diff", "insert_content", "search_and_replace",
              "edit_file", "replace_in_file"}
READ_TOOLS = {"read_file", "list_files", "search_files", "list_code_definition_names",
              "codebase_search", "getsymbolsoverview", "get_symbols_overview"}
READ_COMMANDS = {"cat", "head", "tail", "less", "grep", "rg", "find", "ls", "tree", "wc", "sed", "awk"}


def read_events(log):
    events = []
    p = Path(log)
    if not p.exists():
        return events
    for line in p.read_text(encoding="utf-8", errors="replace").splitlines():
        line = line.strip()
        if not line.startswith("{"):
            continue
        try:
            ev = json.loads(line)
        except json.JSONDecodeError:
            continue
        if isinstance(ev, dict):
            events.append(ev)
    return events


def ev_type(ev):
    return str(ev.get("type") or ev.get("event") or "")


def tool_name(ev):
    return str(ev.get("tool_name") or ev.get("name") or ev.get("tool") or "")


def tool_params(ev):
    p = ev.get("parameters", ev.get("input", ev.get("args", {})))
    if isinstance(p, str):
        try:
            p = json.loads(p)
        except json.JSONDecodeError:
            p = {"value": p}
    return p if isinstance(p, dict) else {"value": p}


def tool_kind(name):
    n = name.lower()
    if n in EDIT_TOOLS or any(k in n for k in ("write", "apply", "insert", "replace", "edit")):
        return "edit"
    if "command" in n or n.startswith("exec"):
        return "command"
    if n in READ_TOOLS or any(k in n for k in ("read", "list", "search", "symbol")):
        return "read"
    return "other"


def command_of(params):
    for key in ("command", "cmd"):
        if isinstance(params.get(key), str):
            return params[key]
    return ""


def paths_of(params):
    out = []
    for key, value in params.items():
        k = key.lower()
        if not any(t in k for t in ("path", "file", "dir", "cwd")):
            continue
        if isinstance(value, str):
            out.append(value)
        elif isinstance(value, list):
            out.extend(v for v in value if isinstance(v, str))
    return out


def command_segments(cmd):
    """First word of every simple command in a shell line (skipping VAR=value)."""
    firsts = []
    for segment in re.split(r"&&|\|\||[;|()\n`]", cmd):
        try:
            words = shlex.split(segment, posix=True)
        except ValueError:
            words = segment.split()
        while words and re.match(r"^[A-Za-z_][A-Za-z0-9_]*=", words[0]):
            words = words[1:]
        while words and words[0] in ("sudo", "command", "exec", "time", "nohup", "xargs"):
            words = words[1:]
        if words:
            firsts.append(os.path.basename(words[0]))
    return firsts


def command_tokens(cmd):
    try:
        words = shlex.split(cmd, posix=True)
    except ValueError:
        words = cmd.split()
    tokens = []
    for w in words:
        tokens.extend(t for t in re.split(r"[=,;|&<>()]", w) if t)
    return tokens


def is_read_command(ev):
    if tool_kind(tool_name(ev)) != "command":
        return False
    firsts = command_segments(command_of(tool_params(ev)))
    return bool(firsts) and all(f in READ_COMMANDS for f in firsts)


def audit(log, run_dir):
    violations = []
    for i, ev in enumerate(read_events(log)):
        if ev_type(ev) != "tool_use":
            continue
        name, params = tool_name(ev), tool_params(ev)
        cmd = command_of(params)
        if cmd:
            firsts = command_segments(cmd)
            for word, label in (("git", "git command"), ("curl", "network command"), ("wget", "network command"),
                                ("env", "environment variables"), ("printenv", "environment variables")):
                if word in firsts:
                    violations.append({"event": i, "tool": name, "rule": label, "detail": cmd[:200]})
            if re.search(r"\$\{?BOB_", cmd):
                violations.append({"event": i, "tool": name, "rule": "environment variables", "detail": cmd[:200]})
            for tok in command_tokens(cmd):
                if (tok.startswith(("/", "~")) or ".." in tok) and not inside(tok, run_dir):
                    violations.append({"event": i, "tool": name, "rule": "path outside the workspace", "detail": tok[:200]})
        for p in paths_of(params):
            if not inside(p, run_dir):
                violations.append({"event": i, "tool": name, "rule": "path outside the workspace", "detail": p[:200]})
    key = os.environ.get("BOB_API_KEY", "")
    if len(key) >= 8 and Path(log).exists() and key in Path(log).read_text(encoding="utf-8", errors="replace"):
        violations.append({"event": None, "tool": None, "rule": "BOB_API_KEY value appears in the log",
                           "detail": "<redacted>"})
    return {"audit_violations": len(violations), "audit_details": violations[:50]}


def bobstats(log, run_dir):
    events = read_events(log)
    res = {}
    result = next((ev for ev in reversed(events) if ev_type(ev) == "result"), None)
    if result is None:
        res["bob_status"] = "no result event"
    else:
        res["bob_status"] = result.get("status")
        for key, value in (result.get("stats") or {}).items():
            if value is None or isinstance(value, (int, float, str)):
                res[f"bob_{key}"] = value
    res["bob_error_events"] = sum(1 for ev in events if ev_type(ev) == "error")

    uses = [ev for ev in events if ev_type(ev) == "tool_use"]
    results_by_id = {str(ev.get("tool_id")): ev for ev in events if ev_type(ev) == "tool_result"}
    kinds = [tool_kind(tool_name(ev)) for ev in uses]
    res["tool_uses"] = len(uses)
    for kind in ("read", "edit", "command", "other"):
        res[f"tool_uses_{kind}"] = kinds.count(kind)

    first_edit = next((i for i, k in enumerate(kinds) if k == "edit"), len(uses))
    res["tool_uses_before_first_edit"] = first_edit
    res["reads_before_first_edit"] = sum(
        1 for ev in uses[:first_edit] if tool_kind(tool_name(ev)) == "read" or is_read_command(ev))

    def touched(ev):
        return [relative(p, run_dir) for p in paths_of(tool_params(ev))]

    def doc_like(p):
        return p.startswith("docs/") or p in ("AGENTS.md", "REQUIREMENTS.md")

    res["doc_reads"] = sum(1 for ev in uses if tool_kind(tool_name(ev)) == "read" and any(doc_like(p) for p in touched(ev)))
    edits = [(i, p) for i, ev in enumerate(uses) if tool_kind(tool_name(ev)) == "edit" for p in touched(ev)]
    first_doc = next((i for i, p in edits if p.startswith("docs/")), None)
    first_code = next((i for i, p in edits if p.startswith("src/")), None)
    res["docs_edited"] = first_doc is not None
    res["docs_before_code"] = first_doc is not None and (first_code is None or first_doc < first_code)

    builds = [ev for ev in uses if tool_kind(tool_name(ev)) == "command"
              and any(f in ("mvn", "mvnw") for f in command_segments(command_of(tool_params(ev))))]
    failed = 0
    for ev in builds:
        r = results_by_id.get(str(ev.get("tool_id")))
        if r is None:
            continue
        text = json.dumps(r)
        if str(r.get("status", "")).lower() in ("error", "failed", "failure") or "BUILD FAILURE" in text or "[ERROR]" in text:
            failed += 1
    res["builds"] = len(builds)
    res["builds_failed"] = failed
    return res


# ------------------------------------------------- Open/Closed (git diff)

def git(run_dir, *args):
    code, out = sh(["git", "-C", str(run_dir), *args], timeout=120)
    if code != 0:
        raise RuntimeError(out)
    return out


def ocp(run_dir, from_ref, to_ref):
    res = {k: 0 for k in ("main_added", "main_modified", "main_deleted",
                          "test_added", "test_modified", "test_deleted", "docs_changed")}
    for line in git(run_dir, "diff", "--name-status", "-M", from_ref, to_ref).splitlines():
        parts = line.split("\t")
        if len(parts) < 2:
            continue
        status, path = parts[0][0], parts[-1]
        if path.startswith("docs/"):
            res["docs_changed"] += 1
            continue
        if path.startswith("src/main/java/") and path.endswith(".java"):
            kind = "main"
        elif path.startswith("src/test/") and path.endswith(".java"):
            kind = "test"
        else:
            continue
        if status == "A":
            res[f"{kind}_added"] += 1
        elif status == "D":
            res[f"{kind}_deleted"] += 1
        else:
            res[f"{kind}_modified"] += 1
    churn = 0
    for line in git(run_dir, "diff", "--numstat", "-M", "--diff-filter=MRD", from_ref, to_ref,
                    "--", "src/main/java").splitlines():
        added, deleted = line.split("\t")[:2]
        if added.isdigit() and deleted.isdigit():
            churn += int(added) + int(deleted)
    res["churn_existing_main"] = churn
    total = res["main_added"] + res["main_modified"]
    res["ocp_ratio"] = round(res["main_added"] / total, 3) if total else None
    res["existing_tests_touched"] = res["test_modified"] + res["test_deleted"]
    return res


# ------------------------------------------------------------ vocabulary

DISCARDED = ["cell", "tile", "grid", "matrix", "mark", "token", "symbol", "stone", "spot", "turn", "tie",
             "victory", "position", "user", "participant", "computer", "bot", "cpu", "ai"]
MODEL_TERMS = ["board", "square", "coordinate", "piece", "color", "player", "round", "put", "goal", "win", "draw"]
JAVA_KEYWORDS = set("""abstract assert boolean break byte case catch char class const continue default do double else
enum extends final finally float for goto if implements import instanceof int interface long native new package private
protected public return short static strictfp super switch synchronized this throw throws transient try void volatile
while var record yield sealed permits true false null""".split())
WORD = re.compile(r"[A-Z]+(?=[A-Z][a-z]|\d|\b|_)|[A-Z]?[a-z]+|\d+")


def strip_java(src):
    src = re.sub(r"/\*.*?\*/", " ", src, flags=re.S)
    src = re.sub(r"//[^\n]*", " ", src)
    src = re.sub(r'"""(.*?)"""', " ", src, flags=re.S)
    src = re.sub(r'"(\\.|[^"\\\n])*"', " ", src)
    return re.sub(r"'(\\.|[^'\\\n])+'", " ", src)


def norm(word):
    return word[:-1] if len(word) > 3 and word.endswith("s") and not word.endswith("ss") else word


def vocab(run_dir, phase):
    discarded = DISCARDED + (["move"] if phase < 3 else [])
    terms = MODEL_TERMS + (["human", "pc"] if phase >= 2 else []) + (["move"] if phase >= 3 else [])
    counts = {w: 0 for w in discarded}
    seen = set()
    io_files = 0
    for f in Path(run_dir, "src", "main", "java").rglob("*.java"):
        src = f.read_text(encoding="utf-8", errors="replace")
        if re.search(r"System\.(out|err|in)\b|\bScanner\b|\bjava\.io\.Console\b", src):
            io_files += 1
        for ident in re.findall(r"\b[A-Za-z_$][A-Za-z0-9_$]*\b", strip_java(src)):
            if ident in JAVA_KEYWORDS:
                continue
            for w in (norm(x.lower()) for x in WORD.findall(ident)):
                seen.add(w)
                if w in counts:
                    counts[w] += 1
    found = {w: n for w, n in counts.items() if n}
    missing = [t for t in terms if t not in seen]
    return {
        "discarded_terms": sum(found.values()),
        "discarded_terms_detail": json.dumps(found, sort_keys=True),
        "model_terms_present": len(terms) - len(missing),
        "model_terms_total": len(terms),
        "model_terms_missing": " ".join(missing),
        "io_classes": io_files,
    }


# --------------------------------------------------------- design metrics

def surefire_and_jacoco(run_dir):
    res = {}
    code, out = sh("mvn -q -Dmaven.test.failure.ignore=true verify", cwd=run_dir, timeout=3600)
    res["verify_exit"] = code
    if code != 0:
        res["verify_log_tail"] = out[-1500:]
    tests = failures = 0
    for f in Path(run_dir, "target", "surefire-reports").glob("TEST-*.xml"):
        try:
            root = ET.parse(f).getroot()
        except ET.ParseError:
            continue
        tests += int(root.get("tests", 0))
        failures += int(root.get("failures", 0)) + int(root.get("errors", 0))
    res["unit_tests"] = tests
    res["unit_failures"] = failures
    report = Path(run_dir, "target", "site", "jacoco", "jacoco.csv")
    if report.exists():
        lm = lc = bm = bc = 0
        with report.open(encoding="utf-8") as fh:
            for row in csv.DictReader(fh):
                lm += int(row["LINE_MISSED"])
                lc += int(row["LINE_COVERED"])
                bm += int(row["BRANCH_MISSED"])
                bc += int(row["BRANCH_COVERED"])
        res["line_coverage"] = round(100 * lc / (lm + lc), 1) if lm + lc else None
        res["branch_coverage"] = round(100 * bc / (bm + bc), 1) if bm + bc else None
    return res


def pit(run_dir):
    code, out = sh("mvn -q org.pitest:pitest-maven:mutationCoverage", cwd=run_dir, timeout=3600)
    report = Path(run_dir, "target", "pit-reports", "mutations.xml")
    if not report.exists():
        return {"mutation_error": out[-1500:]}
    mutations = ET.parse(report).getroot().findall("mutation")
    killed = sum(1 for m in mutations if m.get("detected") == "true")
    return {"mutants": len(mutations), "mutants_killed": killed,
            "mutation_score": round(100 * killed / len(mutations), 1) if mutations else None}


def ck(run_dir, ck_jar):
    out_dir = Path(tempfile.mkdtemp(prefix="ck-"))
    code, out = sh(["java", "-jar", ck_jar, str(Path(run_dir, "src", "main", "java")), "false", "0", "false",
                    str(out_dir) + os.sep], timeout=900)
    class_csv = next(iter(sorted(out_dir.glob("*class.csv"))), None)
    if class_csv is None:
        return {"ck_error": out[-1500:]}
    with class_csv.open(encoding="utf-8") as fh:
        rows = list(csv.DictReader(fh))

    def values(col):
        vals = []
        for row in rows:
            try:
                v = float(row.get(col, ""))
            except ValueError:
                continue
            if v == v and v >= 0:  # skip NaN and the -1 that CK uses for "not applicable"
                vals.append(v)
        return vals

    res = {"ck_classes": len(rows)}
    for col, label in (("cbo", "cbo"), ("fanout", "fanout"), ("wmc", "wmc"), ("rfc", "rfc"), ("lcom*", "lcom_star")):
        vals = values(col)
        if vals:
            res[f"ck_{label}_median"] = round(statistics.median(vals), 3)
            res[f"ck_{label}_max"] = round(max(vals), 3)
    vals = values("tcc")
    if vals:
        res["ck_tcc_median"] = round(statistics.median(vals), 3)
        res["ck_tcc_min"] = round(min(vals), 3)
    return res


def jdeps(run_dir):
    classes = Path(run_dir, "target", "classes")
    if not classes.exists():
        return {"jdeps_error": "target/classes is missing"}
    code, out = sh(["jdeps", "-verbose:package", str(classes)], timeout=300)
    edges = set()
    for line in out.splitlines():
        m = re.match(r"\s+(\S+)\s+->\s+(\S+)", line)
        if m and m.group(1).startswith("tictactoe") and m.group(2).startswith("tictactoe") and m.group(1) != m.group(2):
            edges.add((m.group(1), m.group(2)))
    packages = {".".join(p.relative_to(classes).parent.parts) or "(default)" for p in classes.rglob("*.class")}
    ce = {p: len({b for a, b in edges if a == p}) for p in packages}
    ca = {p: len({a for a, b in edges if b == p}) for p in packages}
    instability = {p: round(ce[p] / (ca[p] + ce[p]), 2) if ca[p] + ce[p] else None for p in packages}
    return {
        "packages": len(packages),
        "package_edges": len(edges),
        "package_cycles": sum(1 for a, b in edges if (b, a) in edges) // 2,
        "package_ce_max": max(ce.values(), default=0),
        "package_instability": json.dumps(instability, sort_keys=True),
    }


def sonar(run_dir, key):
    host = os.environ["SONAR_HOST_URL"].rstrip("/")
    code, out = sh(f"mvn -q org.sonarsource.scanner.maven:sonar-maven-plugin:sonar "
                   f"-Dsonar.projectKey={key} -Dsonar.host.url={host}", cwd=run_dir, timeout=1800)
    if code != 0:
        return {"sonar_error": out[-1500:]}
    auth = "Basic " + base64.b64encode(f"{os.environ['SONAR_TOKEN']}:".encode()).decode()

    def get(url):
        req = urllib.request.Request(url, headers={"Authorization": auth})
        with urllib.request.urlopen(req, timeout=30) as resp:
            return json.load(resp)

    task = Path(run_dir, "target", "sonar", "report-task.txt")
    props = dict(line.split("=", 1) for line in task.read_text().splitlines() if "=" in line) if task.exists() else {}
    if props.get("ceTaskUrl"):
        for _ in range(120):
            if get(props["ceTaskUrl"]).get("task", {}).get("status") in ("SUCCESS", "FAILED", "CANCELED"):
                break
            time.sleep(5)
    keys = ("ncloc,complexity,cognitive_complexity,duplicated_lines_density,code_smells,"
            "sqale_index,sqale_rating,coverage")
    data = get(f"{host}/api/measures/component?component={key}&metricKeys={keys}")
    return {f"sonar_{m['metric']}": m.get("value") for m in data.get("component", {}).get("measures", [])}


def design(run_dir, run_id, phase):
    res = surefire_and_jacoco(run_dir)
    res.update(pit(run_dir))
    if os.environ.get("CK_JAR"):
        res.update(ck(run_dir, os.environ["CK_JAR"]))
    res.update(jdeps(run_dir))
    if os.environ.get("SONAR_HOST_URL") and os.environ.get("SONAR_TOKEN"):
        res.update(sonar(run_dir, f"ttt-{run_id}-F{phase}"))
    return res


# ------------------------------------------------------- session history

HISTORY_TYPES = ["ambiguity", "gap", "design", "naming", "scope", "deviation", "workaround"]
PHASE_TITLES = {1: "Build version 1", 2: "CR-1, Human vs. PC", 3: "CR-2, three Pieces each"}


def md_section(text, title):
    """Body of the '## title' section, up to the next '## ' heading."""
    m = re.search(rf"^##\s+{re.escape(title)}\s*$(.*?)(?=^##\s|\Z)", text, re.M | re.S | re.I)
    return m.group(1) if m else ""


def table_rows(body, prefix):
    rows = []
    for line in body.splitlines():
        if not line.strip().startswith("|"):
            continue
        cells = [c.strip() for c in line.strip().strip("|").split("|")]
        if cells and re.fullmatch(rf"{prefix}\d+", cells[0].strip("`* ")):
            rows.append(cells)
    return rows


def parse_history(path, where=""):
    res = {"history_found": False, "history_where": where or ""}
    p = Path(path) if path else None
    if p is None or not p.exists() or not p.read_text(encoding="utf-8", errors="replace").strip():
        return res
    text = p.read_text(encoding="utf-8", errors="replace")
    res["history_found"] = True
    res["history_path_ok"] = where == "history/session.md"
    m = re.search(r"\b(partially done|done|blocked)\b", md_section(text, "Result"), re.I)
    res["history_result"] = m.group(1).lower() if m else "unknown"
    decisions = table_rows(md_section(text, "Decisions I made on my own"), "A")
    res["history_decisions"] = len(decisions)
    for t in HISTORY_TYPES:
        res[f"history_decisions_{t}"] = sum(1 for r in decisions if len(r) > 1 and r[1].strip("`* ").lower() == t)
    res["history_decisions_other"] = res["history_decisions"] - sum(res[f"history_decisions_{t}"] for t in HISTORY_TYPES)
    res["history_pending"] = len(table_rows(md_section(text, "Pending"), "P"))
    res["history_docs_relied"] = sum(1 for line in md_section(text, "Documentation I relied on").splitlines()
                                     if re.match(r"\s*([-*]|\d+\.)\s+\S", line))
    timing = md_section(text, "Timing")
    for key, label in (("history_bob_start", "Start"), ("history_bob_end", "End")):
        m = re.search(rf"{label}:\s*`?(\d{{4}}-\d\d-\d\dT\d\d:\d\d:\d\dZ)", timing)
        res[key] = m.group(1) if m else ""
    return res


def wall_clock(started, ended):
    res = {"started_at": started or "", "ended_at": ended or ""}
    try:
        fmt = "%Y-%m-%dT%H:%M:%SZ"
        res["wall_seconds"] = int((datetime.strptime(ended, fmt) - datetime.strptime(started, fmt)).total_seconds())
    except (TypeError, ValueError):
        res["wall_seconds"] = None
    return res


def _num(v):
    try:
        return float(v)
    except (TypeError, ValueError):
        return None


def _int(v):
    n = _num(v)
    return "—" if n is None else f"{int(n):,}"


def _val(v, suffix=""):
    return "—" if v in (None, "") else f"{v}{suffix}"


def _duration(data):
    ms = _num(data.get("bob_duration_ms"))
    seconds = int(ms / 1000) if ms is not None else data.get("wall_seconds")
    return "—" if seconds is None else f"{seconds // 60} min {seconds % 60:02d} s"


def _phase_data(out_dir, phase):
    data = {}
    for name in (f"F{phase}.json", f"F{phase}-design.json"):
        f = Path(out_dir) / name
        if f.exists():
            data.update(json.loads(f.read_text(encoding="utf-8")))
    return data


def _demote_headings(text, levels=1):
    lines, fenced = [], False
    for line in text.splitlines():
        if line.lstrip().startswith("```"):
            fenced = not fenced
        lines.append("#" * levels + line if line.startswith("#") and not fenced else line)
    return "\n".join(lines)


def history_report(out_dir, run_id, phase):
    out_dir = Path(out_dir)
    d = _phase_data(out_dir, phase)
    raw = out_dir / "history" / f"F{phase}.bob.md"
    bob_text = raw.read_text(encoding="utf-8", errors="replace").strip() if raw.exists() else ""
    rows = [
        ("Started → ended (UTC), measured by the harness", f"{_val(d.get('started_at'))} → {_val(d.get('ended_at'))}"),
        ("Duration of the Bob session", _duration(d)),
        ("Tokens: total · input · output · cache read",
         " · ".join(_int(d.get(k)) for k in ("bob_total_tokens", "bob_input_tokens", "bob_output_tokens",
                                             "bob_cache_read_tokens"))),
        ("Cost (Bobcoins)", _val(d.get("bob_session_costs"))),
        ("Tool calls · reads before the first edit", f"{_val(d.get('tool_uses'))} · {_val(d.get('reads_before_first_edit'))}"),
        ("Builds · failed builds", f"{_val(d.get('builds'))} · {_val(d.get('builds_failed'))}"),
        ("Acceptance tests passed", f"{_val(d.get('acceptance_passed'))} of {_val(d.get('acceptance_total'))}"),
        ("Classes added · classes modified · Open/Closed ratio",
         f"{_val(d.get('main_added'))} · {_val(d.get('main_modified'))} · {_val(d.get('ocp_ratio'))}"),
        ("Lines changed in existing classes · existing tests touched",
         f"{_val(d.get('churn_existing_main'))} · {_val(d.get('existing_tests_touched'))}"),
        ("Decisions Bob made on its own · pending items",
         f"{_val(d.get('history_decisions'))} · {_val(d.get('history_pending'))}"),
        ("Discarded terms in the code · files with console I/O",
         f"{_val(d.get('discarded_terms'))} · {_val(d.get('io_classes'))}"),
        ("Line coverage · mutation score", f"{_val(d.get('line_coverage'), '%')} · {_val(d.get('mutation_score'), '%')}"),
        ("CBO median · LCOM* median", f"{_val(d.get('ck_cbo_median'))} · {_val(d.get('ck_lcom_star_median'))}"),
        ("Isolation violations", _val(d.get("audit_violations"))),
    ]
    parts = [f"# {run_id} · F{phase} · {PHASE_TITLES.get(phase, '')}", "",
             "Numbers measured by the harness from Bob's own stats and the repository. Bob's account of the session follows.",
             "", "| Measure | Value |", "|---|---|"]
    parts += [f"| {k} | {v} |" for k, v in rows]
    parts += ["", "---", ""]
    if not bob_text:
        parts.append("_Bob did not write `history/session.md` in this session._")
    else:
        if d.get("history_where") and d.get("history_where") != "history/session.md":
            parts += [f"_Bob wrote this history at `{d['history_where']}` instead of `history/session.md`._", ""]
        parts.append(_demote_headings(bob_text))
    target = out_dir / "history" / f"F{phase}.md"
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text("\n".join(parts) + "\n", encoding="utf-8")
    return target


RESULT_ROWS = [
    ("Started (UTC)", lambda d: _val(d.get("started_at"))),
    ("Duration of the Bob session", _duration),
    ("Tokens, total", lambda d: _int(d.get("bob_total_tokens"))),
    ("Tokens: input · output · cache read",
     lambda d: " · ".join(_int(d.get(k)) for k in ("bob_input_tokens", "bob_output_tokens", "bob_cache_read_tokens"))),
    ("Cost (Bobcoins)", lambda d: _val(d.get("bob_session_costs"))),
    ("Tool calls", lambda d: _val(d.get("tool_uses"))),
    ("Reads before the first edit", lambda d: _val(d.get("reads_before_first_edit"))),
    ("Builds · failed builds", lambda d: f"{_val(d.get('builds'))} · {_val(d.get('builds_failed'))}"),
    ("Acceptance tests passed", lambda d: f"{_val(d.get('acceptance_passed'))} of {_val(d.get('acceptance_total'))}"),
    ("Decisions Bob made on its own", lambda d: _val(d.get("history_decisions"))),
    ("Pending items", lambda d: _val(d.get("history_pending"))),
    ("Classes added · modified", lambda d: f"{_val(d.get('main_added'))} · {_val(d.get('main_modified'))}"),
    ("Open/Closed ratio", lambda d: _val(d.get("ocp_ratio"))),
    ("Lines changed in existing classes", lambda d: _val(d.get("churn_existing_main"))),
    ("Existing tests touched", lambda d: _val(d.get("existing_tests_touched"))),
    ("Discarded terms in the code", lambda d: _val(d.get("discarded_terms"))),
    ("Files with console I/O", lambda d: _val(d.get("io_classes"))),
    ("Line coverage · mutation score",
     lambda d: f"{_val(d.get('line_coverage'), '%')} · {_val(d.get('mutation_score'), '%')}"),
    ("CBO median · LCOM* median", lambda d: f"{_val(d.get('ck_cbo_median'))} · {_val(d.get('ck_lcom_star_median'))}"),
    ("Isolation violations", lambda d: _val(d.get("audit_violations"))),
]


def results_document(out_dir, run_id):
    """RESULTS.md for the branch: one table with every phase, then Bob's account of each session."""
    out_dir = Path(out_dir)
    meta = {}
    if (out_dir / "run.txt").exists():
        meta = dict(l.split("=", 1) for l in (out_dir / "run.txt").read_text().splitlines() if "=" in l)
    branch = meta.get("branch", "?")
    phases = [ph for ph in (1, 2, 3) if _phase_data(out_dir, ph)]
    data = {ph: _phase_data(out_dir, ph) for ph in phases}
    lines = [
        f"# Results and history: {branch}", "",
        f"IBM Bob wrote the Tic-Tac-Toe in this branch, one phase per session (run `{run_id}`). Every session was "
        f"new, without memory of the previous ones, and started from `REQUIREMENTS.md`"
        + (" and the DDD documentation in `docs/domain/`." if branch == "with-ddd" else "."),
        "The table was measured by the experiment's harness. Each phase's account below was written by Bob "
        "at the end of its session, following `.bob/rules/02-history.md`.", "",
        "## Play this version", "",
        "```bash",
        "mvn -q package",
        "java -jar target/tictactoe.jar                  # two people",
        "java -jar target/tictactoe.jar --vs-pc          # against the PC (after CR-1)",
        "java -jar target/tictactoe.jar --three-pieces   # three Pieces each (after CR-2)",
        "```", "",
        "## Results", "",
        "| | " + " | ".join(f"F{ph} · {PHASE_TITLES.get(ph, '')}" for ph in phases) + " |",
        "|---|" + "---|" * len(phases),
    ]
    for label, fn in RESULT_ROWS:
        lines.append(f"| {label} | " + " | ".join(fn(data[ph]) for ph in phases) + " |")
    for ph in phases:
        lines += ["", f"## F{ph} · {PHASE_TITLES.get(ph, '')}: Bob's account", ""]
        raw = out_dir / "history" / f"F{ph}.bob.md"
        text = raw.read_text(encoding="utf-8", errors="replace").strip() if raw.exists() else ""
        where = data[ph].get("history_where")
        if not text:
            lines.append("_Bob did not write `history/session.md` in this session._")
            continue
        if where and where != "history/session.md":
            lines += [f"_Bob wrote this history at `{where}` instead of `history/session.md`._", ""]
        lines.append(_demote_headings(text, levels=2))
    return "\n".join(lines) + "\n"


def live(prefix):
    """Prints one line per Bob action while a stream-json log is being written."""
    for raw in sys.stdin:
        raw = raw.strip()
        if not raw.startswith("{"):
            continue
        try:
            ev = json.loads(raw)
        except json.JSONDecodeError:
            continue
        kind = ev_type(ev)
        if kind == "tool_use":
            params = tool_params(ev)
            what = command_of(params) or next(iter(paths_of(params)), "")
            print(f"{prefix} {tool_name(ev)} {what[:110]}", flush=True)
        elif kind == "error":
            print(f"{prefix} error: {str(ev.get('message') or ev.get('error') or ev)[:200]}", flush=True)
        elif kind == "result":
            stats = ev.get("stats") or {}
            print(f"{prefix} finished: {ev.get('status')}, {stats.get('total_tokens')} tokens, "
                  f"{stats.get('session_costs')} Bobcoins", flush=True)


# --------------------------------------------------------------- collect

KEY_METRICS = ["bob_total_tokens", "bob_session_costs", "bob_duration_ms", "tool_uses",
               "tool_uses_before_first_edit", "builds_failed", "acceptance_passed", "acceptance_total",
               "main_modified", "ocp_ratio", "churn_existing_main", "existing_tests_touched",
               "discarded_terms", "io_classes", "history_decisions", "history_pending",
               "ck_cbo_median", "ck_lcom_star_median", "mutation_score"]


def collect(results_dir, quiet=False):
    results_dir = Path(results_dir)
    rows = []
    for run in sorted(p for p in results_dir.iterdir() if p.is_dir()):
        meta = {}
        if (run / "run.txt").exists():
            meta = dict(l.split("=", 1) for l in (run / "run.txt").read_text().splitlines() if "=" in l)
        for ph in (1, 2, 3):
            row = {}
            for name in (f"F{ph}.json", f"F{ph}-design.json"):
                if (run / name).exists():
                    row.update(json.loads((run / name).read_text(encoding="utf-8")))
            if not row:
                continue
            row = {k: v for k, v in row.items()
                   if not isinstance(v, (list, dict)) and not k.endswith(("_tail", "_error"))}
            row.update({"run_id": run.name, "branch": meta.get("branch", ""), "phase": ph})
            rows.append(row)
    keys = ["run_id", "branch", "phase"] + sorted({k for r in rows for k in r} - {"run_id", "branch", "phase"})
    tmp = results_dir / f".summary.{os.getpid()}.csv"
    with tmp.open("w", newline="", encoding="utf-8") as fh:
        writer = csv.DictWriter(fh, fieldnames=keys)
        writer.writeheader()
        writer.writerows(rows)
    os.replace(tmp, results_dir / "summary.csv")
    print(f"{results_dir / 'summary.csv'}: {len(rows)} rows")
    if quiet:
        return

    def num(v):
        try:
            return float(v)
        except (TypeError, ValueError):
            return None

    invalid = sorted({f"{r['run_id']} F{r['phase']}" for r in rows if (num(r.get("audit_violations")) or 0) > 0})
    if invalid:
        print("Left out of the medians because of isolation violations: " + ", ".join(invalid))
    rows = [r for r in rows if not (num(r.get("audit_violations")) or 0) > 0]

    for ph in sorted({r["phase"] for r in rows}):
        print(f"\nPhase F{ph} — median per branch (number of runs in brackets)")
        branches = sorted({r["branch"] for r in rows if r["phase"] == ph})
        print(f"{'metric':32}" + "".join(f"{b:>18}" for b in branches))
        for metric in KEY_METRICS:
            cells = []
            for b in branches:
                vals = [num(r.get(metric)) for r in rows if r["phase"] == ph and r["branch"] == b]
                vals = [v for v in vals if v is not None]
                cells.append(f"{statistics.median(vals):>14.6g} [{len(vals)}]" if vals else f"{'—':>18}")
            print(f"{metric:32}" + "".join(cells))


# ------------------------------------------------------------------ CLI

def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = ap.add_subparsers(dest="cmd", required=True)

    p = sub.add_parser("phase")
    p.add_argument("--run-dir", required=True)
    p.add_argument("--run-id", required=True)
    p.add_argument("--phase", type=int, required=True)
    p.add_argument("--log", required=True)
    p.add_argument("--scenarios", required=True)
    p.add_argument("--bob-exit", type=int, default=None)
    p.add_argument("--skip-build", action="store_true")
    p.add_argument("--history", default="", help="the history Bob wrote in this session")
    p.add_argument("--history-where", default="", help="where Bob wrote it, relative to the run folder")
    p.add_argument("--started", default="", help="UTC start of the session, YYYY-MM-DDTHH:MM:SSZ")
    p.add_argument("--ended", default="", help="UTC end of the session, YYYY-MM-DDTHH:MM:SSZ")
    p.add_argument("--out", required=True)

    d = sub.add_parser("design")
    d.add_argument("--run-dir", required=True)
    d.add_argument("--run-id", required=True)
    d.add_argument("--phase", type=int, required=True)
    d.add_argument("--out", required=True)

    a = sub.add_parser("acceptance")
    a.add_argument("--run-dir", required=True)
    a.add_argument("--phase", type=int, required=True)
    a.add_argument("--scenarios", required=True)
    a.add_argument("--skip-build", action="store_true")

    u = sub.add_parser("audit")
    u.add_argument("--log", required=True)
    u.add_argument("--run-dir", required=True)

    h = sub.add_parser("history-report")
    h.add_argument("--results-run", required=True, help="results/<run-id>")
    h.add_argument("--run-id", required=True)
    h.add_argument("--phase", type=int, required=True)

    i = sub.add_parser("results-doc")
    i.add_argument("--results-run", required=True, help="results/<run-id>")
    i.add_argument("--run-id", required=True)
    i.add_argument("--out", required=True)

    v = sub.add_parser("live")
    v.add_argument("--prefix", default="")

    c = sub.add_parser("collect")
    c.add_argument("--results", required=True)
    c.add_argument("--quiet", action="store_true", help="only write summary.csv")

    args = ap.parse_args()
    if args.cmd == "phase":
        res = {"run_id": args.run_id, "phase": args.phase, "bob_exit": args.bob_exit}
        res.update(audit(args.log, args.run_dir))
        res.update(bobstats(args.log, args.run_dir))
        res.update(acceptance(args.run_dir, args.phase, args.scenarios, args.skip_build))
        res.update(ocp(args.run_dir, f"p{args.phase - 1}", f"p{args.phase}"))
        res.update(vocab(args.run_dir, args.phase))
        res.update(parse_history(args.history, args.history_where))
        res.update(wall_clock(args.started, args.ended))
        write_json(args.out, res)
        print(f"[{args.run_id} F{args.phase}] tokens={res.get('bob_total_tokens')} cost={res.get('bob_session_costs')} "
              f"acceptance={res['acceptance_passed']}/{res['acceptance_total']} "
              f"ocp_ratio={res['ocp_ratio']} discarded_terms={res['discarded_terms']} "
              f"decisions={res.get('history_decisions', '—')} pending={res.get('history_pending', '—')}")
        if not res["history_found"]:
            print("NOTE: Bob did not write history/session.md in this session.")
        if res["audit_violations"]:
            print(f"WARNING: {res['audit_violations']} isolation violations — discard this run. "
                  f"See audit_details in {args.out}")
    elif args.cmd == "design":
        res = design(args.run_dir, args.run_id, args.phase)
        write_json(args.out, res)
        print(f"[{args.run_id} F{args.phase}] design metrics -> {args.out}")
    elif args.cmd == "acceptance":
        res = acceptance(args.run_dir, args.phase, args.scenarios, args.skip_build)
        print(json.dumps(res, indent=2))
    elif args.cmd == "audit":
        print(json.dumps(audit(args.log, args.run_dir), indent=2))
    elif args.cmd == "history-report":
        print(history_report(args.results_run, args.run_id, args.phase))
    elif args.cmd == "results-doc":
        Path(args.out).write_text(results_document(args.results_run, args.run_id), encoding="utf-8")
        print(args.out)
    elif args.cmd == "live":
        live(args.prefix)
    elif args.cmd == "collect":
        collect(args.results, args.quiet)


if __name__ == "__main__":
    try:
        main()
    except BrokenPipeError:  # e.g. output piped into head
        os._exit(0)
