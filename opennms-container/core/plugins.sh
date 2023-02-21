#!/bin/bash

. common.sh

installCloudSmith

mkdir ~/test
cd ~/test || exit
urls=$(cloudsmith list packages --query="opennms-alec-plugin tag:latest" opennms/common -F json  | jq -r '.data[].cdn_url')
for url in $urls; do 
 wget "$url"
done
dpkg-deb -R *-alec-plugin_*_all.deb ./
find . -name '*.kar' -exec mv {} /usr/share/opennms/deploy \;


urls=$(cloudsmith list packages --query="opennms-plugin-cloud tag:latest" opennms/common -F json  | jq -r '.data[].cdn_url')
for url in $urls; do
    wget "$url"
done
dpkg-deb -R *-plugin-cloud_*_all.deb ./
find . -name '*.kar' -exec mv {} /usr/share/opennms/deploy \;

cd ..
rm -r test

cd /usr/share/opennms/deploy || exit 
urls=$(curl --silent https://api.github.com/repos/OpenNMS/opennms-cortex-tss-plugin/releases | jq -r '.[0].assets[0].browser_download_url')
for url in $urls; do
    wget "$url"
done

cleanUp