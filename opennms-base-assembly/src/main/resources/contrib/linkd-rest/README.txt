linkd-rest.pl is a sample perl script using curl to add links between existent nodes in the new OpenNMS topology

It takes a file in the following format as input:

<Links>
<Link>
        <AEND deviceName="austin-mx80" interface="ge-0/0/0"/>
        <ZEND deviceName="phoenix-mx80" interface="ge-1/0/0"/>
</Link>
</Links>

Where deviceName is the device label and the interface is the ifDescr field
for the particular. And it generates an XML that is the format used by
the Linkd REST API and sends it out.

Usage:

linkd-rest.pl <source_xml>|-delete=<id>  [(http|https)://opennms_server_ip:port]

Where:

<source_xml>: Is a file in the format above
<id>: It is the link id from the datalinkinterface table and it is returned
with a GET call to opennms/rest/links.

You either pass a file in which case the script will try to add all links to
the DB (will skip dups) or pass -delete=<id> in which case it will remove the
link referred by that id (make sure to not add any space when removing)
