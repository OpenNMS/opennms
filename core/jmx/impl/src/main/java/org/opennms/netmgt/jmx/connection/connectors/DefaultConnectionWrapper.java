package org.opennms.netmgt.jmx.connection.connectors;


import org.opennms.netmgt.jmx.connection.JmxServerConnectionWrapper;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.util.Objects;

class DefaultConnectionWrapper implements JmxServerConnectionWrapper {

    private JMXConnector connector;
    private MBeanServerConnection connection;

    protected DefaultConnectionWrapper(JMXConnector connector, MBeanServerConnection connection) {
        this.connector = Objects.requireNonNull(connector, "connector must not be null");
        this.connection = Objects.requireNonNull(connection, "connection must not be null");
    }

    @Override
    public MBeanServerConnection getMBeanServerConnection() {
        return connection;
    }

    @Override
    public void close() {
        if (connector != null) {
            try {
                connector.close();
            } catch (IOException e) {

            }
        }
        connection = null;
    }

}
