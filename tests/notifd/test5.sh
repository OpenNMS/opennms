#!/bin/bash

/opt/OpenNMS/bin/send-event.pl http://uei.opennms.org/nodes/nodeLostService -n 13 -i 192.168.0.188 -s Informix
