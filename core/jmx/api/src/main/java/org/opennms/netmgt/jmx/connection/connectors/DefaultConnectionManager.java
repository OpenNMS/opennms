package org.opennms.netmgt.jmx.connection.connectors;

import org.opennms.netmgt.jmx.connection.ConnectionManager;
import org.opennms.netmgt.jmx.connection.Connections;
import org.opennms.netmgt.jmx.connection.MBeanServerConnectionException;
import org.opennms.netmgt.jmx.connection.MBeanServerConnector;
import org.opennms.netmgt.jmx.connection.WiuConnectionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DefaultConnectionManager implements ConnectionManager {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultConnectionManager.class);

    private static final RetryCallback NULL_CALLBACK = new RetryCallback() {
        @Override
        public void onRetry() {
            // do nothing
        }
    } ;

    private final Map<String, MBeanServerConnector> connectorMap = new HashMap<>();

    private final int retries;

    public DefaultConnectionManager(int retryCount) {
        retries = retryCount <= 0 ? 3 : retryCount;
        connectorMap.put(Connections.JSR160, new Jsr160MBeanServerConnector());
        connectorMap.put(Connections.MX4J, new MX4JMBeanServerConnector());
        connectorMap.put(Connections.JBOSS, new JBossMBeanServerConnector());
        connectorMap.put(Connections.JMX_SECURE, new JMXSecureMBeanServerConnector());
    }

    @Override
    public WiuConnectionWrapper connect(String connectionName, String ipAddress, Map<String, String> properties, RetryCallback retryCallback) throws MBeanServerConnectionException {
        if (!connectorMap.containsKey(connectionName)) {
            throw new MBeanServerConnectionException("No Connector available for connection name '" + connectionName + "'");
        }

        // if null, use dummy implementation
        if (retryCallback == null) {
            retryCallback = NULL_CALLBACK;
        }

        MBeanServerConnectionException lastException = null;
        final MBeanServerConnector connector = connectorMap.get(connectionName);
        for (int i = 0; i < retries; i++) {
            LOG.debug("{}/{}: Try connecting to {}", (i+1), retries, ipAddress);
            retryCallback.onRetry();
            try {
                WiuConnectionWrapper connectionWrapper = connector.createConnection(ipAddress, properties);
                if (connectionWrapper == null) {
                    throw new MBeanServerConnectionException("Received null connection");
                }
                return connectionWrapper;
            } catch (MBeanServerConnectionException mbex) {
                LOG.debug("Connection could not be established", mbex);
                lastException = mbex;
            }
        }

        if (lastException != null) {
            throw lastException;
        }
        throw new MBeanServerConnectionException("Connection could not be established. Reason: No retries left.");
    }
}
