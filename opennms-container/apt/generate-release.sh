#!/bin/bash

set -e

CODE_NAME=$1

if [ -z "$CODE_NAME" ]; then
  >&2 echo "Please pass code name. e.g. $0 opennms-29"
  exit
fi

do_hash() {
    HASH_NAME=$1
    HASH_CMD=$2
    echo "${HASH_NAME}:"
    for f in $(find -type f); do
        f=$(echo $f | cut -c3-) # remove ./ prefix
        if [ "$f" = "Release" ]; then
            continue
        fi
        echo " $(${HASH_CMD} ${f}  | cut -d" " -f1) $(wc -c $f)"
    done
}

cat << EOF
Architectures: all
Codename: $CODE_NAME
Description: OpenNMS Repository - $CODE_NAME
Label: OpenNMS Repository - $CODE_NAME
Origin: OpenNMS
Suite: $CODE_NAME
Date: $(date -Ru)
EOF
do_hash "MD5Sum" "md5sum"
do_hash "SHA1" "sha1sum"
do_hash "SHA256" "sha256sum"
