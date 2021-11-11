# Poller Configuration

## Website monitoring

```
<service name="Web-Site-Monitor" interval="300000" user-defined="true" status="on">
   <pattern><![CDATA[^Web:.*$]]></pattern>
   <parameter key="retry" value="1" />
   <parameter key="timeout" value="3000" />
   <parameter key="port" value="${requisition:port|443}" />
   <parameter key="host-name" value="${requisition:vhost}" />
   <parameter key="url" value="${requisition:path|/}" />
   <parameter key="response" value="${requisition:response|200}" />
   <parameter key="rrd-repository" value="/opt/opennms/share/rrd/response" />
   <parameter key="rrd-base-name" value="http-${requisition:vhost}" />
   <parameter key="ds-name" value="${service:name}" />
</service>

<monitor service="Web-Site-Monitor" class-name="org.opennms.netmgt.poller.monitors.HttpsMonitor"/>(9)
```
