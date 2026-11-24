#!/usr/bin/env python3

"""
Compare shadow-mode test selections with the tests that failed.

For each recent pipeline that ran the select-tests job, the script downloads the
selection artifact and the failed test classes of the integration and smoke jobs.
It prints the selected and total test counts, and whether the selection included
each failed class. A failure that passed on a retry or a workflow rerun is flaky, and
a failure that also occurred in the last develop pipelines before the PR pipeline is
not caused by the PR. Neither is counted as a miss. Pipelines that still run, were
canceled or ran no test jobs are skipped.
"""

import argparse
import datetime
import json
import os
import time
import urllib.error
import urllib.request

API = "https://circleci.com/api/v2"
PROJECT = "gh/OpenNMS/opennms"
TEST_JOBS = ("integration-test", "smoke-test")
# Number of develop pipelines with test jobs, before a PR pipeline, whose failures excuse a PR failure.
DEVELOP_PIPELINES = 2


def get(url):
    req = urllib.request.Request(url)
    if os.environ.get("CIRCLE_TOKEN"):
        req.add_header("Circle-Token", os.environ["CIRCLE_TOKEN"])
    for attempt in range(5):
        try:
            with urllib.request.urlopen(req, timeout=60) as r:
                return json.load(r)
        except urllib.error.HTTPError as e:
            if e.code == 404:
                return None
            if e.code != 429 or attempt == 4:
                raise
            time.sleep(int(e.headers.get("Retry-After") or 10))


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


def workflows(pipeline_id):
    """Return the workflows of a pipeline, oldest first, each with its jobs."""
    items = (get(f"{API}/pipeline/{pipeline_id}/workflow") or {"items": []})["items"]
    for w in items:
        w["jobs"] = [j for j in (get(f"{API}/workflow/{w['id']}/job") or {"items": []})["items"] if j.get("job_number")]
    return sorted(items, key=lambda w: w["created_at"])


def ran_tests(wfs):
    return any(j["name"].startswith(TEST_JOBS) and j["status"] in ("success", "failed") for w in wfs for j in w["jobs"])


def failures(wfs):
    """Return the failed IT classes, split into real and flaky.

    A rerun of a workflow lists all jobs again, so only the last run of each workflow
    gives the final result. A class that failed only in a job that passed, or only in an
    earlier run, is flaky.
    """
    final_failed = {j["job_number"] for w in {w["name"]: w for w in wfs}.values() for j in w["jobs"]
                    if j["status"] == "failed"}
    seen, failed, all_failed = set(), set(), set()
    for w in wfs:
        for j in w["jobs"]:
            if not j["name"].startswith(TEST_JOBS) or j["status"] not in ("success", "failed"):
                continue
            if j["job_number"] not in seen:
                seen.add(j["job_number"])
                classes = {c for c in failed_classes(j["job_number"]) if c.endswith(("IT", "ITCase"))}
                all_failed |= classes
                if j["job_number"] in final_failed:
                    failed |= classes
    return failed, all_failed - failed


def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--days", type=int, default=7)
    a = ap.parse_args()
    cutoff = datetime.datetime.now(datetime.timezone.utc) - datetime.timedelta(days=a.days)

    records, develop_runs, skipped, no_test_jobs, token = [], [], 0, 0, None
    while True:
        page = get(f"{API}/project/{PROJECT}/pipeline" + (f"?page-token={token}" if token else "")) or {"items": []}
        for p in page["items"]:
            created = datetime.datetime.fromisoformat(p["created_at"].replace("Z", "+00:00"))
            if created < cutoff:
                break
            wfs = workflows(p["id"])
            if any(w["status"] in ("running", "failing", "on_hold", "canceled") for w in {w["name"]: w for w in wfs}.values()):
                skipped += 1
                continue
            jobs = [j for w in wfs for j in w["jobs"]]
            branch = p["vcs"].get("branch")
            if branch == "develop":
                if ran_tests(wfs):
                    develop_runs.append((created, failures(wfs)[0]))
                continue
            sel_job = next((j for j in jobs if j["name"] == "select-tests"), None)
            sel = sel_job and selection(sel_job["job_number"])
            if sel and sel["mode"] == "selected" and not ran_tests(wfs):
                no_test_jobs += 1
            elif sel:
                records.append((p["number"], created, branch, sel,
                                *(failures(wfs) if sel["mode"] == "selected" else (set(), set()))))
        else:
            token = page.get("next_page_token")
            if token:
                continue
        break

    totals = {"pipelines": len(records), "skipped_unfinished": skipped, "no_test_jobs": no_test_jobs,
              "selected": 0, "full_run": 0, "selected_its": 0, "total_its": 0,
              "selected_smoke": 0, "total_smoke": 0, "failed": 0, "caught": 0, "missed": 0,
              "failing_on_develop": 0, "flaky": 0}
    for number, created, branch, sel, failed, flaky in records:
        if sel["mode"] != "selected":
            totals[sel["mode"]] += 1
            print(f"{number} {branch}: {sel['mode']} ({sel.get('reason')})")
            continue
        totals["selected"] += 1
        picked = set(sel["its"]) | set(sel["smoke"])
        # Only develop failures from just before the PR pipeline count; later ones can come from the PR after its merge.
        before = sorted((r for r in develop_runs if r[0] < created), key=lambda r: r[0])[-DEVELOP_PIPELINES:]
        develop_failed = set().union(*(c for t, c in before))
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
