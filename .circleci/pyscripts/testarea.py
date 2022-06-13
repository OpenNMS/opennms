from library import tests

x=tests.tests(True)

print("Flaky testcases:")
for t in x.retrieveFlakyTests():
    print("\t",t)

print("===========")
print("Smoke testcases (without Flaky):")
for t in x.retrieveSmokeTests():
    print("\t",t)

#x.retrieveSmokeTests()
#print(x.retrieveIntegrationTests())