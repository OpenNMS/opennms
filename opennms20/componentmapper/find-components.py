#/usr/bin/env python3
import argparse
import os
import sys

# from git import get_current_branch, get_parent_branch, get_changed_files
from maven import MavenProject

csvDir = "target"
csvFilename = "components.csv"

def load_maven_project(maven_project_root):
    if not os.path.isfile(os.path.join(maven_project_root, "pom.xml")):
        raise Exception("Invalid Maven project root: " + maven_project_root)
    os.chdir(maven_project_root)

    structure_graph_file = 'target/structure-graph.json'
    if not os.path.isfile(structure_graph_file):
        raise Exception("Cannot find structure graph at: " + structure_graph_file)

    return MavenProject.load(structure_graph_file)


def apply_component_inheritance(maven_module, maven_project):
    dir = os.path.relpath(maven_module.path, maven_project._root_module.path)

    while (maven_module.componentName == '' or maven_module.subcomponentName == '') and dir != '.':
      # Look for a 'parent' module one dir up the path
      dir = os.path.abspath(os.path.join(dir, '..'))
      dir = os.path.relpath(dir, maven_project._root_module.path)
      # print("Checking  " + dir)
      parent = maven_project.modules_by_relative_path[dir]
      if maven_module.componentName == '' and parent.componentName != '':
        maven_module.componentName = parent.componentName
      if maven_module.subcomponentName == '' and parent.subcomponentName != '':
        maven_module.subcomponentName = parent.subcomponentName

def generate_csv_file(project):
    if not os.path.exists(csvDir):
      os.makedirs(csvDir)
    filename = os.path.join(csvDir, csvFilename)
    with open(filename, 'w') as f:
      modules = project.modules
      for module in modules:
        print(module.componentName + ',' + module.subcomponentName + ',' + module.path, file=f)

def generate_stats(project):
    modules = project.modules
    withComponent = 0
    withSub = 0
    total = 0
    for module in modules:
      # Ignore the root module
      if not module._is_root:
        total += 1
        if module.componentName != '':
          withComponent += 1
        if module.subcomponentName != '':
          withSub += 1

    print("Total: " + str(total) + ", With Component Name: " + str(withComponent) + \
    ', with Subcomponent Name: ' + str(withSub))


def generate_components(maven_project_root): 
    project = load_maven_project(maven_project_root)

    modules = project.modules

    # dirs = project.modules_by_relative_path.keys()
    for module in modules:
      apply_component_inheritance(module, project)

    generate_csv_file(project)
    generate_stats(project)



parser = argparse.ArgumentParser(prog='find-tests.py')
subparsers = parser.add_subparsers(help='sub-command help', dest='cmd')

parser_a = subparsers.add_parser('generate-components', help='generate-components help')
parser_a.add_argument("maven_project_root", type=str, help="path to Maven project root")

args = parser.parse_args()
generate_components(args.maven_project_root)
