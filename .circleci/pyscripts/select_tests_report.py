#!/usr/bin/env python3

"""
Compare shadow-mode test selections with the tests that failed.

For each recent pipeline that ran the select-tests job, the script downloads the
selection artifact and the failed test classes of the integration and smoke jobs.
It prints the selected and total test counts, and whether the selection included
each failed class. A failure in a job that passed (after a retry) is flaky, and a
failure that also occurred on develop before the PR pipeline is not caused by the PR.
Neither is counted as a miss.
"""

import argparse
import datetime
import json
import os
import urllib.error
import urllib.request

API = "https://circleci.com/api/v2"
PROJECT = "gh/OpenNMS/opennms"


def get(url):
    req = urllib.request.Request(url)
    if os.environ.get("CIRCLE_TOKEN"):
        req.add_header("Circle-Token", os.environ["CIRCLE_TOKEN"])
    try:
        with urllib.request.urlopen(req, timeout=60) as r:
            return json.load(r)
    except urllib.error.HTTPError as e:
        if e.code == 404:
            return None
        raise


def failed_classes(job_number):
    failed, token = set(), None
    while True:
        page = get(f"{API}/project/{PROJECT}/{job_number}/tests" + (f"?page-token={token}" if token else ""))
        if page is None:
            return failed
        failed |= {t["classname"] for t in page["items"] if t["result"] in ("failure", "error")}
        token = page.get("next_page_token")
        if not token:
            return failed


def selection(job_number):
    artifacts = get(f"{API}/project/{PROJECT}/{job_number}/artifacts") or {"items": []}
    for a in artifacts["items"]:
        if a["path"].endswith("selection/selection.json"):
            return get(a["url"])
    return None


def failures(jobs):
    """Return the failed IT classes of the test jobs, split into real and flaky (the job passed after a retry)."""
    failed, flaky = set(), set()
    for j in jobs:
        if not j["name"].startswith(("integration-test", "smoke-test")) or j["status"] not in ("success", "failed"):
            continue
        classes = {c for c in failed_classes(j["job_number"]) if c.endswith(("IT", "ITCase"))}
        (flaky if j["status"] == "success" else failed).update(classes)
    return failed, flaky - failed


def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--days", type=int, default=7)
    a = ap.parse_args()
    cutoff = datetime.datetime.now(datetime.timezone.utc) - datetime.timedelta(days=a.days)

    records, develop_runs, token = [], [], None
    while True:
        page = get(f"{API}/project/{PROJECT}/pipeline" + (f"?page-token={token}" if token else "")) or {"items": []}
        for p in page["items"]:
            created = datetime.datetime.fromisoformat(p["created_at"].replace("Z", "+00:00"))
            if created < cutoff:
                break
            jobs = [j for w in (get(f"{API}/pipeline/{p['id']}/workflow") or {"items": []})["items"]
                    for j in (get(f"{API}/workflow/{w['id']}/job") or {"items": []})["items"] if j.get("job_number")]
            branch = p["vcs"].get("branch")
            if branch == "develop":
                develop_runs.append((created, failures(jobs)[0]))
                continue
            sel_job = next((j for j in jobs if j["name"] == "select-tests"), None)
            sel = sel_job and selection(sel_job["job_number"])
            if sel:
                records.append((p["number"], created, branch, sel, *(failures(jobs) if sel["mode"] == "selected" else (set(), set()))))
        else:
            token = page.get("next_page_token")
            if token:
                continue
        break

    totals = {"pipelines": len(records), "selected": 0, "full_run": 0, "selected_its": 0, "total_its": 0,
              "selected_smoke": 0, "total_smoke": 0, "failed": 0, "caught": 0, "missed": 0,
              "failing_on_develop": 0, "flaky": 0}
    for number, created, branch, sel, failed, flaky in records:
        if sel["mode"] != "selected":
            totals["full_run"] += 1
            print(f"{number} {branch}: full run ({sel.get('reason')})")
            continue
        totals["selected"] += 1
        picked = set(sel["its"]) | set(sel["smoke"])
        # Only develop failures from before the PR pipeline count; later ones can come from the PR after its merge.
        develop_failed = set().union(*(c for t, c in develop_runs if t < created))
        status = {c: "caught" if c in picked else "develop" if c in develop_failed else "MISSED" for c in failed}
        for key in ("its", "smoke"):
            totals[f"selected_{key}"] += len(sel[key])
            totals[f"total_{key}"] += sel.get(f"total_{key}", 0)
        totals["failed"] += len(failed)
        totals["caught"] += sum(1 for v in status.values() if v == "caught")
        totals["missed"] += sum(1 for v in status.values() if v == "MISSED")
        totals["failing_on_develop"] += sum(1 for v in status.values() if v == "develop")
        totals["flaky"] += len(flaky)
        failed_text = ", ".join(f"{c.rsplit('.', 1)[-1]}={v}" for c, v in sorted(status.items()))
        flaky_text = ", ".join(sorted(c.rsplit(".", 1)[-1] for c in flaky))
        print(f"{number} {branch}: ITs {len(sel['its'])}/{sel.get('total_its', '?')},"
              f" smoke {len(sel['smoke'])}/{sel.get('total_smoke', '?')}"
              + (f"; failed: {failed_text}" if failed else "") + (f"; flaky: {flaky_text}" if flaky else ""))
    print(json.dumps(totals))


if __name__ == "__main__":
    main()
