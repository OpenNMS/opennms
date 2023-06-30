#!/bin/bash

set -euo pipefail
IFS=$'\n\t'

export ALEC_VERSION="latest"
export CLOUD_VERSION="latest"
export CORTEX_VERSION="latest"
export VELOCLOUD_VERSION="latest"

export DEPLOY_FOLDER="/opt/usr-plugins"

mkdir -p "$DEPLOY_FOLDER"

microdnf -y install cpio python3-pip jq
pip3 install --upgrade cloudsmith-cli 

mkdir ~/test
cd ~/test || exit
artifact_urls=$(cloudsmith list packages --query="opennms-alec-plugin version:$ALEC_VERSION format:rpm" opennms/common -F json  | jq -r '.data[].cdn_url')
for url in $artifact_urls; do 
 curl -sS -L -O "$url"
done
rpm2cpio *-alec-plugin*.rpm | cpio -id
find . -name '*.kar' -exec mv '{}' "$DEPLOY_FOLDER" \;

cd ~/ || exit
rm -rf test
mkdir ~/test
cd ~/test || exit

artifact_urls=$(cloudsmith list packages --query="opennms-plugin-cloud version:$CLOUD_VERSION format:rpm" opennms/common -F json  | jq -r '.data[].cdn_url')
for url in $artifact_urls; do
    curl -sS -L -O "$url"
done
rpm2cpio *-plugin-cloud*.rpm | cpio -id
find . -name '*.kar' -exec mv '{}' "$DEPLOY_FOLDER" \;

cd ..
rm -r test

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
