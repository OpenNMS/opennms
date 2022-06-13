from library import tests,libgit
import json 
import subprocess

x=tests.tests(True)
_git=libgit.libgit()

print("Flaky testcases:")
for t in x.retrieveFlakyTests():
    print("\t",t)

print("===========")
print("Smoke testcases (without Flaky):")
for t in x.retrieveSmokeTests():
    print("\t",t)

print(">>> GIT Testing Area <<<")
lastestCommit= _git.getLastCommit()
print(lastestCommit)
print(_git.getChangedFilesInCommits("HEAD","HEAD~"))
print(_git.getChangedFilesOnFileSystem())
print(_git.extractKeywordsFromLastCommit())
print(">>>END GIT Testing Area <<<")

_data={}
with open("/tmp/pipeline-parameters.json","r") as f:
    _data=json.load(f)

#Lets see if we can detect "#flak-tests"
print("Commit message ")
print(lastestCommit)

if "#tests-flak" in lastestCommit:
    print("Detected tests-flak keyword")
    _data["trigger-flaky-tests"]=True
print("=== End of commit message")


with open("/tmp/pipeline-parameters.json","w") as f:
    json.dump(_data,f)

#x.retrieveSmokeTests()
#print(x.retrieveIntegrationTests())