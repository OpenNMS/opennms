#!/bin/bash -e

for file in */build_container_image.sh; do
	DIR="$(dirname "$file")"
	pushd "$DIR"
		./build_container_image.sh
	popd
done
