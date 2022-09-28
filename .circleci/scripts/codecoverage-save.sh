#!/bin/sh -e
cd ~/project
mkdir -p ~/code-coverage
find . -type d | grep -E ".*/target$" | zip ~/code-coverage/target-"$CIRCLE_NODE_INDEX.zip" -9r@ -x \*.gz -x \*.zip -x \*.war -x \*target/dist\* -x \*/node/\* -x \*target/unpacked/\*
