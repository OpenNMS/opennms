/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.jmx.impl.connection.connectors;

import org.opennms.netmgt.jmx.connection.JmxConnectionManager;
import org.opennms.netmgt.jmx.connection.JmxConnectors;
import org.opennms.netmgt.jmx.connection.JmxServerConnectionException;
import org.opennms.netmgt.jmx.connection.JmxServerConnectionWrapper;
import org.opennms.netmgt.jmx.connection.JmxServerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles the establishing of a connection to the jmx server.
 * Also implements a retry mechanism.
 *
 */
public class DefaultConnectionManager implements JmxConnectionManager {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultConnectionManager.class);

    /**
     * The dummy RetryCallback, if no callback is defined.
     */
    private static final RetryCallback NULL_CALLBACK = new RetryCallback() {
        @Override
        public void onRetry() {
            // do nothing
        }
    } ;

    /**
     * All known connectors ({@link org.opennms.netmgt.jmx.connection.JmxConnectors}
     * and its implementation {@link org.opennms.netmgt.jmx.connection.JmxServerConnector}.
     */
    private final Map<String, JmxServerConnector> connectorMap = new HashMap<>();

    /**
     * Number of retries: how many times should establishing of a connection retried, until failure.
     */
    private final int retries;

    /**
     *
     * @param retryCount Any value >= 1. If <= 0, 3 is used.
     */
    public DefaultConnectionManager(int retryCount) {
        retries = retryCount <= 0 ? 3 : retryCount;
        connectorMap.put(JmxConnectors.JSR160, new Jsr160MBeanServerConnector());
        connectorMap.put(JmxConnectors.MX4J, new MX4JMBeanServerConnector());
        connectorMap.put(JmxConnectors.JBOSS, new JBossMBeanServerConnector());
        connectorMap.put(JmxConnectors.JMX_SECURE, new JMXSecureMBeanServerConnector());
        connectorMap.put(JmxConnectors.PLATFORM, new PlatformMBeanServerConnector());
    }

    /**
     * Same as {@link #DefaultConnectionManager(int)} with a <code>retryCount = 3</code>.
     */
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

    /**
     * Access-Method for the {@link #connectorMap}.
     *
     * @param connectorName
     * @return
     * @throws JmxServerConnectionException
     */
    public JmxServerConnector getConnector(String connectorName) throws JmxServerConnectionException {
        if (!connectorMap.containsKey(connectorName)) {
            throw new JmxServerConnectionException("No Connector available for connection name '" + connectorName + "'");
        }
        final JmxServerConnector connector = connectorMap.get(connectorName);
        return connector;
    }
}
