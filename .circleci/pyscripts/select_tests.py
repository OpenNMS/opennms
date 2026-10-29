#!/usr/bin/env python3

"""
Select the integration and smoke test classes that a pull request can affect.

The script makes a catalog of test classes from the source tree, sends it with
the PR diff to the Claude API, and writes the selection to an output directory.
Fixed rules come first: some changes always cause a full run, and some tests
always run. Any error also causes a full run.
"""

import argparse
import json
import os
import re
import subprocess
import sys
from collections import Counter

MODEL = "claude-opus-5-5"

# Changes here reach most of the suite, so the model is not asked.
FULL_RUN_PATHS = [
    r"^pom\.xml$", r"^core/xml/src/main/", r"^core/lib/src/main/", r"^core/spring/src/main/",
    r"^core/soa/src/main/", r"^core/test-api/", r"^opennms-model/src/main/", r"^opennms-dao-api/src/main/",
    r"^container/features/", r"^dependencies/.*pom\.xml$", r"^\.circleci/scripts/itest\.sh$",
]
FULL_SMOKE_PATHS = [r"^smoke-test/pom\.xml$", r"^smoke-test/src/main/", r"^opennms-container/",
                    r"^\.circleci/scripts/smoke\.sh$"]
MAX_CHANGED_FILES = 100
# About 100K tokens; a larger diff costs much and gives little benefit.
MAX_DIFF_CHARS = 400_000

# The model gets only the names of changed files under these paths.
EXCLUDE_FROM_DIFF = ("ui/", "docs/", "core/web-assets/")

IMPORT_RE = re.compile(r"^import (?:static )?org\.opennms\.([\w.]+)\.\w+;", re.M)
CONFIG_CLASS_RE = re.compile(r"^import (?:static )?org\.opennms\.[\w.]*\.(\w+(?:Factory|Dao|ConfigManager))(?:\.\w+)?;", re.M)
CTX_RE = re.compile(r'"(?:classpath\*?:|file:)[^"]*?([\w\-.]+)\.xml"')
CONST_RE = re.compile(r'static final String (\w+)\s*=\s*"(?:classpath\*?:|file:)[^"]*?([\w\-.]+)\.xml"')
RUNS_RE = re.compile(r'"(opennms:[\w\-]+)|"((?:/opennms)?/(?:rest|api/v2)/[\w\-]+(?:/[\w\-]+)?)')
EXTENDS_RE = re.compile(r"\bclass\s+\w+(?:<[^>]*>)?\s+extends\s+(\w+)")

SCHEMA = {
    "type": "object",
    "properties": {
        "change_summary": {"type": "string"},
        "its": {"type": "array", "items": {"type": "string"}},
        "smoke": {"type": "array", "items": {"type": "string"}},
        "reasons": {"type": "string"},
        "confidence": {"type": "string", "enum": ["high", "medium", "low"]},
    },
    "required": ["change_summary", "its", "smoke", "reasons", "confidence"],
    "additionalProperties": False,
}

RULES = """You select which OpenNMS test classes a pull request must run in CI.

You get a catalog of integration test (IT) and smoke test classes, and the PR diff.
Each catalog line: class | suite | module | ctx: Spring contexts and XML files it loads | uses: org.opennms packages it imports | config: config factory and DAO classes it imports | runs: Karaf shell commands and REST paths it calls.
For files under ui/, docs/ and core/web-assets/ you get only the file names, not the diff.
A change to a default config file under opennms-base-assembly/src/main/filtered/etc affects every test that reads that config through its factory or DAO, for example poller-configuration.xml through PollerConfigFactory.
Smoke tests run against the assembled Horizon/Minion/Sentinel containers.

Select every test whose result the change could plausibly affect:
- Tests that exercise the changed classes, directly or through the daemons, DAOs, configs or REST/UI paths that use them.
- Tests for features that read the changed config files, schemas or resources.
- Smoke tests that cover the user-visible feature, daemon or Minion/Sentinel path that the change touches.

Do not select tests that only share generic infrastructure (DAO layer, Spring wiring, test utilities) with the change.
A missed test is worse than an extra one. When unsure about a test, include it.
Return class names exactly as written in the catalog. In reasons, name the code paths that link the change to the selected groups."""


