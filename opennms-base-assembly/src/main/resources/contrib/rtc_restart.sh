#!/bin/sh

wget --proxy=off -O /dev/null "http://manager:manager@localhost:8181/invoke?objectname=OpenNMS%3AName%3DRtcd&operation=stop"
wget --proxy=off -O /dev/null "http://manager:manager@localhost:8181/invoke?objectname=OpenNMS%3AName%3DRtcd&operation=init"
wget --proxy=off -O /dev/null "http://manager:manager@localhost:8181/invoke?objectname=OpenNMS%3AName%3DRtcd&operation=start"
