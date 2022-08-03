#!/usr/bin/env python3

import os
from random import sample
import shutil
import re
import tempfile
from turtle import pos
from library import common
from library import libfile

from library import cci

def append_to_sample_workflow(workflow_path,entry):
    if len(workflow_path) > 1:
        for e in entry:
            #if e not in workflow_path:
            workflow_path.append(e)
    else:
        workflow_path=entry

    return workflow_path

def combine_workflow_path(job_entry_spaces,workflow_path):
    _entries=[]
    combined_entries=[]
    for index,element in enumerate(workflow_path):
        re_match = re.match("^"+(" "*job_entry_spaces)+"- ", element)
        if  re_match:
            _entries.append(index)

    for index,position in enumerate(_entries):        
        if index < len(_entries)-1:
            _output="\n".join(workflow_path[position:_entries[index+1]])
        else:
            _output="\n".join(workflow_path[position:])

        if _output not in combined_entries:
            combined_entries.append(_output)
       
    return combined_entries

            




circleCI = cci.cci()

file_library = libfile.libfile()
common_library = common.common()

working_directory = tempfile.TemporaryDirectory()

# We don't want to modify the main files, make a copy of the .circleci folder
# into a working directory
shutil.copytree(".circleci", os.path.join(working_directory.name, ".circleci"))

path_to_workflow_json = os.path.join(
    ".circleci", "main", "workflows", "workflows_v2.json")


main_filename = "@main.yml"
path_to_main_folder = os.path.join(working_directory.name, ".circleci", "main")
path_to_main_yml = os.path.join(path_to_main_folder, main_filename)
path_to_modified_main = os.path.join(
    working_directory.name, ".circleci", main_filename.replace("@", ""))

path_to_executors_yml = os.path.join(path_to_main_folder, "executors.yml")
path_to_parameters_yml = os.path.join(path_to_main_folder, "parameters.yml")

path_to_pipeline_parameters = os.path.join("/tmp", "pipeline-parameters.json")
pipeline_parameters = file_library.load_json(path_to_pipeline_parameters)


path_to_build_components = os.path.join("/tmp", "build-triggers.json")
build_components = file_library.load_json(path_to_build_components)


alias_folder = "aliases"
commands_folder = "commands"
workflow_folder = "workflows"
job_folder = "jobs"
component_folders = [alias_folder,
                     commands_folder, workflow_folder, job_folder]

components_path = os.path.join(working_directory.name, ".circleci", "main")

if "filters" in build_components:
    filters_enabled=build_components["filters"]["enabled"]
else:
    filters_enabled=True

print("main_filename:", main_filename)
print("path_to_main:", path_to_main_yml)
print("path_to_modified_main:", path_to_modified_main)
print("components_path:", components_path)
print("Filters Enabled",filters_enabled)

if os.path.exists(os.path.join("/tmp", ".circleci")):
    print("clean up existing folder:", os.path.join("/tmp", ".circleci"))
    shutil.rmtree(os.path.join("/tmp", ".circleci"))


# Read the @main.yml file
main_yml_content = file_library.read_file(path_to_main_yml)
keywords = common_library.extract_keywords(path_to_main_yml)

for keyword in keywords:
    for sub_keyword in keywords[keyword]:
        if "workflows" in keyword:
            continue
        tmp_page = sub_keyword.replace("#", "").replace(keyword+":", "")
        if ".index" in tmp_page:
            keywords[keyword][sub_keyword]["commands"] = common_library.expand_index(
                sub_keyword, path_to_main_folder, [])
        else:
            keywords[keyword][sub_keyword]["commands"] = common_library.expand_keyword(
                sub_keyword, path_to_main_folder)

final_output = ""
re_pattern = "^.*#.*#"

