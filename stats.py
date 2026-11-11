#!/usr/bin/env python3
"""Statistics of several runs of the experiment.

Usage:  python3 measure.py stats      (or  python3 stats.py)

Reads the history: each runs/<name>/metrics.json and, if it exists, results/metrics.json (the run in
progress). Writes:
  runs/stats.md      median, range, mean and deviation per phase and folder; the paired difference per run
                     (with DDD minus without DDD), how many runs each folder wins and the p of the sign test;
                     efficiency, scalability and Spearman correlations between design, accuracy and cost
  runs/stats.json    the same, for other scripts
  runs/history.csv   one row per run, phase and folder with every numeric metric, to open in a spreadsheet

Only runs of the same series are combined (measure.py SERIES: it goes up when the requirements, the prompts
or the scenarios change). By default, the latest series; another one with  SERIES=1 python3 measure.py stats.

With fewer than 6 runs, the sign test cannot give p < 0.05 even if one folder always wins:
what matters is whether the difference repeats. The correlations are exploratory.
"""
import csv
import json
import math
import os
import re
import statistics as st
from pathlib import Path

ROOT = Path(__file__).resolve().parent
RUNS = Path(os.environ.get("RUNS", ROOT / "runs")).resolve()
CURRENT = Path(os.environ.get("RESULTS", ROOT / "results")).resolve()
FOLDERS = ["without-ddd", "with-ddd"]

# (label, key or function, better: "max" | "min" | None)
METRICS = [
    ("Accuracy", None, None),
    ("Correct games according to the JSONL log (%)", "acceptance_log", "max"),
    ("Games with every move valid (%)", "valid_moves", "max"),
    ("Console: games with the exact output (%)", "acceptance", "max"),
    ("Console: with the correct logic (%)", "acceptance_logic", "max"),
    ("Console: shows the same board as the log (%)", "console_matches_log", "max"),
    ("Mutation score without the console (%)", "mutation_score_no_io", "max"),
    ("Bob's tests that pass (%)", "tests_passed_pct", "max"),
    ("Bob's suite agrees with the hidden games (%)", "suite_right", "max"),
    ("Escapes: hidden games failing with the suite green", "test_escapes", "min"),
    ("Cost and efficiency", None, None),
    ("Bobcoins", "cost_bobcoins", "min"),
    ("Time (s)", "duration_s", "min"),
    ("Max context (k tokens)", "context_k", "min"),
    ("Model calls", "model_calls", "min"),
    ("Bobcoins per correct game", "cost_per_game", "min"),
    ("Seconds per correct game", "seconds_per_game", "min"),
    ("Bobcoins per game with the correct logic", "cost_per_game_logic", "min"),
    ("Bobcoins per correct game according to the log", "cost_per_game_log", "min"),
    ("How Bob worked", None, None),
    ("Code read before editing", "code_read_before_edit", "min"),
    ("Human interventions", "interventions", "min"),
    ("Questions to the user", "questions_to_user", "min"),
    ("Denied commands it tried", "denied_commands", "min"),
    ("Own decisions", "decisions", "min"),
    ("  · gaps or ambiguities", lambda r: (r.get("dec_gap", 0) + r.get("dec_ambiguity", 0)) if "decisions" in r else None, "min"),
    ("Size", None, None),
    ("Classes", "classes", None),
    ("Production lines", "loc_main", None),
    ("Lines per file (max)", "file_lines_max", "min"),
    ("Files with more than 200 lines", "files_>200_lines", "min"),
    ("Methods with more than 30 lines", "methods_>30_lines", "min"),
    ("Methods with more than 3 parameters", "methods_>3_params", "min"),
    ("Coupling and cohesion", None, None),
    ("Ce max", "Ce_max", "min"),
    ("Propagation cost (%)", "propagation_cost", "min"),
    ("Yourdon & Constantine index", "yc_index", "min"),
    ("Pairs with control, external, common or content", lambda r: sum((r.get("yc_pairs") or {}).get(t, 0) for t in ("control", "external", "common", "content")) if "yc_pairs" in r else None, "min"),
    ("TCC median", "TCC_median", "max"),
    ("LCC median", "LCC_median", "max"),
    ("LCOM* median", "LCOM*_median", "min"),
    ("Public methods that change state", lambda r: len(r["public_mutators"]) if "public_mutators" in r else None, "min"),
    ("Getters that expose state", lambda r: len(r["state_leaks"]) if "state_leaks" in r else None, "min"),
    ("Change (F2 and F3)", None, None),
    ("Existing classes modified", "modified_classes", "min"),
    ("Lines changed in existing classes", "lines_changed_in_existing", "min"),
    ("Existing tests touched", "existing_tests_touched", "min"),
    ("Bobcoins per 100 lines touched", "cost_per_100_lines_touched", "min"),
    ("Language", None, None),
    ("Discarded terms in the code", "discarded_terms", "min"),
]

