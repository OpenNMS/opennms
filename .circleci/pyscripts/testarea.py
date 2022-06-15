from library import tests,libgit
import json 

#Tests keyword
tests_trigger_keywords={
    "trigger-flaky-tests": ["smoke-flaky","tests-flaky","test-flaky"],
    "trigger-smoke-tests": ["smoke","tests-smoke","test-smoke"],
    "trigger-integration-tests": ["smoke-integration","tests-integration","test-integration"],
    "trigger-smin-tests": ["smoke-smin","tests-smin"],
    "trigger-all-tests" : ["test-all","tests-all"]
}

trigger_keywords=tests_trigger_keywords
triggers_enabled={}


x=tests.tests()
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
git_keywords=_git.extractKeywordsFromLastCommit()
print(lastestCommit)
print("Changed Files in the commit:",_git.getChangedFilesInCommits("HEAD","HEAD~"))
print("Changed Files on system:",_git.getChangedFilesOnFileSystem())
print("Detected Keywords:",git_keywords)
print(">>>END GIT Testing Area <<<")

print("Initial Triggers Enabled",triggers_enabled)
for key in trigger_keywords.keys():
    for git_key in git_keywords:
        if git_key.replace("#","") in trigger_keywords[key]:
            triggers_enabled[key]=True

#Clean up
# If we have enabled all tests, then lets delete other tiggers
print("[Before - Clean Up] Triggers Enabled",triggers_enabled)
if "trigger-all-tests" in triggers_enabled:
    for test_triggers in tests_trigger_keywords.keys():
        if test_triggers in triggers_enabled and test_triggers != "trigger-all-tests":
            del triggers_enabled[test_triggers]

print("[After - Clean Up] Triggers Enabled",triggers_enabled)

_data={}
with open("/tmp/pipeline-parameters.json","r") as f:
    _data=json.load(f)


#Lets see if we can detect "#flak-tests"
#if "#tests-flak" in lastestCommit:
#    print("Detected tests-flak keyword")
#    _data["trigger-flaky-tests"]=True
#print("=== End of commit message")
for trigger in triggers_enabled.keys():
    _data[trigger] = triggers_enabled[trigger]

with open("/tmp/pipeline-parameters.json","w") as f:
    json.dump(_data,f)

#x.retrieveSmokeTests()
#print(x.retrieveIntegrationTests())