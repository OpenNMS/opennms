#!/usr/bin/env python3
import glob
import os
import sys
import xml.etree.ElementTree as ET
import argparse

def does_module_have_any_tests(module_path):
    return any(True for _ in glob.iglob(module_path + '/src/**/*Test.java', recursive=True)) \
           or any(True for _ in glob.iglob(module_path + '/src/**/*IT.java', recursive=True))

def should_skip_module_in(pom):
    if 'opennms-tools/' in pom:
        return True
    if 'smoke-test-v2/' in pom:
        return True
    if 'smoke-test/' in pom:
        return True
    if 'opennms-assemblies/' in pom:
        return True
    if 'opennms-full-assembly/' in pom:
        return True
    # In tree, but not linked to root pom
    if 'remote-poller-18/' in pom:
        return True
    if 'org.opennms.features.topology.plugins.ssh/' in pom:
        return True
    if 'opennms-install/' in pom:
        return True
    return False

def do(basepath):
    poms = []
    for pom in glob.iglob(basepath + '/**/pom.xml', recursive=True):
        if should_skip_module_in(pom):
            continue
        path = os.path.dirname(pom)
        poms.append((pom, path, does_module_have_any_tests(path)))
    return poms

def do_rec(basepath, poms):
    for pom in glob.iglob(basepath + '/**/pom.xml'):
        has_subpom = False
        path = os.path.dirname(pom)
        for subpom in glob.iglob(path + '/**/pom.xml'):
            subpath = os.path.dirname(subpom)
            do_rec(subpath, poms)
            has_subpom = True
        if not has_subpom:
            poms.append(pom)


def find_test_projects_in(path):
    poms = do(path)
    poms_with_tests = [os.path.relpath(pom[0], path) for pom in poms if pom[2]]
    poms_with_tests.sort()
    for pom_with_test in poms_with_tests:
        ns = {'ns': 'http://maven.apache.org/POM/4.0.0'}
        root_el = ET.parse(open(pom_with_test)).getroot()
        groupId_el = root_el.find('ns:groupId', namespaces=ns)
        if groupId_el is None:
            groupId_el = root_el.find('ns:parent/ns:groupId', namespaces=ns)
        groupId = groupId_el.text
        artifactId = root_el.find('ns:artifactId', namespaces=ns).text
        print("%s:%s" % (groupId, artifactId))

def find_test_classes_in(path):
    classes = []
    for src_file in glob.iglob(path + '/src/test/**/*.java', recursive=True):
        classes.append(os.path.splitext(os.path.basename(src_file))[0])
    classes.sort()
    for clazz in classes:
        print(clazz)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Generate a list of tests')
    parser.add_argument('path')
    parser.add_argument('-c', '--use-class-names', dest='use_class_names', action='store_true')
    args = parser.parse_args()

    if args.use_class_names:
        find_test_classes_in(args.path)
    else:
        find_test_projects_in(args.path)

