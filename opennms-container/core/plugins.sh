#!/bin/bash

set -euo pipefail
IFS=$'\n\t'

export CORTEX_VERSION="latest"
export VELOCLOUD_VERSION="latest"

export DEPLOY_FOLDER="/opt/usr-plugins"

mkdir -p "$DEPLOY_FOLDER"

microdnf -y install cpio python3-pip jq
pip3 install --upgrade cloudsmith-cli 

cd "$DEPLOY_FOLDER" || exit 
if [ "$CORTEX_VERSION" == "latest" ]
then
 artifact_urls=$(curl -sS https://api.github.com/repos/OpenNMS/opennms-cortex-tss-plugin/releases | jq -r '.[0].assets[0].browser_download_url')
else
 artifact_urls=$(curl -sS https://api.github.com/repos/OpenNMS/opennms-cortex-tss-plugin/releases | jq -r '.[] | select(.tag_name=="$CORTEX_VERSION") | .assets[0].browser_download_url')
fi
if [ -n "$artifact_urls" ]; then
 for url in $artifact_urls; do
    curl -sS -L -O "$url"
 done
fi

cd "$DEPLOY_FOLDER" || exit 
if [ "$VELOCLOUD_VERSION" == "latest" ]
then
 artifact_urls=$(curl -sS https://api.github.com/repos/OpenNMS/opennms-velocloud-plugin/releases | jq -r '.[0].assets[0].browser_download_url')
else
 artifact_urls=$(curl -sS https://api.github.com/repos/OpenNMS/opennms-velocloud-plugin/releases | jq -r '.[] | select(.tag_name=="$VELOCLOUD_VERSION") | .assets[0].browser_download_url')
fi
if [ -n "$artifact_urls"  ]; then
 for url in $artifact_urls; do
    curl -sS -L -O "$url"
 done
fi
