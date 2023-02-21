#!/bin/bash

installCloudSmith(){
    apt-get update
    apt-get install -y python3-pip wget curl jq
    pip3 install --upgrade cloudsmith-cli 
}

cleanUp(){
    apt-get remove -y python3-pip wget curl jq 
    apt-get clean
    rm -rf /var/cache/apt /var/lib/apt/lists/* /tmp/security.sources.list 
    rm -rf ~/.cache/pip
}