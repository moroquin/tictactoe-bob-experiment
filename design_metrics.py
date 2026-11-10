#!/usr/bin/env python3
"""Design metrics of the experiment: coupling, cohesion, encapsulation and file and method size.

measure.py uses it; it can also be tried alone on some compiled classes:
  python3 design_metrics.py <folder>/target/classes [<folder>/src/main/java]

Coupling
  - Ce, Ca and instability per class (jdeps).
  - Propagation cost (MacCormack, Rusnak and Baldwin, 2006): the density of the reachability matrix between
    classes. It is the share of the system a change in one class can reach, directly or transitively.
    It measures scalability: it grows when the system gets tangled.
  - Type of connection by Yourdon & Constantine (Structured Design, 1979), from best to worst:
      1 data       the call only passes elementary values (primitives, String, enums)
      2 stamp      it passes a structure (a project object, an array or a collection)
                   of which the callee may use only a part
      3 control    it passes a flag (boolean or enum) the callee uses to decide what to do
      4 external   both classes depend on the same external device (the console, files, the process)
      5 common     both classes read or write the same mutable global data (a non-final static)
      6 content    one class reads or writes a field of another directly
    It is measured on the bytecode: each call between methods is a connection. Each pair of classes takes the
    worst type of its connections, and the Y&C index is the mean of those weights (1 = data only, 6 = content).
    It is an approximation: "control" detects a boolean or enum parameter that decides a branch in the callee,
    which also includes some validations.

Cohesion (per class, interfaces excluded; lambda methods count inside the method that holds them)
  - LCOM* (Henderson-Sellers), without getters: 0 = every method uses every field.
  - LCOM4 (Hitz and Montazeri): groups of methods and fields with no connection between them. > 1 = the class can be split.
  - TCC and LCC (Bieman and Kang, 1995): share of pairs of visible (non-private) methods that share a field,
    directly (TCC) or through a chain of methods (LCC). A method also uses the fields of the own methods it
    calls. 1 = very cohesive; TCC < 0.5 = not very cohesive.

Files and methods (on the source code)
  - Lines per file (without blank lines, comments, imports or package): median, max and files with more
    than 200 lines.
  - Lines per method (without blank lines or comments, from the signature to the closing brace) and
    parameters per method. Methods with more than 30 lines and methods with more than 3 parameters are counted.
"""
import re
import statistics as st
import subprocess
import sys
from collections import defaultdict
from pathlib import Path

PROJECT = "tictactoe"
MAX_LINES = 30
MAX_PARAMS = 3
MAX_FILE = 200
YC_TYPES = ["data", "stamp", "control", "external", "common", "content"]
YC_WEIGHT = {t: i + 1 for i, t in enumerate(YC_TYPES)}
DEVICES = {
    "console": r"java/lang/System\.(out|in|err)|java/util/Scanner|java/io/Console",
    "files": r"java/io/File|java/nio/file/|java/io/(Buffered|File)(Reader|Writer)",
    "process": r"java/lang/System\.exit|java/lang/Runtime",
}


def _sh(cmd):
    p = subprocess.run(cmd, capture_output=True, text=True)
    return p.stdout + p.stderr


def _outer(c):
    return c.split("$")[0]


def _short(c):
    return c.replace(PROJECT + ".", "")


def _in_project(name):
    return name.replace("/", ".").startswith(PROJECT + ".")


# ---------------------------------------------------------------- reading the bytecode (javap)
INSTR = re.compile(r"^\s+(\d+): ([a-z_0-9]+)\b\s*(.*)$")
REF = re.compile(r"// (Field|Method|InterfaceMethod) (?:(\"?[\w/$\[;]+\"?)\.)?(\"?[\w$<>]+\"?):(\S+)")
MODS = r"(?:(?:public|private|protected|static|final|abstract|synchronized|native|default|strictfp|transient|volatile) )*"


