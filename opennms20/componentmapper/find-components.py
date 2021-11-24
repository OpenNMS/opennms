#/usr/bin/env python3
import argparse
import os
import sys

# from git import get_current_branch, get_parent_branch, get_changed_files
from maven import MavenProject

fileDir = "target"
csvFilename = "components.csv"
htmlFilename = "components.html"

def load_maven_project(maven_project_root):
    if not os.path.isfile(os.path.join(maven_project_root, "pom.xml")):
        raise Exception("Invalid Maven project root: " + maven_project_root)
    os.chdir(maven_project_root)

    structure_graph_file = 'target/structure-graph.json'
    if not os.path.isfile(structure_graph_file):
        raise Exception("Cannot find structure graph at: " + structure_graph_file)

    return MavenProject.load(structure_graph_file)

def is_module_missing_component_names(module):
    return (module.componentName == '' or module.subcomponentName == '' \
            or module.stability == '')

def apply_component_inheritance(maven_module, maven_project):
    dir = os.path.relpath(maven_module.path, maven_project._root_module.path)

    while is_module_missing_component_names(maven_module) and dir != '.':
      # Look for a 'parent' module one dir up the path
      dir = os.path.abspath(os.path.join(dir, '..'))
      dir = os.path.relpath(dir, maven_project._root_module.path)
      # print("Checking  " + dir)
      parent = maven_project.modules_by_relative_path[dir]
      if maven_module.componentName == '' and parent.componentName != '':
        maven_module.componentName = parent.componentName
      if maven_module.subcomponentName == '' and parent.subcomponentName != '':
        maven_module.subcomponentName = parent.subcomponentName
      if maven_module.stability == '' and parent.stability != '':
        maven_module.stability = parent.stability

def generate_csv_file(project):
    if not os.path.exists(fileDir):
      os.makedirs(fileDir)
    filename = os.path.join(fileDir, csvFilename)
    with open(filename, 'w') as f:
      print('Path,Group,Artifact,Component,Subcomponent,Stability', file=f)
      modules = project.modules
      for module in modules:
        print(module.path + ',' + module.group_id + ',' + module.artifact_id + ',' + module.componentName + \
              ',' + module.subcomponentName + ',' + module.stability, file=f)

def addModuleToTree(top, module, missing):
    if not module._is_root:
      if module.componentName != '':
        subcomponentList = top.get(module.componentName)
        if subcomponentList == None:
          subcomponentList = dict()
          top[module.componentName] = subcomponentList

        subname = module.subcomponentName
        if subname == '':
          subname='none'

        moduleList = subcomponentList.get(subname)
        if moduleList == None:
          moduleList = dict()
          moduleList[subname] = dict()
          subcomponentList[subname] = moduleList

        moduleList[module.artifact_id] = module
      else:
        missing.append(module)

def generateComponentTree(project, missingModules):
    top = dict()
    for module in project.modules:
      addModuleToTree(top, module, missingModules)
    return top

def addHtmlHeader(f):
    header="""<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<style>
.collapsible {
  cursor: pointer;
  border: none;
  text-align: left;
  outline: none;
  background-color: #FFF;
}

.collapsible:after {
  content: '\\02795'; /* Unicode character for "plus" sign (+) */
  font-size:8px;
  float: left;
}

.active:after {
  content: "\\2796"; /* Unicode character for "minus" sign (-) */
  font-size:8px;
  float: left;
}

.content {
  padding: 0 18px;
  display: none;
  overflow: hidden;
}
</style>
</head>
<body>"""
    print(header, file=f)

def add_html_footer(f):
    footer="""<script>
var coll = document.getElementsByClassName("collapsible");
var i;

for (i = 0; i < coll.length; i++) {
  coll[i].addEventListener("click", function() {
    this.classList.toggle("active");
    var content = this.nextElementSibling;
    if (content.style.display === "block") {
      content.style.display = "none";
    } else {
      content.style.display = "block";
    }
  });

  coll[i].click();
  if (coll[i].textContent.includes("Subcomponent:")) {
    coll[i].click();
  }
}
</script>

</body>
</html>"""
    print(footer, file=f)
def generate_html_file(project):
    missing = []
    tree = generateComponentTree(project, missing)
    filename = os.path.join(fileDir, htmlFilename)
    with open(filename, 'w') as f:
      addHtmlHeader(f)
      print('<h1>OpenNMS Components</h1>', file=f)
      if missing:
        generate_stats(project, f)
      components = tree.keys()
      print('<h2>Component Tree</h2>', file=f)
      print('<OL>', file=f)
      for component in components:
        print('<LI><button type="button" class="collapsible">Component:' + component + '</button>', file=f)
        subcomponents = tree.get(component)
        if subcomponents:
            print('<UL>', file=f)
            for subcomponent in subcomponents.keys():
              print('<LI><button type="button" class="collapsible">Subcomponent: ' + subcomponent + '</button>', file=f)
              modules = subcomponents.get(subcomponent).keys()
              if modules:
                  print("<UL>", file=f)
                  for module in modules:
                    print("<LI>Module: " + module, file=f)
                  print("</UL>", file=f)
            print("</UL>", file=f)
      print('</OL>', file=f)

      if missing:
        # Save to html and output directly
        print('<ht>', file=f)
        print('<h2>Modules Missing Component Info</h2>', file=f)
        print('<UL>', file=f)

        # Total project modules includes root module - exclude it from total
        print('The following Maven modules (' + str(len(missing)) + ' out of ' + str(len(project.modules)-1) + ') are missing tags:')
        for module in missing:
          print('<LI>' + module.relative_path + '/pom.xml ' + module.group_id + ":" + module.artifact_id, file=f)
          print(module.relative_path + '/pom.xml ' + module.group_id + ":" + module.artifact_id)
        print('</UL>', file=f)
        print('')
        print('See https://github.com/OpenNMS/opennms/blub/features/opennms20/opennms20/COMPONENTS.md')
      add_html_footer(f)
      if missing:
        return 1
    return 0




def generate_stats(project, f):
    modules = project.modules
    withComponent = 0
    withSub = 0
    withStability = 0
    total = 0
    for module in modules:
      # Ignore the root module
      if not module._is_root:
        total += 1
        if module.componentName != '':
          withComponent += 1
        if module.subcomponentName != '':
          withSub += 1
        if module.stability != '':
          withStability += 1

    print("<p>Total: " + str(total) + ", With Component Name: " + str(withComponent) + \
    ', with Subcomponent Name: ' + str(withSub) + ", with stability: " + str(withStability) + "</p>", file=f)


def generate_components(maven_project_root): 
    project = load_maven_project(maven_project_root)

    modules = project.modules

    # dirs = project.modules_by_relative_path.keys()
    for module in modules:
      apply_component_inheritance(module, project)

    generate_csv_file(project)
    exit_code = generate_html_file(project)
    sys.exit(exit_code)



parser = argparse.ArgumentParser(prog='find-tests.py')
subparsers = parser.add_subparsers(help='sub-command help', dest='cmd')

parser_a = subparsers.add_parser('generate-components', help='generate-components help')
parser_a.add_argument("maven_project_root", type=str, help="path to Maven project root")

args = parser.parse_args()
generate_components(args.maven_project_root)
