import re
import subprocess
import os


def get_current_branch():
    return subprocess.check_output(['git', 'rev-parse', '--abbrev-ref', 'HEAD']).decode('utf-8').strip()


def get_parent_branch(repo_path):
    parent_branch = None
    with open(os.path.join(repo_path, ".nightly"), "r") as nightly:
        for line in nightly.readlines():
            match = re.match(r'parent_branch: (.*)$', line, re.M | re.I)
            if match:
                parent_branch = match.group(1)
    return parent_branch.strip()


def get_changed_files(repo_path):
    current_branch = get_current_branch()
    parent_branch = get_parent_branch(repo_path)
    print("Comparing: %s to: %s" % (current_branch, parent_branch))

    try:
        files_changed_in_commits = subprocess.check_output(
            ['git', 'diff', '--name-only', 'origin/' + parent_branch + '...HEAD']) \
            .decode('utf-8') \
            .split('\n')
        files_changed_in_tree = subprocess.check_output(['git', 'diff', '--name-only']) \
            .decode('utf-8') \
            .split('\n')
    except subprocess.SubprocessError as e:
        print("Some problems occurred with: ", e.cmd)
        print("Return code: ", e.returncode)
        with open("error.log", "w") as errorFile:
            errorFile.write(e.output.decode("utf-8"))
            print("Check error.log for details")
        raise e

    files_changed = set(files_changed_in_commits + files_changed_in_tree)
    return list(os.path.join(repo_path, f) for f in files_changed if f != "" and not f.isspace())
