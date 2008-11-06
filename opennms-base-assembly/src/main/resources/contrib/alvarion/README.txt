In order to get link quality information from Alvarion (http://www.alvarion.com/) 
Breeze Access wireless devices an snmpset has to be periodically sent. 
The included scripts perform this action.

The Link Quality indicator (baVLReadUpLinkQu) is actually the result of a
calculation that must be triggered by writing to a snmp variable. The
calculation itself takes about 10 seconds and the result is stored in
baVLReadUpLinkQu.

This script will attempt this against any device with an Alvarion nodesysoid
in the database.

