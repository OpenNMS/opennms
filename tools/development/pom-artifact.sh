#!/bin/sh -e

for POM in "$@"; do
	xmllint --xpath 'concat(/*[local-name()="project"]/*[local-name()="groupId"]/text(), ":", /*[local-name()="project"]/*[local-name()="artifactId"]/text())' "$POM"
	echo ""
done
