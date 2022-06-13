from distutils.log import debug
import subprocess

class tests:
    _debug=False

    
    def __init__(self,debug=False) -> None:
        self._debug=debug

    def _print(self,msg) -> None:
        if self._debug:
            print(msg)

    def _run(self,cmd) -> list:
        return subprocess.run(
            cmd,
            check=True,
            capture_output=True
        ).stdout.decode('utf-8').strip().split()

    def retrieveFlakyTests(self) -> list:
        flakySmokeTests=self._run(['grep', '-rlm','1', '@org.junit.experimental.categories.Category(org.opennms.smoketest.junit.FlakyTests.class)','./smoke-test'])
               
        if len(flakySmokeTests) >0:
            self._print("Flaky tests ... Detected ("+str(len(flakySmokeTests))+")")
        else:
            self._print("Flaky tests ... Not detected")
        return flakySmokeTests

    def retrieveSmokeTests(self) -> list:
        _flakyTests=self.retrieveFlakyTests()
        SmokeTests = self._run(['grep', '-rlm','1', '@Test','./smoke-test'])

        if len(SmokeTests) >0:
            self._print("Smoke tests ... Detected ("+str(len(SmokeTests))+")")
            self._print("retrieveSmokeTests :: Testcase count before filtering flaky tests: "+str(len(SmokeTests)))
            SmokeTests=set(SmokeTests)-set(_flakyTests)
            self._print("retrieveSmokeTests :: Testcase count after filtering flaky tests: "+str(len(SmokeTests)))
        else:
            self._print("Smoke tests ... Not detected")
        return SmokeTests
