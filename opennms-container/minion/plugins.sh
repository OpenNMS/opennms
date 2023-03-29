#!/bin/bash

export VELOCLOUD_VERSION="latest"

#export DEPLOY_FOLDER="/usr/share/opennms/deploy" 
export DEPLOY_FOLDER="/opt/usr-plugins"

mkdir $DEPLOY_FOLDER 

apt-get update
apt-get install -y python3-pip wget curl jq
pip3 install --upgrade cloudsmith-cli 

cd $DEPLOY_FOLDER || exit 
if [ $VELOCLOUD_VERSION == "latest" ]
then
 urls=$(curl --silent https://api.github.com/repos/OpenNMS/opennms-velocloud-plugin/releases | jq -r '.[0].assets[0].browser_download_url')
else
 urls=$(curl --silent https://api.github.com/repos/OpenNMS/opennms-velocloud-plugin/releases | jq -r '.[] | select(.tag_name=="$CORTEX_TAG") | .assets[0].browser_download_url')
fi
for url in $urls; do
    wget "$url"
done