def read_class(path):
    """Structure of a class from `javap -c -p -s`."""
    txt = _sh(["javap", "-c", "-p", "-s", str(path)])
    head = re.search(r"^(.*?)\b(class|interface) ([\w.$]+)(.*)\{\s*$", txt, re.M)
    if not head:
        return None
    name = head.group(3)
    extends = head.group(4)
    kind = ("interface" if head.group(2) == "interface" else "enum" if "java.lang.Enum<" in extends
            else "record" if "java.lang.Record" in extends else "abstract" if "abstract" in head.group(1) else "class")
    c = {"name": name, "internal": name.replace(".", "/"), "kind": kind, "fields": {}, "methods": [], "txt": txt}
    last = None
    for l in txt.splitlines():
        if re.match(r"^  \S", l) and l.rstrip().endswith(";"):
            decl = l.strip()[:-1]
            if decl == "static {}":
                last = {"name": "<clinit>", "mods": "static ", "signature": decl, "desc": "()V", "instr": [], "ctor": True}
                c["methods"].append(last)
            elif "(" in decl:
                m = re.match(rf"^({MODS})(.*?)([\w$<>]+)\((.*)\)(?: throws .*)?$", decl)
                if not m:
                    last = None
                    continue
                n = m.group(3)
                ctor = n == name.split(".")[-1]
                last = {"name": "<init>" if ctor else n, "mods": m.group(1), "signature": decl, "desc": "",
                        "instr": [], "ctor": ctor}
                c["methods"].append(last)
            else:
                m = re.match(rf"^({MODS})(.+) ([\w$]+)$", decl)
                if m:
                    c["fields"][m.group(3)] = {"mods": m.group(1), "type": m.group(2)}
                last = c["fields"].get(m.group(3)) if m else None
        elif l.strip().startswith("descriptor:") and last is not None:
            last["desc"] = l.split("descriptor:")[1].strip()
        elif last is not None and "instr" in last:
            m = INSTR.match(l)
            if m:
                ref = REF.search(m.group(3))
                last["instr"].append((m.group(2), m.group(3), ref.groups() if ref else None))
    return c


def _own(c, owner):
    return owner is None or owner.strip('"') == c["internal"]


def _params(desc):
    """Parameter types of a descriptor: ['I', 'Ltictactoe/Board;', '[[C', ...]."""
    m = re.match(r"\((.*)\)", desc or "")
    return re.findall(r"\[*(?:[BCDFIJSZ]|L[^;]+;)", m.group(1)) if m else []


CONSTANT = re.compile(r"^(iconst_\w+|bipush|sipush|aconst_null)$")


def _is_guard(ins, j):
    """The branch at ins[j] is a guard: one of its paths only throws an exception or returns a constant."""
    nxt = [op for op, _, _ in ins[j + 1:j + 7]]
    if "athrow" in nxt[:6] and not any(re.match(r"^if|switch", op) for op in nxt[:nxt.index("athrow")]):
        return True
    return bool(nxt) and (nxt[0] == "return" or (len(nxt) > 1 and CONSTANT.match(nxt[0]) and nxt[1].endswith("return")))


def _flags(c, met, enums):
    """Indexes of the boolean or enum parameters the method uses to choose what to do (control coupling):
    `if (flag)`, a switch on the parameter or a comparison with an enum constant. Guards (validate and throw an
    exception, or return a constant) do not count, nor does comparing the parameter with another value (that is using the data)."""
    slot = 0 if "static" in met["mods"] else 1
    slots = {}
    for i, p in enumerate(_params(met["desc"])):
        if p == "Z" or (p.startswith("L") and p[1:-1] in enums):
            slots[slot] = (i, p)
        slot += 2 if p in ("J", "D") else 1
    used = set()
    ins = met["instr"]
    for i, (op, arg, _) in enumerate(ins):
        m = re.match(r"^[ai]load(?:_(\d)|$)", op)
        if not m:
            continue
        s = int(m.group(1)) if m.group(1) else int(arg.split()[0]) if arg.split() and arg.split()[0].isdigit() else None
        if s not in slots:
            continue
        idx, typ = slots[s]
        nxt = ins[i + 1:i + 4]
        ops = [o for o, _, _ in nxt]
        if typ == "Z":
            j = i + 1 if ops[:1] and ops[0] in ("ifeq", "ifne") else None
        elif ops[:1] == ["getstatic"] and nxt[0][2] and nxt[0][2][1] and nxt[0][2][1].strip('"') == typ[1:-1] \
                and len(ops) > 1 and ops[1].startswith("if_acmp"):
            j = i + 2                                   # color == Color.X
        elif ops[:3] and ops[0] == "invokevirtual" and ".ordinal:" in nxt[0][1] and len(ops) > 2 \
                and ops[1] == "iaload" and ops[2] in ("tableswitch", "lookupswitch"):
            j = i + 3                                   # switch (color)
        else:
            j = None
        if j is not None and not _is_guard(ins, j):
            used.add(idx)
    return used


