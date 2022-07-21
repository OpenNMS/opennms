import subprocess
import re
from library import libprocess

import os

class libgit:
    def __init__(self,log_fullpath) -> None:
        self.libprocess=libprocess.libprocess()
        self.log_fullpath=log_fullpath

    def getLastCommit(self)->str:
        return subprocess.run(['git','log','-1'],check=True,capture_output=True).stdout.decode('utf-8').strip()
    
    def getCommitSHA(self,revision):
        return subprocess.run(['git','rev-parse',revision],check=True,capture_output=True).stdout.decode('utf-8').strip()

    def getChangedFilesInCommits(self,baseCommit,Commit)->list:
        print("=","Running","getChangedFilesInCommits","=")
        output=self.libprocess.runProcess(['git', 'diff', '--name-only', baseCommit, Commit],working_directory=os.getcwd(),redirectSTDERR=True,redirectSTDOUT=True,outputFile=self.log_fullpath)
        self.libprocess.printSummary(output)
        print("=","Stopping","switchBranch","=")
        return output["Output"]["stdout"].splitlines()

    def getChangedFilesOnFileSystem(self)->list:
        return subprocess.run(
            ['git', 'diff', '--name-only'],
            check=True,
            capture_output=True
            ).stdout.decode('utf-8').splitlines()

    def extractKeywordsFromLastCommit(self)->list:
        last_commit=self.getLastCommit()
        # TODO: add ability to set True or False flags 
        keywords=re.findall("\#([\w]+)?(:[\w]+-?[\w]+)?",last_commit)
        keywords_dict={}

        for e in keywords:
            key,value=e
            if key in keywords_dict:
                _current=keywords_dict[key]
                keywords_dict[key]=_current
                keywords_dict[key].append(value.replace(":","") if value.strip() else True)
            else:
                keywords_dict[key]=value.replace(":","") if value.strip() else True
 
        return keywords_dict

    def switchBranch(self,branch=""):
        if os.path.exists("opennms"):
            os.chdir("opennms")
        if not branch:
            cmd=['git','branch']
        else:
            cmd=['git','checkout',branch.replace("'","")]

        print("=","Running","switchBranch","=")
        output=self.libprocess.runProcess(cmd,working_directory=os.getcwd(),redirectSTDERR=True,redirectSTDOUT=True,outputFile=self.log_fullpath)
        self.libprocess.printSummary(output)
        print("=","Stopping","switchBranch","=")
        return output

    def getSourceCode(self,repository="",branch=""):
        cmd=['git','clone']
        if repository:
            cmd.append(repository.replace("'",""))

        if branch:
            cmd.append('-b')
            cmd.append(branch.replace("'",""))

        print("=","Running","getSourceCode","=")
        output=self.libprocess.runProcess(cmd,working_directory=os.getcwd(),redirectSTDERR=True,redirectSTDOUT=True,outputFile=self.log_fullpath)
        self.libprocess.printSummary(output)
        print("=","Stopping","getSourceCode","=")
        return output

    def updateSourceCode(self):
        """ Run's a simple git pull command in the current directory """
        cmd=['git','pull']

        print("=","Running","updateSourceCode","=")
        output=self.libprocess.runProcess(cmd,working_directory=os.getcwd(),redirectSTDERR=True,redirectSTDOUT=True,outputFile=self.log_fullpath)
        self.libprocess.printSummary(output)
        print("=","Stopping","updateSourceCode","=")

        return output

    def commonAncestor(self,base_revision,head):
        cmd=['git', 'merge-base', base_revision, head]

        print("=","Running","commonAncestor","=")
        output = self.libprocess.runProcess(cmd,working_directory=os.getcwd(),redirectSTDERR=True,redirectSTDOUT=True,outputFile="stdout")
        self.libprocess.printSummary(output)
        print("=","Stopping","commonAncestor","=")
        return output["Output"]["stdout"].strip()