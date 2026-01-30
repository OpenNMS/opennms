#!/usr/bin/env python3

"""
This script helps with deciding on what we should build, by looking at the
incoming changes and the build-triggers override file (if available).
"""

import os
import re
import json
import subprocess
from typing import Optional
from library import libgit as libgit_module

# Constants for branch names
BRANCH_DEVELOP = "develop"
BRANCH_MASTER = "master"
BRANCH_RELEASE_PREFIX = "release-"
BRANCH_FOUNDATION_PREFIX = "foundation-"
BRANCH_MERGE_FOUNDATION_PREFIX = "merge-foundation/"

# Path constants
PATH_BUILD_COMPONENTS = os.path.join("/tmp", "build-triggers.json")
PATH_BUILD_TRIGGER_OVERRIDE = os.path.join(
    ".circleci", "build-triggers.override.json"
)
PATH_WORKFLOW = os.path.join(".circleci", "main", "workflows", "workflows_v2.json")

# Empty tree SHA for git comparisons
EMPTY_TREE_SHA = "4b825dc642cb6eb9a060e54bf8d69288fbee4904"


def get_required_env(name: str) -> str:
    """Get a required environment variable or raise an error."""
    value = os.environ.get(name)
    if value is None:
        raise EnvironmentError(f"Required environment variable '{name}' is not set")
    return value


