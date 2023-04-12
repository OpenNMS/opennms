#!/bin/bash


export VELOCLOUD_VERSION="latest"
export DEPLOY_FOLDER="/opt/usr-plugins"

mkdir $DEPLOY_FOLDER 

apt-get update
apt-get install -y wget curl jq

cd $DEPLOY_FOLDER || exit 
if [ $VELOCLOUD_VERSION == "latest" ]
then
 artifact_urls=$(curl --silent https://api.github.com/repos/OpenNMS/opennms-velocloud-plugin/releases | jq -r '.[0].assets[0].browser_download_url')
else
 artifact_urls=$(curl --silent https://api.github.com/repos/OpenNMS/opennms-velocloud-plugin/releases | jq -r '.[] | select(.tag_name=="$VELOCLOUD_VERSION") | .assets[0].browser_download_url')
fi
if [ -n "$artifact_urls" ]; then
 for url in $artifact_urls; do
    wget "$url"
 done
fi
