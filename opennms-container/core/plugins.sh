#!/bin/bash

export ALEC_VERSION="latest"
export CLOUD_VERSION="latest"
export CORTEX_VERSION="latest"
#export DEPLOY_FOLDER="/usr/share/opennms/deploy" 
export DEPLOY_FOLDER="/opt/usr-plugins"

mkdir $DEPLOY_FOLDER 

apt-get update
apt-get install -y python3-pip wget curl jq
pip3 install --upgrade cloudsmith-cli 

mkdir ~/test
cd ~/test || exit
urls=$(cloudsmith list packages --query="opennms-alec-plugin version:$ALEC_VERSION format:deb" opennms/common -F json  | jq -r '.data[].cdn_url')
for url in $urls; do 
 wget "$url"
done
dpkg-deb -R *-alec-plugin_*_all.deb ./
find . -name '*.kar' -exec mv {} $DEPLOY_FOLDER \;


urls=$(cloudsmith list packages --query="opennms-plugin-cloud version:$CLOUD_VERSION format:deb" opennms/common -F json  | jq -r '.data[].cdn_url')
for url in $urls; do
    wget "$url"
done
dpkg-deb -R *-plugin-cloud_*_all.deb ./
find . -name '*.kar' -exec mv {} $DEPLOY_FOLDER \;

cd ..
rm -r test

cd $DEPLOY_FOLDER || exit 
if [ $CORTEX_VERSION == "latest" ]
then
 urls=$(curl --silent https://api.github.com/repos/OpenNMS/opennms-cortex-tss-plugin/releases | jq -r '.[0].assets[0].browser_download_url')
else
 urls=$(curl --silent https://api.github.com/repos/OpenNMS/opennms-cortex-tss-plugin/releases | jq -r '.[] | select(.tag_name=="$CORTEX_TAG") | .assets[0].browser_download_url')
fi
for url in $urls; do
    wget "$url"
done

apt-get remove -y python3-pip wget curl jq 
apt-get clean
rm -rf /var/cache/apt /var/lib/apt/lists/* /tmp/security.sources.list 
rm -rf ~/.cache/pip
