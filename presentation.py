#!/usr/bin/env python3
"""Generates results/presentation.pptx from results/metrics.json.

Usage:
  python3 presentation.py                    # every run: runs/*/ and results/
  python3 presentation.py other/metrics.json # only that metrics file

Slides: the rules of the demo, what we measure and how, the domain model of with-ddd,
an executive summary for each measured phase (F1, F2 and, if it exists, F3) that compares the runs,
the failures and their cause, a conclusion with what repeats across runs and considerations
for large projects. Needs python-pptx:  pip install python-pptx
"""
import json
import os
import sys
from pathlib import Path

try:
    from pptx import Presentation
    from pptx.dml.color import RGBColor
    from pptx.enum.shapes import MSO_CONNECTOR, MSO_SHAPE
    from pptx.enum.text import MSO_ANCHOR, PP_ALIGN
    from pptx.oxml.ns import qn
    from pptx.util import Inches, Pt
except ImportError:
    sys.exit("python-pptx is missing: pip install python-pptx")

ROOT = Path(__file__).resolve().parent
OUTPUT = Path(os.environ.get("OUTPUT", ROOT / "results" / "presentation.pptx"))

# Palette
INK = RGBColor(0x1B, 0x24, 0x30)
BLUE = RGBColor(0x1F, 0x3B, 0x4D)
TEAL = RGBColor(0x0F, 0x7C, 0x8C)
ORANGE = RGBColor(0xC0, 0x6A, 0x1B)
GREY = RGBColor(0x5B, 0x6B, 0x7A)
LIGHT = RGBColor(0xEE, 0xF3, 0xF6)
LINE = RGBColor(0xC9, 0xD5, 0xDE)
WHITE = RGBColor(0xFF, 0xFF, 0xFF)
TITLE_FONT, BODY_FONT = "Cambria", "Calibri"


# ---------------------------------------------------------------- helpers
def text(slide, x, y, w, h, content, size=14, color=INK, bold=False, font=BODY_FONT, align=PP_ALIGN.LEFT,
         anchor=MSO_ANCHOR.TOP, italic=False):
    """content: str or a list of paragraphs; each paragraph is a str or a list of (text, {bold, color, size})."""
    tb = slide.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h))
    tf = tb.text_frame
    tf.word_wrap = True
    tf.margin_left = tf.margin_right = tf.margin_top = tf.margin_bottom = 0
    tf.vertical_anchor = anchor
    paragraphs = content if isinstance(content, list) else [content]
    for i, par in enumerate(paragraphs):
        p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
        p.alignment = align
        runs = par if isinstance(par, list) else [(par, {})]
        for t, o in runs:
            r = p.add_run()
            r.text = t
            f = r.font
            f.name = o.get("font", font)
            f.size = Pt(o.get("size", size))
            f.bold = o.get("bold", bold)
            f.italic = o.get("italic", italic)
            f.color.rgb = o.get("color", color)
        if i:
            p.space_before = Pt(4)
    return tb


def box(slide, x, y, w, h, fill=LIGHT, line=None, shape=MSO_SHAPE.ROUNDED_RECTANGLE, radius=0.06, dash=False):
    s = slide.shapes.add_shape(shape, Inches(x), Inches(y), Inches(w), Inches(h))
    s.shadow.inherit = False
    if fill is None:
        s.fill.background()
    else:
        s.fill.solid()
        s.fill.fore_color.rgb = fill
    if line is None:
        s.line.fill.background()
    else:
        s.line.color.rgb = line
        s.line.width = Pt(1.25)
        if dash:
            s.line.dash_style = 4  # dashes
    if shape == MSO_SHAPE.ROUNDED_RECTANGLE:
        s.adjustments[0] = radius
    s.text_frame.text = ""
    return s


def circle(slide, x, y, d, label, fill=TEAL, size=16):
    box(slide, x, y, d, d, fill=fill, shape=MSO_SHAPE.OVAL)
    text(slide, x, y, d, d, label, size=size, bold=True, color=WHITE, font=TITLE_FONT, align=PP_ALIGN.CENTER,
         anchor=MSO_ANCHOR.MIDDLE)


def title(slide, t, sub=None):
    text(slide, 0.6, 0.35, 12.1, 0.7, t, size=30, bold=True, color=BLUE, font=TITLE_FONT, anchor=MSO_ANCHOR.MIDDLE)
    if sub:
        text(slide, 0.6, 1.02, 12.1, 0.4, sub, size=14, color=GREY)


