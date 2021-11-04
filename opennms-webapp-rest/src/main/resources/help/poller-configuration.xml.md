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

(1) We call the test `Web-Site-Monitor` and it runs every 5 minutes (300000 ms)
(2) The test is applied on all service tests that match the regexp pattern `^Web:.*$`, e.g., our provisoned services `Web:opennms`, `Web:wiki`, `Web:planet`)
(3) We replace the value here with the key `port` from requisition context
(4) Replacement for the value `vhost` from the requisition context
(5) Replacement for the value `path` from the requisition context
(6) Replacement for the value `response` from the requisition context and use `200` as default in case no `response` key is set
(7)(8) Replacement for the value `vhost` and `service name` to have unique response times for each web test
(9) Associate our HttpsMonitor with our `Web-Site-Monitor`
