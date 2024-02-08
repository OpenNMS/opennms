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
package org.opennms.netmgt.jmx.connection;

import java.net.InetAddress;
import java.util.Map;

/**
 * The connection manager is responsible to create a
 * {@link org.opennms.netmgt.jmx.connection.JmxServerConnector}
 * implementation according to the connectionName and establish a connection using that server connector.
 * <p/>
 * If no {@link org.opennms.netmgt.jmx.connection.JmxServerConnector} could be used for a given
 * connectionName the error handling is up to the implementation.
 * <p/>
 * If a connection to a JMX Server could not be established the {@link JmxConnectionManager} may try again.
 * If a connection manager supports retries is up to the implementation.
 */
public interface JmxConnectionManager {

    /**
     * Connects to the given <code>ipAddress</code> using the
     * {@link org.opennms.netmgt.jmx.connection.JmxServerConnector} registered with <code>connectionString</code>.
     * <p/>
     * If the connection to the server could not be established (e.g. no retries left) a JmxServerConnectionException is thrown.
     *
     * @param connectionName       The {@link org.opennms.netmgt.jmx.connection.JmxConnectors} name of the connection. May be null.
     * @param ipAddress            the address to connect to
     * @param connectionProperties properties for the connection (e.g. port, user, etc.)
     * @param retryCallback        A callback, which should be called BEFORE creating the connection. May be null.
     * @return A JmxServerConnectionWrapper for the MBeanServerConnection.
     * @throws JmxServerConnectionException if the connection to the given ipAddress using the registered JmxServerConnector could not be established.
     */
    JmxServerConnectionWrapper connect(JmxConnectors connectionName, InetAddress ipAddress, Map<String, String> connectionProperties, RetryCallback retryCallback) throws JmxServerConnectionException;

    /**
     * This callback should always be invoked BEFORE invoking
     * the {@link org.opennms.netmgt.jmx.connection.JmxServerConnector#createConnection(String, java.util.Map)}
     * method.
     * <p/>
     * It may contain additional logic when a retry is made (e.g. reset a timer)
     */
    interface RetryCallback {
        void onRetry();
    }
}