# ---------------------------------------------------------------- coupling
def _jdeps(classes_dir):
    out = _sh(["jdeps", "-verbose:class", "-filter:none", str(classes_dir)])
    ce, classes = defaultdict(set), set()
    for l in out.splitlines():
        m = re.match(r"\s+(\S+)\s+->\s+(\S+)", l)
        if m:
            a, b = _outer(m.group(1)), _outer(m.group(2))
            if a.startswith(PROJECT):
                classes.add(a)
                if b.startswith(PROJECT) and a != b:
                    ce[a].add(b)
    return ce, classes


def propagation_cost(ce, classes):
    """Density of the reachability matrix (diagonal included), in %, and how many classes a change in each one reaches."""
    cl = sorted(classes)
    if not cl:
        return None, {}
    reach = {}
    for a in cl:
        seen, stack = {a}, [a]
        while stack:
            for b in ce.get(stack.pop(), ()):
                if b not in seen:
                    seen.add(b)
                    stack.append(b)
        reach[a] = seen
    affected = {b: sum(1 for a in cl if b in reach[a]) - 1 for b in cl}  # classes that depend on b, directly or indirectly
    return round(100 * sum(len(v) for v in reach.values()) / len(cl) ** 2, 1), affected


def yourdon_constantine(classes):
    """Connections between methods classified by Yourdon & Constantine type."""
    enums = {c["internal"] for c in classes if c["kind"] == "enum"}
    method = {}
    by_name = defaultdict(list)
    for c in classes:
        for m in c["methods"]:
            method[(c["internal"], m["name"], m["desc"])] = (c, m)
            by_name[(m["name"], m["desc"])].append((c, m))
    flags = {(c["internal"], m["name"], m["desc"]): _flags(c, m, enums) for c in classes for m in c["methods"] if m["instr"]}

    def param_type(p):
        if p.startswith("["):
            return "stamp"
        if not p.startswith("L"):
            return "data"
        t = p[1:-1]
        if t in enums or t in ("java/lang/String",) or re.match(r"java/lang/(Integer|Long|Short|Byte|Character|Boolean|Double|Float)$", t):
            return "data"
        return "stamp"

    connections = {}  # (class A, method or "↔", class B, method, field or resource) -> type
    pairs = {}        # (A, B) or (A, B, "↔") -> worst type
    def mark(pair, t):
        if YC_WEIGHT[t] > YC_WEIGHT.get(pairs.get(pair), 0):
            pairs[pair] = t

    globals_ = {(c["internal"], f) for c in classes for f, d in c["fields"].items()
                if "static" in d["mods"] and "final" not in d["mods"] and "$" not in f}
    use_global = defaultdict(set)
    devices = defaultdict(set)
    for c in classes:
        a = _outer(c["name"])
        for dev, rx in DEVICES.items():
            if re.search(rx, c["txt"]):
                devices[dev].add(a)
        for m in c["methods"]:
            origin = re.sub(r"^lambda\$(.+)\$\d+$", r"\1", m["name"])
            for op, arg, ref in m["instr"]:
                if not ref:
                    continue
                kind, owner, name, desc = ref
                owner = (owner or c["internal"]).strip('"')
                if not _in_project(owner):
                    continue
                b = _outer(owner.replace("/", "."))
                if kind == "Field":
                    if (owner, name) in globals_:
                        use_global[(owner, name)].add(a)
                    elif op in ("getfield", "putfield") and b != a:
                        connections[(a, origin, b, f"{name} (field)")] = "content"
                        mark((a, b), "content")
                    continue
                target_m = method.get((owner, name.strip('"'), desc))
                candidates = [target_m] if target_m and target_m[1]["instr"] else by_name.get((name.strip('"'), desc), [])
                control = any(flags.get((cc["internal"], mm["name"], mm["desc"])) for cc, mm in candidates)
                params = [param_type(p) for p in _params(desc)]
                t = "control" if control else "stamp" if "stamp" in params else "data"
                target = name.strip('"')
                if (a, origin) == (b, target):
                    continue
                k = (a, origin, b, target)
                if YC_WEIGHT[t] > YC_WEIGHT.get(connections.get(k), 0):
                    connections[k] = t
                if b != a:
                    mark((a, b), t)
    for (owner, f), who in use_global.items():
        holder = _outer(owner.replace("/", "."))
        who = sorted(who | ({holder} if len(who - {holder}) else set()))
        for i, x in enumerate(who):
            for y in who[i + 1:]:
                mark((x, y, "↔"), "common")
                connections[(x, "↔", y, f"{_short(holder)}.{f} (global)")] = "common"
    for dev, who in devices.items():
        q = sorted(who)
        for i, x in enumerate(q):
            for y in q[i + 1:]:
                mark((x, y, "↔"), "external")
                connections[(x, "↔", y, dev)] = "external"

    between = {k: t for k, t in connections.items() if k[0] != k[2]}
    internal = {k: t for k, t in connections.items() if k[0] == k[2]}
    def text(k):
        a, ma, b, mb = k
        return f"{_short(a)} ↔ {_short(b)} ({mb})" if ma == "↔" else f"{_short(a)}.{ma} → {_short(b)}.{mb}"
    count = lambda d: {t: sum(1 for x in d.values() if x == t) for t in YC_TYPES}
    weights = [YC_WEIGHT[t] for t in pairs.values()]
    worst = [f"{text(k)}: {t}" for k, t in sorted(between.items(), key=lambda x: (-YC_WEIGHT[x[1]], x[0])) if YC_WEIGHT[t] >= 3]
    internal_control = [text(k) for k, t in sorted(internal.items()) if t == "control"]
    return {"yc_connections": count(between), "yc_internal_connections": count(internal),
            "yc_pairs": count(pairs), "yc_index": round(st.mean(weights), 2) if weights else None,
            "yc_worst": max(pairs.values(), key=YC_WEIGHT.get) if pairs else None,
            "yc_detail": worst[:20], "yc_internal_control": internal_control[:20],
            "yc_by_pair": sorted(f"{_short(p[0])} {'↔' if len(p) == 3 else '→'} {_short(p[1])}: {t}" for p, t in pairs.items())}