def git(*args):
    return subprocess.check_output(["git", *args], text=True, errors="replace")


def read_files(ref, paths):
    """Read many files at one ref with a single git cat-file process."""
    proc = subprocess.run(
        ["git", "cat-file", "--batch"], input="".join(f"{ref}:{p}\n" for p in paths).encode(),
        capture_output=True, check=True)
    out, data, pos = {}, proc.stdout, 0
    for p in paths:
        header_end = data.index(b"\n", pos)
        header = data[pos:header_end].split()
        if header[-1] == b"missing":
            pos = header_end + 1
            continue
        size = int(header[2])
        out[p] = data[header_end + 1:header_end + 1 + size].decode("utf-8", "replace")
        pos = header_end + 1 + size + 1
    return out


def build_catalog(ref):
    tree = git("ls-tree", "-r", "--name-only", ref).split()
    files = [f for f in tree if "/src/test/java/" in f and f.endswith(".java")]
    test_api = [f for f in tree if f.startswith("core/test-api/") and "/src/main/java/" in f and f.endswith(".java")]
    sources = read_files(ref, files + test_api)
    by_simple = {}
    for f in test_api + files:
        by_simple.setdefault(f.rsplit("/", 1)[1][:-5], []).append(f)
    consts = {name: xml for src in sources.values() for name, xml in CONST_RE.findall(src)}

    def parent_file(name, child):
        """Find the file of a parent class: the only match, a match in the same package, or an imported match."""
        paths = by_simple.get(name, [])
        if len(paths) == 1:
            return paths[0]
        package = child.split("/java/", 1)[1].rsplit("/", 1)[0]
        for p in paths:
            if p.split("/java/", 1)[1].rsplit("/", 1)[0] == package:
                return p
        src = sources.get(child, "")
        for p in paths:
            if f"import {p.split('/java/', 1)[1][:-5].replace('/', '.')};" in src:
                return p
        return None

    def own(f):
        src = sources.get(f, "")
        ctx = set(CTX_RE.findall(src)) | {xml for name, xml in consts.items() if name in src}
        pkgs = {".".join(p.split(".")[:3]) for p in IMPORT_RE.findall(src)}
        runs = {a or b.replace("/opennms", "", 1) for a, b in RUNS_RE.findall(src)}
        configs = set(CONFIG_CLASS_RE.findall(src))
        m = EXTENDS_RE.search(src)
        return ctx, pkgs, runs, configs, (m.group(1) if m else None)

    entries = []
    for f in files:
        name = f.rsplit("/", 1)[1]
        if not re.search(r"IT(?:Case)?\.java$", name) or name.startswith("Abstract"):
            continue
        src = sources.get(f, "")
        if re.search(r"^(?:public )?abstract class", src, re.M):
            continue
        ctx, pkgs, runs, configs, parent = own(f)
        text, child = src, f
        for _ in range(3):  # merge parents up to three levels
            pfile = parent and parent_file(parent, child)
            if not pfile:
                break
            pctx, ppkgs, pruns, pconfigs, parent = own(pfile)
            text, child = text + sources.get(pfile, ""), pfile
            ctx |= pctx
            pkgs |= ppkgs
            runs |= pruns
            configs |= pconfigs
        module = f.split("/src/test/")[0]
        if module == "smoke-test":
            suite = ("smoke-minion" if "MinionTests" in src else "smoke-sentinel" if "SentinelTests" in src
                     else "smoke-flaky" if "FlakyTests" in src else "smoke-core")
        else:
            suite = "it"
        if suite == "smoke-flaky":  # CI does not run the flaky suite
            continue
        cls = f.split("src/test/java/")[1][:-5].replace("/", ".")
        entries.append({"cls": cls, "suite": suite, "module": module, "ctx": ctx, "pkgs": pkgs,
                        "runs": runs, "configs": configs, "text": text})

    # Drop entries that most classes share; they do not help the choice.
    n = len(entries)
    common = {k for k, c in Counter(x for e in entries for x in e["ctx"] | e["pkgs"]).items() if c > 0.3 * n}
    lines = []
    for e in sorted(entries, key=lambda e: e["cls"]):
        lines.append(" | ".join([
            e["cls"], e["suite"], e["module"],
            "ctx: " + ", ".join(sorted(e["ctx"] - common)),
            "uses: " + ", ".join(sorted(e["pkgs"] - common)),
            "config: " + ", ".join(sorted(e["configs"])),
            "runs: " + ", ".join(sorted(e["runs"])[:15]),
        ]))
    return entries, lines


