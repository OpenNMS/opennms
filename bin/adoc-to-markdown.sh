#!/bin/bash

if [ -z "$1" ]; then
	echo "usage: $0 path/to/file.adoc"
	exit 1
fi

asciidoc -b docbook -o - "$1" | \
	iconv -t utf-8 | \
	pandoc -f docbook -t markdown_strict --wrap=none | \
	sed -e :a -e '$!N;s/\n- /- /;ta' -e 'P;D' | \
	iconv -f utf-8 > /tmp/markdown.$$
less /tmp/markdown.$$
rm /tmp/markdown.$$
