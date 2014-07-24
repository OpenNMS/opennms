package org.opennms.netmgt.jmx.connection;

import java.util.Map;

public interface JmxServerConnector {

    JmxServerConnectionWrapper createConnection(final String address, final Map<String, String> propertiesMap) throws JmxServerConnectionException;
}