# For the correlations: design traits against outcomes.
DESIGN = [("Lines", "loc_main"), ("Ce max", "Ce_max"), ("Propagation cost", "propagation_cost"),
          ("Y&C index", "yc_index"), ("TCC median", "TCC_median"), ("LCOM* median", "LCOM*_median"),
          ("Methods > 30 lines", "methods_>30_lines"), ("Lines per file max", "file_lines_max")]
OUTCOME = [("Correct according to the log %", "acceptance_log"), ("Exact console %", "acceptance"), ("Correct logic %", "acceptance_logic"),
           ("Mutation score %", "mutation_score_no_io"), ("Bobcoins", "cost_bobcoins"), ("Time", "duration_s")]


def num(v):
    """'11/12' -> 91.7 (%), numbers -> float, lists -> length; None when it cannot."""
    if isinstance(v, bool):  # yes/no as a percentage, so that the median across runs reads as "% of runs"
        return 100.0 * v
    if isinstance(v, (int, float)):
        return float(v)
    if isinstance(v, list):
        return float(len(v))
    if isinstance(v, str):
        m = re.fullmatch(r"\s*(\d+)\s*/\s*(\d+)\s*", v)
        if m:
            return round(100 * int(m.group(1)) / int(m.group(2)), 1) if int(m.group(2)) else None
        try:
            return float(v)
        except ValueError:
            return None
    return None


def value(r, key):
    if not r:
        return None
    return num(key(r) if callable(key) else r.get(key))


def series_of(folder, data):
    """The series measure.py recorded in each phase or, failing that, the one in reset.json; runs without a series are series 1."""
    for c in data.values():
        for r in c.values():
            if r.get("series"):
                return int(r["series"])
    try:
        return int(json.loads((folder / "reset.json").read_text()).get("series", 1))
    except (OSError, ValueError):
        return 1


def load():
    """[(name, description, data, series)] from runs/*/ and from results/, in that order."""
    paths = sorted(RUNS.glob("*/metrics.json"), key=lambda p: [int(x) if x.isdigit() else x for x in re.split(r"(\d+)", p.parent.name)])
    current = CURRENT / "metrics.json"
    if current.exists():
        paths.append(current)
    out = []
    for p in paths:
        d = p.parent / "description.txt"
        name = "current (results/)" if p == current else p.parent.name
        data = json.loads(p.read_text(encoding="utf-8"))
        out.append((name, d.read_text(encoding="utf-8").strip() if d.exists() else "", data, series_of(p.parent, data)))
    return out


def describe(xs):
    xs = [x for x in xs if x is not None]
    if not xs:
        return None
    return {"n": len(xs), "median": round(st.median(xs), 2), "mean": round(st.mean(xs), 2),
            "sd": round(st.stdev(xs), 2) if len(xs) > 1 else 0.0, "min": min(xs), "max": max(xs)}


def sign_test(wins, losses):
    """Exact two-sided sign test (ties are dropped)."""
    n = wins + losses
    if not n:
        return None
    k = min(wins, losses)
    return round(min(1.0, 2 * sum(math.comb(n, i) for i in range(k + 1)) / 2 ** n), 3)


def ranks(xs):
    order = sorted(range(len(xs)), key=lambda i: xs[i])
    r = [0.0] * len(xs)
    i = 0
    while i < len(order):
        j = i
        while j + 1 < len(order) and xs[order[j + 1]] == xs[order[i]]:
            j += 1
        for k in range(i, j + 1):
            r[order[k]] = (i + j) / 2 + 1
        i = j + 1
    return r


