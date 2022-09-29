#!/bin/bash

set -e
set -o pipefail

mkdir -p ~/code-coverage
cd ~/project

find . -type f '!' -path './.git/*' | grep -E '(surefire-reports|failsafe-reports|jacoco)' > /tmp/coverage-files.txt
if [ -s /tmp/coverage-files.txt ]; then
  zip '-9@' ~/code-coverage/target-"${CIRCLE_NODE_INDEX}.zip" < /tmp/coverage-files.txt
fi