def main() -> None:
    """Main entry point for the build trigger processing."""
    output_path = get_required_env("OUTPUT_PATH")
    head = get_required_env("CIRCLE_SHA1")
    base_revision = get_required_env("BASE_REVISION")
    branch_name = get_required_env("CIRCLE_BRANCH")
    mapping_str = get_required_env("MAPPING")

    git = libgit_module.libgit("/tmp/performance.txt")

    git.switch_branch(base_revision)
    git.switch_branch(head)

    base = git.common_ancestor(base_revision, head)

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
            base = git.get_commit_sha("HEAD~1")
        except subprocess.CalledProcessError:
            # This can fail if this is the first commit of the repo, so that
            # HEAD~1 actually doesn't resolve. In this case we can compare
            # against this magic SHA below, which is the empty tree. The diff
            # to that is just the first commit as patch.
            base = EMPTY_TREE_SHA

    print("Base:", base)
    print()

    changed_files = git.get_changed_files_in_commits(base, head)

    mappings = [m.split() for m in mapping_str.splitlines()]


    def check_mapping(mapping: list[str]) -> bool:
        """
        Checks the validity of the mapping
        """
        if 3 != len(mapping):
            raise ValueError(f"Invalid mapping size. Current mapping: {mapping}")
        path, param, value = mapping
        regex = re.compile(r"^" + path + r"$")
        for change in changed_files:
            if regex.match(change):
                return True
        return False

    def convert_mapping(mapping_entry: list[str]) -> list:
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

    what_to_build: list[str] = []


    def add_to_build_list(item: str) -> None:
        """
        Adds the item to what_to_build list
        """
        if item not in what_to_build:
            what_to_build.append(item)


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
            if BRANCH_MERGE_FOUNDATION_PREFIX not in branch_name:
                add_to_build_list("build")

    if changed_files:
        print("Changed file(s):")
        for item in changed_files:
            if item:
                print(" ", "*", item)
        print()

    combine_build_element = ""

    if what_to_build:
        print("What we want to build:")
        for item in what_to_build:
            combine_build_element += item + ','
            print(" ", "*", item)
        print()

    git_keywords = git.extract_keywords_from_last_commit()

    with open(PATH_WORKFLOW, "r", encoding="UTF-8") as file_handler:
        workflow_data = json.load(file_handler)

    workflow_keywords = workflow_data["bundles"].keys()

    print("Supported Workflow Keywords:")
    for item in workflow_keywords:
        print(" ", "*", item)
    print()


    # Step 2: Take action on them

    def is_main_branch(name: str) -> bool:
        """Check if the branch is a main/protected branch."""
        return (
            BRANCH_DEVELOP in name
            or BRANCH_MASTER in name
            or BRANCH_RELEASE_PREFIX in name
            or BRANCH_FOUNDATION_PREFIX in name
            or BRANCH_MERGE_FOUNDATION_PREFIX in name
        )

    # Check to see if build-trigger.override file exists and we are not
    # on the main branches
    build_trigger_override_found = (
        os.path.exists(PATH_BUILD_TRIGGER_OVERRIDE) and not is_main_branch(branch_name)
    )

    if build_trigger_override_found:
        with open(PATH_BUILD_TRIGGER_OVERRIDE, "r", encoding="UTF-8") as file_handler:
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
            "oci-arm64": False,
            "oci-all": False,
            "build-publish": False,
            "release-build": False,
            "trivy-scan": False,
            "trivy-analyze": False,
            "experimental": False,
        }

    print("Build Trigger Override Found:", str(build_trigger_override_found))
    print()

    # Epoch file will force a build to run
    if ".circleci/epoch" in changed_files:
        if "trigger-coverage" not in mappings:
            print("`epoch` file detected")
            mappings["trigger-build"] = True
        else:
            print("`epoch` file detected, but will be ignored as we are running coverage build")
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

    def should_proceed(item: str, build_list: list[str], build_element: str) -> bool:
        """Check if the build should proceed based on what's being built."""
        non_build_items = {"docs", "ui", "circleci_configuration"}

        # Check if the item is one of the specified values and if build_list has one item
        is_single_item = item in non_build_items and len(build_list) == 1

        # Check if any two of the specified values are present in build_element
        is_two_items = (
            (("docs" in build_element and "ui" in build_element) or
             ("docs" in build_element and "circleci_configuration" in build_element) or
             ("circleci_configuration" in build_element and "ui" in build_element)) and
            len(build_list) == 2
        )

        # Check if all three specified values are present and if build_list has three items
        is_three_items = (
            "docs" in build_element and
            "ui" in build_element and
            "circleci_configuration" in build_element and
            len(build_list) == 3
        )

        # Return True if any of the conditions are met
        return is_single_item or is_two_items or is_three_items

    def clear_build_state() -> None:
        """Clear the build state when skipping builds."""
        if "trigger-build" in mappings:
            del mappings["trigger-build"]
        what_to_build.clear()

    def reset_all_mappings(workflow_name: str) -> None:
        """Reset all build mappings and set a specific workflow."""
        print(f"Execute workflow: {workflow_name}")
        print()
        for item in build_mappings:
            build_mappings[item] = False
        mappings.clear()
        what_to_build.clear()
        build_mappings[workflow_name] = True

    if "trigger-build" in mappings:
        if BRANCH_MASTER in branch_name and BRANCH_MERGE_FOUNDATION_PREFIX not in branch_name:
            for item in what_to_build:
                if should_proceed(item, what_to_build, combine_build_element):
                    clear_build_state()
                    break
                else:
                    print("Executing workflow: release-build")
                    build_mappings["release-build"] = mappings["trigger-build"]
                    print()
                    break
        elif (
            BRANCH_DEVELOP in branch_name
            or BRANCH_RELEASE_PREFIX in branch_name
            or BRANCH_FOUNDATION_PREFIX in branch_name
        ) and BRANCH_MERGE_FOUNDATION_PREFIX not in branch_name:
            for item in what_to_build:
                if should_proceed(item, what_to_build, combine_build_element):
                    clear_build_state()
                    break
                else:
                    print("Executing workflow: build-publish")
                    build_mappings["build-publish"] = mappings["trigger-build"]
                    print()
                    break
        elif BRANCH_MERGE_FOUNDATION_PREFIX in branch_name and not build_trigger_override_found:
            reset_all_mappings("merge-foundation")
        elif branch_name in BRANCH_MASTER and not build_trigger_override_found:
            reset_all_mappings("master-branch")
        elif not build_trigger_override_found and BRANCH_MERGE_FOUNDATION_PREFIX not in branch_name:
            for item in what_to_build:
                if should_proceed(item, what_to_build, combine_build_element):
                    clear_build_state()
                    break
                else:
                    print("Executing workflow: build-deploy")
                    print()
                    build_mappings["build-deploy"] = mappings["trigger-build"]
                    break

    if "trigger-docs" in mappings:
        build_mappings["docs"] = mappings["trigger-docs"]

    if "trigger-ui" in mappings:
        build_mappings["ui"] = mappings["trigger-ui"]

    if "trigger-coverage" in mappings:
        build_mappings["coverage"] = mappings["trigger-coverage"]
        mappings.clear()
        what_to_build.clear()
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
        "circleci_configuration" in what_to_build
        and len(what_to_build) == 1
        and not build_mappings["build-deploy"]
        and not build_mappings["build-publish"]
    ):
        # if circleci_configuration is the only entry in the list we don't want to trigger builds.
        mappings["trigger-build"] = False
        build_mappings["build-deploy"] = False
        build_mappings["build-publish"] = False

    for keyword in git_keywords:
        if keyword in workflow_keywords:
            if "docs" in keyword or "docs" in what_to_build:
                build_mappings["docs"] = True
            if "ui" in keyword or "ui" in what_to_build:
                build_mappings["ui"] = True
            if "build-deploy" in keyword:
                build_mappings["build-deploy"] = True
            if "smoke" in keyword or "smoke_tests" in what_to_build:
                build_mappings["smoke"] = True
            if "rpms" in keyword:
                build_mappings["rpms"] = True
            if "debs" in keyword:
                build_mappings["debs"] = True
            if "oci" in keyword or "oci" in what_to_build:
                build_mappings["oci"] = True
            if "oci-arm64" in keyword or "oci-arm64" in what_to_build:
                build_mappings["oci-arm64"] = True
            if "oci-all" in keyword or "oci-all" in what_to_build:
                build_mappings["oci-all"] = True
            if "build-publish" in keyword:
                build_mappings["build-publish"] = True
            if "trivy-scan" in keyword:
                build_mappings["trivy-scan"] = True
            if "trivy-analyze" in keyword:
                build_mappings["trivy-analyze"] = True



    # Apply git keywords to build mappings
    keyword_mapping = {
        "smoke": "smoke",
        "oci": "oci",
        "oci-arm64": "oci-arm64",
        "oci-all": "oci-all",
        "rpms": "rpms",
        "trivy-scan": "trivy-scan",
        "trivy-analyze": "trivy-analyze",
        "debs": "debs",
    }

    for keyword, mapping_key in keyword_mapping.items():
        if keyword in git_keywords:
            build_mappings[mapping_key] = True

    if "smoke_tests" in what_to_build:
        build_mappings["smoke"] = True

    if "integration" in git_keywords or "Integration_tests" in what_to_build:
        build_mappings["integration"] = True

    if "build" in what_to_build and not build_mappings["experimental"]:
        build_mappings["build-deploy"] = True

    if (
        "doc" in git_keywords
        or "docs" in git_keywords
        or "doc" in what_to_build
        or "docs" in what_to_build
    ):
        build_mappings["docs"] = True

    if "ui" in git_keywords or "ui" in what_to_build:
        build_mappings["ui"] = True

    with open(output_path, "w", encoding="UTF-8") as file_handler:
        file_handler.write(json.dumps(mappings))

    with open(PATH_BUILD_COMPONENTS, "w", encoding="UTF-8") as file_handler:
        file_handler.write(json.dumps(build_mappings, indent=4))


if __name__ == "__main__":
    main()
