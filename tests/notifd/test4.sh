#!/bin/bash

/opt/OpenNMS/bin/send-event.pl http://uei.opennms.org/products/bluebird/nodes/nodeLostService -n 21 -i 192.168.0.197 -s FTP