def arrow(slide, x1, y1, x2, y2, head="triangle", tail=None, color=GREY, dash=False):
    c = slide.shapes.add_connector(MSO_CONNECTOR.STRAIGHT, Inches(x1), Inches(y1), Inches(x2), Inches(y2))
    c.line.color.rgb = color
    c.line.width = Pt(1.25)
    if dash:
        c.line.dash_style = 4
    ln = c.line._get_or_add_ln()
    if tail:
        e = ln.makeelement(qn("a:headEnd"), {"type": tail, "w": "med", "len": "med"})
        ln.append(e)
    if head:
        e = ln.makeelement(qn("a:tailEnd"), {"type": head, "w": "med", "len": "med"})
        ln.append(e)
    return c


def notes(slide, t):
    if os.environ.get("NO_NOTES") != "1":  # NO_NOTES=1 for the macOS preview, which does not show decks with notes
        slide.notes_slide.notes_text_frame.text = t


# ---------------------------------------------------------------- fixed slides
def rules(prs):
    s = prs.slides.add_slide(prs.slide_layouts[6])
    title(s, "Rules of the demo", "What makes the comparison fair and the data valid")
    cards = [
        ("1", "Same starting point", "Same REQUIREMENTS.md, pom.xml and Bob rules. The only difference: with-ddd has docs/domain/ and an AGENTS.md that says the docs drive the development."),
        ("2", "Same prompts, no help", "Each prompt is sent as is. Nobody answers Bob: it works alone, decides when information is missing and writes it down in its history."),
        ("3", "New session per phase", "F1, F2 and F3 are new sessions, without memory. The history leaves the folder when the snapshot is taken, so it cannot serve as documentation."),
        ("4", "Audited isolation", "Each folder works alone: no git, no network, no reading outside its folder. measure.py audit checks every session in Bob's database."),
        ("5", "Hidden games", "Accuracy is decided by acceptance games Bob never sees (scenarios.tsv), with those of earlier phases as a regression test."),
        ("6", "External measurement", "Cost, reads, coupling, cohesion and mutants are measured by measure.py, not by Bob. One execution per folder is a demonstration, not proof."),
    ]
    w, h, gx, gy = 3.85, 2.55, 0.28, 0.28
    for i, (n, t, d) in enumerate(cards):
        x, y = 0.6 + (i % 3) * (w + gx), 1.65 + (i // 3) * (h + gy)
        box(s, x, y, w, h)
        circle(s, x + 0.25, y + 0.25, 0.6, n)
        text(s, x + 1.0, y + 0.25, w - 1.2, 0.6, t, size=17, bold=True, color=BLUE, font=TITLE_FONT, anchor=MSO_ANCHOR.MIDDLE)
        text(s, x + 0.25, y + 1.0, w - 0.5, h - 1.15, d, size=15)
    notes(s, "These restrictions are in the .bob/rules of each folder and in the README workflow. "
             "If a session leaves its folder or uses git, curl or env, the audit marks it as invalid and the phase is repeated.")


def metrics_slide(prs):
    s = prs.slides.add_slide(prs.slide_layouts[6])
    title(s, "What we measure and how", "measure.py builds a snapshot of each phase in a temporary folder and reads Bob's sessions in ~/.bob/db/bob.db")
    rows = [
        ("Accuracy", "Correct hidden games (JSONL log and console), regressions, tests and mutation score", "scenarios.tsv, Maven, PIT", "Higher"),
        ("Agent cost", "Cost, max context, duration, model calls; Bobcoins per correct game", "bob.db", "Lower"),
        ("How it worked", "Docs and code files read before editing; whether it edited the docs first", "bob.db", "Docs first"),
        ("Coupling", "Ce · Ca · propagation cost · Yourdon & Constantine connection type", "jdeps, javap", "Lower"),
        ("Cohesion and size", "TCC, LCC, LCOM*; lines per file and method; methods > 30 lines or > 3 parameters", "javap, sources", "TCC high"),
        ("Encapsulation", "Public methods that change state; getters that let callers skip the rules", "javap", "Fewer"),
        ("Change (F2, F3)", "Existing classes modified, lines changed, tests touched, Bobcoins per 100 lines", "diff between snapshots", "Fewer"),
        ("Language", "Discarded glossary terms in the code; terms present", "glossary.md", "0 discarded"),
        ("Autonomy", "Decisions Bob made alone and the question it would have asked", "Bob's history", "Fewer gaps"),
    ]
    table = s.shapes.add_table(len(rows) + 1, 4, Inches(0.6), Inches(1.6), Inches(12.1), Inches(5.4)).table
    widths = [2.1, 6.4, 2.1, 1.5]
    for i, a in enumerate(widths):
        table.columns[i].width = Inches(a)
    for j, head in enumerate(["What", "Metric", "Source", "Better"]):
        cell(table.cell(0, j), head, bold=True, color=WHITE, fill=BLUE)
    for i, row in enumerate(rows, start=1):
        for j, v in enumerate(row):
            cell(table.cell(i, j), v, bold=j == 0, color=TEAL if j == 0 else INK, fill=WHITE if i % 2 else LIGHT)
    for r in table.rows:
        r.height = Inches(0.54)
    notes(s, "Ce (efferent coupling): how many project classes a class uses. Ca (afferent): how many use it; "
             "high is only fine for stable classes, such as an enum. Propagation cost: share of the system a change can reach. "
             "Yourdon & Constantine: data, stamp, control, external, common or content connection, from best to worst. "
             "TCC and LCC: share of method pairs that share fields. Mutation score: PIT plants small bugs in the code and counts how many the tests detect.")


def cell(c, t, bold=False, color=INK, fill=WHITE, size=13, align=PP_ALIGN.LEFT):
    c.fill.solid()
    c.fill.fore_color.rgb = fill
    c.margin_left = c.margin_right = Inches(0.08)
    c.margin_top = c.margin_bottom = Inches(0.03)
    c.vertical_anchor = MSO_ANCHOR.MIDDLE
    tf = c.text_frame
    tf.text = ""
    p = tf.paragraphs[0]
    p.alignment = align
    r = p.add_run()
    r.text = str(t)
    r.font.size = Pt(size)
    r.font.bold = bold
    r.font.name = BODY_FONT
    r.font.color.rgb = color


def uml_class(slide, x, y, w, name, members, stereotype=None, root=False):
    height = 0.42 + 0.235 * len(members) + 0.1
    box(slide, x, y, w, height, fill=WHITE, line=TEAL if root else LINE, radius=0.04)
    box(slide, x, y, w, 0.42, fill=TEAL if root else BLUE, radius=0.15)
    label = [(name, {"bold": True, "color": WHITE, "size": 14, "font": TITLE_FONT})]
    if stereotype:
        label.append((f"  «{stereotype}»", {"color": WHITE, "size": 10}))
    text(slide, x + 0.1, y, w - 0.2, 0.42, [label], anchor=MSO_ANCHOR.MIDDLE)
    text(slide, x + 0.12, y + 0.48, w - 0.24, height - 0.5, [m for m in members], size=11, color=INK)
    return (x, y, w, height)


def model(prs):
    s = prs.slides.add_slide(prs.slide_layouts[6])
    title(s, "The domain model of with-ddd", "src/with-ddd/docs/domain/model.puml: classes, responsibilities and who can change the game")
    W = 2.75
    player = uml_class(s, 0.6, 1.55, W, "Player", ["+ color(): Color"])
    color = uml_class(s, 0.6, 3.55, W, "Color", ["X · O · EMPTY"], "enum")
    piece = uml_class(s, 0.6, 5.55, W, "Piece", ["+ color(): Color"])
    ttt = uml_class(s, 3.85, 1.55, 3.2, "TicTacToe", ["+ put(Coordinate): boolean", "+ state(): State", "+ currentPlayer(): Player",
                                                      "+ colorAt(Coordinate): Color"], "aggregate root", root=True)
    board = uml_class(s, 3.85, 3.85, 3.2, "Board", ["+ colorAt · isEmpty · isFull", "+ hasWin(Color): boolean", "~ put(Coordinate, Piece)"])
    square = uml_class(s, 3.85, 5.55, 3.2, "Square", ["+ coordinate() · color() · isEmpty()", "~ put(Piece)"])
    rnd = uml_class(s, 7.55, 1.55, W, "Round", ["+ current(): Player", "~ next()"])
    line = uml_class(s, 7.55, 3.55, W, "Line", ["+ row(n) · column(n)", "+ diagonal() · inverseDiagonal()", "+ all(): 8 Lines"], "value")
    coord = uml_class(s, 7.55, 5.55, W, "Coordinate", ["+ row() · column(): 1..3", "+ all(): reading order"], "value")
    uml_class(s, 10.6, 1.55, 2.15, "State", ["X_TO_PUT · O_TO_PUT", "X_WINS · O_WINS", "DRAW · isOver()"], "enum")
    # Outside the domain
    box(s, 10.6, 3.55, 2.15, 3.35, fill=None, line=GREY, dash=True, radius=0.05)
    text(s, 10.75, 3.65, 1.9, 3.2, [[("Outside the domain", {"bold": True, "color": BLUE, "font": TITLE_FONT, "size": 14})],
                                    "console: reads and writes; only talks to TicTacToe",
                                    "Main: creates the Players and the game",
                                    [("~ = only inside the domain package (invariant I10)", {"color": ORANGE, "size": 11})]], size=12)

    def cx(b):
        return b[0] + b[2] / 2

    def bottom(b):
        return b[1] + b[3]

    # Relations: diamond = composition, arrow = association, dashes = uses
    arrow(s, cx(ttt), bottom(ttt), cx(board), board[1], head=None, tail="diamond")          # TicTacToe ◆— Board
    arrow(s, ttt[0] + ttt[2], 1.95, rnd[0], 1.95, head=None, tail="diamond")               # TicTacToe ◆— Round
    arrow(s, ttt[0], 1.95, player[0] + player[2], 1.95, head="triangle")                  # TicTacToe → Player (2)
    text(s, player[0] + player[2] + 0.05, 1.6, 0.4, 0.3, "2", size=11, color=GREY)
    arrow(s, cx(board), bottom(board), cx(square), square[1], head=None, tail="diamond")    # Board ◆— Square (9)
    text(s, cx(square) + 0.08, square[1] - 0.3, 0.4, 0.3, "9", size=11, color=GREY)
    arrow(s, square[0], square[1] + 0.45, piece[0] + piece[2], piece[1] + 0.45, head="triangle")  # Square → Piece 0..1
    text(s, piece[0] + piece[2] + 0.05, piece[1] + 0.12, 0.6, 0.3, "0..1", size=11, color=GREY)
    arrow(s, square[0] + square[2], square[1] + 0.45, coord[0], coord[1] + 0.45, head="triangle")  # Square → Coordinate
    arrow(s, board[0] + board[2], board[1] + 0.6, line[0], line[1] + 0.9, head="triangle", dash=True)  # Board ..> Line
    arrow(s, cx(line), bottom(line), cx(coord), coord[1], head="triangle")                  # Line → Coordinate (3)
    text(s, cx(coord) + 0.08, coord[1] - 0.3, 0.4, 0.3, "3", size=11, color=GREY)
    arrow(s, cx(player), bottom(player), cx(color), color[1], head="triangle")              # Player → Color
    arrow(s, cx(piece), piece[1], cx(color), bottom(color), head="triangle")                # Piece → Color
    notes(s, "Diamond: composition. Solid arrow: association. Dashes: uses. Round alternates the two Players (current) and TicTacToe keeps the State (state()). TicTacToe is the aggregate root: "
             "it is the only one that changes the game (I10); Board, Square and Round have their mutators inside the package. "
             "A Piece does not keep its Coordinate: its position is the Square that holds it (D7). "
             "Goal and Put from the original diagram are not classes in version 1 (D5, D6).")


def large_projects(prs):
    s = prs.slides.add_slide(prs.slide_layouts[6])
    title(s, "For large projects", "What this small experiment teaches before taking agents to a real system")
    cards = [
        ("Security", "Minimal permissions: no git push, no network, no environment variables or secrets. Work in an isolated folder or container, and audit every command the agent runs."),
        ("Context and cost", "Documentation costs tokens. A short index (AGENTS.md) and a file map let the agent read only what it needs; if the docs add to the code instead of replacing it, the cost grows."),
        ("Living docs", "Docs only drive if they are up to date: docs first, every decision recorded, and executable rules (ArchUnit, vocabulary) in CI so that drift is detected."),
        ("Verifiable quality", "Acceptance tests the agent never sees, mutation testing and human review of the design. Passing mvn verify is not enough: the same agent writes the tests."),
        ("Memory and isolation", "A session without memory is reproducible. Histories, agent databases and stray files can leak context between tasks, or sensitive data."),
        ("Measure before deciding", "Repeat each experiment 3 to 5 times, compare medians and separate effects: documentation, workflow and the agent's model."),
    ]
    w, h, gx, gy = 3.85, 2.55, 0.28, 0.28
    for i, (t, d) in enumerate(cards):
        x, y = 0.6 + (i % 3) * (w + gx), 1.65 + (i // 3) * (h + gy)
        box(s, x, y, w, h)
        text(s, x + 0.25, y + 0.22, w - 0.5, 0.45, t, size=17, bold=True, color=TEAL if i else ORANGE, font=TITLE_FONT)
        text(s, x + 0.25, y + 0.75, w - 0.5, h - 0.9, d, size=15)
    notes(s, "Security comes first: in this experiment the agent runs commands without human approval, "
             "so the permissions deny git, curl, wget, env and printenv, and the audit checks every session.")


# ---------------------------------------------------------------- summary per phase (one or several runs)
def _v(r, k):
    return (r or {}).get(k)


def _num(v):
    if isinstance(v, bool):
        return float(v)
    if isinstance(v, (int, float)):
        return float(v)
    if isinstance(v, list):
        return float(len(v))
    if isinstance(v, str) and "/" in v:
        a, b = v.split("/")[:2]
        try:
            return int(a) / int(b) if int(b) else None
        except ValueError:
            return None
    return None


# (label, key or function, better: "max" | "min" | None)
PHASE_ROWS = [
    ("Correct according to the log", "acceptance_log", "max"),
    ("Exact console", "acceptance", "max"),
    ("  · with the correct logic", "acceptance_logic", "max"),
    ("Console = log", "console_matches_log", "max"),
    ("Mutation score without console (%)", "mutation_score_no_io", "max"),
    ("Escapes (hidden games failing, suite green)", "test_escapes", "min"),
    ("Cost", "cost_bobcoins", "min"),
    ("Max context (k tokens)", "context_k", "min"),
    ("Duration (s)", "duration_s", "min"),
    ("Code read before editing", "code_read_before_edit", "min"),
    ("Classes · lines", lambda r: f"{_v(r, 'classes')} · {_v(r, 'loc_main')}", None),
    ("Bobcoins per game (correct logic)", "cost_per_game_logic", "min"),
    ("Methods > 30 lines · > 3 parameters", lambda r: None if _v(r, "methods_total") is None else f"{_v(r, 'methods_>30_lines')} · {_v(r, 'methods_>3_params')}", None),
    ("Ce max", "Ce_max", "min"),
    ("Propagation cost (%)", "propagation_cost", "min"),
    ("Yourdon & Constantine index", "yc_index", "min"),
    ("TCC median", "TCC_median", "max"),
    ("LCOM* median", "LCOM*_median", "min"),
    ("Public methods that change state", lambda r: len(_v(r, "public_mutators") or []), "min"),
    ("Getters that expose state", lambda r: len(_v(r, "state_leaks") or []), "min"),
    ("Existing classes modified", "modified_classes", "min"),
    ("Lines changed in existing", "lines_changed_in_existing", "min"),
    ("Bobcoins per 100 lines touched", "cost_per_100_lines_touched", "min"),
    ("Human interventions · questions", lambda r: None if _v(r, "interventions") is None else f"{_v(r, 'interventions')} · {_v(r, 'questions_to_user')}", None),
    ("Doc gaps (gap + ambiguity)", lambda r: None if _v(r, "decisions") is None else (_v(r, "dec_gap") or 0) + (_v(r, "dec_ambiguity") or 0), "min"),
    ("Discarded terms", "discarded_terms", "min"),
]
NAMES = {"F1": "F1 · build the game", "F2": "F2 · CR-1: play against the PC", "F3": "F3 · CR-2: three pieces that move"}
WITH, WITHOUT = "with DDD", "without DDD"


def value(r, key):
    return key(r) if callable(key) else _v(r, key)


def winner(without, with_, key, better):
    """0 = without DDD wins, 1 = with DDD wins, "=" = tie, None = does not apply."""
    if not better or not without or not with_:
        return None
    a, b = _num(value(without, key)), _num(value(with_, key))
    if a is None or b is None:
        return None
    if a == b:
        return "="
    return 0 if (a > b) == (better == "max") else 1


def repeats(winners):
    g = [x for x in winners if x is not None]
    if len(g) < 2:
        return None
    sides = {x for x in g if x != "="}
    if not sides:
        return "tie"
    if len(sides) == 1:
        side = {0: WITHOUT, 1: WITH}[sides.pop()]
        return side if "=" not in g else f"{side} or tie"
    return "varies"


def phase_slide(prs, phase, runs):
    s = prs.slides.add_slide(prs.slide_layouts[6])
    available = [(n, d) for n, _, d in runs if d.get("without-ddd", {}).get(phase) and d.get("with-ddd", {}).get(phase)]
    several = len(available) > 1
    sub = (f"{len(available)} runs per folder: in bold the best value of each run; the last column says whether it repeats."
           if several else "One execution per folder: observations of this run, not proof. In bold, the best value.")
    title(s, f"Executive summary: {NAMES.get(phase, phase)}", sub)
    if not available:
        box(s, 0.6, 1.7, 12.1, 2.0)
        text(s, 0.9, 1.9, 11.5, 1.6, [f"There is no data for {phase} yet.",
                                      f"Run the phase, then  python3 measure.py snapshot {phase}  and  python3 measure.py, and generate the presentation again."],
             size=18, color=GREY, anchor=MSO_ANCHOR.MIDDLE)
        return
    rows = []
    for label, key, better in PHASE_ROWS:
        vals, wins = [], []
        for _, d in available:
            without, with_ = d["without-ddd"][phase], d["with-ddd"][phase]
            vals += [value(without, key), value(with_, key)]
            wins.append(winner(without, with_, key, better))
        if all(v is None for v in vals):
            continue
        rows.append((label, vals, wins, repeats(wins) if several else None))
    cols = 1 + 2 * len(available) + (1 if several else 0)
    widths = [2.75] + [0.98] * (2 * len(available)) + ([1.0] if several else [])
    if not several:
        widths = [3.7, 1.6, 1.6]
    table_width = sum(widths)
    height = min(0.3, 5.75 / (len(rows) + 2))  # so that the table fits on the slide even with many metrics
    fs = 11 if height >= 0.28 else 9
    t = s.shapes.add_table(len(rows) + 2, cols, Inches(0.6), Inches(1.55), Inches(table_width), Inches(height * (len(rows) + 2))).table
    for i, a in enumerate(widths):
        t.columns[i].width = Inches(a)
    # Two header rows: run and folder
    cell(t.cell(0, 0), "", fill=BLUE, size=fs)
    cell(t.cell(1, 0), "Metric", bold=True, color=WHITE, fill=BLUE, size=fs)
    for k, (n, _) in enumerate(available):
        c0 = 1 + 2 * k
        t.cell(0, c0).merge(t.cell(0, c0 + 1))
        cell(t.cell(0, c0), n if several else "", bold=True, color=WHITE, fill=BLUE, size=fs, align=PP_ALIGN.CENTER)
        cell(t.cell(1, c0), WITHOUT, bold=True, color=WHITE, fill=BLUE, size=fs, align=PP_ALIGN.CENTER)
        cell(t.cell(1, c0 + 1), WITH, bold=True, color=WHITE, fill=BLUE, size=fs, align=PP_ALIGN.CENTER)
    if several:
        cell(t.cell(0, cols - 1), "", fill=BLUE, size=fs)
        cell(t.cell(1, cols - 1), "Repeats?", bold=True, color=WHITE, fill=BLUE, size=fs, align=PP_ALIGN.CENTER)
    for i, (label, vals, wins, rep) in enumerate(rows, start=2):
        fill = WHITE if i % 2 else LIGHT
        cell(t.cell(i, 0), label, size=fs, fill=fill)
        for j, v in enumerate(vals):
            g = wins[j // 2]
            won = g == j % 2
            cell(t.cell(i, 1 + j), "—" if v is None else (round(v, 2) if isinstance(v, float) else v), size=fs, fill=fill,
                 align=PP_ALIGN.CENTER, bold=won, color=TEAL if won else INK)
        if several:
            rep = rep or "—"
            color = TEAL if rep.startswith(WITH) else ORANGE if rep.startswith(WITHOUT) else GREY
            cell(t.cell(i, cols - 1), rep, size=fs, fill=fill, align=PP_ALIGN.CENTER, bold=rep.startswith((WITH, WITHOUT)), color=color)
    for r in t.rows:
        r.height = Inches(height)
    x = 0.6 + table_width + 0.3
    w = 12.7 - x
    y = 1.55
    for tt, d in findings(phase, available)[:4]:
        box(s, x, y, w, 1.28)
        text(s, x + 0.18, y + 0.1, w - 0.36, 0.32, tt, size=14, bold=True, color=TEAL, font=TITLE_FONT)
        text(s, x + 0.18, y + 0.44, w - 0.36, 0.82, d, size=11)
        y += 1.38
    rebuilt = [n for n, d in available if _v(d["with-ddd"][phase], "snapshot_rebuilt") or _v(d["without-ddd"][phase], "snapshot_rebuilt")]
    notes(s, "Data from each run's metrics.json, generated by measure.py. "
          + (f"The {phase} snapshot of {', '.join(rebuilt)} was rebuilt from bob.db and validated against the reads of the next phase. " if rebuilt else "")
          + "What with-ddd received in each run: " + "; ".join(f"{n}: {desc}" for n, desc, _ in runs if desc))


def findings(phase, available):
    h = []
    def per_run(fn):
        return " · ".join(f"{n}: {fn(d['without-ddd'][phase], d['with-ddd'][phase])}" for n, d in available)
    h.append(("Accuracy", per_run(lambda a, b: f"without {a.get('acceptance_log') or a.get('acceptance')} ({a.get('acceptance_logic', '?')} logic), "
                                                f"with {b.get('acceptance_log') or b.get('acceptance')}")
              + _failures(phase, available)))
    h.append(("Cost", per_run(lambda a, b: f"with DDD {(_num(b.get('cost_bobcoins')) or 0) / (_num(a.get('cost_bobcoins')) or 1):.1f}× "
                                           f"({b.get('cost_bobcoins')} vs {a.get('cost_bobcoins')})")))
    if all(_v(d["with-ddd"][phase], "code_read_before_edit") is not None for _, d in available):
        h.append(("Did the docs drive?", "Code read before editing, with DDD vs without: "
                  + per_run(lambda a, b: f"{b.get('code_read_before_edit')} vs {a.get('code_read_before_edit')}") + "."
                  + ("" if phase == "F1" else " Docs edited before the code: "
                     + ", ".join(f"{n} {'yes' if _v(d['with-ddd'][phase], 'docs_before_code') else 'no'}" for n, d in available) + ".")))
    if all(_v(d["with-ddd"][phase], "modified_classes") is not None for _, d in available):
        h.append(("Change", "Existing classes modified, with vs without: "
                  + per_run(lambda a, b: f"{b.get('modified_classes')} vs {a.get('modified_classes')} ({b.get('lines_changed_in_existing')} vs {a.get('lines_changed_in_existing')} lines)")))
    else:
        h.append(("Design and language", "Getters that expose state and discarded terms, with vs without: "
                  + per_run(lambda a, b: f"{len(b.get('state_leaks') or [])}/{b.get('discarded_terms')} vs {len(a.get('state_leaks') or [])}/{a.get('discarded_terms')}")))
    return h


def _failures(phase, available):
    extra = []
    for n, d in available:
        for c, name in (("without-ddd", WITHOUT), ("with-ddd", WITH)):
            r = d[c][phase]
            if r.get("requirements_modified"):
                extra.append(f"{n} {name} modified REQUIREMENTS.md")
    return (". " + "; ".join(extra) + ".") if extra else "."


def conclusion_slide(prs, phases, runs):
    valid = [(n, d) for n, _, d in runs if d]
    if len(valid) < 2:
        return
    s = prs.slides.add_slide(prs.slide_layouts[6])
    title(s, f"Conclusion: what repeats in {len(valid)} runs",
          "A difference that shows up in every run is a signal; one that changes is noise or the effect of what changed between runs")
    groups = {WITH: [], WITHOUT: [], "varies": []}
    for label, key, better in PHASE_ROWS:
        if not better:
            continue
        for f in phases:
            wins = [winner(d.get("without-ddd", {}).get(f), d.get("with-ddd", {}).get(f), key, better) for _, d in valid]
            rep = repeats(wins) or ""
            k = WITH if rep.startswith(WITH) else WITHOUT if rep.startswith(WITHOUT) else rep
            if k in groups:
                groups[k].append(f"{label.strip(' ·')} ({f})" + (" *" if rep.endswith("tie") and k != rep else ""))
    columns = [("Always in favor of with DDD", WITH, TEAL), ("Always in favor of without DDD", WITHOUT, ORANGE),
               ("Changes between runs", "varies", GREY)]
    verdict_f = ROOT / "conclusion.txt"
    verdict = verdict_f.read_text(encoding="utf-8").strip() if verdict_f.exists() else ""
    height = 3.15 if verdict else 4.15
    w, gx = 3.9, 0.2
    for i, (t, k, color) in enumerate(columns):
        x = 0.6 + i * (w + gx)
        box(s, x, 1.6, w, height)
        text(s, x + 0.2, 1.72, w - 0.4, 0.4, t, size=16, bold=True, color=color, font=TITLE_FONT)
        items = _group(groups[k]) or ["No metric"]
        text(s, x + 0.2, 2.2, w - 0.4, height - 0.95, items, size=12)
        if k != "varies":
            text(s, x + 0.2, 1.6 + height - 0.35, w - 0.4, 0.3, "* tied in one run", size=10, color=GREY)
    if verdict:
        box(s, 0.6, 4.88, 12.1, 0.92, fill=BLUE)
        text(s, 0.85, 4.88, 11.6, 0.92, verdict, size=14, bold=True, color=WHITE, anchor=MSO_ANCHOR.MIDDLE)
    desc = [f"{n}: {d}" for n, d, _ in runs if d]
    box(s, 0.6, 5.9, 12.1, 1.2, fill=WHITE, line=LINE)
    text(s, 0.8, 5.98, 11.7, 1.05, [[("What with-ddd received in each run", {"bold": True, "color": BLUE, "font": TITLE_FONT, "size": 14})]] + desc,
         size=12)
    notes(s, "Computed by presentation.py: for each metric and phase it looks at which folder wins in each run. "
             "without-ddd receives the same in every run, so its differences between runs show Bob's own variation.")


def _group(items):
    """'Cost (F1)', 'Cost (F2)' -> 'Cost (F1, F2)'."""
    order, phases = [], {}
    for it in items:
        mark = " *" if it.endswith(" *") else ""
        m, f = it.removesuffix(" *").rsplit(" (", 1)
        m += mark
        if m not in phases:
            order.append(m)
            phases[m] = []
        phases[m].append(f.rstrip(")"))
    return [f"{m} ({', '.join(phases[m])})" for m in order]


def failures_slide(prs):
    """Failures documented in failures.json (summary of FAILURES.md): what happened, the cause and whether it is the domain."""
    f = ROOT / "failures.json"
    if not f.exists():
        return
    failures = json.loads(f.read_text(encoding="utf-8"))
    s = prs.slides.add_slide(prs.slide_layouts[6])
    title(s, "Failures and their cause", "Was domain context missing, or was it something else? The detail and the evidence are in FAILURES.md")
    w, gx = 3.9, 0.2
    colors = {"domain": TEAL, "agent": GREY, "documentation": ORANGE}
    for i, fl in enumerate(failures[:3]):
        x = 0.6 + i * (w + gx)
        box(s, x, 1.6, w, 5.45)
        color = colors.get(fl.get("type"), GREY)
        box(s, x + 0.2, 1.78, w - 0.4, 0.42, fill=color, radius=0.3)
        text(s, x + 0.2, 1.78, w - 0.4, 0.42, fl["verdict"], size=12, bold=True, color=WHITE, align=PP_ALIGN.CENTER,
             anchor=MSO_ANCHOR.MIDDLE)
        text(s, x + 0.2, 2.35, w - 0.4, 0.75, fl["title"], size=16, bold=True, color=BLUE, font=TITLE_FONT)
        text(s, x + 0.2, 3.1, w - 0.4, 3.9, [
            [("What happened. ", {"bold": True}), (fl["what"], {})],
            [("Cause. ", {"bold": True}), (fl["cause"], {})],
            [("Lesson. ", {"bold": True, "color": color}), (fl["lesson"], {})],
        ], size=12)
    notes(s, " ".join(fl.get("note", "") for fl in failures))


# ---------------------------------------------------------------- main
def load_runs():
    """The runs archived in runs/<name>/ and the current one in results/, in that order.
    When a metrics.json is passed as an argument, only that one."""
    if len(sys.argv) > 1:
        return [("Run", "", json.loads(Path(sys.argv[1]).read_text(encoding="utf-8")))]
    paths = sorted((ROOT / "runs").glob("*/metrics.json")) + [ROOT / "results" / "metrics.json"]
    out = []
    for i, r in enumerate(paths, start=1):
        if not r.exists():
            continue
        desc_f = r.parent / "description.txt"
        desc = desc_f.read_text(encoding="utf-8").strip() if desc_f.exists() else ""
        out.append((f"Run {i}", desc, json.loads(r.read_text(encoding="utf-8"))))
    return out


def main():
    runs = load_runs()
    phases = sorted({f for _, _, d in runs for c in d.values() for f in c} | {"F1", "F2"})
    prs = Presentation()
    prs.slide_width, prs.slide_height = Inches(13.333), Inches(7.5)
    rules(prs)
    metrics_slide(prs)
    model(prs)
    for f in phases:
        phase_slide(prs, f, runs)
    failures_slide(prs)
    conclusion_slide(prs, phases, runs)
    large_projects(prs)
    OUTPUT.parent.mkdir(exist_ok=True)
    prs.save(OUTPUT)
    print(f"wrote {OUTPUT} ({len(prs.slides._sldIdLst)} slides, {len(runs)} runs)")


if __name__ == "__main__":
    main()
