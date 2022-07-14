#/usr/bin/env python3
import os
from library import libgit

output_path = os.environ.get('OUTPUT_PATH')
head = os.environ.get('CIRCLE_SHA1')
base_revision = os.environ.get('BASE_REVISION')

libgit = libgit.libgit("stdout")

libgit.switchBranch(base_revision)
libgit.switchBranch(head)

base= libgit.commonAncestor(base_revision, head)

# Why do I need to do this??????
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
