from library import tests
import json 
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

_data["pytest"]="worked"

with open("/tmp/pipeline-parameters.json","w") as f:
    json.dump(_data,f)

#x.retrieveSmokeTests()
#print(x.retrieveIntegrationTests())