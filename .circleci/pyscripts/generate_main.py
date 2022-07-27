#!/usr/bin/env python3

import glob
import json
from operator import index
import os
import shutil 
import re
import tempfile
from library import common


common_library=common.common()

working_directory=tempfile.TemporaryDirectory()

# We don't want to modify the main files, make a copy of the .circleci folder 
# into our working directory 
shutil.copytree(".circleci",os.path.join(working_directory.name,".circleci"))

main_filename="@main.yml"
path_to_main_folder=os.path.join(working_directory.name,".circleci","main")
path_to_main_yml=os.path.join(path_to_main_folder,main_filename)
path_to_modified_main=os.path.join(working_directory.name,".circleci",main_filename.replace("@",""))
 
path_to_executors_yml=os.path.join(path_to_main_folder,"executors.yml")
path_to_parameters_yml=os.path.join(path_to_main_folder,"parameters.yml")

alias_folder="aliases"
commands_folder="commands"
workflow_folder="workflows"
job_folder="jobs"
component_folders=[alias_folder,commands_folder,workflow_folder,job_folder]

components_path=os.path.join(working_directory.name,".circleci","main")

print("main_filename:",main_filename)
print("path_to_main:",path_to_main_yml)
print("path_to_modified_main:",path_to_modified_main)
print("components_path:",components_path)


# Read the @main.yml file
main_yml_content=common_library.read_file(path_to_main_yml)
keywords=common_library.extract_keywords(path_to_main_yml)

for keyword in keywords:
    for sub_keyword in keywords[keyword]:
        tmp_page=sub_keyword.replace("#","").replace(keyword+":","") 
        if ".index" in tmp_page:
            keywords[keyword][sub_keyword]["commands"]=common_library.expand_index(sub_keyword,path_to_main_folder,[])
        else:
            keywords[keyword][sub_keyword]["commands"]=common_library.expand_keyword(sub_keyword,path_to_main_folder)

final_output=""
re_pattern="^.*#.*#"

for e in main_yml_content:
    re_match=re.match(re_pattern,e)
    if re_match:
        block_type,step=re_match.group().split(":")
        commands=keywords[block_type.replace("#","").strip()][re_match.group().strip()]["commands"]
        for command in commands:
            if type(command) == list:
                for sub_command in command:
                    final_output+=sub_command
            else:
                final_output+=command
    else:
        final_output+=e

final_output+="\n"
executors_yml_content=common_library.read_file(path_to_executors_yml)
for e in executors_yml_content:
    final_output+=e

final_output+="\n"
parameters_yml_content=common_library.read_file(path_to_parameters_yml)
for e in parameters_yml_content:
    final_output+=e

with open(path_to_modified_main,"w") as f:
        f.write(final_output)


os.remove(os.path.join(working_directory.name,".circleci","main","@main.yml"))
os.remove(os.path.join(working_directory.name,".circleci","main","executors.yml"))
os.remove(os.path.join(working_directory.name,".circleci","main","parameters.yml"))

## move the .circleci with updated main.yml file into tmp directory
shutil.move(os.path.join(working_directory.name,".circleci"),"/tmp/")


for folder in component_folders:
    shutil.rmtree(os.path.join("/tmp",".circleci","main",folder))

working_directory.cleanup()