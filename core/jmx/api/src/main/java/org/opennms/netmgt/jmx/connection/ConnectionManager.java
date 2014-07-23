package org.opennms.netmgt.jmx.connection;

import java.util.Map;

public interface ConnectionManager {

    WiuConnectionWrapper connect(String connectionName, String ipAddress, Map<String, String> connectionProperties, RetryCallback retryCallback) throws MBeanServerConnectionException;

    interface RetryCallback {
        void onRetry();
    }
}
