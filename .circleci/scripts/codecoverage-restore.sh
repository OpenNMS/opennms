#!/bin/sh -e
cd ~/project
for file in $(ls -1 ~/code-coverage/target-*.zip); do
    unzip -qo $file
done
