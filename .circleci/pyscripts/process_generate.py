#!/usr/bin/env python3

"""
This script determines what components to build by analyzing incoming changes
and the build-triggers override file (if available).
"""

import os
import re
import json
import sys
from typing import List, Dict, Set, Any
from pathlib import Path
from library import libgit

# Constants
EMPTY_TREE_SHA = "4b825dc642cb6eb9a060e54bf8d69288fbee4904"
MAIN_BRANCH_PATTERNS = ["develop", "master"]
RELEASE_BRANCH_PREFIXES = ["release-", "foundation-"]
MERGE_FOUNDATION_PREFIX = "merge-foundation/"
NON_CODE_CHANGES = {"docs", "ui", "circleci_configuration"}

# File paths
PATH_BUILD_COMPONENTS = Path("/tmp/build-triggers.json")
PATH_BUILD_TRIGGER_OVERRIDE = Path(".circleci/build-triggers.override.json")
PATH_WORKFLOW = Path(".circleci/main/workflows/workflows_v2.json")

# Environment variables
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
    except Exception as e:
        # This can fail if this is the first commit of the repo, so that
        # HEAD~1 actually doesn't resolve. In this case we can compare
        # against the empty tree. The diff to that is just the first commit as patch.
        print(f"Warning: Could not get HEAD~1, using empty tree: {e}")
        base = EMPTY_TREE_SHA

print("Base:", base)
print()

changed_files = libgit.get_changed_files_in_commits(base, head)

mappings = [m.split() for m in os.environ.get("MAPPING", "").splitlines()]


def check_mapping(mapping: List[str]) -> bool:
    """Check if mapping is valid and matches any changed file."""
    if len(mapping) != 3:
        raise ValueError(f"Invalid mapping size. Expected 3 parts, got {len(mapping)}: {mapping}")
    
    path, param, value = mapping
    regex = re.compile(r"^" + path + r"$")
    return any(regex.match(change) for change in changed_files)


def convert_mapping(mapping_entry: List[str]) -> List[Any]:
    """Convert mapping entry to [param, parsed_value] format."""
    return [mapping_entry[1], json.loads(mapping_entry[2])]


mappings = filter(check_mapping, mappings)
mappings = map(convert_mapping, mappings)
mappings = dict(mappings)

print("Mappings:")
for item in mappings:
    print(" ", "*", item, "[", mappings[item], "]")
print()

what_to_build: Set[str] = set()


def add_to_build_list(item: str) -> None:
    """Add component to the build list."""
    what_to_build.add(item)


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
    elif "trivy-config/trivyignore" in change:
        add_to_build_list("trivy-analyze")
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

if what_to_build:
    print("What we want to build:")
    for item in what_to_build:
        print(" ", "*", item)
    print()

git_keywords = libgit.extract_keywords_from_last_commit()

try:
    with open(PATH_WORKFLOW, "r", encoding="UTF-8") as file_handler:
        workflow_data = json.load(file_handler)
    workflow_keywords = workflow_data["bundles"].keys()
except (FileNotFoundError, json.JSONDecodeError) as e:
    print(f"Error loading workflow configuration: {e}")
    sys.exit(1)

print("Supported Workflow Keywords:")
for item in workflow_keywords:
    print(" ", "*", item)
print()


def is_main_branch(branch: str) -> bool:
    """Check if the branch is a main/protected branch."""
    if not branch:
        return False
    return (branch in MAIN_BRANCH_PATTERNS or 
            any(branch.startswith(prefix) for prefix in RELEASE_BRANCH_PREFIXES))


def is_merge_foundation_branch(branch: str) -> bool:
    """Check if the branch is a merge-foundation branch."""
    return branch and branch.startswith(MERGE_FOUNDATION_PREFIX)


def load_build_mappings() -> Dict[str, bool]:
    """Load build mappings from override file or return defaults."""
    # Check if override file exists and we're not on a main branch
    build_trigger_override_found = (
        PATH_BUILD_TRIGGER_OVERRIDE.exists() and 
        not is_main_branch(branch_name) and 
        not is_merge_foundation_branch(branch_name)
    )
    
    if build_trigger_override_found:
        try:
            with open(PATH_BUILD_TRIGGER_OVERRIDE, "r", encoding="UTF-8") as file_handler:
                return json.load(file_handler)
        except (FileNotFoundError, json.JSONDecodeError) as e:
            print(f"Warning: Could not load build trigger override: {e}")
    
    # Default mappings
    return {
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
        "trivy-analyze": False,
        "experimental": False,
    }


# Step 2: Take action on them
build_mappings = load_build_mappings()
build_trigger_override_found = PATH_BUILD_TRIGGER_OVERRIDE.exists() and not is_main_branch(branch_name)

print("Build Trigger Override Found:", str(build_trigger_override_found))
print()

# Epoch file will force a build to run
if ".circleci/epoch" in changed_files:
    print("`epoch` file detected")
    mappings["trigger-build"] = True
    print()


