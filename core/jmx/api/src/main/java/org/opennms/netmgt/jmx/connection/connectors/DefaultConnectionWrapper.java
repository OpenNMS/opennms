package org.opennms.netmgt.jmx.connection.connectors;


import org.opennms.netmgt.jmx.connection.WiuConnectionWrapper;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import java.io.IOException;

class DefaultConnectionWrapper implements WiuConnectionWrapper {

    private JMXConnector connector;
    private MBeanServerConnection connection;

    protected DefaultConnectionWrapper(JMXConnector connector, MBeanServerConnection connection) {
        this.connector = connector;
        this.connection = connection;
    }

    @Override
    public MBeanServerConnection getMBeanServerConnection() {
        if (connection == null) {
            // TODO mvr what to do in this case?
//            throw new WiuDefaultJmxCollector.MBeanServerConnectionException("Connection has not been established or was closed.");
        }
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
