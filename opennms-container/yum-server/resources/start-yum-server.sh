#!/bin/sh

set -e

echo "=== creating repo in /repo ==="
createrepo /repo
echo ""

echo "=== repo files ==="
find /repo -type f | sort -u
echo ""

echo "=== running lighttpd ==="
lighttpd -f /etc/httpd.conf -p
exec lighttpd -D -f /etc/httpd.conf
