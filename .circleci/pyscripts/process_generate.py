#/usr/bin/env python3
import os
import re
import json
from library import libgit
from library import libfile

path_to_build_components=os.path.join("/tmp","build-components.json")

output_path = os.environ.get('OUTPUT_PATH')
head = os.environ.get('CIRCLE_SHA1')
base_revision = os.environ.get('BASE_REVISION')

libgit = libgit.libgit("stdout")

libfile = libfile.libfile()
#os.chdir(os.environ.get("CIRCLE_WORKING_DIRECTORY"))

libgit.switchBranch(base_revision)
libgit.switchBranch(head)

base= libgit.commonAncestor(base_revision, head)

print("output_path",output_path)
print("head",head)
print("base_revision",base_revision)
print("base",base)

if head == base:
    try:
        base = libgit.getCommitSHA("HEAD~1")
    except:
        base = '4b825dc642cb6eb9a060e54bf8d69288fbee4904'

print("base",base)

changes=libgit.getChangedFilesInCommits(base,head)

mappings=[
    m.split() for m in
    os.environ.get('MAPPING').splitlines()
]

def check_mapping(m):
    if 3 != len(m):
        raise Exception("Invalid mapping")
    path, param, value = m
    regex = re.compile(r'^' + path + r'$')
    for change in changes:
        if regex.match(change):
            return True
    return False

def convert_mapping(m):
    return [m[1], json.loads(m[2])]

mappings = filter(check_mapping, mappings)
mappings = map(convert_mapping, mappings)
mappings = dict(mappings)

print("Mappings:",mappings)

# If *IT.java files have changed -> enable integration builds
# If *Test.java files have changed -> enable smoke builds
# if Dockerfiles (under opennms-container) have changed enable docker builds
What_to_build=[]
for change in changes:
    if "IT.java" in change:
        if "Integration_tests" not in What_to_build:
            What_to_build.append("Integration_tests")
    elif "Test.java" in change:
        if "Smoke_tests" not in What_to_build:
            What_to_build.append("Smoke_tests")
    elif "opennms-container" in change:
        if "horizon" in change:
            if "OCI_horizon_image" not in What_to_build:
                What_to_build.append("OCI_horizon_image")
        elif "minion" in change:
            if "OCI_minion_image" not in What_to_build:
                What_to_build.append("OCI_minion_image")
        elif "sentinel" in change:
            if "OCI_sentinel_image" not in What_to_build:
                What_to_build.append("OCI_sentinel_image")
    elif ".circleci" in change:
        if "circleci_configuration" not in What_to_build:
            What_to_build.append("circleci_configuration")

print("What we want to build:",What_to_build,len(What_to_build))
git_keywords=libgit.extractKeywordsFromLastCommit()
print("Git Keywords:",git_keywords)
if "circleci_configuration" in What_to_build and len(What_to_build) == 1 :
    #if circleci_configuration is the only entry in the list we don't want to trigger a buildss.
    mappings["trigger-build"]=False

if "smoke" in git_keywords or "Smoke_tests" in What_to_build:   
    mappings["trigger-smoke"]=True
else:
    mappings["trigger-smoke"]=False

if "docker" in git_keywords:   
    mappings["trigger-docker"]=True
else:
    mappings["trigger-docker"]=False

if "rpms" in git_keywords:   
    mappings["trigger-rpms"]=True
else:
    mappings["trigger-rpms"]=False

if "debs" in git_keywords:   
    mappings["trigger-debs"]=True
else:
    mappings["trigger-debs"]=False

if "integration" in git_keywords or "Integration_tests" in What_to_build:   
    mappings["trigger-integration"]=True
else:
    mappings["trigger-integration"]=False

if "experimentalPath" in git_keywords:
    mappings["trigger-experimental"]=True
else:
    mappings["trigger-experimental"]=False

libfile.write_file(output_path,json.dumps(mappings))

#TODO: Create the build-components.json file 
libfile.write_file(path_to_build_components,json.dumps(mappings))

