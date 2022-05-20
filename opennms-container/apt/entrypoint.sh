#!/bin/bash 

# Exit immediately if anything returns non-zero
set -e

# Exit script if a statement returns a non-true return value.
set -o errexit

# Use the error status of the first failure, rather than that of the last item in a pipeline.
set -o pipefail

BASE_PATH=$1
PORT=$2

echo "BASE_PATH=${BASE_PATH} PORT=${PORT}"

apt update
apt install -y --no-install-recommends dpkg-dev pgp busybox

# make sure nothing exist
rm -f $BASE_PATH/dists/stable/Release $BASE_PATH/dists/stable/InRelease

cd $BASE_PATH
dpkg-scanpackages dists/stable/main/binary-all > dists/stable/main/binary-all/Packages 

cd $BASE_PATH/dists/stable
$BASE_PATH/generate-release.sh stable > Release

echo "Sign Release by temp gpg"
export GNUPGHOME="$(mktemp -d /tmp/pgpkeys-XXXXXX)"
gpg --no-tty --batch --gen-key ${BASE_PATH}/temp-pgp-key.batch
gpg --armor --export tempkey > ${BASE_PATH}/pgp-key.public
cat Release | gpg --default-key tempkey -abs --clearsign > InRelease

echo "Start apt server @ $PORT"
cd $BASE_PATH
busybox httpd -p $PORT -f
