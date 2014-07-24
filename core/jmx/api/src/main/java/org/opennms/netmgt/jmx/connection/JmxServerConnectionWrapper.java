package org.opennms.netmgt.jmx.connection;

import javax.management.MBeanServerConnection;
import java.io.Closeable;

public interface JmxServerConnectionWrapper extends Closeable {

    MBeanServerConnection getMBeanServerConnection();

    @Override
    void close();
}
