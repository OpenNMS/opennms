#/usr/bin/env python3
import os
import re
import json
from library import libgit
from library import libfile

path_to_build_components=os.path.join("/tmp","build-triggers.json")
path_to_build_trigger_override=os.path.join(".circleci","build-triggers.override.json")

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
def addTobuildList(item):
    if item not in What_to_build:
        What_to_build.append(item)

for change in changes:
    if not change:
        continue
    if "IT.java" in change:
        #addTobuildList("Integration_tests")
        #addTobuildList("build")
        if "Integration_tests" not in What_to_build:
            What_to_build.append("Integration_tests")
    elif "Test.java" in change:
        addTobuildList("Smoke_tests")
        addTobuildList("build")
        #if "Smoke_tests" not in What_to_build:
        #    What_to_build.append("Smoke_tests")
    elif "opennms-container" in change:
        addTobuildList("build")
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
    elif "doc" in change :
        if "doc" not in What_to_build:
            What_to_build.append("doc")
    elif "ui" in change :
        if "ui" not in What_to_build:
            What_to_build.append("ui")
    else:
        if "build" not in What_to_build:
            What_to_build.append("build")

print("What we want to build:",What_to_build,len(What_to_build))
git_keywords=libgit.extractKeywordsFromLastCommit()

build_mappings=libfile.load_json(path_to_build_trigger_override)

#build_mappings["build"]["build"]=mappings["trigger-build"]
if "trigger-docs" in mappings:
    build_mappings["build"]["docs"]=mappings["trigger-docs"]

if "trigger-ui" in mappings:
    build_mappings["build"]["ui"]=mappings["trigger-ui"]

build_mappings["build"]["coverage"]=mappings["trigger-coverage"]
build_mappings["tests"]["smoke"]=mappings["trigger-flaky-smoke"]


print("Git Keywords:",git_keywords)
if "circleci_configuration" in What_to_build and len(What_to_build) == 1 and not build_mappings["build"]["build"]:
    #if circleci_configuration is the only entry in the list we don't want to trigger a buildss.
    mappings["trigger-build"]=False
    build_mappings["build"]["build"]=False

if "smoke" in git_keywords or "Smoke_tests" in What_to_build:   
    print(git_keywords)
    print(len(git_keywords))
    build_mappings["tests"]["smoke"]=True
    if len(git_keywords)>1:
        for i,v in enumerate(git_keywords["smoke"]):
            print(i,v)
            if v in ["flaky","core","minion"]:
                if "override" not in build_mappings["tests"]["smoke"]:
                    build_mappings["tests"]["smoke"]["override"]={}
                build_mappings["tests"]["smoke"]["override"]=v

if "oci" in git_keywords:
    build_mappings["build"]["build"]=True
    build_mappings["oci-images"]["minion"]=True
    build_mappings["oci-images"]["horizon"]=True
    build_mappings["oci-images"]["sentinel"]=True

if "rpms" in git_keywords:
    build_mappings["build"]["build"]=True
    build_mappings["rpm-packages"]["minion"]=True
    build_mappings["rpm-packages"]["horizon"]=True
    build_mappings["rpm-packages"]["sentinel"]=True
    
if "debs" in git_keywords:   
    build_mappings["build"]["build"]=True
    build_mappings["debian-packages"]["minion"]=True
    build_mappings["debian-packages"]["horizon"]=True
    build_mappings["debian-packages"]["sentinel"]=True    

if "integration" in git_keywords or "Integration_tests" in What_to_build:   
    build_mappings["build"]["build"]=True
    build_mappings["tests"]["integration"]=True

if "build" in What_to_build:
    build_mappings["build"]["build"]=True

if "doc" in git_keywords or "docs" in git_keywords or "doc" in What_to_build:
    build_mappings["build"]["docs"]=True

if "ui" in git_keywords or "ui" in What_to_build:
    build_mappings["build"]["ui"]=True

if "experimentalPath" in git_keywords:
    build_mappings["experimental"]=True
    build_mappings["build"]["build"]=False


libfile.write_file(output_path,json.dumps(mappings))

libfile.write_file(path_to_build_components,json.dumps(build_mappings,indent=4))

