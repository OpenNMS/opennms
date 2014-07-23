package org.opennms.netmgt.jmx.connection;

import java.util.Map;

public interface MBeanServerConnector {

    WiuConnectionWrapper createConnection(final String address, final Map<String, String> propertiesMap) throws MBeanServerConnectionException;
}
