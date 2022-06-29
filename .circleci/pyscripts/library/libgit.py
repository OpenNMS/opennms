import subprocess
import re

class libgit:
    def __init__(self) -> None:
        pass

    def getLastCommit(self)->str:
        return subprocess.run(['git','log','-1'],check=True,capture_output=True).stdout.decode('utf-8').strip()

    def getChangedFilesInCommits(self,baseCommit,Commit)->list:
        return subprocess.run(
            ['git', 'diff', '--name-only', baseCommit, Commit],
            check=True,
            capture_output=True
            ).stdout.decode('utf-8').splitlines()

    def getChangedFilesOnFileSystem(self)->list:
        return subprocess.run(
            ['git', 'diff', '--name-only'],
            check=True,
            capture_output=True
            ).stdout.decode('utf-8').splitlines()

    def extractKeywordsFromLastCommit(self)->list:
        _lastCommit=self.getLastCommit()
        return re.findall("\#[\w+]+((?:)?:?-?\w+)+",_lastCommit)
