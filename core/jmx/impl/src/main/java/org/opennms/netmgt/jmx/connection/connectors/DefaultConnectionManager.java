package org.opennms.netmgt.jmx.connection.connectors;

import org.opennms.netmgt.jmx.connection.JmxConnectionManager;
import org.opennms.netmgt.jmx.connection.JmxConnectors;
import org.opennms.netmgt.jmx.connection.JmxServerConnectionException;
import org.opennms.netmgt.jmx.connection.JmxServerConnectionWrapper;
import org.opennms.netmgt.jmx.connection.JmxServerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DefaultConnectionManager implements JmxConnectionManager {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultConnectionManager.class);

    private static final RetryCallback NULL_CALLBACK = new RetryCallback() {
        @Override
        public void onRetry() {
            // do nothing
        }
    } ;

    private final Map<String, JmxServerConnector> connectorMap = new HashMap<>();

    private final int retries;

    public DefaultConnectionManager(int retryCount) {
        retries = retryCount <= 0 ? 3 : retryCount;
        connectorMap.put(JmxConnectors.JSR160, new Jsr160MBeanServerConnector());
        connectorMap.put(JmxConnectors.MX4J, new MX4JMBeanServerConnector());
        connectorMap.put(JmxConnectors.JBOSS, new JBossMBeanServerConnector());
        connectorMap.put(JmxConnectors.JMX_SECURE, new JMXSecureMBeanServerConnector());
        connectorMap.put(JmxConnectors.PLATFORM, new PlatformMBeanServerConnector());
    }

    public DefaultConnectionManager() {
        this(1);
    }

    @Override
    public JmxServerConnectionWrapper connect(String connectorName, String ipAddress, Map<String, String> properties, RetryCallback retryCallback) throws JmxServerConnectionException {
        // if null, use dummy implementation
        if (retryCallback == null) {
            retryCallback = NULL_CALLBACK;
        }

        JmxServerConnectionException lastException = null;
        final JmxServerConnector connector = getConnector(connectorName);
        for (int i = 0; i < retries; i++) {
            LOG.debug("{}/{}: Try connecting to {}", (i+1), retries, ipAddress);
            retryCallback.onRetry();
            try {
                JmxServerConnectionWrapper connectionWrapper = connector.createConnection(ipAddress, properties);
                if (connectionWrapper == null) {
                    throw new JmxServerConnectionException("Received null connection");
                }
                return connectionWrapper;
            } catch (JmxServerConnectionException ex) {
                LOG.debug("Connection could not be established", ex);
                lastException = ex;
            }
        }

        if (lastException != null) {
            throw lastException;
        }
        throw new JmxServerConnectionException("Connection could not be established. Reason: No retries left.");
    }

    public JmxServerConnector getConnector(String connectorName) throws JmxServerConnectionException {
        if (!connectorMap.containsKey(connectorName)) {
            throw new JmxServerConnectionException("No Connector available for connection name '" + connectorName + "'");
        }
        final JmxServerConnector connector = connectorMap.get(connectorName);
        return connector;
    }
}
