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
path_to_workflow = os.path.join(".circleci", "main", "workflows", "workflows_v2.json")

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
        add_to_build_list("smoke_tests")
    elif "opennms-container" in change:
        add_to_build_list("oci")
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

with open(path_to_workflow, "r", encoding="UTF-8") as file_handler:
    workflow_data = json.load(file_handler)

workflow_keywords = workflow_data["bundles"].keys()
print("Workflow Keywords:", workflow_keywords)

# Check to see if build-trigger.overrride file exists and we are not
# on the main branches
if os.path.exists(path_to_build_trigger_override) and (
    "develop" not in branch_name
    and "master" not in branch_name
    and "release-" not in branch_name
    and "foundation-" not in branch_name
    and "merge-foundation/" not in branch_name
):
    build_trigger_override_found = True
else:
    build_trigger_override_found = False


if build_trigger_override_found:
    with open(path_to_build_trigger_override, "r", encoding="UTF-8") as file_handler:
        build_mappings = json.load(file_handler)
else:
    build_mappings = {
        "build-deploy": False,
        "coverage": False,
        "doc": False,
        "ui": False,
        "integration": False,
        "smoke": False,
        "smoke-flaky": False,
        "rpms": False,
        "debs": False,
        "oci": False,
        "build-publish": False,
        "experimental": False,
    }

print("Build Trigger Override Found:", str(build_trigger_override_found))

if "trigger-build" in mappings:
    if (
        "develop" in branch_name
        or "master" in branch_name
        or "release-" in branch_name
        or "foundation-" in branch_name
        and "merge-foundation/" not in branch_name
    ):
        print("Executing workflow: build-publish")
        build_mappings["build-publish"] = mappings["trigger-build"]
    else:
        if "merge-foundation/" in branch_name:
            print("Execute workflow: merge-foundation")
            build_mappings["merge-foundation"] = True
            build_mappings["build-publish"] = False
            build_mappings["build-deploy"] = False
        elif not build_trigger_override_found:
            print("Executing workflow: build-deploy")
            build_mappings["build-deploy"] = mappings["trigger-build"]

if "trigger-docs" in mappings:
    build_mappings["docs"] = mappings["trigger-docs"]

if "trigger-ui" in mappings:
    build_mappings["ui"] = mappings["trigger-ui"]

if "trigger-coverage" in mappings:
    build_mappings["coverage"] = mappings["trigger-coverage"]

if "trigger-flaky-smoke" in mappings:
    if not build_mappings["smoke-flaky"]:
        build_mappings["smoke-flaky"] = mappings["trigger-flaky-smoke"]

if re.match(".*smoke.*", branch_name):
    print("Detected smoke in the branch name")
    build_mappings["smoke"] = True

if re.match(".*flaky.*", branch_name):
    print("Detected smoke in the branch name")
    build_mappings["smoke-flaky"] = True


print("Git Keywords:", git_keywords)
if (
    "circleci_configuration" in What_to_build
    and len(What_to_build) == 1
    and not build_mappings["build-deploy"]
    and not build_mappings["build-publish"]
):
    # if circleci_configuration is the only entry in the list we don't want to trigger a buildss.
    mappings["trigger-build"] = False
    build_mappings["build-deploy"] = False
    build_mappings["build-publish"] = False

for keyword in git_keywords:
    if keyword in workflow_keywords:
        if "doc" in keyword or "doc" in What_to_build:
            build_mappings["docs"] = True
        if "ui" in keyword or "ui" in What_to_build:
            build_mappings["ui"] = True
        if "build-deploy" in keyword:
            build_mappings["build-deploy"] = True
        if "smoke" in keyword or "smoke_tests" in What_to_build:
            if "flaky" in str(git_keywords):
                build_mappings["smoke-flaky"] = True
            else:
                build_mappings["smoke"] = True
        if "rpms" in keyword:
            build_mappings["rpms"] = True
        if "debs" in keyword:
            build_mappings["debs"] = True
        if "oci" in keyword or "oci" in What_to_build:
            build_mappings["oci"] = True
        if "build-publish" in keyword:
            build_mappings["build-publish"] = True


if "smoke" in git_keywords or "smoke_tests" in What_to_build:
    if "flaky" in str(git_keywords):
        build_mappings["smoke-flaky"] = True
    else:
        build_mappings["smoke"] = True
if "oci" in git_keywords:
    build_mappings["oci"] = True

if "rpms" in git_keywords:
    build_mappings["rpms"] = True

if "debs" in git_keywords:
    build_mappings["debs"] = True

if "integration" in git_keywords or "Integration_tests" in What_to_build:
    build_mappings["integration"] = True

if "build" in What_to_build:
    build_mappings["build-deploy"] = True

if "doc" in git_keywords or "docs" in git_keywords or "doc" in What_to_build:
    build_mappings["doc"] = True

if "ui" in git_keywords or "ui" in What_to_build:
    build_mappings["ui"] = True

if "experimentalPath" in git_keywords:
    build_mappings["experimental"] = True

with open(output_path, "w", encoding="UTF-8") as file_handler:
    file_handler.write(json.dumps(mappings))

with open(path_to_build_components, "w", encoding="UTF-8") as file_handler:
    file_handler.write(json.dumps(build_mappings, indent=4))
