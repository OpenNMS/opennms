#!/bin/sh

set -e

MYDIR="$(dirname "$0")"
cd "$MYDIR"

docker build -t opennms-yum-server .
