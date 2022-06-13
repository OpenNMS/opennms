from distutils.log import debug
import subprocess
import glob 
import os 

class tests:
    _debug=False
    _testcases=[]
    _testcases_filename=["Test","IT"]
    _flakyTests=[]
    _smokeTests=[]
    _integrationTests=[]

    
    def __init__(self,debug=False) -> None:
        self._debug=debug
        for e in self._testcases_filename:
            self._testcases.extend(self._scanFolders('**/test/java/**/*'+e+'.java'))

        self.retrieveFlakyTests()
        self.retrieveIntegrationTests()
        self.retrieveSmokeTests()
    

    def _print(self,msg) -> None:
        if self._debug:
            print(msg)

    def _run(self,cmd) -> list:
        return subprocess.run(
            cmd,
            check=True,
            capture_output=True
        ).stdout.decode('utf-8').strip().split()

    def _scanFolders(self,filter)-> list:
        _output=[]
        for file in glob.iglob(filter,recursive=True):
            if ".java" in os.path.splitext(file)[1]:
                _output.append(file)
        return _output

    def retrieveFlakyTests(self,force=False) -> list:
        #flakySmokeTests=self._run(['grep', '-rlm','1', '@org.junit.experimental.categories.Category(org.opennms.smoketest.junit.FlakyTests.class)',e])
        if self._flakyTests and not force:
            return self._flakyTests

        _flakySmokeTests=[]
        for e in self._testcases:
            try:
                _flakySmokeTests.extend(self._run(['grep', '-l', '@org.junit.experimental.categories.Category(org.opennms.smoketest.junit.FlakyTests.class)',e]))
            except:
                pass 
        self._flakyTests=_flakySmokeTests

        return _flakySmokeTests

    def retrieveIntegrationTests(self,force=False) -> list:
        _tests=[]

        if self._integrationTests and not force:
            return self._integrationTests

        for e in _tests:
            if "IT.java" in e:
                _tests.append(e)

        #_it=self._scanFolders('**/test/java/**/*IT.java')
        self._integrationTests=_tests
        return _tests

    def retrieveSmokeTests(self,force=False) -> list:
        _tests=[]

        if self._smokeTests and not force:
            return self._smokeTests

        _flakytests=self.retrieveFlakyTests()
        _integrationtests=self.retrieveIntegrationTests()

        for e in self._testcases:
            if e in _flakytests or e in _integrationtests:
                continue
            else:
                if "Test.java" in e:
                    _tests.append(e)
        #_it=self._scanFolders('**/test/java/**/*Test.java')

        if len(_tests) >0:
            self._print("Smoke tests ... Detected ("+str(len(_tests))+")")
            self._print("retrieveSmokeTests :: Testcase count before filtering flaky tests: "+str(len(self._testcases)))
            _tests1=set(_tests)-set(_flakytests)
            self._print("retrieveSmokeTests :: Testcase count after filtering flaky tests: "+str(len(_tests)))
        else:
            self._print("Smoke tests ... Not detected")
        self._smokeTests=list(_tests1)
        return _tests1
