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


libgit = libgit.libgit("/tmp/performance.txt")


libgit.switch_branch(base_revision)
libgit.switch_branch(head)

base = libgit.common_ancestor(base_revision, head)

print("Branch Name:", branch_name)
print("Output Path:", output_path)
print("Branch HEAD:", head)
print("Base Revision:", base_revision)

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

print("Base:", base)
print()

changed_files = libgit.get_changed_files_in_commits(base, head)

mappings = [m.split() for m in os.environ.get("MAPPING").splitlines()]


def check_mapping(mapping):
    """
    Checks the validity of the mapping
    """
    if 3 != len(mapping):
        raise Exception("Invalid mapping size. Current mapping:", mapping)
    path, param, value = mapping
    regex = re.compile(r"^" + path + r"$")
    for change in changed_files:
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

print("Mappings:")
for item in mappings:
    print(" ", "*", item, "[", mappings[item], "]")
print()

What_to_build = []


def add_to_build_list(item):
    """
    Adds the item to What_to_build list
    """
    if item not in What_to_build:
        What_to_build.append(item)


# Step 1, Detect all changes and Git keywords (if any)
for change in changed_files:
    if not change:
        continue
    if "src/test/" in change and "smoke-test/" not in change:
        add_to_build_list("Integration_tests")
    elif "src/test/" in change and "smoke-test/" in change:
        add_to_build_list("smoke_tests")
    elif "trivy-config/trivyignore" in change:
        add_to_build_list("trivy-scan")
    elif "opennms-container" in change:
        add_to_build_list("oci")
    elif ".circleci" in change and ".circleci/epoch" not in change:
        add_to_build_list("circleci_configuration")
    elif "docs/" in change:
        add_to_build_list("docs")
    elif "ui" in change:
        add_to_build_list("ui")
    else:
        if "merge-foundation/" not in branch_name:
            add_to_build_list("build")

if changed_files:
    print("Changed file(s):")
    for item in changed_files:
        if item:
            print(" ", "*", item)
    print()

if What_to_build:
    print("What we want to build:")
    for item in What_to_build:
        print(" ", "*", item)
    print()

git_keywords = libgit.extract_keywords_from_last_commit()

with open(path_to_workflow, "r", encoding="UTF-8") as file_handler:
    workflow_data = json.load(file_handler)

workflow_keywords = workflow_data["bundles"].keys()

print("Supported Workflow Keywords:")
for item in workflow_keywords:
    print(" ", "*", item)
print()


# Step 2: Take action on them

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
        "docs": False,
        "ui": False,
        "integration": False,
        "smoke": False,
        "rpms": False,
        "debs": False,
        "oci": False,
        "build-publish": False,
        "trivy-scan": False,
        "experimental": False,
    }

print("Build Trigger Override Found:", str(build_trigger_override_found))
print()

# Epoch file will force a build to run
if ".circleci/epoch" in changed_files:
    print("`epoch` file detected")
    mappings["trigger-build"] = True
    print()


if build_mappings["experimental"] or "experimentalPath" in git_keywords:
    print("Experimental path detected, will disable other paths")
    print()
    # If experimental path is enabled, disable other paths
    for item in build_mappings:
        build_mappings[item] = False

    # Clear the mappings
    mappings.clear()

    build_mappings["experimental"] = True

if "trigger-build" in mappings:
    if (
        "develop" in branch_name
        or "master" in branch_name
        or "release-" in branch_name
        or "foundation-" in branch_name
    ) and "merge-foundation/" not in branch_name:
        print("Executing workflow: build-publish")
        build_mappings["build-publish"] = mappings["trigger-build"]
        print()
    elif "merge-foundation/" in branch_name and not build_trigger_override_found:
        print("Execute workflow: merge-foundation")
        print()
        # If experimental path is enabled, disable other paths
        for item in build_mappings:
            build_mappings[item] = False

        # Clear the mappings
        mappings.clear()
        What_to_build.clear()
        build_mappings["merge-foundation"] = True
    elif branch_name in "master" and not build_trigger_override_found:
        print("Execute workflow: master-branch")
        print()
        for item in build_mappings:
            build_mappings[item] = False

        # Clear the mappings
        mappings.clear()
        What_to_build.clear()
        build_mappings["master-branch"] = True
    elif not build_trigger_override_found and "merge-foundation/" not in branch_name:
        print("Executing workflow: build-deploy")
        print()
        build_mappings["build-deploy"] = mappings["trigger-build"]

if "trigger-docs" in mappings:
    build_mappings["docs"] = mappings["trigger-docs"]

if "trigger-ui" in mappings:
    build_mappings["ui"] = mappings["trigger-ui"]

if "trigger-coverage" in mappings:
    build_mappings["coverage"] = mappings["trigger-coverage"]
    mappings.clear()
    What_to_build.clear()
    git_keywords.clear()

if re.match(".*smoke.*", branch_name) and (
    not build_mappings["experimental"] or "experimentalPath" not in git_keywords
):
    print("Detected smoke in the branch name")
    build_mappings["smoke"] = True
    print()

if git_keywords:
    print("Detected GIT keywords:")
    for item in git_keywords:
        print(" ", "*", item)
    print()

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
        if "docs" in keyword or "docs" in What_to_build:
            build_mappings["docs"] = True
        if "ui" in keyword or "ui" in What_to_build:
            build_mappings["ui"] = True
        if "build-deploy" in keyword:
            build_mappings["build-deploy"] = True
        if "smoke" in keyword or "smoke_tests" in What_to_build:
            build_mappings["smoke"] = True
        if "rpms" in keyword:
            build_mappings["rpms"] = True
        if "debs" in keyword:
            build_mappings["debs"] = True
        if "oci" in keyword or "oci" in What_to_build:
            build_mappings["oci"] = True
        if "build-publish" in keyword:
            build_mappings["build-publish"] = True
        if "trivy-scan" in keyword:
            build_mappings["trivy-scan"] = True



if "smoke" in git_keywords or "smoke_tests" in What_to_build:
    build_mappings["smoke"] = True
if "oci" in git_keywords:
    build_mappings["oci"] = True

if "rpms" in git_keywords:
    build_mappings["rpms"] = True

if "trivy-scan" in git_keywords:
    build_mappings["trivy-scan"] = True
    
if "debs" in git_keywords:
    build_mappings["debs"] = True

if "integration" in git_keywords or "Integration_tests" in What_to_build:
    build_mappings["integration"] = True

if "build" in What_to_build and not build_mappings["experimental"]:
    build_mappings["build-deploy"] = True

if (
    "doc" in git_keywords
    or "docs" in git_keywords
    or "doc" in What_to_build
    or "docs" in What_to_build
):
    build_mappings["docs"] = True

if "ui" in git_keywords or "ui" in What_to_build:
    build_mappings["ui"] = True

with open(output_path, "w", encoding="UTF-8") as file_handler:
    file_handler.write(json.dumps(mappings))

with open(path_to_build_components, "w", encoding="UTF-8") as file_handler:
    file_handler.write(json.dumps(build_mappings, indent=4))
