#!/usr/bin/env python3

"""
Generates main.yml file from provided build triggers.
"""

import os
import sys
import shutil
import re
import json
import tempfile
from typing import List, Dict, Any
from pathlib import Path
from library import common
from library import cci

# Constants
MAIN_FILENAME = "@main.yml"
COMMANDS_FOLDER = "commands"
WORKFLOW_FOLDER = "workflows"
JOB_FOLDER = "jobs"
RE_PATTERN = r"^.*#.*#"

COMPONENT_FOLDERS = [COMMANDS_FOLDER, WORKFLOW_FOLDER, JOB_FOLDER]

# Component to workflow name mapping
COMPONENT_TO_WORKFLOW = {
    'master-branch': 'master-branch',
    'merge-foundation': 'merge-foundation-branch',
    'rpms': 'rpms',
    'integration': 'integration-test',
    'smoke': 'smoke',
    'debs': 'debs',
    'oci': 'oci',
    'trivy-scan': 'trivy-scan',
    'trivy-analyze': 'trivy-analyze',
    'experimental': 'experimental',
    'build-deploy': 'build-deploy',
    'docs': 'docs',
    'ui': 'ui',
    'coverage': 'weekly-coverage',
    'build-publish': 'build-publish',
}


def append_to_sample_workflow(workflow_path: List, entry: List) -> List:
    """Append workflow entries to the workflow path."""
    if len(workflow_path) > 1:
        for e in entry:
            workflow_path.append(e)
    else:
        workflow_path = entry
    return workflow_path


def combine_workflow_path(job_entry_spaces: int, workflow_path: List[str]) -> List[str]:
    """Combine workflow path entries by grouping job entries."""
    _entries = []
    combined_entries = []
    
    for index, element in enumerate(workflow_path):
        if re.match(r"^" + (" " * job_entry_spaces) + r"- ", element):
            _entries.append(index)

    for index, position in enumerate(_entries):
        if index < len(_entries) - 1:
            _output = "\n".join(workflow_path[position : _entries[index + 1]])
        else:
            _output = "\n".join(workflow_path[position:])

        if _output not in combined_entries:
            combined_entries.append(_output)

    return combined_entries


def determine_workflow_name(build_components: Dict[str, bool]) -> str:
    """Determine the workflow name based on enabled components."""
    enabled = [k for k, v in build_components.items() if v]
    
    if len(enabled) == 0:
        return "build"
    elif len(enabled) == 1:
        single_component_workflows = ['docs', 'ui', 'build-publish', 'build-deploy', 'experimental']
        return enabled[0] if enabled[0] in single_component_workflows else "build"
    else:
        return "combined-builds"


def add_workflow_job(
    workflow_path: List, 
    indent_level: int, 
    enable_filters: bool, 
    job_name: str,
    circleCI: Any
) -> List:
    """Add a job to the workflow and log the addition."""
    print(f"{job_name}: {circleCI.get_Workflow_dependency(job_name)}")
    workflow = circleCI.get_Workflow_yaml(job_name, indent_level, enable_filters=enable_filters)
    return append_to_sample_workflow(workflow_path, workflow)


def load_json_file(filepath: Path, description: str) -> Dict:
    """Load a JSON file with error handling."""
    try:
        with open(filepath, "r", encoding="UTF-8") as file_handler:
            return json.load(file_handler)
    except (FileNotFoundError, json.JSONDecodeError) as e:
        print(f"Error loading {description}: {e}")
        sys.exit(1)


# Initialize libraries
circleCI = cci.cci()
common_library = common.common()

# Create working directory
working_directory = tempfile.TemporaryDirectory()

# Copy .circleci folder to working directory
try:
    shutil.copytree(".circleci", os.path.join(working_directory.name, ".circleci"))
except Exception as e:
    print(f"Error copying .circleci folder: {e}")
    sys.exit(1)

# Define paths
path_to_workflow_json = Path(".circleci/main/workflows/workflows_v2.json")
path_to_main_folder = Path(working_directory.name) / ".circleci" / "main"
path_to_main_yml = path_to_main_folder / MAIN_FILENAME
path_to_modified_main = Path(working_directory.name) / ".circleci" / MAIN_FILENAME.replace("@", "")
path_to_executors_yml = path_to_main_folder / "executors.yml"
path_to_parameters_yml = path_to_main_folder / "parameters.yml"

# Load configuration files
pipeline_parameters = load_json_file(Path("/tmp/pipeline-parameters.json"), "pipeline parameters")
build_components = load_json_file(Path("/tmp/build-triggers.json"), "build components")

components_path = path_to_main_folder

filters_enabled = True

print("main_filename:", MAIN_FILENAME)
print("path_to_main:", path_to_main_yml)
print("path_to_modified_main:", path_to_modified_main)
print("components_path:", components_path)

