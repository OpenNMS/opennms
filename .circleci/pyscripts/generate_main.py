#!/usr/bin/env python3

"""
Generates main.yml file from provided build triggers.
"""

import os
import shutil
import re
import json
import tempfile
from typing import Any

from library import common
from library import cci

# Constants
MAIN_FILENAME = "@main.yml"
COMMANDS_FOLDER = "commands"
WORKFLOW_FOLDER = "workflows"
JOB_FOLDER = "jobs"
COMPONENT_FOLDERS = [COMMANDS_FOLDER, WORKFLOW_FOLDER, JOB_FOLDER]

# Path constants
TMP_DIR = "/tmp"
CIRCLECI_DIR = ".circleci"
PATH_TO_WORKFLOW_JSON = os.path.join(CIRCLECI_DIR, "main", "workflows", "workflows_v2.json")
PATH_TO_PIPELINE_PARAMETERS = os.path.join(TMP_DIR, "pipeline-parameters.json")
PATH_TO_BUILD_COMPONENTS = os.path.join(TMP_DIR, "build-triggers.json")

# YAML indentation level increment
INDENT_STEP = 2

# Minimum workflow path length before adding empty job
MIN_WORKFLOW_PATH_LENGTH = 4

# Regex pattern for parsing main.yml
RE_PATTERN = r"^.*#.*#"

# Workflow name mappings (key in build_components -> workflow name)
SINGLE_COMPONENT_WORKFLOW_NAMES = {
    "docs": "docs",
    "ui": "ui",
    "build-publish": "build-publish",
    "build-deploy": "build-deploy",
    "release-build": "release-build",
    "experimental": "experimental",
}

# Build component to job name mappings for workflow generation
# Format: (component_key, job_name, requires_key_check)
BUILD_COMPONENT_JOBS = [
    ("master-branch", "master-branch", True),
    ("merge-foundation", "merge-foundation-branch", True),
    ("rpms", "rpms", False),
    ("integration", "integration-test", False),
    ("smoke", "smoke", False),
    ("debs", "debs", False),
    ("oci", "oci", False),
    ("oci-arm64", "oci-arm64", False),
    ("oci-all", "oci-all", False),
    ("trivy-scan", "trivy-scan", False),
    ("trivy-analyze", "trivy-analyze", False),
    ("experimental", "experimental", False),
    ("build-deploy", "build-deploy", False),
    ("release-build", "release-build", False),
    ("docs", "docs", False),
    ("ui", "ui", False),
    ("coverage", "weekly-coverage", False),
    ("build-publish", "build-publish", False),
]


def append_to_sample_workflow(workflow_path: list[str], entry: list[str]) -> list[str]:
    """Append workflow entries to the workflow path."""
    if len(workflow_path) > 1:
        workflow_path.extend(entry)
    else:
        workflow_path = entry
    return workflow_path


def combine_workflow_path(job_entry_spaces: int, workflow_path: list[str]) -> list[str]:
    """Combine workflow path entries, removing duplicates."""
    entry_indices = []
    combined_entries = []
    
    pattern = "^" + (" " * job_entry_spaces) + "- "
    for index, element in enumerate(workflow_path):
        if re.match(pattern, element):
            entry_indices.append(index)

    for index, position in enumerate(entry_indices):
        if index < len(entry_indices) - 1:
            output = "\n".join(workflow_path[position : entry_indices[index + 1]])
        else:
            output = "\n".join(workflow_path[position:])

        if output not in combined_entries:
            combined_entries.append(output)

    return combined_entries


def flatten_workflow_output(workflow_path: list[Any]) -> str:
    """Flatten nested workflow path into a single string output."""
    output = ""
    for line in workflow_path:
        if isinstance(line, list):
            for entry_lvl2 in line:
                if isinstance(entry_lvl2, list):
                    for entry_lvl3 in entry_lvl2:
                        output += entry_lvl3 + "\n"
                else:
                    output += entry_lvl2 + "\n"
        else:
            output += line + "\n"
    return output