# ---------------------------------------------------------------- cohesion
def cohesion(c):
    """LCOM*, LCOM4, TCC and LCC of a class."""
    empty = {"methods": 0, "fields": 0, "LCOM*": None, "LCOM4": None, "TCC": None, "LCC": None}
    if c["kind"] == "interface":
        return empty
    fields = {f for f, d in c["fields"].items() if "static" not in d["mods"]}
    exclude = {"values", "valueOf", "<init>", "<clinit>"} | ({"equals", "hashCode", "toString"} if c["kind"] == "record" else set())
    instance = {m["name"] for m in c["methods"]
                if m["instr"] and "static" not in m["mods"] and m["name"] not in exclude
                and not m["name"].startswith(("lambda$", "access$", "$"))}
    visible = {m["name"] for m in c["methods"] if m["name"] in instance and "private" not in m["mods"]}
    use = {k: (set(), set()) for k in instance}  # method -> (fields it uses, own methods it calls)
    for m in c["methods"]:
        lam = re.match(r"lambda\$(.+)\$\d+$", m["name"])
        k = lam.group(1) if lam else m["name"]
        if k not in use:
            continue
        for op, arg, ref in m["instr"]:
            if not ref or not _own(c, ref[1]):
                continue
            n = ref[2].strip('"')
            if ref[0] == "Field" and n in fields:
                use[k][0].add(n)
            elif ref[0] != "Field":
                n = re.sub(r"^lambda\$(.+)\$\d+$", r"\1", n)
                if n in use and n != k:
                    use[k][1].add(n)
    r = dict(empty, methods=len(use), fields=len(fields))
    if not use or not fields:
        return r

    # LCOM* without getters: getters (read a single field and call nothing) inflate LCOM* in value objects.
    def getter(k, v):
        if len(v[0]) != 1 or v[1]:
            return False
        f = next(iter(v[0])).lower()
        return k.lower() in (f, "get" + f, "is" + f)
    ms = {k: v for k, v in use.items() if not getter(k, v)} or use
    if len(ms) > 1:
        mu = sum(sum(1 for v in ms.values() if f in v[0]) for f in fields) / len(fields)
        r["LCOM*"] = round(max(0.0, (len(ms) - mu) / (len(ms) - 1)), 2)

    # LCOM4: connected components of the method-method graph (they share a field or one calls the other).
    parent = {k: k for k in use}
    def root(x):
        while parent[x] != x:
            x = parent[x]
        return x
    ks = list(use)
    for i, a in enumerate(ks):
        for b in ks[i + 1:]:
            if use[a][0] & use[b][0] or b in use[a][1] or a in use[b][1]:
                parent[root(a)] = root(b)
    r["LCOM4"] = len({root(k) for k in ks})

    # TCC and LCC: pairs of visible methods connected by fields used directly or indirectly.
    def transitive_fields(k, seen=None):
        seen = seen or set()
        if k in seen:
            return set()
        seen.add(k)
        fs = set(use[k][0])
        for called in use[k][1]:
            fs |= transitive_fields(called, seen)
        return fs
    vis = sorted(visible)
    if len(vis) > 1:
        tf = {k: transitive_fields(k) for k in vis}
        pairs = [(a, b) for i, a in enumerate(vis) for b in vis[i + 1:]]
        direct = {(a, b) for a, b in pairs if tf[a] & tf[b]}
        r["TCC"] = round(len(direct) / len(pairs), 2)
        group = {k: k for k in vis}
        def g(x):
            while group[x] != x:
                x = group[x]
            return x
        for a, b in direct:
            group[g(a)] = g(b)
        r["LCC"] = round(sum(1 for a, b in pairs if g(a) == g(b)) / len(pairs), 2)
    return r


