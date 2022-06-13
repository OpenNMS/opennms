from library import tests
import json 
import subprocess

x=tests.tests(True)

print("Flaky testcases:")
for t in x.retrieveFlakyTests():
    print("\t",t)

print("===========")
print("Smoke testcases (without Flaky):")
for t in x.retrieveSmokeTests():
    print("\t",t)

_data={}
with open("/tmp/pipeline-parameters.json","r") as f:
    _data=json.load(f)

#Lets see if we can detect "#flak-tests"
lastestCommit= subprocess.run(['git','log','-1'],check=True,capture_output=True).stdout.decode('utf-8').strip()
print("Commit message ")
print(lastestCommit)

if "#tests-flak" in lastestCommit:
    _data["trigger-flaky-tests"]=True
print("=== End of commit message")


with open("/tmp/pipeline-parameters.json","w") as f:
    json.dump(_data,f)

#x.retrieveSmokeTests()
#print(x.retrieveIntegrationTests())