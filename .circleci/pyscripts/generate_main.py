#!/usr/bin/env python3

"""
Generates main.yml file from provided build triggers.
"""

import os
import shutil
import re
import json
import tempfile
from library import common
from library import cci


def append_to_sample_workflow(workflow_path, entry):
    if len(workflow_path) > 1:
        for e in entry:
            workflow_path.append(e)
    else:
        workflow_path = entry

    return workflow_path


def combine_workflow_path(job_entry_spaces, workflow_path):
    _entries = []
    combined_entries = []
    for index, element in enumerate(workflow_path):
        re_match = re.match("^" + (" " * job_entry_spaces) + "- ", element)
        if re_match:
            _entries.append(index)

    for index, position in enumerate(_entries):
        if index < len(_entries) - 1:
            _output = "\n".join(workflow_path[position : _entries[index + 1]])
        else:
            _output = "\n".join(workflow_path[position:])

        if _output not in combined_entries:
            combined_entries.append(_output)

    return combined_entries


circleCI = cci.cci()

common_library = common.common()

working_directory = tempfile.TemporaryDirectory()

# We don't want to modify the main files, make a copy of the .circleci folder
# into a working directory
shutil.copytree(".circleci", os.path.join(working_directory.name, ".circleci"))

path_to_workflow_json = os.path.join(
    ".circleci", "main", "workflows", "workflows_v2.json"
)


MAIN_FILENAME = "@main.yml"
path_to_main_folder = os.path.join(working_directory.name, ".circleci", "main")
path_to_main_yml = os.path.join(path_to_main_folder, MAIN_FILENAME)
path_to_modified_main = os.path.join(
    working_directory.name, ".circleci", MAIN_FILENAME.replace("@", "")
)

path_to_executors_yml = os.path.join(path_to_main_folder, "executors.yml")
path_to_parameters_yml = os.path.join(path_to_main_folder, "parameters.yml")

path_to_pipeline_parameters = os.path.join("/tmp", "pipeline-parameters.json")
with open(path_to_pipeline_parameters, "r", encoding="UTF-8") as file_handler:
    pipeline_parameters = json.load(file_handler)


path_to_build_components = os.path.join("/tmp", "build-triggers.json")
with open(path_to_build_components, "r", encoding="UTF-8") as file_handler:
    build_components = json.load(file_handler)


COMMANDS_FOLDER = "commands"
WORKFLOW_FOLDER = "workflows"
JOB_FOLDER = "jobs"
component_folders = [COMMANDS_FOLDER, WORKFLOW_FOLDER, JOB_FOLDER]

components_path = os.path.join(working_directory.name, ".circleci", "main")

filters_enabled = True

print("main_filename:", MAIN_FILENAME)
print("path_to_main:", path_to_main_yml)
print("path_to_modified_main:", path_to_modified_main)
print("components_path:", components_path)

if os.path.exists(os.path.join("/tmp", ".circleci")):
    print("clean up existing folder:", os.path.join("/tmp", ".circleci"))
    shutil.rmtree(os.path.join("/tmp", ".circleci"))


# Read the @main.yml file
with open(path_to_main_yml, "r", encoding="UTF-8") as file_handler:
    main_yml_content = file_handler.readlines()

keywords = common_library.extract_keywords(path_to_main_yml)

for keyword in keywords:
    for sub_keyword in keywords[keyword]:
        if "workflows" in keyword:
            continue
        tmp_page = sub_keyword.replace("#", "").replace(keyword + ":", "")
        if ".index" in tmp_page:
            keywords[keyword][sub_keyword]["commands"] = common_library.expand_index(
                sub_keyword, path_to_main_folder, []
            )
        else:
            keywords[keyword][sub_keyword]["commands"] = common_library.expand_keyword(
                sub_keyword, path_to_main_folder
            )

final_output = ""
RE_PATTERN = "^.*#.*#"


def print_add(workflow_path, level, filters_enabled, job_name):
    print(
        job_name + ":",
        circleCI.get_Workflow_dependency(job_name),
    )
    workflow = circleCI.get_Workflow_yaml(
        job_name, level, enable_filters=filters_enabled
    )
    return append_to_sample_workflow(workflow_path, workflow)


