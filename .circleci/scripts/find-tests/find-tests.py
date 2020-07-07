#/usr/bin/env python3
import argparse
import os
import sys

from git import get_current_branch, get_parent_branch, get_changed_files
from maven import MavenProject


def load_maven_project(maven_project_root):
    if not os.path.isfile(os.path.join(maven_project_root, "pom.xml")):
        raise Exception("Invalid Maven project root: " + maven_project_root)
    os.chdir(maven_project_root)

    structure_graph_file = os.path.join(maven_project_root, 'target/structure-graph.json')
    if not os.path.isfile(structure_graph_file):
        raise Exception("Cannot find structure graph at: " + structure_graph_file)

    return MavenProject.load(structure_graph_file)


def find_changes(maven_project_root):
    project = load_maven_project(maven_project_root)
    print("Maven project contains %d modules." % len(project.modules))

    files_changed = get_changed_files(maven_project_root)
    print("Files with changes:")
    for file_changed in files_changed:
        print(file_changed)


def generate_test_lists(maven_project_root, changes_only=True, unit_test_output=None, integration_test_output=None):
    project = load_maven_project(maven_project_root)
    print("Maven project contains %d modules." % len(project.modules))

    current_branch = get_current_branch()
    print("Current branch: %s" % current_branch)
    parent_branch = get_parent_branch(maven_project_root)
    print("Parent branch: %s" % parent_branch)

    if current_branch != parent_branch and changes_only:
        files_changed = get_changed_files(maven_project_root)
        print("Files with changes:")
        for file_changed in files_changed:
            print(file_changed)

        modules_with_changes = project.get_modules_related_to(files_changed)
        print("Modules with changes:")
        for module_with_changes in modules_with_changes:
            print(module_with_changes)

        module_users = project.get_module_users(modules_with_changes)
        modules_to_consider = set(module_users).union(set(modules_with_changes))
    else:
        # Consider all modules
        modules_to_consider = project.modules

    print("Modules to consider:")
    for module_to_consider in modules_to_consider:
        print(module_to_consider)

    # Open our file handles
    unit_file = None
    if unit_test_output is not None:
        unit_file = open(unit_test_output, 'w')

    itest_file = None
    if integration_test_output is not None:
        itest_file = open(integration_test_output, 'w')

    print("Modules with tests:")
    for m in modules_to_consider:
        if m.has_tests():
            print(m)
            for test in m.find_tests():
                print("\t%s - %s (%s)" % (test.file, test.classname, "Failsafe" if test.is_integration_test else "Surefire"))

                if not test.is_integration_test:
                    if unit_file:
                        unit_file.write("%s\n" % test.classname)
                else:
                    if itest_file:
                        itest_file.write("%s\n" % test.classname)


def generate_test_modules(maven_project_root, test_class_names, output_file):
    project = load_maven_project(maven_project_root)
    print("Maven project contains %d modules." % len(project.modules))

    modules_for_tests = project.get_modules_for_classnames(test_class_names)
    print("Modules for tests:")
    for module_for_tests in modules_for_tests:
        print(module_for_tests)
        output_file.write("%s:%s\n" % (module_for_tests.group_id, module_for_tests.artifact_id))


parser = argparse.ArgumentParser(prog='find-tests.py')
subparsers = parser.add_subparsers(help='sub-command help', dest='cmd')

# create the parser for the "generate-test-lists" command
parser_a = subparsers.add_parser('generate-test-lists', help='generate-test-lists help')
parser_a.add_argument("--changes-only", dest="changes_only", default="true", type=lambda s: s.lower() in ['true', 't', 'yes', '1'],
                    help="only consider changed files")
parser_a.add_argument("--output-unit-test-classes", type=str, dest="unit_test_output",
                    help="target file in which to output the list of unit test classes")
parser_a.add_argument("--output-integration-test-classes", type=str, dest="integration_test_output",
                    help="target file in which to output the list of integration test classes")
parser_a.add_argument("maven_project_root", type=str, help="path to Maven project root")

# create the parser for the "generate-test-modules" command
parser_b = subparsers.add_parser('generate-test-modules', help='generate-test-modules help')
parser_b.add_argument("maven_project_root", type=str, help="path to Maven project root")
parser_b.add_argument('infile', default=sys.stdin, type=argparse.FileType('r'), nargs='?')
parser_b.add_argument('--output', type=str, dest="output", required=True,
                      help="target file in which to output the list of modules")

# create the parser for the "find-changes" command
parser_c = subparsers.add_parser('find-changes', help='find-changes help')
parser_c.add_argument("maven_project_root", type=str, help="path to Maven project root")

args = parser.parse_args()
if args.cmd == 'generate-test-lists':
    generate_test_lists(args.maven_project_root, changes_only=args.changes_only,
                        unit_test_output=args.unit_test_output,
                        integration_test_output=args.integration_test_output)
elif args.cmd == 'generate-test-modules':
    test_names = args.infile.read()
    with open(args.output, 'w') as target_file:
        generate_test_modules(args.maven_project_root, test_names, target_file)
elif args.cmd == 'find-changes':
    find_changes(args.maven_project_root)

