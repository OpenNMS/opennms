package org.opennms.netmgt.jmx.connection;

import java.util.Map;

public interface JmxConnectionManager {

    JmxServerConnectionWrapper connect(String connectionName, String ipAddress, Map<String, String> connectionProperties, RetryCallback retryCallback) throws JmxServerConnectionException;

    interface RetryCallback {
        void onRetry();
    }
}