def get_workflow_name(build_components: dict[str, bool]) -> str:
    """Determine the workflow name based on enabled build components."""
    enabled_count = sum(1 for v in build_components.values() if v)
    
    if enabled_count > 1:
        return "combined-builds"
    elif enabled_count == 1:
        for key, name in SINGLE_COMPONENT_WORKFLOW_NAMES.items():
            if build_components.get(key):
                return name
    return "build"


def should_add_auto_reruns(branch_name: str | None, build_components: dict[str, bool]) -> bool:
    """Check if auto-reruns should be enabled for the workflow."""
    if not branch_name:
        return False
    return (
        branch_name == "develop" or branch_name.startswith(("foundation-", "release-"))
    ) and not build_components.get("coverage", False)


def add_workflow_job(
    workflow_path: list[str],
    level: int,
    filters_enabled: bool,
    job_name: str,
    circle_ci: cci.cci,
) -> list[str]:
    """Add a job to the workflow path and print its dependencies."""
    print(f"{job_name}:", circle_ci.get_Workflow_dependency(job_name))
    workflow = circle_ci.get_Workflow_yaml(job_name, level, enable_filters=filters_enabled)
    return append_to_sample_workflow(workflow_path, workflow)


def process_build_components(
    build_components: dict[str, bool],
    workflow_path: list[str],
    level: int,
    filters_enabled: bool,
    circle_ci: cci.cci,
) -> list[str]:
    """Process all build components and add corresponding jobs to workflow."""
    for component_key, job_name, requires_key_check in BUILD_COMPONENT_JOBS:
        if requires_key_check:
            if component_key in build_components and build_components[component_key]:
                workflow_path = add_workflow_job(
                    workflow_path, level, filters_enabled, job_name, circle_ci
                )
        else:
            if build_components.get(component_key):
                workflow_path = add_workflow_job(
                    workflow_path, level, filters_enabled, job_name, circle_ci
                )
    return workflow_path


def should_add_empty_job(build_components: dict[str, bool], workflow_path: list[str]) -> bool:
    """Check if an empty job should be added to the workflow."""
    return (
        not build_components.get("build-deploy")
        and not build_components.get("docs")
        and not build_components.get("ui")
        and not build_components.get("coverage")
        and len(workflow_path) < MIN_WORKFLOW_PATH_LENGTH
    )


def load_json_file(path: str) -> dict[str, Any]:
    """Load and parse a JSON file."""
    with open(path, "r", encoding="UTF-8") as file_handler:
        return json.load(file_handler)


def read_file_lines(path: str) -> list[str]:
    """Read a file and return its lines."""
    with open(path, "r", encoding="UTF-8") as file_handler:
        return file_handler.readlines()


def write_file(path: str, content: str) -> None:
    """Write content to a file."""
    with open(path, "w", encoding="UTF-8") as file_handler:
        file_handler.write(content)


def cleanup_temp_files(working_dir: str, main_folder: str) -> None:
    """Clean up temporary files after processing."""
    files_to_remove = ["@main.yml", "executors.yml", "parameters.yml"]
    for filename in files_to_remove:
        filepath = os.path.join(main_folder, filename)
        if os.path.exists(filepath):
            os.remove(filepath)


