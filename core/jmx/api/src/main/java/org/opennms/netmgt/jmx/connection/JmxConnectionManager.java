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

package org.opennms.netmgt.jmx.connection;

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
     * @param ipAddress            the address to connecto to
     * @param connectionProperties properties for the connection (e.g. port, user, etc.)
     * @param retryCallback        A callback, which should be called BEFORE creating the connection. May be null.
     * @return A JmxServerConnectionWrapper for the MBeanServerConnection.
     * @throws JmxServerConnectionException if the connection to the given ipAddress using the registered JmxServerConnector could not be established.
     */
    JmxServerConnectionWrapper connect(String connectionName, String ipAddress, Map<String, String> connectionProperties, RetryCallback retryCallback) throws JmxServerConnectionException;

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