# ---------------------------------------------------------------- encapsulation
def encapsulation(top_classes):
    enc = {"fields": 0, "private_fields": 0, "methods": 0, "public_methods": 0}
    mutators = defaultdict(list)   # class -> public methods that write an own field
    returns = []                   # (class, method, type of the returned field)
    for c in top_classes:
        for f, d in c["fields"].items():
            if "static" in d["mods"] and "final" in d["mods"]:
                continue  # constants and enum values
            enc["fields"] += 1
            enc["private_fields"] += "private" in d["mods"]
        for m in c["methods"]:
            n = m["name"]
            if m["ctor"] or n.startswith(("lambda$", "$")) or n in ("values", "valueOf"):
                continue
            public = "public" in m["mods"]
            enc["methods"] += 1
            enc["public_methods"] += public
            if not public:
                continue
            arrays_read = False
            ins = m["instr"]
            for i, (op, arg, ref) in enumerate(ins):
                own = ref and ref[0] == "Field" and _own(c, ref[1])
                if op == "getfield" and ref and ref[3].startswith("["):
                    arrays_read = True
                if (arrays_read and re.match(r"^[abcdfils]astore$", op)) or (op == "putfield" and own and ref[2] in c["fields"]):
                    if n not in mutators[c["name"]]:
                        mutators[c["name"]].append(n)
                if op == "getfield" and own and i + 1 < len(ins) and ins[i + 1][0] == "areturn":
                    returns.append((c["name"], n, c["fields"].get(ref[2], {}).get("type", "")))
    mutable = {c for c, v in mutators.items() if v}
    leaks = sorted({f"{_short(c)}.{m}() returns {_short(t)}" for c, m, t in returns if t.endswith("]") or t in mutable})
    return {"private_fields": f"{enc['private_fields']}/{enc['fields']}",
            "private_fields_pct": round(100 * enc["private_fields"] / enc["fields"]) if enc["fields"] else None,
            "public_methods_pct": round(100 * enc["public_methods"] / enc["methods"]) if enc["methods"] else None,
            "public_mutators": sorted(f"{_short(c)}.{m}" for c, ms in mutators.items() for m in ms),
            "state_leaks": leaks}


