#!/bin/bash -e

for file in */build_container_image.sh; do
	"$file"
done