def spearman(xs, ys):
    pairs = [(x, y) for x, y in zip(xs, ys) if x is not None and y is not None]
    if len(pairs) < 4:
        return None, len(pairs)
    rx, ry = ranks([p[0] for p in pairs]), ranks([p[1] for p in pairs])
    mx, my = st.mean(rx), st.mean(ry)
    num_ = sum((a - mx) * (b - my) for a, b in zip(rx, ry))
    den = math.sqrt(sum((a - mx) ** 2 for a in rx) * sum((b - my) ** 2 for b in ry))
    return (round(num_ / den, 2) if den else None), len(pairs)


def f(x):
    if x is None:
        return "—"
    if isinstance(x, float) and x.is_integer():
        return str(int(x))
    return str(x)


def correlation_table(points, out, prefix=""):
    md = ["| Design trait | " + " | ".join(l for l, _ in OUTCOME) + " |", "|---|" + "---|" * len(OUTCOME)]
    for ld, kd in DESIGN:
        cells = []
        for lr, kr in OUTCOME:
            rho, n = spearman([value(p[3], kd) for p in points], [value(p[3], kr) for p in points])
            out[f"{prefix}{ld} vs {lr}"] = {"rho": rho, "n": n}
            cells.append(f"{f(rho)} (n={n})" if rho is not None else "—")
        md.append(f"| {ld} | " + " | ".join(cells) + " |")
    return md


