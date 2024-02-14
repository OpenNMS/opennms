#!/bin/bash

export ALEC_VERSION="latest"
#export DEPLOY_FOLDER="/usr/share/opennms/deploy" 
export DEPLOY_FOLDER="/opt/usr-plugins"

mkdir -p "$DEPLOY_FOLDER"

microdnf -y install cpio python3-pip jq
pip3 install --upgrade cloudsmith-cli 

mkdir ~/test
cd ~/test || exit
urls=$(cloudsmith list packages --query="sentinel-alec-plugin version:$ALEC_VERSION format:rpm" opennms/common -F json  | jq -r '.data[].cdn_url')
for url in $urls; do 
 curl -sS -L -O "$url"
done
rpm2cpio *-alec-plugin*.rpm | cpio -id
find . -name '*.kar' -exec mv '{}' "$DEPLOY_FOLDER" \;

cd ..
rm -r test