def module_of(path, poms):
    parts = path.split("/")
    for k in range(len(parts) - 1, 0, -1):
        d = "/".join(parts[:k])
        if d in poms:
            return d
    return ""


def ask_model(system, user):
    import anthropic

    headers = {}
    if os.environ.get("ANTHROPIC_WORKSPACE_ID"):
        headers["anthropic-workspace-id"] = os.environ["ANTHROPIC_WORKSPACE_ID"]
    client = anthropic.Anthropic(timeout=600, max_retries=2, default_headers=headers or None)
    with client.beta.messages.stream(
        model=MODEL, max_tokens=32000,
        betas=["server-side-fallback-2026-07-01"], fallbacks="default",
        output_config={"effort": "medium", "format": {"type": "json_schema", "schema": SCHEMA}},
        system=system, messages=[{"role": "user", "content": user}],
    ) as stream:
        resp = stream.get_final_message()
    if resp.stop_reason != "end_turn":
        raise RuntimeError(f"stop_reason={resp.stop_reason}")
    text = next(b.text for b in resp.content if b.type == "text")
    return json.loads(text), resp.usage.model_dump()


def select(ref, base):
    merge_base = git("merge-base", base, ref).strip()
    changed = git("diff", "--name-only", f"{merge_base}..{ref}").split()
    result = {"ref": git("rev-parse", ref).strip(), "base": base, "merge_base": merge_base, "changed_files": len(changed)}

    full_run = sorted({p for p in changed for r in FULL_RUN_PATHS if re.search(r, p)})
    code_changes = [p for p in changed if not p.startswith(EXCLUDE_FROM_DIFF)]
    if len(code_changes) > MAX_CHANGED_FILES:
        full_run.append(f"{len(code_changes)} changed files > {MAX_CHANGED_FILES}")
    if full_run:
        return {**result, "mode": "full_run", "reason": "rule", "paths": full_run}
    if not code_changes:
        return {**result, "mode": "full_run", "reason": "no code changes"}

    entries, lines = build_catalog(ref)
    known = {e["cls"]: e["suite"] for e in entries}
    poms = {p.rsplit("/", 1)[0] for p in git("ls-tree", "-r", "--name-only", ref).split() if p.endswith("/pom.xml")}
    changed_modules = {module_of(p, poms) for p in changed} - {""}
    changed_xml = {p.rsplit("/", 1)[1][:-4] for p in changed if p.endswith(".xml") and "/src/main/" in p}
    changed_tests = {p.split("/src/test/java/")[1][:-5].replace("/", ".") for p in changed
                     if "/src/test/java/" in p and p.endswith(".java")}
    # A changed test class runs, and so does every test that uses it as a helper or (grand)parent class.
    used = re.compile(r"\b(?:" + "|".join(sorted(c.rsplit(".", 1)[1] for c in changed_tests)) + r")\b")
    always = sorted(e["cls"] for e in entries if changed_tests and used.search(e["text"])
                    or e["suite"] == "it" and (e["module"] in changed_modules or e["ctx"] & changed_xml))
    full_smoke = sorted({p for p in changed for r in FULL_SMOKE_PATHS if re.search(r, p)})

    diff = git("diff", f"{merge_base}..{ref}", "--", ".", ":(exclude)*.svg", ":(exclude)*package-lock.json",
               *[f":(exclude){d}" for d in EXCLUDE_FROM_DIFF])
    if len(diff) > MAX_DIFF_CHARS:
        return {**result, "mode": "full_run", "reason": f"diff has {len(diff)} characters > {MAX_DIFF_CHARS}"}
    user = ("Changed files:\n" + "\n".join(changed)
            + "\n\nTests that always run (the PR changes them or a test class they use, or ITs in the same module as a change,"
            + " or ITs that load a changed Spring XML file):\n"
            + ("\n".join(always) or "(none)") + f"\n\nDiff:\n{diff}")
    system = RULES + "\n\nCatalog:\n" + "\n".join(lines)
    sel, usage = ask_model(system, user)

    # The model sometimes names a test in its reasons but leaves it out of the lists; include those.
    by_simple = {}
    for c in known:
        by_simple.setdefault(c.rsplit(".", 1)[1], []).append(c)
    named = set(re.findall(r"\b(\w+IT\w*)\b", sel["reasons"]))
    listed = {c.rsplit(".", 1)[1] for c in sel["its"] + sel["smoke"]}
    added_from_reasons = sorted(c for name in named - listed for c in by_simple.get(name, []))

    picked = set(sel["its"]) | set(sel["smoke"]) | set(added_from_reasons) | set(always)
    its = sorted(c for c in picked if known.get(c) == "it")
    smoke_all = {c for c, s in known.items() if s.startswith("smoke")}
    smoke = sorted(smoke_all if full_smoke else picked & smoke_all)
    return {**result, "mode": "selected", "its": its, "smoke": smoke, "always": always,
            "total_its": sum(1 for s in known.values() if s == "it"), "total_smoke": len(smoke_all),
            "full_smoke_paths": full_smoke, "added_from_reasons": added_from_reasons,
            "unknown_dropped": sorted(picked - set(known)),
            "change_summary": sel["change_summary"], "reasons": sel["reasons"], "confidence": sel["confidence"],
            "suites": {c: known[c] for c in smoke}, "model": MODEL, "usage": usage}


