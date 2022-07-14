#/usr/bin/env python3
import os
import re
import json
from library import libgit

output_path = os.environ.get('OUTPUT_PATH')
head = os.environ.get('CIRCLE_SHA1')
base_revision = os.environ.get('BASE_REVISION')

libgit = libgit.libgit("stdout")

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

print(mappings)