def is_non_code_change_only(changes: Set[str]) -> bool:
    """Check if changes are only non-code items (docs, UI, CircleCI config)."""
    return changes and changes.issubset(NON_CODE_CHANGES)


def enable_experimental_mode(mappings: Dict, build_mappings: Dict) -> None:
    """Enable experimental mode and disable all other paths."""
    print("Experimental path detected, will disable other paths")
    print()
    
    for key in build_mappings:
        build_mappings[key] = False
    
    mappings.clear()
    build_mappings["experimental"] = True


if build_mappings["experimental"] or "experimentalPath" in git_keywords:
    enable_experimental_mode(mappings, build_mappings)

if "trigger-build" in mappings:
    if is_main_branch(branch_name) and not is_merge_foundation_branch(branch_name):
        # Skip build if only non-code changes on main branches
        if is_non_code_change_only(what_to_build):
            del mappings["trigger-build"]
            what_to_build.clear()
        else:
            print("Executing workflow: build-publish")
            build_mappings["build-publish"] = mappings["trigger-build"]
            print()
    
    elif is_merge_foundation_branch(branch_name) and not build_trigger_override_found:
        print("Execute workflow: merge-foundation")
        print()
        for key in build_mappings:
            build_mappings[key] = False
        mappings.clear()
        what_to_build.clear()
        build_mappings["merge-foundation"] = True
    
    elif branch_name == "master" and not build_trigger_override_found:
        print("Execute workflow: master-branch")
        print()
        for key in build_mappings:
            build_mappings[key] = False
        mappings.clear()
        what_to_build.clear()
        build_mappings["master-branch"] = True
    
    elif not build_trigger_override_found and not is_merge_foundation_branch(branch_name):
        # Skip build if only non-code changes
        if is_non_code_change_only(what_to_build):
            del mappings["trigger-build"]
            what_to_build.clear()
        else:
            print("Executing workflow: build-deploy")
            print()
            build_mappings["build-deploy"] = mappings["trigger-build"]

# Mapping of component keywords to their variations
COMPONENT_KEYWORD_MAP = {
    'docs': ['doc', 'docs'],
    'ui': ['ui'],
    'smoke': ['smoke', 'smoke_tests'],
    'integration': ['integration', 'Integration_tests'],
    'oci': ['oci'],
    'rpms': ['rpms'],
    'debs': ['debs'],
    'trivy-scan': ['trivy-scan'],
    'trivy-analyze': ['trivy-analyze'],
}


def should_build_component(component_key: str, keywords: Set[str], changes: Set[str]) -> bool:
    """Determine if a component should be built based on keywords and changes."""
    variations = COMPONENT_KEYWORD_MAP.get(component_key, [component_key])
    return any(var in keywords or var in changes for var in variations)


# Handle trigger mappings
if "trigger-coverage" in mappings:
    build_mappings["coverage"] = mappings["trigger-coverage"]
    mappings.clear()
    what_to_build.clear()
    git_keywords.clear()

if "trigger-docs" in mappings:
    build_mappings["docs"] = mappings["trigger-docs"]

if "trigger-ui" in mappings:
    build_mappings["ui"] = mappings["trigger-ui"]

# Smoke test based on branch name
if branch_name and re.match(".*smoke.*", branch_name):
    if not build_mappings["experimental"] and "experimentalPath" not in git_keywords:
        print("Detected smoke in the branch name")
        build_mappings["smoke"] = True
        print()

if git_keywords:
    print("Detected GIT keywords:")
    for item in git_keywords:
        print(" ", "*", item)
    print()

# Don't trigger builds if only CircleCI config changed
if (
    what_to_build == {"circleci_configuration"}
    and not build_mappings["build-deploy"]
    and not build_mappings["build-publish"]
):
    mappings.pop("trigger-build", None)
    build_mappings["build-deploy"] = False
    build_mappings["build-publish"] = False

# Process workflow keywords
for keyword in git_keywords:
    if keyword in workflow_keywords:
        if keyword in ["build-deploy", "build-publish"]:
            build_mappings[keyword] = True
        elif should_build_component(keyword, git_keywords, what_to_build):
            build_mappings[keyword] = True

# Apply component keyword mappings
for component_key in COMPONENT_KEYWORD_MAP:
    if should_build_component(component_key, git_keywords, what_to_build):
        build_mappings[component_key] = True

# Handle general build trigger
if "build" in what_to_build and not build_mappings["experimental"]:
    build_mappings["build-deploy"] = True

# Write output files
try:
    with open(output_path, "w", encoding="UTF-8") as file_handler:
        file_handler.write(json.dumps(mappings))
    
    with open(PATH_BUILD_COMPONENTS, "w", encoding="UTF-8") as file_handler:
        file_handler.write(json.dumps(build_mappings, indent=4))
except IOError as e:
    print(f"Error writing output files: {e}")
    sys.exit(1)