def main():
    every = load()
    if not every:
        raise SystemExit("there are no runs: measure with  python3 measure.py  and archive with  python3 measure.py archive")
    series = int(os.environ.get("SERIES") or max(x[3] for x in every))
    runs = [x[:3] for x in every if x[3] == series]
    if not runs:
        raise SystemExit(f"there are no runs of series {series}")
    phases = sorted({ph for _, _, d in runs for c in d.values() for ph in c})
    out = {"series": series, "runs": [n for n, _, _ in runs], "phases": {}}

    md = ["# Statistics of every run", "",
          "Generated by `python3 measure.py stats` from `runs/*/metrics.json` and `results/metrics.json`.", "",
          f"The tables combine only the runs of **series {series}**: runs of different series received different requirements, prompts or scenarios.", "",
          "| Run | Series | Included? | Phases | What with-ddd received |", "|---|---|---|---|---|"]
    for n, desc, d, se in every:
        md.append(f"| {n} | {se} | {'yes' if se == series else 'no'} | {', '.join(sorted({ph for c in d.values() for ph in c}))} | {desc or '—'} |")
    md += ["", "How to read: **median [min–max]** of each folder; **Δ** is the median of the paired difference per run "
           "(with DDD minus without DDD); **with DDD better** counts in how many runs with-ddd won (ties apart); "
           "**p** is the exact sign test. With fewer than 6 runs, p does not go below 0.05 even if one folder always wins: "
           "look at whether the difference repeats.", ""]

    for phase in phases:
        out["phases"][phase] = {}
        md += [f"## {phase}", "",
               "| Metric | n | without-ddd | with-ddd | Δ (with − without) | With DDD better | p |", "|---|---|---|---|---|---|---|"]
        for label, key, better in METRICS:
            if key is None:
                md.append(f"| **{label}** | | | | | | |")
                continue
            per = {c: [value(d.get(c, {}).get(phase), key) for _, _, d in runs] for c in FOLDERS}
            res = {c: describe(per[c]) for c in FOLDERS}
            if not any(res.values()):
                continue
            diffs = [b - a for a, b in zip(per["without-ddd"], per["with-ddd"]) if a is not None and b is not None]
            wins = losses = ties = 0
            if better:
                for dlt in diffs:
                    if dlt == 0:
                        ties += 1
                    elif (dlt > 0) == (better == "max"):
                        wins += 1
                    else:
                        losses += 1
            p = sign_test(wins, losses) if better else None
            out["phases"][phase][label.strip(" ·")] = {"by_folder": res, "differences": diffs,
                                                      "with_ddd_better": wins, "with_ddd_worse": losses, "ties": ties, "p_sign": p}
            cell = lambda s: "—" if not s else f"{f(s['median'])} [{f(s['min'])}–{f(s['max'])}]"
            n = max((s["n"] for s in res.values() if s), default=0)
            better_txt = f"{wins}/{len(diffs)}" + (f" ({ties} tie{'s' if ties != 1 else ''})" if ties else "") if better and diffs else "—"
            md.append(f"| {label} | {n} | {cell(res['without-ddd'])} | {cell(res['with-ddd'])} "
                      f"| {f(round(st.median(diffs), 2)) if diffs else '—'} | {better_txt} | {f(p)} |")
        md.append("")

    # Efficiency and scalability: medians per phase, to see the trend while the system grows
    md += ["## Efficiency and scalability (medians across runs)", "",
           "A design that scales grows in lines without growing its propagation cost, its coupling or the cost of each change, and without losing correct games.", "",
           "| Folder | Phase | n | Lines | Propagation cost (%) | Y&C index | TCC | Correct games (%) | Bobcoins | Bobcoins per game (correct logic) | Bobcoins per 100 lines touched |",
           "|---|---|---|---|---|---|---|---|---|---|---|"]
    for c in FOLDERS:
        for phase in phases:
            rs = [d.get(c, {}).get(phase) for _, _, d in runs if d.get(c, {}).get(phase)]
            if not rs:
                continue
            med = lambda k: f((describe([value(r, k) for r in rs]) or {}).get("median"))
            correct = med("acceptance_log") if any(r.get("acceptance_log") for r in rs) else med("acceptance")
            md.append(f"| {c} | {phase} | {len(rs)} | {med('loc_main')} | {med('propagation_cost')} | {med('yc_index')} | {med('TCC_median')} "
                      f"| {correct} | {med('cost_bobcoins')} | {med('cost_per_game_logic')} | {med('cost_per_100_lines_touched')} |")
    md.append("")

    # Correlations: each point is one run × phase × folder
    points = [(n, c, phase, r) for n, _, d in runs for c in FOLDERS for phase, r in d.get(c, {}).items()]
    out["correlations"] = {}
    md += ["## Correlations between design, accuracy and cost", "",
           f"Spearman (ρ) over {len(points)} points (one run, one phase and one folder per point). ρ near 1 or −1 = strong relation; "
           "near 0 = no relation. With few points it is exploratory: it points at what to look at, it does not prove causes. "
           "The points of one folder are not independent across phases.", ""]
    md += correlation_table(points, out["correlations"])
    md += ["", "Careful: if one folder almost always wins, the correlation above measures the folder, not the trait. "
           "Inside each folder the comparison is cleaner (with fewer points):", ""]
    for c in FOLDERS:
        pc = [p for p in points if p[1] == c]
        md += [f"**{c}** ({len(pc)} points)", ""]
        md += correlation_table(pc, out["correlations"], prefix=f"{c}: ")
        md.append("")
    md.append("")

    # history.csv: every series, with its number
    RUNS.mkdir(exist_ok=True)
    keys = sorted({k for _, _, d, _ in every for c in d.values() for r in c.values() for k, v in r.items()
                   if num(v) is not None and not isinstance(v, (list, dict)) and k != "series"})
    with open(RUNS / "history.csv", "w", newline="", encoding="utf-8") as fh:
        w = csv.writer(fh)
        w.writerow(["run", "series", "folder", "phase"] + keys)
        for n, _, d, se in every:
            for c in FOLDERS:
                for phase, r in sorted(d.get(c, {}).items()):
                    w.writerow([n, se, c, phase] + [num(r.get(k)) if r.get(k) is not None else "" for k in keys])

    (RUNS / "stats.md").write_text("\n".join(md) + "\n", encoding="utf-8")
    (RUNS / "stats.json").write_text(json.dumps(out, indent=2, ensure_ascii=False), encoding="utf-8")
    print(f"wrote runs/stats.md, stats.json and history.csv (series {series}: {len(runs)} runs, {len(points)} snapshots measured)")


if __name__ == "__main__":
    main()
