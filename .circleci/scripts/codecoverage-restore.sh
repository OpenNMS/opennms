#!/bin/sh -e
cd ~/project
for file in ~/code-coverage/target-*.zip; do
    unzip -qo "$file"
done