cleanup_path = Path("/tmp/.circleci")
if cleanup_path.exists():
    print(f"Cleaning up existing folder: {cleanup_path}")
    shutil.rmtree(cleanup_path)

# Read the @main.yml file
try:
    with open(path_to_main_yml, "r", encoding="UTF-8") as file_handler:
        main_yml_content = file_handler.readlines()
except IOError as e:
    print(f"Error reading main.yml: {e}")
    sys.exit(1)

keywords = common_library.extract_keywords(str(path_to_main_yml))

for keyword in keywords:
    for sub_keyword in keywords[keyword]:
        if "workflows" in keyword:
            continue
        tmp_page = sub_keyword.replace("#", "").replace(keyword + ":", "")
        if ".index" in tmp_page:
            keywords[keyword][sub_keyword]["commands"] = common_library.expand_index(
                sub_keyword, str(path_to_main_folder), []
            )
        else:
            keywords[keyword][sub_keyword]["commands"] = common_library.expand_keyword(
                sub_keyword, str(path_to_main_folder)
            )

final_output = ""


for e in main_yml_content:
    re_match = re.match(RE_PATTERN, e)
    if re_match:
        if "#workflows#" in re_match.group():
            circleCI.set_Workflow(str(path_to_workflow_json))
            workflow_path = []

            level = 0
            if "workflows:" not in workflow_path:
                workflow_path.append(common_library.create_space(level) + "workflows:")
            level += 2

            # Determine workflow name
            workflow_name = determine_workflow_name(build_components)
            workflow_path.append(common_library.create_space(level) + workflow_name + ":")

            level += 2
            branch_name = os.environ.get("CIRCLE_BRANCH")
            
            # Add max_auto_reruns for specific branches
            if (branch_name and 
                (branch_name == "develop" or branch_name.startswith(("foundation-", "release-"))) and 
                not build_components.get("coverage", False)):
                workflow_path.append(
                    common_library.create_space(level) + "max_auto_reruns: 3\n" +
                    common_library.create_space(level) + "jobs:"
                )
            else:
                workflow_path.append(common_library.create_space(level) + "jobs:")
            
            level += 2
            job_entry_spaces = level

            # Add workflow jobs based on enabled components
            for component_key, workflow_job_name in COMPONENT_TO_WORKFLOW.items():
                if build_components.get(component_key, False):
                    workflow_path = add_workflow_job(
                        workflow_path, level, filters_enabled, workflow_job_name, circleCI
                    )

            # Add empty workflow if no components are enabled
            no_components_enabled = not any([
                build_components.get("build-deploy", False),
                build_components.get("docs", False),
                build_components.get("ui", False),
                build_components.get("coverage", False)
            ])
            
            if no_components_enabled and len(workflow_path) < 4:
                workflow_path = add_workflow_job(
                    workflow_path, level, filters_enabled, "empty", circleCI
                )

            if workflow_path:
                finaly_workflow_path = ["\n".join(workflow_path[:3])]
                finaly_workflow_path.extend(
                    combine_workflow_path(job_entry_spaces, workflow_path[3:])
                )

                for line in finaly_workflow_path:
                    if isinstance(line, list):
                        for entry_lvl2 in line:
                            if isinstance(entry_lvl2, list):
                                for entry_lvl3 in entry_lvl2:
                                    final_output += entry_lvl3 + "\n"
                            else:
                                final_output += entry_lvl2 + "\n"
                    else:
                        final_output += line + "\n"
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
        final_output += e

def append_file_content(output: str, filepath: Path) -> str:
    """Append file content to output string."""
    try:
        with open(filepath, "r", encoding="UTF-8") as file_handler:
            return output + "\n" + file_handler.read()
    except IOError as e:
        print(f"Error reading {filepath}: {e}")
        sys.exit(1)


# Append executors and parameters to final output
final_output = append_file_content(final_output, path_to_executors_yml)
final_output = append_file_content(final_output, path_to_parameters_yml)

# Write the final output
try:
    with open(path_to_modified_main, "w", encoding="UTF-8") as file_handler:
        file_handler.write(final_output)
except IOError as e:
    print(f"Error writing modified main file: {e}")
    sys.exit(1)

# Clean up intermediate files
for filename in [MAIN_FILENAME, "executors.yml", "parameters.yml"]:
    filepath = path_to_main_folder / filename
    if filepath.exists():
        filepath.unlink()

# Move the .circleci directory to /tmp/
try:
    shutil.move(str(Path(working_directory.name) / ".circleci"), "/tmp/")
except Exception as e:
    print(f"Error moving .circleci folder: {e}")
    sys.exit(1)

# Remove component folders
for folder in COMPONENT_FOLDERS:
    folder_path = Path("/tmp/.circleci/main") / folder
    if folder_path.exists():
        shutil.rmtree(folder_path)

working_directory.cleanup()
