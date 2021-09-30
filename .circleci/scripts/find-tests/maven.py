import glob
import json
import os
import re
from pathlib import Path


class MavenIdentifier(object):
    def __init__(self, json_obj):
        self.artifact_id = json_obj['artifactId']
        self.group_id = json_obj['groupId']
        self.version = json_obj['version']
        self.key = "%s:%s:%s" % (self.group_id, self.artifact_id, self.version)

    def matches(self, module):
        return self.artifact_id == module.artifact_id \
               and self.group_id == module.group_id \
               and self.version == module.version


class JUnitTest(object):
    def __init__(self, module, file, is_integration_test=False):
        self.module = module
        self.file = file
        self.classname = JUnitTest.classname_from_filename(file)
        self.is_integration_test = is_integration_test

    def __str__(self):
        return self.file

    @staticmethod
    def classname_from_filename(filename):
        # Example filename: "opennms-services/src/test/java/org/opennms/netmgt/poller/PollerIT.java"
        # Target classname: "org.opennms.netmgt.poller.PollerIT"
        # We want to take the last part after src/*/java/(*.)\. and replace the forward slashes with dots
        match = re.search(r".*[\/\\]src[\/\\](main|test)[\/\\]java[\/\\](.*).java", filename)
        if match is None:
            raise Exception("Failed to match: " + filename)
        return re.sub(r'[\/\\]', '.', match.group(2))


class MavenModule(MavenIdentifier):
    def __init__(self, json_obj, is_root=False):
        super().__init__(json_obj)
        self.pom = json_obj['pom']
        self.path = os.path.abspath(os.path.join(self.pom, '..'))
        self.dependencies = [MavenIdentifier(obj) for obj in json_obj['dependencies']]
        self._is_root = is_root

    def __str__(self):
        return self.key

    def is_root(self):
        return self._is_root

    def depends_on(self, module):
        """ Returns True if this module depends on the given module, False otherwise
            We do not consider transitive, only direct dependencies when performing this check.
        """
        for dep in self.dependencies:
            if dep.matches(module):
                return True
        return False

    def has_tests(self):
        """ Returns True is this module contains one or more tests, False otherwise
            We search for files ending in *Test.java or *IT.java
        """
        return len(self.find_tests()) > 0

    def find_tests(self):
        tests = []
        for test_file in glob.iglob(self.path + '/src/**/test/java/**/*Test.java', recursive=True):
            tests.append(JUnitTest(self, test_file))
        for test_file in glob.iglob(self.path + '/src/**/test/java/**/*IT.java', recursive=True):
            tests.append(JUnitTest(self, test_file, is_integration_test=True))
        return tests


class MavenProject(object):
    def __init__(self, graph):
        self.modules = []
        self.modules_by_key = {}
        self.modules_by_relative_path = {}
        self.modules_by_dependencies = {}

        if len(graph) < 1:
            raise Exception("Graph must contain at least one module!")

        # Assume the first module is the root module
        is_root = True
        for module in graph:
            m = MavenModule(module, is_root=is_root)
            # Append to list
            self.modules.append(m)
            # Index by module key
            self.modules_by_key[m.key] = m
            # Only the first is the root
            if is_root:
                self._root_module = m
                is_root = False

        # Index the modules by relative paths
        for module in self.modules:
            # Determine the path of the module relative to the root module
            relative_path = os.path.relpath(module.path, self._root_module.path)
            self.modules_by_relative_path[relative_path] = module

        # Index the modules by their dependencies
        for module in self.modules:
            for dependency in module.dependencies:
                modules_with_dep = self.modules_by_dependencies.get(dependency.key)
                if modules_with_dep is None:
                    # Create a new set
                    modules_with_dep = set()
                    self.modules_by_dependencies[dependency.key] = modules_with_dep
                modules_with_dep.add(module)

    @staticmethod
    def load(json_file):
        """ Create a new MavenProject from the JSON in the given file """
        with open(json_file) as graph_json:
            graph = json.load(graph_json)
            return MavenProject(graph)

    def get_module(self, module_key):
        """ module_key takes the format groupId:artifactId:version """
        return self.modules_by_key.get(module_key)

    def get_root_module(self):
        return self._root_module

    def get_modules_related_to(self, files_changed):
        modules_with_changes = set()

        for file in files_changed:
            # Determine the path of the file relative to the project root
            path_in_project = os.path.relpath(file, self.get_root_module().path)
            # Now split the path into components (or parts)
            path_parts = Path(path_in_project).parts

            # Cycle back through the path components to try and find the module that owns the file
            module = None
            for k in reversed(range(1, len(path_parts) + 1)):
                parts_to_use = path_parts[:k]
                path = os.path.join(*parts_to_use)
                module = self.modules_by_relative_path.get(path)
                if module is not None:
                    modules_with_changes.add(module)
                    break

            if module is None:
                # No module matched
                print("No module match for " + file)

        return modules_with_changes

    def _get_module_users(self, dependencies, users):
        all_dep_users = set()
        # Gather all the users for the given set of modules
        for dependency in dependencies:
            dep_users = self.modules_by_dependencies.get(dependency.key)
            if dep_users is not None:
                all_dep_users.update(dep_users)

        # Now filter out the modules that we already know we need
        new_dep_users = list([m for m in all_dep_users if not (m in users)])

        # If we have no new users, we're done
        if len(new_dep_users) < 1:
            return

        # Add the new users to the tracking set
        users.update(new_dep_users)

        # Recurse
        self._get_module_users(new_dep_users, users)

    def get_module_users(self, dependencies):
        all_users = set()
        self._get_module_users(dependencies, all_users)
        return all_users

    def get_modules_for_classnames(self, classnames):
        modules_with_classes = set()
        for module in self.modules:
            for test in module.find_tests():
                if test.classname in classnames:
                    modules_with_classes.add(module)
                    continue
        return modules_with_classes
