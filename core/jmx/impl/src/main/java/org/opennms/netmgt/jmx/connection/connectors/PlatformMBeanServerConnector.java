package org.opennms.netmgt.jmx.connection.connectors;

import org.opennms.netmgt.jmx.connection.JmxServerConnectionException;
import org.opennms.netmgt.jmx.connection.JmxServerConnectionWrapper;
import org.opennms.netmgt.jmx.connection.JmxServerConnector;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import java.lang.management.ManagementFactory;
import java.util.Map;

public class PlatformMBeanServerConnector implements JmxServerConnector {
    @Override
    public JmxServerConnectionWrapper createConnection(String address, Map<String, String> propertiesMap) throws JmxServerConnectionException {
        final MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        final JmxServerConnectionWrapper jmxConnectionWrapper = new JmxServerConnectionWrapper() {
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
