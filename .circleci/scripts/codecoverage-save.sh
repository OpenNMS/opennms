#!/bin/bash

TYPE="$1"; shift
[ -z "$TYPE" ] && TYPE="coverage"

set -e
set -o pipefail

mkdir -p ~/code-coverage

find . -type f '!' -path './.git/*' '!' -path '*/node_modules/*' '!' -name '*.jar' '!' -name '*.sh' | grep -E '(surefire-reports|failsafe-reports|jacoco|coverage)' > /tmp/coverage-files.txt
if [ -s /tmp/coverage-files.txt ]; then
  zip '-9@' ~/code-coverage/${TYPE}-"${CIRCLE_NODE_INDEX}.zip" < /tmp/coverage-files.txt
fi
