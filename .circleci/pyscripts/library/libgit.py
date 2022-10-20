import os
import re
import subprocess

from library import libprocess


class libgit:
    def __init__(self, log_fullpath) -> None:
        self.libprocess = libprocess.libprocess()
        self.log_fullpath = log_fullpath

    def get_last_commit(self) -> str:
        return (
            subprocess.run(["git", "log", "-1"], check=True, capture_output=True)
            .stdout.decode("utf-8")
            .strip()
        )

    def get_commit_sha(self, revision):
        return (
            subprocess.run(
                ["git", "rev-parse", revision], check=True, capture_output=True
            )
            .stdout.decode("utf-8")
            .strip()
        )

    def get_changed_files_in_commits(self, baseCommit, Commit) -> list:
        output = self.libprocess.runProcess(
            ["git", "diff", "--name-only", baseCommit, Commit],
            working_directory=os.getcwd(),
            redirectSTDERR=True,
            redirectSTDOUT=True,
            outputFile=self.log_fullpath,
        )
        return output["Output"]["stdout"].splitlines()

    def get_changed_files_on_fileSystem(self) -> list:
        return (
            subprocess.run(
                ["git", "diff", "--name-only"], check=True, capture_output=True
            )
            .stdout.decode("utf-8")
            .splitlines()
        )

    def extract_keywords_from_last_commit(self) -> list:
        last_commit = self.get_last_commit()
        keywords = re.findall("\!([\w]+)?(:[\w]+-?[\w]+)?", last_commit)
        keywords_dict = {}

        for e in keywords:
            key, value = e
            if key in keywords_dict:
                print("Processing:", key)
                if isinstance(keywords_dict[key], list):
                    print("Current List:", keywords_dict[key])
                    keywords_dict[key].append(
                        value.replace(":", "") if value.strip() else True
                    )
                else:
                    print("Current Dictionary:", keywords_dict[key])
                    _current = keywords_dict[key]
                    keywords_dict[key] = []
                    keywords_dict[key].append(_current)
                    keywords_dict[key].append(
                        value.replace(":", "") if value.strip() else True
                    )
            else:
                keywords_dict[key] = value.replace(":", "") if value.strip() else True

        return keywords_dict

    def switch_branch(self, branch=""):
        if os.path.exists("opennms"):
            os.chdir("opennms")
        if not branch:
            cmd = ["git", "branch"]
        else:
            cmd = ["git", "checkout", branch.replace("'", "")]

        output = self.libprocess.runProcess(
            cmd,
            working_directory=os.getcwd(),
            redirectSTDERR=True,
            redirectSTDOUT=True,
            outputFile=self.log_fullpath,
        )
        return output

    def get_source_code(self, repository="", branch=""):
        cmd = ["git", "clone"]
        if repository:
            cmd.append(repository.replace("'", ""))

        if branch:
            cmd.append("-b")
            cmd.append(branch.replace("'", ""))

        output = self.libprocess.runProcess(
            cmd,
            working_directory=os.getcwd(),
            redirectSTDERR=True,
            redirectSTDOUT=True,
            outputFile=self.log_fullpath,
        )
        return output

    def update_source_code(self):
        """Run's a simple git pull command in the current directory"""
        cmd = ["git", "pull"]

        output = self.libprocess.runProcess(
            cmd,
            working_directory=os.getcwd(),
            redirectSTDERR=True,
            redirectSTDOUT=True,
            outputFile=self.log_fullpath,
        )

        return output

    def common_ancestor(self, base_revision, head):
        cmd = ["git", "merge-base", base_revision, head]

        output = self.libprocess.runProcess(
            cmd,
            working_directory=os.getcwd(),
            redirectSTDERR=True,
            redirectSTDOUT=True,
            outputFile="stdout",
        )
        return output["Output"]["stdout"].strip()
