#! /bin/bash
# Run from base of repo

BASEDIR=opennms
MAPPER=$BASEDIR/opennms-tools/componentmapper/runmapper.sh

# Use base if no dir passed in
if [ $# -eq 0 ]; then
  DIR="opennms"
  # Initialize the CSV file
  echo "Location, Component, Subcomponent" > /tmp/components.csv
  cd ..
else
  DIR="$1"
fi
POM=0

# First, look for a pom:
if [[ -f "$DIR/pom.xml" ]]; then
  echo "Found pom in $DIR"
  POM=1
  # Now look for our component properties
  COMPONENT=`xmllint --xpath '/*[local-name()="project"]/*[local-name()="properties"]/*[local-name()="opennms.doc.component"]/text()' $DIR/pom.xml 2>/dev/null`
  SUBCOMPONENT=`xmllint --xpath '/*[local-name()="project"]/*[local-name()="properties"]/*[local-name()="opennms.doc.subcomponent"]/text()' $DIR/pom.xml 2>/dev/null`

fi

if [ -z "$COMPONENT" ]; then
  # Empty component, try using arg
  COMPONENT="$2"
fi
if [ -z "$SUBCOMPONENT" ]; then
  # Empty subcomponent, try using arg
  SUBCOMPONENT="$3"
fi

if [ "$POM" -eq 1 ]; then
  echo "$DIR, $COMPONENT, $SUBCOMPONENT" >> /tmp/components.csv
fi

# Next, find sub directories
find "$DIR" -maxdepth 1 -mindepth 1 -type d -exec $MAPPER {} "$COMPONENT" "$SUBCOMPONENT" \;