def write_outputs(out_dir, result):
    os.makedirs(out_dir, exist_ok=True)
    with open(os.path.join(out_dir, "selection.json"), "w") as f:
        json.dump(result, f, indent=2)
    if result["mode"] != "selected":
        return
    with open(os.path.join(out_dir, "it_classes.txt"), "w") as f:
        f.write("".join(c + "\n" for c in result["its"]))
    for suite in ("core", "minion", "sentinel"):
        with open(os.path.join(out_dir, f"smoke_{suite}.txt"), "w") as f:
            f.write("".join(c + "\n" for c in result["smoke"] if result["suites"][c] == f"smoke-{suite}"))


def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--base", required=True, help="base branch or commit of the pull request")
    ap.add_argument("--ref", default="HEAD")
    ap.add_argument("--out-dir", required=True)
    a = ap.parse_args()

    try:
        if not os.environ.get("ANTHROPIC_API_KEY"):
            result = {"mode": "full_run", "reason": "ANTHROPIC_API_KEY is not set"}
        else:
            result = select(a.ref, a.base)
    except Exception as e:  # any failure means a full run
        result = {"mode": "full_run", "reason": f"{type(e).__name__}: {e}"}

    write_outputs(a.out_dir, result)
    if result["mode"] == "selected":
        u = result["usage"]
        print(f"Selected {len(result['its'])} ITs and {len(result['smoke'])} smoke tests "
              f"({len(result['always'])} tests always run). Confidence: {result['confidence']}. "
              f"Tokens: {u['input_tokens']} in, {u['output_tokens']} out.")
        print(f"Change: {result['change_summary']}")
    else:
        print(f"Full run: {result['reason']} {result.get('paths', '')}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
