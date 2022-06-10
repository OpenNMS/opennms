#!/usr/bin/env python3

import subprocess
import glob
import os
import shutil 
import sys
import tempfile


#this shouldn't be here
subprocess.Process()
print(os.getcwd())
base = subprocess.run(
    ['grep', '-rm','1', '@org.junit.experimental.categories.Category(org.opennms.smoketest.junit.FlakyTests.class)','./'],
    check=True,
    capture_output=True
).stdout.decode('utf-8').strip()
print(base)
print("We are not suppose to have this line here")
#end this shouldn't be here


working_directory=tempfile.TemporaryDirectory()

# We don't want to modify the main files, make a copy of the .circleci folder 
# into our working directory 
shutil.copytree(".circleci",os.path.join(working_directory.name,".circleci"))

main_filename="@main.yml"
path_to_main=os.path.join(working_directory.name,".circleci","main",main_filename)
path_to_modified_main=os.path.join(working_directory.name,".circleci","main",main_filename)
 
workflow_folder="workflows"
job_folder="jobs"

components_path=os.path.join(working_directory.name,".circleci","main")

print("main_filename:",main_filename)
print("path_to_main:",path_to_main)
print("path_to_modified_main:",path_to_modified_main)
print("components_path:",components_path)

main_yml_content={}

# Read the @main.yml file
with open(path_to_main,"r") as f:
    main_yml_content=f.readlines()

# Load the yml files into a dictionary for easier processing
components_data={}
for folder in [workflow_folder,job_folder]:
    print("Processing",folder,"component")

    component_files=glob.glob(os.path.join(components_path,folder,"*.yml"))

    print("","\n ".join(component_files).replace(os.path.join(components_path,"jobs"),""))

    for file in component_files:
        token=os.path.basename(file).replace(".yml","")

        print("Processing",token)

        if token not in components_data:
            components_data[token]=""

        with open(file,"r") as f:
            components_data[token]=f.readlines()
        
        if components_data[token][0] in ["workflows:\n","jobs:\n"]:
            print("\tDeleting extra",components_data[token][0].strip(),"entries")
            del components_data[token][0] 

print("Generating the @main.yml")

for token in components_data.keys():
    print("Processing "+token.strip())

    token_position=[i for i, item in enumerate(main_yml_content) if token in item]
    if len(token_position) > 1:
        print("\tSomething is wrong, we shouldn't have duplicate entry for "+token+" in "+main_filename)
        sys.exit(1)
    
    if len(token_position) < 1:
        print("\tSomething is wrong, we don't have an entry for "+token+" in "+main_filename)
        sys.exit(1)

    token_position=token_position[0]

    string_to_append=""
    for line in components_data[token]:
        string_to_append+=line

    print("\tReplacing the occurance on line "+str(token_position))
    main_yml_content[token_position]=string_to_append+"\n"
    
for folder in [workflow_folder,job_folder]:
    _files=glob.glob(os.path.join(components_path,folder,"*.yml"))

    for _file in _files:
        os.rename(_file,_file+"_analyzed")


with open(path_to_modified_main,"w") as f:
    for _l in main_yml_content:
        f.write(_l)

# move the .circleci with updated main.yml file into tmp directory
shutil.move(os.path.join(working_directory.name,".circleci"),"/tmp/")

working_directory.cleanup()