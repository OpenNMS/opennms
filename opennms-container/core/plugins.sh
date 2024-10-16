#!/bin/bash

export ALEC_VERSION="latest"
export CORTEX_VERSION="latest"
export VELOCLOUD_VERSION="latest"

export DEPLOY_FOLDER="/opt/usr-plugins"

mkdir $DEPLOY_FOLDER 

apt-get update
apt-get install -y python3-pip wget curl jq
pip3 install --upgrade cloudsmith-cli 

mkdir ~/test
cd ~/test || exit
artifact_urls=$(cloudsmith list packages --query="opennms-alec-plugin version:$ALEC_VERSION format:deb" opennms/common -F json  | jq -r '.data[].cdn_url')
for url in $artifact_urls; do 
 wget "$url"
done
dpkg-deb -R *-alec-plugin_*_all.deb ./
find . -name '*.kar' -exec mv {} $DEPLOY_FOLDER \;

cd ~/ || exit
rm -rf test

cd $DEPLOY_FOLDER || exit 
if [ $CORTEX_VERSION == "latest" ]
then
 artifact_urls=$(curl --silent https://api.github.com/repos/OpenNMS/opennms-cortex-tss-plugin/releases | jq -r '.[0].assets[0].browser_download_url')
else
 artifact_urls=$(curl --silent https://api.github.com/repos/OpenNMS/opennms-cortex-tss-plugin/releases | jq -r '.[] | select(.tag_name=="$CORTEX_VERSION") | .assets[0].browser_download_url')
fi
if [ -n "$artifact_urls" ]; then
 for url in $artifact_urls; do
    wget "$url"
 done
fi

cd $DEPLOY_FOLDER || exit 
if [ $VELOCLOUD_VERSION == "latest" ]
then
 artifact_urls=$(curl --silent https://api.github.com/repos/OpenNMS/opennms-velocloud-plugin/releases | jq -r '.[0].assets[0].browser_download_url')
else
 artifact_urls=$(curl --silent https://api.github.com/repos/OpenNMS/opennms-velocloud-plugin/releases | jq -r '.[] | select(.tag_name=="$VELOCLOUD_VERSION") | .assets[0].browser_download_url')
fi
if [ -n "$artifact_urls"  ]; then
 for url in $artifact_urls; do
    wget "$url"
 done
fi
