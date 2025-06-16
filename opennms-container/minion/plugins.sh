#!/bin/bash

set -euo pipefail
trap 's=$?; echo "$0: Error on line "$LINENO": $BASH_COMMAND"; exit $s' ERR
IFS=$'\n\t'

export VELOCLOUD_VERSION="latest"
export DEPLOY_FOLDER="/opt/usr-plugins"

mkdir -p "$DEPLOY_FOLDER"

microdnf -y install jq

cd $DEPLOY_FOLDER || exit 
if [ $VELOCLOUD_VERSION == "latest" ]
then
 artifact_urls=$(curl -sS https://api.github.com/repos/OpenNMS/opennms-velocloud-plugin/releases | jq -r '.[0].assets[0].browser_download_url')
else
 artifact_urls=$(curl -sS https://api.github.com/repos/OpenNMS/opennms-velocloud-plugin/releases | jq -r '.[] | select(.tag_name=="$VELOCLOUD_VERSION") | .assets[0].browser_download_url')
fi
if [ -n "$artifact_urls" ]; then
 for url in $artifact_urls; do
    curl -sS -L -O "$url"
 done
fi
