#!/usr/bin/env python3
import xml.etree.ElementTree as ET
import argparse

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Get version number from a given POM file.")
    parser.add_argument("path", help="Relative or absolute path to pom.xml file to read the version from.")
    args = parser.parse_args()
    version = ET.parse(open(args.path)).getroot().find('{http://maven.apache.org/POM/4.0.0}version').text

print(version)
