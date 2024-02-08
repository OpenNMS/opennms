/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.jmx.impl.connection.connectors;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.jmx.connection.JmxConnectionManager;
import org.opennms.netmgt.jmx.connection.JmxConnectors;
import org.opennms.netmgt.jmx.connection.JmxServerConnectionException;
import org.opennms.netmgt.jmx.connection.JmxServerConnectionWrapper;
import org.opennms.netmgt.jmx.connection.JmxServerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final Map<JmxConnectors, JmxServerConnector> connectorMap = new HashMap<>();

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
        connectorMap.put(JmxConnectors.DEFAULT, new DefaultJmxConnector());
        connectorMap.put(JmxConnectors.jsr160, new DefaultJmxConnector());
        connectorMap.put(JmxConnectors.platform, new PlatformMBeanServerConnector());
    }

    /**
     * Same as {@link #DefaultConnectionManager(int)} with a <code>retryCount = 3</code>.
     */
    public DefaultConnectionManager() {
        this(1);
    }

    @Override
    public JmxServerConnectionWrapper connect(JmxConnectors connectorName, InetAddress ipAddress, Map<String, String> properties, RetryCallback retryCallback) throws JmxServerConnectionException {
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
    public JmxServerConnector getConnector(JmxConnectors connectorName) throws JmxServerConnectionException {
        if (!connectorMap.containsKey(connectorName)) {
            throw new JmxServerConnectionException("No Connector available for connection name '" + connectorName + "'");
        }
        final JmxServerConnector connector = connectorMap.get(connectorName);
        return connector;
    }
}