def main() -> None:
    """Main entry point for generating the CircleCI main.yml file."""
    circle_ci = cci.cci()
    common_library = common.common()
    
    with tempfile.TemporaryDirectory() as working_dir:
        # Copy .circleci folder to working directory
        circleci_work_path = os.path.join(working_dir, CIRCLECI_DIR)
        shutil.copytree(CIRCLECI_DIR, circleci_work_path)
        
        # Set up paths
        main_folder = os.path.join(circleci_work_path, "main")
        path_to_main_yml = os.path.join(main_folder, MAIN_FILENAME)
        path_to_modified_main = os.path.join(
            circleci_work_path, MAIN_FILENAME.replace("@", "")
        )
        path_to_executors_yml = os.path.join(main_folder, "executors.yml")
        path_to_parameters_yml = os.path.join(main_folder, "parameters.yml")
        
        # Load configuration files
        pipeline_parameters = load_json_file(PATH_TO_PIPELINE_PARAMETERS)
        build_components = load_json_file(PATH_TO_BUILD_COMPONENTS)
        
        filters_enabled = True
        
        print("main_filename:", MAIN_FILENAME)
        print("path_to_main:", path_to_main_yml)
        print("path_to_modified_main:", path_to_modified_main)
        print("components_path:", main_folder)
        
        # Clean up existing temp folder
        tmp_circleci_path = os.path.join(TMP_DIR, CIRCLECI_DIR)
        if os.path.exists(tmp_circleci_path):
            print("clean up existing folder:", tmp_circleci_path)
            shutil.rmtree(tmp_circleci_path)
        
        # Read and process main.yml
        main_yml_content = read_file_lines(path_to_main_yml)
        
        keywords = common_library.extract_keywords(path_to_main_yml)
        
        for keyword in keywords:
            for sub_keyword in keywords[keyword]:
                if "workflows" in keyword:
                    continue
                tmp_page = sub_keyword.replace("#", "").replace(keyword + ":", "")
                if ".index" in tmp_page:
                    keywords[keyword][sub_keyword]["commands"] = common_library.expand_index(
                        sub_keyword, main_folder, []
                    )
                else:
                    keywords[keyword][sub_keyword]["commands"] = common_library.expand_keyword(
                        sub_keyword, main_folder
                    )
        
        final_output = ""
        
        for line in main_yml_content:
            re_match = re.match(RE_PATTERN, line)
            if re_match:
                if "#workflows#" in re_match.group():
                    circle_ci.set_Workflow(PATH_TO_WORKFLOW_JSON)
                    workflow_path: list[str] = []
                    
                    level = 0
                    if "workflows:" not in workflow_path:
                        workflow_path.append(common_library.create_space(level) + "workflows:")
                    level += INDENT_STEP
                    
                    workflow_name = get_workflow_name(build_components)
                    workflow_path.append(
                        common_library.create_space(level) + workflow_name + ":"
                    )
                    
                    level += INDENT_STEP
                    branch_name = os.environ.get("CIRCLE_BRANCH")
                    
                    if should_add_auto_reruns(branch_name, build_components):
                        workflow_path.append(
                            common_library.create_space(level) + "max_auto_reruns: 3\n" +
                            common_library.create_space(level) + "jobs:"
                        )
                    else:
                        workflow_path.append(common_library.create_space(level) + "jobs:")
                    
                    level += INDENT_STEP
                    job_entry_spaces = level
                    
                    # Process all build components
                    workflow_path = process_build_components(
                        build_components, workflow_path, level, filters_enabled, circle_ci
                    )
                    
                    # Add empty job if needed
                    if should_add_empty_job(build_components, workflow_path):
                        workflow_path = add_workflow_job(
                            workflow_path, level, filters_enabled, "empty", circle_ci
                        )
                    
                    if workflow_path:
                        final_workflow_path = ["\n".join(workflow_path[:3])]
                        final_workflow_path.extend(
                            combine_workflow_path(job_entry_spaces, workflow_path[3:])
                        )
                        final_output += flatten_workflow_output(final_workflow_path)
                    continue
                
                block_type, step = re_match.group().split(":")
                commands = keywords[block_type.replace("#", "").strip()][
                    re_match.group().strip()
                ]["commands"]
                
                for command in commands:
                    if isinstance(command, list):
                        for sub_command in command:
                            final_output += sub_command
                    else:
                        final_output += command
            else:
                final_output += line
        
        # Append executors and parameters
        final_output += "\n"
        executors_content = read_file_lines(path_to_executors_yml)
        final_output += "".join(executors_content)
        
        final_output += "\n"
        parameters_content = read_file_lines(path_to_parameters_yml)
        final_output += "".join(parameters_content)
        
        # Write the modified main.yml
        write_file(path_to_modified_main, final_output)
        
        # Clean up and move files
        cleanup_temp_files(working_dir, main_folder)
        
        # Move the .circleci with updated main.yml file into tmp directory
        shutil.move(circleci_work_path, TMP_DIR)
        
        # Remove component folders
        for folder in COMPONENT_FOLDERS:
            folder_path = os.path.join(tmp_circleci_path, "main", folder)
            if os.path.exists(folder_path):
                shutil.rmtree(folder_path)


if __name__ == "__main__":
    main()
