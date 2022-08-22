# /usr/bin/env python3

"""
This script helps with deciding on what we should build, by looking at the
incoming changes and the build-triggers override file (if available)
"""

import os
import re
import json
from library import libgit

path_to_build_components = os.path.join("/tmp", "build-triggers.json")
path_to_build_trigger_override = os.path.join(
    ".circleci", "build-triggers.override.json"
)

output_path = os.environ.get("OUTPUT_PATH")
head = os.environ.get("CIRCLE_SHA1")
base_revision = os.environ.get("BASE_REVISION")
branch_name = os.environ.get("CIRCLE_BRANCH")

libgit = libgit.libgit("stdout")


libgit.switch_branch(base_revision)
libgit.switch_branch(head)

base = libgit.common_ancestor(base_revision, head)

print("branch_name", branch_name)
print("output_path", output_path)
print("head", head)
print("base_revision", base_revision)
print("base", base)

if head == base:
    try:
        # If building on the same branch as BASE_REVISION, we will get the
        # current commit as merge base. In that case try to go back to the
        # first parent, i.e. the last state of this branch before the
        # merge, and use that as the base.
        base = libgit.get_commit_sha("HEAD~1")
    except:
        # This can fail if this is the first commit of the repo, so that
        # HEAD~1 actually doesn't resolve. In this case we can compare
        # against this magic SHA below, which is the empty tree. The diff
        # to that is just the first commit as patch.
        base = "4b825dc642cb6eb9a060e54bf8d69288fbee4904"

print("base", base)

changes = libgit.get_changed_files_in_commits(base, head)

mappings = [m.split() for m in os.environ.get("MAPPING").splitlines()]


def check_mapping(mapping):
    """
    Checks the validity of the mapping
    """
    if 3 != len(mapping):
        raise Exception("Invalid mapping size. Current mapping:", mapping)
    path, param, value = mapping
    regex = re.compile(r"^" + path + r"$")
    for change in changes:
        if regex.match(change):
            return True
    return False


def convert_mapping(mapping_entry) -> list:
    """
    Converts mapping to a list
    """
    return [mapping_entry[1], json.loads(mapping_entry[2])]


mappings = filter(check_mapping, mappings)
mappings = map(convert_mapping, mappings)
mappings = dict(mappings)

print("Mappings:", mappings)


# If *IT.java files have changed -> enable integration builds
# If *Test.java files have changed -> enable smoke builds
# if Dockerfiles (under opennms-container) have changed enable docker builds
What_to_build = []


def add_to_build_list(item):
    """
    Adds the item to What_to_build list
    """
    if item not in What_to_build:
        What_to_build.append(item)


for change in changes:
    if not change:
        continue
    if "src/test/" in change and "smoke-test/" not in change:
        add_to_build_list("Integration_tests")
    elif "src/test/" in change and "smoke-test/" in change:
        add_to_build_list("Smoke_tests")
    elif "opennms-container" in change:
        if "horizon" in change:
            add_to_build_list("oci_horizon_image")
        elif "minion" in change:
            add_to_build_list("oci_minion_image")
        elif "sentinel" in change:
            add_to_build_list("oci_sentinel_image")
    elif ".circleci" in change:
        add_to_build_list("circleci_configuration")
    elif "doc" in change:
        add_to_build_list("doc")
    elif "ui" in change:
        add_to_build_list("ui")
    else:
        add_to_build_list("build")

print("What we want to build:", What_to_build, len(What_to_build))
git_keywords = libgit.extract_keywords_from_last_commit()

if os.path.exists(path_to_build_trigger_override):
    with open(path_to_build_trigger_override, "r", encoding="UTF-8") as file_handler:
        build_mappings = json.load(file_handler)
else:
    build_mappings = {
        "build": {
            "docs": False,
            "ui": False,
            "coverage": False,
            "build": False,
            "deploy": False,
        },
        "publish": {"packages": False},
        "tests": {"smoke": False, "smoke-flaky": False, "integration": False},
        "oci-images": {"minion": False, "horizon": False, "sentinel": False},
        "debian-packages": {"minion": False, "horizon": False, "sentinel": False},
        "rpm-packages": {"minion": False, "horizon": False, "sentinel": False},
        "experimental": False,
        "filters": {"enabled": True},
    }

if "trigger-docs" in mappings:
    build_mappings["build"]["docs"] = mappings["trigger-docs"]

if "trigger-ui" in mappings:
    build_mappings["build"]["ui"] = mappings["trigger-ui"]

if "trigger-coverage" in mappings:
    build_mappings["build"]["coverage"] = mappings["trigger-coverage"]

if "trigger-flaky-smoke" in mappings:
    build_mappings["tests"]["smoke-flaky"] = mappings["trigger-flaky-smoke"]

if re.match(".*smoke.*", branch_name):
    print("Detected smoke in the branch name")
    build_mappings["tests"]["smoke"] = True

if re.match(".*flaky.*", branch_name):
    print("Detected smoke in the branch name")
    build_mappings["tests"]["smoke-flaky"] = True


print("Git Keywords:", git_keywords)
if (
    "circleci_configuration" in What_to_build
    and len(What_to_build) == 1
    and not build_mappings["build"]["build"]
):
    # if circleci_configuration is the only entry in the list we don't want to trigger a buildss.
    mappings["trigger-build"] = False
    build_mappings["build"]["build"] = False

if "smoke" in git_keywords or "Smoke_tests" in What_to_build:
    if "flaky" in str(git_keywords):
        build_mappings["tests"]["smoke-flaky"] = True
    else:
        build_mappings["tests"]["smoke"] = True
    build_mappings["build"]["build"] = True
    # We disable filters for smoke jobs in generate_main.py
    # build_mappings["filters"]["enabled"] = False

if (
    "oci" in git_keywords
    or "oci_horizon_image" in What_to_build
    or "oci_horizon_image" in What_to_build
    or "oci_sentinel_image" in What_to_build
):
    build_mappings["build"]["build"] = True
    build_mappings["oci-images"]["minion"] = True
    build_mappings["oci-images"]["horizon"] = True
    build_mappings["oci-images"]["sentinel"] = True

if "rpms" in git_keywords:
    build_mappings["build"]["build"] = True
    build_mappings["rpm-packages"]["minion"] = True
    build_mappings["rpm-packages"]["horizon"] = True
    build_mappings["rpm-packages"]["sentinel"] = True

if "debs" in git_keywords:
    build_mappings["build"]["build"] = True
    build_mappings["debian-packages"]["minion"] = True
    build_mappings["debian-packages"]["horizon"] = True
    build_mappings["debian-packages"]["sentinel"] = True

if "integration" in git_keywords or "Integration_tests" in What_to_build:
    build_mappings["build"]["build"] = True
    build_mappings["tests"]["integration"] = True

if "build" in What_to_build:
    build_mappings["build"]["build"] = True

if "doc" in git_keywords or "docs" in git_keywords or "doc" in What_to_build:
    build_mappings["build"]["docs"] = True

if "ui" in git_keywords or "ui" in What_to_build:
    build_mappings["build"]["ui"] = True

if "experimentalPath" in git_keywords:
    build_mappings["experimental"] = True
    build_mappings["build"]["build"] = False

with open(output_path, "w", encoding="UTF-8") as file_handler:
    file_handler.write(json.dumps(mappings))

with open(path_to_build_components, "w", encoding="UTF-8") as file_handler:
    file_handler.write(json.dumps(build_mappings, indent=4))