for e in main_yml_content:
    re_match = re.match(RE_PATTERN, e)
    if re_match:
        if "#workflows#" in re_match.group():
            circleCI.set_Workflow(path_to_workflow_json)
            workflow_path = []

            level = 0
            if "workflows:" not in workflow_path:
                workflow_path.append(common_library.create_space(level) + "workflows:")

            level = level + 2

            enabled_components = []
            for component in build_components:
                enabled_components.append(build_components[component])

            if enabled_components.count(True) > 1:
                workflow_name = "combined-builds"
            elif enabled_components.count(True) == 1:
                if build_components["docs"]:
                    workflow_name = "docs"
                elif build_components["ui"]:
                    workflow_name = "ui"
                elif build_components["build-publish"]:
                    workflow_name = "build-publish"
                elif build_components["build-deploy"]:
                    workflow_name = "build-deploy"
                elif build_components["experimental"]:
                    workflow_name = "experimental"
                else:
                    workflow_name = "build"
            else:
                workflow_name = "build"

            workflow_path.append(
                common_library.create_space(level) + workflow_name + ":"
            )

            level += 2
            workflow_path.append(common_library.create_space(level) +"max_auto_reruns: 3")
            workflow_path.append(common_library.create_space(level) + "jobs:")
            level += 2
            job_entry_spaces = level

            if (
                "master-branch" in build_components
                and build_components["master-branch"]
            ):
                workflow_path = print_add(
                    workflow_path, level, filters_enabled, "master-branch"
                )

            if (
                "merge-foundation" in build_components
                and build_components["merge-foundation"]
            ):
                workflow_path = print_add(
                    workflow_path, level, filters_enabled, "merge-foundation-branch"
                )

            if build_components["rpms"]:
                workflow_path = print_add(workflow_path, level, filters_enabled, "rpms")

            if build_components["integration"]:
                workflow_path = print_add(
                    workflow_path, level, filters_enabled, "integration-test"
                )

            if build_components["smoke"]:
                if filters_enabled:
                    tmp_filters_enabled = False

                workflow_path = print_add(
                    workflow_path, level, filters_enabled, "smoke"
                )

            if build_components["debs"]:
                workflow_path = print_add(workflow_path, level, filters_enabled, "debs")

            if build_components["oci"]:
                workflow_path = print_add(workflow_path, level, filters_enabled, "oci")

            if build_components["trivy-scan"]:
                workflow_path = print_add(workflow_path, level, filters_enabled, "trivy-scan")
            
            if build_components["trivy-analyze"]:
                workflow_path = print_add(workflow_path, level, filters_enabled, "trivy-analyze")

            if build_components["experimental"]:
                workflow_path = print_add(
                    workflow_path, level, filters_enabled, "experimental"
                )

            if build_components["build-deploy"]:
                workflow_path = print_add(
                    workflow_path, level, filters_enabled, "build-deploy"
                )

            if build_components["docs"]:
                workflow_path = print_add(workflow_path, level, filters_enabled, "docs")

            if build_components["ui"]:
                workflow_path = print_add(workflow_path, level, filters_enabled, "ui")

            if build_components["coverage"]:
                workflow_path = print_add(
                    workflow_path, level, filters_enabled, "weekly-coverage"
                )

            if build_components["build-publish"]:
                workflow_path = print_add(
                    workflow_path, level, filters_enabled, "build-publish"
                )

            if (
                not build_components["build-deploy"]
                and not build_components["docs"]
                and not build_components["ui"]
                and not build_components["coverage"]
                and len(workflow_path) < 4
            ):
                workflow_path = print_add(
                    workflow_path, level, filters_enabled, "empty"
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

final_output += "\n"
with open(path_to_executors_yml, "r", encoding="UTF-8") as file_handler:
    executors_yml_content = file_handler.readlines()

for e in executors_yml_content:
    final_output += e

final_output += "\n"
with open(path_to_parameters_yml, "r", encoding="UTF-8") as file_handler:
    parameters_yml_content = file_handler.readlines()

for e in parameters_yml_content:
    final_output += e

with open(path_to_modified_main, "w", encoding="UTF-8") as file_handler:
    file_handler.write(final_output)


os.remove(os.path.join(working_directory.name, ".circleci", "main", "@main.yml"))
os.remove(os.path.join(working_directory.name, ".circleci", "main", "executors.yml"))
os.remove(os.path.join(working_directory.name, ".circleci", "main", "parameters.yml"))

# move the .circleci with updated main.yml file into tmp directory
shutil.move(os.path.join(working_directory.name, ".circleci"), "/tmp/")

for folder in component_folders:
    shutil.rmtree(os.path.join("/tmp", ".circleci", "main", folder))

working_directory.cleanup()
