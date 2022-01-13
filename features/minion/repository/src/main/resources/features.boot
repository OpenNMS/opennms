# Minion Default Features
pax-war
# Install specific versions of the spring and spring-jms features to
# reduce the computations needed by the bundle dependency resolver
spring/${springVersion}
spring-jms/${springVersion}
opennms-core-ipc-jms
opennms-core-ipc-twin-shell
opennms-syslogd-listener-camel-netty
opennms-trapd-listener
opennms-events-sink-dispatcher
opennms-send-event-command
opennms-dnsresolver-netty
minion-shell
minion-heartbeat-producer
minion-snmp-proxy
minion-provisiond-detectors
minion-provisiond-requisitions
minion-poller
minion-collection
minion-icmp-proxy
minion-telemetryd-receivers
opennms-core-ipc-sink-offheap
# Default SCV implementation
scv-jceks-impl
scv-shell
minion-rest-service
minion-health-check
minion-api-layer
