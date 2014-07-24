package org.opennms.netmgt.jmx.connection.connectors;

import org.opennms.netmgt.jmx.connection.MBeanServerConnectionException;
import org.opennms.netmgt.jmx.connection.MBeanServerConnector;
import org.opennms.netmgt.jmx.connection.WiuConnectionWrapper;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import java.lang.management.ManagementFactory;
import java.util.Map;

public class PlatformMBeanServerConnector implements MBeanServerConnector {
    @Override
    public WiuConnectionWrapper createConnection(String address, Map<String, String> propertiesMap) throws MBeanServerConnectionException {
        final MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        final WiuConnectionWrapper jmxConnectionWrapper = new WiuConnectionWrapper() {
            @Override
            public MBeanServerConnection getMBeanServerConnection() {
                return platformMBeanServer;
            }

            @Override
            public void close() {

            }
        };

        return jmxConnectionWrapper;
    }
}