# ---------------------------------------------------------------- all together
def _med(xs, nd=2):
    xs = [x for x in xs if x is not None]
    return round(st.median(xs), nd) if xs else None


def design(classes_dir):
    """Coupling, cohesion and encapsulation of the classes compiled in classes_dir."""
    classes_dir = Path(classes_dir)
    ce, names = _jdeps(classes_dir)
    ca = defaultdict(set)
    for a, bs in ce.items():
        for b in bs:
            ca[b].add(a)
    every = [c for c in (read_class(f) for f in sorted(classes_dir.rglob("*.class"))) if c]
    top = [c for c in every if "$" not in c["name"]]
    io = sorted({_short(_outer(c["name"])) for c in every if re.search(DEVICES["console"], c["txt"])})
    coh = {c["name"]: cohesion(c) for c in top}
    pc, affected = propagation_cost(ce, names)
    table = []
    for n in sorted(names):
        e, a = len(ce[n]), len(ca[n])
        k = coh.get(n, {})
        table.append({"class": _short(n), "Ce": e, "Ca": a, "I": round(e / (e + a), 2) if e + a else 0.0,
                      "change_reach": affected.get(n, 0), "methods": k.get("methods", 0), "fields": k.get("fields", 0),
                      "LCOM*": k.get("LCOM*"), "LCOM4": k.get("LCOM4"), "TCC": k.get("TCC"), "LCC": k.get("LCC")})
    pk = defaultdict(set)
    for a, bs in ce.items():
        for b in bs:
            pa, pb = a.rsplit(".", 1)[0], b.rsplit(".", 1)[0]
            if pa != pb:
                pk[pa].add(pb)
    cycles = sorted({tuple(sorted((a, b))) for a in pk for b in pk[a] if a in pk.get(b, ())})
    ces = [t["Ce"] for t in table] or [0]
    cas = [t["Ca"] for t in table] or [0]
    r = {"classes": len(names), "packages": len({c.rsplit('.', 1)[0] for c in names}),
         "Ce_median": round(st.median(ces), 1), "Ce_max": max(ces), "Ca_max": max(cas),
         "propagation_cost": pc,
         "LCOM*_median": _med(t["LCOM*"] for t in table),
         "classes_LCOM4>1": sum(1 for t in table if (t["LCOM4"] or 0) > 1),
         "TCC_median": _med(t["TCC"] for t in table), "LCC_median": _med(t["LCC"] for t in table),
         "classes_TCC<0.5": sorted(t["class"] for t in table if t["TCC"] is not None and t["TCC"] < 0.5),
         "classes_with_IO": io,
         "package_dependencies": {k.replace(PROJECT, "(root)") if k == PROJECT else k: sorted(v) for k, v in pk.items()},
         "package_cycles": cycles, "by_class": table}
    r.update(yourdon_constantine(every))
    r.update(encapsulation(top))
    return r


# ---------------------------------------------------------------- methods: lines and parameters (source code)
NOT_METHOD = {"if", "for", "while", "switch", "catch", "synchronized", "try", "else", "do", "return", "new", "throw", "case"}


def _clean(code):
    """Removes comments and empties literals, keeping the line breaks."""
    def blank(m):
        return re.sub(r"[^\n]", " ", m.group(0))
    code = re.sub(r'"""(?:.|\n)*?"""', blank, code)
    code = re.sub(r"/\*(?:.|\n)*?\*/", blank, code)
    code = re.sub(r"//[^\n]*", blank, code)
    code = re.sub(r'"(?:\\.|[^"\\\n])*"', '""', code)
    return re.sub(r"'(?:\\.|[^'\\\n])+'", "' '", code)


