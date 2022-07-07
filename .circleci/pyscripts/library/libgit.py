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
        last_commit=self.getLastCommit()
        keywords=re.findall("\#([\w]+)?(:[\w]+(-?[\w]+)+)?",last_commit)
        keywords_dict={}
        #Exp: not a good idea
        if len(keywords[0]) >2:
            keywords=keywords[0][0:2]

        for e in keywords:
            key,value=e
            keywords_dict[key]=value.replace(":","") if value.strip() else True
 
        return keywords_dict
