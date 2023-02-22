#!/bin/bash

export ALEC_VERSION="latest"
export CLOUD_VERSION="latest"
#export DEPLOY_FOLDER="/usr/share/opennms/deploy" 
export DEPLOY_FOLDER="/opt/usr-plugins"

apt-get update
apt-get install -y python3-pip wget curl jq
pip3 install --upgrade cloudsmith-cli 

mkdir ~/test
cd ~/test || exit
urls=$(cloudsmith list packages --query="sentinel-alec-plugin version:$ALEC_VERSION format:deb" opennms/common -F json  | jq -r '.data[].cdn_url')
for url in $urls; do 
 wget "$url"
done
dpkg-deb -R *-alec-plugin_*_all.deb ./
find . -name '*.kar' -exec mv {} $DEPLOY_FOLDER \;


urls=$(cloudsmith list packages --query="sentinel-plugin-cloud version:$CLOUD_VERSION format:deb" opennms/common -F json  | jq -r '.data[].cdn_url')
for url in $urls; do
    wget "$url"
done
dpkg-deb -R *-plugin-cloud_*_all.deb ./
find . -name '*.kar' -exec mv {} $DEPLOY_FOLDER \;

cd ..
rm -r test
