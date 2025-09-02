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
    if isinstance(workflow_path, list):
        workflow_path.extend(entry)
    else:
        workflow_path = entry
    return workflow_path

def combine_workflow_path(job_entry_spaces, workflow_path):
    entries = []
    indices = []
    for idx, line in enumerate(workflow_path):
        if re.match(fr"^{ ' ' * {job_entry_spaces} }-", line):
            indices.append(idx)
    combined = []
    for i, idx in enumerate(indices):
        start = idx
        end = indices[i + 1] if i + 1 < len(indices) else len(workflow_path)
        combined.append("\n".join(workflow_path[start:end]))
    return combined

def main():
    circleCI = cci.cci()
    common_library = common.common()
    working_dir = tempfile.TemporaryDirectory()
    
    # Copy .circleci directory to working directory
    shutil.copytree(".circleci", os.path.join(working_dir.name, ".circleci"))
    
    path_to_main_yml = os.path.join(working_dir.name, ".circleci", "main", "@main.yml")
    path_to_modified_main = os.path.join(working_dir.name, ".circleci", "main.yml")
    path_to_pipeline_params = os.path.join("/tmp", "pipeline-parameters.json")
    path_to_build_components = os.path.join("/tmp", "build-triggers.json")

    with open(path_to_pipeline_params, "r", encoding="utf-8") as f:
        pipeline_params = json.load(f)

    with open(path_to_build_components, "r", encoding="utf-8") as f:
        build_components = json.load(f)

    # Read main.yml and extract keywords
    with open(path_to_main_yml, "r", encoding="utf-8") as f:
        main_yml_content = f.readlines()
    
    keywords = common_library.extract_keywords(path_to_main_yml)

    for keyword, sub_keywords in keywords.items():
        for sub_keyword in sub_keywords:
            if "workflows" in keyword:
                continue
            tmp_page = sub_keyword.replace("#", "").replace(f"{keyword}:", "")
            if ".index" in tmp_page:
                keywords[keyword][sub_keyword]["commands"] = common_library.expand_index(
                    sub_keyword, os.path.join(working_dir.name, ".circleci", "main"), []
                )
            else:
                keywords[keyword][sub_keyword]["commands"] = common_library.expand_keyword(
                    sub_keyword, os.path.join(working_dir.name, ".circleci", "main")
                )

    final_output = []
    RE_PATTERN = r"^.*#.*#$"

    for line in main_yml_content:
        if re.match(RE_PATTERN, line):
            if "#workflows#" in line:
                circleCI.set_Workflow(os.path.join(".circleci", "main", "workflows", "workflows_v2.json"))
                workflow_path = []
                level = 0
                if "workflows:" not in workflow_path:
                    workflow_path.append("  workflows:")
                level += 2

                workflow_name = "build"
                enabled_components = [val for val in build_components.values() if val]
                if len(enabled_components) > 1:
                    workflow_name = "combined-builds"
                elif len(enabled_components) == 1:
                    workflow_name = {
                        True: "build-publish",
                        False: "build"
                    }[enabled_components[0]]
                else:
                    workflow_name = "build"

                workflow_path.append("  " + workflow_name + ":")
                workflow_path.append("    jobs:")

                job_entry_spaces = 4
                workflow_path = print_add(workflow_path, job_entry_spaces, filters_enabled=True, **build_components)

                finaly_workflow_path = ["\n".join(workflow_path[:3])]
                finaly_workflow_path.extend(combine_workflow_path(job_entry_spaces, workflow_path[3:]))

                for entry in finaly_workflow_path:
                    if isinstance(entry, list):
                        for line in entry:
                            final_output.append(line)
                    else:
                        final_output.append(entry)
                continue

            block_type, step = re.match(r"^(.+):(.+)$", line).groups()
            commands = keywords[block_type.replace("#", "").strip()][step.strip()]["commands"]
            for command in commands:
                if isinstance(command, list):
                    final_output.extend(command)
                else:
                    final_output.append(command)
        else:
            final_output.append(line)

    # Append parameters and executors
    with open(os.path.join(working_dir.name, ".circleci", "main", "parameters.yml"), "r") as f:
        final_output.extend(f.readlines())
    with open(os.path.join(working_dir.name, ".circleci", "main", "executors.yml"), "r") as f:
        final_output.extend(f.readlines())

    # Write final output
    with open(path_to_modified_main, "w", encoding="utf-8") as f:
        f.write("\n".join(final_output))

    # Cleanup
    os.remove(os.path.join(working_dir.name, ".circlecy", "main", "@main.yml"))
    os.remove(os.path.join(working_dir.name, ".circlecy", "main", "executors.yml"))
    os.remove(os.path.join(working_dir.name, ".circlecy", "main", "parameters.yml"))
    shutil.move(os.path.join(working_dir.name, ".circleci"), "/tmp/")
    for folder in ["main", "executors", "parameters"]:
        shutil.rmtree(os.path.join("/tmp", ".circleci", folder))
    working_dir.cleanup()

def print_add(workflow_path, job_entry_spaces, filters_enabled, **components):
    for name, enabled in components.items():
        if enabled:
            job_name = {
                "build-publish": "build-publish",
                "build": "build",
                "combined-builds": "combined-builds"
            }[name]
            workflow_path.append(f"    {job_name}:")
    return workflow_path

if __name__ == "__main__":
    main()