def _count_params(p):
    p = re.sub(r"@\w+(\([^)]*\))?\s*", "", p).strip()
    if not p:
        return 0
    depth, n = 0, 1
    for ch in p:
        depth += ch == "<"
        depth -= ch == ">"
        n += ch == "," and depth == 0
    return n


def source_methods(sources):
    """sources: {relative path: Java code}. Returns a list of methods and constructors with their lines and parameters."""
    out = []
    for path, code in sorted(sources.items()):
        clean = _clean(code)
        lines = clean.split("\n")
        cls = Path(path).stem
        stack = []  # positions of the open braces
        last_cut = 0
        for i, ch in enumerate(clean):
            if ch in ";}":
                if ch == "}" and stack:
                    start, met = stack.pop()
                    if met:
                        l0 = clean.count("\n", 0, met["pos"])
                        l1 = clean.count("\n", 0, i)
                        met["lines"] = sum(1 for l in lines[l0:l1 + 1] if l.strip())
                        out.append(met)
                last_cut = i + 1
            elif ch == "{":
                head = clean[last_cut:i]
                met = None
                m = re.search(r"\b(\w+)\s*\(((?:[^()]|\([^()]*\))*)\)\s*(?:throws\s+[\w.,\s]+)?\s*$", head)
                # A method or constructor: "type name(params) {" or "Class(params) {", but not if/for/while...,
                # nor an anonymous class (new X() {), nor a record, nor a lambda, nor an initializer.
                is_method = (m and m.group(1) not in NOT_METHOD
                             and not re.search(r"\b(new|class|record|interface|enum)\b|->|=", head)
                             and (re.search(r"\w", head[:m.start(1)]) or m.group(1) == cls))
                if is_method:
                    met = {"class": cls, "method": m.group(1), "params": _count_params(m.group(2)),
                           "pos": last_cut + m.start(1)}
                stack.append((i, met))
                last_cut = i + 1
    return [{k: v for k, v in m.items() if k != "pos"} for m in out]


def file_lines(code):
    """Code lines of a file: without blank lines, comments, imports or package."""
    return sum(1 for l in _clean(code).splitlines() if l.strip() and not l.strip().startswith(("import ", "package ")))


def size_metrics(sources):
    """Size of files and methods, and parameters per method."""
    by_file = {Path(f).with_suffix("").as_posix(): file_lines(c) for f, c in sources.items()}
    lf = sorted(by_file.values())
    files = {"files": len(lf), "file_lines_median": _med(lf, 1), "file_lines_max": max(lf) if lf else None,
             f"files_>{MAX_FILE}_lines": sum(1 for x in lf if x > MAX_FILE),
             "lines_by_file": dict(sorted(by_file.items(), key=lambda x: -x[1]))}
    return dict(files, **_method_metrics(sources))


def _method_metrics(sources):
    ms = source_methods(sources)
    long_ = sorted((m for m in ms if m["lines"] > MAX_LINES), key=lambda m: -m["lines"])
    many = sorted((m for m in ms if m["params"] > MAX_PARAMS), key=lambda m: -m["params"])
    lin = [m["lines"] for m in ms]
    return {"methods_total": len(ms),
            f"methods_>{MAX_LINES}_lines": len(long_),
            f"methods_>{MAX_PARAMS}_params": len(many),
            "methods_up_to_30_lines_pct": round(100 * (len(ms) - len(long_)) / len(ms)) if ms else None,
            "method_lines_median": _med(lin, 1), "method_lines_max": max(lin) if lin else None,
            "params_max": max((m["params"] for m in ms), default=None),
            "long_methods": [f"{m['class']}.{m['method']} ({m['lines']} lines)" for m in long_],
            "many_params_methods": [f"{m['class']}.{m['method']} ({m['params']} parameters)" for m in many]}


if __name__ == "__main__":
    import json
    if len(sys.argv) < 2:
        sys.exit(__doc__)
    r = design(sys.argv[1])
    if len(sys.argv) > 2:
        base = Path(sys.argv[2])
        r.update(size_metrics({str(p.relative_to(base)): p.read_text(encoding="utf-8") for p in base.rglob("*.java")}))
    print(json.dumps(r, indent=2, ensure_ascii=False))
