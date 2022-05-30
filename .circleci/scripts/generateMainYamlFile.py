
import glob
import os 
import sys

_mainFile="@main.yml"
pathToMainFile=os.path.join(".",_mainFile)

workflow_folder="workflows"
job_folder="jobs"

print("HI from generateMainYamlFile")
sys.exit()

_data={}
for folder in [workflow_folder,job_folder]:
    _files=glob.glob(os.path.join(folder,"*.yml"))

    #Process the files:
    for file in _files:
        _key=os.path.basename(file).replace(".yml","")
        _data[_key]=""
        with open(file,"r") as f:
            _data[_key]=f.readlines()
        if _data[_key][0] in ["workflows:  \n","jobs:\n"]:
            del _data[_key][0] 

_mainFileContent=""
with open(pathToMainFile,"r") as f:
    _mainFileContent=f.readlines()

for k in _data.keys():
    print("Processing "+k.strip())
    
    _position_of_occurance=[i for i, item in enumerate(_mainFileContent) if k in item]
    
    if len(_position_of_occurance) > 1:
        print("Something is wrong, we shouldn't have duplicate entry for "+k+" in "+_mainFile)
        sys.exit(1)
    
    if len(_position_of_occurance) < 1:
        print("Something is wrong, we don't have an entry for "+k+" in "+_mainFile)
        sys.exit(1)

    _position_of_occurance=_position_of_occurance[0]
    
    _string_to_append=""
    for l in _data[k]:
        _string_to_append+=l

    _mainFileContent[_position_of_occurance]=_string_to_append+"\n"

    

with open(_mainFile+"_changed","w") as f:
    for _l in _mainFileContent:
        f.write(_l)