for e in main_yml_content:
    re_match = re.match(re_pattern, e)
    if re_match:
        if "#workflows#" in re_match.group():
            circleCI.set_Workflow(path_to_workflow_json)
            workflow_path=[]

            level = 0
            if "workflows:" not in workflow_path:
                workflow_path.append(
                    common_library.create_space(level)+"workflows:")
            level = level+2

            if not build_components["experimental"] and \
               (build_components["build"]["docs"] and build_components["build"]["ui"]) or \
               (build_components["build"]["docs"] and build_components["build"]["build"]) or \
               (build_components["build"]["ui"] and build_components["build"]["build"]):
                workflow_path.append(
                    common_library.create_space(level)+"multibuild:")
            elif not build_components["experimental"] and \
                    build_components["build"]["docs"] and \
                    not build_components["build"]["ui"] and \
                    not build_components["build"]["build"]:
                workflow_path.append(
                    common_library.create_space(level)+"docs:")
            elif not build_components["experimental"] and \
                    not build_components["build"]["docs"] and \
                    build_components["build"]["ui"] and \
                    not build_components["build"]["build"]:
                workflow_path.append(
                    common_library.create_space(level)+"ui:")
            elif not build_components["experimental"] and \
                    not build_components["build"]["docs"] and \
                    not build_components["build"]["ui"] and \
                    build_components["build"]["build"]:
                workflow_path.append(
                    common_library.create_space(level)+"build:")
            else:
                workflow_path.append(
                    common_library.create_space(level)+"autobuild:")

            level += 2
            workflow_path.append(common_library.create_space(level)+"jobs:")
            level += 2
            job_entry_spaces=level

            if build_components["rpm-packages"]["minion"] or \
               build_components["rpm-packages"]["horizon"] or \
               build_components["rpm-packages"]["sentinel"]:
                print("rpm-packages > all:",
                      circleCI.get_Workflow_dependency('rpms'))
                workflow = circleCI.get_Workflow_yaml(
                    "rpms", level, enable_filters=filters_enabled)
                workflow_path=append_to_sample_workflow(workflow_path,workflow)

            if build_components["tests"]["integration"]:
                print("tests > integration:",
                      circleCI.get_Workflow_dependency('integration-test'))

                workflow = circleCI.get_Workflow_yaml(
                    "integration-test", level,enable_filters=filters_enabled)
                workflow_path=append_to_sample_workflow(workflow_path,workflow)

            if build_components["tests"]["smoke"]:
                print("tests > smoke:", circleCI.get_Workflow_dependency('smoke'))
                workflow = circleCI.get_Workflow_yaml(
                    "smoke", level,enable_filters=False)
                workflow_path=append_to_sample_workflow(workflow_path,workflow)

            if build_components["tests"]["smoke-flaky"]:
                print("tests > smoke-flaky:", circleCI.get_Workflow_dependency('smoke-test-flaky'))
                workflow = circleCI.get_Workflow_yaml(
                    "smoke-test-flaky", level,enable_filters=False)
                workflow_path=append_to_sample_workflow(workflow_path,workflow)

            if build_components["debian-packages"]["minion"] or \
               build_components["debian-packages"]["horizon"] or \
               build_components["debian-packages"]["sentinel"]:
                print("debian-packages > all:",
                      circleCI.get_Workflow_dependency('debs'))
                workflow = circleCI.get_Workflow_yaml(
                    "debs", level,enable_filters=filters_enabled)
                workflow_path=append_to_sample_workflow(workflow_path,workflow)

            if build_components["oci-images"]["minion"] or \
               build_components["oci-images"]["horizon"] or \
               build_components["oci-images"]["sentinel"]:
                if build_components["oci-images"]["minion"] and \
                   not build_components["oci-images"]["horizon"] and \
                   not build_components["oci-images"]["sentinel"]:
                    print("oci-images > minion:",
                          circleCI.get_Workflow_dependency('minion-image'))
                    workflow = circleCI.get_Workflow_yaml(
                        "minion-image", level,enable_filters=filters_enabled)
                elif not build_components["oci-images"]["minion"] and \
                        build_components["oci-images"]["horizon"] and \
                        not build_components["oci-images"]["sentinel"]:
                    print("oci-images > horizon:",
                          circleCI.get_Workflow_dependency('horizon-image'))
                    workflow = circleCI.get_Workflow_yaml(
                        "horizon-image", level,enable_filters=filters_enabled)
                elif not build_components["oci-images"]["minion"] and \
                        not build_components["oci-images"]["horizon"] and \
                        build_components["oci-images"]["sentinel"]:
                    print("oci-images > sentinel:",
                          circleCI.get_Workflow_dependency('sentinel-image'))
                    workflow = circleCI.get_Workflow_yaml(
                        "sentinel-image", level,enable_filters=filters_enabled)
                else:
                    print("oci-images > all:",
                          circleCI.get_Workflow_dependency('oci'))
                    workflow = circleCI.get_Workflow_yaml(
                        "oci", level,enable_filters=filters_enabled)
                workflow_path=append_to_sample_workflow(workflow_path,workflow)

            if build_components["experimental"]:
                print("experimental:",
                      circleCI.get_Workflow_dependency('experimental'))
                workflow = circleCI.get_Workflow_yaml(
                    "experimental", level,enable_filters=filters_enabled)
                workflow_path=append_to_sample_workflow(workflow_path,workflow)
                
            if build_components["build"]["build"]:
                print("build> build:", circleCI.get_Workflow_dependency('build'))
                workflow = circleCI.get_Workflow_yaml(
                    "build", level,enable_filters=filters_enabled)
                workflow_path=append_to_sample_workflow(workflow_path,workflow)

            if build_components["build"]["docs"]:
                print("build> docs :", circleCI.get_Workflow_dependency('docs'))
                workflow = circleCI.get_Workflow_yaml(
                    "docs", level,enable_filters=filters_enabled)
                workflow_path=append_to_sample_workflow(workflow_path,workflow)

            if build_components["build"]["ui"]:
                print("build> ui :", circleCI.get_Workflow_dependency('ui'))
                workflow = circleCI.get_Workflow_yaml(
                    "ui", level,enable_filters=filters_enabled)
                workflow_path=append_to_sample_workflow(workflow_path,workflow)

            if build_components["build"]["coverage"]:
                print("build> coverage :",
                      circleCI.get_Workflow_dependency('weekly-coverage'))
                workflow = circleCI.get_Workflow_yaml(
                    "weekly-coverage", level,enable_filters=filters_enabled)
                workflow_path=append_to_sample_workflow(workflow_path,workflow)

            if build_components["build"]["deploy"]:
                print("build> deploy :",
                      circleCI.get_Workflow_dependency('build-deploy'))
                workflow = circleCI.get_Workflow_yaml(
                    "build-deploy", level,enable_filters=filters_enabled)
                workflow_path=append_to_sample_workflow(workflow_path,workflow)

            if build_components["publish"]["packages"]:
                print("publish> packages :",
                      circleCI.get_Workflow_dependency('build-publish'))
                workflow = circleCI.get_Workflow_yaml(
                    "build-publish", level,enable_filters=filters_enabled)
                workflow_path=append_to_sample_workflow(workflow_path,workflow)

            if not build_components["build"]["build"] and \
               not build_components["build"]["docs"] and \
               not build_components["build"]["ui"] and \
               not build_components["build"]["coverage"] and len(workflow_path) < 4:
                print("empty:", circleCI.get_Workflow_dependency('empty'))
                workflow = circleCI.get_Workflow_yaml(
                    "empty", level,enable_filters=filters_enabled)
                workflow_path=append_to_sample_workflow(workflow_path,workflow)
                
            if workflow_path:
                finaly_workflow_path=["\n".join(workflow_path[:3])]
                finaly_workflow_path.extend(combine_workflow_path(job_entry_spaces,workflow_path[3:]))

                for line in finaly_workflow_path:
                    if type(line) == list:
                        for entry_lvl2 in line:
                            if type(entry_lvl2) == list:
                                for entry_lvl3 in entry_lvl2:
                                    final_output += entry_lvl3+"\n"
                            else:
                                final_output += entry_lvl2+"\n"
                    else:
                        final_output += line+"\n"
            continue

        block_type, step = re_match.group().split(":")
        commands = keywords[block_type.replace(
            "#", "").strip()][re_match.group().strip()]["commands"]

        for command in commands:
            if type(command) == list:
                for sub_command in command:
                    final_output += sub_command
            else:
                final_output += command
    else:
        final_output += e

final_output += "\n"
executors_yml_content = file_library.read_file(path_to_executors_yml)
for e in executors_yml_content:
    final_output += e

final_output += "\n"
parameters_yml_content = file_library.read_file(path_to_parameters_yml)
for e in parameters_yml_content:
    final_output += e


file_library.write_file(path_to_modified_main, final_output)


os.remove(os.path.join(working_directory.name,
          ".circleci", "main", "@main.yml"))
os.remove(os.path.join(working_directory.name,
          ".circleci", "main", "executors.yml"))
os.remove(os.path.join(working_directory.name,
          ".circleci", "main", "parameters.yml"))

# move the .circleci with updated main.yml file into tmp directory
shutil.move(os.path.join(working_directory.name, ".circleci"), "/tmp/")

for folder in component_folders:
    shutil.rmtree(os.path.join("/tmp", ".circleci", "main", folder))

working_directory.cleanup()


